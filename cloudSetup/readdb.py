import boto3

def lambda_handler(event, context):
    client=boto3.resource('dynamodb')
    table=client.Table('Racks')
    response=table.scan()
    return response
