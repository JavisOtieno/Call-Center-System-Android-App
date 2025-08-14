package com.scg.scgcalls;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.twilio.voice.RegistrationException;
import com.twilio.voice.RegistrationListener;
import com.twilio.voice.Voice;
import com.twilio.voice.Voice.RegistrationChannel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        // 🔧 Ensure Firebase is initialized first
        FirebaseApp.initializeApp(this);

//        // 1) First get the FCM token
//        FcmHelper.getFcmToken(this, fcmToken -> {
//            // 2) Now fetch your Twilio Access Token from your server
////            fetchTwilioAccessToken(accessToken -> {
//
//                Log.d("TwilioSetup", "📲 FCM Token: " + fcmToken);
//
//                // Step 2: Fetch Twilio access token from your Laravel server
//                fetchTwilioAccessToken(accessToken -> {
//                    // Step 3: Log Twilio token
//                    Log.d("TwilioSetup", "🔑 Twilio Access Token: " + accessToken);
//                // 3) Register with Twilio using the 4‑arg signature
//                Voice.register(
//                        accessToken,                    // your Twilio Access Token string
//                        RegistrationChannel.FCM,        // tell the SDK this is FCM
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
//            });
//        });
    }

    /** Helper to asynchronously fetch the Twilio Access Token from your backend */
    private void fetchTwilioAccessToken(@NonNull TwilioTokenCallback cb) {
        // Example with OkHttp (add your own dependency/imports):
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Constants.BASE_URL+"twilio/token")  // "https://…/token"
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("MyApplication", "Token fetch failed", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
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
}
