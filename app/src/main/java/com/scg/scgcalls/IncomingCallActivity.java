package com.scg.scgcalls;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.twilio.voice.Call;
import com.twilio.voice.CallException;
import com.twilio.voice.ConnectOptions;
import com.twilio.voice.Voice;


public class IncomingCallActivity extends AppCompatActivity {
        private String accessToken;
        private String caller;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_incoming_call2);

//            accessToken = getIntent().getStringExtra("access_token");
            SharedPreferences prefs = getSharedPreferences("call_data", MODE_PRIVATE);
            accessToken = prefs.getString("access_token", null);
            Toast.makeText(IncomingCallActivity.this,accessToken+"-",Toast.LENGTH_SHORT).show();
            caller = getIntent().getStringExtra("caller");

            TextView callerText = findViewById(R.id.callerName);
            callerText.setText("Call from: " + caller);

            findViewById(R.id.answerButton).setOnClickListener(v -> answerCall());
        }

        private void answerCall() {
            ConnectOptions connectOptions = new ConnectOptions.Builder(accessToken).build();
            Voice.connect(getApplicationContext(), connectOptions, new Call.Listener() {
                @Override
                public void onConnected(Call call) {
                    Toast.makeText(getApplicationContext(), "Call Connected", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onReconnecting(@NonNull Call call, @NonNull CallException callException) {

                }

                @Override
                public void onReconnected(@NonNull Call call) {

                }

                @Override
                public void onDisconnected(Call call, CallException error) {
                    Toast.makeText(getApplicationContext(), "Call Ended", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onConnectFailure(Call call, CallException error) {
                    Toast.makeText(getApplicationContext(), "Call Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onRinging(@NonNull Call call) {

                }
            });
        }
    }
