package io.horizen.lambo.transactions.fixtures.parameters;

import com.horizen.box.data.RegularBoxData;
import com.horizen.proof.Signature25519;
import io.horizen.lambo.car.box.CarBox;
import io.horizen.lambo.car.box.data.CarBoxData;
import io.horizen.lambo.car.info.CarSellOrderInfo;

import java.util.List;
import java.util.Random;

import static io.horizen.lambo.transactions.fixtures.BoxFixtures.getPublicKey25519Proposition;
import static io.horizen.lambo.transactions.fixtures.BoxFixtures.getRandomSignature25519;

public class SellCarParameters extends AbstractParameters {
    public final CarSellOrderInfo carSellOrderInfo;

    public SellCarParameters(){
        CarBox carBox = new CarBox(new CarBoxData(getPublicKey25519Proposition(), "vin", 1000, "model", "color"), new Random().nextInt());
        carSellOrderInfo = new CarSellOrderInfo(carBox, getRandomSignature25519(), 100, getPublicKey25519Proposition());
    }

    public SellCarParameters(
            List<byte[]> inputsIds,
            List<Signature25519> proofs,
            List<RegularBoxData> outputsData,
            CarSellOrderInfo carSellOrderInfo,
            long fee,
            long timestamp){
        super(inputsIds, proofs, outputsData, fee, timestamp);
        this.carSellOrderInfo = carSellOrderInfo;
    }

    // Creates a new instance of the current parameters with re-initialization of carSellOrderInfo
    public SellCarParameters sellOrderInfo(CarSellOrderInfo _sellOrderInfo) {
        return new SellCarParameters(inputsIds, proofs, outputsData, _sellOrderInfo, fee, timestamp);
    }

    @Override
    SellCarParameters newParams(
            List<byte[]> inputsIds,
            List<Signature25519> proofs,
            List<RegularBoxData> outputsData,
            long fee,
            long timestamp){
        return new SellCarParameters(inputsIds, proofs, outputsData, carSellOrderInfo, fee, timestamp);
    }
}
