import sys
import os
sys.path.append(os.getenv("SIDECHAIN_SDK", "") + '/qa/')
from test_framework.util import assert_equal, assert_true, assert_false, fail, forward_transfer_to_sidechain
from httpCalls.transaction.sendCoinsToAddress import sendCoinsToAddress
from httpCalls.carApi.createCar import createCar
from httpCalls.carApi.createCarSellOrder import createCarSellOrder
from httpCalls.carApi.acceptCarSellOrder import acceptCarSellOrder
from httpCalls.block.best import http_block_best
from httpCalls.wallet.allBoxes import http_wallet_allBoxes
from httpCalls.wallet.balance import http_wallet_balance
from httpCalls.wallet.createPrivateKey25519 import  http_wallet_createPrivateKey25519
from utils.searchBoxListByAttributes import searchBoxListByAttributes
from utils.searchTransactionInBlock import searchTransactionInBlock
from basicTest import BasicTest
from resources.testdata import CAR, BOXTYPE_STANDARD, BOXTYPE_CUSTOM

"""
This test checks a standard workflow: creating a car and selling it to another wallet

Network Configuration:
    1 MC nodes and 2 SC nodes 
    
Workflow modelled in this test:
    SCNode1: spendForgingStake
    SCNode1: createCar
    SCNode2: createPrivateKey25519  of UserB (buyer)
    SCNode1: createCarSellOrder to the UserB
    SCNode2: acceptCarSellOrder  
"""
class LamboTest1(BasicTest):

    def __init__(self):
        #setup network with 2 sidechain nodes
        super(LamboTest1, self).__init__(2)

    def run_test(self):
        print "Starting test"
        mc_node = self.nodes[0]
        sc_node1 = self.sc_nodes[0]
        sc_node2 = self.sc_nodes[1]
        publicKeySeller = self.sc_nodes_bootstrap_info.genesis_account.publicKey
        self.sc_sync_all()

        #we need regular coins (the genesis account balance is locked into forging stake), so we perform a
        #forward transfer to sidechain for an amount equals to the genesis_account_balance
        forward_transfer_to_sidechain(self.sc_nodes_bootstrap_info.sidechain_id,
                                      mc_node,
                                      publicKeySeller,
                                      self.sc_nodes_bootstrap_info.genesis_account_balance)
        self.sc_sync_all()
        self.generateOneBlock(sc_node1)
        self.sc_sync_all()

        #check that the wallet balance is doubled now (forging stake + the forward transfer) (we need to convert to zentoshi also)
        assert_equal(http_wallet_balance(sc_node1),  (self.sc_nodes_bootstrap_info.genesis_account_balance * 2) * 100000000)

        #declare a new car
        (success, transactionid) = createCar(sc_node1, CAR.VIN, CAR.YEAR, CAR.MODEL, CAR.COLOR, publicKeySeller, 1000)
        assert_true(success)
        self.sc_sync_all()
        self.generateOneBlock(sc_node1)
        self.sc_sync_all()

        #check that the declared car is present in wallet
        boxes = http_wallet_allBoxes(sc_node1)
        (searchBoxFound, carBoxId) = searchBoxListByAttributes(boxes,
                                                'typeId', BOXTYPE_CUSTOM.CAR,
                                                'vin', CAR.VIN
                                                )
        assert_true(searchBoxFound)

        #generate new key in sidechain node 2 (will be the buyer)
        publiKeyNode2 = http_wallet_createPrivateKey25519(sc_node2)

        #create a car sell order for the buyer
        txId = createCarSellOrder(sc_node1, carBoxId, publiKeyNode2, 10000000, 1000)
        self.sc_sync_all()
        self.generateOneBlock(sc_node1)
        self.sc_sync_all()

        #check that the transaction was included in last block
        bestBlock = http_block_best(sc_node1)
        (searchFound, foundTx) = searchTransactionInBlock(bestBlock, txId)
        assert_true(searchFound)

        #check that the sell order was created correctly
        (searchBoxFound, sellOrderBoxId) = searchBoxListByAttributes(foundTx['newBoxes'],
                    'typeId', BOXTYPE_CUSTOM.SELL_ORDER,
                    'vin', CAR.VIN,
                    )
        assert_true(searchBoxFound)

        #send some coin to the user on sidechain node 2
        sendCoinsToAddress(sc_node1, publiKeyNode2, 50000000, 1000)
        self.sc_sync_all()
        self.generateOneBlock(sc_node1)
        self.sc_sync_all()

        #check the user on sidechain node2 has received the money
        assert_equal(http_wallet_balance(sc_node2), 50000000)

        #user on sidechain node2 accepts the sell order
        acceptCarSellOrder(sc_node2, sellOrderBoxId, 1000)
        self.sc_sync_all()
        self.generateOneBlock(sc_node1)
        self.sc_sync_all()

        #user on sidechain node 2 now should own the car
        boxes = http_wallet_allBoxes(sc_node2)
        (searchBoxFound, sellOrderBoxId)  = searchBoxListByAttributes(boxes,
                                                'typeId', BOXTYPE_CUSTOM.CAR,
                                                'vin', CAR.VIN,
                                                )
        assert_true(searchBoxFound)

        #user on sidechain node 1 (the seller) shold not own the car anymore
        boxes = http_wallet_allBoxes(sc_node1)
        (searchBoxFound, sellOrderBoxId)  = searchBoxListByAttributes(boxes,
                                              'typeId',  BOXTYPE_CUSTOM.CAR,
                                              'vin', CAR.VIN,
                                              )
        assert_false(searchBoxFound)

        #user on sidechain node 2 has correct balance (initial - car price  - feee + car value (actually 1 hardcoded in code))
        assert_equal(http_wallet_balance(sc_node2), 39999001)


if __name__ == "__main__":
    LamboTest1().main()