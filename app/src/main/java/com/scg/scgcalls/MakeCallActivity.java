package com.scg.scgcalls;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.scg.scgcalls.util.EncryptedPrefsUtil;
import com.twilio.voice.Call;
import com.twilio.voice.CallException;
import com.twilio.voice.ConnectOptions;
import com.twilio.voice.Voice;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MakeCallActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_make_call);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Make Call");



        tvStatus  = findViewById(R.id.tvStatus);
        btnCall   = findViewById(R.id.btnCall);
        btnHangup = findViewById(R.id.btnHangup);

//        Button crashButton = findViewById(R.id.crash_button);
//        crashButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                throw new RuntimeException("Test Crash"); // Force a crash
//            }
//        });

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM token failed", task.getException());
                        return;
                    }

                    // Get the token
                    String token = task.getResult();
                    Log.d("FCM", "Fetched token manually: " + token);
                });


        fetchAccessToken();

        btnCall.setOnClickListener(v -> makeCall());
        btnHangup.setOnClickListener(v -> hangupCall());
    }




    private void fetchAccessToken() {
        OkHttpClient client = new OkHttpClient();
        String token = "Bearer " + EncryptedPrefsUtil.getString("authToken", "");

        Request request = new Request.Builder()
                .url(TOKEN_URL)
                .addHeader("Authorization", token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                Log.e(TAG, "Token fetch failed", e);
                runOnUiThread(() -> tvStatus.setText("Token fetch failed"));
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Unexpected code " + response);
                    runOnUiThread(() -> tvStatus.setText("Token error"));
                    return;
                }
                try {
                    JSONObject json = new JSONObject(response.body().string());
                    accessToken = json.getString("token");
                    runOnUiThread(() -> {
                        tvStatus.setText("Ready to call");
                        btnCall.setEnabled(true);
                    });
                } catch (Exception e) {
                    Log.e(TAG, "JSON parse error", e);
                }
            }
        });
    }

    private void makeCall() {
        Toast.makeText(MakeCallActivity.this,accessToken,Toast.LENGTH_LONG).show();
//        ConnectOptions options = new ConnectOptions.Builder(accessToken)
//                .params(Collections.singletonMap("to", TO_NUMBER))
//                .build();

        Map<String, String> params = new HashMap<>();
        String callerPhone = getIntent().getStringExtra("callerPhone");
        if (callerPhone != null && !callerPhone.isEmpty()) {
            // Use callerPhone safely
        } else {
            // default for now
            callerPhone = "+15048263789"; // or any default/fallback value
        }
        params.put("phoneNumber", callerPhone);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                Intent intent = new Intent(MakeCallActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


}