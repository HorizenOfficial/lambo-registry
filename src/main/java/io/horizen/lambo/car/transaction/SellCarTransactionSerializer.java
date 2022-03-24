package io.horizen.lambo.car.transaction;

import com.horizen.transaction.TransactionSerializer;
import scorex.util.serialization.Reader;
import scorex.util.serialization.Writer;

public final class SellCarTransactionSerializer implements TransactionSerializer<SellCarTransaction> {

    private static SellCarTransactionSerializer serializer = new SellCarTransactionSerializer();

    private SellCarTransactionSerializer() {
        super();
    }

    public static SellCarTransactionSerializer getSerializer() {
        return serializer;
    }

    @Override
    public void serialize(SellCarTransaction transaction, Writer writer) {
        transaction.serialize(writer);
    }

    @Override
    public SellCarTransaction parse(Reader reader) {
        return SellCarTransaction.parse(reader);
    }
}
