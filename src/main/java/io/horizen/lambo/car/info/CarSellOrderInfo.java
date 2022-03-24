package io.horizen.lambo.car.info;

import io.horizen.lambo.car.box.CarBox;
import io.horizen.lambo.car.box.data.CarSellOrderBoxData;
import io.horizen.lambo.car.proposition.SellOrderProposition;
import com.horizen.proof.Signature25519;
import com.horizen.proposition.PublicKey25519Proposition;
import scorex.core.serialization.BytesSerializable;
import scorex.core.serialization.ScorexSerializer;

// CarBuyOrderInfo contains the minimal set of data needed to construct SellCarTransaction specific inputs an outputs.
public final class CarSellOrderInfo implements BytesSerializable {

    final CarBox carBoxToOpen;    // Car box to be spent in SellCarTransaction
    final Signature25519 proof;   // Proof to unlock the box above
    final long price;             // The Car price specified by the owner
    final PublicKey25519Proposition buyerProposition; // The potential buyer of the car.

    public CarSellOrderInfo(CarBox carBoxToOpen, Signature25519 proof, long price, PublicKey25519Proposition buyerProposition) {
        this.carBoxToOpen = carBoxToOpen;
        this.proof = proof;
        this.price = price;
        this.buyerProposition = buyerProposition;
    }

    // Input box data for unlocker construction.
    public CarBox getCarBoxToOpen() {
        return carBoxToOpen;
    }
    // Input proof data for unlocker construction.
    public Signature25519 getCarBoxSpendingProof() {
        return proof;
    }

    // Recreates output CarSellOrderBoxData with the same Car attributes specified in CarBox
    // and price/buyer specified in current CarSellOrderInfo instance.
    public CarSellOrderBoxData getSellOrderBoxData() {
        return new CarSellOrderBoxData(
                new SellOrderProposition(carBoxToOpen.proposition().pubKeyBytes(), buyerProposition.pubKeyBytes()),
                price,
                carBoxToOpen.getVin(),
                carBoxToOpen.getYear(),
                carBoxToOpen.getModel(),
                carBoxToOpen.getColor()
        );
    }

    @Override
    public byte[] bytes() {
        return serializer().toBytes(this);
    }

    @Override
    public ScorexSerializer<BytesSerializable> serializer() {
        return (ScorexSerializer) CarSellOrderInfoSerializer.getSerializer();
    }
}
