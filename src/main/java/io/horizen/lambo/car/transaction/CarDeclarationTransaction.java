package io.horizen.lambo.car.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.horizen.box.NoncedBox;
import com.horizen.box.data.NoncedBoxData;
import com.horizen.box.data.ZenBoxData;
import com.horizen.transaction.AbstractRegularTransaction;
import io.horizen.lambo.car.box.CarBox;
import io.horizen.lambo.car.box.data.CarBoxData;
import io.horizen.lambo.car.box.data.CarBoxDataSerializer;
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
import java.util.stream.Collectors;

import static io.horizen.lambo.car.transaction.CarRegistryTransactionsIdsEnum.CarDeclarationTransactionId;

// CarDeclarationTransaction is nested from AbstractRegularTransaction so support regular coins transmission as well.
// Moreover it was designed to declare new Cars in the sidechain network.
// As outputs it contains possible ZenBoxes(to pay fee and change) and new CarBox entry.
// No specific unlockers to parent class logic, but has specific new box.
// TODO: add specific mempool incompatibility checker to deprecate keeping in the Mempool txs that declare the same Car.
public final class CarDeclarationTransaction extends AbstractRegularTransaction {

    private final CarBoxData outputCarBoxData;

    public CarDeclarationTransaction(List<byte[]> inputZenBoxIds,
                                     List<Signature25519> inputZenBoxProofs,
                                     List<ZenBoxData> outputZenBoxesData,
                                     CarBoxData outputCarBoxData,
                                     long fee) {
        super(inputZenBoxIds, inputZenBoxProofs, outputZenBoxesData, fee);
        this.outputCarBoxData = outputCarBoxData;
    }

    // Specify the unique custom transaction id.
    @Override
    public byte transactionTypeId() {
        return CarDeclarationTransactionId.id();
    }

    @Override
    protected List<NoncedBoxData<Proposition, NoncedBox<Proposition>>> getCustomOutputData() {
        return Arrays.asList((NoncedBoxData) outputCarBoxData);
    }

    // Define object serialization, that should serialize both parent class entries and CarBoxData as well
    @Override
    public byte[] bytes() {
        ByteArrayOutputStream inputsIdsStream = new ByteArrayOutputStream();
        for(byte[] id: inputZenBoxIds)
            inputsIdsStream.write(id, 0, id.length);

        byte[] inputZenBoxIdsBytes = inputsIdsStream.toByteArray();

        byte[] inputZenBoxProofsBytes = zenBoxProofsSerializer.toBytes(inputZenBoxProofs);

        byte[] outputZenBoxesDataBytes = zenBoxDataListSerializer.toBytes(outputZenBoxesData);

        byte[] outputCarBoxDataBytes = outputCarBoxData.bytes();

        return Bytes.concat(
                Longs.toByteArray(fee()),                               // 8 bytes
                Ints.toByteArray(inputZenBoxIdsBytes.length),           // 4 bytes
                inputZenBoxIdsBytes,                                    // depends on previous value (>=4 bytes)
                Ints.toByteArray(inputZenBoxProofsBytes.length),        // 4 bytes
                inputZenBoxProofsBytes,                                 // depends on previous value (>=4 bytes)
                Ints.toByteArray(outputZenBoxesDataBytes.length),       // 4 bytes
                outputZenBoxesDataBytes,                                // depends on previous value (>=4 bytes)
                Ints.toByteArray(outputCarBoxDataBytes.length),         // 4 bytes
                outputCarBoxDataBytes                                   // depends on previous value (>=4 bytes)
        );
    }

    // Define object deserialization similar to 'toBytes()' representation.
    public static CarDeclarationTransaction parseBytes(byte[] bytes) {
        int offset = 0;

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

        CarBoxData outputCarBoxData = CarBoxDataSerializer.getSerializer().parseBytes(Arrays.copyOfRange(bytes, offset, offset + batchSize));

        return new CarDeclarationTransaction(inputZenBoxIds, inputZenBoxProofs, outputZenBoxesData, outputCarBoxData, fee);
    }

    // Set specific Serializer for CarDeclarationTransaction class.
    @Override
    public TransactionSerializer serializer() {
        return CarDeclarationTransactionSerializer.getSerializer();
    }

}
