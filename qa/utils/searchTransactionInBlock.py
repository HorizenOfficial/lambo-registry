"""
search for a specific transaction in a block
Return format: (<boolean_true_if_found>, <transaction>)
"""
def searchTransactionInBlock(block, transactionId):
    for tx in block["sidechainTransactions"]:
        if (tx["id"] == transactionId):
             return (True, tx)
    return (False, None)
