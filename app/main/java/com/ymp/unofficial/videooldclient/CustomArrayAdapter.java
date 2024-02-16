package com.ymp.unofficial.videooldclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class CustomArrayAdapter extends ArrayAdapter<MainActivity.VideoInfo> {
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_INSTANCE_INVIDIOUS_LINK = "https://yt.cdaut.de/";
    public CustomArrayAdapter(@NonNull VideoDashboardActivity context, List<MainActivity.VideoInfo> videoInfoList) {
        super(context, 0, videoInfoList);
    }

    private void getLikeCountForVideo(String videoId) {

        String apiUrl = "https://yt.cdaut.de/api/v1/videos/" + videoId;

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, apiUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int likeCount = response.getInt("likeCount");
                            // Update the UI with the received like count
                            updateLikeCount(likeCount);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            // Handle the case when there is no like count in the API response
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        // Handle network errors or other errors during the request
                    }
                });

        // Add the request to the Volley queue
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonRequest);
    }

    private void updateLikeCount(int likeCount) {
        // Update the UI, for example, set text in the corresponding TextView
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_list_view_tablet, parent, false);
        }

        ImageView thumbnailImageView = convertView.findViewById(R.id.thumbnailImageView);
        TextView titleTextView = convertView.findViewById(R.id.titleTextView);


        MainActivity.VideoInfo videoInfo = getItem(position);

        if (videoInfo != null && videoInfo.thumbnailUrl != null) {
            String thumbnailUrl = videoInfo.thumbnailUrl.replace("mqdefault.jpg", "maxresdefault.jpg");
            Picasso.get().load(thumbnailUrl).into(thumbnailImageView);
        }

        if (videoInfo != null && videoInfo.title != null) {
            titleTextView.setText(videoInfo.title);
        }


        return convertView;
    }
}
