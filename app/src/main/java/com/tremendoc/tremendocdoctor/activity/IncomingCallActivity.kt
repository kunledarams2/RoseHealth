package com.tremendoc.tremendocdoctor.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.sinch.android.rtc.PushPair
import com.sinch.android.rtc.calling.Call
import com.sinch.android.rtc.calling.CallListener
import com.tremendoc.tremendocdoctor.R
import com.tremendoc.tremendocdoctor.model.CallLog
import com.tremendoc.tremendocdoctor.service.ChatService
import com.tremendoc.tremendocdoctor.utils.AudioPlayer
import com.tremendoc.tremendocdoctor.utils.CallConstants
import com.tremendoc.tremendocdoctor.utils.Timer
import kotlinx.android.synthetic.main.activity_incoming_call.*
import org.joda.time.DateTime
import com.tremendoc.tremendocdoctor.utils.UI



class IncomingCallActivity : BaseActivity() {

    private var mAudioPlayer: AudioPlayer? = null
    private var pickupTimer: Timer? = null
    private var chatListener: MyChatListener? = null

    private var mCallId: String? = null
    private var mCallType: String? = null
    private var mPatientName: String? = null
    private var consultationId: String? = null
    private var mPatientToken: String? = null

    private var answered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_call)
        setupViews()

        mAudioPlayer = AudioPlayer(this)
        mAudioPlayer?.playRingtone()
        mCallId = intent?.getStringExtra(CallConstants.CALL_ID)
        mCallType = intent?.getStringExtra(CallLog.CALL_TYPE)
        mPatientName = intent?.getStringExtra(CallLog.PATIENT_NAME)
        consultationId = intent?.getStringExtra(CallLog.CONSULTATION_ID)
        mPatientToken = intent?.getStringExtra(CallLog.PATIENT_TOKEN)
    }

    override fun onResume() {
        super.onResume()

        mCallId = intent?.getStringExtra(CallConstants.CALL_ID)
    }

    override fun onServiceConnected() {
        if (mCallType == "VIDEO" || mCallType == "AUDIO") {
            val call : Call? = sinchServiceInterface.getCall(mCallId)
            if (call == null) {
                finish()
                return
            }

            call.addCallListener(SinchCallListener())
            label.text = "Incoming ${mCallType?.toLowerCase()} call from $mPatientName"
        } else if (mCallType == "CHAT") {
            chatListener = MyChatListener()
            chatServiceInterface?.setChatListener(chatListener)
            label.text = "Incoming chat from $mPatientName"

            pickupTimer = object : Timer(30000 /* 30 seconds */, 1000 /* 1 second */, true) {
                override fun onTick(millisUntilFinished: Long) {
                    //Log.d("IncomingCallActivity", "TIMER TICK")
                }

                override fun onFinish() {
                    if (!answered)
                        chatListener?.onChatEnded("no answer")
                }
            }.create()
        }
    }

    private fun setupViews() {
        accept_btn.setOnClickListener { pickup()  }
        reject_btn.setOnClickListener { decline() }
    }

    private fun pickup() {
        mAudioPlayer?.stopRingtone()

        if (mCallType == "VIDEO" || mCallType == "AUDIO") {
            val call: Call? = sinchServiceInterface.getCall(mCallId)
            if (call != null) {
                call.answer()
                sinchServiceInterface.setOngoing(consultationId)

                var callScreen: Intent? = null
                when (mCallType) {
                    "AUDIO" -> callScreen = Intent(this, AudioCallActivity::class.java)
                    "VIDEO" -> callScreen = Intent(this, VideoCallActivity::class.java)
                }
                if (intent?.extras != null) {
                    val bundle = intent?.extras
                    callScreen?.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    if (bundle != null) {
                        callScreen?.putExtras(bundle)
                    }
                }
                startActivity(callScreen)
            } else {
                finish()
            }
        } else if (mCallType == "CHAT") {
            answered = true
            chatServiceInterface.acceptRequest(mPatientToken)
            val screen = Intent(this, ChatActivity::class.java)
            screen.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            val bundle = intent?.extras
            if (bundle != null) {
                screen.putExtras(bundle)
            }
            startActivity(screen)
            //finish()
        }
    }

    private fun decline() {
        mAudioPlayer?.stopRingtone()
        if (mCallType == "VIDEO" || mCallType == "AUDIO") {
            val call = sinchServiceInterface.getCall(mCallId)
            call?.hangup()
        } else if (mCallType == "CHAT") {
            chatServiceInterface.endChat(mPatientToken, "rejected")
        }

        logCall()
        finish()
    }

    private fun logCall() {
        UI.createNotification(applicationContext, mPatientName)
        try {
            val time = DateTime.now().toString()
            val uuid = intent?.getStringExtra(CallLog.PATIENT_UUID)
            val callType = intent?.getStringExtra(CallLog.CALL_TYPE)
            val patientId = intent?.getStringExtra(CallLog.PATIENT_ID)
            val patientToken = intent?.getStringExtra(CallLog.PATIENT_TOKEN)
            val doctorToken = intent?.getStringExtra(CallLog.DOCTOR_TOKEN)

            val callLog = CallLog(this@IncomingCallActivity)
            callLog.set(CallLog.TIME, time)
            callLog.set(CallLog.PATIENT_ID, patientId)
            callLog.set(CallLog.CALL_TYPE, callType)
            callLog.set(CallLog.PATIENT_NAME, mPatientName)
            callLog.set(CallLog.PATIENT_UUID, uuid)
            callLog.set(CallLog.DOCTOR_TOKEN, doctorToken)
            callLog.set(CallLog.PATIENT_TOKEN, patientToken)
            callLog.set(CallLog.CONSULTATION_ID, consultationId)
            callLog.save()
        } catch (e: Exception) {
            Log.d("IncomingCallActivity","ERROR CREATING CALL LOG " + e.message)
        }


    }

    private inner class SinchCallListener: CallListener {

        override fun onCallEnded(call: Call?) {
            val cause = call?.details?.endCause
            Log.d("IncomingCallActivity", "Call ended cause: ${cause?.toString()}")
            mAudioPlayer?.stopRingtone()

            logCall()

            val v = Intent(this@IncomingCallActivity, MainActivity::class.java)
            v.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(v)
            finish()
        }

        override fun onCallEstablished(call: Call?) {
            Log.d("IncomingCallActivity", "Call established")
        }

        override fun onCallProgressing(call: Call?) {
            Log.d("IncomingCallActivity", "Call progressing")
        }

        override fun onShouldSendPushNotification(p0: Call?, p1: MutableList<PushPair>?) {
        }
    }

    private inner class MyChatListener: ChatService.ChatListener {
        override fun onChatEnded(reason: String?) {
            mAudioPlayer?.stopRingtone()

            logCall()

            val v = Intent(this@IncomingCallActivity, MainActivity::class.java)
            v.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(v)
            finish()
        }

        override fun onChatEstablished() {

        }

        override fun onChatProgressing() {

        }

        override fun onIncomingChat() {

        }

        override fun onTyping(isTyping: Boolean) {

        }
    }
}
