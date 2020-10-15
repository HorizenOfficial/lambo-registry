package io.horizen.lambo.transactions.serialization;

import io.horizen.lambo.car.transaction.BuyCarTransaction;
import io.horizen.lambo.transactions.fixtures.parameters.BuyCarParameters;
import org.junit.Test;

import static io.horizen.lambo.transactions.fixtures.parameters.AbstractParameters.create;
import static org.junit.Assert.assertArrayEquals;

public class BuyCarTransactionSerializationTest {
    @Test
    public void serialization(){
        byte[] bytes = create(new BuyCarParameters()).bytes();
        assertArrayEquals("Serialization-parsing-serialization is not correct",
                BuyCarTransaction.parseBytes(bytes).bytes(), bytes);
    }
}
