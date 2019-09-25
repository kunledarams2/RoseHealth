package com.tremendoc.tremendocdoctor.activity

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import com.sinch.android.rtc.calling.Call
import com.sinch.android.rtc.calling.CallState
import com.sinch.android.rtc.video.VideoController
import com.tremendoc.tremendocdoctor.R
import com.tremendoc.tremendocdoctor.utils.AudioPlayer
import com.tremendoc.tremendocdoctor.utils.CallConstants
import kotlinx.android.synthetic.main.activity_video_call.*
import java.util.*
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.sinch.android.rtc.PushPair
import com.sinch.android.rtc.video.VideoCallListener
import com.tremendoc.tremendocdoctor.dialog.MedicalRecordDialog
import com.tremendoc.tremendocdoctor.dialog.NewNoteDialog
import com.tremendoc.tremendocdoctor.dialog.PrescriptionDialog
import com.tremendoc.tremendocdoctor.model.CallLog


class VideoCallActivity : BaseActivity() {

    private val ADDED_LISTENER = "addedListener"
    private val VIEWS_TOGGLED = "viewsToggled"

    private var mAudioPlayer: AudioPlayer? = null
    private var mTimer: Timer? = null
    private var mDurationTask: UpdateCallDurationTask? = null

    private var mCallId: String? = null
    private var mPatientId: String? = null
    private var mPatientName: String? = null
    private var mConsultationId: String? = null

    private var mAddedListener: Boolean = false
    private  var writeNote:Boolean=false
    private var mRemoteVideoViewAdded = false
    private var mLocalVideoViewAdded = false
    var mToggleVideoViewPositions = false

