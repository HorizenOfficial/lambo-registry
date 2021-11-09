package io.horizen.lambo.car.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.horizen.box.BoxUnlocker;
import com.horizen.box.NoncedBox;
import com.horizen.box.data.NoncedBoxData;
import com.horizen.box.data.ZenBoxData;
import com.horizen.transaction.AbstractRegularTransaction;
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
    protected List<NoncedBoxData<Proposition, NoncedBox<Proposition>>> getCustomOutputData() {
        return Arrays.asList((NoncedBoxData)carBuyOrderInfo.getNewOwnerCarBoxData());
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
    @Override
    public byte[] bytes() {
        ByteArrayOutputStream inputsIdsStream = new ByteArrayOutputStream();
        for(byte[] id: inputZenBoxIds)
            inputsIdsStream.write(id, 0, id.length);

        byte[] inputZenBoxIdsBytes = inputsIdsStream.toByteArray();

        byte[] inputZenBoxProofsBytes = zenBoxProofsSerializer.toBytes(inputZenBoxProofs);

        byte[] outputZenBoxesDataBytes = zenBoxDataListSerializer.toBytes(outputZenBoxesData);

        byte[] carBuyOrderInfoBytes = carBuyOrderInfo.bytes();

        return Bytes.concat(
                new byte[] {version()},
                Longs.toByteArray(fee()),                               // 8 bytes
                Ints.toByteArray(inputZenBoxIdsBytes.length),           // 4 bytes
                inputZenBoxIdsBytes,                                    // depends on previous value (>=4 bytes)
                Ints.toByteArray(inputZenBoxProofsBytes.length),        // 4 bytes
                inputZenBoxProofsBytes,                                 // depends on previous value (>=4 bytes)
                Ints.toByteArray(outputZenBoxesDataBytes.length),       // 4 bytes
                outputZenBoxesDataBytes,                                // depends on previous value (>=4 bytes)
                Ints.toByteArray(carBuyOrderInfoBytes.length),          // 4 bytes
                carBuyOrderInfoBytes                                    // depends on previous value (>=4 bytes)
        );
    }

    // Define object deserialization similar to 'toBytes()' representation.
    public static BuyCarTransaction parseBytes(byte[] bytes) {
        int offset = 0;

        byte version = bytes[offset];
        offset += 1;

        long fee = BytesUtils.getLong(bytes, offset);
        offset += 8;

        int batchSize = BytesUtils.getInt(bytes, offset);
        offset += 4;

        ArrayList<byte[]> inputZenBoxIds = new ArrayList<>();
        int idLength = NodeViewModifier$.MODULE$.ModifierIdSize();
        while(batchSize > 0) {
            inputZenBoxIds.add(Arrays.copyOfRange(bytes, offset, offset + idLength));
            offset += idLength;
            batchSize -= idLength;
        }

        batchSize = BytesUtils.getInt(bytes, offset);
        offset += 4;

        List<Signature25519> inputZenBoxProofs = zenBoxProofsSerializer.parseBytes(Arrays.copyOfRange(bytes, offset, offset + batchSize));
        offset += batchSize;

        batchSize = BytesUtils.getInt(bytes, offset);
        offset += 4;

        List<ZenBoxData> outputZenBoxesData = zenBoxDataListSerializer.parseBytes(Arrays.copyOfRange(bytes, offset, offset + batchSize));
        offset += batchSize;

        batchSize = BytesUtils.getInt(bytes, offset);
        offset += 4;

        CarBuyOrderInfo carBuyOrderInfo = CarBuyOrderInfo.parseBytes(Arrays.copyOfRange(bytes, offset, offset + batchSize));

        return new BuyCarTransaction(inputZenBoxIds, inputZenBoxProofs, outputZenBoxesData, carBuyOrderInfo, fee, version);
    }

    // Set specific Serializer for BuyCarTransaction class.
    @Override
    public TransactionSerializer serializer() {
        return BuyCarTransactionSerializer.getSerializer();
    }
}
