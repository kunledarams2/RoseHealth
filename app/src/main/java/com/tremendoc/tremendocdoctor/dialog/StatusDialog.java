package com.tremendoc.tremendocdoctor.dialog;

import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.activity.MainActivity;
import com.tremendoc.tremendocdoctor.api.StringCall;
import com.tremendoc.tremendocdoctor.api.URLS;
import com.tremendoc.tremendocdoctor.utils.CallConstants;
import com.tremendoc.tremendocdoctor.utils.Formatter;
import com.tremendoc.tremendocdoctor.utils.IO;
import com.tremendoc.tremendocdoctor.utils.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class StatusDialog extends Dialog {
    private MainActivity activity;
    private Switch statusSwitch;
    private TextView statusLabel;
    private View indicator;

    public StatusDialog(MainActivity activity) {
        super(activity);
        this.activity = activity;
        setContentView(R.layout.dialog_online_status);
        setupViews();
    }

    private void setupViews() {
        boolean isSetOnline = IO.getData(getContext(), CallConstants.ONLINE_STATUS).equals(CallConstants.ONLINE);
        statusSwitch = findViewById(R.id.status_switch);
        statusLabel = findViewById(R.id.status_label);
        indicator = findViewById(R.id.status_indicator);
        Button closeBtn = findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(btn -> dismiss());

        statusSwitch.setChecked(isSetOnline);
        statusLabel.setText(isSetOnline ? "You are currently online" : "You are currently offline");
        indicator.setBackgroundResource(isSetOnline ? R.drawable.circle_green : R.drawable.circle_red);

        statusSwitch.setOnCheckedChangeListener((compoundButton, b) -> setOnline(b));
        //UI.createNotification(,"Tremendoc", "You are online and can receive incoming chats" );

    }

    private void setOnline() {
        IO.setData(getContext(), CallConstants.ONLINE_STATUS, CallConstants.ONLINE);
        activity.setOnline();
        statusLabel.setText( "You are currently online");
        indicator.setBackgroundResource(R.drawable.circle_green);
    }

    private void setOffline() {
        IO.setData(getContext(), CallConstants.ONLINE_STATUS, CallConstants.OFFLINE);
        activity.setOffline();
        statusLabel.setText("You are currently offline");
        indicator.setBackgroundResource(R.drawable.circle_red);
    }

    private void setOnline(boolean status) {
        StringCall call = new StringCall(getContext());
        Map<String, String> params = new HashMap<>();
        params.put("mode", status ? "ONLINE" : "OFFLINE");
        call.get(URLS.ONLINE_STATUS, params, false, response -> {
            log(response);
            try {
                JSONObject object = new JSONObject(response);
                if (object.has("code") && object.getInt("code") == 0) {
                    ToastUtil.showLong(getContext(), "You are now online");
                    if (status) {
                        setOnline();
                    } else {
                        setOffline();
                    }
                } else {
                    ToastUtil.showLong(getContext(), object.getString("description"));
                }
                log("STATUS DIALOG -> " + object.getString("description"));
            }catch (JSONException e) {
                log(e.getLocalizedMessage());
            }
        }, error -> {
            log("VOLLEY ERROR");
            log(error.getMessage());
            if (error.networkResponse == null) {
                log("Network response is null");
            } else {
                log("DATA: " + Formatter.bytesToString(error.networkResponse.data));
            }
        });
    }


    private void log(String log) {
        Log.d("Status Dialog", "---_--_---_---__---_----------__--__" + log);
    }

}
