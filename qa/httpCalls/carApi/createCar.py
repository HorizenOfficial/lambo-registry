import json
#create and send a custom transaction carApi/createCar
#returns (<success>, <transactionid>)
def createCar(sidechainNode, vin, year, model, color, proposition, fee):
      j = {\
           "vin": vin,\
           "year": year,\
           "model": model,\
           "color": color, \
           "proposition": proposition, \
           "fee": fee \
      }
      request = json.dumps(j)
      response = sidechainNode.carApi_createCar(request)
      if ("error" in response):
          return (False, None)
      else:
          transactionBytes = response["result"]["transactionBytes"]
          j = {\
               "transactionBytes": transactionBytes,\
          }
          request = json.dumps(j)
          response = sidechainNode.transaction_sendTransaction(request)
          return (True, response["result"]["transactionId"])


