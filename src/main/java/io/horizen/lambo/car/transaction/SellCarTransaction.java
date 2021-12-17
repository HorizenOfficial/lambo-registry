package io.horizen.lambo.car.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.horizen.box.BoxUnlocker;
import com.horizen.box.Box;
import com.horizen.box.data.BoxData;
import com.horizen.box.data.ZenBoxData;
import com.horizen.transaction.AbstractRegularTransaction;
import io.horizen.lambo.car.info.CarSellOrderInfo;
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

import static io.horizen.lambo.car.transaction.CarRegistryTransactionsIdsEnum.SellCarTransactionId;

// SellCarTransaction is nested from AbstractRegularTransaction so support regular coins transmission as well.
// SellCarTransaction was designed to create a SellOrder for a specific buyer for given CarBox owned by the user.
// As outputs it contains possible ZenBoxes(to pay fee and make change) and new CarSellOrder entry.
// As unlockers it contains ZenBoxes and CarBox to open.
public final class SellCarTransaction extends AbstractRegularTransaction {

    // CarSellOrderInfo is a view that describes what car box to open and what is the sell order(car attributes, price and buyer info).
    // But inside it contains just a minimum set of info (like CarBox itself and price) that is the unique source of data.
    // So, no one outside controls what will be the specific outputs of this transaction.
    // Any malicious actions will lead to transaction invalidation.
    // For example, if CarBox was opened, the CarSellOrder obliged to contains the same car attributes and owner info.
    private final CarSellOrderInfo carSellOrderInfo;

    public final static byte CAR_SELL_TRANSACTION_VERSION = 1;

    private byte version;

    public SellCarTransaction(List<byte[]> inputZenBoxIds,
                              List<Signature25519> inputZenBoxProofs,
                              List<ZenBoxData> outputZenBoxesData,
                              CarSellOrderInfo carSellOrderInfo,
                              long fee,
                              byte version) {
        super(inputZenBoxIds, inputZenBoxProofs, outputZenBoxesData, fee);
        this.carSellOrderInfo = carSellOrderInfo;
        this.version = version;
    }

    // Specify the unique custom transaction id.
    @Override
    public byte transactionTypeId() {
        return SellCarTransactionId.id();
    }

    // Override unlockers to contains ZenBoxes from the parent class appended with CarBox entry to be opened.
    @Override
    public List<BoxUnlocker<Proposition>> unlockers() {
        // Get Regular unlockers from base class.
        List<BoxUnlocker<Proposition>> unlockers = super.unlockers();

        BoxUnlocker<Proposition> unlocker = new BoxUnlocker<Proposition>() {
            @Override
            public byte[] closedBoxId() {
                return carSellOrderInfo.getCarBoxToOpen().id();
            }

            @Override
            public Proof boxKey() {
                return carSellOrderInfo.getCarBoxSpendingProof();
            }
        };
        // Append with the CarBox unlocker entry.
        unlockers.add(unlocker);

        return unlockers;
    }

    @Override
    protected List<BoxData<Proposition, Box<Proposition>>> getCustomOutputData() {
        return Arrays.asList((BoxData)carSellOrderInfo.getSellOrderBoxData());
    }

    @Override
    public byte[] customFieldsData() {
        return carSellOrderInfo.getSellOrderBoxData().bytes();
    }

    @Override
    public byte[] customDataMessageToSign() {
        return new byte[0];
    }

    @Override
    public byte version() {
        return version;
    }

    // Define object serialization, that should serialize both parent class entries and CarSellOrderInfo as well
    @Override
    public byte[] bytes() {
        ByteArrayOutputStream inputsIdsStream = new ByteArrayOutputStream();
        for(byte[] id: inputZenBoxIds)
            inputsIdsStream.write(id, 0, id.length);

        byte[] inputZenBoxIdsBytes = inputsIdsStream.toByteArray();

        byte[] inputZenBoxProofsBytes = zenBoxProofsSerializer.toBytes(inputZenBoxProofs);

        byte[] outputZenBoxesDataBytes = zenBoxDataListSerializer.toBytes(outputZenBoxesData);

        byte[] carSellOrderInfoBytes = carSellOrderInfo.bytes();

        return Bytes.concat(
                new byte[] {version()},                                 // 1 byte
                Longs.toByteArray(fee()),                               // 8 bytes
                Ints.toByteArray(inputZenBoxIdsBytes.length),           // 4 bytes
                inputZenBoxIdsBytes,                                    // depends on previous value (>=4 bytes)
                Ints.toByteArray(inputZenBoxProofsBytes.length),        // 4 bytes
                inputZenBoxProofsBytes,                                 // depends on previous value (>=4 bytes)
                Ints.toByteArray(outputZenBoxesDataBytes.length),       // 4 bytes
                outputZenBoxesDataBytes,                                // depends on previous value (>=4 bytes)
                Ints.toByteArray(carSellOrderInfoBytes.length),         // 4 bytes
                carSellOrderInfoBytes                                   // depends on previous value (>=4 bytes)
        );
    }

    // Define object deserialization similar to 'toBytes()' representation.
    public static SellCarTransaction parseBytes(byte[] bytes) {
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

        CarSellOrderInfo carSellOrderInfo = CarSellOrderInfo.parseBytes(Arrays.copyOfRange(bytes, offset, offset + batchSize));

        return new SellCarTransaction(inputZenBoxIds, inputZenBoxProofs, outputZenBoxesData, carSellOrderInfo, fee, version);
    }

    // Set specific Serializer for SellCarTransaction class.
    @Override
    public TransactionSerializer serializer() {
        return SellCarTransactionSerializer.getSerializer();
    }
}