#!/usr/bin/env python2
import sys
import os
sys.path.append(os.getenv("SIDECHAIN_SDK", "") + '/qa/')
from test_framework.util import assert_equal, assert_true, assert_false, fail
from lambo_test1_standardWorkflow import LamboTest1
from lambo_test2_duplicateVin import LamboTest2

def run_test(test):
    try:

        test.main()
    except SystemExit as e:
        return e.code
    return 0

def run_tests(log_file):
    print "Running all tests "
    original = sys.stdout
    sys.stdout = log_file

    result = run_test(LamboTest1())
    assert_equal(0, result, "LamboTest1 test failed!")

    result = run_test(LamboTest2())
    assert_equal(0, result, "LamboTest2 test failed!")


    sys.stdout = original
    print "Test suite completed! - see log file: " + log_file.name
if __name__ == "__main__":
    log_file = open("sc_test.log", "w")
    run_tests(log_file)
