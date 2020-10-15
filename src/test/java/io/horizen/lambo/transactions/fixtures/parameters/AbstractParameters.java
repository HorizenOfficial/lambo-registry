package io.horizen.lambo.transactions.fixtures.parameters;

import com.horizen.box.data.RegularBoxData;
import com.horizen.proof.Signature25519;
import com.horizen.transaction.AbstractRegularTransaction;
import io.horizen.lambo.car.transaction.BuyCarTransaction;
import io.horizen.lambo.car.transaction.CarDeclarationTransaction;
import io.horizen.lambo.car.transaction.SellCarTransaction;

import java.util.*;

import static io.horizen.lambo.transactions.fixtures.BoxFixtures.*;

// This abstract class defines parameters of the AbstractRegularTransaction
abstract public class AbstractParameters {

    // Parameters for the AbstractRegularTransaction which is a base for the Lambo-transactions
    public final List<byte[]> inputsIds;
    public final List<Signature25519> proofs;
    public final List<RegularBoxData> outputsData;
    public final long fee;
    public final long timestamp;

    // Initialization of the AbstractRegularTransaction parameters is random by default
    AbstractParameters(){
        // List of 2 random Ids
        inputsIds = Arrays.asList(getRandomBoxId(), getRandomBoxId());

        // List of 2 random proofs
        proofs = new ArrayList<>();
        proofs.add(getRandomSignature25519());
        proofs.add(getRandomSignature25519());

        // List of one output box
        outputsData = new ArrayList<>();
        outputsData.add(getRegularBoxData());

        // Random non-negative fee and timestamp
        fee = new Random().nextInt(10000);
        timestamp = new Random().nextInt(10000);
    }

    AbstractParameters(
            List<byte[]> inputsIds,
            List<Signature25519> proofs,
            List<RegularBoxData> outputsData,
            long fee,
            long timestamp){
        this.inputsIds = inputsIds;
        this.proofs = proofs;
        this.outputsData = outputsData;
        this.fee = fee;
        this.timestamp = timestamp;
    }

    // Creates a new instance of an AbstractParameters inheritor
    abstract AbstractParameters newParams(
            List<byte[]> inputsIds,
            List<Signature25519> proofs,
            List<RegularBoxData> outputsData,
            long fee,
            long timestamp);

    // Methods for creating a new instance of the current parameters with re-initialization of one of them with a specified value
    public AbstractParameters inputsIds(List<byte[]> _inputsIds){
        return newParams(_inputsIds, proofs, outputsData, fee, timestamp);
    }
    public AbstractParameters proofs(List<Signature25519> _proofs){
        return newParams(inputsIds, _proofs, outputsData, fee, timestamp);
    }
    public AbstractParameters outputsData(List<RegularBoxData> _outputsData){
        return newParams(inputsIds, proofs, _outputsData, fee, timestamp);
    }
    public AbstractParameters fee(long _fee) {
        return newParams(inputsIds, proofs, outputsData, _fee, timestamp);
    }
    public AbstractParameters timestamp(long _timestamp) {
        return newParams(inputsIds, proofs, outputsData, fee, _timestamp);
    }

    // Static method for creating a transaction which corresponds to a current parameters set
    // The Lambo-transactions constructors can throw an exception so an exception handler is used
    private static Optional<AbstractRegularTransaction> tryToCreate(AbstractParameters p){
        Optional<AbstractRegularTransaction> transactionOpt = Optional.empty();
        try {
            if(p instanceof CarDeclarationParameters){
                transactionOpt = Optional.of(new CarDeclarationTransaction(p.inputsIds, p.proofs, p.outputsData, ((CarDeclarationParameters)p).carBoxData, p.fee, p.timestamp));
            }
            else if(p instanceof SellCarParameters){
                transactionOpt = Optional.of(new SellCarTransaction(p.inputsIds, p.proofs, p.outputsData, ((SellCarParameters)p).carSellOrderInfo, p.fee, p.timestamp));
            }
            else if(p instanceof BuyCarParameters){
                transactionOpt = Optional.of(new BuyCarTransaction(p.inputsIds, p.proofs, p.outputsData, ((BuyCarParameters)p).carBuyOrderInfo, p.fee, p.timestamp));
            }
        }
        catch (Exception e) {
            return Optional.empty();
        }
        return transactionOpt;
    }

    // Checks if transaction can be created from the specified parameters
    public static boolean canCreate(AbstractParameters p){
        return tryToCreate(p).isPresent();
    }

    // Creates transaction assuming that specified parameters are correct
    public static AbstractRegularTransaction create(AbstractParameters p){
        return tryToCreate(p).get();
    }
}
