package com.digicomme.tremendocdoctor.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.digicomme.tremendocdoctor.R;
import com.digicomme.tremendocdoctor.callback.FragmentChanger;
import com.digicomme.tremendocdoctor.fragment.calendar.ChooseDays;

public class CalendarActivity extends AppCompatActivity implements FragmentChanger {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        changeView(ChooseDays.newInstance());
    }

    private void changeView(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame, fragment);
        transaction.commit();
    }

    @Override
    public void changeFragment(Fragment fragment) {
        changeView(fragment);
    }
}
