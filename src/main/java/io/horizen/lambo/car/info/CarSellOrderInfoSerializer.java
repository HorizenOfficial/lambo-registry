package io.horizen.lambo.car.info;

import com.horizen.proof.Signature25519;
import com.horizen.proof.Signature25519Serializer;
import com.horizen.proposition.PublicKey25519Proposition;
import com.horizen.proposition.PublicKey25519PropositionSerializer;
import io.horizen.lambo.car.box.CarBox;
import io.horizen.lambo.car.box.CarBoxSerializer;
import scorex.core.serialization.ScorexSerializer;
import scorex.util.serialization.Reader;
import scorex.util.serialization.Writer;

public class CarSellOrderInfoSerializer implements ScorexSerializer<CarSellOrderInfo> {

    private static final CarSellOrderInfoSerializer serializer = new CarSellOrderInfoSerializer();

    private CarSellOrderInfoSerializer() {
        super();
    }

    public static CarSellOrderInfoSerializer getSerializer() {
        return serializer;
    }

    @Override
    public void serialize(CarSellOrderInfo info, Writer writer) {
        CarBoxSerializer.getSerializer().serialize(info.carBoxToOpen, writer);
        Signature25519Serializer.getSerializer().serialize(info.proof, writer);
        writer.putLong(info.price);
        PublicKey25519PropositionSerializer.getSerializer().serialize(info.buyerProposition, writer);
    }

    @Override
    public CarSellOrderInfo parse(Reader reader) {
        CarBox carBoxToOpen = CarBoxSerializer.getSerializer().parse(reader);
        Signature25519 proof = Signature25519Serializer.getSerializer().parse(reader);
        long price = reader.getLong();
        PublicKey25519Proposition buyerProposition = PublicKey25519PropositionSerializer.getSerializer().parse(reader);

        return new CarSellOrderInfo(carBoxToOpen, proof, price, buyerProposition);
    }
}
