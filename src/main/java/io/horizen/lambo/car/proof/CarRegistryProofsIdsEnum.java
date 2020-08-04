package io.horizen.lambo.car.proof;

// Declare all custom proofs ids in a single enum to avoid collisions.
// Used during Proofs serializations.
public enum CarRegistryProofsIdsEnum {
    SellOrderSpendingProofId((byte)1);

    private final byte id;

    CarRegistryProofsIdsEnum(byte id) {
        this.id = id;
    }

    public byte id() {
        return id;
    }
}
