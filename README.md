**Lambo Registry Application**

Lambo Registry is a blockchain application based on [Sidechains SDK by Horizen](https://github.com/HorizenOfficial/Sidechains-SDK) Beta version.
It supports all Sidechains SDK core features as well as introduce custom logic.

Lambo Registry is an example of how to use and extend Sidechains SDK:
* How to setup the project.
* How to introduce custom Transactions, Boxes, Proofs and Propositions.
* How to define custom API to do custom logic operation.
* How to let SDK manage custom types and API endpoints.

**Lambo custom logic**

The application introduce the following logic:
* Declare a new Car token by specifying main car attributes: VIN, model, year and color.
* Sell an owned car by creation of the sell order with a car price to be accepted by a specific buyer.
* Cancel car selling operation, when the owner reverts sell order back.
* Accept car sell order, when a specific buyer does a payment to the previous car owner and become a new owner.

Note: currently anyone can declare a new car without any proofs of real car existence and ownership.

**Supported platforms**

Lambo Registry Application is available and tested on Linux and Windows (64bit).

**Requirements**

* Java 8 or newer (Java 11 recommended)
* Maven

**Bootstrap and run**

A detailed description of how to set up Lambo sidechain network and run a node with a connection to the mainchain 
is identical to the one described in [Sidechains-SDK docs](https://github.com/HorizenOfficial/Sidechains-SDK/blob/master/examples/simpleapp/mc_sc_workflow_example.md).

There is no custom steps specific to Lambo application.

For initial testing on the regtest the predefined [lambo_settings.conf](src/main/resources/lambo_settings.conf) configuration file can be used.

To run Lambo node with `lambo_settings.conf`:
1. Go to project root folder.
2. Build and package Lambo jar `mvn package`.
3. Execute application using the following command:

For Linux: 
```
java -cp ./target/lambo-registry-0.1.0.jar:./target/lib/* io.horizen.lambo.CarRegistryApp ./src/main/resources/lambo_settings.conf
```

For Windows:
```
java -cp ./target/lambo-registry-0.1.0.jar;./target/lib/* io.horizen.lambo.CarRegistryApp ./src/main/resources/lambo_settings.conf
```





**Interaction**

Each node has an API server bound to the `address:port` specified in a configuration file.

Use any HTTP client that supports POST requests. For example, curl or Postman.


To do all the operations described in "Lambo custom logic" chapter the following API endpoints used (curl examples provided):

* To declare new car:
```
curl --location --request POST '127.0.0.1:9085/carApi/createCar' \
--header 'Content-Type: application/json' \
--data-raw '{
    "vin": "443JNI12SDAQ2RTRT",
    "year": 2015,
    "model": "Lamborghini Aventador",
    "color": "Yellow",
    "proposition": "a5b10622d70f094b7276e04608d97c7c699c8700164f78e16fe5e8082f4bb2ac",
    "fee": 100
}'
```

* To create a sell order:
```
curl --location --request POST '127.0.0.1:9085/carApi/createCarSellOrder' \
--header 'Content-Type: application/json' \
--data-raw '{
    "carBoxId": "2b05167dbae0f6f6cb2a6c09cc9fbdc450c93b2a12c6b7847a3119f3a1779b09",
    "sellPrice": 10000000000,
    "buyerProposition": "3368c35a21d9edef9a643dbd4fce0d7fa8c8bf4e556bd449780d9926e4f09689",
    "fee": 100
}'
```
* To cancel sell order by the owner:
```
curl --location --request POST '127.0.0.1:9085/carApi/cancelCarSellOrder' \
--header 'Content-Type: application/json' \
--data-raw '{
    "carSellOrderId": "21639caab0743478ee6ebf06dd0070f6ab0faef4b8511631a9cd517b88d6e853",
    "fee": 100
}'
```  
* To purchase a car that is on sale:
```
curl --location --request POST '127.0.0.1:9085/carApi/acceptCarSellOrder' \
--header 'Content-Type: application/json' \
--data-raw '{
    "carSellOrderId": "408573c37b96a7e292ffd66eb62a5a53e98a5e6d91c4a93b1784997f3d7e6c7e",
    "fee": 100
}'
```



