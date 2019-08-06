package com.tremendoc.tremendocdoctor.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.tremendoc.tremendocdoctor.callback.MyCallback;
import com.tremendoc.tremendocdoctor.utils.Formatter;
import com.tremendoc.tremendocdoctor.utils.IO;
import com.tremendoc.tremendocdoctor.utils.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * Created by ikay on 6/14/2018.
 */

public class API {
    private static API mApi;
    private RequestQueue requestQueue;
    private ImageLoader imageLoader;
    private static Context context;

    public static final String SHARED_PREFERENCES = "com.tremendoc.tremendocdoctor.SHARED_PREFERENCES";
    public static final String EMAIL = "email";
    public static final String SESSION_ID = "sessionId";
    public static final String USERNAME = "username";
    public static final String DOCTOR_ID = "doctorId";
    public static final String USER_DATA = "user_data";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String IMAGE = "image";
    public static final String SPECIALTY_ID = "specialtyId";
    public static final String RATING = "ratings";

    public static final String SERVER_KEY = "SERVER_KEY";
    public static final String PUSH_TOKEN_SET = "PUSH_TOKEN_SET";

    private static final String MY_UUID = "MY_UUID";

    private API(Context context) {
        this.context = context;
        this.requestQueue = getRequestQueue();

        /*imageLoader = new ImageLoader(requestQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> cache = new LruCache<>(20);
            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }
        }); */
    }

    public static synchronized API getInstance(Context context) {
        if (mApi == null) {
            mApi = new API(context);
        }
        return mApi;
    }

    public Context getContext() {
        return context;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(tag);
        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader() {
        return  imageLoader;
    }

    public void cancelPendingLoading(Object tag) {
        if (requestQueue != null) {
            requestQueue.cancelAll(tag);
        }

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        };
        StringRequest sr = new StringRequest(Request.Method.POST, "", listener, errorListener);
        //JsonObjectRequest jor = new JsonObjectRequest();
    }

    public static String buildParams(String url, Map<String, String> params) {
        if (params.size() > 0) {
            StringBuilder sb = new StringBuilder(url);
            sb.append("?");
            for (String key: params.keySet()) {
                sb.append(key);
                sb.append("=");
                sb.append(params.get(key));
                sb.append("&");
            }

            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        }
        return url;
    }

    public String getTag(String url, String method) {
        url = url.replace(URLS.SERVER, "");
        String tag = url.replace("/", "-");
        tag = tag + method;
        return  tag;
    }

    /*public static void log(String key, String content){
        byte[] bytes = content.getBytes();
        File folder = new File(Environment.getExternalStorageDirectory(), "EngageSM/log");
        if(!folder.exists()) folder.mkdirs();
        try {
            FileOutputStream fos = new FileOutputStream(new File(folder, key));
            fos.write(bytes);
            fos.close();
        }catch(IOException e){
            //ToastUtil.error(context, e.getLocalizedMessage());
        }
    } */

    private static Map<String, String> jsonToMap(String jsonString) {
        Map<String, String> map = new HashMap<>();
        try {
            JSONObject obj = new JSONObject(jsonString);
            if (obj.has(EMAIL))
                map.put(EMAIL, obj.getString(EMAIL));

            if (obj.has(SESSION_ID))
                map.put(SESSION_ID, obj.getString(SESSION_ID));

            if (obj.has(DOCTOR_ID))
                map.put(DOCTOR_ID, obj.getString(DOCTOR_ID));

            if (obj.has(USERNAME))
                map.put(USERNAME, obj.getString(USERNAME));

            if (obj.has(FIRST_NAME))
                map.put(FIRST_NAME, obj.getString(FIRST_NAME));

            if (obj.has(LAST_NAME))
                map.put(LAST_NAME, obj.getString(LAST_NAME));

            if (obj.has(IMAGE))
                map.put(IMAGE, obj.getString(IMAGE));

            if (obj.has(RATING))
                map.put(RATING, obj.getString(RATING));

        }catch (JSONException e){

        }
        return map;
    }

    public static void setCredentials(Context context, String json) {
        setCredentials(context, jsonToMap(json));
    }

