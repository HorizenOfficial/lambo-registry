package io.horizen.lambo.transactions.functionality;

import io.horizen.lambo.transactions.fixtures.parameters.BuyCarParameters;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static io.horizen.lambo.transactions.fixtures.parameters.AbstractParameters.canCreate;
import static io.horizen.lambo.transactions.fixtures.parameters.AbstractParameters.create;
import static io.horizen.lambo.transactions.fixtures.BoxFixtures.*;
import static org.junit.Assert.*;


public class BuyCarTransactionTest {

    BuyCarParameters validParams;

    @Before
    public void initialiazeParams(){ validParams = new BuyCarParameters(); }

    @Test
    public void creation(){
        // Tests for the common functionality
        AbstractRegularTransactionTest.creation(validParams);

        // SellCar specific test: buyOrderInfo is null
        assertFalse("CarBuyOrderInfo: Exception expected", canCreate(validParams.buyOrderInfo(null)));
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
