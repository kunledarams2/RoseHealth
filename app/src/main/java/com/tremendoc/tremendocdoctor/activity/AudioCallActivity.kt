package com.tremendoc.tremendocdoctor.activity

import android.content.Intent
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import com.sinch.android.rtc.AudioController
import com.sinch.android.rtc.PushPair
import com.sinch.android.rtc.calling.Call
import com.sinch.android.rtc.calling.CallEndCause
import com.sinch.android.rtc.calling.CallListener
import com.tremendoc.tremendocdoctor.R
import com.tremendoc.tremendocdoctor.dialog.MedicalRecordDialog
import com.tremendoc.tremendocdoctor.dialog.NewNoteDialog
import com.tremendoc.tremendocdoctor.dialog.NoteDialog
import com.tremendoc.tremendocdoctor.dialog.PrescriptionDialog
import com.tremendoc.tremendocdoctor.model.CallLog
import com.tremendoc.tremendocdoctor.utils.AudioPlayer
import com.tremendoc.tremendocdoctor.utils.CallConstants
import kotlinx.android.synthetic.main.activity_audio_call.*
import java.util.*



class AudioCallActivity : BaseActivity() {

    private var mAudioPlayer: AudioPlayer? = null

    private var mCallId: String? = null
    private var mPatientName: String? = null
    private var mPatientId: String?  = null
    private var mConsultationId: String? = null


    private var mTimer: Timer? = null
    private var mDurationTask: UpdateCallDurationTask? = null


    private inner class UpdateCallDurationTask : TimerTask() {
        override fun run() {
            this@AudioCallActivity.runOnUiThread { updateCallDuration() }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_call)

        mAudioPlayer = AudioPlayer(this)

        mCallId = intent?.getStringExtra(CallConstants.CALL_ID)
        mPatientId = intent?.getStringExtra(CallLog.PATIENT_ID)
        mPatientName = intent?.getStringExtra(CallLog.PATIENT_NAME)
        mConsultationId = intent?.getStringExtra(CallLog.CONSULTATION_ID)

        initAudio(mAudioPlayer)
        initViews()

        val timerTask = object: TimerTask() {
            override fun run() {
                //Log.d("AudioCallActivity", " ---------------- WE ARE DONE HERE")
                endCall()
            }
        }
        val interval:Long = 1000 * 60 * 10 //10 minute
        val timer = Timer()
        timer.schedule(timerTask, interval)
    }

    private fun initViews() {
        end_btn.setOnClickListener { endCall() }
        mute_btn.setOnClickListener { toggleMute(mAudioPlayer) }
        speaker_btn.setOnClickListener { toggleSpeaker(mAudioPlayer) }
        new_note_btn.setOnClickListener {
            NewNoteDialog(this, mConsultationId, mPatientId).show()
        }
        med_record_btn.setOnClickListener {
            MedicalRecordDialog(this, mPatientId).show()
        }
        new_prescription_btn.setOnClickListener {
            PrescriptionDialog(this, mPatientId, mConsultationId).show()
        }
    }

