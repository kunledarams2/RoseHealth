package com.tremendoc.tremendocdoctor.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tremendoc.tremendocdoctor.R
import com.tremendoc.tremendocdoctor.utils.AudioPlayer

class IncomingVoiceCallActivity : AppCompatActivity() {

    var mAudioPlayer: AudioPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_voice_call)

        mAudioPlayer?.playRingtone()
    }
}
