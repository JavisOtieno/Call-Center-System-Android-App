package com.scg.scgcalls;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;

import com.scg.scgcalls.util.EncryptedPrefsUtil;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SendWhatsappMessageActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ActionBar actionBar;
    private Button btnSendSms;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_whatsappmessage);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Whatsapp Message");
        EditText etPhoneNumber = findViewById(R.id.etPhoneNumber);
        EditText etMessage = findViewById(R.id.etMessage);
        Button btnSendSms = findViewById(R.id.btnSendSms);

        btnSendSms.setOnClickListener(v -> {
            String toNumber = etPhoneNumber.getText().toString().trim();
            String messageBody = etMessage.getText().toString().trim();

            if (toNumber.isEmpty() || messageBody.isEmpty()) {
                Toast.makeText(SendWhatsappMessageActivity.this, "Please enter both phone number and message", Toast.LENGTH_SHORT).show();
            } else {
                sendSms(toNumber, messageBody);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                Intent intent = new Intent(SendWhatsappMessageActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void sendSms(String toNumber, String messageBody) {
        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        try {
            json.put("phoneNumber", toNumber);
            json.put("message", messageBody);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        String authToken = EncryptedPrefsUtil.getString("authToken", "");

        Request request = new Request.Builder()
                .url(Constants.BASE_URL+"sendwhatsappmessage")
                .addHeader("Authorization", "Bearer " + authToken)
                .post(okhttp3.RequestBody.create(
                        json.toString(),
                        okhttp3.MediaType.parse("application/json")))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                Log.e("SendWhatsappMessageActivity", "Whatsapp send failed", e);
                runOnUiThread(() -> Toast.makeText(SendWhatsappMessageActivity.this, "Whatsapp send failed", Toast.LENGTH_SHORT).show());

            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(SendWhatsappMessageActivity.this, "Whatsapp sent!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SendWhatsappMessageActivity.this, WhatsappmessagesActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(SendWhatsappMessageActivity.this, "Whatsapp send error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

}

