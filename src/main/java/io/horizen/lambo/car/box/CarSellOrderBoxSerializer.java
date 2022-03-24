package io.horizen.lambo.car.box;

import com.horizen.box.BoxSerializer;
import io.horizen.lambo.car.box.data.CarSellOrderBoxData;
import io.horizen.lambo.car.box.data.CarSellOrderBoxDataSerializer;
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
    public void serialize(CarSellOrderBox box, Writer writer) {
        writer.putLong(box.nonce());
        CarSellOrderBoxDataSerializer.getSerializer().serialize(box.getBoxData(), writer);
    }

    @Override
    public CarSellOrderBox parse(Reader reader) {
        long nonce = reader.getLong();
        CarSellOrderBoxData boxData = CarSellOrderBoxDataSerializer.getSerializer().parse(reader);

        return new CarSellOrderBox(boxData, nonce);
    }
}
