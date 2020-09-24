import sys
import os
sys.path.append(os.getenv("SIDECHAIN_SDK", "") + '/qa/')
from test_framework.util import assert_equal, assert_true, assert_false, fail, forward_transfer_to_sidechain
from httpCalls.carApi.createCar import createCar
from httpCalls.wallet.allBoxes import http_wallet_allBoxes
from httpCalls.wallet.balance import http_wallet_balance
from utils.searchBoxListByAttributes import searchBoxListByAttributes
from basicTest import BasicTest
from resources.testdata import CAR, BOXTYPE_STANDARD, BOXTYPE_CUSTOM

"""
This test checks that is not admitted to create more than one car with the same VIN

Network Configuration:
    1 MC nodes and 1 SC node 
    
Workflow modelled in this test:
    SCNode1: spendForgingStake
    SCNode1: createCar
    SCNode1: createCar (with the same VIN) : checks that the create has not success          
"""
class LamboTest2(BasicTest):


    def __init__(self):
        #setup network with 1 sidechain node
        super(LamboTest2, self).__init__(1)


    def run_test(self):
        mc_node = self.nodes[0]
        sc_node1 = self.sc_nodes[0]
        publicKey = self.sc_nodes_bootstrap_info.genesis_account.publicKey
        self.sc_sync_all()


        #we need regular coins (the genesis account balance is locked into forging stake), so we perform a
        #forward transfer to sidechain for an amount equals to the genesis_account_balance
        forward_transfer_to_sidechain(self.sc_nodes_bootstrap_info.sidechain_id,
                                      mc_node,
                                      publicKey,
                                      self.sc_nodes_bootstrap_info.genesis_account_balance)
        self.sc_sync_all()
        self.generateOneBlock(sc_node1)
        self.sc_sync_all()

        #check that the wallet balance is doubled now (forging stake + the forward transfer) (we need to convert to zentoshi also)
        assert_equal(http_wallet_balance(sc_node1),  (self.sc_nodes_bootstrap_info.genesis_account_balance * 2) * 100000000)

        #declare a new car
        (success, transactionid)  = createCar(sc_node1, CAR.VIN, CAR.YEAR, CAR.MODEL, CAR.COLOR, publicKey, 1000)
        assert_true(success)
        self.sc_sync_all()
        self.generateOneBlock(sc_node1)
        self.sc_sync_all()

        #check that the declared car is present in wallet
        boxes = http_wallet_allBoxes(sc_node1)
        (searchBoxFound, carBoxId) = searchBoxListByAttributes(boxes,
                                                'typeId', BOXTYPE_CUSTOM.CAR,
                                                'vin', CAR.VIN,
                                                )
        assert_true(searchBoxFound)

        #declare anoter car with the same VIN
        (success, transactionid) = createCar(sc_node1, CAR.VIN, "2018", "Lamborghini Aventador 2", "black", publicKey, 1000)

        #check the call failed
        assert_false(success)


if __name__ == "__main__":
    LamboTest2().main()