    public static void setCredentials(Context context, Map<String, String> credentials){
        SharedPreferences.Editor editor = context.getSharedPreferences(API.SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
        editor.putString(API.SESSION_ID, credentials.get(API.SESSION_ID));
        editor.putString(API.EMAIL, credentials.get(API.EMAIL));
        editor.putString(API.DOCTOR_ID, credentials.get(API.DOCTOR_ID));
        editor.putString(API.USERNAME, credentials.get(API.USERNAME));
        editor.putString(API.FIRST_NAME, credentials.get(API.FIRST_NAME));
        editor.putString(API.LAST_NAME, credentials.get(API.LAST_NAME));
        editor.putString(API.IMAGE, credentials.get(API.IMAGE));
        editor.putString(API.RATING, credentials.get(API.RATING));
        editor.apply();
    }

    public static Map<String, String> getCredentials(Context context){
        Map<String, String> credentials = new HashMap<>();
        SharedPreferences prefs = context.getSharedPreferences(API.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        String email = prefs.getString(API.EMAIL,"");
        String sessionId = prefs.getString(API.SESSION_ID,"");
        String username = prefs.getString(API.USERNAME, "");
        String customerId = prefs.getString(API.DOCTOR_ID, "");
        String firstName = prefs.getString(API.FIRST_NAME, "");
        String lastName = prefs.getString(API.LAST_NAME, "");
        String image = prefs.getString(API.IMAGE, "");
        credentials.put(API.EMAIL, email);
        credentials.put(API.SESSION_ID, sessionId);
        credentials.put(API.USERNAME, username);
        credentials.put(API.DOCTOR_ID, customerId);
        credentials.put(API.FIRST_NAME, firstName);
        credentials.put(API.LAST_NAME, lastName);
        credentials.put(API.IMAGE, image);
        return credentials;
    }

    public static String getFullName() {
        SharedPreferences prefs = context.getSharedPreferences(API.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        String firstName = prefs.getString(API.FIRST_NAME, "");
        String lastName = prefs.getString(API.LAST_NAME, "");
        return firstName + " " + lastName;
    }

    public static String getTitledName() {
        return "Dr. " + getFullName();
    }

    public static String getUUID(Context ctx) {
        String uuid = IO.getData(ctx, API.MY_UUID);
        if (uuid.isEmpty()) {
            uuid = UUID.randomUUID().toString();
            IO.setData(ctx, API.MY_UUID, uuid);
        }
        return uuid;
    }

    public static void setUserData(Context context, JSONObject data){
        SharedPreferences.Editor editor = context.getSharedPreferences(API.SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
        editor.putString(API.USER_DATA, data.toString());
        editor.apply();
    }

    /*public static User getUserData(Context context) throws JSONException{
        SharedPreferences prefs = context.getSharedPreferences(API.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        String string = prefs.getString(API.USER_DATA,"{}");
        JSONObject obj = new JSONObject(string);
        User user = User.parse(obj);
        return user;
    }*/

    public static boolean isLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(API.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return prefs.contains(API.SESSION_ID);
    }

    public static String getSessionId(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(API.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return prefs.getString(SESSION_ID, "");
    }

    public static void logout(Context context) {
        context.getSharedPreferences(API.SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .edit().clear().commit();

        StringCall call = new StringCall(context);
        Map<String, String> params = new HashMap<>();
        params.put("mode", "OFFLINE");
        call.get(URLS.ONLINE_STATUS, params, false, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.has("code") && object.getInt("code") == 0) {
                } else {
                    ToastUtil.showLong(context, object.getString("description"));
                }
            }catch (JSONException e) {
            }
        }, error -> {
            if (error.networkResponse == null) {
                ToastUtil.showModal(context, "Please check your internet connection");
            } else {
                Log.d("Logout Error", "DATA: " + Formatter.bytesToString(error.networkResponse.data));
            }
        });
    }

    private void setOnline(boolean status) {
    }

    public static String getDoctorId(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(API.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return prefs.getString(DOCTOR_ID, "");
    }


    public static void setPushToken(Context context) {
        String status = IO.getData(context, API.PUSH_TOKEN_SET);
        if (status.equalsIgnoreCase("true")) {
            Log.d("SET-PUSH-TOKEN", "setPushToken: PUSH TOKEN HAS BEEN SET");
            return;
        }

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FIREBASE TOKEN", "getInstanceId failed", task.getException());
                        return;
                    }

                    // Get new Instance ID token
                    String token = task.getResult().getToken();

                    // Log and toast
                    //String msg = getString(R.string.msg_token_fmt, token);
                    Log.d("FIREBASE TOKEN ", token);

                    StringCall call = new StringCall(context);
                    Map<String, String> params = new HashMap<>();
                    params.put("pushToken", token);
                    call.post(URLS.PUSH_TOKEN, params, response -> {
                        Log.d("Set firebase", response);
                                try {
                                    JSONObject obj = new JSONObject(response);
                                    if (obj.has("code") && obj.getInt("code") == 0) {
                                        IO.setData(context, API.PUSH_TOKEN_SET, "true");
                                        //ToastUtil.showLong(context, "Firebase token is set");
                                        Log.d("Set firebase", "Firebase token is set");
                                    }
                                    else {
                                        Log.d("Set firebase", obj.getString("description"));
                                    }
                                }catch (JSONException e) {
                                    Log.d("Set firebase", e.getMessage());
                                }
                        }
                        , error -> {
                                IO.setData(context, API.PUSH_TOKEN_SET, "false");
                                //Toa(context, "Firebase token NOT SET");
                                if (error.getMessage() != null)
                                    Log.d("Set firebase", error.getMessage());
                                else
                                    Log.d("Set firebase", "error.getMessage() is NULL");

                                if (error.networkResponse != null)
                                    Log.d("Set firebase error ", Formatter.bytesToString(error.networkResponse.data));
                                else
                                    Log.d("Set firebase", "error.networkResponse is NULL");

                            }
                    );
                });
    }

}
