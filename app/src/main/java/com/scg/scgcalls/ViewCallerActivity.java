package com.scg.scgcalls;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;


import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import okhttp3.ResponseBody;

public class ViewCallerActivity extends AppCompatActivity implements OnSuccessListener {
    private ActionBar actionBar;
    private Toolbar toolbar;
    private Button endCallButton;
    private Boolean isCallRunning = false;
    private EditText startLocationEditText;
    private EditText destinationLocationEditText;
    private EditText descriptionEditText;
    private EditText stopoverEditText;
    private EditText amountEditText;
    private String startLocation;
    private String destinationLocation;
    private String description;
    private String amount;
    private String endpoint;
    private String callerId;
    private SharedPreferences mPrefs;
    private String mCallId;

    private EditText dateEditText;
    private EditText nameEditText;
    private EditText timeEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private EditText callerEditText;
    private EditText typeEditText;
    private Button whatsappButton;
    private Button messageButton;
    private Button videoButton;
    private Button callButton;
    private Button emailButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_caller);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("View Caller");





        phoneEditText = (EditText) findViewById(R.id.phoneEditText);
        nameEditText = (EditText) findViewById(R.id.nameEditText);
        emailEditText = (EditText) findViewById(R.id.emailEditText);

        dateEditText = (EditText) findViewById(R.id.dateEditText);
        timeEditText = (EditText) findViewById(R.id.timeEditText);
        whatsappButton = (Button) findViewById(R.id.whatsappButton);
        messageButton = (Button) findViewById(R.id.messageButton);
        videoButton = (Button) findViewById(R.id.videoButton);
        callButton = (Button) findViewById(R.id.callButton);
        emailButton = (Button) findViewById(R.id.emailButton);

//        typeEditText = (EditText) findViewById(R.id.typeEditText);

        whatsappButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewCallerActivity.this,WhatsappThreadActivity.class);
                intent.putExtra("callerId",callerId);
                intent.putExtra("callerPhone",phoneEditText.getText().toString());
                startActivity(intent);
            }
        });

        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ViewCallerActivity.this,CreateRoomActivity.class);
                intent.putExtra("callerId",callerId);
                intent.putExtra("callerPhone",phoneEditText.getText().toString());
                intent.putExtra("callerEmail",emailEditText.getText().toString());
                startActivity(intent);

            }
        });

        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewCallerActivity.this,MessagesThreadActivity.class);
                intent.putExtra("callerId",callerId);
                intent.putExtra("callerPhone",phoneEditText.getText().toString());
                startActivity(intent);
            }
        });

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewCallerActivity.this,MakeCallActivity.class);
//                intent.putExtra("callerId",callerId);
                intent.putExtra("callerPhone",phoneEditText.getText().toString());
                startActivity(intent);

            }
        });

        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });






        Intent intent = getIntent();
        callerId = intent.getStringExtra("callerId");
        endpoint = "caller/"+callerId;


        NetworkUtils.
                fetchData("GET",endpoint,
                        null, ViewCallerActivity.this,
                        ViewCallerActivity.this);

        //START OF LATEST LOCATION CALL UPDATE


        //END OF LATEST LOCATION CALL UPDATE





//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

    }

    @Override
    public void onSuccess(ResponseBody responseBody) {
        String responseBodyString = null;
        try {
            String result = responseBody.string();
            System.out.println("Result: "+result);

            if (result != null && result.contains("caller") &&
                    endpoint.equals("caller/"+callerId)){

                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.has("caller") &&
                        jsonObject.get("caller") instanceof JSONObject
                ) {

                    JSONObject caller = jsonObject.getJSONObject("caller");



                    nameEditText.setText(caller.
                            isNull("name") ? "Unknown Name" : caller.optString("name"));
                    emailEditText.setText(caller.isNull("email") ? "No Email Provided" : caller.optString("email"));
                    phoneEditText.setText(caller.isNull("phone" ) ? "No Phone Provided" : caller.optString("phone"));




                    String createdAt = caller.getString("created_at");
                    ZonedDateTime dateTime = Instant.parse(createdAt).atZone(ZoneId.of("Africa/Nairobi"));
                    String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
                    String formattedTime = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
                    dateEditText.setText(formattedDate);
                    timeEditText.setText(formattedTime);


                }
                else if(result.contains("errors")){
                    String caller = jsonObject.getString("caller");
//                    Toasts.toastIconError(IndividualDealActivity.this,caller);
                    Toast.makeText(getBaseContext(), caller, Toast.LENGTH_SHORT).show();
                }
                else{
//                    Toasts.toastIconError(IndividualDealActivity.this,"Error Loading Account. Please Try Again");
                    Toast.makeText(getBaseContext(), "Error Loading Caller. Please Try Again", Toast.LENGTH_SHORT).show();
//                    FirebaseCrashlytics.getInstance().recordException(new Exception("Add Account Error: "+result));

                }

            }

            else{
//                Toasts.toastIconError(IndividualDealActivity.this,"Loading Account Failed. Please Try Again");
                Toast.makeText(getBaseContext(), "Something went wrong. Please Try Again", Toast.LENGTH_SHORT).show();
                FirebaseCrashlytics.getInstance().recordException(new Exception("Add Account Error: "+result));

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

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
//                Intent intent = new Intent(ViewCallerActivity.this, OutgoingCallersActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //START OF LATEST LOCATION CODE


    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }




    //PICKED FROM OLD CODE BUT MIGHT BE VALID
    @Override
    public void onResume() {
        super.onResume();

    }
    //PICKED FROM OLD CODE BUT MIGHT BE VALID
    // method to check for permissions



}