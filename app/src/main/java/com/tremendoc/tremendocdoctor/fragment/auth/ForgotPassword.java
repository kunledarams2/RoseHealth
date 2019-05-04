package com.tremendoc.tremendocdoctor.fragment.auth;


import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.activity.AuthActivity;
import com.tremendoc.tremendocdoctor.api.StringCall;
import com.tremendoc.tremendocdoctor.api.URLS;
import com.tremendoc.tremendocdoctor.callback.FragmentChanger;
import com.tremendoc.tremendocdoctor.dialog.ProgressDialog;
import com.tremendoc.tremendocdoctor.utils.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import androidx.fragment.app.Fragment;

public class ForgotPassword extends Fragment implements View.OnClickListener {

    FragmentChanger fragmentChanger;
    Button resetBtn, rememberBtn;
    EditText emailField;
    ProgressDialog progressDialog;


    public ForgotPassword() {
        // Required empty public constructor
    }

    public static ForgotPassword newInstance() {
        ForgotPassword fragment = new ForgotPassword();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);
        fragmentChanger = (AuthActivity) getActivity();
        emailField = view.findViewById(R.id.email_address);
        resetBtn = view.findViewById(R.id.reset_btn);
        rememberBtn = view.findViewById(R.id.remember_password_btn);
        resetBtn.setOnClickListener(this);
        rememberBtn.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        if (view == rememberBtn) {
            fragmentChanger.changeFragment(Login.newInstance());
        } else if (view == resetBtn) {
            final Context ctx = getContext();
            String email = emailField.getText().toString();
            if (TextUtils.isEmpty(email)) {
                ToastUtil.showShort(ctx, "Email is required");
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                ToastUtil.showShort(ctx, "Please enter a valid email address");
            } else {
                initiateReset(email);
            }
        }
    }

    private void initiateReset(String email) {
        final Context ctx = getContext();
        if (progressDialog == null)
            progressDialog = new ProgressDialog(ctx);
        progressDialog.show();

        Map<String, String> params = new HashMap();
        params.put("email", email);

        StringCall call = new StringCall(ctx);
        call.post(URLS.INITIATE_PASSWORD_RESET, params, response -> {
            progressDialog.hide();
            //showModal("RESPONSE " + response);
            try {
                JSONObject resObj = new JSONObject(response);
                if (resObj.has("code") &&  resObj.getInt("code") == 0) {
                    ToastUtil.showModal(ctx, "A password reset link has been sent to your email address");
                    fragmentChanger.changeFragment(Login.newInstance());
                } else if (resObj.has("description")) {
                    ToastUtil.showModal(ctx,resObj.getString("description"));
                }
            } catch (JSONException e) {
                ToastUtil.showModal(ctx, e.getMessage());
            }
        }, error -> {
            progressDialog.hide();
            if (error.networkResponse == null) {
                ToastUtil.showModal(ctx, "Please check your internet connection");
                return;
            }

            ToastUtil.showModal(ctx, "Sorry an error occurred. Please try again");
        });
    }
}
