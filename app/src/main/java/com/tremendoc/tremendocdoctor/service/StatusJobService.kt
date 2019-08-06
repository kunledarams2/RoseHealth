package com.tremendoc.tremendocdoctor.service

import android.app.Service
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.tremendoc.tremendocdoctor.activity.IncomingCallActivity
import com.tremendoc.tremendocdoctor.api.API
import com.tremendoc.tremendocdoctor.api.StringCall
import com.tremendoc.tremendocdoctor.api.URLS
import com.tremendoc.tremendocdoctor.utils.CallConstants
import com.tremendoc.tremendocdoctor.utils.Formatter
import com.tremendoc.tremendocdoctor.utils.IO
import com.tremendoc.tremendocdoctor.utils.ToastUtil
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

class StatusJobService : JobService() {


    override fun onStartJob(params: JobParameters?): Boolean {
        log("StatusJobService is running: onStartJob() ")
        val isSetOnline = IO.getData(this, CallConstants.ONLINE_STATUS) == CallConstants.ONLINE
        val isOnCall = IncomingCallActivity.getCallStatus(this)
        if (isSetOnline && !isOnCall) {
            log("StatusJobService is running: onStartJob() -> setOnline() ")
            setOnline()
        }
        jobFinished(params, isOnCall)
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        log("StatusJobService is running: onStopJob()")
        return IncomingCallActivity.getCallStatus(this)
    }

    private fun setOnline() {
        log("StatusJobService is running: setOnline() $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$")
        val call = StringCall(this)
        val params = HashMap<String, String>()
        params["mode"] = if (API.isLoggedIn(this)) "ONLINE" else "OFFLINE"
        call.get(URLS.ONLINE_STATUS, params, false, { response ->
            try {
                val `object` = JSONObject(response)
                if (`object`.has("code") && `object`.getInt("code") == 0) {
                    //ToastUtil.showLong(getContext(), "You are now online")
                    /*if (status) {
                        setOnline()
                    } else {
                        setOffline()
                    }*/
                } else {
                    //ToastUtil.showLong(getContext(), `object`.getString("description"))
                }
            } catch (e: JSONException) {
                //log(e.localizedMessage)
            }
        }, { error ->
            //log("VOLLEY ERROR")
            //log(error.message)
            if (error.networkResponse == null) {
                //log("Network response is null")
            } else {
                //log("DATA: " + Formatter.bytesToString(error.networkResponse.data))
            }
        })
    }


    private fun log(log: String) {
        Log.d("StatusJobService: ", "log -> $log")
    }
}
