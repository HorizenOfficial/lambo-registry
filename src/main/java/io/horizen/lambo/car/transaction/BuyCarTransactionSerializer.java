package io.horizen.lambo.car.transaction;

import com.horizen.transaction.TransactionSerializer;
import scorex.util.serialization.Reader;
import scorex.util.serialization.Writer;

public final class BuyCarTransactionSerializer implements TransactionSerializer<BuyCarTransaction> {

    private static final BuyCarTransactionSerializer serializer = new BuyCarTransactionSerializer();

    private BuyCarTransactionSerializer() {
        super();
    }

    public static BuyCarTransactionSerializer getSerializer() {
        return serializer;
    }

    @Override
    public void serialize(BuyCarTransaction transaction, Writer writer) {
        writer.putBytes(transaction.bytes());
    }

    @Override
    public BuyCarTransaction parse(Reader reader) {
        return BuyCarTransaction.parseBytes(reader.getBytes(reader.remaining()));
    }
}
