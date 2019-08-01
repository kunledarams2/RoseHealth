package com.tremendoc.tremendocdoctor.activity;

import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.callback.FragmentChanger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public abstract class FragmentActivity  extends BaseActivity implements FragmentChanger {
    public enum FragmentType { Login,  ForgotPassword, ResetPassword, CallLogs}

    private void changeView(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, fragment);
        transaction.commit();
    }

    public final void changeFragment(Fragment fragment) {
        changeView(fragment);
    }

    protected abstract void changeView(FragmentType fragmentType);


}
