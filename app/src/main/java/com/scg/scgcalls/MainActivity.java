package com.scg.scgcalls;
// src/main/java/com/yourapp/MainActivity.java

//package com.example.twiliovoice;


import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.scg.scgcalls.util.EncryptedPrefsUtil;
import com.twilio.voice.Call;
import com.twilio.voice.CallException;
import com.twilio.voice.CallInvite;
import com.twilio.voice.ConnectOptions;
import com.twilio.voice.Voice;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Callback;
import okhttp3.ResponseBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnSuccessListener {
    private static final String TAG = "TwilioVoice";
    private static final String TOKEN_URL = "https://testproject.scgafrica.com/api/twilio/token";
    private static final String TO_NUMBER  = "+15048263789";  // hard-coded

    private String accessToken;
    private Call activeCall;

    private TextView tvStatus;
    private Button btnCall, btnHangup;
    private static final int REQUEST_RECORD_AUDIO = 1;
    private ActionBar actionBar;
    private Toolbar toolbar;

    public static boolean isActive = false;

    private static CallInvite pendingInvite;
//    private static Call activeCall;
    private static Context appContext;
//    private static final String TAG = "MainActivity";

//    private Toolbar toolbar;
//    private ActionBar actionBar;

    private String token;

    private String mUserId;
    private String mAuthToken;

    private static AlertDialog incomingCallDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appContext = this;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Dashboard Alerts");

        initNavigationMenu();

        EncryptedPrefsUtil.init(this);

        mUserId = EncryptedPrefsUtil.getString("userId", "0");
        mAuthToken = EncryptedPrefsUtil.getString("authToken", "");


        if (mUserId.equals("0")) {

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }else {


            createNotificationChannel();

//            FirebaseMessaging.getInstance().getToken()
//                    .addOnCompleteListener(task -> {
//                        if (!task.isSuccessful()) {
//                            Log.w("FCM", "Fetching FCM token failed", task.getException());
//                            return;
//                        }
//
//                        // Get the token
//                        token = task.getResult();
//                        Log.d("FCM", "Fetched FCM token manually: " + token);
//
//                        fetchTwilioAccessToken(accessToken -> {
//
//                            Log.d("FCM", "🔑 Twilio Access Token: " + accessToken);
////                        SharedPreferences prefs = getSharedPreferences("call_data", MODE_PRIVATE);
////                        SharedPreferences.Editor editor = prefs.edit();
////                        editor.putString("access_token", accessToken);
////                        editor.apply();
//
//
//                            MainActivity.this.runOnUiThread(() -> {
//                                Toast.makeText(MainActivity.this, accessToken + "-receive", Toast.LENGTH_SHORT).show();
//                            });
//
//                            SharedPreferences prefs = getSharedPreferences("call_data", MODE_PRIVATE);
//                            SharedPreferences.Editor editor = prefs.edit();
//                            editor.putString("access_token", token);
//                            editor.apply();
//
//
////
////                        Voice.register(
////                                accessToken,                    // your Twilio Access Token string
////                                Voice.RegistrationChannel.FCM,        // tell the SDK this is FCM
////                                token,                       // the FCM device token
////                                new RegistrationListener() {
////                                    @Override
////                                    public void onRegistered(@NonNull String at, @NonNull String rt) {
////                                        Log.i(TAG, "Twilio registered. AccessToken=" + at);
////                                    }
////
////                                    @Override
////                                    public void onError(@NonNull RegistrationException error,
////                                                        @NonNull String at,
////                                                        @NonNull String rt) {
////                                        Log.e(TAG, "Registration failed: " + error.getMessage());
////                                    }
////                                }
////                        );
//                        });
//                    });
//
//
//            new Handler(Looper.getMainLooper()).postDelayed(() -> {
//                JSONObject requestBody = new JSONObject();
//                try {
//                    requestBody.put("fcmtoken", token);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//                NetworkUtils.
//                        fetchDataPost("registerfcmtoken",
//                                "", requestBody, MainActivity.this,
//                                MainActivity.this);
//            }, 4000);






            ensureAudioPermission();
        }

//        fetchAccessToken();

//        btnCall.setOnClickListener(v -> makeCall());
//        btnHangup.setOnClickListener(v -> hangupCall());
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

    public static void showIncomingCallAlertDialog(String caller) {
        Context context = appContext; // Only available if you're holding it
        if (context == null) return;

//        pendingInvite = invite;



        AlertDialog.Builder builder = new AlertDialog.Builder(context).setTitle("Incoming Call")
                .setMessage("From: " +caller+" - Check Web Dashboard to Answer")
//                .setPositiveButton("Accept", (dialog, which) -> {
//                    activeCall = invite.accept(context, callListener);
//                    pendingInvite = null;
//                })
                .setNegativeButton("IGNORE", (dialog, which) -> {
//                    invite.reject(context);
//                    pendingInvite = null;
                })
                .setCancelable(false);
        incomingCallDialog = builder.create();
        incomingCallDialog.show();
    }

    public static void dismissIncomingCallAlertDialog() {
        if (incomingCallDialog != null && incomingCallDialog.isShowing()) {
            incomingCallDialog.dismiss();
            incomingCallDialog = null;
        }
    }

    private void initNavigationMenu() {
        NavigationView nav_view = (NavigationView) findViewById(R.id.nav_view);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(final MenuItem item) {
//                Toast.makeText(getApplicationContext(), item.getTitle() + " Selected", Toast.LENGTH_SHORT).show();
                if (item.getTitle().equals("Dashboard Alerts")) {
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    //finish();
                    //startActivity(getIntent());
                }
                else if (item.getTitle().equals("Callers")) {
                    Intent intent = new Intent(MainActivity.this, CallersActivity.class);
                    startActivity(intent);
                }
                else if (item.getTitle().equals("Make Call")) {
                    Intent intent = new Intent(MainActivity.this, MakeCallActivity.class);
                    startActivity(intent);
                }
                else if (item.getTitle().equals("Send Message")) {
                    Intent intent = new Intent(MainActivity.this, MessageActivity.class);
                    startActivity(intent);
                }
                else if (item.getTitle().equals("Send Whatsapp Message")) {
                    Intent intent = new Intent(MainActivity.this, SendWhatsappMessageActivity.class);
                    startActivity(intent);
                }

                else if (item.getTitle().equals("Outgoing Calls")) {
                    Intent intent = new Intent(MainActivity.this, OutgoingCallsActivity.class);
                    startActivity(intent);
                }

                else if (item.getTitle().equals("Incoming Calls")) {
                    Intent intent = new Intent(MainActivity.this, IncomingCallsActivity.class);
                    startActivity(intent);
                }
                else if (item.getTitle().equals("Messages")) {
                    Intent intent = new Intent(MainActivity.this, MessagesActivity.class);
                    startActivity(intent);
                }
                else if (item.getTitle().equals("Whatsapp Messages")) {
                    Intent intent = new Intent(MainActivity.this, WhatsappmessagesActivity.class);
                    startActivity(intent);
                }
                else if (item.getTitle().equals("Whatsapp Thread")) {
                    Intent intent = new Intent(MainActivity.this, WhatsappThreadActivity.class);
                    startActivity(intent);
                }
                else if (item.getTitle().equals("Gemini Test")) {
                    Intent intent = new Intent(MainActivity.this, GeminiTestActivity.class);
                    startActivity(intent);
                }
                else if (item.getTitle().equals("Video Call")) {
                    Intent intent = new Intent(MainActivity.this, CreateRoomActivity.class);
                    startActivity(intent);
                }
                else if (item.getTitle().equals("Email")) {
                    Intent intent = new Intent(MainActivity.this, EmailActivity.class);
                    startActivity(intent);
                }
                else if (item.getTitle().equals("Emails")) {
                    Intent intent = new Intent(MainActivity.this, EmailsActivity.class);
                    startActivity(intent);
                }
                else if (item.getTitle().equals("Video Calls")) {
                    Intent intent = new Intent(MainActivity.this, VideocallsActivity.class);
                    startActivity(intent);
                }





                else if (item.getTitle().equals("Profile")) {
//                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
//                    startActivity(intent);
                }

                else if (item.getTitle().equals("Log out")) {
//                    SharedPreferences.Editor mEditor = mPrefs.edit();
//                    mEditor.putString("userId", "0").commit();
//                    EncryptedPrefsUtil.saveString("userId", "0");
//                    EncryptedPrefsUtil.saveString("authToken", "");
//                    EncryptedPrefsUtil.saveString("email", "");
//                    EncryptedPrefsUtil.saveString("password", "");

//                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
                }

//                else if(item.getTitle().equals("Profile")){
//                    Intent intent = new Intent(MainActivity.this,ProfileActivity.class);
//                    startActivity(intent);
//                }else if(item.getTitle().equals("POS Audits")){
//                    Intent intent = new Intent(MainActivity.this,POSAuditsListActivity.class);
//                    startActivity(intent);
//                }

                //actionBar.setTitle(item.getTitle());
                drawer.closeDrawers();
                return true;

            }
        });

        // open drawer at start
        //drawer.openDrawer(GravityCompat.START);
    }





    private void ensureAudioPermission() {
        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{ android.Manifest.permission.RECORD_AUDIO },
                    REQUEST_RECORD_AUDIO);
        } else {
//            startRecording();  // your logic
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                startRecording();
            } else {
                Toast.makeText(this,
                        "Microphone access is required to record audio",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "default",
                    "Default Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Used for FCM");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void makeCall() {
        Toast.makeText(MainActivity.this,accessToken,Toast.LENGTH_LONG).show();
//        ConnectOptions options = new ConnectOptions.Builder(accessToken)
//                .params(Collections.singletonMap("to", TO_NUMBER))
//                .build();

        Map<String, String> params = new HashMap<>();
        params.put("phoneNumber", "+15048263789");
        params.put("name", "Testing");
        params.put("id", "1");
        params.put("to", TO_NUMBER);

        ConnectOptions options = new ConnectOptions.Builder(accessToken)
                .params(params)
                .build();

        activeCall = Voice.connect(getApplicationContext(), options, callListener);

        new DialPadDialog(activeCall).show(getSupportFragmentManager(), "dialPad");

        tvStatus.setText("Calling…");
        btnHangup.setEnabled(true);
        btnCall.setEnabled(false);
    }

    private void hangupCall() {
        if (activeCall != null) {
            activeCall.disconnect();
        }
    }

    private final Call.Listener callListener = new Call.Listener() {
        @Override
        public void onRinging(@NonNull Call call) {
            runOnUiThread(() -> tvStatus.setText("Ringing…"));
        }

        @Override
        public void onConnected(@NonNull Call call) {
            runOnUiThread(() -> tvStatus.setText("In call"));
        }

        @Override
        public void onReconnecting(@NonNull Call call, @NonNull CallException error) {
            // Triggered when network blips and SDK is trying to recover
            runOnUiThread(() -> tvStatus.setText("Reconnecting…"));
        }

        @Override
        public void onReconnected(@NonNull Call call) {
            // Triggered after a successful network recovery
            runOnUiThread(() -> tvStatus.setText("Reconnected"));
        }

        @Override
        public void onDisconnected(@NonNull Call call, CallException error) {
            runOnUiThread(() -> {
                tvStatus.setText("Call ended");
                btnHangup.setEnabled(false);
                btnCall.setEnabled(true);
            });
        }

        @Override
        public void onConnectFailure(@NonNull Call call, @NonNull CallException error) {
            runOnUiThread(() -> {
                tvStatus.setText("Connect failed: " + error.getMessage());
                btnHangup.setEnabled(false);
                btnCall.setEnabled(true);
            });
        }
    };


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


}
