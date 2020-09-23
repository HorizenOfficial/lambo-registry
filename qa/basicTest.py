#!/usr/bin/env python2
import sys
import os
sys.path.append(os.getenv("SIDECHAIN_SDK", "") + '/qa/')
from SidechainTestFramework.sc_test_framework import SidechainTestFramework
from SidechainTestFramework.sc_boostrap_info import SCNodeConfiguration, SCCreationInfo, MCConnectionInfo, \
    SCNetworkConfiguration
from test_framework.util import assert_true, initialize_chain_clean, start_nodes, \
    websocket_port_by_mc_node_index, connect_nodes_bi, disconnect_nodes_bi
from SidechainTestFramework.scutil import bootstrap_sidechain_nodes, start_sc_nodes, generate_next_blocks, connect_sc_nodes, initialize_default_sc_chain_clean
from SidechainTestFramework.sc_forging_util import *
from httpCalls.transaction.spendForgingStake import spendForgingStake

"""
Basic class for test - extend this to create your own test
This basic class handles the bootstrap of the sidechain and exposes a bounch of commonly used methods (convertInitialForging and generateOneBlock)
"""
class BasicTest(SidechainTestFramework):

    def __init__(self, sc_nodes = 1):
        print "---------"
        print "Initializing test " + str(self.__class__)
        #check variable configuration
        assert_true(sc_nodes > 0, "Sidechain nodes must be > 0")
        assert_true(len(os.getenv("BITCOINCLI", "")) > 0, "BITCOINCLI env var must be set")
        assert_true(len(os.getenv("BITCOIND", "")) > 0, "BITCOIND env var must be set")
        assert_true(len(os.getenv("SIDECHAIN_SDK", "")) > 0, "SIDECHAIN_SDK env var must be set")
        assert_true(len(os.getenv("APP_JAR", "")) > 0, "APP_JAR env var must be set")
        assert_true(len(os.getenv("APP_LIB", "")) > 0, "APP_LIB env var must be set")
        assert_true(len(os.getenv("APP_MAIN", "")) > 0, "APP_MAIN env var must be set")
        print "Configuration of this test:"
        print "    1 mainchain node"
        print "    {} sidechains node".format(sc_nodes)
        self.number_of_mc_nodes = 1
        self.number_of_sidechain_nodes = sc_nodes

    def setup_chain(self):
        initialize_chain_clean(self.options.tmpdir,  self.number_of_mc_nodes)

    def setup_network(self, split = False):
        print("Initializing Mainchain nodes...")
        self.nodes = self.setup_nodes()
        self.sync_all()
        print("OK\n")

    def setup_nodes(self):
        return start_nodes(self.number_of_mc_nodes, self.options.tmpdir)

    def sc_setup_chain(self):
        mc_node_1 = self.nodes[0]

        sc_node_configuration = []
        for x in range(1,  self.number_of_sidechain_nodes +1):
            sc_node_configuration.append(
                SCNodeConfiguration(
                    MCConnectionInfo(address="ws://{0}:{1}".format(mc_node_1.hostname, websocket_port_by_mc_node_index(0)))
                )
            )
        network = SCNetworkConfiguration(SCCreationInfo(mc_node_1, 600, 1000), *sc_node_configuration)
        bootstrap_sidechain_nodes(self.options.tmpdir, network)



    def sc_setup_network(self, split = False):
        print("Initializing {} Sidechain nodes...".format(self.number_of_sidechain_nodes))
        self.sc_nodes = self.sc_setup_nodes()
        if (self.number_of_sidechain_nodes > 1):
            for x in range(1,  self.number_of_sidechain_nodes):
                print('Connecting sidechain node node0 with node{}'.format(x))
                connect_sc_nodes(self.sc_nodes[0], x)
        self.sc_sync_all()
        print("OK\n")

    def sc_setup_nodes(self):
        lib_separator = ":"
        if sys.platform.startswith('win'):
            lib_separator = ";"
        app_jar = os.getenv("APP_JAR", "")
        app_lib = os.getenv("APP_LIB", "")
        app_main = os.getenv("APP_MAIN", "")

        path = []
        for x in range(1,  self.number_of_sidechain_nodes + 1):
            path .append(app_jar + lib_separator + app_lib + " " + app_main)

        return start_sc_nodes(self.number_of_sidechain_nodes, self.options.tmpdir, None, None, path)

    #spends initial forgin stake to create a stadard coinbox
    def convertInitialForging(self):
        sc_node1 = self.sc_nodes[0]
        response = sc_node1.wallet_allBoxes()
        box = response["result"]["boxes"][0]
        box_id = box["id"]
        publicKey = box["proposition"]["publicKey"]
        blockSignPublicKey = box["blockSignProposition"]["publicKey"]
        vrfPubKey = box["vrfPubKey"]["publicKey"]
        convertedForgingStakeValue = box["value"]
        #spends forgin stake to create a stadard coinbox
        spendForgingStake(sc_node1, box_id, convertedForgingStakeValue, 0, publicKey, blockSignPublicKey, vrfPubKey)
        return publicKey, convertedForgingStakeValue

    def generateOneBlock(self, sidechainNode):
        return generate_next_blocks(sidechainNode, "", 1)[0]