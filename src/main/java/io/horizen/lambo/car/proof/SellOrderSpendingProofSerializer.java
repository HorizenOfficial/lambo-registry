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
        writer.putBytes(boxData.bytes());
    }

    @Override
    public SellOrderSpendingProof parse(Reader reader) {
        return SellOrderSpendingProof.parseBytes(reader.getBytes(reader.remaining()));
    }
}
