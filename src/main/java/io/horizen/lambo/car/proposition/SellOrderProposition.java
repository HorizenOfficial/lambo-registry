package io.horizen.lambo.car.proposition;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.primitives.Bytes;
import com.horizen.proposition.ProofOfKnowledgeProposition;
import com.horizen.proposition.PropositionSerializer;
import com.horizen.secret.PrivateKey25519;
import com.horizen.serialization.Views;
import com.horizen.utils.Ed25519;

import java.util.Arrays;

// SellOrderProposition introduced to lock SellOrderBoxes.
// It consists of 2 public keys: owner and buyer.
// So it can be opened in case if owner canceled car sell or buyer purchased the car.
// Unlocking procedure is defined in SellOrderSpendingProof and BuyCarTransaction.

// Declare default JSON view for SellOrderProposition object.
@JsonView(Views.Default.class)
public final class SellOrderProposition implements ProofOfKnowledgeProposition<PrivateKey25519> {
    static final int KEY_LENGTH = Ed25519.publicKeyLength();

    // Specify json attribute name for the ownerPublicKeyBytes field.
    @JsonProperty("ownerPublicKey")
    private final byte[] ownerPublicKeyBytes;

    // Specify json attribute name for the buyerPublicKeyBytes field.
    @JsonProperty("buyerPublicKey")
    private final byte[] buyerPublicKeyBytes;

    public SellOrderProposition(byte[] ownerPublicKeyBytes, byte[] buyerPublicKeyBytes) {
        if(ownerPublicKeyBytes.length != KEY_LENGTH)
            throw new IllegalArgumentException(String.format("Incorrect ownerPublicKeyBytes length, %d expected, %d found", KEY_LENGTH, ownerPublicKeyBytes.length));

        if(buyerPublicKeyBytes.length != KEY_LENGTH)
            throw new IllegalArgumentException(String.format("Incorrect buyerPublicKeyBytes length, %d expected, %d found", KEY_LENGTH, buyerPublicKeyBytes.length));

        this.ownerPublicKeyBytes = Arrays.copyOf(ownerPublicKeyBytes, KEY_LENGTH);

        this.buyerPublicKeyBytes = Arrays.copyOf(buyerPublicKeyBytes, KEY_LENGTH);
    }


    @Override
    public byte[] pubKeyBytes() {
        return Arrays.copyOf(ownerPublicKeyBytes, KEY_LENGTH);
    }

    public byte[] getOwnerPublicKeyBytes() {
        return pubKeyBytes();
    }

    public byte[] getBuyerPublicKeyBytes() {
        return Arrays.copyOf(buyerPublicKeyBytes, KEY_LENGTH);
    }

    @Override
    public PropositionSerializer serializer() {
        return SellOrderPropositionSerializer.getSerializer();
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(ownerPublicKeyBytes);
        result = 31 * result + Arrays.hashCode(buyerPublicKeyBytes);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof SellOrderProposition))
            return false;
        if (obj == this)
            return true;
        SellOrderProposition that = (SellOrderProposition) obj;
        return Arrays.equals(ownerPublicKeyBytes, that.ownerPublicKeyBytes)
                && Arrays.equals(buyerPublicKeyBytes, that.buyerPublicKeyBytes);
    }
}
