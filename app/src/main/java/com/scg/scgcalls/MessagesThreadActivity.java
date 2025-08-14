package com.scg.scgcalls;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.Button;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionStateChange;
import com.scg.scgcalls.adapters.MessageAdapter;
import com.scg.scgcalls.models.Message;
import com.scg.scgcalls.util.EncryptedPrefsUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MessagesThreadActivity extends AppCompatActivity {

    private RecyclerView rv;
    private MessageAdapter adapter;
    private List<Message> messages = new ArrayList<>();
    private EditText et;
    private Button btn;
    private Pusher pusher;
    private Channel channel;
    private String callerPhone; // pass dynamically as needed
    private String callerId;
    private Toolbar toolbar;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages_thread);
        rv = findViewById(R.id.rvMessages);
        et = findViewById(R.id.etMessage);
        btn = findViewById(R.id.btnSend);


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Messages");

        callerPhone = getIntent().getStringExtra("callerPhone");


        callerId = getIntent().getStringExtra("callerId");




        // Send on button
        btn.setOnClickListener(v -> sendMessage());

        // Send on Enter
        et.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_UP) {
                sendMessage();
                return true;
            }
            return false;
        });

        loadHistory();




        initPusher();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendMessage() {
        String text = et.getText().toString().trim();
        if (text.isEmpty()) return;
        et.setText("");

        // Post to Laravel
        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        try {
            json.put("phoneNumber", callerPhone);
            json.put("message", text);
        } catch (JSONException e) { e.printStackTrace(); }

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.get("application/json; charset=utf-8")
        );
        String token = "Bearer " + EncryptedPrefsUtil.getString("authToken", "");

        Request req = new Request.Builder()
                .url(Constants.BASE_URL+"sendmessage")
                .post(body)
                .addHeader("Authorization", "Bearer " + token)
//                .addHeader("X-CSRF-TOKEN", "YOUR_TOKEN_HERE")
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            //            @Override public void onFailure(Call c, IOException e) { }
            @Override public void onResponse(Call c, Response r) {
                // on success, add to list locally (optional)
//                runOnUiThread(() -> {
//                    String time = new SimpleDateFormat(
//                            "yyyy-MM-dd HH:mm", Locale.getDefault()
//                    ).format(new Date());
//                    messages.add(new Message(text, time, true));
//                    Toast.makeText(WhatsappThreadActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
//                    adapter.notifyItemInserted(messages.size()-1);
//                });

                if (r.isSuccessful()) {
                    // on success, add to list locally
//                    runOnUiThread(() -> {
//                        String time = new SimpleDateFormat(
//                                "yyyy-MM-dd HH:mm", Locale.getDefault()
//                        ).format(new Date());
//                        messages.add(new Message(text, time, true));
//                        Toast.makeText(WhatsappThreadActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
//                        adapter.notifyItemInserted(messages.size() - 1);
//                    });
                } else {
                    // Handle HTTP error (e.g., 400, 500)
                    runOnUiThread(() ->
                            Toast.makeText(MessagesThreadActivity.this, "Failed to send message: " + r.code(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void initPusher() {
        PusherOptions opts = new PusherOptions()
                .setCluster("ap2");
        pusher = new Pusher("86173f2abab182590bf5", opts);

        channel = pusher.subscribe("caller."+callerId);
        channel.bind("App\\Events\\MessageCreated", (event) -> {
            // parse JSON
            try {
                JSONObject d = new JSONObject(event.getData());
                final String msg = d.getString("message");
                final String time = d.getString("time");
                final String type = d.getString("type");
                runOnUiThread(() -> {
                    if(type.equals("inbound")){
                        messages.add(new Message(msg, time, false));
                    }else{
                        messages.add(new Message(msg, time, true));
                    }

                    adapter.notifyItemInserted(messages.size()-1);
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

//        pusher.connect();
        pusher.connect(new ConnectionEventListener() {
            @Override
            public void onConnectionStateChange(ConnectionStateChange change) {
                Log.d("Pusher", "State changed: " + change.getCurrentState());
            }

            @Override
            public void onError(String message, String code, Exception e) {
                Log.e("Pusher", "Error: " + message + ", code: " + code, e);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (pusher != null) pusher.disconnect();
        super.onDestroy();
    }

    private void loadHistory() {
        OkHttpClient client = new OkHttpClient();
        String url = Constants.BASE_URL+"threadmessages/" + callerId;
        Request req = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MessagesThreadActivity.this,
                                "Failed to load history: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(MessagesThreadActivity.this,
                                    "Error loading history: HTTP " + response.code(),
                                    Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                String json = response.body().string();
                Log.d("API Response", json);



                try {
                    JSONObject jsonObject = new JSONObject(json);

//                    JSONArray arr = new JSONArray(json);
                    JSONArray arr = jsonObject.getJSONArray("messages");
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        String text = obj.getString("message");

                        String createdAt = obj.getString("created_at");
                        ZonedDateTime dateTime = Instant.parse(createdAt).atZone(ZoneId.of("Africa/Nairobi"));
                        String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
                        String formattedTime = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
//                        dateEditText.setText(formattedDate);
                        String when = formattedDate+" "+formattedTime;
//                        timeEditText.setText(formattedTime);
                        boolean outbound = "outbound".equals(obj.getString("type"));
                        messages.add(new Message(text, when, outbound));
                    }
                    runOnUiThread(() -> {

                        adapter = new MessageAdapter(MessagesThreadActivity.this, messages);
                        rv.setLayoutManager(new LinearLayoutManager(MessagesThreadActivity.this));
                        rv.setAdapter(adapter);

                        // Scroll to bottom on data change
                        adapter.registerAdapterDataObserver(
                                new RecyclerView.AdapterDataObserver() {
                                    @Override
                                    public void onItemRangeInserted(int pos, int count) {
                                        rv.smoothScrollToPosition(adapter.getItemCount()-1);
                                    }
                                });

//                        adapter.notifyDataSetChanged();
                        rv.scrollToPosition(messages.size() - 1);
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
