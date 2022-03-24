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
        writer.putBytes(proposition.getOwnerPublicKeyBytes());
        writer.putBytes(proposition.getBuyerPublicKeyBytes());
    }

    @Override
    public SellOrderProposition parse(Reader reader) {
        byte[] ownerPublicKeyBytes = reader.getBytes(SellOrderProposition.KEY_LENGTH);
        byte[] buyerPublicKeyBytes = reader.getBytes(SellOrderProposition.KEY_LENGTH);

        return new SellOrderProposition(ownerPublicKeyBytes, buyerPublicKeyBytes);
    }
}
