package io.horizen.lambo.transactions.fixtures.parameters;

import com.horizen.box.data.RegularBoxData;
import com.horizen.proof.Signature25519;
import io.horizen.lambo.car.box.data.CarBoxData;

import java.util.List;

import static io.horizen.lambo.transactions.fixtures.BoxFixtures.getPublicKey25519Proposition;

public class CarDeclarationParameters extends AbstractParameters {
    public final CarBoxData carBoxData;

    public CarDeclarationParameters(){
        carBoxData = new CarBoxData(getPublicKey25519Proposition(), "vin", 1000, "model", "color");
    }

    public CarDeclarationParameters(
            List<byte[]> inputsIds,
            List<Signature25519> proofs,
            List<RegularBoxData> outputsData,
            CarBoxData carBoxData,
            long fee,
            long timestamp){
        super(inputsIds, proofs, outputsData, fee, timestamp);
        this.carBoxData = carBoxData;
    }

    // Creates a new instance of the current parameters with re-initialization of carBoxData
    public CarDeclarationParameters boxData(CarBoxData _boxData) {
        return new CarDeclarationParameters(inputsIds, proofs, outputsData, _boxData, fee, timestamp);
    }

    @Override
    CarDeclarationParameters newParams(
            List<byte[]> inputsIds,
            List<Signature25519> proofs,
            List<RegularBoxData> outputsData,
            long fee,
            long timestamp){
        return new CarDeclarationParameters(inputsIds, proofs, outputsData, carBoxData, fee, timestamp);
    }
}
