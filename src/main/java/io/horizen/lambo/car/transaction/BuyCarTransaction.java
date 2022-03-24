package io.horizen.lambo.car.transaction;

import com.horizen.box.BoxUnlocker;
import com.horizen.box.Box;
import com.horizen.box.data.BoxData;
import com.horizen.box.data.ZenBoxData;
import com.horizen.transaction.AbstractRegularTransaction;
import io.horizen.lambo.car.info.CarBuyOrderInfo;
import com.horizen.proof.Proof;
import com.horizen.proof.Signature25519;
import com.horizen.proposition.Proposition;
import com.horizen.transaction.TransactionSerializer;
import io.horizen.lambo.car.info.CarBuyOrderInfoSerializer;
import scorex.core.NodeViewModifier$;
import scorex.util.serialization.Reader;
import scorex.util.serialization.Writer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.horizen.lambo.car.transaction.CarRegistryTransactionsIdsEnum.BuyCarTransactionId;

// BuyCarTransaction is nested from AbstractRegularTransaction so support regular coins transmission as well.
// BuyCarTransaction was designed to accept the SellOrder by specific buyer or to cancel it by the owner.
// As outputs it contains possible ZenBoxes(to pay to the sell order owner and make change) and new CarBox entry.
// As unlockers it contains ZenBoxes and CarSellOrder to open.
public final class BuyCarTransaction extends AbstractRegularTransaction {

    // CarBuyOrderInfo is a view that describes what sell order to open and who will be the next owner.
    // But inside it contains just a minimum set of info (like CarSellOrderBox itself and proof) that is the unique source of data.
    // So, no one outside controls what will be the specific outputs of this transaction.
    // Any malicious actions will lead to transaction invalidation.
    // For example, if SellOrder was accepted by the buyer specified, CarBuyOrderInfo view returns as the new box data
    // new instance of CarBoxData the owned by the buyer and ZenBoxData with the payment to previous owner.
    private final CarBuyOrderInfo carBuyOrderInfo;

    public final static byte BUY_CAR_TRANSACTION_VERSION = 1;

    private byte version;

    public BuyCarTransaction(List<byte[]> inputZenBoxIds,
                             List<Signature25519> inputZenBoxProofs,
                             List<ZenBoxData> outputZenBoxesData,
                             CarBuyOrderInfo carBuyOrderInfo,
                             long fee,
                             byte version) {
        super(inputZenBoxIds, inputZenBoxProofs, outputZenBoxesData, fee);
        this.carBuyOrderInfo = carBuyOrderInfo;
        this.version = version;
    }

    // Specify the unique custom transaction id.
    @Override
    public byte transactionTypeId() {
        return BuyCarTransactionId.id();
    }

    @Override
    protected List<BoxData<Proposition, Box<Proposition>>> getCustomOutputData() {
        ArrayList<BoxData<Proposition, Box<Proposition>>> customOutputData = new ArrayList<>();
        customOutputData.add((BoxData)carBuyOrderInfo.getNewOwnerCarBoxData());
        if(!carBuyOrderInfo.isSpentByOwner())
            customOutputData.add((BoxData)carBuyOrderInfo.getPaymentBoxData());

        return customOutputData;
    }

    @Override
    public byte[] customDataMessageToSign() {
        return new byte[0];
    }

    @Override
    public byte[] customFieldsData() {
        return carBuyOrderInfo.getNewOwnerCarBoxData().bytes();
    }

    @Override
    public byte version() {
        return version;
    }

    // Override unlockers to contains ZenBoxes from the parent class appended with CarSellOrderBox entry.
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

    // Define object serialization, that should serialize both parent class entries and CarBuyOrderInfo as well
    void serialize(Writer writer) {
        writer.put(version());
        writer.putLong(fee());

        writer.putInt(inputZenBoxIds.size());
        for(byte[] id: inputZenBoxIds)
            writer.putBytes(id);

        zenBoxProofsSerializer.serialize(inputZenBoxProofs, writer);
        zenBoxDataListSerializer.serialize(outputZenBoxesData, writer);
        CarBuyOrderInfoSerializer.getSerializer().serialize(carBuyOrderInfo, writer);
    }

    static BuyCarTransaction parse(Reader reader) {
        byte version = reader.getByte();
        long fee = reader.getLong();

        int inputBytesIdsLength = reader.getInt();
        int idLength = NodeViewModifier$.MODULE$.ModifierIdSize();
        List<byte[]> inputZenBoxIds = new ArrayList<>();
        while(inputBytesIdsLength-- > 0)
            inputZenBoxIds.add(reader.getBytes(idLength));

        List<Signature25519> inputZenBoxProofs = zenBoxProofsSerializer.parse(reader);
        List<ZenBoxData> outputZenBoxesData = zenBoxDataListSerializer.parse(reader);
        CarBuyOrderInfo carBuyOrderInfo = CarBuyOrderInfoSerializer.getSerializer().parse(reader);

        return new BuyCarTransaction(inputZenBoxIds, inputZenBoxProofs, outputZenBoxesData,
                carBuyOrderInfo, fee, version);
    }

    // Set specific Serializer for BuyCarTransaction class.
    @Override
    public TransactionSerializer serializer() {
        return BuyCarTransactionSerializer.getSerializer();
    }
}
