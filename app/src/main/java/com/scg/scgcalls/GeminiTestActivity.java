package com.scg.scgcalls;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.*;
import android.view.View;

import org.json.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.*;

public class GeminiTestActivity extends AppCompatActivity {
    private LinearLayout chatLayout;
    private EditText messageInput;
    private Button sendBtn;
    private ScrollView scrollView;

    private final List<String[]> history = new ArrayList<>();
    private final OkHttpClient client = new OkHttpClient();
    private Toolbar toolbar;
    private ActionBar actionBar;
    private final String BACKEND_URL = "https://testproject.scgafrica.com/api/chat"; // Replace with your server

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gemini_test);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Gemini Test");

        chatLayout = findViewById(R.id.chatLayout);
        messageInput = findViewById(R.id.messageInput);
        sendBtn = findViewById(R.id.sendBtn);
        scrollView = findViewById(R.id.scrollView);

        sendBtn.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                addMessage(message, true);
                sendMessageToGemini(message);
                messageInput.setText("");
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
//                Intent intent = new Intent(ViewMessageActivity.this, OutgoingMessagesActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void addMessage(String text, boolean isUser) {
        TextView textView = new TextView(this);
        textView.setText((isUser ? "You: " : "Bot: ") + text);
        textView.setTextSize(16);
        textView.setPadding(10, 10, 10, 10);
        chatLayout.addView(textView);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void sendMessageToGemini(String userMsg) {
        try {
            JSONObject json = new JSONObject();
            json.put("message", userMsg);

            JSONArray histArray = new JSONArray();
            for (String[] pair : history) {
                JSONArray turn = new JSONArray();
                turn.put(pair[0]);  // user
                turn.put(pair[1]);  // bot
                histArray.put(turn);
            }
            json.put("history", histArray);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(BACKEND_URL)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    final String errorMsg = "Error: 1" + e.getMessage();
                    runOnUiThread(() -> addMessage(errorMsg, false));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String resStr = response.body().string();

                    if (!response.isSuccessful()) {
                        final String errorMsg = "Error: 2" + response.message();
                        runOnUiThread(() -> addMessage(errorMsg, false));
                        return;
                    }

                    String botReply;
                    try {
                        JSONObject resJson = new JSONObject(resStr);
                        botReply = resJson.optString("response", "No response");
                    } catch (JSONException e) {
                        final String parseError = "Error parsing response";
                        runOnUiThread(() -> addMessage(parseError, false));
                        return;
                    }

                    final String finalReply = botReply;
                    runOnUiThread(() -> {
                        addMessage(finalReply, false);
                        history.add(new String[]{userMsg, finalReply});
                    });
                }

            });
        } catch (JSONException e) {
            addMessage("Invalid JSON", false);
        }
    }
}