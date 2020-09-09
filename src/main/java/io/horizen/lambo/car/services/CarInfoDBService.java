package io.horizen.lambo.car.services;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.horizen.box.Box;
import com.horizen.node.NodeMemoryPool;
import com.horizen.proposition.Proposition;
import com.horizen.storage.Storage;
import com.horizen.transaction.BoxTransaction;
import com.horizen.utils.ByteArrayWrapper;
import com.horizen.utils.Pair;
import io.horizen.lambo.car.box.CarBox;
import io.horizen.lambo.car.box.CarSellOrderBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

/**
 * This service manages a local db with the list of all veichle identification numbers (vin) declared on the chain.
 * The vin could be present inside two type of boxes: CarBox and CarSellOrderBox.
 */
public class CarInfoDBService {

    private Storage carInfoStorage;
    protected Logger log = LoggerFactory.getLogger(CarInfoDBService.class.getName());

    @Inject
    public CarInfoDBService(@Named("CarInfoStorage") Storage carInfoStorage){
        this.carInfoStorage = carInfoStorage;
    }

    public void updateVin(byte[] version, Set<String> vinToAdd,  Set<String> vinToRemove){
        log.debug("carInfoStorage updateVin");
        log.debug(" vinToAdd "+vinToAdd.size());
        log.debug(" vinToRemove "+vinToRemove.size());
        List<Pair<ByteArrayWrapper, ByteArrayWrapper>> toUpdate = new ArrayList<>(vinToAdd.size());
        List<ByteArrayWrapper> toRemove = new ArrayList<>(vinToRemove.size());
        vinToAdd.forEach(ele -> {
            toUpdate.add(buildDBElement(ele));
        });
        vinToRemove.forEach(ele -> {
            toRemove.add(buildDBElement(ele).getKey());
        });
        carInfoStorage.update(new ByteArrayWrapper(version), toUpdate, toRemove);
        log.debug("carInfoStorage now contains: "+carInfoStorage.getAll().size()+" elements");
    }


    /**
     * Validate the given vehicle identification number against the db list and (optionally) the mempool transactions.
     * @param vin the vehicle identification number to check
     * @param memoryPool if not null, the vin is checked also against the mempool transactions
     * @return true if the vin is valid (not already declared)
     */
    public boolean validateVin(String vin, NodeMemoryPool memoryPool){
        if (carInfoStorage.get(buildDBElement(vin).getKey()).isPresent()){
            return false;
        }
        //in the vin is not found, and the mempool was provided, we check also there
        if (memoryPool != null) {
            for (BoxTransaction<Proposition, Box<Proposition>> transaction : memoryPool.getTransactions()) {
                Set<String> vinInMempool = extractVinFromBoxes(transaction.newBoxes());
                if (vinInMempool.contains(vin)){
                    return false;
                }
            }
        }
        //if we arrive here, the vin is valid
        return true;
    }

    public void rollback(byte[] version) {
        carInfoStorage.rollback(new ByteArrayWrapper(version));
    }

    /**
     * Extracts the list of vehicle identification numbers (vin) declared in the given box list.
     * The vin could be present inside two type of boxes: CarBox and CarSellOrderBox
     */
    public Set<String> extractVinFromBoxes(List<Box<Proposition>> boxes){
        Set<String> vinList = new HashSet<String>();
        for (Box<Proposition> currentBox : boxes) {
            if (CarBox.class.isAssignableFrom(currentBox.getClass())){
                String vin  = CarBox.parseBytes(currentBox.bytes()).getVin();
                vinList.add(vin);
            } else if (CarSellOrderBox.class.isAssignableFrom(currentBox.getClass())){
                String vin  = CarSellOrderBox.parseBytes(currentBox.bytes()).getVin();
                vinList.add(vin);
            }
        }
        return vinList;
    }



    private Pair<ByteArrayWrapper, ByteArrayWrapper> buildDBElement(String vin){
        //we use a fixed key size of 32, which is the default of iohk.iodb used as underline storage
        ByteBuffer keyBuffer = ByteBuffer.allocate(32).put(vin.getBytes());
        ByteArrayWrapper keyWrapper = new ByteArrayWrapper(keyBuffer.array());
        //the value is not important (we need just a key set, each key is a vin)
        ByteArrayWrapper valueWrapper = new ByteArrayWrapper(new byte[1]);
        return new Pair<>(keyWrapper, valueWrapper);
    }

}
