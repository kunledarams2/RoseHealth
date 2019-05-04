package com.tremendoc.tremendocdoctor.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;


import com.tremendoc.tremendocdoctor.R;

import java.io.FileInputStream;
import java.io.IOException;

public class AudioPlayer {

    static final String LOG_TAG = AudioPlayer.class.getSimpleName();
    private Context context;
    private MediaPlayer mPlayer;
    private AudioManager manager;
    private AudioTrack progressTone;

    private final static int SAMPLE_RATE = 16000;

    public AudioPlayer(Context context) {
        this.context = context.getApplicationContext();
        manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void playRingtone() {
        //AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        //Honour silent mode
        switch (manager.getRingerMode()) {
            case AudioManager.RINGER_MODE_NORMAL:
                mPlayer = new MediaPlayer();
                mPlayer.setAudioStreamType(AudioManager.STREAM_RING);

                try {
                    mPlayer.setDataSource(context,
                            Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.phone_loud1));
                    mPlayer.prepare();
                } catch (IOException e) {
                    log("Could npt setup media player for ringtone");
                    log(e.getMessage());
                    mPlayer = null;
                    return;
                }
                mPlayer.setLooping(true);
                mPlayer.start();
                break;
        }
    }

    public void stopRingtone() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    public void playProgressTone() {
        stopProgressTone();
        try {
            progressTone = createProgressTone(context);
            progressTone.play();
        } catch (Exception e) {
            log("Could not play progress tone");
            log(e.getMessage());
        }
    }

    public void stopProgressTone() {
        if (progressTone != null) {
            progressTone.stop();
            progressTone.release();
            progressTone = null;
        }
    }

    private static AudioTrack createProgressTone(Context context) throws IOException {
        AssetFileDescriptor fd = context.getResources().openRawResourceFd(R.raw.progress_tone);
        int length = (int) fd.getLength();

        AudioTrack track = new AudioTrack(AudioManager.STREAM_VOICE_CALL, SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, length,
                AudioTrack.MODE_STATIC);

        byte[] data = new byte[length];
        readFileToBytes(fd, data);

        track.write(data, 0, data.length);
        track.setLoopPoints(0, data.length / 2, 30);
        return track;
    }

    private static void readFileToBytes(AssetFileDescriptor fd, byte[] data) throws IOException{
        FileInputStream stream = fd.createInputStream();

        int bytes = 0;
        while (bytes < data.length) {
            int res = stream.read(data, bytes, (data.length - bytes));
            if (res == -1) {
                break;
            }
            bytes += res;
        }

    }

    public boolean isMute() {
        return manager.isMicrophoneMute();
    }

    public boolean isOnSpeaker(){
        return manager.isSpeakerphoneOn();
    }

    private void log(String str) {
        Log.d(LOG_TAG, str);
    }
}
