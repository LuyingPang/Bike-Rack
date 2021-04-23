# Bike-Rack

### Cloud Setup
Follow `cloudSetup.pdf` (in the `cloudSetup` folder) to setup the cloud. It details the steps to creating the Lambda functions, DynamoDB tables, REST API (on API Gateway), Cognito pools and IoT Core thing as well as how to integrate all this elements.

---

### BikesSecure app
Open the `BikesSecure` folder in Android Studio (the main Android Studio project folder). If using you own AWS services, replace the following in `amplifyconfiguration.json` with `amplifyconfiguration_template.json` (folder: `app\src\main\res\raw`). You would need to have completed the cloud setup step.  
Now replace the following as specified:
```
  [COGNITO IDENTITY POOL ID] : from ‘Creating Cognito identity pool’ step 7
  [REGION] : the region your services are on
  [COGNITO USER POOL ID] : from ‘Creating Cognito user pool’ step 7
  [COGNITO USER POOL APP CLIENT ID] : from ‘Creating Cognito user pool’ step 13
  [API GATEWAY ENDPOINT] : from ‘Setup API Gateway’ step 16 (do not include ‘beta’)
```
You should also edit the relevant paths for the POST and GET methods in `strings.xml` (the values of `api_stage_name_qrscanner` and `api_stage_name_map` respectively). They should both be `beta` if you followed `cloudSetup.pdf`.  
Once that is done, the app is ready to be built.

### Log in to the app
To log in, you would first need to create an account. Key in a username, password and email to sign up.
Once you click `Sign Up`, a confirmation code would be send to your email. Key it in to the new text box and press `Cofnrim Code`. You would then sign in to the app.

On subsequent sign ins, you would just need your username and password. For this demo, you can use 'username' with 'Password123'.

---

### IoT device setup
The setup for our ESP32 device can be found at the end of `cloudSetup.pdf`.
