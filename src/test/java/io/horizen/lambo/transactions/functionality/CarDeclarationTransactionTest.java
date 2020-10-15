package io.horizen.lambo.transactions.functionality;

import io.horizen.lambo.transactions.fixtures.parameters.CarDeclarationParameters;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static io.horizen.lambo.transactions.fixtures.parameters.AbstractParameters.canCreate;
import static io.horizen.lambo.transactions.fixtures.parameters.AbstractParameters.create;
import static io.horizen.lambo.transactions.fixtures.BoxFixtures.*;
import static org.junit.Assert.*;


public class CarDeclarationTransactionTest {

    CarDeclarationParameters validParams;

    @Before
    public void initialiazeParams(){ validParams = new CarDeclarationParameters(); }

    @Test
    public void creation(){
        // Tests for the common functionality
        AbstractRegularTransactionTest.creation(validParams);

        // CarDeclaration specific test: carBoxData is null
        assertFalse("CarBoxData: Exception expected", canCreate(validParams.boxData(null)));
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
