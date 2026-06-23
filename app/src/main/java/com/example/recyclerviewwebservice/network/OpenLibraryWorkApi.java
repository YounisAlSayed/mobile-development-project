package com.example.recyclerviewwebservice.network;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

public class OpenLibraryWorkApi {
    private static final String OPEN_LIBRARY_URL = "https://openlibrary.org";
    private static final String REQUEST_TAG = "OPEN_LIBRARY_WORK_DETAILS";
    private static final int REQUEST_TIMEOUT_MS = 30_000;

    private final VolleyRequestQueue requestQueue;
    private final OpenLibraryWorkParser parser = new OpenLibraryWorkParser();

    public interface Callback {
        void onSuccess(String description);

        void onError();
    }

    public OpenLibraryWorkApi(Context context) {
        requestQueue = VolleyRequestQueue.getInstance(context);
    }

    public void fetchDescription(String workId, Callback callback) {
        requestQueue.cancelAll(REQUEST_TAG);
        String normalizedId = workId.startsWith("/") ? workId : "/works/" + workId;
        String endpoint = OPEN_LIBRARY_URL + normalizedId + ".json";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                endpoint,
                null,
                response -> {
                    String description = parser.parseDescription(response.toString());
                    if (description.isEmpty()) {
                        callback.onError();
                    } else {
                        callback.onSuccess(description);
                    }
                },
                error -> callback.onError()
        );

        request.setTag(REQUEST_TAG);
        request.setRetryPolicy(new DefaultRetryPolicy(
                REQUEST_TIMEOUT_MS,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        requestQueue.add(request);
    }

    public void shutdown() {
        requestQueue.cancelAll(REQUEST_TAG);
    }
}
