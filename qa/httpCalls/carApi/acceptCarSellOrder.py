import json
#create and send a custom transaction carApi/acceptCarSellOrder
def acceptCarSellOrder(sidechainNode, carSellOrderId, fee):
      j = {\
           "carSellOrderId": carSellOrderId,\
           "fee": fee \
      }
      request = json.dumps(j)
      response = sidechainNode.carApi_acceptCarSellOrder(request)
      transactionBytes = response["result"]["transactionBytes"]
      j = {\
           "transactionBytes": transactionBytes,\
      }
      request = json.dumps(j)
      response = sidechainNode.transaction_sendTransaction(request)
      return response["result"]["transactionId"]


