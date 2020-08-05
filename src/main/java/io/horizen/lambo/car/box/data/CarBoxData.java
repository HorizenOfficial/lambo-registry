package io.horizen.lambo.car.box.data;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.horizen.box.data.AbstractNoncedBoxData;
import com.horizen.box.data.NoncedBoxDataSerializer;
import io.horizen.lambo.car.box.CarBox;
import com.horizen.proposition.PublicKey25519Proposition;
import com.horizen.proposition.PublicKey25519PropositionSerializer;
import com.horizen.serialization.Views;
import scorex.crypto.hash.Blake2b256;

import java.util.Arrays;

import static io.horizen.lambo.car.box.data.CarRegistryBoxesDataIdsEnum.CarBoxDataId;

@JsonView(Views.Default.class)
public final class CarBoxData extends AbstractNoncedBoxData<PublicKey25519Proposition, CarBox, CarBoxData> {

    // In CarRegistry example we defined 4 main car attributes:
    private final String vin;   // Vehicle Identification Number
    private final int year;     // Car manufacture year
    private final String model; // Car Model
    private final String color; // Car color

    // Additional check on VIN length can be done as well, but not present as a part of current example.
    public CarBoxData(PublicKey25519Proposition proposition, String vin,
                      int year, String model, String color) {
        //AbstractNoncedBoxData requires value to be set in constructor. However, our car is unique object without any value in ZEN by default. So just set value to 1
        super(proposition, 1);
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
    public CarBox getBox(long nonce) {
        return new CarBox(this, nonce);
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
        return CarBoxDataSerializer.getSerializer();
    }

    @Override
    public byte boxDataTypeId() {
        return CarBoxDataId.id();
    }

    @Override
    public byte[] bytes() {
        return Bytes.concat(
                proposition().bytes(),
                Ints.toByteArray(vin.getBytes().length),
                vin.getBytes(),
                Ints.toByteArray(year),
                Ints.toByteArray(model.getBytes().length),
                model.getBytes(),
                Ints.toByteArray(color.getBytes().length),
                color.getBytes()
        );
    }

    public static CarBoxData parseBytes(byte[] bytes) {
        int offset = 0;

        PublicKey25519Proposition proposition = PublicKey25519PropositionSerializer.getSerializer()
                .parseBytes(Arrays.copyOf(bytes, PublicKey25519Proposition.getLength()));
        offset += PublicKey25519Proposition.getLength();

        int size = Ints.fromByteArray(Arrays.copyOfRange(bytes, offset, offset + Ints.BYTES));
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

        return new CarBoxData(proposition, vin, year, model, color);
    }

    @Override
    public String toString() {
        return "CarBoxData{" +
                "vin=" + vin +
                ", proposition=" + proposition() +
                ", model=" + model +
                ", color=" + color +
                ", year=" + year +
                '}';
    }
}
