"""
search inside a given box list for a box having specific attribute values.
Up to 3 different attributes can be specified: if present, they are checked in  AND (all conditions must be true)
Return format: (<boolean_true_if_found>, <box_id>)
"""
def searchBoxListByAttributes(boxes,
                  attribute1, attribute1Value,
                  attribute2 = None, attribute2Value = None,
                  attribute3 = None, attribute3Value = None
                  ):

    conditions = [TestCondition(attribute1, attribute1Value)]
    if (attribute2 != None):
        conditions.append(TestCondition(attribute2, attribute2Value))
    if (attribute3 != None):
        conditions.append(TestCondition(attribute3, attribute3Value))

    for box in boxes:
        found = True
        for cond in conditions:
            if (cond.test(box) == False):
                found = False
        if (found):
            return (True, box['id'])
    return (False, None)

class TestCondition:
     def __init__(self, attributeName, attributeValue):
         self.attributeName = attributeName
         self.attributeValue = attributeValue

     def test(self, box):
         if (self.attributeName in box and box[self.attributeName] == self.attributeValue):
             return True
         else:
             return False
