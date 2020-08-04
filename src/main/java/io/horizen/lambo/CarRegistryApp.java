package io.horizen.lambo;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.horizen.SidechainApp;

import java.io.File;

// Car Registry application starting point.
// Application expect to be executed with a single argument - path to configuration file
// Car Registry example has no custom settings and need only Core settings to be defined, like:
// path to data folder, wallet seed, API server address, etc.
public class CarRegistryApp {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please provide settings file name as first parameter!");
            return;
        }

        if (!new File(args[0]).exists()) {
            System.out.println("File on path " + args[0] + " doesn't exist");
            return;
        }

        String settingsFileName = args[0];

        // To Initialize the core starting point - SidechainApp, Guice DI is used.
        // Note: it's possible to initialize SidechainApp both using Guice DI or directly by emitting the constructor.
        Injector injector = Guice.createInjector(new CarRegistryAppModule(settingsFileName));
        SidechainApp sidechainApp = injector.getInstance(SidechainApp.class);

        // Start the car registry sidechain node.
        sidechainApp.run();
        System.out.println("Car Registry Sidechain application successfully started...");
    }
}
