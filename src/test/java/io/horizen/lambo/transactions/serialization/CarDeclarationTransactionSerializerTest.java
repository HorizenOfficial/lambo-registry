package io.horizen.lambo.transactions.serialization;

import io.horizen.lambo.car.transaction.CarDeclarationTransaction;
import io.horizen.lambo.transactions.fixtures.parameters.CarDeclarationParameters;
import org.junit.Test;

import static io.horizen.lambo.transactions.fixtures.parameters.AbstractParameters.create;
import static org.junit.Assert.assertArrayEquals;

public class CarDeclarationTransactionSerializerTest {
    @Test
    public void serialization(){
        byte[] bytes = create(new CarDeclarationParameters()).bytes();
        assertArrayEquals("Serialization-parsing-serialization is not correct",
                CarDeclarationTransaction.parseBytes(bytes).bytes(), bytes);
    }
}
