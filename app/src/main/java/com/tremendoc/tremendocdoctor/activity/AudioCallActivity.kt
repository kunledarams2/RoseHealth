package com.tremendoc.tremendocdoctor.activity

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
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
import com.tremendoc.tremendocdoctor.fragment.CallLogs
import com.tremendoc.tremendocdoctor.model.CallLog
import com.tremendoc.tremendocdoctor.utils.AudioPlayer
import com.tremendoc.tremendocdoctor.utils.CallConstants
import com.tremendoc.tremendocdoctor.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_audio_call.*
import kotlinx.android.synthetic.main.notedialog_note.*
import kotlinx.android.synthetic.main.notedialog_note.view.*
import java.util.*


class AudioCallActivity : BaseActivity() {

    private var mAudioPlayer: AudioPlayer? = null

    private var mCallId: String? = null
    private var mPatientName: String? = null
    private var mPatientId: String? = null
    private var mConsultationId: String? = null
    private var mCustomerType: String? = null
    private var mWritenNote = false
    private var callEnded = false
    val ONE_MINUTE = 60
    val TEN_MINUTES = ONE_MINUTE * 10

    private var mTimer: Timer? = null
    private var mDurationTask: UpdateCallDurationTask? = null
    private var currentFragment: Fragment? = null


    private inner class UpdateCallDurationTask : TimerTask() {
        override fun run() {
            this@AudioCallActivity.runOnUiThread { updateCallDuration() }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_call)

        mAudioPlayer = AudioPlayer(this)

        //val op = ImageOperation()

        mCallId = intent?.getStringExtra(CallConstants.CALL_ID)
        mPatientId = intent?.getStringExtra(CallLog.PATIENT_ID)
        mPatientName = intent?.getStringExtra(CallLog.PATIENT_NAME)
        mConsultationId = intent?.getStringExtra(CallLog.CONSULTATION_ID)
        mCustomerType = intent?.getStringExtra(CallLog.CUSTOMER_TYPE)

        initAudio(mAudioPlayer)
        initViews()

        val timerTask = object : TimerTask() {
            override fun run() {
                //Log.d("AudioCallActivity", " ---------------- WE ARE DONE HERE")
                endCall(true)
//                closeScreen()
            }
        }
        val interval: Long = 1000 * 60 * 10 //10 minute
        val timer = Timer()
        timer.schedule(timerTask, interval)
    }

    private fun initViews() {
        end_btn.setOnClickListener { endCall(true) }
        mute_btn.setOnClickListener { toggleMute(mAudioPlayer) }
        speaker_btn.setOnClickListener { toggleSpeaker(mAudioPlayer) }
        new_note_btn.setOnClickListener {
            NewNoteDialog(this, mConsultationId, mPatientId).show()
            mWritenNote=true
        }
        med_record_btn.setOnClickListener {
            MedicalRecordDialog(this, mPatientId).show()

        }
        new_prescription_btn.setOnClickListener {
            PrescriptionDialog(this, mPatientId, mConsultationId).show()
        }

        call_status.text = "Connecting ..."
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
        customerType.text = "Plan: $mCustomerType"
    }

    override fun onPause() {
        super.onPause()
        mDurationTask?.cancel()
        mTimer?.cancel()
    }

    override fun onResume() {
        super.onResume()
        mCallId = intent?.getStringExtra(CallConstants.CALL_ID)

        mTimer = Timer()
        mDurationTask = UpdateCallDurationTask()
        mTimer?.schedule(mDurationTask, 0, 500)
    }

    override fun onStop() {
        super.onStop()
        mAudioPlayer?.stopRingtone()
        mAudioPlayer?.stopProgressTone()
    }

    override fun onBackPressed() {
        //super.onBackPressed()
    }

//    fun endCall(closeScreen:Boolean){
//        mAudioPlayer?.stopProgressTone()
////        val call:Call?=sinchServiceInterface.getCall(mCallId)
////        call?.hangup()
//        if(closeScreen){
//            if(mWriteNote){
//                writeNote()
//            }else{
////                closeScreen()
//            }
//        }
//    }


