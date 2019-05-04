package com.tremendoc.tremendocdoctor.activity;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.tabs.TabLayout;
import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.fragment.appointments.MyAppointments;
import com.tremendoc.tremendocdoctor.fragment.appointments.MySchedules;
import com.tremendoc.tremendocdoctor.ui.MyViewPager;

public class AppointmentActivity extends BaseActivity{
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private MyViewPager mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);
        setupViews();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    private void setupViews(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_navigation);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.tab_container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        TabLayout.Tab tab = tabLayout.newTab();
        tab.setText("Schedule");
        tabLayout.addTab(tab);
        TabLayout.Tab tab2 = tabLayout.newTab();
        tab2.setText("Appointments");
        tabLayout.addTab(tab2);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        boolean refresh = false;
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    MySchedules frag = MySchedules.newInstance(refresh);
                    if (refresh) {
                        frag.retry();
                    }
                    return frag;
                case 1:
                    return MyAppointments.newInstance(refresh);
                default:
                    return MySchedules.newInstance(refresh);
            }
        }

        public int getCount() {
            return 2;
        }

    }


}
