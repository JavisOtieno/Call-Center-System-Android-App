package com.scg.scgcalls;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
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

public class EmailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ActionBar actionBar;
    private Button btnSendEmail;

    private String callerEmail;
    private String selectedEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_email);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Email");
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etMessage = findViewById(R.id.etMessage);
        EditText etSubject = findViewById(R.id.etSubject);
        Button btnSendEmail = findViewById(R.id.btnSendEmail);

        Spinner spinnerEmailFrom = findViewById(R.id.spinnerEmailFrom);

        String callerEmail = getIntent().getStringExtra("callerEmail");
        if (callerEmail != null && !callerEmail.isEmpty()) {
            etEmail.setText(callerEmail);
            // Use callerEmail safely
        } else {
            // default for now
//            callerEmail = "+15048263789"; // or any default/fallback value
        }

// Replace these with actual values from your backend if needed
        String username = "johndoe"; // Get this dynamically
        String[] emails = {
                username + "@inbound.testinboundparse.av.ke",
                "sales@inbound.testinboundparse.av.ke"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, emails);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEmailFrom.setAdapter(adapter);

// Optional: handle selection
        spinnerEmailFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedEmail = parent.getItemAtPosition(position).toString();
                Toast.makeText(getApplicationContext(), "Selected: " + selectedEmail, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });


        if (callerEmail != null && !callerEmail.isEmpty()) {
            // Use callerPhone safely
            etEmail.setText(callerEmail);
        } else {
            // default for now
//            callerEmail = "+15048263789"; // or any default/fallback value
        }

        btnSendEmail.setOnClickListener(v -> {
            String toEmail = etEmail.getText().toString().trim();
            String messageBody = etMessage.getText().toString().trim();
            String subject = etSubject.getText().toString().trim();

            if (toEmail.isEmpty() || messageBody.isEmpty()) {
                Toast.makeText(EmailActivity.this, "Please enter both email and message", Toast.LENGTH_SHORT).show();
            } else {
                sendEmail(selectedEmail,toEmail,subject, messageBody);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                Intent intent = new Intent(EmailActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void sendEmail(String emailfrom, String emailto, String subject,
                           String messageBody) {
        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        try {
            json.put("emailfrom", emailfrom);
            json.put("emailto", emailto);
            json.put("subject",subject);
            json.put("message", messageBody);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        String authToken = EncryptedPrefsUtil.getString("authToken", "");

        Request request = new Request.Builder()
                .url(Constants.BASE_URL+"sendemail")
                .addHeader("Authorization", "Bearer " + authToken)
                .post(okhttp3.RequestBody.create(
                        json.toString(),
                        okhttp3.MediaType.parse("application/json")))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                Log.e("EmailActivity", "Email send failed", e);
                runOnUiThread(() -> Toast.makeText(EmailActivity.this, "Email send failed", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(EmailActivity.this, "Email sent!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(EmailActivity.this, EmailsActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(EmailActivity.this, "Email send error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

}