    private fun initAudio(mAudioPlayer: AudioPlayer?) {
        if (mAudioPlayer == null) return

        if (mAudioPlayer.isMute) {
            mute_btn.text = "Unmute"
            mute_btn.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            mute_btn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, ContextCompat.getDrawable(this, R.drawable.ic_mic_white), null, null)
        } else {
            mute_btn.text = "Mute"
            mute_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray))
            mute_btn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, ContextCompat.getDrawable(this, R.drawable.ic_mic_off_gray), null, null)
        }

        if (mAudioPlayer.isOnSpeaker) {
            speaker_btn.text = "Normal"
            speaker_btn.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            speaker_btn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, ContextCompat.getDrawable(this, R.drawable.ic_volume_up_white), null, null)
        } else {
            speaker_btn.text = "Speaker"
            speaker_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray))
            speaker_btn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, ContextCompat.getDrawable(this, R.drawable.ic_volume_down_gray), null, null)
        }
    }

    override fun onServiceConnected() {
        val call: Call? = sinchServiceInterface.getCall(mCallId)
        if (call == null) {
            finish()
            return
        }

        call.addCallListener(SinchCallListener())
        patient_name.text = mPatientName
    }

    override fun onPause() {
        super.onPause()
        mDurationTask?.cancel()
        mTimer?.cancel()
    }

    override fun onResume() {
        super.onResume()
        mCallId = intent?.getStringExtra(CallConstants.CALL_ID)

        mTimer =  Timer()
        mDurationTask = UpdateCallDurationTask()
        mTimer?.schedule(mDurationTask, 0, 500)
    }

    override fun onBackPressed() {
        //super.onBackPressed()
    }

    private fun endCall () {
        mAudioPlayer?.stopProgressTone()
        val call: Call? = sinchServiceInterface.getCall(mCallId)
        call?.hangup()

        val v = Intent(this@AudioCallActivity, MainActivity::class.java)
        v.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(v)
        finish()
    }

    private fun toggleMute(mAudioPlayer: AudioPlayer?) {
        if (mAudioPlayer != null) {
            if (mAudioPlayer.isMute) {
                sinchServiceInterface.audioController.unmute()
                mute_btn.text = "Mute"
                mute_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray))
                mute_btn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, ContextCompat.getDrawable(this, R.drawable.ic_mic_off_gray), null, null)
            } else {
                sinchServiceInterface.audioController.mute()
                mute_btn.text = "Unmute"
                mute_btn.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
                mute_btn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, ContextCompat.getDrawable(this, R.drawable.ic_mic_white), null, null)
            }

        }
    }

    private fun toggleSpeaker(mAudioPlayer: AudioPlayer?) {
        if (mAudioPlayer == null) return

        if (mAudioPlayer.isOnSpeaker) {
            sinchServiceInterface.audioController.disableSpeaker()
            speaker_btn.text = "Speaker"
            speaker_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray))
            speaker_btn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, ContextCompat.getDrawable(this, R.drawable.ic_volume_down_gray), null, null)
        } else {
            sinchServiceInterface.audioController.enableSpeaker()
            speaker_btn.text = "Normal"
            speaker_btn.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            speaker_btn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, ContextCompat.getDrawable(this, R.drawable.ic_volume_up_white), null, null)
        }
    }

    private fun formatTimespan(totalSeconds: Int?): String {
        val minutes: Int? = totalSeconds?.div(60)
        val seconds: Int? = totalSeconds?.rem(60)
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    private fun updateCallDuration() {
        val call: Call? = sinchServiceInterface.getCall(mCallId)
        timer.text = formatTimespan(call?.details?.duration)
    }

    private fun createTimer() {
        val tenMinutes: Long = 1000 * 60 * 10

        val mConsultationTimer = object: com.tremendoc.tremendocdoctor.utils.Timer(1000, tenMinutes, true) {

            override fun onTick(millisUntilFinished: Long) {

                val ellapsed = tenMinutes - millisUntilFinished
                val mins = ellapsed / 6000.0
                Log.d("CALL TIMER", "Ellpsed minite $mins")
            }
            override fun onFinish() {
                Log.d("CALL TIMER", "onFinished()")
                endCall()
            }
        }
        mConsultationTimer.resume()
    }

    private inner class SinchCallListener : CallListener{

        override fun onCallEnded(call: Call?) {
            val cause: CallEndCause? = call?.details?.endCause
            log("Call ended reason: ${cause?.toString()}")

            mAudioPlayer?.stopProgressTone()
            volumeControlStream = AudioManager.USE_DEFAULT_STREAM_TYPE

            endCall()
        }

        override fun onCallEstablished(p0: Call?) {
            log("Call established")
            mAudioPlayer?.stopProgressTone()
            volumeControlStream = AudioManager.STREAM_VOICE_CALL
            val audioController: AudioController = sinchServiceInterface.audioController
            audioController.disableSpeaker()
        }

        override fun onCallProgressing(p0: Call?) {
            mAudioPlayer?.playProgressTone()
        }

        override fun onShouldSendPushNotification(p0: Call?, p1: MutableList<PushPair>?) {

        }
    }

    private fun log(log: String?) {
        Log.e("VoiceCallActivity", "--__--_--__-----___-----__-----_--_-----   $log")
    }

}
