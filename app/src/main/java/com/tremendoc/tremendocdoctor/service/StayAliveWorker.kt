package com.tremendoc.tremendocdoctor.service

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.tremendoc.tremendocdoctor.activity.BaseActivity
import com.tremendoc.tremendocdoctor.activity.MainActivity
import com.tremendoc.tremendocdoctor.api.API
import com.tremendoc.tremendocdoctor.api.StringCall
import com.tremendoc.tremendocdoctor.api.URLS
import com.tremendoc.tremendocdoctor.utils.CallConstants
import com.tremendoc.tremendocdoctor.utils.Formatter
import com.tremendoc.tremendocdoctor.utils.IO
import com.tremendoc.tremendocdoctor.utils.UI
import org.json.JSONException
import org.json.JSONObject
import java.security.AccessController.getContext
import java.util.HashMap


class StayAliveWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        try{
            setOnline()
            return Result.success()
        } catch (e:Exception){

            log(e.toString())
            log(Result.failure().toString())

            return Result.failure()
        }

    }

    private fun setOnline() {
        log("StayAliveWorker is running: setOnline() $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$")

        val call = StringCall(applicationContext)
        val params = HashMap<String, String>()

        val isSetOnline = IO.getData(applicationContext, CallConstants.ONLINE_STATUS) == CallConstants.ONLINE
        params["mode"] = if (API.isLoggedIn(applicationContext) && isSetOnline) "ONLINE" else "OFFLINE"
        call.get(URLS.ONLINE_STATUS, params, false, { response ->
            try {
                val `object` = JSONObject(response)
                if (`object`.has("code") && `object`.getInt("code") == 0) {
                    log("Worker request is successful")
                    if (params["mode"] == "ONLINE") {
                        UI.notifyOnline(applicationContext)
//                        MainActivity().startSinch()

                    } else {
                        UI.clearOnlineNotification(applicationContext)
                    }
                } else {
                    log("Worker request is not successful")
                    //ToastUtil.showLong(getContext(), `object`.getString("description"))
                }
            } catch (e: JSONException) {
                log("Worker error ${e.message}")
                //log(e.localizedMessage)
            }
        }, { error ->
            //log("VOLLEY ERROR")
            //log(error.message)
            if (error.networkResponse == null) {
                log("Network response is null")
            } else {
                log("DATA: " + Formatter.bytesToString(error.networkResponse.data))
            }
        })
    }

    private fun log(log: String) {
        Log.d("StayAliveWorker: ", "log -> $log")
    }

    companion object {
        private val WORKER_1 = "tremendoc_worker_1"
        private val WORKER_2 = "tremendoc_worker_2"
    }

}