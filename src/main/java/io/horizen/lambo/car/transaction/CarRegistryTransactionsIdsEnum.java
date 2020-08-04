package io.horizen.lambo.car.transaction;

public enum CarRegistryTransactionsIdsEnum {
    CarDeclarationTransactionId((byte)1),
    SellCarTransactionId((byte)2),
    BuyCarTransactionId((byte)3);

    private final byte id;

    CarRegistryTransactionsIdsEnum(byte id) {
        this.id = id;
    }

    public byte id() {
        return id;
    }
}
