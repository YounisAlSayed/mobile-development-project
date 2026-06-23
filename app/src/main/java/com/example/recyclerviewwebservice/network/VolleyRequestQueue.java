package com.example.recyclerviewwebservice.network;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public final class VolleyRequestQueue {
    private static VolleyRequestQueue instance;

    private final RequestQueue requestQueue;

    private VolleyRequestQueue(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public static synchronized VolleyRequestQueue getInstance(Context context) {
        if (instance == null) {
            instance = new VolleyRequestQueue(context);
        }
        return instance;
    }

    public <T> void add(Request<T> request) {
        requestQueue.add(request);
    }

    public void cancelAll(Object tag) {
        requestQueue.cancelAll(tag);
    }
}
