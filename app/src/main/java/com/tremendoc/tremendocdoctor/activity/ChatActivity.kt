package com.tremendoc.tremendocdoctor.activity

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.tremendoc.tremendocdoctor.R
import com.tremendoc.tremendocdoctor.binder.ChatBinder
import com.tremendoc.tremendocdoctor.dialog.MedicalRecordDialog
import com.tremendoc.tremendocdoctor.dialog.NewNoteDialog
import com.tremendoc.tremendocdoctor.dialog.PrescriptionDialog
import com.tremendoc.tremendocdoctor.model.CallLog
import com.tremendoc.tremendocdoctor.model.Message
import com.tremendoc.tremendocdoctor.utils.CallConstants
import kotlinx.android.synthetic.main.activity_chat.*
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.tremendoc.tremendocdoctor.api.API
import com.tremendoc.tremendocdoctor.repository.ChatRepo
import org.json.JSONObject
import com.tremendoc.tremendocdoctor.service.ChatService
import com.tremendoc.tremendocdoctor.utils.Timer
import java.util.*


class ChatActivity : BaseActivity() {

    private var binder: ChatBinder? = null

    private var myName: String? = null
    private var mCallId: String? = null
    private var mPatientName: String? = null
    private var mPatientId: String? = null
    private var mConsultationId: String? = null
    private var mPatientToken: String? = null
    private var mDoctorToken: String? = null

    private var chatListener: MyChatListener? = null

    private var clearTimer: java.util.Timer? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        myName = API.getTitledName()
        mCallId = intent?.getStringExtra(CallConstants.CALL_ID)
        mPatientId = intent.getStringExtra(CallLog.PATIENT_ID)
        mPatientName = intent.getStringExtra(CallLog.PATIENT_NAME)
        mConsultationId = intent.getStringExtra(CallLog.CONSULTATION_ID)
        mPatientToken = intent.getStringExtra(CallLog.PATIENT_TOKEN)
        mDoctorToken = intent.getStringExtra(CallLog.DOCTOR_TOKEN)

        setAdapter()
        setViews(binder)
        initPusher(binder)

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val timerTask = object: TimerTask() {
            override fun run() {
                chatServiceInterface.endChat(mPatientToken, "hangup")
                fun close () {
                    val intent = Intent(this@ChatActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                val builder = AlertDialog.Builder(this@ChatActivity)
                builder.setTitle("Consultation time elapsed.")
                        .setMessage("The 10 minutes allocated to each consultation has elapsed.")
                        .setPositiveButton("Ok") { dialog, _ ->
                            dialog.cancel()
                            close()
                        }
                        .setOnCancelListener { close() }
                        .setOnDismissListener { close() }
                        .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                        .show()
            }
        }
        val interval:Long = 1000 * 60 * 10 //10 minute
        val timer = Timer()
        timer.schedule(timerTask, interval)
    }

    private fun setViews(binder: ChatBinder?) {
        toolbar.title = mPatientName
        send_btn.setOnClickListener{
            val text = message_field.text.toString()
            if (!TextUtils.isEmpty(text.trim())) {
                sendMessage(text.trim())
                val msg = Message()
                msg.content = text.trim()
                msg.sender = myName
                msg.type = Message.Type.SENT
                binder?.add(msg)
                if (binder != null) {
                    val pos = if ( binder.itemCount > 0) binder.itemCount else 0
                    recycler_view.smoothScrollToPosition(pos)
                    message_field.editableText.clear()
                }
            }
        }

        end_session.setOnClickListener { endSession() }
        new_note_btn.setOnClickListener {
            NewNoteDialog(this, mConsultationId, mPatientId).show()
        }
        med_record_btn.setOnClickListener {
            MedicalRecordDialog(this, mPatientId).show()
        }
        new_prescription_btn.setOnClickListener {
            PrescriptionDialog(this, mPatientId, mConsultationId).show()
        }

        val watcher = object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isTyping()
            }
            override fun afterTextChanged(s: Editable?) {
            }
        }

