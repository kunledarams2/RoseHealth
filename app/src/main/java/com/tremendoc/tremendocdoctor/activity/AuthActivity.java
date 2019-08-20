package com.tremendoc.tremendocdoctor.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.api.API;
import com.tremendoc.tremendocdoctor.fragment.auth.Login;

import androidx.fragment.app.Fragment;

public class AuthActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (API.isLoggedIn(this)) {
            Intent intent = new Intent(this,  MainActivity.class);
            startActivity(intent);
//            getSinchServiceInterface().startClient();
            finish();
        }

        setContentView(R.layout.activity_login);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String string  = bundle.getString("fragment");
            FragmentType type = FragmentType.valueOf(string);
            changeView(type);
        } else {
            changeFragment(Login.newInstance());
        }
    }

    protected void changeView(FragmentType fragmentType) {
        if (fragmentType.equals(FragmentType.Login)) {
            changeFragment(Login.newInstance());
        } else if (fragmentType.equals(FragmentType.ForgotPassword)) {

        } else if (fragmentType.equals(FragmentType.ResetPassword)) {

        }
    }


}
