package io.horizen.lambo;

import com.google.inject.Inject;
import com.horizen.block.SidechainBlock;
import com.horizen.box.Box;
import com.horizen.proposition.Proposition;
import com.horizen.state.ApplicationState;
import com.horizen.state.SidechainStateReader;
import com.horizen.transaction.BoxTransaction;
import io.horizen.lambo.car.box.CarBox;
import io.horizen.lambo.car.box.CarSellOrderBox;
import io.horizen.lambo.car.services.CarInfoDBService;
import io.horizen.lambo.car.transaction.CarDeclarationTransaction;
import scala.util.Success;
import scala.util.Try;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class CarRegistryApplicationState implements ApplicationState {
	
	private CarInfoDBService carInfoDbService;

	@Inject
	public CarRegistryApplicationState(CarInfoDBService carInfoDbService) {
	    this.carInfoDbService = carInfoDbService;
	}
	
    @Override
    public boolean validate(SidechainStateReader stateReader, SidechainBlock block) {
	    return true;
    }

    @Override
    public boolean validate(SidechainStateReader stateReader, BoxTransaction<Proposition, Box<Proposition>> transaction) {
        // we go though all CarDeclarationTransactions and verify that each CarBox reflects to unique Car.
        if (CarDeclarationTransaction.class.isAssignableFrom(transaction.getClass())){
            Set<String> vinList = carInfoDbService.extractVinFromBoxes(transaction.newBoxes());
            for (String vin : vinList) {
                if (! carInfoDbService.validateVin(vin, null)){
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Try<ApplicationState> onApplyChanges(SidechainStateReader stateReader,
                                                byte[] version,
                                                List<Box<Proposition>> newBoxes, List<byte[]> boxIdsToRemove) {
        //we update the Car info database. The data from it will be used during validation.

        //collect the vin to be added: the ones declared in new boxes
        Set<String> vinToAdd = carInfoDbService.extractVinFromBoxes(newBoxes);
        //collect the vin to be removed: the ones contained in the removed boxes that are not present in the prevoius list
        Set<String> vinToRemove = new HashSet<>();
        for (byte[] boxId : boxIdsToRemove) {
            stateReader.getClosedBox(boxId).ifPresent( box -> {
                    if (box instanceof CarBox){
                        String vin = ((CarBox)box).getVin();
                        if (!vinToAdd.contains(vin)){
                            vinToRemove.add(vin);
                        }
                    } else if (box instanceof CarSellOrderBox){
                        String vin = ((CarSellOrderBox)box).getVin();
                        if (!vinToAdd.contains(vin)){
                            vinToRemove.add(vin);
                        }
                    }
                }
            );
        }
        carInfoDbService.updateVin(version, vinToAdd, vinToRemove);
        return new Success<>(this);
    }


    @Override
    public Try<ApplicationState> onRollback(byte[] version) {
        carInfoDbService.rollback(version);
        return new Success<>(this);
    }
}
