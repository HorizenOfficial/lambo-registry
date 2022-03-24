package io.horizen.lambo.car.box.data;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.horizen.box.data.AbstractBoxData;
import com.horizen.box.data.BoxDataSerializer;
import io.horizen.lambo.car.box.CarBox;
import com.horizen.proposition.PublicKey25519Proposition;
import com.horizen.serialization.Views;
import scorex.crypto.hash.Blake2b256;


@JsonView(Views.Default.class)
public final class CarBoxData extends AbstractBoxData<PublicKey25519Proposition, CarBox, CarBoxData> {

    // In CarRegistry example we defined 4 main car attributes:
    private final String vin;   // Vehicle Identification Number
    private final int year;     // Car manufacture year
    private final String model; // Car Model
    private final String color; // Car color

    // Additional check on VIN length can be done as well, but not present as a part of current example.
    public CarBoxData(PublicKey25519Proposition proposition, String vin,
                      int year, String model, String color) {
        //AbstractBoxData requires value to be set in constructor. However, our car is unique object without any value in ZEN by default. So just set value to 1
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
    public BoxDataSerializer serializer() {
        return CarBoxDataSerializer.getSerializer();
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
