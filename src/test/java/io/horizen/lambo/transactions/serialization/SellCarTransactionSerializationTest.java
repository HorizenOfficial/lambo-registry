package io.horizen.lambo.transactions.serialization;

import io.horizen.lambo.car.transaction.SellCarTransaction;
import io.horizen.lambo.transactions.fixtures.parameters.SellCarParameters;
import org.junit.Test;

import static io.horizen.lambo.transactions.fixtures.parameters.AbstractParameters.create;
import static org.junit.Assert.assertArrayEquals;

public class SellCarTransactionSerializationTest {
    @Test
    public void serialization(){
        byte[] bytes = create(new SellCarParameters()).bytes();
        assertArrayEquals("Serialization-parsing-serialization is not correct",
                SellCarTransaction.parseBytes(bytes).bytes(), bytes);
    }
}
