package com.tremendoc.tremendocdoctor.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.api.API;
import com.tremendoc.tremendocdoctor.binder.ChatBinder;
import com.tremendoc.tremendocdoctor.dialog.MedicalRecordDialog;
import com.tremendoc.tremendocdoctor.dialog.NewNoteDialog;
import com.tremendoc.tremendocdoctor.model.CallLog;
import com.tremendoc.tremendocdoctor.model.Message;
import com.tremendoc.tremendocdoctor.repository.ChatRepo;
import com.tremendoc.tremendocdoctor.service.ChatService;
import com.tremendoc.tremendocdoctor.service.OneSignalService;
import com.tremendoc.tremendocdoctor.utils.AudioPlayer;
import com.tremendoc.tremendocdoctor.utils.CallConstants;
import com.tremendoc.tremendocdoctor.utils.DeviceName;
import com.tremendoc.tremendocdoctor.utils.IO;
import com.tremendoc.tremendocdoctor.utils.UI;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.sinch.android.rtc.AudioController;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class ChatActivity extends BaseActivity implements View.OnClickListener {

    private View incomingView, activeView;
    private TextView label, endSession;
    private Button acceptBtn, rejectBtn, viewBtn, noteBtn;

    private NewNoteDialog noteDialog;
    private MedicalRecordDialog recordDialog;
    private AudioPlayer mAudioPlayer;

    private ChatBinder binder;
    private RecyclerView recyclerView;
    private LinearLayoutManager manager;
    private Toolbar toolbar;

    private EditText messageField;
    private ImageButton sendBtn;
    private String patientName, myName, patientId, consultationId;
    private String doctorToken, patientToken;

    private boolean answered = false;

    private com.tremendoc.tremendocdoctor.utils.Timer respTimer;

    private MyChatListener chatListener = new MyChatListener();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        myName = API.getFullName();
        patientId = getIntent().getStringExtra(CallLog.PATIENT_ID);
        patientName = getIntent().getStringExtra(CallLog.PATIENT_NAME);
        consultationId = getIntent().getStringExtra(CallLog.CONSULTATION_ID);
        mAudioPlayer = new AudioPlayer(this);

        patientToken = getIntent().getStringExtra(CallLog.PATIENT_TOKEN);
        doctorToken = getIntent().getStringExtra(CallLog.DOCTOR_TOKEN);

        setViews();
        setupAdapter();

        log("onCreate()");

        respTimer = new com.tremendoc.tremendocdoctor.utils.Timer(30000 /* 30 seconds */, 1000 /* 1 second */, false) {
            @Override
            public void onTick(long millisUntilFinished) {
                log("TIMER TICK");
            }

            @Override
            public void onFinish() {
                if (!answered)
                    chatListener.onChatEnded("no answer");
            }
        }.create();

        //setupPusher(DeviceName.getUUID(this));
        setupPusher();
    }

    private void setupAdapter() {
        recyclerView = findViewById(R.id.recycler_view);
        binder = new ChatBinder(this, false);
        manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(binder);
        //populate();
    }

    private void populate() {
        String[] strings = {
                "Hello guys! I need your help...",
                "... on a consultation", "Hello Doctor",
                "I have not feeling well lately", "Me too I need someone",
                "How are we doing today?", "How are we doing today?"};
        String[] doctors = {"Dr John", "Dr Mark", "Me", "Me", "Dr Francis", "Dr Hinn", "Dr Ben"};
        for (int i = 0; i < strings.length; i++) {
            Message message = new Message();
            message.setContent(strings[i]);
            message.setSender(doctors[i]);
            if (doctors[i].equals("Me")) {
                message.setType(Message.Type.SENT);
            }
            else {
                message.setType(Message.Type.INCOMING);
            }

            binder.add(message);
        }
        int pos = binder.getItemCount() == 0 ? 0 : binder.getItemCount() - 1;
        recyclerView.smoothScrollToPosition(pos);
    }

    @Override
    protected void onServiceConnected() {
        log("onServiceConnected()");
        if (getChatServiceInterface() != null)
            getChatServiceInterface().setChatListener(chatListener);

    }

    private void setViews() {
        toolbar = findViewById(R.id.toolbar);

        label = findViewById(R.id.label);
        incomingView = findViewById(R.id.incoming);
        activeView = findViewById(R.id.activeView);
        acceptBtn = findViewById(R.id.accept_btn);
        rejectBtn = findViewById(R.id.reject_btn);
        viewBtn = findViewById(R.id.view_btn);
        noteBtn = findViewById(R.id.write_note);
        endSession = findViewById(R.id.end_session);
        viewBtn.setOnClickListener(this);
        acceptBtn.setOnClickListener(this);
        rejectBtn.setOnClickListener(this);
        noteBtn.setOnClickListener(this);

        messageField = findViewById(R.id.message_field);
        sendBtn = findViewById(R.id.send_btn);

        messageField.addTextChangedListener(new TextWatcher() {
            boolean isTyping = false;
            Timer timer = new Timer();

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!isTyping) {
                    isTyping = true;
                    //getOneSignalInterface().setTyping(true);
                }

                timer.cancel();
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        isTyping = false;
                        //getWebSocketInterface().setTyping(false);
                    }
                }, 5000);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        endSession.setOnClickListener(btn -> endSession());
        sendBtn.setOnClickListener(btn -> {
            String msg = messageField.getText().toString();
            if (!TextUtils.isEmpty(msg) && !TextUtils.isEmpty(msg.trim())) {
                sendMessage( msg.trim());
                Message message = new Message();
                message.setContent(msg.trim());
                message.setSender(myName);
                message.setType(Message.Type.SENT);
                binder.add(message);
                int pos = binder.getItemCount() == 0 ? 0 : binder.getItemCount() - 1;
                recyclerView.smoothScrollToPosition(pos);
                messageField.setText("");
            }
        });

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("status")) {
            label.setText("Connecting...");
            rejectBtn.setText("Cancel");
            acceptBtn.setVisibility(View.GONE);
            answered = true;
        } else {
            incomingView.setVisibility(View.VISIBLE);
            activeView.setVisibility(View.GONE);
            label.setText("Incoming chat from " + patientName);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        log("onPostCreate()");
        if (getChatServiceInterface() != null) {
            getChatServiceInterface().setChatListener(chatListener);
        }
        if (getIntent().getBooleanExtra("incoming", false)) {
            mAudioPlayer.playRingtone();
        }
    }

    @Override
    public void onClick(View view) {
        if (view == acceptBtn) {
            //incomingView.setVisibility(View.GONE);
            //activeView.setVisibility(View.VISIBLE);
            getChatServiceInterface().acceptRequest(patientToken);
            answered = true;
        } else if (view == rejectBtn) {
            getChatServiceInterface().endChat(patientToken, "denied");
            //Intent intent = new Intent(ChatActivity.this, MainActivity.class);
            //startActivity(intent);
            //finish();
        } else if (view == viewBtn) {
            if (recordDialog == null) {
                recordDialog = new MedicalRecordDialog(this, patientId);
            }
            recordDialog.show();
        } else if (view == noteBtn) {
            if (noteDialog == null) {
                noteDialog = new NewNoteDialog(this, consultationId, patientId);
            }
            noteDialog.show();
        }
    }

    private class MyChatListener implements ChatService.ChatListener {
        private boolean createdLog = false;
        @Override
        public void onChatEnded(String reason) {
            mAudioPlayer.stopProgressTone();
            mAudioPlayer.stopRingtone();
            //setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);

            if (!answered && !createdLog && getIntent() != null && getIntent().getBooleanExtra("incoming", false)) {
                log("CREATE CALL LOG");
                UI.createNotification(getApplicationContext(), patientName);
                try {
                    String time = DateTime.now().toString();
                    String uuid =  getIntent().getExtras().getString(CallLog.PATIENT_UUID);
                    String patientToken = getIntent().getStringExtra(CallLog.PATIENT_TOKEN);
                    String doctorToken = getIntent().getStringExtra(CallLog.DOCTOR_TOKEN);

                    CallLog callLog = new CallLog(ChatActivity.this);
                    callLog.set(CallLog.TIME, time);
                    callLog.set(CallLog.PATIENT_ID, patientId);
                    callLog.set(CallLog.CALL_TYPE, "CHAT");
                    callLog.set(CallLog.PATIENT_NAME, patientName);
                    callLog.set(CallLog.PATIENT_UUID, uuid);
                    callLog.set(CallLog.DOCTOR_TOKEN, doctorToken);
                    callLog.set(CallLog.PATIENT_TOKEN, patientToken);
                    callLog.set(CallLog.CONSULTATION_ID, consultationId);
                    callLog.save();
                    createdLog = true;
                } catch (Exception e) {
                    log("ERROR CREATING CALL LOG "+ e.getMessage());
                }
            }
            //log("Call ended. Reason: " + cause.toString());

            runOnUiThread(() -> {
                if (getIntent() != null && getIntent().getBooleanExtra("incoming", false)) {
                    finish();
                } else {
                    Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                    intent.putExtra("fragment", MainActivity.CALL_LOGS);
                    startActivity(intent);
                }
            });
        }

        @Override
        public void onChatEstablished() {
            Log.d("ChatActivity", "Chat established");
            answered = true;
            mAudioPlayer.stopRingtone();
            mAudioPlayer.stopProgressTone();
            //mCallStatus.setText("Ongoing...");
            //setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            runOnUiThread(() -> {
                incomingView.setVisibility(View.GONE);
                activeView.setVisibility(View.VISIBLE);
                respTimer.resume();
            });

            getChatServiceInterface().setOngoing(consultationId);
        }

        @Override
        public void onChatProgressing() {
            Log.d("CahtActivity","Chat Progressing");
            mAudioPlayer.playProgressTone();

            //mCallStatus.setText("Connecting...");
        }

        @Override
        public void onIncomingChat() {
            mAudioPlayer.playRingtone();
        }

        @Override
        public void onTyping(boolean typing) {
            runOnUiThread(()-> {
                //if (typing)
                //    toolbar.setSubtitle("typing...");
                //else
                //    toolbar.setSubtitle("");
            });
        }

    }

    private void log(String log) {
        Log.d("ChatActivity", "--__--_--__-----___-----__-----_--_-----   " + log);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        endSession();
    }

    private void endSession() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chat Cancellation")
                .setMessage("Do you want to end this chat session")
                .setPositiveButton("Yes", (dialog, i) -> {
                    getChatServiceInterface().endChat(patientToken, "hangup");
                    Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", (dialog, i) -> dialog.dismiss())
                .show();
    }

    private void setupPusher() {
        PusherOptions options = new PusherOptions();
        options.setCluster("eu");
        Pusher pusher = new Pusher("09e2351b4321af759a47", options);
        Channel channel = pusher.subscribe(consultationId);
        channel.bind("test_event", (channelName1, eventName, data) -> runOnUiThread(() -> {
            Message message = Message.dirtyParse(data);
            if (!message.getSender().equals(myName)) {
                binder.add(message);
                int pos = binder.getItemCount() == 0 ? 0 : binder.getItemCount() - 1;
                recyclerView.smoothScrollToPosition(pos);
            }
        }));
        pusher.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAudioPlayer.stopRingtone();
        mAudioPlayer.stopProgressTone();
    }

    ChatRepo.MsgCallback msgCallback = new ChatRepo.MsgCallback() {
        @Override
        public void onSuccess() {

        }

        @Override
        public void onError() {

        }
    };

    public void sendMessage(String msg) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("message", msg);
            obj.put("sender", myName);
            String channel = consultationId;
            ChatRepo.getInstance(this).sendMessage(obj.toString(), channel, msgCallback);
        } catch (JSONException e) {

        }
    }

}
