package io.horizen.lambo;

import com.horizen.box.Box;
import com.horizen.proposition.Proposition;
import com.horizen.secret.Secret;
import com.horizen.wallet.ApplicationWallet;

import java.util.List;

// There is no custom logic for Car registry Wallet now.
// TODO: in ApplicationWallet collect related CarSellOrderBoxes: both as owner and as buyer.
// TODO: introduce the getters for such custom data and add specific API routes for users to see this info.
public class CarRegistryApplicationWallet implements ApplicationWallet {

    @Override
    public void onAddSecret(Secret secret) {
        // No custom secret logic supposed in Car Registry App
    }

    @Override
    public void onRemoveSecret(Proposition proposition) {
        // No custom secret logic supposed in Car Registry App
    }

    @Override
    public void onChangeBoxes(byte[] version, List<Box<Proposition>> boxesToUpdate, List<byte[]> boxIdsToRemove) {
        // TODO: here we should detect and process new SellOrderBoxes and remove opened SellOrderBoxes.
    }

    @Override
    public void onRollback(byte[] version) {
        // TODO: rollback car sell orders database to certain point.
    }
}
