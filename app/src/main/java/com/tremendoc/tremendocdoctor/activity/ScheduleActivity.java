package com.tremendoc.tremendocdoctor.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.callback.FragmentChanger;
import com.tremendoc.tremendocdoctor.fragment.appointments.AppointmentSchedule;
import com.tremendoc.tremendocdoctor.fragment.appointments.AppointmentSuccess;

public class ScheduleActivity extends BaseActivity implements FragmentChanger {

    public static final String HOME = "AppointmentSchedule";
    public static final String CALENDAR = "Calendar";
    public static final String SUCCESS = "Success";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        changeFragment(AppointmentSchedule.newInstance());
    }

    @Override
    public void changeFragment(Fragment fragment) {
        changeView(fragment);
    }

    private void changeView(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, fragment);
        transaction.commit();
    }

    private void changeView(String string) {
        if (string.equals(CALENDAR)) {
            changeView(AppointmentSchedule.newInstance());
        } else if (string.equals(SUCCESS)){
            changeView(AppointmentSuccess.newInstance());
        } else {
            changeView(AppointmentSchedule.newInstance());
        }
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            goBack();
        }
        return super.onOptionsItemSelected(item);
    }

    private void goBack() {
        Intent intent = new Intent(this, AppointmentActivity.class);
        startActivity(intent);
        finish();
    }
}
