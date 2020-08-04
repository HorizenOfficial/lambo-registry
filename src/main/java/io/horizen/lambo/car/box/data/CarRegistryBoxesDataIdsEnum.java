package io.horizen.lambo.car.box.data;

// Declare all custom box data type ids in a single enum to avoid collisions.
// Used during BoxData serializations.
public enum CarRegistryBoxesDataIdsEnum {
    CarBoxDataId((byte)1),
    CarSellOrderBoxDataId((byte)2);

    private final byte id;

    CarRegistryBoxesDataIdsEnum(byte id) {
        this.id = id;
    }

    public byte id() {
        return id;
    }
}
