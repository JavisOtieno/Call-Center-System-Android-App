package com.scg.scgcalls;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.scg.scgcalls.util.EncryptedPrefsUtil;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class TwilioFCMService extends FirebaseMessagingService implements OnSuccessListener {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("TwilioFCMService", "Service created");
    }

    @Override
    public void onNewToken(String fcmtoken) {
        super.onNewToken(fcmtoken);
        Log.d("FCM", "New token: " + fcmtoken);
        // Send token to your server if needed

        SharedPreferences prefs = getSharedPreferences("call_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("access_token", fcmtoken);
        editor.apply();

        String token = EncryptedPrefsUtil.getString("authToken", "");

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("fcmtoken", fcmtoken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiService apiService = ApiClient.getInstance().getApiService();
        RequestBody body = RequestBody.create(MediaType.parse("application/json"),
                requestBody.toString());
        retrofit2.Call<ResponseBody> call = apiService.postData("api/registerfcmtoken", "Bearer " + token,
                "application/json", body);

        String finalToken = token;
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseBody> call, Response<ResponseBody> response) {

                Log.d("API Success", "Data: " + response.body());

                if (response.isSuccessful() && response.body() != null) {
//                    listener.onSuccess(response.body());
                    Log.d("API Success", "Data: " + response.body());
                } else {
                    Log.e("API Error", "Response code: " + response.code());
                    if (response.code() == 401) {
//                        Toast.makeText(context, "401", Toast.LENGTH_SHORT).show();
//                        Toasts.toastIconError(context, finalToken + "Session Expired. Please login again");
////                        return;
//                        EncryptedPrefsUtil.saveString("userId", "0");
//                        EncryptedPrefsUtil.saveString("authToken", "");
//                        EncryptedPrefsUtil.saveString("email", "");
//                        EncryptedPrefsUtil.saveString("password", "");
//                        Intent intent = new Intent(context, LoginActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                        context.startActivity(intent);
                    } else {
//                        Toast.makeText(context, "Error Loading. Please try again", Toast.LENGTH_SHORT).show();
                        Log.d( "API Error", "Error Loading. Please try again");

                    }

                    try {
                        if (response.errorBody() != null) {
                            String errorMessage = response.errorBody().string();
                            JSONObject errorJson = new JSONObject(errorMessage);
                            Log.e("API Error Body", errorMessage);
//                            Toast.makeText(context, errorJson.getString("message"), Toast.LENGTH_SHORT).show();


                        } else {
                            Log.e("API Error", "No error body returned from the server");
//                            Toast.makeText(context, "Error Loading. Please try again", Toast.LENGTH_SHORT).show();

                        }
                    } catch (IOException e) {
                        Log.e("API Error", "Failed to read error body", e);
//                        Toast.makeText(context, "Error Loading. Please try again", Toast.LENGTH_SHORT).show();

                    } catch (JSONException e) {
                        Log.e("API Error", "Failed to convert error to json", e);
//                        Toast.makeText(context, "Error Loading. Please try again", Toast.LENGTH_SHORT).show();

                    }


                }
            }

            @Override
            public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
//                dialog.dismiss();
//                Toast.makeText(context, "Error Loading. Please try again", Toast.LENGTH_SHORT).show();

                Log.e("API Failure", "Error: " + t.getMessage());

                if (t instanceof HttpException) {
                    HttpException httpException = (HttpException) t;
                    int responseCode = httpException.code();
                    Log.e("API Failure", "Response code: " + responseCode);

                    try {
                        Response<?> response = httpException.response();
                        if (response != null && response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e("API Failure", "Error body: " + errorBody);
                        }
                    } catch (IOException e) {
                        Log.e("API Failure", "Error reading error body: " + e.getMessage());
                    }
                } else {
                    Log.e("API Failure", "Unknown error", t);
                }
            }
        });




    }

    private void showNotification(String title, String message) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String channelId = "default";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Default Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("FCM Notification Channel");
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        manager.notify(1, builder.build());
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();

        Log.d("TwilioFCMService", "Received message");



        new Handler(Looper.getMainLooper()).post(() -> {
            if (MainActivity.isActive
                    && remoteMessage.getData().get("status").equals("incoming")
            ) {
                Log.d("TwilioFCMService","MainActivity active");
                MainActivity.showIncomingCallAlertDialog(remoteMessage.getData().get("caller"));
            }else {
                Log.d("TwilioFCMService","MainActivity not active");
                MainActivity.dismissIncomingCallAlertDialog();
            }
                });

        if (remoteMessage.getData() != null && remoteMessage.getData().get("status").equals("incoming")) {
            showNotification("Incoming Call", "Number: "+remoteMessage.getData().get("caller"));
        }





