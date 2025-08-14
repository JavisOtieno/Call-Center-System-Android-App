package com.scg.scgcalls;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.twilio.video.Camera2Capturer;
import com.twilio.video.CameraCapturer;
import com.twilio.video.ConnectOptions;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.RemoteAudioTrack;
import com.twilio.video.RemoteAudioTrackPublication;
import com.twilio.video.RemoteDataTrack;
import com.twilio.video.RemoteDataTrackPublication;
import com.twilio.video.RemoteParticipant;
import com.twilio.video.RemoteVideoTrack;
import com.twilio.video.RemoteVideoTrackPublication;
import com.twilio.video.Room;
import com.twilio.video.TwilioException;
import com.twilio.video.Video;
import com.twilio.video.VideoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tvi.webrtc.Camera1Enumerator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class VideoRoomActivity extends AppCompatActivity {

    private static final int CAMERA_MIC_PERMISSION_REQUEST_CODE = 1;

    private VideoView localVideoView, remoteVideoView;
    private LocalVideoTrack localVideoTrack;
    private LocalAudioTrack localAudioTrack;
    private final Map<String, RemoteVideoTrack> remoteTracks =
            new LinkedHashMap<>();
    private String currentParticipantSid = null;
    private Room room;

    private Toolbar toolbar;
    private ActionBar actionBar;
    private Button joinRoomButton;
    private Button copyLinkButton;

    private String ROOM_NAME = "fygujh-20250722132458-i7irwn";
    private String IDENTITY = "user_" + new Random().nextInt(10000);

    private EditText linkEditText;
    private Button shareLinkButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_room);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Video");

        localVideoView = findViewById(R.id.local_video_view);
        remoteVideoView = findViewById(R.id.remote_video_view);
        joinRoomButton = findViewById(R.id.joinRoom);
        copyLinkButton = findViewById(R.id.copyLinkButton);
        linkEditText = findViewById(R.id.linkEditText);
        shareLinkButton = findViewById(R.id.shareLinkButton);


        Intent intent = getIntent();
        String roomName = intent.hasExtra("roomName") ? intent.getStringExtra("roomName") : null;

        if (roomName != null && !roomName.isEmpty()) {
            // Do something with roomName
            Log.d("Intent", "Received roomName: " + roomName);
            linkEditText.setEnabled(false);
            linkEditText.setText(Constants.SITE_URL+"videocall/"+roomName);
        } else {
            // Handle the case where roomName is missing
            Log.d("Intent", "No roomName provided in the Intent");
            // Optional fallback or warning
        }



        copyLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullUrl = linkEditText.getText().toString();

// Copy to clipboard
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied URL", fullUrl);
                clipboard.setPrimaryClip(clip);

// Show toast
                Toast.makeText(getApplicationContext(), "Link copied to clipboard", Toast.LENGTH_SHORT).show();


            }
        });

        shareLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullUrl = linkEditText.getText().toString();
//                String linkToShare = "https://example.com"; // Replace with your actual link

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check this out");
                shareIntent.putExtra(Intent.EXTRA_TEXT, fullUrl);

                v.getContext().startActivity(Intent.createChooser(shareIntent, "Share via"));

            }
        });


        joinRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String fullUrl = linkEditText.getText().toString();

//                fullUrl = "https://testproject.scgafrica.com/videocall/gujhkjl-20250723051619-0y56dk";
                ROOM_NAME = fullUrl.replace(Constants.SITE_URL+"videocall/", "");

                if (!hasPermissions()) {
                    ActivityCompat.requestPermissions(VideoRoomActivity.this,
                            new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO},
                            CAMERA_MIC_PERMISSION_REQUEST_CODE);
                } else {
                    try {
                        initializeVideoSDK();
                    } catch (CameraAccessException e) {
                        throw new RuntimeException(e);
                    }
                }


            }
        });
    }

    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void initializeVideoSDK() throws CameraAccessException {
        localAudioTrack = LocalAudioTrack.create(this, true);
//        CameraCapturer cameraCapturerPrev = new CameraCapturer(
//                this,
//                CameraCapturer.CameraSource.FRONT_CAMERA
//        );
//        Camera2Capturer cameraCapturerprevi = new Camera2Capturer(
//                this,
//                Camera2Capturer.CameraSource.FRONT_CAMERA,
//                camera2CapturerListener
//        );



        // 1) Obtain the CameraManager and list of camera IDs
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String frontCameraId = null;
        for (String id : manager.getCameraIdList()) {
            CameraCharacteristics chars = manager.getCameraCharacteristics(id);
            Integer facing = chars.get(CameraCharacteristics.LENS_FACING);
            if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                frontCameraId = id;
                break;
            }
        }



        String[] cameraIdList = manager.getCameraIdList();
        for (String cameraId : cameraIdList) {
            Log.d("CameraID", "Found camera: " + cameraId);
        }
