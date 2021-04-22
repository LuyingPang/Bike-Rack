import boto3
import json 
from process_request import updateStatus
from update_rackstable import update_rack
def lambda_handler(event, context):
    rackID=event["Rack ID"]
    standID=event["Stand ID"]
    request=event["Request"]
    password=event["Password"]
    # # if request = lock, return  password if update success, "The lock is faulty or in use, plaese try ohter locks. " if update fail
    # # if request = unlock, return "Unlock Successfully!", False if update fail
    # # if request = faulty, return "Register the stand as fualty successfully!", False if update fail
    response=updateStatus(rackID, standID, request,password)
    # print(response)
    update_rack(rackID,request,response)
    
    client = boto3.client('lambda')
    iotresponse = client.invoke(
        FunctionName = 'arn:aws:lambda:YOUR_REGION:YOUR_ACCOUNT_ID:function:iotfunction',
        InvocationType = 'RequestResponse',
        Payload=json.dumps(response)
    )
    
    print(iotresponse['Payload'].read())
    
    return response
