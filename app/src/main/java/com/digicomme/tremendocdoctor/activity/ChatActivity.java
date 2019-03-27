package com.digicomme.tremendocdoctor.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.digicomme.tremendocdoctor.model.Message;
import com.digicomme.tremendocdoctor.service.CallService;
import com.digicomme.tremendocdoctor.service.ChatService;
import com.digicomme.tremendocdoctor.utils.AudioPlayer;
import com.digicomme.tremendocdoctor.utils.IO;
import com.sinch.android.rtc.AudioController;

import java.util.ArrayList;

public class ChatActivity extends BaseActivity implements View.OnClickListener {

    private View incomingView, activeView;
    private TextView label, endSession;
    private Button acceptBtn, rejectBtn, viewBtn;

    private MedicalRecordDialog recordDialog;
    private String mCallId;
    private AudioPlayer mAudioPlayer;

    private ChatBinder binder;
    private RecyclerView recyclerView;
    private LinearLayoutManager manager;

    private EditText messageField;
    private ImageButton sendBtn;
    private String patientName, myName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        myName = API.getFullName();
        patientName = IO.getData(this, CallService.PATIENT_NAME);

        mAudioPlayer = new AudioPlayer(this);
        mAudioPlayer.playRingtone();

        setViews();
        setupAdapter();

        log("onCreate()");
    }

    private void setupAdapter() {
        recyclerView = findViewById(R.id.recycler_view);
        binder = new ChatBinder(this, false);
        manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(binder);
        populate();
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
        endSession = findViewById(R.id.end_session);
        viewBtn.setOnClickListener(this);
        acceptBtn.setOnClickListener(this);
        rejectBtn.setOnClickListener(this);

        messageField = findViewById(R.id.message_field);
        sendBtn = findViewById(R.id.send_btn);

        endSession.setOnClickListener(btn -> endSession());
        sendBtn.setOnClickListener(btn -> {
            String msg = messageField.getText().toString();
            String receiverId = IO.getData(this, CallService.PATIENT_ID);
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

        incomingView.setVisibility(View.VISIBLE);
        activeView.setVisibility(View.GONE);
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
        } else if (view == rejectBtn) {
            getWebSocketInterface().endChat("denied");
        } else if (view == viewBtn) {
            if (recordDialog == null) {
                recordDialog = new MedicalRecordDialog(this, mCallId);
            }
            recordDialog.show();
        }
    }

    private class MyChatListener implements ChatService.ChatListener {
        @Override
        public void onChatEnded() {
            //log("Call ended. Reason: " + cause.toString());
            mAudioPlayer.stopProgressTone();
            mAudioPlayer.stopRingtone();
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            //String endMsg = "Call ended: " + call.getDetails().toString();
            //ToastUtil.showLong(ContactActivity.this, endMsg);

            //boolean showCallbackModal = call.getDetails().getDuration() < FIFTEEN_MINUTES;
            // endCall(call, !showCallbackModal);
            //closeScreen();
            runOnUiThread(() -> finish());
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
