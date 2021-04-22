import boto3
import random
import string
from botocore.exceptions import ClientError


def updateStatus(rackID, standID, request,inpass=None):
    client=boto3.resource("dynamodb")
    table=client.Table("Stands")
    
    if request == "lock":
        #if try block does not raise any errors, the else block is executed:
        try:
            # password=passwordGen()
            response=table.update_item(
                Key={
                    'Rack ID': rackID,
                    'Stand ID': standID
                },
                ExpressionAttributeNames={
                    '#attribute':'Status'
                },
                UpdateExpression="SET #attribute=:newStatus",
                ConditionExpression="#attribute=:currentStatus",
                ExpressionAttributeValues={
                    ':newStatus':inpass,
                    ':currentStatus':'free'
                },
                ReturnValues="UPDATED_NEW"
            )
        except ClientError as e:
            # if the error caused by condition checking
            if e.response['Error']['Code'] == "ConditionalCheckFailedException":
                print(e.response['Error']['Message'])
                response={"device":"None","app":"The lock is faulty or in use, please try other stands.","update":"false"}
                return response
            # else raise other errors
            else:
                raise
                response={"device":"None","app":"An error occured. Please try other stands.","update":"false"}
                return response
        else:
            print("Update Dynamodb successfully!")
            print(response)
            response={"device":"lock","app":"Locked successfully!","update":"true"}
            return response
            
            
            
            
    elif request=="unlock":
        #if try block does not raise any errors, the else block is executed:
        try:
            response=table.update_item(
                Key={
                    'Rack ID': rackID,
                    'Stand ID': standID
                },
                ExpressionAttributeNames={
                    '#attribute':'Status'
                },
                UpdateExpression="SET #attribute=:newStatus",
                ConditionExpression="#attribute = :password",
                ExpressionAttributeValues={
                    ':newStatus':'free',
                    ':password':inpass
                },
                ReturnValues="UPDATED_NEW"
            )
        except ClientError as e:
            # if the error caused by condition checking
            if e.response['Error']['Code'] == "ConditionalCheckFailedException":
                print(e.response['Error']['Message'])
                response={"device":"None","app":"You have no permission or the stand is not locked.","update":"false"}
                return response
            # else raise other errors
            else:
                raise
                response={"device":"None","app":"An error occured.Please try other stands.","update":"false"}
                return response
        else:
            print("Update Dynamodb successfully!")
            print(response)
            response={"device":"unlock","app":"Unlocked Successfully!","update":"true"}
            return response
            
            
            
            
    elif request=="faulty":
        #if try block does not raise any errors, the else block is executed:
        try:
            response=table.update_item(
                Key={
                    'Rack ID': rackID,
                    'Stand ID': standID
                },
                ExpressionAttributeNames={
                    '#attribute':'Status'
                },
                UpdateExpression="SET #attribute=:newStatus",
                ConditionExpression="#attribute = :currentStatus",
                ExpressionAttributeValues={
                    ':newStatus':'faulty',
                    ':currentStatus':'free'
                },
                ReturnValues="UPDATED_NEW"
            )
        except ClientError as e:
            # if the error caused by condition checking
            if e.response['Error']['Code'] == "ConditionalCheckFailedException":
                print(e.response['Error']['Message'])
                checkstatus=table.get_item(Key={'Rack ID': rackID, 'Stand ID': standID})["Item"]["Status"]
                if checkstatus=="faulty":
                    response={"device":"None","app":"The stand has been marked as faulty. Thanks for reporting it.","update":"false"}
                elif checkstatus==inpass:
                    table.update_item(
                        Key={
                            'Rack ID': rackID,
                            'Stand ID': standID
                        },
                        ExpressionAttributeNames={
                            '#attribute':'Status'
                        },
                        UpdateExpression="SET #attribute=:newStatus",
                        # ConditionExpression="#attribute = :password",
                        ExpressionAttributeValues={
                            ':newStatus':'faulty'
                        },
                        ReturnValues="UPDATED_NEW"
                    )
                    response={"device":"unlock","app":"Your feedback has been noted. We have tried to unlock the stand. Please contact 91234567 for further assistance if your bike is still locked.","update":"true"}
                else:
                    response={"device":"None","app":"This stand is in use. You have no permission to mark this stand as faulty.","update":"false"}
                return response
            # # else raise other errors
            else:
                raise
                response={"device":"None","app":"An error occured. Please try other stands.","update":"false"}
                return response
        else:
            print("Update Dynamodb successfully!")
            print(response)
            response={"device":"faulty","app":"The stand has been registered as faulty successfully!","update":"true"}
            return response
    else:
        response={"device":"None","app":"Bad request","update":"false"}
        return response
    
