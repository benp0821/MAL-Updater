package com.bpow.malupdater.request;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Pair;

import com.android.volley.Request;
import com.android.volley.Response;
import com.bpow.malupdater.activity.UserListActivity;

import java.util.HashMap;
import java.util.Map;

public class UserListRequest {

    private static final int LIMIT_PER_REQUEST = 100;

    private static Pair<String, HashMap<String, String>> constructUserRequest(Context context, String user, String endOfUrl, HashMap<String, String> queryParameters){
        final Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("api.myanimelist.net")
                .appendPath("v2")
                .appendPath("users")
                .appendPath(user);

        if (endOfUrl != null) {
            builder.appendPath(endOfUrl);
        }

        for (Map.Entry<String, String> queryParameter : queryParameters.entrySet()){
            builder.appendQueryParameter(queryParameter.getKey(), queryParameter.getValue());
        }

        String url = builder.build().toString();

        final HashMap<String, String> headers = new HashMap<>();
        SharedPreferences sharedPref = context.getSharedPreferences("malupdaterprefs", Context.MODE_PRIVATE);
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", sharedPref.getString("token_type", "") + " " + sharedPref.getString("access_token", ""));

        return new Pair<>(url, headers);
    }

    public static void getUserName(Context context, final Response.Listener<String> listener){
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("fields", "name");

        Pair<String, HashMap<String, String>> urlWithHeaders = constructUserRequest(context, "@me", null, queryParams);

        HttpRequest.sendRequest(context, Request.Method.GET, urlWithHeaders.first, null, urlWithHeaders.second, listener);
    }

    public static void getUserList(final Context context, String user, UserListActivity.ListType listType, Pair<String, String> status,
                                   Pair<String, String> sort, int pageNum,
                                   final Response.Listener<String> listener){
        HashMap<String, String> queryParams = new HashMap<>();
        if (status != null && status.second != null) {
            queryParams.put("status", status.second);
        }
        if (sort != null && sort.second != null) {
            queryParams.put("sort", sort.second);
        }
        queryParams.put("limit", Integer.toString(LIMIT_PER_REQUEST));
        queryParams.put("offset", Integer.toString((pageNum * LIMIT_PER_REQUEST) - LIMIT_PER_REQUEST));

        Pair<String, HashMap<String, String>> urlWithHeaders = null;
        if (listType == UserListActivity.ListType.ANIME) {
            queryParams.put("fields", "alternative_titles{synonyms,en,ja},genres,media_type,status,num_episodes,start_season{year,season},source,average_episode_duration,rating,my_list_status{status,score,num_watched_episodes}");
            urlWithHeaders = constructUserRequest(context, user, "animelist", queryParams);
        }else if (listType == UserListActivity.ListType.MANGA){
            queryParams.put("fields", "alternative_titles{synonyms,en,ja},genres,media_type,status,num_chapters,num_volumes,my_list_status{status,score,num_volumes_read, num_chapters_read}");
            urlWithHeaders = constructUserRequest(context, user, "mangalist", queryParams);
        }

        if (urlWithHeaders != null) {
            HttpRequest.sendRequest(context, Request.Method.GET, urlWithHeaders.first, null, urlWithHeaders.second, listener);
        }
    }
}
