package com.bpow.malupdater.request;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private static RequestQueue queue = null;

    public static void sendRequest(Context context,
                                   int method,
                                   String url,
                                   final Map<String, String> params,
                                   final HashMap<String, String> headers,
                                   Response.Listener<String> listener){

        if (queue == null){
            queue = Volley.newRequestQueue(context);
        }

        StringRequest stringRequest = new StringRequest(
                method,
                url,
                listener,
                error -> {
                    try {
                        System.out.println(new String(error.networkResponse.data, StandardCharsets.UTF_8));
                    }catch (NullPointerException e){
                        System.out.println(error.toString());
                        System.out.println(Arrays.toString(error.getStackTrace()));
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() {
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                return headers;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 5, 2.0F));

        queue.add(stringRequest);
    }
}
