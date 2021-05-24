package io.horizen.lambo.transactions.fixtures.parameters;

import com.horizen.box.data.RegularBoxData;
import com.horizen.proof.Signature25519;
import io.horizen.lambo.car.box.CarSellOrderBox;
import io.horizen.lambo.car.box.data.CarSellOrderBoxData;
import io.horizen.lambo.car.info.CarBuyOrderInfo;
import io.horizen.lambo.car.proof.SellOrderSpendingProof;
import io.horizen.lambo.car.proposition.SellOrderProposition;

import java.util.List;
import java.util.Random;

import static io.horizen.lambo.transactions.fixtures.BoxFixtures.getPublicKey25519Proposition;
import static io.horizen.lambo.transactions.fixtures.BoxFixtures.getRandomSignature25519;

public class BuyCarParameters extends AbstractParameters {
    public final CarBuyOrderInfo carBuyOrderInfo;

    public BuyCarParameters(){
        SellOrderProposition carSellOrderProposition = new SellOrderProposition(getPublicKey25519Proposition().bytes(), getPublicKey25519Proposition().bytes());
        CarSellOrderBox carSellOrderBoxBox = new CarSellOrderBox(new CarSellOrderBoxData(carSellOrderProposition, 100, "vin", 1000, "model", "color"), new Random().nextInt());
        SellOrderSpendingProof proof = new SellOrderSpendingProof(getRandomSignature25519().bytes(), true);

        carBuyOrderInfo = new CarBuyOrderInfo(carSellOrderBoxBox, proof);
    }

    public BuyCarParameters(
            List<byte[]> inputsIds,
            List<Signature25519> proofs,
            List<RegularBoxData> outputsData,
            CarBuyOrderInfo carBuyOrderInfo,
            long fee,
            long timestamp){
        super(inputsIds, proofs, outputsData, fee, timestamp);
        this.carBuyOrderInfo = carBuyOrderInfo;
    }

    // Creates a new instance of the current parameters with re-initialization of carBuyOrderInfo
    public BuyCarParameters buyOrderInfo(CarBuyOrderInfo _buyOrderInfo) {
        return new BuyCarParameters(inputsIds, proofs, outputsData, _buyOrderInfo, fee, timestamp);
    }

    @Override
    BuyCarParameters newParams(
            List<byte[]> inputsIds,
            List<Signature25519> proofs,
            List<RegularBoxData> outputsData,
            long fee,
            long timestamp){
        return new BuyCarParameters(inputsIds, proofs, outputsData, carBuyOrderInfo, fee, timestamp);
    }
}
