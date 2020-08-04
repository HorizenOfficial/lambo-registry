package io.horizen.lambo.car.api;

import akka.http.javadsl.server.Route;
import com.fasterxml.jackson.annotation.JsonView;
import com.horizen.api.http.ApiResponse;
import com.horizen.api.http.ApplicationApiGroup;
import com.horizen.api.http.ErrorResponse;
import com.horizen.api.http.SuccessResponse;
import com.horizen.box.RegularBox;
import com.horizen.box.data.RegularBoxData;
import com.horizen.companion.SidechainBoxesDataCompanion;
import com.horizen.companion.SidechainProofsCompanion;
import com.horizen.companion.SidechainTransactionsCompanion;
import io.horizen.lambo.car.api.request.SpendCarSellOrderRequest;
import io.horizen.lambo.car.api.request.CreateCarBoxRequest;
import io.horizen.lambo.car.api.request.CreateCarSellOrderRequest;
import io.horizen.lambo.car.box.CarBox;
import io.horizen.lambo.car.box.CarSellOrderBox;
import io.horizen.lambo.car.box.data.CarBoxData;
import io.horizen.lambo.car.info.CarBuyOrderInfo;
import io.horizen.lambo.car.info.CarSellOrderInfo;
import io.horizen.lambo.car.proof.SellOrderSpendingProof;
import io.horizen.lambo.car.transaction.BuyCarTransaction;
import io.horizen.lambo.car.transaction.CarDeclarationTransaction;
import io.horizen.lambo.car.transaction.SellCarTransaction;
import com.horizen.node.NodeMemoryPool;
import com.horizen.node.SidechainNodeView;
import com.horizen.proof.Signature25519;
import com.horizen.proposition.Proposition;
import com.horizen.proposition.PublicKey25519Proposition;
import com.horizen.proposition.PublicKey25519PropositionSerializer;
import com.horizen.secret.Secret;
import com.horizen.serialization.Views;
import com.horizen.transaction.BoxTransaction;
import com.horizen.utils.ByteArrayWrapper;
import com.horizen.utils.BytesUtils;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import scala.Option;
import scala.Some;

import com.horizen.box.Box;

import java.util.*;

public class CarApi extends ApplicationApiGroup {

    private final SidechainTransactionsCompanion sidechainTransactionsCompanion;
    private final SidechainBoxesDataCompanion sidechainBoxesDataCompanion;
    private final SidechainProofsCompanion sidechainProofsCompanion;

    public CarApi(SidechainTransactionsCompanion sidechainTransactionsCompanion,
                  SidechainBoxesDataCompanion sidechainBoxesDataCompanion,
                  SidechainProofsCompanion sidechainProofsCompanion) {
        this.sidechainTransactionsCompanion = sidechainTransactionsCompanion;
        this.sidechainBoxesDataCompanion = sidechainBoxesDataCompanion;
        this.sidechainProofsCompanion = sidechainProofsCompanion;
    }

    // Define the base path for API url
    @Override
    public String basePath() {
        return "carApi";
    }

    // Add routes to be processed by API server.
    @Override
    public List<Route> getRoutes() {
        List<Route> routes = new ArrayList<>();
        routes.add(bindPostRequest("createCar", this::createCar, CreateCarBoxRequest.class));
        routes.add(bindPostRequest("createCarSellOrder", this::createCarSellOrder, CreateCarSellOrderRequest.class));
        routes.add(bindPostRequest("acceptCarSellOrder", this::acceptCarSellOrder, SpendCarSellOrderRequest.class));
        routes.add(bindPostRequest("cancelCarSellOrder", this::cancelCarSellOrder, SpendCarSellOrderRequest.class));
        return routes;
    }

