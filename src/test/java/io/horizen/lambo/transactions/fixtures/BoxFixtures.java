package io.horizen.lambo.transactions.fixtures;

import com.horizen.box.data.RegularBoxData;
import com.horizen.proof.Signature25519;
import com.horizen.proposition.PublicKey25519Proposition;
import com.horizen.secret.PrivateKey25519;
import com.horizen.secret.PrivateKey25519Creator;
import scorex.core.NodeViewModifier$;

import java.util.Random;

// Methods for generating random cryptographic objects for tests
public class BoxFixtures {

    public static byte[] getRandomBoxId() {
        byte[] id = new byte[NodeViewModifier$.MODULE$.ModifierIdSize()];
        new Random().nextBytes(id);
        return id;
    }

    public static PrivateKey25519 getPrivateKey25519() {
        byte[] seed = new byte[32];
        new Random().nextBytes(seed);
        return PrivateKey25519Creator.getInstance().generateSecret(seed);
    }

    public static Signature25519 getRandomSignature25519(){
        PrivateKey25519 pk = getPrivateKey25519();
        byte[] message = "12345".getBytes();
        return pk.sign(message);
    }

    public static PublicKey25519Proposition getPublicKey25519Proposition(){
        return getPrivateKey25519().publicImage();
    }

    public static RegularBoxData getRegularBoxData()  {
        return new RegularBoxData(getPublicKey25519Proposition(), new Random().nextInt(100));
    }
}
