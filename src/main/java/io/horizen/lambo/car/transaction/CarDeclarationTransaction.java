package io.horizen.lambo.car.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.horizen.box.NoncedBox;
import com.horizen.box.data.RegularBoxData;
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

import static io.horizen.lambo.car.transaction.CarRegistryTransactionsIdsEnum.CarDeclarationTransactionId;

// CarDeclarationTransaction is nested from AbstractRegularTransaction so support regular coins transmission as well.
// Moreover it was designed to declare new Cars in the sidechain network.
// As outputs it contains possible RegularBoxes(to pay fee and change) and new CarBox entry.
// No specific unlockers to parent class logic, but has specific new box.
// TODO: add specific mempool incompatibility checker to deprecate keeping in the Mempool txs that declare the same Car.
public final class CarDeclarationTransaction<P extends Proposition, B extends NoncedBox<P>> extends AbstractRegularTransaction {

    private final CarBoxData outputCarBoxData;
    private List<NoncedBox<Proposition>> newBoxes;

    public CarDeclarationTransaction(List<byte[]> inputRegularBoxIds,
                                     List<Signature25519> inputRegularBoxProofs,
                                     List<RegularBoxData> outputRegularBoxesData,
                                     CarBoxData outputCarBoxData,
                                     long fee,
                                     long timestamp) {
        super(inputRegularBoxIds, inputRegularBoxProofs, outputRegularBoxesData, fee, timestamp);
        this.outputCarBoxData = outputCarBoxData;
    }

    // Specify the unique custom transaction id.
    @Override
    public byte transactionTypeId() {
        return CarDeclarationTransactionId.id();
    }

    // Override newBoxes to contains regularBoxes from the parent class appended with CarBox entry.
    // The nonce calculation algorithm for CarBox is the same as in parent class.
    @Override
    public synchronized List<NoncedBox<Proposition>> newBoxes() {
        if(newBoxes == null) {
            newBoxes = new ArrayList<>(super.newBoxes());
            long nonce = getNewBoxNonce(outputCarBoxData.proposition(), newBoxes.size());
            newBoxes.add((NoncedBox) new CarBox(outputCarBoxData, nonce));
        }
        return Collections.unmodifiableList(newBoxes);
    }

    // Define object serialization, that should serialize both parent class entries and CarBoxData as well
    @Override
    public byte[] bytes() {
        ByteArrayOutputStream inputsIdsStream = new ByteArrayOutputStream();
        for(byte[] id: inputRegularBoxIds)
            inputsIdsStream.write(id, 0, id.length);

        byte[] inputRegularBoxIdsBytes = inputsIdsStream.toByteArray();

        byte[] inputRegularBoxProofsBytes = regularBoxProofsSerializer.toBytes(inputRegularBoxProofs);

        byte[] outputRegularBoxesDataBytes = regularBoxDataListSerializer.toBytes(outputRegularBoxesData);

        byte[] outputCarBoxDataBytes = outputCarBoxData.bytes();

        return Bytes.concat(
                Longs.toByteArray(fee()),                               // 8 bytes
                Longs.toByteArray(timestamp()),                         // 8 bytes
                Ints.toByteArray(inputRegularBoxIdsBytes.length),       // 4 bytes
                inputRegularBoxIdsBytes,                                // depends on previous value (>=4 bytes)
                Ints.toByteArray(inputRegularBoxProofsBytes.length),    // 4 bytes
                inputRegularBoxProofsBytes,                             // depends on previous value (>=4 bytes)
                Ints.toByteArray(outputRegularBoxesDataBytes.length),   // 4 bytes
                outputRegularBoxesDataBytes,                            // depends on previous value (>=4 bytes)
                Ints.toByteArray(outputCarBoxDataBytes.length),         // 4 bytes
                outputCarBoxDataBytes                                   // depends on previous value (>=4 bytes)
        );
    }

    // Define object deserialization similar to 'toBytes()' representation.
    public static CarDeclarationTransaction parseBytes(byte[] bytes) {
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

        CarBoxData outputCarBoxData = CarBoxDataSerializer.getSerializer().parseBytes(Arrays.copyOfRange(bytes, offset, offset + batchSize));

        return new CarDeclarationTransaction(inputRegularBoxIds, inputRegularBoxProofs, outputRegularBoxesData, outputCarBoxData, fee, timestamp);
    }

    // Set specific Serializer for CarDeclarationTransaction class.
    @Override
    public TransactionSerializer serializer() {
        return CarDeclarationTransactionSerializer.getSerializer();
    }
}
