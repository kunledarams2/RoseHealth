package com.tremendoc.tremendocdoctor.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.JsonObject;
import com.tremendoc.tremendocdoctor.api.API;
import com.tremendoc.tremendocdoctor.api.Result;
import com.tremendoc.tremendocdoctor.api.StringCall;
import com.tremendoc.tremendocdoctor.api.URLS;
import com.tremendoc.tremendocdoctor.model.NewCallLog;
import com.tremendoc.tremendocdoctor.utils.ToastUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NewCallLogRepository {

    private Context context;
    private StringCall call;
    private static  NewCallLogRepository instance;
    private static  String TAG= NewCallLogRepository.class.getSimpleName();


    public NewCallLogRepository(Context conxt) {
        this.context = conxt;
        this.call= new StringCall(conxt);
    }

    public static NewCallLogRepository getInstance(Context conxt) {

        if(instance==null){
            instance=new NewCallLogRepository(conxt);

        }
        return instance;
    }

    public LiveData<Result<NewCallLog>> getCallLog(){

        MutableLiveData<Result<NewCallLog>> data = new MutableLiveData<>();
        Result<NewCallLog> result = new Result<>();

        call.get(URLS.CALL_LOG_STATUS,null, response -> {
            logMgs( "RESPONSE" + response);

            try{
                JSONObject obj = new JSONObject(response);
                if(obj.has("code") && obj.getInt("code")==0 ){
                    List<NewCallLog> callLogList = new ArrayList<>();
                    if(!obj.isNull("consultationCallLog")){
                        JSONArray objArrays= obj.getJSONArray("consultationCallLog");

                        for(int i=0; i<objArrays.length(); i++ ){

                            NewCallLog callLog= NewCallLog.parse(objArrays.getJSONObject(i));
                            callLogList.add(callLog);


                        }
                        result.setDataList(callLogList);
//                        result.setMessage("SUCCESSFUL........ Okay");
                        logMgs(result.getDataList().get(0).getCallStatus() + " okay");

                    }
                    else {
                        result.setMessage(obj.getString("description"));
                    }


                }
                data.setValue(result);
            }catch (JSONException e){
                logMgs("JSONException ERROR" + e.getMessage() );
                result.setMessage(e.getMessage());
                data.setValue(result);
            }


        },error -> {
            if(error.networkResponse==null){
                ToastUtil.showLong(context,"Please Check Your Internet Connection....");
                result.setMessage("Please Check Your Internet Connection....");
            }
            data.setValue(result);
        });

        return data;
    }

    private void logMgs(String msg){
        Log.d(TAG, "----_____-----_______--_---______" + msg);
    }

}
