package com.scg.scgcalls;


import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

public class FcmHelper {
    private static final String TAG = "FcmHelper";

    public interface TokenCallback {
        void onTokenReady(String token);
    }

    public static void getFcmToken(Context ctx, TokenCallback cb) {
        FirebaseApp.initializeApp(ctx);
        FirebaseMessaging.getInstance()
                .getToken()
                .addOnSuccessListener(cb::onTokenReady)
                .addOnFailureListener(e -> Log.e(TAG, "FCM token failure", e));
    }

    public static void sendTokenToServer(String token) {
        // TODO: POST the token to your backend for Twilio registration
        Log.i(TAG, "Send this FCM token to your server: " + token);
    }
}

