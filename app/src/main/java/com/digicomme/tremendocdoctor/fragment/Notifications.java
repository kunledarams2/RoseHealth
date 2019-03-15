package com.digicomme.tremendocdoctor.fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.digicomme.tremendocdoctor.R;

public class Notifications extends Fragment {

    public static Notifications newInstance() {
        Notifications fragment = new Notifications();
        Bundle bundle = new Bundle();
        //bundle.putString("action", action);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.notifications_fragment, container, false);
        return view;
    }

}
