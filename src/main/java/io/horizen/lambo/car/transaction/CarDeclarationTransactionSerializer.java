package io.horizen.lambo.car.transaction;

import com.horizen.transaction.TransactionSerializer;
import scorex.util.serialization.Reader;
import scorex.util.serialization.Writer;

public final class CarDeclarationTransactionSerializer implements TransactionSerializer<CarDeclarationTransaction> {

    private static final CarDeclarationTransactionSerializer serializer = new CarDeclarationTransactionSerializer();

    private CarDeclarationTransactionSerializer() {
        super();
    }

    public static CarDeclarationTransactionSerializer getSerializer() {
        return serializer;
    }

    @Override
    public void serialize(CarDeclarationTransaction transaction, Writer writer) {
        transaction.serialize(writer);
    }

    @Override
    public CarDeclarationTransaction parse(Reader reader) {
        return CarDeclarationTransaction.parse(reader);
    }
}
