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

public class ResetPassword extends Fragment implements View.OnClickListener {
    FragmentChanger fragmentChanger;
    Button resetBtn;
    EditText emailField, passwordField, password2Field, tokenField;
    ProgressDialog progressDialog;


    public ResetPassword() {
        // Required empty public constructor
    }

    public static ResetPassword newInstance() {
        ResetPassword fragment = new ResetPassword();
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
        View view = inflater.inflate(R.layout.fragment_reset_password, container, false);
        fragmentChanger = (AuthActivity) getActivity();
        emailField = view.findViewById(R.id.email_address);
        passwordField = view.findViewById(R.id.password);
        password2Field = view.findViewById(R.id.confirm_password);
        tokenField = view.findViewById(R.id.token);
        resetBtn = view.findViewById(R.id.reset_btn);
        resetBtn.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        if (view == resetBtn) {
            final Context ctx = getContext();
            String email = emailField.getText().toString();
            String password = passwordField.getText().toString();
            String password2 = password2Field.getText().toString();
            String token = tokenField.getText().toString();
            if (TextUtils.isEmpty(email)) {
                ToastUtil.showShort(ctx, "Email is required");
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                ToastUtil.showShort(ctx, "Please enter a valid email address");
            } else if (TextUtils.isEmpty(password)) {
                ToastUtil.showShort(ctx, "Password is required");
            } else if (TextUtils.isEmpty(token)) {
                ToastUtil.showShort(ctx, "Token is required");
            } else if (!password.equals(password2)) {
                ToastUtil.showShort(ctx, "Please verify your password correctly.");
            }
            else {
                resetPassword(email, password, token);
            }
        }
    }

    private void resetPassword(String email, String password, String token) {
        final Context ctx = getContext();
        if (progressDialog == null)
            progressDialog = new ProgressDialog(ctx);
        progressDialog.show();

        Map<String, String> params = new HashMap();
        params.put("email", email);
        params.put("password", password);
        params.put("token", token);

        StringCall call = new StringCall(ctx);
        call.post(URLS.COMPLETE_PASSWORD_RESET, params, response -> {
            progressDialog.hide();
            //showModal("RESPONSE " + response);
            try {
                JSONObject resObj = new JSONObject(response);
                if (resObj.has("code") &&  resObj.getInt("code") == 0) {
                    ToastUtil.showLong(ctx, "Password reset completed");
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
