package com.tremendoc.tremendocdoctor.activity;

import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Response;
import com.sinch.android.rtc.PushTokenRegistrationCallback;
import com.sinch.android.rtc.SinchError;
import com.squareup.picasso.Picasso;
import com.tremendoc.tremendocdoctor.EndPointAPI.DoctorSchedule;
import com.tremendoc.tremendocdoctor.api.API;
import com.tremendoc.tremendocdoctor.api.StringCall;
import com.tremendoc.tremendocdoctor.api.URLS;
import com.tremendoc.tremendocdoctor.callback.DoctorScheduleListener;
import com.tremendoc.tremendocdoctor.callback.MyCallback;
import com.tremendoc.tremendocdoctor.dialog.ClockingDialog;
import com.tremendoc.tremendocdoctor.dialog.DoctorScheduleDialog;
import com.tremendoc.tremendocdoctor.dialog.StatusDialog;
import com.tremendoc.tremendocdoctor.fragment.CallLogs;
import com.tremendoc.tremendocdoctor.fragment.Prescriptions;
import com.tremendoc.tremendocdoctor.fragment.appointments.AppointmentSchedule;
import com.tremendoc.tremendocdoctor.model.DoctorClocking;
import com.tremendoc.tremendocdoctor.service.CallService;
import com.tremendoc.tremendocdoctor.service.StayAliveWorker;
import com.tremendoc.tremendocdoctor.utils.CallConstants;
import com.tremendoc.tremendocdoctor.utils.DoctorScheduleContants;
import com.tremendoc.tremendocdoctor.utils.IO;
import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
//import androidx.work.PeriodicWorkRequest;
//import androidx.work.WorkManager;

