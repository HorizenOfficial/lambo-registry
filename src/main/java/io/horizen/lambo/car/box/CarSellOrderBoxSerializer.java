package io.horizen.lambo.car.box;

import com.horizen.box.BoxSerializer;
import scorex.util.serialization.Reader;
import scorex.util.serialization.Writer;

public final class CarSellOrderBoxSerializer implements BoxSerializer<CarSellOrderBox> {

    private static final CarSellOrderBoxSerializer serializer = new CarSellOrderBoxSerializer();

    private CarSellOrderBoxSerializer() {
        super();
    }

    public static CarSellOrderBoxSerializer getSerializer() {
        return serializer;
    }

    @Override
    public void serialize(CarSellOrderBox carSellOrder, Writer writer) {
        writer.putBytes(carSellOrder.bytes());
    }

    @Override
    public CarSellOrderBox parse(Reader reader) {
        return CarSellOrderBox.parseBytes(reader.getBytes(reader.remaining()));
    }
}
