package io.horizen.lambo.car.box.data;

import com.horizen.box.data.BoxDataSerializer;
import com.horizen.proposition.PublicKey25519Proposition;
import com.horizen.proposition.PublicKey25519PropositionSerializer;
import scorex.util.serialization.Reader;
import scorex.util.serialization.Writer;

import java.nio.charset.StandardCharsets;

public final class CarBoxDataSerializer implements BoxDataSerializer<CarBoxData> {

    private static final CarBoxDataSerializer serializer = new CarBoxDataSerializer();

    private CarBoxDataSerializer() {
        super();
    }

    public static CarBoxDataSerializer getSerializer() {
        return serializer;
    }

    @Override
    public void serialize(CarBoxData boxData, Writer writer) {
        PublicKey25519PropositionSerializer.getSerializer().serialize(boxData.proposition(), writer);
        byte[] vinBytes = boxData.getVin().getBytes(StandardCharsets.UTF_8);
        writer.putInt(vinBytes.length);
        writer.putBytes(vinBytes);
        writer.putInt(boxData.getYear());
        byte[] modelBytes = boxData.getModel().getBytes(StandardCharsets.UTF_8);
        writer.putInt(modelBytes.length);
        writer.putBytes(modelBytes);
        byte[] colorBytes = boxData.getColor().getBytes(StandardCharsets.UTF_8);
        writer.putInt(colorBytes.length);
        writer.putBytes(colorBytes);
    }

    @Override
    public CarBoxData parse(Reader reader) {
        PublicKey25519Proposition proposition = PublicKey25519PropositionSerializer.getSerializer().parse(reader);
        int vinBytesLength = reader.getInt();
        String vin = new String(reader.getBytes(vinBytesLength), StandardCharsets.UTF_8);
        int year = reader.getInt();
        int modelBytesLength = reader.getInt();
        String model = new String(reader.getBytes(modelBytesLength), StandardCharsets.UTF_8);
        int colorBytesLength = reader.getInt();
        String color = new String(reader.getBytes(colorBytesLength), StandardCharsets.UTF_8);
        return new CarBoxData(proposition, vin, year, model, color);
    }
}
