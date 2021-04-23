# Bike-Rack

### Cloud Setup
Follow `cloudSetup.pdf` (in the `cloudSetup` folder) to setup the cloud. It details the steps to creating the Lambda functions, DynamoDB tables, REST API (on API Gateway), Cognito pools and IoT Core thing as well as how to integrate all this elements.

---

### BikesSecure app
Open the `BikesSecure` folder in Android Studio (the main Android Studio project folder)  
Replace the following in `amplifyconfiguration.json` (folder: `app\src\main\res\raw`). You would need to have completed the cloud setup step.
```
  [COGNITO IDENTITY POOL ID] : from ‘Creating Cognito identity pool’ step 7
  [REGION] : the region your services are on
  [COGNITO USER POOL ID] : from ‘Creating Cognito user pool’ step 7
  [COGNITO USER POOL APP CLIENT ID] : from ‘Creating Cognito user pool’ step 13
  [API GATEWAY ENDPOINT] : from ‘Setup API Gateway’ step 16 (do not include ‘beta’)
```

---

### IoT device setup
The setup for our ESP32 device can be found at the end of `cloudSetup.pdf`.
