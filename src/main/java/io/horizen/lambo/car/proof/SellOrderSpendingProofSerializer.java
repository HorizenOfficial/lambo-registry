package io.horizen.lambo.car.proof;

import com.horizen.proof.ProofSerializer;
import scorex.util.serialization.Reader;
import scorex.util.serialization.Writer;

public final class SellOrderSpendingProofSerializer implements ProofSerializer<SellOrderSpendingProof> {

    private static final SellOrderSpendingProofSerializer serializer = new SellOrderSpendingProofSerializer();

    private SellOrderSpendingProofSerializer() {
        super();
    }

    public static SellOrderSpendingProofSerializer getSerializer() {
        return serializer;
    }

    @Override
    public void serialize(SellOrderSpendingProof boxData, Writer writer) {
        writer.put(boxData.isSeller() ? (byte)1 : (byte)0);
        writer.putBytes(boxData.signatureBytes());
    }

    @Override
    public SellOrderSpendingProof parse(Reader reader) {
        boolean isSeller = reader.getByte() != 0;
        byte[] signatureBytes = reader.getBytes(SellOrderSpendingProof.SIGNATURE_LENGTH);

        return new SellOrderSpendingProof(signatureBytes, isSeller);
    }
}
