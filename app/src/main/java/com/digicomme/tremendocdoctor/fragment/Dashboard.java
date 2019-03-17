package com.digicomme.tremendocdoctor.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.digicomme.tremendocdoctor.R;
import com.digicomme.tremendocdoctor.api.API;
import com.digicomme.tremendocdoctor.api.StringCall;
import com.digicomme.tremendocdoctor.api.URLS;
import com.digicomme.tremendocdoctor.binder.CircleAppBinder;
import com.digicomme.tremendocdoctor.dialog.NewTipDialog;
import com.digicomme.tremendocdoctor.model.Tip;
import com.digicomme.tremendocdoctor.utils.Formatter;
import com.digicomme.tremendocdoctor.utils.ImageLoader;
import com.digicomme.tremendocdoctor.viewmodel.ProfileViewModel;
import com.digicomme.tremendocdoctor.viewmodel.TipsViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Dashboard  extends Fragment {

    private LinearLayoutManager manager;
    private RecyclerView appointmentsRecyclerView;
    private CircleAppBinder adapter;

    private CircleImageView tipImage;
    private TextView tipTitle, tipSummary;

    private NewTipDialog tipDialog;
    private TipsViewModel viewModel;
    private int attempts = 0;
    private ProfileViewModel profileViewModel;
    private CircleImageView doctorAvatar;
    private TextView doctorName, doctorSpecialty;
    private TextView currentEarning, totalEarning, noConsultation, totalConsultation;

    //Earnings
    private View earningPlaceholder, cardAmount, cardConsultation;
    private ProgressBar earningLoader;
    private ImageButton earningRetryBtn;
    private TextView earningText;

    //appointments
    private View appointmentPlaceholder, appointmentView;
    private ProgressBar appointmentLoader;
    private ImageButton appointmentRetryBtn;
    private TextView appointmentText;


    public static Dashboard newInstance() {
        Dashboard fragment = new Dashboard();
        Bundle bundle = new Bundle();
        //bundle.putString("action", action);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        appointmentsRecyclerView = view.findViewById(R.id.appointments_recycler_view);
        setupViews(view);
        setupAdapter();
        return view;
    }

    private void setupViews(View view) {
        //doctor's details
        Map<String, String> data = API.getCredentials(getContext());
        doctorName = view.findViewById(R.id.doctor_name);
        doctorSpecialty = view.findViewById(R.id.doctor_spec);
        doctorAvatar = view.findViewById(R.id.avatar);

        doctorName.setText(data.get(API.FIRST_NAME) + " " + data.get(API.LAST_NAME));

        new ImageLoader(doctorAvatar).execute(API.getCredentials(getContext()).get(API.IMAGE));

        //Earnings
        earningPlaceholder = view.findViewById(R.id.earnings_placeholder);
        cardAmount = view.findViewById(R.id.card_amount);
        cardConsultation = view.findViewById(R.id.card_consultation);
        earningLoader = view.findViewById(R.id.earnings_placeholder_progress_bar);
        earningText = view.findViewById(R.id.earnings_placeholder_text);
        earningRetryBtn = view.findViewById(R.id.earnings_placeholder_retry_btn);
        earningRetryBtn.setOnClickListener(btn -> fetchEarnings());
        //for showing figures
        currentEarning = view.findViewById(R.id.amount_figure);
        totalEarning = view.findViewById(R.id.amount_figure2);
        noConsultation = view.findViewById(R.id.consultation_figure);
        totalConsultation = view.findViewById(R.id.consultation_figure2);

        //appointments
        appointmentPlaceholder = view.findViewById(R.id.appointments_placeholder);
        appointmentView = view.findViewById(R.id.appointments);
        appointmentLoader = view.findViewById(R.id.appointments_placeholder_progress_bar);
        appointmentText = view.findViewById(R.id.appointments_placeholder_text);
        appointmentRetryBtn = view.findViewById(R.id.appointments_placeholder_retry_btn);
        appointmentRetryBtn.setOnClickListener(btn -> fetchAppointments());


        //Health tips
        tipImage = view.findViewById(R.id.health_tip_image);
        tipTitle = view.findViewById(R.id.health_tip_title);
        tipSummary = view.findViewById(R.id.health_tip_content);
        Button newTipBtn = view.findViewById(R.id.new_tip_btn);
        newTipBtn.setOnClickListener(btn -> {
            if (tipDialog == null)
                tipDialog = new NewTipDialog(getActivity());
            tipDialog.show();
        });

    }

    private void setupAdapter() {
        //ItemDecorator decorator =
        manager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        adapter = new CircleAppBinder();
        appointmentsRecyclerView.setLayoutManager(manager);
        appointmentsRecyclerView.setAdapter(adapter);
    }

    private void fetchEarnings() {
        //when we are just getting in and data are yet to come in
        earningPlaceholder.setVisibility(View.VISIBLE);
        cardConsultation.setVisibility(View.GONE);
        cardAmount.setVisibility(View.GONE);
        earningLoader.setVisibility(View.VISIBLE);
        earningText.setVisibility(View.GONE);
        earningRetryBtn.setVisibility(View.GONE);

        StringCall call = new StringCall(getContext());
        call.get(URLS.CURRENT_EARNINGS, null, response -> {
            Log.d("Dashboard Earnings", response);
            try {
                JSONObject data = new JSONObject(response);
                if (data.has("code") && data.getInt("code") == 0) {
                    currentEarning.setText( "N" + data.getString("earnedToday"));
                    totalEarning.setText("N" + data.getString("totalEarned"));
                    noConsultation.setText(data.getString("consultationsToday"));
                    totalConsultation.setText(data.getString("totalConsultations"));

                    earningPlaceholder.setVisibility(View.GONE);
                    cardAmount.setVisibility(View.VISIBLE);
                    cardConsultation.setVisibility(View.VISIBLE);
                } else {
                    Log.d("Dashboard earning", data.getString("description"));
                    earningLoader.setVisibility(View.GONE);
                    earningText.setText(data.getString("description"));
                    earningRetryBtn.setVisibility(View.VISIBLE);
                    earningText.setVisibility(View.VISIBLE);
                }
            } catch (JSONException e) {
                Log.d("Dashboard earning", e.getMessage());
                earningLoader.setVisibility(View.GONE);
                earningText.setText(e.getMessage());
                earningRetryBtn.setVisibility(View.VISIBLE);
                earningText.setVisibility(View.VISIBLE);
            }
        }, error -> {
            Log.d("Dashboard Earnings Err", "Error "  + error.getMessage());
            if (error.networkResponse != null) {
                String string = Formatter.bytesToString(error.networkResponse.data);
                Log.d("Dashboard Earnings Err", "SERver Error:   " + string);
            }
        });
    }

    private void fetchAppointments() {
        //when we are just getting in and data are yet to come in
        appointmentPlaceholder.setVisibility(View.VISIBLE);
        appointmentView.setVisibility(View.GONE);
        appointmentRetryBtn.setVisibility(View.GONE);
        appointmentLoader.setVisibility(View.VISIBLE);
        appointmentText.setVisibility(View.GONE);

        StringCall call = new StringCall(getContext());
        call.get(URLS.APPOINTMENTS + "PENDING", null, response -> {
            log( "APPOINTMENT RESPONSE " + response);
            try {
                JSONObject object = new JSONObject(response);
                if (object.has("code") && object.getInt("code") == 0) {
                    JSONArray array = object.getJSONArray("appointments");

                    if (array.length() > 0) {
                        List<String> list = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            list.add(obj.getString("appointmentDate"));
                        }
                        adapter.setAppointments(list);
                        appointmentPlaceholder.setVisibility(View.GONE);
                        appointmentView.setVisibility(View.VISIBLE);
                    } else {
                        appointmentText.setText("Sorry you have not scheduled any appointments");
                        appointmentText.setVisibility(View.VISIBLE);
                        appointmentLoader.setVisibility(View.GONE);
                        appointmentRetryBtn.setVisibility(View.VISIBLE);
                    }
                } else {
                    log("fetchAppointments   " + object.getString("description"));
                    appointmentText.setText(object.getString("description"));
                    appointmentText.setVisibility(View.VISIBLE);
                    appointmentLoader.setVisibility(View.GONE);
                    appointmentRetryBtn.setVisibility(View.VISIBLE);
                    //ToastUtil.showModal(context, object.getString("description"));
                    //result.setMessage(object.getString("description"));
                }
            } catch (JSONException e) {
                log(e.getMessage());
                appointmentText.setText(e.getMessage());
                appointmentText.setVisibility(View.VISIBLE);
                appointmentLoader.setVisibility(View.GONE);
                appointmentRetryBtn.setVisibility(View.VISIBLE);
                //result.setMessage(e.getMessage());
            }
        }, error -> {
            Log.d("Dashboard Earnings Err", "Error "  + error.getMessage());
            if (error.networkResponse != null) {
                String string = Formatter.bytesToString(error.networkResponse.data);
                Log.d("Dashboard Earnings Err", "SERver Error:   " + string);
                appointmentText.setText("Sorry, something went wrong. Please try again");
            } else {
                appointmentText.setText("Please check your internet connection");
            }
            appointmentText.setVisibility(View.VISIBLE);
            appointmentLoader.setVisibility(View.GONE);
            appointmentRetryBtn.setVisibility(View.VISIBLE);
        });
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(TipsViewModel.class);
        observableViewModel(viewModel);

        profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel.class);
        observeProfile(profileViewModel);


        fetchEarnings();
        fetchAppointments();
        //fetchTodayConsultation();
    }

    private void observeProfile(ProfileViewModel viewModel) {
        viewModel.getMediatorLiveData().observe(this, result -> {
            if (result.isSuccessful()) {
                try {
                    JSONObject profile = result.getData();
                    //if (profile.has("firstname") && profile.has("lastName"))
                    //    doctorName.setText(profile.getString("firstname") +" " + profile.getString("lastName"));
                    if (profile.has("specialty"))
                        doctorSpecialty.setText(profile.getString("specialty"));

                } catch (JSONException e) {
                    Log.d("Dashboard Profile", e.getMessage());
                }
            }
        });
    }

    private void observableViewModel(TipsViewModel viewModel) {
        viewModel.getMediatorLiveData().observe(this, result -> {
            if (result.isSuccessful() && result.getDataList().isEmpty()) {

            } else if (result.isSuccessful() && !result.getDataList().isEmpty()) {
                Tip tip = result.getDataList().get(0);
                tipTitle.setText(tip.getTitle());
                tipSummary.setText(tip.getSummary());
                new ImageLoader(tipImage).execute(tip.getImage());
            } else if (!result.isSuccessful()) {
                if (attempts < 5) {
                    viewModel.refresh();
                    attempts++;
                }
            }
        });
    }



    private static void log(String string) {
        Log.d("Dashboard", " __-_-_-_---_-_-_-_-_-_-_-_-_-_-_-_-_-_-_____-_-_-_-_-_  " + string);
    }

}