    /*
      Route to create car (register new car in the Sidechain).
      Input parameters are car properties and fee amount to pay.
      Route checks if the is enough regular box balance to pay fee and then creates CarDeclarationTransaction.
      Output of this transaction is new Car Box token.
      Returns the hex representation of the transaction.
    */
    private ApiResponse createCar(SidechainNodeView view, CreateCarBoxRequest ent) {
        try {
            // Parse the proposition of Car owner.
            PublicKey25519Proposition carOwnershipProposition = PublicKey25519PropositionSerializer.getSerializer()
                    .parseBytes(BytesUtils.fromHexString(ent.proposition));

            CarBoxData carBoxData = new CarBoxData(carOwnershipProposition, ent.vin, ent.year, ent.model, ent.color);

            // Try to collect regular boxes to pay fee
            List<Box<Proposition>> paymentBoxes = new ArrayList<>();
            long amountToPay = ent.fee;

            // Avoid to add boxes that are already spent in some Transaction that is present in node Mempool.
            List<byte[]> boxIdsToExclude = boxesFromMempool(view.getNodeMemoryPool());
            List<Box<Proposition>> regularBoxes = view.getNodeWallet().boxesOfType(RegularBox.class, boxIdsToExclude);
            int index = 0;
            while (amountToPay > 0 && index < regularBoxes.size()) {
                paymentBoxes.add(regularBoxes.get(index));
                amountToPay -= regularBoxes.get(index).value();
                index++;
            }

            if (amountToPay > 0) {
                throw new IllegalStateException("Not enough coins to pay the fee.");
            }

            // Set change if exists
            long change = Math.abs(amountToPay);
            List<RegularBoxData> regularOutputs = new ArrayList<>();
            if (change > 0)
                regularOutputs.add(new RegularBoxData((PublicKey25519Proposition) paymentBoxes.get(0).proposition(), change));


            // Create fake proofs to be able to create transaction to be signed.
            List<byte[]> inputIds = new ArrayList<>();
            for (Box b : paymentBoxes)
                inputIds.add(b.id());

            List fakeProofs = Collections.nCopies(inputIds.size(), null);
            Long timestamp = System.currentTimeMillis();

            CarDeclarationTransaction unsignedTransaction = new CarDeclarationTransaction(
                    inputIds,
                    fakeProofs,
                    regularOutputs,
                    carBoxData,
                    ent.fee,
                    timestamp);

            // Get the Tx message to be signed.
            byte[] messageToSign = unsignedTransaction.messageToSign();

            // Create signatures.
            List<Signature25519> proofs = new ArrayList<>();
            for (Box<Proposition> box : paymentBoxes) {
                proofs.add((Signature25519) view.getNodeWallet().secretByPublicKey(box.proposition()).get().sign(messageToSign));
            }

            // Create the resulting signed transaction.
            CarDeclarationTransaction signedTransaction = new CarDeclarationTransaction(
                    inputIds,
                    proofs,
                    regularOutputs,
                    carBoxData,
                    ent.fee,
                    timestamp);

            return new TxResponse(ByteUtils.toHexString(sidechainTransactionsCompanion.toBytes((BoxTransaction) signedTransaction)));
        }
        catch (Exception e) {
            return new CarResponseError("0102", "Error during Car declaration.", Some.apply(e));
        }
    }

