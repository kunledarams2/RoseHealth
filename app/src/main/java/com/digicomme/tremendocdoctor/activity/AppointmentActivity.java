package com.digicomme.tremendocdoctor.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.digicomme.tremendocdoctor.R;
import com.digicomme.tremendocdoctor.callback.FragmentChanger;
import com.digicomme.tremendocdoctor.fragment.appointments.AppointmentHome;
import com.digicomme.tremendocdoctor.fragment.appointments.AppointmentSchedule;

public class AppointmentActivity extends BaseActivity implements FragmentChanger {

    public static final String HOME = "AppointmentSchedule";
    public static final String CALENDAR = "Calendar";
    public static final String SUCCESS = "Success";

    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);
        changeFragment(AppointmentHome.newInstance());
    }

    @Override
    public void changeFragment(Fragment fragment) {
        currentFragment = fragment;
        changeView(fragment);
    }

    private void changeView(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, fragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (currentFragment == null)
            super.onBackPressed();

        goBack();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home && currentFragment != null) {
            goBack();
        }
        return super.onOptionsItemSelected(item);
    }

    private void goBack() {
        if (currentFragment instanceof AppointmentHome) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else if (currentFragment instanceof AppointmentSchedule) {
            changeFragment(AppointmentHome.newInstance());
        }

    }
}