// 2) Fall back if no “front” found
        if (frontCameraId == null && manager.getCameraIdList().length > 0) {

            frontCameraId = manager.getCameraIdList()[0];
        }

        frontCameraId = getFrontCameraId(this);


        if (Camera2Capturer.isSupported(this)) {
            // 3) Create the Camera2Capturer with that ID
            Camera2Capturer cameraCapturer = new Camera2Capturer(
                    this,               // your Context
                    frontCameraId,      // the cameraId String
                    new Camera2Capturer.Listener() {
                        @Override
                        public void onFirstFrameAvailable() { /* optional */ }
                        @Override
                        public void onCameraSwitched(String newCameraId) {
                            Log.i("Camera2Capturer", "Switched to: " + newCameraId);
                        }
                        @Override
                        public void onError(Camera2Capturer.Exception error) {
                            Log.e("Camera2Capturer", error.getMessage());
                        }
                    }
            );
            localVideoTrack = LocalVideoTrack.create(this, true, cameraCapturer);
//            localVideoView.setMirror(true);
//            cameraCapturer = new Camera2Capturer(context, cameraId, cameraListener);
        } else {
            CameraCapturer cameraCapturer
                    = new CameraCapturer(this, frontCameraId,
                    new CameraCapturer.Listener() {
                        @Override
                        public void onFirstFrameAvailable() {

                        }

                        @Override
                        public void onCameraSwitched(@NonNull String newCameraId) {

                        }

                        @Override
                        public void onError(int errorCode) {

                        }
                    });
            localVideoTrack = LocalVideoTrack.create(this, true, cameraCapturer);
        }
        localVideoView.setMirror(true);
        localVideoTrack.addSink(localVideoView);
        ;

        fetchAccessTokenFromServer();
    }

    private void fetchAccessTokenFromServer() {
        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        try {
            json.put("identity", IDENTITY);
            json.put("roomextended", ROOM_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        RequestBody requestBody = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(Constants.BASE_URL+"video/token")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) return;

                String body = response.body().string();
                try {
                    JSONObject responseJson = new JSONObject(body);
                    String token = responseJson.getString("token");

                    runOnUiThread(() -> connectToRoom(token));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                Intent intent = new Intent(VideoRoomActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void connectToRoom(String accessToken) {
        ConnectOptions connectOptions = new ConnectOptions.Builder(accessToken)
                .roomName(ROOM_NAME)
                .audioTracks(Collections.singletonList(localAudioTrack))
                .videoTracks(Collections.singletonList(localVideoTrack))
                .build();

        room = Video.connect(this, connectOptions, roomListener());


    }

    private void switchToParticipant(String sid) {
        if (sid.equals(currentParticipantSid) || !remoteTracks.containsKey(sid)) {
            return;
        }
        // remove old
        if (currentParticipantSid != null) {
            remoteTracks.get(currentParticipantSid).removeSink(remoteVideoView);
        }
        // add new
        remoteTracks.get(sid).addSink(remoteVideoView);
        currentParticipantSid = sid;
    }


    private Room.Listener roomListener() {
        return new Room.Listener() {
            @Override public void onConnected(@NonNull Room room) {
                for (RemoteParticipant participant : room.getRemoteParticipants()) {
                    addParticipantListeners(participant);
                }
            }

            @Override public void onConnectFailure(@NonNull Room room, @NonNull TwilioException e) {
                Log.e("Twilio", "Connection failed: " + e.getMessage());
            }

            @Override
            public void onReconnecting(@NonNull Room room, @NonNull TwilioException twilioException) {

            }

            @Override
            public void onReconnected(@NonNull Room room) {

            }

            @Override public void onDisconnected(@NonNull Room room, TwilioException e) {
                VideoRoomActivity.this.room = null;
            }

            @Override public void onParticipantConnected(@NonNull Room room, @NonNull RemoteParticipant participant) {
                addParticipantListeners(participant);
            }

            @Override public void onParticipantDisconnected(@NonNull Room room, @NonNull RemoteParticipant participant) {
                remoteVideoView.setVisibility(View.GONE);
            }

            @Override public void onRecordingStarted(@NonNull Room room) { }

            @Override public void onRecordingStopped(@NonNull Room room) { }

            // <-- This is the one we actually care about:
            @Override
            public void onDominantSpeakerChanged(@NonNull Room room, @Nullable RemoteParticipant speaker) {
                if (speaker != null) {
                    Toast.makeText(VideoRoomActivity.this, "Speaker changed: " + speaker.getSid(), Toast.LENGTH_SHORT).show();
                    switchToParticipant(speaker.getSid());
                }
            }
        };
    }

    private void addParticipantListeners(RemoteParticipant participant) {
        participant.setListener(new RemoteParticipant.Listener() {
            @Override public void onVideoTrackSubscribed(@NonNull RemoteParticipant remoteParticipant,
                                                         @NonNull RemoteVideoTrackPublication publication,
                                                         @NonNull RemoteVideoTrack track) {

                String sid = participant.getSid();
//                track.addSink(remoteVideoView);
                remoteTracks.put(remoteParticipant.getSid(), track);

                // 2) If nothing’s on screen yet, show the first one
                if (currentParticipantSid == null) {
                    currentParticipantSid = sid;
                    track.addSink(remoteVideoView);
                }

            }

            @Override
            public void onVideoTrackSubscriptionFailed(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication, @NonNull TwilioException twilioException) {

            }

            @Override
            public void onVideoTrackUnsubscribed(@NonNull RemoteParticipant remoteParticipant,
                                                 @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication,
                                                 @NonNull RemoteVideoTrack remoteVideoTrack) {
                String sid = participant.getSid();
                // stop showing if it was current
                if (sid.equals(currentParticipantSid)) {
                    remoteVideoTrack.removeSink(remoteVideoView);
                    currentParticipantSid = null;
                }
                // always remove from Map
                remoteTracks.remove(sid);
            }

            @Override
            public void onDataTrackPublished(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteDataTrackPublication remoteDataTrackPublication) {

            }

            @Override
            public void onDataTrackUnpublished(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteDataTrackPublication remoteDataTrackPublication) {

            }

            @Override
            public void onDataTrackSubscribed(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteDataTrackPublication remoteDataTrackPublication, @NonNull RemoteDataTrack remoteDataTrack) {

            }

            @Override
            public void onDataTrackSubscriptionFailed(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteDataTrackPublication remoteDataTrackPublication, @NonNull TwilioException twilioException) {

            }

            @Override
            public void onDataTrackUnsubscribed(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteDataTrackPublication remoteDataTrackPublication, @NonNull RemoteDataTrack remoteDataTrack) {

            }

            @Override
            public void onAudioTrackEnabled(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {

            }

            @Override
            public void onAudioTrackDisabled(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {

            }

            @Override
            public void onVideoTrackEnabled(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {

            }

            @Override
            public void onVideoTrackDisabled(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {

            }

            @Override
            public void onAudioTrackPublished(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {

            }

            @Override
            public void onAudioTrackUnpublished(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {

            }

            @Override public void onAudioTrackSubscribed(@NonNull RemoteParticipant remoteParticipant,
                                                         @NonNull RemoteAudioTrackPublication publication,
                                                         @NonNull RemoteAudioTrack track) {}

            @Override
            public void onAudioTrackSubscriptionFailed(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication, @NonNull TwilioException twilioException) {

            }

            @Override
            public void onAudioTrackUnsubscribed(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication, @NonNull RemoteAudioTrack remoteAudioTrack) {

            }

            @Override
            public void onVideoTrackPublished(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {

            }

            @Override
            public void onVideoTrackUnpublished(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {

            }

            // Optional: Implement other events as needed
        });
    }

    @Override
    protected void onDestroy() {
        if (room != null) {
            room.disconnect();
        }
        if (localVideoTrack != null) {
            localVideoTrack.release();
        }
        if (localAudioTrack != null) {
            localAudioTrack.release();
        }
        super.onDestroy();
    }


// …

    private String getFrontCameraId(Context context) {
        Camera1Enumerator enumerator = new Camera1Enumerator();
        for (String deviceName : enumerator.getDeviceNames()) {
            if (enumerator.isFrontFacing(deviceName)) {
                return deviceName;
            }
        }
        return null;
    }

    private String getBackCameraId(Context context) {
        Camera1Enumerator enumerator = new Camera1Enumerator();
        for (String deviceName : enumerator.getDeviceNames()) {
            if (enumerator.isBackFacing(deviceName)) {
                return deviceName;
            }
        }
        return null;
    }

}
