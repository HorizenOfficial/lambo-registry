**Lambo Registry Application**

"Lambo Registry" is a demo blockchain application based on the Beta version of the [Sidechains SDK by Horizen](https://github.com/HorizenOfficial/Sidechains-SDK). It supports all Sidechains SDK core features, and it also introduces some custom data and logic.

Lambo Registry is an example of how the Sidechains SDK can be extended to support new functionalities. It shows in particular:
* How to introduce custom Transactions, Boxes, Proofs and Propositions.
* How to define custom API.
* How to manage custom types and API endpoints.

**Lambo custom logic**

A user of the Lambo Registry application can:
* Declare a new car token by specifying some car attributes: VIN (vehicle identification number), model, year and colour.
* Sell an owned car by creating a the sell order associated to a price, that will have to be accepted by a specified buyer.
* Cancel the car selling process, i.e. the car owner can revert the sell order.
* Accept a car sell order, with a payment by the specified buyer to the previous car owner. This transaction makes the buyer the new owner of the car.

Note: this is just an example application, and it misses some fundamental elements to really map a real-world car registry application. In particular, this application allows anyone to declare a new car without proving its existence and ownership. 

**Supported platforms**

The Lambo Registry application is available and tested on Linux and Windows (64bit).

**Requirements**

* Java 8 or newer (Java 11 recommended)
* Maven

**Bootstrap and run**

The process to set up a Lambo sidechain network and run its nodes with a connection to the mainchain is identical to the one described in [Sidechains-SDK simple app example](https://github.com/HorizenOfficial/Sidechains-SDK/blob/master/examples/simpleapp/mc_sc_workflow_example.md). There is no custom steps specific to Lambo application.

For initial testing on regtest, you can use the predefined [lambo_settings.conf](src/main/resources/lambo_settings.conf) configuration file.

Then to run a Lambo node, you can:
1. Go to the project root folder.
2. Build and package Lambo jar: `mvn package`.
3. Execute the application with the following command:

For Linux: 
```
java -cp ./target/lambo-registry-0.1.0.jar:./target/lib/* io.horizen.lambo.CarRegistryApp ./src/main/resources/lambo_settings.conf
```

For Windows:
```
java -cp ./target/lambo-registry-0.1.0.jar;./target/lib/* io.horizen.lambo.CarRegistryApp ./src/main/resources/lambo_settings.conf
```



**Interaction**

Each node has an API server bound to the `address:port` specified in its configuration file. You can use any HTTP client that supports POST requests, e.g. Curl or Postman.

To do all the operations described in the "Lambo custom logic" chapter of the SDK tutorial (link), you can use the following API endpoints (curl examples provided):

* To declare a new car:
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
* To cancel a sell order (by the owner):
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



