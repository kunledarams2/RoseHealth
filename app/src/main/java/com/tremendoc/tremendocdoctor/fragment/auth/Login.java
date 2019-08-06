package com.tremendoc.tremendocdoctor.fragment.auth;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.activity.AuthActivity;
import com.tremendoc.tremendocdoctor.activity.MainActivity;
import com.tremendoc.tremendocdoctor.api.API;
import com.tremendoc.tremendocdoctor.api.StringCall;
import com.tremendoc.tremendocdoctor.api.URLS;
import com.tremendoc.tremendocdoctor.callback.FragmentChanger;
import com.tremendoc.tremendocdoctor.dialog.ProgressDialog;
import com.tremendoc.tremendocdoctor.utils.DeviceName;
import com.tremendoc.tremendocdoctor.utils.Formatter;
import com.tremendoc.tremendocdoctor.utils.IO;
import com.tremendoc.tremendocdoctor.utils.Permission;
import com.tremendoc.tremendocdoctor.utils.ToastUtil;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Login#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Login extends Fragment implements View.OnClickListener {

    private FragmentChanger fragmentChanger;
    private Button signinBtn, forgotBtn;
    private TextInputEditText email, password;

    private ProgressDialog progressDialog;

    public static final int PHONE_STATE_PERMISSION = 10;

    public Login() {
        // Required empty public constructor
    }

    public static Login newInstance() {
        Login fragment = new Login();
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
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        fragmentChanger = (AuthActivity) getActivity();
        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.password);
        signinBtn = view.findViewById(R.id.login_btn);
        forgotBtn = view.findViewById(R.id.forgot_password_btn);
        signinBtn.setOnClickListener(this);
        forgotBtn.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        if (view == forgotBtn) {
            fragmentChanger.changeFragment(ForgotPassword.newInstance());
        }
        else if (view == signinBtn) {
            if (!Permission.permissionIsGranted(getContext(), Manifest.permission.READ_PHONE_STATE)) {
                ActivityCompat.requestPermissions(getActivity(), new String[] {
                        Manifest.permission.READ_PHONE_STATE} ,  PHONE_STATE_PERMISSION);
            } else {
                signin();
            }
            //signin();
        }
    }

    private void signin() {
        final Context ctx = getContext();
        String _email = email.getText().toString();
        String _password = password.getText().toString();

        if (TextUtils.isEmpty(_email)) {
            //email.setError("Email is required");
            ToastUtil.showShort(ctx, "Email is required");
        } else if (!Patterns.EMAIL_ADDRESS.matcher(_email).matches()) {
            //email.setError("Please enter a valid email address");
            ToastUtil.showShort(ctx, "Please enter a valid email address");
        } else if (TextUtils.isEmpty(_password)) {
            //password.setError("Password is required");
            ToastUtil.showShort(ctx, "Password is required");
        } else if (!TextUtils.isEmpty(_email) && !TextUtils.isEmpty(_password)) {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(getContext());
            }
            progressDialog.show();
            Map<String, String> params = new HashMap<>();
            params.put("email", _email);
            params.put("password", _password);
            params.put("brand", Build.BRAND);
            params.put("operatingSystem", "ANDROID");
            String myUUID = API.getUUID(getContext());
            params.put("uuid", myUUID);

            StringCall call = new StringCall(getContext());
            call.post(URLS.USER_LOGIN, params, response -> {
                progressDialog.hide();
                Log.d("LOGIN", "response " + response);
                //showModal("RESPONSE " + response);
                try {
                    JSONObject resObj = new JSONObject(response);
                    if (resObj.has("code") &&  resObj.getInt("code") == 0) {
                        ToastUtil.showLong(getContext(), "Login successful");
                        API.setCredentials(getContext(), response);
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        getContext().startActivity(intent);
                    } else if (resObj.has("description")) {
                        ToastUtil.showModal(getContext(),resObj.getString("description"));
                    }
                } catch (JSONException e) {
                    ToastUtil.showModal(getContext(), e.getMessage());
                }
            }, error -> {
                if (error.getMessage() != null)
                    Log.d("Login", "--__---___----__--- error.getMessage() is null");
                else
                    Log.d("Login", "--_--_--_-- " + error.getMessage());
                progressDialog.hide();

                if (error.networkResponse == null) {
                    ToastUtil.showModal(getContext(), "Please check your internet connection");
                    return;
                }

                String msg = Formatter.bytesToString(error.networkResponse.data);
                Log.d("Login ", "--_--__-__-___-----_ " + msg);

                /*String msg = "";
                msg += "STATUS CODE:  " + error.networkResponse.statusCode;
                msg += "\nHEADERS COUNT: " + error.networkResponse.allHeaders.size();
                msg += "\nTO STRING: " + error.networkResponse.toString();
                try {
                    msg += "\nDATA: " + new String(error.networkResponse.data, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    showModal(e.getMessage());
                }
                //showModal("VOLLEY ERROR TO STRING " + error.toString());
                msg = "\nERROR   " + error.toString(); */
                ToastUtil.showModal(getContext(), "Sorry an error occurred. Please try again");
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PHONE_STATE_PERMISSION:
                String grantResultsStr = "";
                for (int r : grantResults) {
                    grantResultsStr += r + " |";
                }
                grantResultsStr += "\n PERMISSION_GRANTED ==== " + PackageManager.PERMISSION_GRANTED;
                ToastUtil.showModal(getContext(), grantResultsStr);
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    signin();
                } else {
                    //not granted
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
