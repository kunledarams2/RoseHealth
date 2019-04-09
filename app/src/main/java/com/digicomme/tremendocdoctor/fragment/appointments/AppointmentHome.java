package com.digicomme.tremendocdoctor.fragment.appointments;


import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.digicomme.tremendocdoctor.R;
import com.digicomme.tremendocdoctor.activity.AppointmentActivity;
import com.digicomme.tremendocdoctor.callback.FragmentChanger;
import com.digicomme.tremendocdoctor.ui.MyViewPager;
import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class AppointmentHome extends Fragment {

    private FragmentChanger fragmentChanger;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private MyViewPager mViewPager;

    public AppointmentHome() {
        // Required empty public constructor
    }

    public static AppointmentHome newInstance() {
        AppointmentHome fragment = new AppointmentHome();
        //fragment.setTitle(AppointmentActivity.HOME);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_appointment_home, container, false);
        fragmentChanger = (AppointmentActivity) getActivity();
        setupViews(view);
        return  view;
    }

    private void setupViews(View view){
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_navigation);

        ((AppointmentActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppointmentActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = view.findViewById(R.id.tab_container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = view.findViewById(R.id.tabs);
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

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return MySchedules.newInstance();
                case 1:
                    return MyAppointments.newInstance();
                default:
                    return MySchedules.newInstance();
            }
        }

        public int getCount() {
            return 2;
        }

    }

}