        message_field.addTextChangedListener(watcher)
    }

    private fun setAdapter() {
        binder = ChatBinder(this, false)
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = binder
    }

    private fun initPusher(binder: ChatBinder?) {
        val options = PusherOptions()
        options.setCluster("eu")
        val pusher = Pusher("09e2351b4321af759a47", options)
        val channel = pusher.subscribe(mConsultationId)
        channel.bind("test_event") { _, _, data ->
            runOnUiThread {
                Log.d("ChatActivity", "MESSAGE RECEIVED ----------------------------------------------- $data")
                val message = Message.dirtyParse(data)
                if (message.sender != myName) {
                    binder?.add(message)
                    if (binder != null) {
                        val pos = if (binder.itemCount == 0) 0 else binder.itemCount - 1
                        recycler_view.smoothScrollToPosition(pos)
                    }
                }
            }
        }

        channel.bind("is_typing") {
            _, _, data -> runOnUiThread {
                //Log.d("ChatActivity", "IS TYPING ----------------------------------------------- $data")
                var json = JSONObject(data)
                val string = json.getString("message")
                json = JSONObject(string)
                val user = json.getString("user")

                if (user != myName) {
                    toolbar.subtitle = "typing"

                    if (clearTimer != null) {
                        clearTimer?.cancel()
                    }
                    startClearTimer()
                }
            }
        }
        pusher.connect()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        if (chatServiceInterface != null) {
            chatListener = MyChatListener()
            chatServiceInterface.setChatListener(chatListener)
        }
    }

    override fun onBackPressed() {
        endSession()
    }

    private fun endSession() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Chat Cancellation")
                .setMessage("Do you want to end this chat session?")
                .setPositiveButton("Yes") { dialog, _ ->
                    dialog.cancel()
                    chatServiceInterface.endChat(mPatientToken, "hangup")
                    val intent = Intent(this@ChatActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .show()
    }

    private fun sendMessage(msg: String) {
        val obj = JSONObject()
        obj.put("message", msg)
        obj.put("sender", myName)
        val channel = mConsultationId
        ChatRepo.getInstance(this).sendMessage(obj.toString(), channel, "test_event", msgCallback)
    }

    private fun isTyping() {
        val obj = JSONObject()
        obj.put("user", myName)
        val channel = mConsultationId
        ChatRepo.getInstance(this).sendMessage(obj.toString(), channel, "is_typing", msgCallback)
    }

    private fun startClearTimer() {
        val timerTask = object: TimerTask() {
            override fun run() {
                Log.d("startClearTimer()", "Reset subtitle")
                runOnUiThread { toolbar.subtitle = "" }
            }
        }
        val interval:Long = 900 //0.9 seconds
        clearTimer = Timer()
        clearTimer?.schedule(timerTask, interval)
    }

    private val msgCallback: ChatRepo.MsgCallback = object : ChatRepo.MsgCallback {
        override fun onSuccess() {

        }

        override fun onError() {

        }
    }

    private inner class MyChatListener : ChatService.ChatListener {
        override fun onChatEnded(reason: String) {
            IncomingCallActivity.setOnCall(this@ChatActivity, false)

            runOnUiThread {
                if (intent != null && intent.getBooleanExtra("incoming", false)) {

                    val v = Intent(this@ChatActivity, MainActivity::class.java)
                    v.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(v)
                    finish()
                } else {
                    val intent = Intent(this@ChatActivity, MainActivity::class.java)
                    intent.putExtra("fragment", MainActivity.CALL_LOGS)
                    startActivity(intent)
                }
            }
        }

        override fun onChatEstablished() {
            Log.d("ChatActivity", "Chat established")
            chatServiceInterface.setOngoing(mConsultationId)
        }

        override fun onChatProgressing() {
            Log.d("ChatActivity", "Chat Progressing")

            //mCallStatus.setText("Connecting...");
        }

        override fun onIncomingChat() {
        }

    }


    private fun log(log: String){
        Log.d("ChatActivity", "__------ $log")
    }
}
