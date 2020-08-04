package io.horizen.lambo.car.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.horizen.box.BoxUnlocker;
import com.horizen.box.NoncedBox;
import com.horizen.box.RegularBox;
import com.horizen.box.data.RegularBoxData;
import io.horizen.lambo.car.box.CarBox;
import io.horizen.lambo.car.info.CarBuyOrderInfo;
import com.horizen.proof.Proof;
import com.horizen.proof.Signature25519;
import com.horizen.proposition.Proposition;
import com.horizen.transaction.TransactionSerializer;
import com.horizen.utils.BytesUtils;
import scorex.core.NodeViewModifier$;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.horizen.lambo.car.transaction.CarRegistryTransactionsIdsEnum.BuyCarTransactionId;

// BuyCarTransaction is nested from AbstractRegularTransaction so support regular coins transmission as well.
// BuyCarTransaction was designed to accept the SellOrder by specific buyer or to cancel it by the owner.
// As outputs it contains possible RegularBoxes(to pay to the sell order owner and make change) and new CarBox entry.
// As unlockers it contains RegularBoxes and CarSellOrder to open.
public final class BuyCarTransaction extends AbstractRegularTransaction {

    // CarBuyOrderInfo is a view that describes what sell order to open and who will be the next owner.
    // But inside it contains just a minimum set of info (like CarSellOrderBox itself and proof) that is the unique source of data.
    // So, no one outside controls what will be the specific outputs of this transaction.
    // Any malicious actions will lead to transaction invalidation.
    // For example, if SellOrder was accepted by the buyer specified, CarBuyOrderInfo view returns as the new box data
    // new instance of CarBoxData the owned by the buyer and RegularBoxData with the payment to previous owner.
    private CarBuyOrderInfo carBuyOrderInfo;

    private List<NoncedBox<Proposition>> newBoxes;

    public BuyCarTransaction(List<byte[]> inputRegularBoxIds,
                             List<Signature25519> inputRegularBoxProofs,
                             List<RegularBoxData> outputRegularBoxesData,
                             CarBuyOrderInfo carBuyOrderInfo,
                             long fee,
                             long timestamp) {
        super(inputRegularBoxIds, inputRegularBoxProofs, outputRegularBoxesData, fee, timestamp);
        this.carBuyOrderInfo = carBuyOrderInfo;
    }

    // Specify the unique custom transaction id.
    @Override
    public byte transactionTypeId() {
        return BuyCarTransactionId.id();
    }

    // Override unlockers to contains regularBoxes from the parent class appended with CarSellOrderBox entry.
    @Override
    public List<BoxUnlocker<Proposition>> unlockers() {
        // Get Regular unlockers from base class.
        List<BoxUnlocker<Proposition>> unlockers = super.unlockers();

        BoxUnlocker<Proposition> unlocker = new BoxUnlocker<Proposition>() {
            @Override
            public byte[] closedBoxId() {
                return carBuyOrderInfo.getCarSellOrderBoxToOpen().id();
            }

            @Override
            public Proof boxKey() {
                return carBuyOrderInfo.getCarSellOrderSpendingProof();
            }
        };
        // Append with the CarSellOrderBox unlocker entry.
        unlockers.add(unlocker);

        return unlockers;
    }

    // Override newBoxes to contains regularBoxes from the parent class appended with CarBox and payment entries.
    // The nonce calculation algorithm for Boxes is the same as in parent class.
    @Override
    public List<NoncedBox<Proposition>> newBoxes() {
        if(newBoxes == null) {
            // Get new boxes from base class.
            newBoxes = new ArrayList<>(super.newBoxes());

            // Set CarBox with specific owner depends on proof. See CarBuyOrderInfo.getNewOwnerCarBoxData() definition.
            long nonce = getNewBoxNonce(carBuyOrderInfo.getNewOwnerCarBoxData().proposition(), newBoxes.size());
            newBoxes.add((NoncedBox) new CarBox(carBuyOrderInfo.getNewOwnerCarBoxData(), nonce));

            // If Sell Order was opened by the buyer -> add payment box for Car previous owner.
            if (!carBuyOrderInfo.isSpentByOwner()) {
                RegularBoxData paymentBoxData = carBuyOrderInfo.getPaymentBoxData();
                nonce = getNewBoxNonce(paymentBoxData.proposition(), newBoxes.size());
                newBoxes.add((NoncedBox) new RegularBox(paymentBoxData, nonce));
            }
        }
        return Collections.unmodifiableList(newBoxes);

    }

