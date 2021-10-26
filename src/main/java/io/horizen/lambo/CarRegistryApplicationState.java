package io.horizen.lambo;

import com.horizen.block.SidechainBlock;
import com.horizen.box.Box;
import com.horizen.proposition.Proposition;
import com.horizen.state.ApplicationState;
import com.horizen.state.SidechainStateReader;
import com.horizen.transaction.BoxTransaction;
import scala.util.Success;
import scala.util.Try;

import java.util.List;

// There is no custom logic for Car registry State now.
// TODO: prevent the declaration of CarBoxes which car information already exists in the previously added CarBoxes or CarSellOrderBoxes.
public class CarRegistryApplicationState implements ApplicationState {
    @Override
    public void validate(SidechainStateReader stateReader, SidechainBlock block) throws IllegalArgumentException {}

    @Override
    public void validate(SidechainStateReader stateReader, BoxTransaction<Proposition, Box<Proposition>> transaction) throws IllegalArgumentException {
        // TODO: here we expect to go though all CarDeclarationTransactions and verify that each CarBox reflects to unique Car.
    }

    @Override
    public Try<ApplicationState> onApplyChanges(SidechainStateReader stateReader, byte[] version, List<Box<Proposition>> newBoxes, List<byte[]> boxIdsToRemove) {
        // TODO: here we expect to update Car info database. The data from it will be used during validation.
        return new Success<>(this);
    }

    @Override
    public Try<ApplicationState> onRollback(byte[] version) {
        // TODO: rollback car info database to certain point.
        return new Success<>(this);
    }
}
