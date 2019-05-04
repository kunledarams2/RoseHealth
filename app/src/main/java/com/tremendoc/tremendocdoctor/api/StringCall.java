package com.tremendoc.tremendocdoctor.api;

import android.content.Context;
import android.util.Log;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ikay on 6/14/2018.
 */

public class StringCall {
    API mApi;

    public StringCall(Context context) {
        this.mApi = API.getInstance(context);
    }

    public void get(String url, Map params, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        String tag = mApi.getTag(url, "get");
        if (null != params) {
            url = mApi.buildParams(url, params);
        }
        StringRequest request = new StringRequest(Request.Method.GET, url, listener, errorListener){
            @Override
            public Map<String, String> getHeaders() {
                Map map = new HashMap();
                map.put("Accept", "application/json");
                if (API.isLoggedIn(mApi.getContext())) {
                    String token = API.getSessionId(mApi.getContext());
                    map.put("sessionid", token);
                    //map.put("Authorization", token);
                    Log.d("SESSION_ID", token);
                }
                //map.put("Content-Type", "application/x-www-form-urlencoded");
                return map;
            }

        };
        request.setTag(tag);
        mApi.getRequestQueue().add(request);
    }

    public void post(String url, final Map params, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        StringRequest request = new StringRequest(Request.Method.POST, url, listener, errorListener){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map map = new HashMap();
                map.put("Accept", "application/json");
                if (API.isLoggedIn(mApi.getContext())) {
                    String token = API.getSessionId(mApi.getContext());
                    map.put("sessionid", token);
                    //map.put("Authorization", token);
                    Log.d("SESSION_ID", token);
                }
                //map.put("Content-Type", "application/x-www-form-urlencoded");
                return map;
            }
        };
        String tag = mApi.getTag(url, "post");
        request.setTag(tag);
        mApi.getRequestQueue().add(request);
    }

    public void put(String url, final Map params, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        StringRequest request = new StringRequest(Request.Method.PUT, url, listener, errorListener){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };
        String tag = mApi.getTag(url, "put");
        request.setTag(tag);
        mApi.getRequestQueue().add(request);
    }

    public void delete(String url, Map params, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        String tag = mApi.getTag(url, "delete");
        if (null != params) {
            url = mApi.buildParams(url, params);
        }
        StringRequest request = new StringRequest(Request.Method.DELETE, url, listener, errorListener);
        request.setTag(tag);
        mApi.getRequestQueue().add(request);
    }
}
