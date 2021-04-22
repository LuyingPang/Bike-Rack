import boto3
import json
client=boto3.client('iot-data',region_name='YOUR_REGION')

def lambda_handler(event, context):
    
    if event['device']=='lock':
        request={"state": {"desired": {"power": "1"}}}
        response=client.publish(
        topic='$aws/things/ESP32/shadow/update',
        qos=0,
        payload=json.dumps(request)
        )
    
        response=client.publish(
        topic='$aws/things/ESP32/shadow/get',
        qos=0,
        payload=json.dumps({})
        )
    elif event['device']=='unlock':
        request={"state": {"desired": {"power": "0"}}}
        response=client.publish(
        topic='$aws/things/ESP32/shadow/update',
        qos=0,
        payload=json.dumps(request)
        )
    
        response=client.publish(
        topic='$aws/things/ESP32/shadow/get',
        qos=0,
        payload=json.dumps({})
        )
    else:
        return {"iot":"Status does not change."}
        
