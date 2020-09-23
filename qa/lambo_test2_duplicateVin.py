import sys
import os
sys.path.append(os.getenv("SIDECHAIN_SDK", "") + '/qa/')
from test_framework.util import assert_equal, assert_true, assert_false, fail
from httpCalls.carApi.createCar import createCar
from httpCalls.wallet.allBoxes import http_wallet_allBoxes
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
        sc_node1 = self.sc_nodes[0]
        self.sc_sync_all()

        #convert initial forging amount to standard coinbox and returns the public key owning it
        (publicKey, convertedForgingStakeValue) = self.convertInitialForging()
        self.sc_sync_all()
        self.generateOneBlock(sc_node1)
        self.sc_sync_all()

        #check that the stadard coinbox is present in wallet
        boxes = http_wallet_allBoxes(sc_node1)
        (searchBoxFound, boxId)  = searchBoxListByAttributes(boxes,
                                               'typeId', BOXTYPE_STANDARD.REGULAR,
                                               'value', convertedForgingStakeValue,
                                               )
        assert_true(searchBoxFound)

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
        assert_false(success)


if __name__ == "__main__":
    LamboTest2().main()