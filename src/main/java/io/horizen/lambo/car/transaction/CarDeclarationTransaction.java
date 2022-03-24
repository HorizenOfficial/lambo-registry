package io.horizen.lambo.car.transaction;

import com.horizen.box.Box;
import com.horizen.box.data.BoxData;
import com.horizen.box.data.ZenBoxData;
import com.horizen.transaction.AbstractRegularTransaction;
import io.horizen.lambo.car.box.data.CarBoxData;
import io.horizen.lambo.car.box.data.CarBoxDataSerializer;
import com.horizen.proof.Signature25519;
import com.horizen.proposition.Proposition;
import com.horizen.transaction.TransactionSerializer;
import scorex.core.NodeViewModifier$;
import scorex.util.serialization.Reader;
import scorex.util.serialization.Writer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.horizen.lambo.car.transaction.CarRegistryTransactionsIdsEnum.CarDeclarationTransactionId;

// CarDeclarationTransaction is nested from AbstractRegularTransaction so support regular coins transmission as well.
// Moreover it was designed to declare new Cars in the sidechain network.
// As outputs it contains possible ZenBoxes(to pay fee and change) and new CarBox entry.
// No specific unlockers to parent class logic, but has specific new box.
// TODO: add specific mempool incompatibility checker to deprecate keeping in the Mempool txs that declare the same Car.
public final class CarDeclarationTransaction extends AbstractRegularTransaction {

    private final CarBoxData outputCarBoxData;

    public final static byte CAR_DECLARATION_TRANSACTION_VERSION = 1;

    private byte version;

    public CarDeclarationTransaction(List<byte[]> inputZenBoxIds,
                                     List<Signature25519> inputZenBoxProofs,
                                     List<ZenBoxData> outputZenBoxesData,
                                     CarBoxData outputCarBoxData,
                                     long fee,
                                     byte version) {
        super(inputZenBoxIds, inputZenBoxProofs, outputZenBoxesData, fee);
        this.outputCarBoxData = outputCarBoxData;
        this.version = version;
    }

    // Specify the unique custom transaction id.
    @Override
    public byte transactionTypeId() {
        return CarDeclarationTransactionId.id();
    }

    @Override
    protected List<BoxData<Proposition, Box<Proposition>>> getCustomOutputData() {
        return Arrays.asList((BoxData) outputCarBoxData);
    }

    @Override
    public byte[] customFieldsData() {
        return outputCarBoxData.bytes();
    }

    @Override
    public byte version() {
        return version;
    }

    @Override
    public byte[] customDataMessageToSign() {
        return new byte[0];
    }

    // Define object serialization, that should serialize both parent class entries and CarBoxData as well
    void serialize(Writer writer) {
        writer.put(version());
        writer.putLong(fee());

        writer.putInt(inputZenBoxIds.size());
        for(byte[] id: inputZenBoxIds)
            writer.putBytes(id);

        zenBoxProofsSerializer.serialize(inputZenBoxProofs, writer);
        zenBoxDataListSerializer.serialize(outputZenBoxesData, writer);
        CarBoxDataSerializer.getSerializer().serialize(outputCarBoxData, writer);
    }

    static CarDeclarationTransaction parse(Reader reader) {
        byte version = reader.getByte();
        long fee = reader.getLong();

        int inputBytesIdsLength = reader.getInt();
        int idLength = NodeViewModifier$.MODULE$.ModifierIdSize();
        List<byte[]> inputZenBoxIds = new ArrayList<>();
        while(inputBytesIdsLength-- > 0)
            inputZenBoxIds.add(reader.getBytes(idLength));

        List<Signature25519> inputZenBoxProofs = zenBoxProofsSerializer.parse(reader);
        List<ZenBoxData> outputZenBoxesData = zenBoxDataListSerializer.parse(reader);
        CarBoxData outputCarBoxData = CarBoxDataSerializer.getSerializer().parse(reader);

        return new CarDeclarationTransaction(inputZenBoxIds, inputZenBoxProofs, outputZenBoxesData,
                outputCarBoxData, fee, version);
    }

    // Set specific Serializer for CarDeclarationTransaction class.
    @Override
    public TransactionSerializer serializer() {
        return CarDeclarationTransactionSerializer.getSerializer();
    }
}
