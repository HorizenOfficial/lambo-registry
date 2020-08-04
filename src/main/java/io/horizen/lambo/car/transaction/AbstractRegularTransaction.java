package io.horizen.lambo.car.transaction;

import com.horizen.box.*;
import com.horizen.box.data.*;
import com.horizen.proof.Proof;
import com.horizen.proof.Signature25519;
import com.horizen.proof.Signature25519Serializer;
import com.horizen.proposition.Proposition;
import com.horizen.transaction.SidechainTransaction;
import com.horizen.utils.ListSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// AbstractRegularTransaction is an abstract class that was designed to work with RegularBoxes only.
// This class can spent RegularBoxes and create new RegularBoxes.
// It also support fee payment logic.
public abstract class AbstractRegularTransaction extends SidechainTransaction<Proposition, NoncedBox<Proposition>> {

    protected List<byte[]> inputRegularBoxIds;
    protected List<Signature25519> inputRegularBoxProofs;
    protected List<RegularBoxData> outputRegularBoxesData;

    protected long fee;
    protected long timestamp;

    protected static ListSerializer<Signature25519> regularBoxProofsSerializer =
            new ListSerializer<>(Signature25519Serializer.getSerializer(), MAX_TRANSACTION_UNLOCKERS);
    protected static ListSerializer<RegularBoxData> regularBoxDataListSerializer =
            new ListSerializer<>(RegularBoxDataSerializer.getSerializer(), MAX_TRANSACTION_NEW_BOXES);

    private List<NoncedBox<Proposition>> newBoxes;

    public AbstractRegularTransaction(List<byte[]> inputRegularBoxIds,              // regular box ids to spent
                                      List<Signature25519> inputRegularBoxProofs,   // proofs to spent regular boxes
                                      List<RegularBoxData> outputRegularBoxesData,  // destinations where to send regular coins
                                      long fee,                                     // fee to be paid
                                      long timestamp) {                             // creation time in milliseconds from epoch
        // Number of input ids should be equal to number of proofs, otherwise transaction is for sure invalid.
        if(inputRegularBoxIds.size() != inputRegularBoxProofs.size())
            throw new IllegalArgumentException("Regular box inputs list size is different to proving signatures list size!");

        this.inputRegularBoxIds = inputRegularBoxIds;
        this.inputRegularBoxProofs = inputRegularBoxProofs;
        this.outputRegularBoxesData = outputRegularBoxesData;
        this.fee = fee;
        this.timestamp = timestamp;
    }


    // Box ids to open and proofs is expected to be aggregated together and represented as Unlockers.
    // Important: all boxes which must be opened as a part of the Transaction MUST be represented as Unlocker.
    @Override
    public List<BoxUnlocker<Proposition>> unlockers() {
        // All the transactions expected to be immutable, so we keep this list cached to avoid redundant calculations.
        List<BoxUnlocker<Proposition>> unlockers = new ArrayList<>();
        // Fill the list with the regular inputs.
        for (int i = 0; i < inputRegularBoxIds.size() && i < inputRegularBoxProofs.size(); i++) {
            int finalI = i;
            BoxUnlocker<Proposition> unlocker = new BoxUnlocker<Proposition>() {
                @Override
                public byte[] closedBoxId() {
                    return inputRegularBoxIds.get(finalI);
                }

                @Override
                public Proof boxKey() {
                    return inputRegularBoxProofs.get(finalI);
                }
            };
            unlockers.add(unlocker);
        }

        return unlockers;
    }

    // Specify the output boxes.
    // Nonce calculation algorithm is deterministic. So it's forbidden to set nonce in different way.
    // The check for proper nonce is defined in SidechainTransaction.semanticValidity method.
    // Such an algorithm is needed to disallow box ids manipulation and different vulnerabilities related to this.
    @Override
    public List<NoncedBox<Proposition>> newBoxes() {
        if(newBoxes == null) {
            newBoxes = new ArrayList<>();
            for (int i = 0; i < outputRegularBoxesData.size(); i++) {
                long nonce = getNewBoxNonce(outputRegularBoxesData.get(i).proposition(), i);
                RegularBoxData boxData = outputRegularBoxesData.get(i);
                newBoxes.add((NoncedBox) new RegularBox(boxData, nonce));
            }
        }
        return Collections.unmodifiableList(newBoxes);
    }

    @Override
    public long fee() {
        return fee;
    }

    @Override
    public long timestamp() {
        return timestamp;
    }

    @Override
    public boolean transactionSemanticValidity() {
        if(fee < 0 || timestamp < 0)
            return false;

        // check that we have enough proofs.
        if(inputRegularBoxIds.size() != inputRegularBoxProofs.size())
            return false;

        return true;
    }
}
