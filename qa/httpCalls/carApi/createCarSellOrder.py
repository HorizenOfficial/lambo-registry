import json
#create and send a custom transaction carApi/createCarSellOrder
def createCarSellOrder(sidechainNode, carBoxId, buyerProposition, sellPrice, fee):
      j = {\
           "carBoxId": carBoxId,\
           "buyerProposition": buyerProposition,\
           "sellPrice": sellPrice,\
           "fee": fee \
      }
      request = json.dumps(j)
      response = sidechainNode.carApi_createCarSellOrder(request)
      transactionBytes = response["result"]["transactionBytes"]
      j = {\
           "transactionBytes": transactionBytes,\
      }
      request = json.dumps(j)
      response = sidechainNode.transaction_sendTransaction(request)
      return response["result"]["transactionId"]


