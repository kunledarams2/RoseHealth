package com.digicomme.tremendocdoctor.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

import com.digicomme.tremendocdoctor.R;
import com.digicomme.tremendocdoctor.api.API;
import com.digicomme.tremendocdoctor.binder.ChatBinder;
import com.digicomme.tremendocdoctor.dialog.MedicalRecordDialog;
import com.digicomme.tremendocdoctor.dialog.NewNoteDialog;
import com.digicomme.tremendocdoctor.model.CallLog;
import com.digicomme.tremendocdoctor.model.Message;
import com.digicomme.tremendocdoctor.service.CallService;
import com.digicomme.tremendocdoctor.service.ChatService;
import com.digicomme.tremendocdoctor.service.FcmListenerService;
import com.digicomme.tremendocdoctor.utils.AudioPlayer;
import com.digicomme.tremendocdoctor.utils.CallConstants;
import com.digicomme.tremendocdoctor.utils.IO;
import com.digicomme.tremendocdoctor.utils.UI;
import com.sinch.android.rtc.AudioController;

import org.joda.time.DateTime;

import java.util.ArrayList;
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

    private boolean answered = false;

    private com.digicomme.tremendocdoctor.utils.Timer respTimer;

    private MyChatListener chatListener = new MyChatListener();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        myName = API.getFullName();
        patientId = IO.getData(this, CallConstants.PATIENT_ID);
        patientName = IO.getData(this, CallConstants.PATIENT_NAME);
        consultationId = IO.getData(this, CallConstants.CONSULTATION_ID);
        mAudioPlayer = new AudioPlayer(this);
        mAudioPlayer.playRingtone();
        toolbar = findViewById(R.id.toolbar);

        setViews();
        setupAdapter();

        log("onCreate()");

        respTimer = new com.digicomme.tremendocdoctor.utils.Timer(30000 /* 30 seconds */, 1000 /* 1 second */, false) {
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
        if (getWebSocketInterface() != null)
        getWebSocketInterface().setChatListener(new MyChatListener());
    }

    private void setViews() {
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
                    getWebSocketInterface().setTyping(true);
                }

                timer.cancel();
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        isTyping = false;
                        getWebSocketInterface().setTyping(false);
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
            String receiverId = IO.getData(this, CallConstants.PATIENT_UUID);
            if (!TextUtils.isEmpty(msg)) {
                getWebSocketInterface().send(receiverId, msg);
                Message message = new Message();
                message.setContent(msg);
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
            incomingView.setVisibility(View.GONE);
            activeView.setVisibility(View.VISIBLE);
            answered = true;
        } else {
            incomingView.setVisibility(View.VISIBLE);
            activeView.setVisibility(View.GONE);
        }
        label.setText("Incoming chat from " + patientName);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        log("onPostCreate()");
        if (getWebSocketInterface() != null) {
            getWebSocketInterface().setChatListener(new MyChatListener());
        }
    }

    @Override
    public void onClick(View view) {
        if (view == acceptBtn) {
            //incomingView.setVisibility(View.GONE);
            //activeView.setVisibility(View.VISIBLE);
            getWebSocketInterface().acceptChat();
            answered = true;
        } else if (view == rejectBtn) {
            getWebSocketInterface().endChat("denied");
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
        @Override
        public void onChatEnded(String reason) {
            if (!answered) {
                UI.createNotification(getApplicationContext(), patientName);
                try {
                    String time = DateTime.now().toString();
                    int pId = Integer.parseInt(patientId);
                    CallLog.createCallLog(ChatActivity.this, patientName, pId, "CHAT", time);
                } catch (Exception e) {
                    log("error creating call log "+ e.getMessage());
                }
            }
            //log("Call ended. Reason: " + cause.toString());
            mAudioPlayer.stopProgressTone();
            mAudioPlayer.stopRingtone();
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);

            //String endMsg = "Call ended: " + call.getDetails().toString();
            //ToastUtil.showLong(ContactActivity.this, endMsg);

            //boolean showCallbackModal = call.getDetails().getDuration() < FIFTEEN_MINUTES;
            // endCall(call, !showCallbackModal);
            //closeScreen();
            runOnUiThread(() -> {
                if (CallConstants.CALL_DIRECTION_INCOMING.equals(IO.getData(ChatActivity.this, CallConstants.CALL_DIRECTION_INCOMING))) {
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
            mAudioPlayer.stopRingtone();
            //mAudioPlayer.stopProgressTone();
            //mCallStatus.setText("Ongoing...");
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            runOnUiThread(() -> {
                incomingView.setVisibility(View.GONE);
                activeView.setVisibility(View.VISIBLE);
            });
        }

        @Override
        public void onChatProgressing() {
            Log.d("CahtActivity","Chat Progressing");
            mAudioPlayer.playProgressTone();

            //mCallStatus.setText("Connecting...");
        }

        @Override
        public void onNewMessage(String msg) {

            runOnUiThread(() -> {
                Message message = new Message();
                message.setContent(msg);
                message.setSender(patientName);
                message.setType(Message.Type.INCOMING);
                binder.add(message);
                int pos = binder.getItemCount() == 0 ? 0 : binder.getItemCount() - 1;
                recyclerView.smoothScrollToPosition(pos);
            });
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
                    getWebSocketInterface().endChat("hangup");
                    Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", (dialog, i) -> dialog.dismiss())
                .show();
    }
}
