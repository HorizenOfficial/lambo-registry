package io.horizen.lambo.car.box;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.horizen.box.AbstractBox;
import com.horizen.box.BoxSerializer;
import io.horizen.lambo.car.box.data.CarSellOrderBoxData;
import io.horizen.lambo.car.proposition.SellOrderProposition;
import com.horizen.serialization.Views;

// Declare default JSON view for CarSellOrderBox object. Will automatically collect all getters except ignored ones.
@JsonView(Views.Default.class)
@JsonIgnoreProperties({"boxData", "carId"})
public final class CarSellOrderBox extends AbstractBox<SellOrderProposition, CarSellOrderBoxData, CarSellOrderBox> {

    public CarSellOrderBox(CarSellOrderBoxData boxData, long nonce) {
        super(boxData, nonce);
    }

    @Override
    public BoxSerializer serializer() {
        return CarSellOrderBoxSerializer.getSerializer();
    }

    @Override
    public byte boxTypeId() {
        return CarRegistryBoxesIdsEnum.CarSellOrderBoxId.id();
    }

    public CarSellOrderBoxData getBoxData() {
        return this.boxData;
    }

    // Set sell order attributes getters, that is used to automatically construct JSON view:
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

    public long getPrice() {
        return value();
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