    fun closeScreen() {

        IncomingCallActivity.setOnCall(this@AudioCallActivity, false)

        mAudioPlayer?.stopProgressTone()
        val call: Call? = sinchServiceInterface.getCall(mCallId)
        call?.hangup()

        val intent = Intent(this@AudioCallActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    fun endCall(closeScreen: Boolean) {
        val call = sinchServiceInterface.getCall(mCallId)
        endCall(call, closeScreen)
    }

    fun endCall(call: Call?, closeScreen: Boolean) {
        mAudioPlayer?.stopProgressTone()
        call?.hangup()
        if (closeScreen) {
            //if (call.getDetails().getDuration() >= ONE_MINUTE ) {
            if (mWritenNote) {
//                writeNote(closeScreen)
                closeScreen()
            } else {
//                sinchServiceInterface.updateConsultation(call, consultationId)

               Toast.makeText(this, "Please add doctor note to exit the screen",Toast.LENGTH_LONG).show()
            }
        } else {
//            writeNote(closeScreen)
        }
        callEnded = true
    }

    fun writeNoteDoctor(mNote:Boolean){


    }

//    private fun endCall(closeScreen: Boolean) {
//
//        writeNote(closeScreen)
//        mAudioPlayer?.stopProgressTone()
//        val call: Call? = sinchServiceInterface.getCall(mCallId)
//        call?.hangup()
//
//
//        sinchServiceInterface.setOngoing(mConsultationId,"DOCTOR_END_CALL")
//        sinchServiceInterface.updateConsultation(mConsultationId,call)
//
//
//        val v = Intent(this@AudioCallActivity, MainActivity::class.java)
//        v.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//        startActivity(v)
//        finish()
//    }

    private fun changeView(fragment: Fragment) {
        currentFragment = fragment
        val fragmentTransitionSupport = supportFragmentManager.beginTransaction()
        fragmentTransitionSupport.replace(R.id.activeView, fragment)
        fragmentTransitionSupport.commit()
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

        val mConsultationTimer = object : com.tremendoc.tremendocdoctor.utils.Timer(1000, tenMinutes, true) {

            override fun onTick(millisUntilFinished: Long) {

                val ellapsed = tenMinutes - millisUntilFinished
                val mins = ellapsed / 6000.0
                Log.d("CALL TIMER", "Ellpsed minite $mins")
            }

            override fun onFinish() {
                Log.d("CALL TIMER", "onFinished()")
//                endCall()
            }
        }
        mConsultationTimer.resume()
    }

    private inner class SinchCallListener : CallListener {

        override fun onCallEnded(call: Call?) {
            val cause: CallEndCause? = call?.details?.endCause
            log("Call ended reason: ${cause?.toString()}")

            mAudioPlayer?.stopProgressTone()
            volumeControlStream = AudioManager.USE_DEFAULT_STREAM_TYPE

            if (!callEnded) {
                val showCallbackModal: Boolean = call!!.getDetails()!!.duration < TEN_MINUTES - 5000
//                endCall(call, !showCallbackModal)
//                endCall(true)

            }


        }

        override fun onCallEstablished(p0: Call?) {
            log("Call established")
            mAudioPlayer?.stopProgressTone()
            callEnded = false
            call_status.text = "Connected"
            volumeControlStream = AudioManager.STREAM_VOICE_CALL
            val audioController: AudioController = sinchServiceInterface.audioController
            audioController.disableSpeaker()
            call_status.text = "Ongoing ..."
        }

        override fun onCallProgressing(p0: Call?) {
            mAudioPlayer?.playProgressTone()
            call_status?.text = "Ringing..."
        }

        override fun onShouldSendPushNotification(p0: Call?, p1: MutableList<PushPair>?) {

        }
    }

    private fun log(log: String?) {
        Log.e("VoiceCallActivity", "--__--_--__-----___-----__-----_--_-----   $log")
    }

    private fun writeNote(closeScreen: Boolean) {

        val dialog = Dialog(this)
        val view = dialog.layoutInflater.inflate(R.layout.notedialog_note, null)
        dialog.setTitle("Note")
        view.doneButton.isEnabled=false

        if(closeScreen){
            view.writeNote.setOnClickListener { view ->
                NewNoteDialog(this, mConsultationId, mPatientId).show()
                view.doneButton.isEnabled=true
            }

        }



        view.doneButton.setOnClickListener {
            val mintent = Intent(this, MainActivity::class.java)
            mintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(mintent)
        }

//        if (mWritenNote) {
//            ToastUtil.showLong(this, "Please write Doctor Note")
//
//
//
//        } else if(mWritenNote==true) {
//            view.doneButton.setOnClickListener {
//                val mintent = Intent(this, MainActivity::class.java)
//                mintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                startActivity(mintent)
//            }
//
//
//        }
        view.writePrescription.setOnClickListener { view ->
            PrescriptionDialog(this, mPatientId, mConsultationId).show()
        }


        dialog.setContentView(view)
        dialog.show()


    }


}
