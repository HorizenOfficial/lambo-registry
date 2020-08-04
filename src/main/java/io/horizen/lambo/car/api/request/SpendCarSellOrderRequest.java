package io.horizen.lambo.car.api.request;

// '.../carApi/createCar' HTTP Post request body representing class.
public class SpendCarSellOrderRequest {
    public String carSellOrderId; // hex representation of box id
    public long fee;

    // Setters to let Akka jackson JSON library to automatically deserialize the request body.

    public void setCarSellOrderId(String carSellOrderId) {
        this.carSellOrderId = carSellOrderId;
    }

    public void setFee(long fee) {
        this.fee = fee;
    }
}
