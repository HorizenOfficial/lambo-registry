package io.horizen.lambo.car.box.data;

import com.horizen.box.data.BoxDataSerializer;
import com.horizen.proposition.PublicKey25519Proposition;
import com.horizen.proposition.PublicKey25519PropositionSerializer;
import io.horizen.lambo.car.proposition.SellOrderProposition;
import io.horizen.lambo.car.proposition.SellOrderPropositionSerializer;
import scorex.util.serialization.Reader;
import scorex.util.serialization.Writer;

import java.nio.charset.StandardCharsets;

public final class CarSellOrderBoxDataSerializer implements BoxDataSerializer<CarSellOrderBoxData> {

    private static final CarSellOrderBoxDataSerializer serializer = new CarSellOrderBoxDataSerializer();

    private CarSellOrderBoxDataSerializer() {
        super();
    }

    public static CarSellOrderBoxDataSerializer getSerializer() {
        return serializer;
    }

    @Override
    public void serialize(CarSellOrderBoxData boxData, Writer writer) {
        SellOrderPropositionSerializer.getSerializer().serialize(boxData.proposition(), writer);
        writer.putLong(boxData.value());
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
    public CarSellOrderBoxData parse(Reader reader) {
        SellOrderProposition proposition = SellOrderPropositionSerializer.getSerializer().parse(reader);
        long price = reader.getLong();
        int vinBytesLength = reader.getInt();
        String vin = new String(reader.getBytes(vinBytesLength), StandardCharsets.UTF_8);
        int year = reader.getInt();
        int modelBytesLength = reader.getInt();
        String model = new String(reader.getBytes(modelBytesLength), StandardCharsets.UTF_8);
        int colorBytesLength = reader.getInt();
        String color = new String(reader.getBytes(colorBytesLength), StandardCharsets.UTF_8);
        return new CarSellOrderBoxData(proposition, price, vin, year, model, color);
    }
}