    private inner class UpdateCallDurationTask: TimerTask() {
        override fun run() {
            runOnUiThread { updateCallDuration() }
        }
    }


    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putBoolean(ADDED_LISTENER, mAddedListener)
        savedInstanceState.putBoolean(VIEWS_TOGGLED, mToggleVideoViewPositions)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        mAddedListener = savedInstanceState.getBoolean(ADDED_LISTENER)
        mToggleVideoViewPositions = savedInstanceState.getBoolean(VIEWS_TOGGLED)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)

        mAudioPlayer = AudioPlayer(this)

        mCallId = intent?.getStringExtra(CallConstants.CALL_ID)
        mPatientId = intent.getStringExtra(CallLog.PATIENT_ID)
        mPatientName = intent.getStringExtra(CallLog.PATIENT_NAME)
        mConsultationId = intent.getStringExtra(CallLog.CONSULTATION_ID)

        setViews()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        initAudio(mAudioPlayer)
        showButtons()

        val timerTask = object: TimerTask() {
            override fun run() {
                endCall(true)
            }
        }
        val interval:Long = 1000 * 60 * 10 //10 minute
        val timer = Timer()
        timer.schedule(timerTask, interval)
    }

    private fun setViews() {
        end_btn.setOnClickListener{ endCall(true) }
        mute_btn.setOnClickListener { toggleMute(mAudioPlayer) }
        speaker_btn.setOnClickListener { toggleSpeaker(mAudioPlayer) }
        new_note_btn.setOnClickListener {
            NewNoteDialog(this, mConsultationId, mPatientId).show()
            writeNote=true
        }
        med_record_btn.setOnClickListener {
            MedicalRecordDialog(this, mPatientId).show()
        }
        new_prescription_btn.setOnClickListener {
            PrescriptionDialog(this, mPatientId, mConsultationId).show()
        }
        remote_video_view.setOnClickListener { showButtons() }
        call_status.text = "Connecting ..."
    }

    private fun showButtons() {
        buttons_panel.visibility = View.VISIBLE
        val btnTimer = object : com.tremendoc.tremendocdoctor.utils.Timer(5000 /* 30 seconds */, 1000 /* 1 second */, false) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d("IncomingCallActivity", "TIMER TICK")
            }

            override fun onFinish() {
                buttons_panel.visibility = View.GONE
            }
        }.create()
        btnTimer.resume()
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

        if (!mAddedListener) {
            call.addCallListener(SinchCallListener())
            mAddedListener = true
        }

        //updateU()
    }

    private fun updateUI() {
        if (sinchServiceInterface == null) return

        val call: Call? = sinchServiceInterface.getCall(mCallId)
        if (call != null) {
            if (call.details.isVideoOffered) {
                showVideoViews(true, call.state == CallState.ESTABLISHED)
            }
        } else {
            showVideoViews(false, false)
        }
    }

    override fun onStop() {
        super.onStop()
        mDurationTask?.cancel()
        mTimer?.cancel()
        removeVideoViews()
    }

    override fun onStart() {
        super.onStart()
        mTimer = Timer()
        mDurationTask = UpdateCallDurationTask()
        mTimer?.schedule(mDurationTask, 0, 500)
        updateUI()
    }


    override fun onBackPressed() {
        //super.onBackPressed()
    }

    private fun endCall (closeScreen:Boolean) {
        IncomingCallActivity.setOnCall(this@VideoCallActivity, false)
        mAudioPlayer?.stopProgressTone()
        val call: Call? = sinchServiceInterface.getCall(mCallId)
        call?.hangup()
        if(closeScreen){
            if(writeNote){

                val v = Intent(this@VideoCallActivity, MainActivity::class.java)
                v.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(v)
                finish()

            }
            else{
                Toast.makeText(this@VideoCallActivity,"Please write doctor note to exit", Toast.LENGTH_LONG).show()
            }
        }




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


    private fun addLocalView() {
        if (mLocalVideoViewAdded || sinchServiceInterface == null) return

        val vc: VideoController = sinchServiceInterface.videoController
        runOnUiThread {
            local_video_view.addView(vc.localView)
            //local_video_view.setOnClickListener{ vc.toggleCaptureDevicePosition() }
            mLocalVideoViewAdded = true
            //vc.setLocalVideoZOrder(!mToggleVideoViewPositions)
        }
    }

    private fun addRemoteView() {
        if (mRemoteVideoViewAdded || sinchServiceInterface == null) return

        val vc: VideoController = sinchServiceInterface.videoController
        runOnUiThread {
            remote_video_view.addView(vc.remoteView)
            mRemoteVideoViewAdded = true
        }
    }

    private fun removeVideoViews() {
        if (sinchServiceInterface == null) return

        val vc: VideoController = sinchServiceInterface.videoController
        runOnUiThread {
            if (vc.remoteView?.parent != null) {
                val remoteParent: ViewGroup? = vc.remoteView?.parent as ViewGroup
                remoteParent?.removeView(vc.remoteView)
                mRemoteVideoViewAdded = false
            }

            if (vc.localView?.parent != null) {
                val localParent: ViewGroup? = vc.localView?.parent as ViewGroup
                localParent?.removeView(vc.localView)
                mLocalVideoViewAdded = false
            }
            //(vc.remoteView.parent as ViewGroup).removeView(vc.remoteView)
            //(vc.localView.parent as ViewGroup).removeView(vc.localView)
        }
    }

    private fun showVideoViews(local: Boolean, remote: Boolean) {
        if (sinchServiceInterface == null) return

        if (!mRemoteVideoViewAdded) {
            addRemoteView()
        }
        if (!mLocalVideoViewAdded) {
            addLocalView()
        }

        val vc = sinchServiceInterface.videoController
        runOnUiThread {
            vc.localView.visibility = if (local) View.VISIBLE else View.GONE
            vc.remoteView.visibility = if (remote) View.VISIBLE else View.GONE
        }
    }



    private inner class SinchCallListener: VideoCallListener{

        override fun onCallEnded(call: Call?) {
            mAudioPlayer?.stopProgressTone()
            volumeControlStream = AudioManager.USE_DEFAULT_STREAM_TYPE

            endCall(true)
        }

        override fun onCallEstablished(call: Call?) {
            mAudioPlayer?.stopProgressTone()
            volumeControlStream = AudioManager.STREAM_VOICE_CALL
            val audioController = sinchServiceInterface.audioController
            audioController.enableSpeaker()
            if (call != null && call.details.isVideoOffered) {
                showVideoViews(true, true)
            }
            call_status.text = "Ongoing ..."
        }

        override fun onCallProgressing(p0: Call?) {
            mAudioPlayer?.playProgressTone()
            showVideoViews(true, false)
            call_status.text = "Connecting ..."
        }

        override fun onShouldSendPushNotification(p0: Call?, p1: MutableList<PushPair>?) {

        }

        override fun onVideoTrackAdded(p0: Call?) {

        }

        override fun onVideoTrackPaused(p0: Call?) {

        }

        override fun onVideoTrackResumed(p0: Call?) {

        }
    }

    private fun log(log: String){
        Log.d("VideoCallActivity ", "log -> $log")
    }
}
