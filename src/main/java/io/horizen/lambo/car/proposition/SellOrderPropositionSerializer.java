package io.horizen.lambo.car.proposition;

import com.horizen.proposition.PropositionSerializer;
import scorex.util.serialization.Reader;
import scorex.util.serialization.Writer;

public final class SellOrderPropositionSerializer implements PropositionSerializer<SellOrderProposition> {

    private static final SellOrderPropositionSerializer serializer = new SellOrderPropositionSerializer();

    private SellOrderPropositionSerializer() {
        super();
    }

    public static SellOrderPropositionSerializer getSerializer() {
        return serializer;
    }

    @Override
    public void serialize(SellOrderProposition proposition, Writer writer) {
        writer.putBytes(proposition.bytes());
    }

    @Override
    public SellOrderProposition parse(Reader reader) {
        return SellOrderProposition.parseBytes(reader.getBytes(reader.remaining()));
    }
}
