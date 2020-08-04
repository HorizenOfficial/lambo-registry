package io.horizen.lambo.car.box.data;

import com.horizen.box.data.NoncedBoxDataSerializer;
import scorex.util.serialization.Reader;
import scorex.util.serialization.Writer;

public final class CarBoxDataSerializer implements NoncedBoxDataSerializer<CarBoxData> {

    private static final CarBoxDataSerializer serializer = new CarBoxDataSerializer();

    private CarBoxDataSerializer() {
        super();
    }

    public static CarBoxDataSerializer getSerializer() {
        return serializer;
    }

    @Override
    public void serialize(CarBoxData boxData, Writer writer) {
        writer.putBytes(boxData.bytes());
    }

    @Override
    public CarBoxData parse(Reader reader) {
        return CarBoxData.parseBytes(reader.getBytes(reader.remaining()));
    }
}
