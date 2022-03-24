package io.horizen.lambo.car.box;

import com.horizen.box.BoxSerializer;
import io.horizen.lambo.car.box.data.CarBoxData;
import io.horizen.lambo.car.box.data.CarBoxDataSerializer;
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
        writer.putLong(box.nonce());
        CarBoxDataSerializer.getSerializer().serialize(box.getBoxData(), writer);
    }

    @Override
    public CarBox parse(Reader reader) {
        long nonce = reader.getLong();
        CarBoxData boxData = CarBoxDataSerializer.getSerializer().parse(reader);

        return new CarBox(boxData, nonce);
    }
}