    /*
      Route to create car sell order.
      Input parameters are Car Box id, sell price and fee amount to pay.
      Route checks if car box exists and then creates SellCarTransaction.
      Output of this transaction is new Car Sell Order token.
      Returns the hex representation of the transaction.
    */
    private ApiResponse createCarSellOrder(SidechainNodeView view, CreateCarSellOrderRequest ent) {
        try {
            // Tre to find CarBox to be opened in the closed boxes list
            CarBox carBox = null;

            for (Box b : view.getNodeWallet().boxesOfType(CarBox.class)) {
                if (Arrays.equals(b.id(), BytesUtils.fromHexString(ent.carBoxId)))
                    carBox = (CarBox) b;
            }

            if (carBox == null)
                throw new IllegalArgumentException("CarBox with given box id not found in the Wallet.");

            // Parse the proposition of the Car buyer.
            PublicKey25519Proposition carBuyerProposition = PublicKey25519PropositionSerializer.getSerializer()
                    .parseBytes(BytesUtils.fromHexString(ent.buyerProposition));

            // Try to collect regular boxes to pay fee
            List<Box<Proposition>> paymentBoxes = new ArrayList<>();
            long amountToPay = ent.fee;
            // Avoid to add boxes that are already spent in some Transaction that is present in node Mempool.
            List<byte[]> boxIdsToExclude = boxesFromMempool(view.getNodeMemoryPool());
            List<Box<Proposition>> regularBoxes = view.getNodeWallet().boxesOfType(RegularBox.class, boxIdsToExclude);
            int index = 0;
            while (amountToPay > 0 && index < regularBoxes.size()) {
                paymentBoxes.add(regularBoxes.get(index));
                amountToPay -= regularBoxes.get(index).value();
                index++;
            }

            if (amountToPay > 0) {
                throw new IllegalStateException("Not enough coins to pay the fee.");
            }

            // Set change if exists
            long change = Math.abs(amountToPay);
            List<RegularBoxData> regularOutputs = new ArrayList<>();
            if (change > 0)
                regularOutputs.add(new RegularBoxData((PublicKey25519Proposition) paymentBoxes.get(0).proposition(), change));

            List<byte[]> inputRegularBoxIds = new ArrayList<>();
            for (Box b : paymentBoxes)
                inputRegularBoxIds.add(b.id());

            // Create fake proofs to be able to create transaction to be signed.
            CarSellOrderInfo fakeSaleOrderInfo = new CarSellOrderInfo(carBox, null, ent.sellPrice, carBuyerProposition);
            List<Signature25519> fakeRegularInputProofs = Collections.nCopies(inputRegularBoxIds.size(), null);

            Long timestamp = System.currentTimeMillis();

            SellCarTransaction unsignedTransaction = new SellCarTransaction(
                    inputRegularBoxIds,
                    fakeRegularInputProofs,
                    regularOutputs,
                    fakeSaleOrderInfo,
                    ent.fee,
                    timestamp);

            // Get the Tx message to be signed.
            byte[] messageToSign = unsignedTransaction.messageToSign();

            // Create signatures.
            List<Signature25519> regularInputProofs = new ArrayList<>();

            for (Box<Proposition> box : paymentBoxes) {
                regularInputProofs.add((Signature25519) view.getNodeWallet().secretByPublicKey(box.proposition()).get().sign(messageToSign));
            }

            CarSellOrderInfo saleOrderInfo = new CarSellOrderInfo(
                    carBox,
                    (Signature25519)view.getNodeWallet().secretByPublicKey(carBox.proposition()).get().sign(messageToSign),
                    ent.sellPrice,
                    carBuyerProposition);


            // Create the resulting signed transaction.
            SellCarTransaction transaction = new SellCarTransaction(
                    inputRegularBoxIds,
                    regularInputProofs,
                    regularOutputs,
                    saleOrderInfo,
                    ent.fee,
                    timestamp);

            return new TxResponse(ByteUtils.toHexString(sidechainTransactionsCompanion.toBytes((BoxTransaction) transaction)));
        }
        catch (Exception e) {
            return new CarResponseError("0102", "Error during Car Sell Order sell operation.", Some.apply(e));
        }
    }

