package io.horizen.lambo.car.box.data;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.horizen.box.data.AbstractNoncedBoxData;
import com.horizen.box.data.NoncedBoxDataSerializer;
import io.horizen.lambo.car.box.CarSellOrderBox;
import io.horizen.lambo.car.proposition.SellOrderProposition;
import io.horizen.lambo.car.proposition.SellOrderPropositionSerializer;
import com.horizen.serialization.Views;
import scorex.crypto.hash.Blake2b256;

import java.util.Arrays;

@JsonView(Views.Default.class)
public final class CarSellOrderBoxData extends AbstractNoncedBoxData<SellOrderProposition, CarSellOrderBox, CarSellOrderBoxData> {

    // Car sell order attributes is similar to car attributes.
    // The only change is that Sell order contains the car price as well.
    private final String vin;
    private final int year;
    private final String model;
    private final String color;

    public CarSellOrderBoxData(SellOrderProposition proposition, long price, String vin,
                      int year, String model, String color) {
        super(proposition, price);
        this.vin = vin;
        this.year = year;
        this.model = model;
        this.color = color;
    }

    public String getVin() {
        return vin;
    }

    public int getYear() {
        return year;
    }

    public String getModel() {
        return model;
    }

    public String getColor() {
        return color;
    }


    @Override
    public CarSellOrderBox getBox(long nonce) {
        return new CarSellOrderBox(this, nonce);
    }

    @Override
    public byte[] customFieldsHash() {
        return Blake2b256.hash(
                Bytes.concat(
                        vin.getBytes(),
                        Ints.toByteArray(year),
                        model.getBytes(),
                        color.getBytes()));
    }

    @Override
    public NoncedBoxDataSerializer serializer() {
        return CarSellOrderBoxDataSerializer.getSerializer();
    }

    @Override
    public byte[] bytes() {
        return Bytes.concat(
                Ints.toByteArray(proposition().bytes().length),
                proposition().bytes(),
                Longs.toByteArray(value()),
                Ints.toByteArray(vin.getBytes().length),
                vin.getBytes(),
                Ints.toByteArray(year),
                Ints.toByteArray(model.getBytes().length),
                model.getBytes(),
                Ints.toByteArray(color.getBytes().length),
                color.getBytes()
        );
    }

    public static CarSellOrderBoxData parseBytes(byte[] bytes) {
        int offset = 0;

        int size = Ints.fromByteArray(Arrays.copyOfRange(bytes, offset, offset + Ints.BYTES));
        offset += Ints.BYTES;

        SellOrderProposition proposition = SellOrderPropositionSerializer.getSerializer()
                .parseBytes(Arrays.copyOfRange(bytes, offset, offset + size));
        offset += size;

        long price = Longs.fromByteArray(Arrays.copyOfRange(bytes, offset, offset + Longs.BYTES));
        offset += Longs.BYTES;

        size = Ints.fromByteArray(Arrays.copyOfRange(bytes, offset, offset + Ints.BYTES));
        offset += Ints.BYTES;

        String vin = new String(Arrays.copyOfRange(bytes, offset, offset + size));
        offset += size;

        int year = Ints.fromByteArray(Arrays.copyOfRange(bytes, offset, offset + Ints.BYTES));
        offset += Ints.BYTES;

        size = Ints.fromByteArray(Arrays.copyOfRange(bytes, offset, offset + Ints.BYTES));
        offset += Ints.BYTES;

        String model = new String(Arrays.copyOfRange(bytes, offset, offset + size));
        offset += size;

        size = Ints.fromByteArray(Arrays.copyOfRange(bytes, offset, offset + Ints.BYTES));
        offset += Ints.BYTES;

        String color = new String(Arrays.copyOfRange(bytes, offset, offset + size));

        return new CarSellOrderBoxData(proposition, price, vin, year, model, color);
    }

    @Override
    public String toString() {
        return "CarSellOrderBoxData{" +
                "vin=" + vin +
                ", proposition=" + proposition() +
                ", value=" + value() +
                ", model=" + model +
                ", color=" + color +
                ", year=" + year +
                '}';
    }
}
