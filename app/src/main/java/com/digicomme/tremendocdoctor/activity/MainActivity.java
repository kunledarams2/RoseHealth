package com.digicomme.tremendocdoctor.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.digicomme.tremendocdoctor.api.API;
import com.digicomme.tremendocdoctor.api.StringCall;
import com.digicomme.tremendocdoctor.api.URLS;
import com.digicomme.tremendocdoctor.fragment.CallLogs;
import com.digicomme.tremendocdoctor.fragment.Prescriptions;
import com.digicomme.tremendocdoctor.fragment.appointments.AppointmentSchedule;
import com.digicomme.tremendocdoctor.utils.Formatter;
import com.digicomme.tremendocdoctor.utils.IO;
import com.digicomme.tremendocdoctor.utils.ToastUtil;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;

import com.digicomme.tremendocdoctor.R;
import com.digicomme.tremendocdoctor.fragment.Chatroom;
import com.digicomme.tremendocdoctor.fragment.Dashboard;
import com.digicomme.tremendocdoctor.fragment.Notes;
import com.digicomme.tremendocdoctor.fragment.Notifications;
import com.digicomme.tremendocdoctor.fragment.Tips;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.digicomme.tremendocdoctor.utils.IO.REQUEST_GALLERY;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String DASHBOARD = "Dashboard";
    public static final String NOTES     = "Doctors Notes";
    public static final String TIPS      = "Health Tips";
    public static final String APPOINTMENTS = "APPOINTMENTS";
    public static final String PRESCRIPTIONS = "Prescriptions";
    public static final String CHATROOM = "CHATROOM";
    //public static final String NOTIFICATIONS = "Notifications";
    public static final String CALL_LOGS = "Call Logs";

    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("fragment")) {
            changeView(bundle.getString("fragment"));
        } else {
            changeView(DASHBOARD);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setOnline();
        //getWebSocketInterface().setOnline();
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
        }*/ else if(id == R.id.nav_call_logs){
            this.changeView(CallLogs.newInstance());
            this.setTitle(CALL_LOGS);
        }
        else if (id == R.id.nav_notifications) {
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
            case CHATROOM: fragment = Chatroom.newInstance();
                break;
            /*case NOTIFICATIONS:
                fragment = Notifications.newInstance();
                setTitle("Notifications");
                break;*/
            case CALL_LOGS:
                fragment = CallLogs.newInstance();
                setTitle("Call Logs");
                break;
            default: fragment = Dashboard.newInstance();
        }
        changeView(fragment);
    }

    private void setOnline() {
        StringCall call = new StringCall(this);
        Map<String, String> params = new HashMap<>();
        params.put("mode", "ONLINE");
        call.get(URLS.ONLINE_STATUS, params, response -> {
            log(response);
            try {
                JSONObject object = new JSONObject(response);
                if (object.has("code") && object.getInt("code") == 0) {
                    ToastUtil.showLong(this, "You are now online");
                    //initiateConsultation(doctor, subId);
                } else {
                    //ToastUtil.showLong(this, object.getString("description"));
                }
            }catch (JSONException e) {
                log(e.getLocalizedMessage());
            }
        }, error -> {
            log("VOLLEY ERROR");
            log(error.getMessage());
            if (error.networkResponse == null) {
                log("Network response is null");
            } else {
                log("DATA: " + Formatter.bytesToString(error.networkResponse.data));
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        Log.d("TipS Fragment", "onActivityResult()");
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IO.REQUEST_CAMERA) {
                log("Request Camera");
                //newTipDialog.onCameraResult(data);
            } else if (requestCode == REQUEST_GALLERY){
                if (currentFragment instanceof Tips) {
                    Tips.newTipDialog.onGalleryResult(data);
                } else if (currentFragment instanceof Dashboard) {
                    Dashboard.tipDialog.onGalleryResult(data);
                }
                log("Request Gallery");
            }
        }
    }

    public  void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GALLERY);
        Log.d("IO __--_-_--", "galleryIntent: ");
    }

    private void log(String log) {
        Log.d("MainActivity", "---_--_---_---__---_----------__--__" + log);
    }

}
