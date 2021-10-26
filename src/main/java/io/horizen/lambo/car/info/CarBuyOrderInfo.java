package io.horizen.lambo.car.info;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.horizen.box.data.ZenBoxData;
import io.horizen.lambo.car.box.CarSellOrderBox;
import io.horizen.lambo.car.box.CarSellOrderBoxSerializer;
import io.horizen.lambo.car.box.data.CarBoxData;
import io.horizen.lambo.car.proof.SellOrderSpendingProof;
import io.horizen.lambo.car.proof.SellOrderSpendingProofSerializer;
import com.horizen.proposition.PublicKey25519Proposition;
import com.horizen.utils.BytesUtils;

import java.util.Arrays;

// CarBuyOrderInfo contains the minimal set of data needed to construct BuyCarTransaction specific inputs an outputs.
public final class CarBuyOrderInfo {
    private final CarSellOrderBox carSellOrderBoxToOpen;  // Sell order box to be spent in BuyCarTransaction
    private final SellOrderSpendingProof proof;           // Proof to unlock the box above

    public CarBuyOrderInfo(CarSellOrderBox carSellOrderBoxToOpen, SellOrderSpendingProof proof) {
        this.carSellOrderBoxToOpen = carSellOrderBoxToOpen;
        this.proof = proof;
    }

    public CarSellOrderBox getCarSellOrderBoxToOpen() {
        return carSellOrderBoxToOpen;
    }

    public SellOrderSpendingProof getCarSellOrderSpendingProof() {
        return proof;
    }

    // Recreates output CarBoxData with the same attributes specified in CarSellOrder.
    // Specifies the new owner depends on proof provided:
    // 1) if the proof is from the seller then the owner remain the same
    // 2) if the proof is from the buyer then it will become the new owner
    public CarBoxData getNewOwnerCarBoxData() {
        PublicKey25519Proposition proposition;
        if(proof.isSeller()) {
            proposition = new PublicKey25519Proposition(carSellOrderBoxToOpen.proposition().getOwnerPublicKeyBytes());
        } else {
            proposition = new PublicKey25519Proposition(carSellOrderBoxToOpen.proposition().getBuyerPublicKeyBytes());
        }

        return new CarBoxData(
                proposition,
                carSellOrderBoxToOpen.getVin(),
                carSellOrderBoxToOpen.getYear(),
                carSellOrderBoxToOpen.getModel(),
                carSellOrderBoxToOpen.getColor()
        );
    }

    // Check if proof is provided by Sell order owner.
    public boolean isSpentByOwner() {
        return proof.isSeller();
    }

    // Coins to be paid to the owner of Sell order in case if Buyer spent the Sell order.
    public ZenBoxData getPaymentBoxData() {
        return new ZenBoxData(
                new PublicKey25519Proposition(carSellOrderBoxToOpen.proposition().getOwnerPublicKeyBytes()),
                carSellOrderBoxToOpen.getPrice()
        );
    }

    // CarBuyOrderInfo minimal bytes representation.
    public byte[] bytes() {
        byte[] carSellOrderBoxToOpenBytes = CarSellOrderBoxSerializer.getSerializer().toBytes(carSellOrderBoxToOpen);
        byte[] proofBytes = SellOrderSpendingProofSerializer.getSerializer().toBytes(proof);

        return Bytes.concat(
                Ints.toByteArray(carSellOrderBoxToOpenBytes.length),
                carSellOrderBoxToOpenBytes,
                Ints.toByteArray(proofBytes.length),
                proofBytes
        );
    }

    // Define object deserialization similar to 'toBytes()' representation.
    public static CarBuyOrderInfo parseBytes(byte[] bytes) {
        int offset = 0;

        int batchSize = BytesUtils.getInt(bytes, offset);
        offset += 4;

        CarSellOrderBox carSellOrderBoxToOpen = CarSellOrderBoxSerializer.getSerializer().parseBytes(Arrays.copyOfRange(bytes, offset, offset + batchSize));
        offset += batchSize;

        batchSize = BytesUtils.getInt(bytes, offset);
        offset += 4;

        SellOrderSpendingProof proof = SellOrderSpendingProofSerializer.getSerializer().parseBytes(Arrays.copyOfRange(bytes, offset, offset + batchSize));

        return new CarBuyOrderInfo(carSellOrderBoxToOpen, proof);
    }
}
