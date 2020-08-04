package io.horizen.lambo.car.box.data;

import com.horizen.box.data.NoncedBoxDataSerializer;
import scorex.util.serialization.Reader;
import scorex.util.serialization.Writer;

public final class CarSellOrderBoxDataSerializer implements NoncedBoxDataSerializer<CarSellOrderBoxData> {

    private static final CarSellOrderBoxDataSerializer serializer = new CarSellOrderBoxDataSerializer();

    private CarSellOrderBoxDataSerializer() {
        super();
    }

    public static CarSellOrderBoxDataSerializer getSerializer() {
        return serializer;
    }

    @Override
    public void serialize(CarSellOrderBoxData boxData, Writer writer) {
        writer.putBytes(boxData.bytes());
    }

    @Override
    public CarSellOrderBoxData parse(Reader reader) {
        return CarSellOrderBoxData.parseBytes(reader.getBytes(reader.remaining()));
    }
}