    // Define object serialization, that should serialize both parent class entries and CarBuyOrderInfo as well
    @Override
    public byte[] bytes() {
        ByteArrayOutputStream inputsIdsStream = new ByteArrayOutputStream();
        for(byte[] id: inputRegularBoxIds)
            inputsIdsStream.write(id, 0, id.length);

        byte[] inputRegularBoxIdsBytes = inputsIdsStream.toByteArray();

        byte[] inputRegularBoxProofsBytes = regularBoxProofsSerializer.toBytes(inputRegularBoxProofs);

        byte[] outputRegularBoxesDataBytes = regularBoxDataListSerializer.toBytes(outputRegularBoxesData);

        byte[] carBuyOrderInfoBytes = carBuyOrderInfo.bytes();

        return Bytes.concat(
                Longs.toByteArray(fee()),                               // 8 bytes
                Longs.toByteArray(timestamp()),                         // 8 bytes
                Ints.toByteArray(inputRegularBoxIdsBytes.length),       // 4 bytes
                inputRegularBoxIdsBytes,                                // depends on previous value (>=4 bytes)
                Ints.toByteArray(inputRegularBoxProofsBytes.length),    // 4 bytes
                inputRegularBoxProofsBytes,                             // depends on previous value (>=4 bytes)
                Ints.toByteArray(outputRegularBoxesDataBytes.length),   // 4 bytes
                outputRegularBoxesDataBytes,                            // depends on previous value (>=4 bytes)
                Ints.toByteArray(carBuyOrderInfoBytes.length),          // 4 bytes
                carBuyOrderInfoBytes                                    // depends on previous value (>=4 bytes)
        );
    }

    // Define object deserialization similar to 'toBytes()' representation.
    public static BuyCarTransaction parseBytes(byte[] bytes) {
        int offset = 0;

        long fee = BytesUtils.getLong(bytes, offset);
        offset += 8;

        long timestamp = BytesUtils.getLong(bytes, offset);
        offset += 8;

        int batchSize = BytesUtils.getInt(bytes, offset);
        offset += 4;

        ArrayList<byte[]> inputRegularBoxIds = new ArrayList<>();
        int idLength = NodeViewModifier$.MODULE$.ModifierIdSize();
        while(batchSize > 0) {
            inputRegularBoxIds.add(Arrays.copyOfRange(bytes, offset, offset + idLength));
            offset += idLength;
            batchSize -= idLength;
        }

        batchSize = BytesUtils.getInt(bytes, offset);
        offset += 4;

        List<Signature25519> inputRegularBoxProofs = regularBoxProofsSerializer.parseBytes(Arrays.copyOfRange(bytes, offset, offset + batchSize));
        offset += batchSize;

        batchSize = BytesUtils.getInt(bytes, offset);
        offset += 4;

        List<RegularBoxData> outputRegularBoxesData = regularBoxDataListSerializer.parseBytes(Arrays.copyOfRange(bytes, offset, offset + batchSize));
        offset += batchSize;

        batchSize = BytesUtils.getInt(bytes, offset);
        offset += 4;

        CarBuyOrderInfo carBuyOrderInfo = CarBuyOrderInfo.parseBytes(Arrays.copyOfRange(bytes, offset, offset + batchSize));

        return new BuyCarTransaction(inputRegularBoxIds, inputRegularBoxProofs, outputRegularBoxesData, carBuyOrderInfo, fee, timestamp);
    }

    // Set specific Serializer for BuyCarTransaction class.
    @Override
    public TransactionSerializer serializer() {
        return BuyCarTransactionSerializer.getSerializer();
    }
}
