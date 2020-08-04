package io.horizen.lambo.car.proof;

import com.google.common.primitives.Bytes;
import io.horizen.lambo.car.proposition.SellOrderProposition;
import com.horizen.proof.AbstractSignature25519;
import com.horizen.proof.ProofSerializer;
import com.horizen.secret.PrivateKey25519;
import com.horizen.utils.Ed25519;

import java.util.Arrays;
import java.util.Objects;

// SellOrderSpendingProof introduced to be able to open the SellOrderProposition, that can be satisfied in 2 cases:
// 1) If the seller decided to cancel car sell - so should provide the proof of ownership.
// 2) If the buyer specified in the SellOrder confirmed the operations - purchased the car.

// No specific JSON view is set for SellOrderSpendingProof, the default one form AbstractSignature25519 is taken.
public final class SellOrderSpendingProof extends AbstractSignature25519<PrivateKey25519, SellOrderProposition> {
    // To distinguish who opened the CarSellOrderBox: seller or buyer
    private boolean isSeller;

    public static int SIGNATURE_LENGTH = Ed25519.signatureLength();

    public SellOrderSpendingProof(byte[] signatureBytes, boolean isSeller) {
        super(signatureBytes);
        if (signatureBytes.length != SIGNATURE_LENGTH)
            throw new IllegalArgumentException(String.format("Incorrect signature length, %d expected, %d found", SIGNATURE_LENGTH,
                    signatureBytes.length));
        this.isSeller = isSeller;
    }

    public boolean isSeller() {
        return isSeller;
    }

    // Depends on isSeller flag value check the signature against seller or buyer public key specified in SellOrderProposition.
    @Override
    public boolean isValid(SellOrderProposition proposition, byte[] message) {
        if(isSeller) {
            // Car seller wants to discard selling.
            return Ed25519.verify(signatureBytes, message, proposition.getOwnerPublicKeyBytes());
        } else {
            // Specific buyer wants to buy the car.
            return Ed25519.verify(signatureBytes, message, proposition.getBuyerPublicKeyBytes());
        }
    }

    @Override
    public byte[] bytes() {
        return Bytes.concat(
                new byte[] { (isSeller ? (byte)1 : (byte)0) },
                signatureBytes
        );
    }

    public static SellOrderSpendingProof parseBytes(byte[] bytes) {
        int offset = 0;

        boolean isSeller = bytes[offset] != 0;
        offset += 1;

        byte[] signatureBytes = Arrays.copyOfRange(bytes, offset, offset + SIGNATURE_LENGTH);

        return new SellOrderSpendingProof(signatureBytes, isSeller);
    }

    @Override
    public ProofSerializer serializer() {
        return SellOrderSpendingProofSerializer.getSerializer();
    }

    @Override
    public byte proofTypeId() {
        return CarRegistryProofsIdsEnum.SellOrderSpendingProofId.id();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SellOrderSpendingProof that = (SellOrderSpendingProof) o;
        return Arrays.equals(signatureBytes, that.signatureBytes) && isSeller == that.isSeller;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(signatureBytes.length);
        result = 31 * result + Arrays.hashCode(signatureBytes);
        result = 31 * result + (isSeller ? 1 : 0);
        return result;
    }
}
