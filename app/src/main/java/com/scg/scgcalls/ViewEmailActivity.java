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

public class ViewEmailActivity extends AppCompatActivity implements OnSuccessListener {
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
    private String emailId;
    private SharedPreferences mPrefs;
    private String mCallId;

    private EditText dateEditText;
    private EditText timeEditText;
    private EditText phoneEditText;
    private EditText emailToEditText;
    private EditText emailFromEditText;
    private EditText subjectEditText;
    private EditText messageEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_email);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("View Email");

        emailToEditText = (EditText) findViewById(R.id.emailToEditText);
        emailFromEditText = (EditText) findViewById(R.id.emailFromEditText);
        messageEditText = (EditText) findViewById(R.id.messageText);
        dateEditText = (EditText) findViewById(R.id.dateEditText);
        timeEditText = (EditText) findViewById(R.id.timeEditText);
        subjectEditText = (EditText) findViewById(R.id.subjectEditText);


        Intent intent = getIntent();
        emailId = intent.getStringExtra("emailId");
        endpoint = "email/"+emailId;


        NetworkUtils.
                fetchData("GET",endpoint,
                        null, ViewEmailActivity.this,
                        ViewEmailActivity.this);

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

            if (result != null && result.contains("email") &&
                    endpoint.equals("email/"+emailId)){

                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.has("email") &&
                        jsonObject.get("email") instanceof JSONObject
                ) {

                    JSONObject email = jsonObject.getJSONObject("email");

                    emailToEditText.setText(email.
                            getString("toemail"));
                    emailFromEditText.setText(email.
                            getString("fromemail"));
                    messageEditText.setText(email.getString("body"));
                    subjectEditText.setText(email.getString("title"));





                    String createdAt = email.getString("created_at");
                    ZonedDateTime dateTime = Instant.parse(createdAt).atZone(ZoneId.of("Africa/Nairobi"));
                    String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
                    String formattedTime = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
                    dateEditText.setText(formattedDate);
                    timeEditText.setText(formattedTime);


                }
                else if(result.contains("errors")){
                    String message = jsonObject.getString("message");
//                    Toasts.toastIconError(IndividualDealActivity.this,message);
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                }
                else{
//                    Toasts.toastIconError(IndividualDealActivity.this,"Error Loading Account. Please Try Again");
                    Toast.makeText(getBaseContext(), "Error Loading Email. Please Try Again", Toast.LENGTH_SHORT).show();
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
//                Intent intent = new Intent(ViewEmailActivity.this, OutgoingEmailsActivity.class);
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