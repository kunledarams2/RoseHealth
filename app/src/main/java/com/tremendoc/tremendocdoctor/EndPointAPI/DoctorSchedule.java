package com.tremendoc.tremendocdoctor.EndPointAPI;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.tremendoc.tremendocdoctor.activity.MainActivity;
import com.tremendoc.tremendocdoctor.api.API;
import com.tremendoc.tremendocdoctor.api.StringCall;
import com.tremendoc.tremendocdoctor.api.URLS;
import com.tremendoc.tremendocdoctor.dialog.ClockingDialog;
import com.tremendoc.tremendocdoctor.utils.DoctorScheduleContants;
import com.tremendoc.tremendocdoctor.utils.IO;
import com.tremendoc.tremendocdoctor.utils.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DoctorSchedule {

    private Context context;
    private static String TAG = DoctorSchedule.class.getSimpleName();
    private ClockingDialog clockingDialog;
    public Boolean clockStatus = false;



    public DoctorSchedule(Context context) {
        this.context = context;
        this.clockStatus = false;

    }

    public void checkClockIn() {

        StringCall stringCall = new StringCall(context);
        Map<String, String> params = new HashMap<>();
        ClockingDialog clockingDialog = new ClockingDialog( context);

        stringCall.get(URLS.DOCTOR_CHECK_SCHEDULE, params,
                response -> {
                    log(response);

                    try {
                        JSONObject obj = new JSONObject(response);
                        if ( obj.getInt("code") == 0) {
//                            clockingDialog.show();

                        }

                        else if (obj.has("code") && obj.getInt("code") == 10  ) {
//                            ToastUtil.showModal(context, obj.getString("description") );
                            IO.setData(context, DoctorScheduleContants.NEXTCLOCKIN,obj.getString("minuteToNextClockIn"));
                        }
                        else {

                            log(obj.getString("description"));

                        }
                    } catch (JSONException e) {

                        log("error:" + e.getMessage());
                    }

                }, error -> {

                });

    }

    public void clockMeIn() {

        StringCall stringCall = new StringCall(context);
        Map<String, String> params = new HashMap<>();
        String doctorId = API.getDoctorId(context);
        params.put("doctorId", doctorId);

        stringCall.post(URLS.DOCTOR_SCHEDULE_CLOCKIN, params,
                response -> {

                    log("clockIn:  " + response);
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.has("code") && obj.getInt("code") == 0) {

                            ToastUtil.showLong(context, "Clock In Successful");

                        } else if (obj.getInt("code") == 10 ) {
                            ToastUtil.showLong(context, obj.getString("description"));

                        }

                    } catch (JSONException e) {

                    }
                },
                error -> {

                });

    }

    public void log(String mgs) {
        Log.d(TAG, "---____---__---____----___--____" + mgs);
    }
}