    /*
      Route to accept car sell order by the specified buyer.
      Input parameter is Car Sell Order box id.
      Route checks if car sell order box exist, buyer proposition is controlled by Nodes wallet and
      wallet has enough balance to pay the car price and fee. And then creates BuyCarTransaction.
      Output of this transaction is new Car Box (with buyer as owner) and regular box with coins amount
      equivalent to sell price as payment for car to previous car owner.
      Returns the hex representation of the transaction.
    */
    private ApiResponse acceptCarSellOrder(SidechainNodeView view, SpendCarSellOrderRequest ent) {
        try {
            // Try to find CarSellOrder to be opened in the closed boxes list
            CarSellOrderBox carSellOrderBox = (CarSellOrderBox)view.getNodeState().getClosedBox(BytesUtils.fromHexString(ent.carSellOrderId)).get();

            // Check that Car sell order buyer public key is controlled by node wallet.
            Optional<Secret> buyerSecretOption = view.getNodeWallet().secretByPublicKey(
                    new PublicKey25519Proposition(carSellOrderBox.proposition().getBuyerPublicKeyBytes()));
            if(!buyerSecretOption.isPresent()) {
                return new CarResponseError("0100", "Can't buy the car, because the buyer proposition is not owned by the Node.", Option.empty());
            }

            // Get Regular boxes to pay the car price + fee
            List<Box<Proposition>> paymentBoxes = new ArrayList<>();
            long amountToPay = carSellOrderBox.getPrice() + ent.fee;

            // Avoid to add boxes that are already spent in some Transaction that is present in node Mempool.
            List<byte[]> boxIdsToExclude = boxesFromMempool(view.getNodeMemoryPool());
            List<Box<Proposition>> regularBoxes = view.getNodeWallet().boxesOfType(RegularBox.class, boxIdsToExclude);
            int index = 0;
            while (amountToPay > 0 && index < regularBoxes.size()) {
                paymentBoxes.add(regularBoxes.get(index));
                amountToPay -= regularBoxes.get(index).value();
                index++;
            }

            if (amountToPay > 0) {
                throw new IllegalStateException("Not enough coins to pay the fee.");
            }

            // Set change if exists
            long change = Math.abs(amountToPay);
            List<RegularBoxData> regularOutputs = new ArrayList<>();
            if (change > 0)
                regularOutputs.add(new RegularBoxData((PublicKey25519Proposition) paymentBoxes.get(0).proposition(), change));

            List<byte[]> inputRegularBoxIds = new ArrayList<>();
            for (Box b : paymentBoxes)
                inputRegularBoxIds.add(b.id());

            // Create fake proofs to be able to create transaction to be signed.
            // Specify that sell order is not opened by the seller, but opened by the buyer.
            boolean isSeller = false;
            SellOrderSpendingProof fakeSellProof = new SellOrderSpendingProof(new byte[SellOrderSpendingProof.SIGNATURE_LENGTH], isSeller);
            CarBuyOrderInfo fakeBuyOrderInfo = new CarBuyOrderInfo(carSellOrderBox, fakeSellProof);

            List<Signature25519> fakeRegularInputProofs = Collections.nCopies(inputRegularBoxIds.size(), null);
            Long timestamp = System.currentTimeMillis();

            BuyCarTransaction unsignedTransaction = new BuyCarTransaction(
                    inputRegularBoxIds,
                    fakeRegularInputProofs,
                    regularOutputs,
                    fakeBuyOrderInfo,
                    ent.fee,
                    timestamp);

            // Get the Tx message to be signed.
            byte[] messageToSign = unsignedTransaction.messageToSign();

            // Create regular signatures.
            List<Signature25519> regularInputProofs = new ArrayList<>();
            for (Box<Proposition> box : paymentBoxes) {
                regularInputProofs.add((Signature25519) view.getNodeWallet().secretByPublicKey(box.proposition()).get().sign(messageToSign));
            }

            // Create sell order spending proof for buyer
            SellOrderSpendingProof buyerProof = new SellOrderSpendingProof(
                    buyerSecretOption.get().sign(messageToSign).bytes(),
                    isSeller
            );

            // Create the resulting signed transaction.
            CarBuyOrderInfo buyOrderInfo = new CarBuyOrderInfo(carSellOrderBox, buyerProof);

            BuyCarTransaction transaction = new BuyCarTransaction(
                    inputRegularBoxIds,
                    regularInputProofs,
                    regularOutputs,
                    buyOrderInfo,
                    ent.fee,
                    timestamp);

            return new TxResponse(ByteUtils.toHexString(sidechainTransactionsCompanion.toBytes((BoxTransaction) transaction)));
        } catch (Exception e) {
            return new CarResponseError("0103", "Error during Car Sell Order buy operation.", Some.apply(e));
        }
    }

