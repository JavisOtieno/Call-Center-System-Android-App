package com.scg.scgcalls;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;

public class CreateRoomActivity extends AppCompatActivity implements OnSuccessListener {

    private String roomExtended;
    private Toolbar toolbar;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_room);
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
        actionBar.setTitle("Create Room");

        Button createRoomBtn = findViewById(R.id.createRoomBtn);
        EditText roomEditText = findViewById(R.id.roomEditText);

        createRoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String baseName = roomEditText.getText().toString();

                if (baseName.isEmpty()) return;

// Generate timestamp in format YYYYMMDDHHMMSS
                String timestamp = new java.text.SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault())
                        .format(new java.util.Date());

// Generate random 6-character alphanumeric suffix
                String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
                StringBuilder suffix = new StringBuilder();
                java.util.Random random = new java.util.Random();
                for (int i = 0; i < 6; i++) {
                    suffix.append(chars.charAt(random.nextInt(chars.length())));
                }
// Combine all parts
                roomExtended = baseName + "-" + timestamp + "-" + suffix.toString();

                String endpoint = "adminvideo/room/"+baseName+"/"+roomExtended;

                NetworkUtils.
                        fetchData("GET",endpoint,
                                null, CreateRoomActivity.this,
                                CreateRoomActivity.this);


            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
//                Intent intent = new Intent(CreateRoomActivity.this, MainActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSuccess(ResponseBody responseBody) {

        String result = null;
        try {
            result = responseBody.string();

            System.out.println("Result: "+result);
            if (result != null && result.contains("room")
            ){

                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.has("room") &&
                        jsonObject.get("room") instanceof String
                ) {
                    String room = jsonObject.getString("room");

                    Toast.makeText(getBaseContext(), "Room Created - "+room, Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(CreateRoomActivity.this, VideoRoomActivity.class);
                    intent.putExtra("roomName", roomExtended);
                    startActivity(intent);

                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public void onComplete() {

    }
}