package com.tremendoc.tremendocdoctor.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.api.API;
import com.tremendoc.tremendocdoctor.api.StringCall;
import com.tremendoc.tremendocdoctor.api.URLS;
import com.tremendoc.tremendocdoctor.callback.DoctorScheduleListener;
import com.tremendoc.tremendocdoctor.model.DoctorClocking;
import com.tremendoc.tremendocdoctor.utils.DoctorScheduleContants;
import com.tremendoc.tremendocdoctor.utils.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class DoctorScheduleDialog extends AppCompatDialogFragment {

    private Switch aSwitch;
    private TextView mDescription;
    private Button buttonClose;
    //        return super.onCreateDialog(savedInstanceState);
    private DoctorScheduleListener scheduleListener;
    private Bundle bundle;
    private static String TAG = DoctorClocking.class.getSimpleName();

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_clocking_dialog, null);
        builder.setView(view)
                .setTitle("Clock In");
        aSwitch = view.findViewById(R.id.switchClockIn);
        mDescription = view.findViewById(R.id.tvDescription);
        buttonClose = view.findViewById(R.id.closeDialog);
        bundle = new Bundle();

        scheduleListener = (DoctorScheduleListener) view.getContext();
        aSwitch.setChecked(false);
        aSwitch.setOnClickListener(view1 -> {
            clockMeIn();

        });

        buttonClose.setOnClickListener(view1 -> {
//            Toast.makeText(getActivity(), bundle.getString(DoctorScheduleContants.NEXTCLOCKIN),Toast.LENGTH_LONG).show();
                    scheduleListener.getClockIntime(bundle.getString(DoctorScheduleContants.NEXTCLOCKIN));
                    dismiss();
                }
        );

        return builder.create();

    }


    private void clockMeIn() {

        StringCall stringCall = new StringCall(getActivity());
        Map<String, String> params = new HashMap<>();
        String doctorId = API.getDoctorId(getActivity());
        params.put("doctorId", doctorId);

        stringCall.post(URLS.DOCTOR_SCHEDULE_CLOCKIN, params,
                response -> {

                    log("clockIn:  " + response);
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.has("code") && obj.getInt("code") == 0) {
                            mDescription.setText(obj.getString("description"));
                            bundle.putString(DoctorScheduleContants.NEXTCLOCKIN,obj.getString("minuteToNextClockIn"));
                            aSwitch.setChecked(true);
                            aSwitch.setEnabled(false);

                        } else {

                            mDescription.setText(obj.getString("description"));
                            aSwitch.setChecked(false);
                        }

                    } catch (JSONException e) {
                        log(e.getMessage());
                    }
                },
                error -> {
                    log(error.getMessage());
                });

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            scheduleListener = (DoctorScheduleListener) context;

        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement DoctorScheduleDialog");
        }
    }

    private void log(String msg) {
        Log.d(TAG, "---____---__---____----___--____" + msg);
    }

}
