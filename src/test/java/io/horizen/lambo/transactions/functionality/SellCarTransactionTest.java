package io.horizen.lambo.transactions.functionality;

import io.horizen.lambo.transactions.fixtures.parameters.SellCarParameters;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static io.horizen.lambo.transactions.fixtures.parameters.AbstractParameters.canCreate;
import static io.horizen.lambo.transactions.fixtures.parameters.AbstractParameters.create;
import static io.horizen.lambo.transactions.fixtures.BoxFixtures.*;
import static org.junit.Assert.*;


public class SellCarTransactionTest {

    SellCarParameters validParams;

    @Before
    public void initialiazeParams(){ validParams = new SellCarParameters(); }

    @Test
    public void creation(){
        // Tests for the common functionality
        AbstractRegularTransactionTest.creation(validParams);

        // SellCar specific test: sellOrderInfo is null
        assertFalse("CarSellOrderInfo: Exception expected", canCreate(validParams.sellOrderInfo(null)));
    }

    @Test
    public void semanticValidity(){
        // Test 1: create semantically valid transaction
        assertTrue("Transaction expected to be semantically Valid",
                create(validParams).semanticValidity());

        // Test 2: create semantically invalid transaction - inputs list contains duplicates
        byte[] boxId = getRandomBoxId();
        assertFalse("Transaction expected to be semantically Invalid",
                create(validParams.inputsIds(Arrays.asList(boxId, boxId))).semanticValidity());
    }
}
