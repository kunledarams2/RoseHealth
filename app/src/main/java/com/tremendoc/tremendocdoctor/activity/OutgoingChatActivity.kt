package com.tremendoc.tremendocdoctor.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.tremendoc.tremendocdoctor.R
import com.tremendoc.tremendocdoctor.model.CallLog
import com.tremendoc.tremendocdoctor.service.ChatService
import com.tremendoc.tremendocdoctor.utils.AudioPlayer
import com.tremendoc.tremendocdoctor.utils.Timer
import kotlinx.android.synthetic.main.activity_outgoing_chat.*

class OutgoingChatActivity : BaseActivity() {

    private var mAudioPlayer: AudioPlayer?  = null
    private var pickupTimer: Timer? = null
    private var chatListener: MyChatListener? = null

    private var answered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_outgoing_chat)

        IncomingCallActivity.setOnCall(this, true)

        mAudioPlayer = AudioPlayer(this)
        mAudioPlayer?.playProgressTone()

        val patientToken = intent.getStringExtra(CallLog.PATIENT_TOKEN)

        pickupTimer = object : Timer(30000 /* 30 seconds */, 1000 /* 1 second */, true) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d("OutgoingChatActivity", "TIMER TICK")
            }

            override fun onFinish() {
                if (!answered) {
                    chatServiceInterface.endChat(patientToken, "no answer")
                    chatListener?.onChatEnded("no answer")
                }
            }
        }.create()
        val mPatientName = intent?.getStringExtra(CallLog.PATIENT_NAME)
        status.text = mPatientName

        end_btn.setOnClickListener {
            chatServiceInterface.endChat(patientToken, "cancelled")
            IncomingCallActivity.setOnCall(this, false)
            chatListener?.onChatEnded("cancelled")
        }

    }

    override fun onServiceConnected() {
        if (chatServiceInterface != null) {
            chatListener = MyChatListener()
            chatServiceInterface.setChatListener(chatListener)
        }
    }

    private inner class MyChatListener: ChatService.ChatListener {
        override fun onChatEnded(reason: String?) {
            IncomingCallActivity.setOnCall(this@OutgoingChatActivity, false)
            mAudioPlayer?.stopProgressTone()

            val v = Intent(this@OutgoingChatActivity, MainActivity::class.java)
            v.putExtra("fragment", MainActivity.CALL_LOGS)
            v.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(v)
            finish()
        }

        override fun onChatEstablished() {
            answered = true
            mAudioPlayer?.stopProgressTone()

            val screen = Intent(this@OutgoingChatActivity, ChatActivity::class.java)
            val bundle = intent?.extras
            if (bundle != null) {
                screen.putExtras(bundle)
            }
            startActivity(screen)
        }

        override fun onChatProgressing() {

        }

        override fun onIncomingChat() {

        }
    }

}
