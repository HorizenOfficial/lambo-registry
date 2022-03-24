package io.horizen.lambo.car.info;

import io.horizen.lambo.car.box.CarSellOrderBox;
import io.horizen.lambo.car.box.CarSellOrderBoxSerializer;
import io.horizen.lambo.car.proof.SellOrderSpendingProof;
import io.horizen.lambo.car.proof.SellOrderSpendingProofSerializer;
import scorex.core.serialization.ScorexSerializer;
import scorex.util.serialization.Reader;
import scorex.util.serialization.Writer;

public class CarBuyOrderInfoSerializer implements ScorexSerializer<CarBuyOrderInfo> {

    private static final CarBuyOrderInfoSerializer serializer = new CarBuyOrderInfoSerializer();

    private CarBuyOrderInfoSerializer() {
        super();
    }

    public static CarBuyOrderInfoSerializer getSerializer() {
        return serializer;
    }

    @Override
    public void serialize(CarBuyOrderInfo info, Writer writer) {
        CarSellOrderBoxSerializer.getSerializer().serialize(info.carSellOrderBoxToOpen, writer);
        SellOrderSpendingProofSerializer.getSerializer().serialize(info.proof, writer);
    }

    @Override
    public CarBuyOrderInfo parse(Reader reader) {
        CarSellOrderBox carSellOrderBoxToOpen = CarSellOrderBoxSerializer.getSerializer().parse(reader);
        SellOrderSpendingProof proof = SellOrderSpendingProofSerializer.getSerializer().parse(reader);

        return new CarBuyOrderInfo(carSellOrderBoxToOpen, proof);
    }
}
