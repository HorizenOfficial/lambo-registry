package io.horizen.lambo;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.horizen.SidechainSettings;
import com.horizen.api.http.ApplicationApiGroup;
import com.horizen.box.Box;
import com.horizen.box.BoxSerializer;
import com.horizen.box.NoncedBox;
import com.horizen.box.data.NoncedBoxData;
import com.horizen.box.data.NoncedBoxDataSerializer;
import com.horizen.companion.SidechainBoxesDataCompanion;
import com.horizen.companion.SidechainProofsCompanion;
import com.horizen.companion.SidechainTransactionsCompanion;
import io.horizen.lambo.car.api.CarApi;
import io.horizen.lambo.car.box.CarBoxSerializer;
import io.horizen.lambo.car.box.CarRegistryBoxesIdsEnum;
import io.horizen.lambo.car.box.CarSellOrderBoxSerializer;
import io.horizen.lambo.car.box.data.CarBoxDataSerializer;
import io.horizen.lambo.car.box.data.CarRegistryBoxesDataIdsEnum;
import io.horizen.lambo.car.box.data.CarSellOrderBoxDataSerializer;
import io.horizen.lambo.car.proof.CarRegistryProofsIdsEnum;
import io.horizen.lambo.car.proof.SellOrderSpendingProofSerializer;
import io.horizen.lambo.car.transaction.BuyCarTransactionSerializer;
import io.horizen.lambo.car.transaction.CarDeclarationTransactionSerializer;
import io.horizen.lambo.car.transaction.CarRegistryTransactionsIdsEnum;
import io.horizen.lambo.car.transaction.SellCarTransactionSerializer;
import com.horizen.proof.Proof;
import com.horizen.proof.ProofSerializer;
import com.horizen.proposition.Proposition;
import com.horizen.secret.Secret;
import com.horizen.secret.SecretSerializer;
import com.horizen.settings.SettingsReader;
import com.horizen.state.ApplicationState;
import com.horizen.storage.IODBStorageUtil;
import com.horizen.storage.Storage;
import com.horizen.transaction.BoxTransaction;
import com.horizen.transaction.TransactionSerializer;
import com.horizen.utils.Pair;
import com.horizen.wallet.ApplicationWallet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class CarRegistryAppModule
    extends AbstractModule
{
    private SettingsReader settingsReader;

    public CarRegistryAppModule(String userSettingsFileName) {
        this.settingsReader = new SettingsReader(userSettingsFileName, Optional.empty());
    }

    @Override
    protected void configure() {
        // Get sidechain settings
        SidechainSettings sidechainSettings = this.settingsReader.getSidechainSettings();


        // Define custom serializers:

        // Specify how to serialize custom Boxes.
        // The hash map expect to have unique Box type ids as the keys.
        HashMap<Byte, BoxSerializer<Box<Proposition>>> customBoxSerializers = new HashMap<>();
        customBoxSerializers.put(CarRegistryBoxesIdsEnum.CarBoxId.id(), (BoxSerializer) CarBoxSerializer.getSerializer());
        customBoxSerializers.put(CarRegistryBoxesIdsEnum.CarSellOrderBoxId.id(), (BoxSerializer) CarSellOrderBoxSerializer.getSerializer());

        // Specify how to serialize custom BoxData.
        HashMap<Byte, NoncedBoxDataSerializer<NoncedBoxData<Proposition, NoncedBox<Proposition>>>> customBoxDataSerializers = new HashMap<>();
        customBoxDataSerializers.put(CarRegistryBoxesDataIdsEnum.CarBoxDataId.id(), (NoncedBoxDataSerializer) CarBoxDataSerializer.getSerializer());
        customBoxDataSerializers.put(CarRegistryBoxesDataIdsEnum.CarSellOrderBoxDataId.id(), (NoncedBoxDataSerializer) CarSellOrderBoxDataSerializer.getSerializer());

        // No custom secrets for CarRegistry app.
        HashMap<Byte, SecretSerializer<Secret>> customSecretSerializers = new HashMap<>();

        // Specify how to serialize custom Proofs.
        HashMap<Byte, ProofSerializer<Proof<Proposition>>> customProofSerializers = new HashMap<>();
        customProofSerializers.put(CarRegistryProofsIdsEnum.SellOrderSpendingProofId.id(), (ProofSerializer) SellOrderSpendingProofSerializer.getSerializer());

        // Specify how to serialize custom Transaction.
        HashMap<Byte, TransactionSerializer<BoxTransaction<Proposition, Box<Proposition>>>> customTransactionSerializers = new HashMap<>();
        customTransactionSerializers.put(CarRegistryTransactionsIdsEnum.CarDeclarationTransactionId.id(), (TransactionSerializer) CarDeclarationTransactionSerializer.getSerializer());
        customTransactionSerializers.put(CarRegistryTransactionsIdsEnum.SellCarTransactionId.id(), (TransactionSerializer) SellCarTransactionSerializer.getSerializer());
        customTransactionSerializers.put(CarRegistryTransactionsIdsEnum.BuyCarTransactionId.id(), (TransactionSerializer) BuyCarTransactionSerializer.getSerializer());

        // Create companions that will allow to serialize and deserialize any kind of core and custom types specified.
        SidechainBoxesDataCompanion sidechainBoxesDataCompanion = new SidechainBoxesDataCompanion(customBoxDataSerializers);
        SidechainProofsCompanion sidechainProofsCompanion = new SidechainProofsCompanion(customProofSerializers);
        SidechainTransactionsCompanion transactionsCompanion = new SidechainTransactionsCompanion(
                customTransactionSerializers, sidechainBoxesDataCompanion, sidechainProofsCompanion);


        // Define Application state and wallet logic:
        ApplicationWallet defaultApplicationWallet = new CarRegistryApplicationWallet();
        ApplicationState defaultApplicationState = new CarRegistryApplicationState();


        // Define the path to storages:
        String dataDirPath = sidechainSettings.scorexSettings().dataDir().getAbsolutePath();
        File secretStore = new File( dataDirPath + "/secret");
        File walletBoxStore = new File(dataDirPath + "/wallet");
        File walletTransactionStore = new File(dataDirPath + "/walletTransaction");
        File walletForgingBoxesInfoStorage = new File(dataDirPath + "/walletForgingStake");
        File stateStore = new File(dataDirPath + "/state");
        File historyStore = new File(dataDirPath + "/history");
        File consensusStore = new File(dataDirPath + "/consensusData");


        // Add car registry specific API endpoints:
        // CarApi endpoints processing will be added to the API server.
        List<ApplicationApiGroup> customApiGroups = new ArrayList<>();
        customApiGroups.add(new CarApi(transactionsCompanion));


        // No core API endpoints to be disabled:
        List<Pair<String, String>> rejectedApiPaths = new ArrayList<>();


        // Inject custom objects:
        // Names are equal to the ones specified in SidechainApp class constructor.
        bind(SidechainSettings.class)
                .annotatedWith(Names.named("SidechainSettings"))
                .toInstance(sidechainSettings);

        bind(new TypeLiteral<HashMap<Byte, BoxSerializer<Box<Proposition>>>>() {})
                .annotatedWith(Names.named("CustomBoxSerializers"))
                .toInstance(customBoxSerializers);
        bind(new TypeLiteral<HashMap<Byte, NoncedBoxDataSerializer<NoncedBoxData<Proposition, NoncedBox<Proposition>>>>>() {})
                .annotatedWith(Names.named("CustomBoxDataSerializers"))
                .toInstance(customBoxDataSerializers);
        bind(new TypeLiteral<HashMap<Byte, SecretSerializer<Secret>>>() {})
                .annotatedWith(Names.named("CustomSecretSerializers"))
                .toInstance(customSecretSerializers);
        bind(new TypeLiteral<HashMap<Byte, ProofSerializer<Proof<Proposition>>>>() {})
                .annotatedWith(Names.named("CustomProofSerializers"))
                .toInstance(customProofSerializers);
        bind(new TypeLiteral<HashMap<Byte, TransactionSerializer<BoxTransaction<Proposition, Box<Proposition>>>>>() {})
                .annotatedWith(Names.named("CustomTransactionSerializers"))
                .toInstance(customTransactionSerializers);

        bind(ApplicationWallet.class)
                .annotatedWith(Names.named("ApplicationWallet"))
                .toInstance(defaultApplicationWallet);

        bind(ApplicationState.class)
                .annotatedWith(Names.named("ApplicationState"))
                .toInstance(defaultApplicationState);

        bind(Storage.class)
                .annotatedWith(Names.named("SecretStorage"))
                .toInstance(IODBStorageUtil.getStorage(secretStore));
        bind(Storage.class)
                .annotatedWith(Names.named("WalletBoxStorage"))
                .toInstance(IODBStorageUtil.getStorage(walletBoxStore));
        bind(Storage.class)
                .annotatedWith(Names.named("WalletTransactionStorage"))
                .toInstance(IODBStorageUtil.getStorage(walletTransactionStore));
        bind(Storage.class)
                .annotatedWith(Names.named("WalletForgingBoxesInfoStorage"))
                .toInstance(IODBStorageUtil.getStorage(walletForgingBoxesInfoStorage));
        bind(Storage.class)
                .annotatedWith(Names.named("StateStorage"))
                .toInstance(IODBStorageUtil.getStorage(stateStore));
        bind(Storage.class)
                .annotatedWith(Names.named("HistoryStorage"))
                .toInstance(IODBStorageUtil.getStorage(historyStore));
        bind(Storage.class)
                .annotatedWith(Names.named("ConsensusStorage"))
                .toInstance(IODBStorageUtil.getStorage(consensusStore));

        bind(new TypeLiteral<List<ApplicationApiGroup>> () {})
                .annotatedWith(Names.named("CustomApiGroups"))
                .toInstance(customApiGroups);

        bind(new TypeLiteral<List<Pair<String, String>>> () {})
                .annotatedWith(Names.named("RejectedApiPaths"))
                .toInstance(rejectedApiPaths);
    }
}
