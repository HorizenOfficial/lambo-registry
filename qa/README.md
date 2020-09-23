**Application Test suite**
--------------------------
**Requirements**

- Install Python 2.7
- Donwload Sidechain SDK

**Additional settings**

Setup these environment variables:
```
BITCOINCLI: path to zen-cli
BITCOIND: path to zend
APP_JAR: sidechain app jar
APP_LIB: sidechain app lib folder
APP_MAIN: main sidechain class to run
SIDECHAIN_SDK: path to SDK folder
```

**Execution**

You can run all tests using command.

```
python run_all.py
```
    
Or run individual test using command like this:

```
python lambo_test1_standardWorkflow.py
```

Example for Linux environment:

```
export BITCOINCLI=/var/horizen/lamboRegistry/zend_oo/src/zen-cli
export BITCOIND=/var/horizen/lamboRegistry/zend_oo/src/zend
export SIDECHAIN_SDK=/var/horizen/lamboRegistry/Sidechains-SDK
export APP_JAR=/var/horizen/lamboRegistry/lambo-registry/target/lambo-registry-0.1.0.jar
export APP_LIB=/var/horizen/lamboRegistry/lambo-registry/target/lib/*
export APP_MAIN=io.horizen.lambo.CarRegistryApp

python  run_all.py --tmpdir=./_tmp
```

