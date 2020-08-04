package io.horizen.lambo.car.api.request;

// '.../carApi/acceptCarSellOrder' and '.../carApi/cancelCarSellOrder'  HTTP Post requests body representing class.
public class CreateCarSellOrderRequest {
    public String carBoxId; // hex representation of box id
    public String buyerProposition; // hex representation of public key proposition
    public long sellPrice;
    public long fee;

    // Setters to let Akka jackson JSON library to automatically deserialize the request body.

    public void setCarBoxId(String carBoxId) {
        this.carBoxId = carBoxId;
    }

    public void setBuyerProposition(String buyerProposition) {
        this.buyerProposition = buyerProposition;
    }

    public void setSellPrice(long sellPrice) {
        this.sellPrice = sellPrice;
    }

    public void setFee(int fee) {
        this.fee = fee;
    }
}
