package com.example.recyclerviewwebservice.network;

import android.content.Context;
import android.net.Uri;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.recyclerviewwebservice.model.Product;

import java.util.List;

public class OpenLibraryProductApi {
    public static final String BASE_URL = "https://openlibrary.org/search.json";
    public static final String SEARCH_QUERY = "subject:fiction";
    private static final String FIELDS =
            "key,title,author_name,cover_i,first_publish_year,edition_count";
    private static final String REQUEST_TAG = "OPEN_LIBRARY_PRODUCTS";
    private static final int REQUEST_TIMEOUT_MS = 30_000;

    private final VolleyRequestQueue requestQueue;
    private final OpenLibraryProductParser parser = new OpenLibraryProductParser();

    public interface Callback {
        void onSuccess(List<Product> products, long totalItems);

        void onError(String message);
    }

    public OpenLibraryProductApi(Context context) {
        requestQueue = VolleyRequestQueue.getInstance(context);
    }

    public void fetchProducts(int page, int pageSize, Callback callback) {
        requestQueue.cancelAll(REQUEST_TAG);

        String endpoint = Uri.parse(BASE_URL)
                .buildUpon()
                .appendQueryParameter("q", SEARCH_QUERY)
                .appendQueryParameter("fields", FIELDS)
                .appendQueryParameter("page", String.valueOf(page))
                .appendQueryParameter("limit", String.valueOf(pageSize))
                .build()
                .toString();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                endpoint,
                null,
                response -> {
                    try {
                        OpenLibraryProductParser.Result result = parser.parse(response.toString());
                        callback.onSuccess(result.getProducts(), result.getTotalItems());
                    } catch (Exception exception) {
                        callback.onError(errorMessage(exception.getMessage()));
                    }
                },
                error -> callback.onError(errorMessage(error))
        );

        request.setTag(REQUEST_TAG);
        request.setRetryPolicy(new DefaultRetryPolicy(
                REQUEST_TIMEOUT_MS,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        requestQueue.add(request);
    }

    private String errorMessage(VolleyError error) {
        if (error.networkResponse != null) {
            return "Open Library returned HTTP " + error.networkResponse.statusCode;
        }
        if (error instanceof TimeoutError) {
            return "The request timed out";
        }
        if (error instanceof NoConnectionError) {
            return "No internet connection";
        }
        return errorMessage(error.getMessage());
    }

    private String errorMessage(String message) {
        return message == null || message.trim().isEmpty()
                ? "Unable to retrieve products"
                : message;
    }

    public void shutdown() {
        requestQueue.cancelAll(REQUEST_TAG);
    }
}
