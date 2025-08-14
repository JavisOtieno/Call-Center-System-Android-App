package com.scg.scgcalls;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.twilio.voice.Call;
import com.twilio.voice.CallException;
import com.twilio.voice.CallInvite;
import com.twilio.voice.RegistrationException;
import com.twilio.voice.RegistrationListener;
import com.twilio.voice.Voice;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ReceiveCallTestActivity  extends AppCompatActivity implements OnSuccessListener {
    public static boolean isActive = false;

    private static CallInvite pendingInvite;
    private static Call activeCall;
    private static Context appContext;
    private static final String TAG = "ReceiveCallTestActivity";

    private Toolbar toolbar;
    private ActionBar actionBar;

    private String token;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_call_test);
        appContext = this;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Calls Alert");

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM token failed", task.getException());
                        return;
                    }

                    // Get the token
                    token = task.getResult();
                    Log.d("FCM", "Fetched FCM token manually: " + token);

                    fetchTwilioAccessToken(accessToken -> {

                        Log.d("FCM", "🔑 Twilio Access Token: " + accessToken);
//                        SharedPreferences prefs = getSharedPreferences("call_data", MODE_PRIVATE);
//                        SharedPreferences.Editor editor = prefs.edit();
//                        editor.putString("access_token", accessToken);
//                        editor.apply();


                        ReceiveCallTestActivity.this.runOnUiThread(() -> {
                                    Toast.makeText(ReceiveCallTestActivity.this, accessToken + "-receive", Toast.LENGTH_SHORT).show();
                                });

                        SharedPreferences prefs = getSharedPreferences("call_data", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("access_token", token);
                        editor.apply();







//
//                        Voice.register(
//                                accessToken,                    // your Twilio Access Token string
//                                Voice.RegistrationChannel.FCM,        // tell the SDK this is FCM
//                                token,                       // the FCM device token
//                                new RegistrationListener() {
//                                    @Override
//                                    public void onRegistered(@NonNull String at, @NonNull String rt) {
//                                        Log.i(TAG, "Twilio registered. AccessToken=" + at);
//                                    }
//
//                                    @Override
//                                    public void onError(@NonNull RegistrationException error,
//                                                        @NonNull String at,
//                                                        @NonNull String rt) {
//                                        Log.e(TAG, "Registration failed: " + error.getMessage());
//                                    }
//                                }
//                        );
                    });
                });


            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    JSONObject requestBody = new JSONObject();
                    try {
                        requestBody.put("fcmtoken", token);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    NetworkUtils.
                            fetchDataPost("registerfcmtoken",
                                    "", requestBody, ReceiveCallTestActivity.this,
                                    ReceiveCallTestActivity.this);
            }, 4000);

        // 1) First get the FCM token
//        FcmHelper.getFcmToken(this, fcmToken -> {
//            // 2) Now fetch your Twilio Access Token from your server
////            fetchTwilioAccessToken(accessToken -> {
//
//            Log.d("TwilioSetup", "📲 FCM Token: " + fcmToken);
//
//            // Step 2: Fetch Twilio access token from your Laravel server
//            fetchTwilioAccessToken(accessToken -> {
//                // Step 3: Log Twilio token
//                Log.d("TwilioSetup", "🔑 Twilio Access Token: " + accessToken);
//                // 3) Register with Twilio using the 4‑arg signature
//                Voice.register(
//                        accessToken,                    // your Twilio Access Token string
//                        Voice.RegistrationChannel.FCM,        // tell the SDK this is FCM
//                        fcmToken,                       // the FCM device token
//                        new RegistrationListener() {
//                            @Override
//                            public void onRegistered(@NonNull String at, @NonNull String rt) {
//                                Log.i(TAG, "Twilio registered. AccessToken=" + at);
//                            }
//
//                            @Override
//                            public void onError(@NonNull RegistrationException error,
//                                                @NonNull String at,
//                                                @NonNull String rt) {
//                                Log.e(TAG, "Registration failed: " + error.getMessage());
//                            }
//                        }
//                );
//
//            });
//        });
//
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActive = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActive = false;
    }

    public static void showIncomingCall(CallInvite invite) {
        Context context = appContext; // Only available if you're holding it
        if (context == null) return;

        pendingInvite = invite;

        new AlertDialog.Builder(context)
                .setTitle("Incoming Call")
                .setMessage("From: " + invite.getFrom())
                .setPositiveButton("Accept", (dialog, which) -> {
                    activeCall = invite.accept(context, callListener);
                    pendingInvite = null;
                })
                .setNegativeButton("Decline", (dialog, which) -> {
                    invite.reject(context);
                    pendingInvite = null;
                })
                .setCancelable(false)
                .show();
    }

    public static void showIncomingCallTest(String caller) {
        Context context = appContext; // Only available if you're holding it
        if (context == null) return;

//        pendingInvite = invite;



        new AlertDialog.Builder(context)
                .setTitle("Incoming Call")
                .setMessage("From: " +caller+" - Check Dashboard to Answer")
//                .setPositiveButton("Accept", (dialog, which) -> {
//                    activeCall = invite.accept(context, callListener);
//                    pendingInvite = null;
//                })
                .setNegativeButton("IGNORE", (dialog, which) -> {
//                    invite.reject(context);
//                    pendingInvite = null;
                })
                .setCancelable(false)
                .show();
    }

    public static void onCallCancelled() {
        pendingInvite = null;
    }

    private static final Call.Listener callListener = new Call.Listener() {
        @Override
        public void onConnected(Call call) { }

        @Override
        public void onReconnecting(@NonNull Call call, @NonNull CallException callException) {

        }

        @Override
        public void onReconnected(@NonNull Call call) {

        }

        @Override
        public void onDisconnected(Call call, CallException error) {
            activeCall = null;
        }
        @Override
        public void onConnectFailure(Call call, CallException error) {
            activeCall = null;
        }

        @Override
        public void onRinging(@NonNull Call call) {

        }
    };

    private void fetchTwilioAccessToken(@NonNull TwilioTokenCallback cb) {
        // Example with OkHttp (add your own dependency/imports):
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Constants.BASE_URL+"twilio/token")  // "https://…/token"
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                Log.e("MyApplication", "Token fetch failed", e);
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("MyApplication", "Unexpected HTTP code: " + response.code());
                    return;
                }

                String responseBody = response.body().string();
                try {
                    JSONObject json = new JSONObject(responseBody);
                    String accessToken = json.getString("token");  // ← extract only the token
                    cb.onToken(accessToken);


                } catch (JSONException e) {
                    Log.e("MyApplication", "Invalid JSON response", e);
                }
            }
        });
    }

    private interface TwilioTokenCallback {
        void onToken(String accessToken);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                Intent intent = new Intent(ReceiveCallTestActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSuccess(ResponseBody responseBody) {

        String responseBodyString = null;
        try {
            String result = responseBody.string();
            System.out.println("Result: "+result);

            if (result != null && result.contains("Token") ){


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