import de.hdodenhof.circleimageview.CircleImageView;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.fragment.Chatroom;
import com.tremendoc.tremendocdoctor.fragment.Dashboard;
import com.tremendoc.tremendocdoctor.fragment.Notes;
import com.tremendoc.tremendocdoctor.fragment.Notifications;
import com.tremendoc.tremendocdoctor.fragment.Tips;
import com.tremendoc.tremendocdoctor.utils.ToastUtil;
import com.tremendoc.tremendocdoctor.utils.UI;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import static com.tremendoc.tremendocdoctor.utils.IO.REQUEST_GALLERY;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, PushTokenRegistrationCallback,
        CallService.StartFailedListener, DoctorScheduleListener {

    public static final String DASHBOARD = "Dashboard";
    public static final String NOTES = "Doctors Notes";
    public static final String TIPS = "Health Tips";
    public static final String APPOINTMENTS = "APPOINTMENTS";
    public static final String PRESCRIPTIONS = "Prescriptions";
    public static final String CHATROOM = "CHATROOM";
    //public static final String NOTIFICATIONS = "Notifications";
    public static final String CALL_LOGS = "Call Logs";
    public static final String SCHEDULE_TIMER = "clockIn";

    private Fragment currentFragment;

    private StatusDialog statusDialog;
    //    private ClockingDialog clockingDialog;
    private View statusIndicator;
    private CircleImageView profileImage, clockImage;
    private DoctorClocking doctorClocking;
//    private ClockingDialog clockingDialog;


    private TextView titleView;
    private String mTimer;
    private TextView nextClockIn;


    private Boolean mPushTokenIsRegistered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!API.isLoggedIn(this)) {
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        titleView = findViewById(R.id.title);

        doctorClocking = new DoctorClocking(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("fragment")) {
            changeView(bundle.getString("fragment"));
        } else {
            changeView(DASHBOARD);
        }

        statusDialog = new StatusDialog(this);
        profileImage = findViewById(R.id.profile_image);
        statusIndicator = findViewById(R.id.online_status_indicator);
        boolean isSetOnline = IO.getData(this, CallConstants.ONLINE_STATUS).equals(CallConstants.ONLINE);
        statusIndicator.setBackgroundResource(isSetOnline ? R.drawable.circle_green : R.drawable.circle_red);

        profileImage.setEnabled(false);
        profileImage.setOnClickListener(v -> statusDialog.show());
        Map<String, String> data = API.getCredentials(this);
        Picasso.get()
                .load(data.get(API.IMAGE))
                .error(R.drawable.ic_account)
                .placeholder(R.drawable.ic_account)
                .into(profileImage);

        API.setPushToken(this);
//        startWorker();
        resetStatus();


        mPushTokenIsRegistered = false;

        clockImage = findViewById(R.id.clock_image);
        clockImage.setOnClickListener(view -> checkClockIn());


    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        titleView.setText(title);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_dashboard) {
            this.changeView(Dashboard.newInstance());
            this.setTitle("Dashboard");
        } else if (id == R.id.nav_doctors_note) {
            this.changeView(Notes.newInstance());
            this.setTitle("Doctor's Notes");
        } else if (id == R.id.nav_health_tips) {
            this.changeView(Tips.newInstance());
            this.setTitle("Health Tips");
        } else if (id == R.id.nav_appointments) {
            changeView(AppointmentActivity.class);
        } else if (id == R.id.nav_prescriptions) {
            this.changeView(Prescriptions.newInstance());
            this.setTitle(PRESCRIPTIONS);
        }
        /*else if (id == R.id.nav_chatroom) {
            this.changeView(Chatroom.newInstance());
            this.setTitle("Chatroom");
        }*/
        else if (id == R.id.nav_call_logs) {
            this.changeView(CallLogs.newInstance());
            this.setTitle(CALL_LOGS);
        } else if (id == R.id.nav_notifications) {
            this.changeView(Notifications.newInstance());
            this.setTitle("Notifications");
        } else if (id == R.id.nav_signout) {
            API.logout(this);
            changeView(AuthActivity.class);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void changeView(Fragment fragment) {
        currentFragment = fragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, fragment);
        transaction.commit();
    }

    private void changeView(Class c) {
        Intent intent = new Intent(this, c);
        startActivity(intent);
        finish();
    }

    private void changeView(String fragmentName) {
        Fragment fragment = null;
        switch (fragmentName) {
            case DASHBOARD:
                fragment = Dashboard.newInstance();
                setTitle("Dashboard");
                break;
            case NOTES:
                fragment = Notes.newInstance();
                setTitle("Doctor's Notes");
                break;
            case TIPS:
                fragment = Tips.newInstance();
                setTitle("Health Tips");
                break;
            case APPOINTMENTS:
                fragment = AppointmentSchedule.newInstance();
                setTitle("Appointments");
                break;
            case CHATROOM:
                fragment = Chatroom.newInstance();
                break;
            /*case NOTIFICATIONS:
                fragment = Notifications.newInstance();
                setTitle("Notifications");
                break;*/
            case CALL_LOGS:
//                changeView(CallLogs.class);
                fragment = CallLogs.newInstance();
                setTitle("Call Logs");
                break;
            default:
                fragment = Dashboard.newInstance();
        }
        changeView(fragment);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("TipS Fragment", "onActivityResult()");
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IO.REQUEST_CAMERA) {
                log("Request Camera");
                //newTipDialog.onCameraResult(data);
            } else if (requestCode == REQUEST_GALLERY) {
                if (currentFragment instanceof Tips) {
                    Tips.newTipDialog.onGalleryResult(data);
                } else if (currentFragment instanceof Dashboard) {
                    Dashboard.tipDialog.onGalleryResult(data);
                }
                log("Request Gallery");
            }
        }
    }

    private void startWorker() {
        PeriodicWorkRequest request = new PeriodicWorkRequest
                .Builder(StayAliveWorker.class, 15, TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance().enqueue(request);
    }

    public void setOnline() {
        getSinchServiceInterface().startClient();
        //getChatServiceInterface().connect();
        if (!mPushTokenIsRegistered) {
            getSinchServiceInterface().registerPushToken(this);
        }
        statusIndicator.setBackgroundResource(R.drawable.circle_green);
    }

    public void setOffline() {

        if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().stopClient();
            getSinchServiceInterface().unregisterPushToken();
        }
        //getChatServiceInterface().disconnect();
        statusIndicator.setBackgroundResource(R.drawable.circle_red);
        UI.clearOnlineNotification(this);
    }

    private void resetStatus() {
        StringCall call = new StringCall(this);
        Map<String, String> params = new HashMap<>();
        boolean isSetOnline = IO.getData(this, CallConstants.ONLINE_STATUS).equals(CallConstants.ONLINE);
        params.put("mode", isSetOnline ? "ONLINE" : "OFFLINE");
        call.get(URLS.ONLINE_STATUS, params, false, response -> {
        }, error -> {
        });
    }

    public void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GALLERY);
        Log.d("IO __--_-_--", "galleryIntent: ");
    }

    @Override
    public void tokenRegistered() {
        mPushTokenIsRegistered = true;

    }

    @Override
    public void tokenRegistrationFailed(SinchError sinchError) {

        mPushTokenIsRegistered = false;
        Toast.makeText(this, "Push token registration failed - incoming calls can't be received!", Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        profileImage.setEnabled(true);
        getSinchServiceInterface().setStartListener(this);
    }

    @Override
    public void onStartFailed(SinchError error) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();

    }

    @Override
    public void onStarted() {
        if (!getSinchServiceInterface().isStarted()) {
            ToastUtil.showModal(this, "Please Toggle Off and No for call to come in");
        }
        log("Sinch have started");
//        startWorker();
    }

    private void openScheduleDialog() {
        DoctorScheduleDialog scheduleDialog = new DoctorScheduleDialog();
        scheduleDialog.show(getSupportFragmentManager(), "openDoctorSchedule");
    }

    private void log(String log) {
        Log.d("MainActivity", "---_--_---_---__---_----------__--__" + log);
    }

    public void checkClockIn() {

        StringCall stringCall = new StringCall(this);
        Map<String, String> params = new HashMap<>();

        stringCall.get(URLS.DOCTOR_CHECK_SCHEDULE, params,
                response -> {
                    log(response);

                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getInt("code") == 0) {

                            openScheduleDialog();

                        } else if (obj.has("code") && obj.getInt("code") == 10) {
                            IO.setData(this, DoctorScheduleContants.NEXTCLOCKIN, obj.getString("minuteToNextClockIn"));
                            ToastUtil.showModal(this, obj.getString("description"));

                            if(!obj.getString("minuteToNextClockIn") .equals("0")){
                                Thread.sleep(1000);
                                Intent intent = getIntent();
                                startActivity(intent);
                            }


                        } else {

                            log(obj.getString("description"));

                        }
                    } catch (JSONException e) {

                        log("error:" + e.getMessage());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }, error -> {

                });

    }

    @Override
    public void getClockIntime(String mTime) {

        if (IO.getData(this, DoctorScheduleContants.NEXTCLOCKIN) != null) {
            IO.deleteData(this, DoctorScheduleContants.NEXTCLOCKIN);
        }

        IO.setData(this, DoctorScheduleContants.NEXTCLOCKIN, mTime);
//        nextClockIn.setText(mTime);
        try {

            Thread.sleep(1000);
            Intent intent = getIntent();
            startActivity(intent);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }


}
