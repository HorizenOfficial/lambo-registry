package io.horizen.lambo.car.info;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import io.horizen.lambo.car.box.CarBox;
import io.horizen.lambo.car.box.CarBoxSerializer;
import io.horizen.lambo.car.box.data.CarSellOrderBoxData;
import io.horizen.lambo.car.proposition.SellOrderProposition;
import com.horizen.proof.Signature25519;
import com.horizen.proof.Signature25519Serializer;
import com.horizen.proposition.PublicKey25519Proposition;
import com.horizen.proposition.PublicKey25519PropositionSerializer;
import com.horizen.utils.BytesUtils;

import java.util.Arrays;

// CarBuyOrderInfo contains the minimal set of data needed to construct SellCarTransaction specific inputs an outputs.
public final class CarSellOrderInfo {

    private final CarBox carBoxToOpen;    // Car box to be spent in SellCarTransaction
    private final Signature25519 proof;   // Proof to unlock the box above
    private final long price;             // The Car price specified by the owner
    private final PublicKey25519Proposition buyerProposition; // The potential buyer of the car.

    public CarSellOrderInfo(CarBox carBoxToOpen, Signature25519 proof, long price, PublicKey25519Proposition buyerProposition) {
        this.carBoxToOpen = carBoxToOpen;
        this.proof = proof;
        this.price = price;
        this.buyerProposition = buyerProposition;
    }

    // Input box data for unlocker construction.
    public CarBox getCarBoxToOpen() {
        return carBoxToOpen;
    }
    // Input proof data for unlocker construction.
    public Signature25519 getCarBoxSpendingProof() {
        return proof;
    }

    // Recreates output CarSellOrderBoxData with the same Car attributes specified in CarBox
    // and price/buyer specified in current CarSellOrderInfo instance.
    public CarSellOrderBoxData getSellOrderBoxData() {
        return new CarSellOrderBoxData(
                new SellOrderProposition(carBoxToOpen.proposition().pubKeyBytes(), buyerProposition.pubKeyBytes()),
                price,
                carBoxToOpen.getVin(),
                carBoxToOpen.getYear(),
                carBoxToOpen.getModel(),
                carBoxToOpen.getColor()
        );
    }

    // CarSellOrderInfo minimal bytes representation.
    public byte[] bytes() {
        byte[] carBoxToOpenBytes = CarBoxSerializer.getSerializer().toBytes(carBoxToOpen);
        byte[] proofBytes = Signature25519Serializer.getSerializer().toBytes(proof);

        byte[] buyerPropositionBytes = PublicKey25519PropositionSerializer.getSerializer().toBytes(buyerProposition);

        return Bytes.concat(
                Ints.toByteArray(carBoxToOpenBytes.length),
                carBoxToOpenBytes,
                Ints.toByteArray(proofBytes.length),
                proofBytes,
                Longs.toByteArray(price),
                Ints.toByteArray(buyerPropositionBytes.length),
                buyerPropositionBytes
        );
    }

    // Define object deserialization similar to 'toBytes()' representation.
    public static CarSellOrderInfo parseBytes(byte[] bytes) {
        int offset = 0;

        int batchSize = BytesUtils.getInt(bytes, offset);
        offset += 4;

        CarBox carBoxToOpen = CarBoxSerializer.getSerializer().parseBytes(Arrays.copyOfRange(bytes, offset, offset + batchSize));
        offset += batchSize;

        batchSize = BytesUtils.getInt(bytes, offset);
        offset += 4;

        Signature25519 proof = Signature25519Serializer.getSerializer().parseBytes(Arrays.copyOfRange(bytes, offset, offset + batchSize));
        offset += batchSize;

        long price = BytesUtils.getLong(bytes, offset);
        offset += 8;

        batchSize = BytesUtils.getInt(bytes, offset);
        offset += 4;

        PublicKey25519Proposition buyerProposition = PublicKey25519PropositionSerializer.getSerializer()
                .parseBytes(Arrays.copyOfRange(bytes, offset, offset + batchSize));

        return new CarSellOrderInfo(carBoxToOpen, proof, price, buyerProposition);
    }
}
