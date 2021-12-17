package io.horizen.lambo.car.box;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.horizen.box.AbstractBox;
import com.horizen.box.BoxSerializer;
import io.horizen.lambo.car.box.data.CarBoxDataSerializer;
import io.horizen.lambo.car.box.data.CarBoxData;
import com.horizen.proposition.PublicKey25519Proposition;
import com.horizen.serialization.Views;

import java.util.Arrays;

import static io.horizen.lambo.car.box.CarRegistryBoxesIdsEnum.CarBoxId;

// Declare default JSON view for CarBox object. Will automatically collect all getters except ignored ones.
@JsonView(Views.Default.class)
@JsonIgnoreProperties({"carId", "value"})
public final class CarBox extends AbstractBox<PublicKey25519Proposition, CarBoxData, CarBox> {

    public CarBox(CarBoxData boxData, long nonce) {
        super(boxData, nonce);
    }

    @Override
    public BoxSerializer serializer() {
        return CarBoxSerializer.getSerializer();
    }

    @Override
    public byte boxTypeId() {
        return CarBoxId.id();
    }

    @Override
    public byte[] bytes() {
        return Bytes.concat(
                Longs.toByteArray(nonce),
                CarBoxDataSerializer.getSerializer().toBytes(boxData)
        );
    }

    public static CarBox parseBytes(byte[] bytes) {
        long nonce = Longs.fromByteArray(Arrays.copyOf(bytes, Longs.BYTES));
        CarBoxData boxData = CarBoxDataSerializer.getSerializer().parseBytes(Arrays.copyOfRange(bytes, Longs.BYTES, bytes.length));

        return new CarBox(boxData, nonce);
    }

    // Set car attributes getters, that is used to automatically construct JSON view:

    public String getVin() {
        return boxData.getVin();
    }

    public int getYear() {
        return boxData.getYear();
    }

    public String getModel() {
        return boxData.getModel();
    }

    public String getColor() {
        return boxData.getColor();
    }

    public byte[] getCarId() {
        return Bytes.concat(
                getVin().getBytes(),
                Ints.toByteArray(getYear()),
                getModel().getBytes(),
                getColor().getBytes()
        );
    }
}
