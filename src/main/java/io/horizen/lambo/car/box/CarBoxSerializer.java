package io.horizen.lambo.car.box;

import com.horizen.box.BoxSerializer;
import scorex.util.serialization.Reader;
import scorex.util.serialization.Writer;

public final class CarBoxSerializer implements BoxSerializer<CarBox> {

    private static final CarBoxSerializer serializer = new CarBoxSerializer();

    private CarBoxSerializer() {
        super();
    }

    public static CarBoxSerializer getSerializer() {
        return serializer;
    }

    @Override
    public void serialize(CarBox box, Writer writer) {
        writer.putBytes(box.bytes());
    }

    @Override
    public CarBox parse(Reader reader) {
        return CarBox.parseBytes(reader.getBytes(reader.remaining()));
    }

}
