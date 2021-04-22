import boto3
from botocore.exceptions import ClientError

def update_rack(rackID,request,response):
    if response["update"]=="true":
        client=boto3.resource("dynamodb")
        table=client.Table("Racks")
        # if request = lock, update freestand-1
        # if request = unlock, update freestand+1
        # if request = faulty, update freestand-1,faultystand+1
        
        if request == "lock":
            #if try block does not raise any errors, the else block is executed:
            try:
                response=table.update_item(
                    Key={
                        'Rack ID': rackID
                    },
                    ExpressionAttributeNames={
                        '#faultyS':'Faulty Stands',
                        '#freeS':'Free Stands'
                    },
                    UpdateExpression="SET #faultyS = #faultyS + :faultyVal, #freeS = #freeS + :freeVal",
                    ExpressionAttributeValues={
                        ':faultyVal':0,
                        ':freeVal':-1
                    },
                    ReturnValues="UPDATED_NEW"
                )
            except ClientError as e:
                raise
            else:
                print("Update Dynamodb-Racks successfully!")
                return response
                
                
                
                
        elif request=="unlock":
            #if try block does not raise any errors, the else block is executed:
            try:
                response=table.update_item(
                    Key={
                        'Rack ID': rackID
                    },
                    ExpressionAttributeNames={
                        '#faultyS':'Faulty Stands',
                        '#freeS':'Free Stands'
                    },
                    UpdateExpression="SET #faultyS = #faultyS + :faultyVal, #freeS = #freeS + :freeVal",
                    ExpressionAttributeValues={
                        ':faultyVal':0,
                        ':freeVal':1
                    },
                    ReturnValues="UPDATED_NEW"
                )
            except ClientError as e:
                raise
            else:
                print("Update Dynamodb-Racks successfully!")
                return response
                
                
                
        elif request=="faulty":
            #if try block does not raise any errors, the else block is executed:
            try:
                response=table.update_item(
                    Key={
                        'Rack ID': rackID
                    },
                    ExpressionAttributeNames={
                        '#faultyS':'Faulty Stands',
                        '#freeS':'Free Stands'
                    },
                    UpdateExpression="SET #faultyS = #faultyS + :faultyVal, #freeS = #freeS + :freeVal",
                    ExpressionAttributeValues={
                        ':faultyVal':1,
                        ':freeVal':-1
                    },
                    ReturnValues="UPDATED_NEW"
                )
            except ClientError as e:
                raise
            else:
                print("Update Dynamodb-Racks successfully!")
                return response
        else:
            response="Bad request."
            return response
    