    /*
      Route to cancel car sell order. Car Sell order can be cancelled by the owner only.
      Input parameters are Car Sell Order box id and fee to pay.
      Route checks if car sell order exists and owned by the node Wallet and then creates BuyCarTransaction.
      Output of this transaction is new Car Box (with seller as owner).
      Returns the hex representation of the transaction.
    */
    private ApiResponse cancelCarSellOrder(SidechainNodeView view, SpendCarSellOrderRequest ent) {
        try {
            // Try to find CarSellOrder to be opened in the closed boxes list
            Optional<Box> carSellOrderBoxOption = view.getNodeState().getClosedBox(BytesUtils.fromHexString(ent.carSellOrderId));

            if (!carSellOrderBoxOption.isPresent())
                throw new IllegalArgumentException("CarSellOrderBox with given box id not found in the State.");

            CarSellOrderBox carSellOrderBox = (CarSellOrderBox)carSellOrderBoxOption.get();

            // Check that Car sell order owner public key is controlled by node wallet.
            Optional<Secret> ownerSecretOption = view.getNodeWallet().secretByPublicKey(
                    new PublicKey25519Proposition(carSellOrderBox.proposition().getOwnerPublicKeyBytes()));
            if(!ownerSecretOption.isPresent()) {
                return new CarResponseError("0100", "Can't buy the car, because the owner proposition is not owned by the Node.", Option.empty());
            }

            // Get Regular boxes to pay the fee
            List<Box<Proposition>> paymentBoxes = new ArrayList<>();
            long amountToPay = ent.fee;

            List<byte[]> boxIdsToExclude = boxesFromMempool(view.getNodeMemoryPool());
            List<Box<Proposition>> regularBoxes = view.getNodeWallet().boxesOfType(RegularBox.class, boxIdsToExclude);
            int index = 0;
            while (amountToPay > 0 && index < regularBoxes.size()) {
                paymentBoxes.add(regularBoxes.get(index));
                amountToPay -= regularBoxes.get(index).value();
                index++;
            }

            if (amountToPay > 0) {
                throw new IllegalStateException("Not enough coins to pay the fee.");
            }

            // Set change if exists
            long change = Math.abs(amountToPay);
            List<RegularBoxData> regularOutputs = new ArrayList<>();
            if (change > 0)
                regularOutputs.add(new RegularBoxData((PublicKey25519Proposition) paymentBoxes.get(0).proposition(), change));

            List<byte[]> inputRegularBoxIds = new ArrayList<>();
            for (Box b : paymentBoxes)
                inputRegularBoxIds.add(b.id());

            // Create fake proofs to be able to create transaction to be signed.
            // Specify that sell order is opened by the seller.
            boolean isSeller = true;
            SellOrderSpendingProof fakeOwnerProof = new SellOrderSpendingProof(new byte[SellOrderSpendingProof.SIGNATURE_LENGTH], isSeller);
            CarBuyOrderInfo fakeBuyOrderInfo = new CarBuyOrderInfo(carSellOrderBox, fakeOwnerProof);

            List<Signature25519> fakeRegularInputProofs = Collections.nCopies(inputRegularBoxIds.size(), null);
            Long timestamp = System.currentTimeMillis();

            BuyCarTransaction unsignedTransaction = new BuyCarTransaction(
                    inputRegularBoxIds,
                    fakeRegularInputProofs,
                    regularOutputs,
                    fakeBuyOrderInfo,
                    ent.fee,
                    timestamp);

            // Get the Tx message to be signed.
            byte[] messageToSign = unsignedTransaction.messageToSign();

            // Create regular signatures.
            List<Signature25519> regularInputProofs = new ArrayList<>();
            for (Box<Proposition> box : paymentBoxes) {
                regularInputProofs.add((Signature25519) view.getNodeWallet().secretByPublicKey(box.proposition()).get().sign(messageToSign));
            }

            // Create sell order spending proof for owner
            SellOrderSpendingProof ownerProof = new SellOrderSpendingProof(
                    ownerSecretOption.get().sign(messageToSign).bytes(),
                    isSeller
            );

            // Create the resulting signed transaction.
            CarBuyOrderInfo buyOrderInfo = new CarBuyOrderInfo(carSellOrderBox, ownerProof);

            BuyCarTransaction transaction = new BuyCarTransaction(
                    inputRegularBoxIds,
                    regularInputProofs,
                    regularOutputs,
                    buyOrderInfo,
                    ent.fee,
                    timestamp);

            return new TxResponse(ByteUtils.toHexString(sidechainTransactionsCompanion.toBytes((BoxTransaction) transaction)));
        } catch (Exception e) {
            return new CarResponseError("0103", "Error during Car Sell Order cancel operation.", Some.apply(e));
        }
    }

    // The CarApi requests success result output structure.
    @JsonView(Views.Default.class)
    class TxResponse implements SuccessResponse {
        public String transactionBytes;

        public TxResponse(String transactionBytes) {
            this.transactionBytes = transactionBytes;
        }
    }

    // The CarApi requests error result output structure.
    static class CarResponseError implements ErrorResponse {
        private final String code;
        private final String description;
        private final Option<Throwable> exception;

        CarResponseError(String code, String description, Option<Throwable> exception) {
            this.code = code;
            this.description = description;
            this.exception = exception;
        }

        @Override
        public String code() {
            return code;
        }

        @Override
        public String description() {
            return description;
        }

        @Override
        public Option<Throwable> exception() {
            return exception;
        }
    }

    // Utility functions to get from the current mempool the list of all boxes to be opened.
    private List<byte[]> boxesFromMempool(NodeMemoryPool mempool) {
        List<byte[]> boxesFromMempool = new ArrayList<>();
        for(BoxTransaction tx : mempool.getTransactions()) {
            Set<ByteArrayWrapper> ids = tx.boxIdsToOpen();
            for(ByteArrayWrapper id : ids) {
                boxesFromMempool.add(id.data());
            }
        }
        return boxesFromMempool;
    }
}

