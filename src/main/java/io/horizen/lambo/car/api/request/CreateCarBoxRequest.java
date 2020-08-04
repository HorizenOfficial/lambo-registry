package io.horizen.lambo.car.api.request;

// '.../carApi/createCar' HTTP Post request body representing class.
public class CreateCarBoxRequest {
    public String vin;
    public int year;
    public String model;
    public String color;
    public String proposition; // hex representation of public key proposition
    public long fee;


    // Setters to let Akka jackson JSON library to automatically deserialize the request body.

    public void setVin(String vin) {
        this.vin = vin;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setProposition(String proposition) {
        this.proposition = proposition;
    }

    public void setFee(long fee) {
        this.fee = fee;
    }
}
