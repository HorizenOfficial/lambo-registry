package io.horizen.lambo.transactions.functionality;

import io.horizen.lambo.transactions.fixtures.parameters.AbstractParameters;

import java.util.Collections;

import static io.horizen.lambo.transactions.fixtures.parameters.AbstractParameters.canCreate;
import static io.horizen.lambo.transactions.fixtures.BoxFixtures.getRandomBoxId;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

// Set of tests for the AbstractRegularTransaction parameters
// This type of transaction can't be instantiated individually, so this set of tests should be applied to any of AbstractRegularTransaction inheritors
public class AbstractRegularTransactionTest {

    public static void creation(AbstractParameters params){

        // Test 1: everything is correct
        assertTrue("Test1: Successful transaction creation expected", canCreate(params));

        // Test 2: inputs ids is null
        assertFalse("Test2: Exception expected", canCreate(params.inputsIds(null)));

        // Test 3: proofs is null
        assertFalse("Test3: Exception expected", canCreate(params.proofs(null)));

        // Test 4: outputs data is null
        assertFalse("Test4: Exception expected", canCreate(params.outputsData(null)));

        // Test 5: inputs ids list is empty
        assertFalse("Test5: Exception expected", canCreate(params.inputsIds(Collections.emptyList())));

        // Test 6: proofs list is empty
        assertFalse("Test6: Exception expected", canCreate(params.proofs(Collections.emptyList())));

        // Test 7: outputs data list is empty
        assertFalse("Test7: Exception expected", canCreate(params.outputsData(Collections.emptyList())));

        // Test 9: fee is negative
        assertFalse("Test8: Exception expected", canCreate(params.fee(-2)));

        // Test 9: timestamp is negative
        assertFalse("Test9: Exception expected", canCreate(params.timestamp(-2)));

        // Test 10: number of inputs (1) is different to number of proofs (2)
        assertFalse("Test10: Exception expected", canCreate(params.inputsIds(Collections.singletonList(getRandomBoxId()))));
    }
}