//            if (remoteMessage.getData().containsKey("action")) {
//                String action = remoteMessage.getData().get("action");
//                if ("incoming_call".equals(action)) {
////                    String accessToken = remoteMessage.getData().get("access_token");
//                    String caller = remoteMessage.getData().get("caller");
//
//                    Intent intent = new Intent(this, IncomingCallActivity.class);
////                    intent.putExtra("access_token", accessToken);
//                    intent.putExtra("caller", caller);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
//                }
//            }




//        Voice.handleMessage(
//                this,
//                remoteMessage.getData(),
//                new MessageListener() {
//                    @Override
//                    public void onCallInvite(@NonNull CallInvite callInvite) {
//                        if (ReceiveCallTestActivity.isActive) {
//                            ReceiveCallTestActivity.showIncomingCall(callInvite);
//                            Log.d("TwilioFCMService", "Something");
//                        }
//                    }
//
//                    @Override
//                    public void onCancelledCallInvite(@NonNull CancelledCallInvite cancelledCallInvite, @Nullable CallException callException) {
//
//                        Log.d("TwilioFCMService", "Something");
//                    }
//
////                    @Override
////                    public void onMessage(@NonNull String messageType, @NonNull Bundle bundle) {
////                        // No-op or handle general Voice SDK messages
////                    }
//                },
//                new Call.CallMessageListener() {
//                    @Override
//                    public void onMessageReceived(String callSid, CallMessage callMesssage) {
//                        Log.d("TwilioFCMService", "Something");
//                    }
//
//                    @Override
//                    public void onMessageSent(String callSid, String voiceEventSID) {
//                        Log.d("TwilioFCMService", "Something");
//                    }
//
//                    @Override
//                    public void onMessageFailure(String callSid, String voiceEventSID, VoiceException error) {
//                        Log.d("TwilioFCMService", "Something");
//
//                    }
//
////                    @Override
////                    public void onCallInvite(@NonNull CallInvite invite) {
////                        if (MainActivity.isActive) {
////                            MainActivity.showIncomingCall(invite);
////                        }
////                    }
////
////                    @Override
////                    public void onCancelledCallInvite(@NonNull CancelledCallInvite cancelledCallInvite,
////                                                      @Nullable CallException error) {
////                        if (MainActivity.isActive) {
////                            MainActivity.onCallCancelled();
////                        }
////                    }
//                }
//        );
    }

    @Override
    public void onSuccess(ResponseBody responseBody) {

        String responseBodyString = null;
        try {
            String result = responseBody.string();
            System.out.println("Result: "+result);

            if (result != null && result.contains("token") ){


                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.has("status") &&
                        jsonObject.get("status") instanceof String &&
                        jsonObject.get("status").equals("success")
                ) {




//                    dbHelper.insertLocation(mLastLocation.getLatitude()+"",
//                            mLastLocation.getLongitude()+"",
//                            mLastLocation.getAccuracy()+"",
//                            "0","unsynced",
//                            Integer.parseInt(tripId),"start",startLocation);

//                    Toasts.toastIconSuccess(AddTripActivity.this,"Trip Added Successfully");
                    Toast.makeText(getBaseContext(), "Token Registered Successfully", Toast.LENGTH_SHORT).show();

//                    Intent intent = new Intent(AddTripActivity.this, TripsActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    startActivity(intent);

                }
                else if(result.contains("errors")){
                    String message = jsonObject.getString("message");
//                    Toasts.toastIconError(AddTripActivity.this,message);
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                }
                else{

//                    Toasts.toastIconError(AddTripActivity.this,"Error Adding Trip. Please Try Again");
                    Toast.makeText(getBaseContext(), "Error Registering Token", Toast.LENGTH_SHORT).show();
//                    FirebaseCrashlytics.getInstance().recordException(new Exception("Add Deal Error: "+result));

                }

            }


//            Log.d("API Success", "Data: " + jsonObject.toString());


        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onComplete() {
//        isTripRunning = false;
    }
}
