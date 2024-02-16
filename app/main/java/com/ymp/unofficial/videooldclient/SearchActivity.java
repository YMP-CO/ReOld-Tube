// SearchActivity.java
package com.ymp.unofficial.videooldclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends Activity {
    private MainActivity.VideoInfo selectedVideoInfo;
    private CustomArrayAdapter adapter;
    private List<MainActivity.VideoInfo> videoInfoList = new ArrayList<>();
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_INSTANCE_INVIDIOUS_LINK = "https://yt.cdaut.de/";
    private List<MainActivity.VideoInfo> searchResults = new ArrayList<>();

    @Override

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);

        EditText searchEditText = findViewById(R.id.searchEditText);
        Button searchButton = findViewById(R.id.searchButton);
        ListView listView = findViewById(R.id.listViewSearchResults);

        adapter = new CustomArrayAdapter(this, new ArrayList<>(), R.layout.custom_list_view_tablet);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                MainActivity.VideoInfo selectedVideoInfo = videoInfoList.get(position);

                // Покажите окно выбора разрешения
                openVideoDetails(selectedVideoInfo);
            }
        });



        searchButton.setOnClickListener(v -> {
            String searchQuery = searchEditText.getText().toString();
            if (!searchQuery.isEmpty()) {
                performSearch(searchQuery);
            }
        });

    }
    private void openVideoDetails(MainActivity.VideoInfo videoInfo) {
        Intent intent22 = new Intent(this, VideoDashboardActivity.class);
        intent22.putExtra("videoUrl", videoInfo.videoUrl);
        intent22.putExtra("title", videoInfo.title);
        intent22.putExtra("pictureUrl", videoInfo.thumbnailUrl);
        intent22.putExtra("publishedText", videoInfo.publishedText);
        intent22.putExtra("videoId", videoInfo.videoId);
        intent22.putExtra("viewCount", videoInfo.viewCount);
        intent22.putExtra("author", videoInfo.author);
        // Загрузите комментарии с помощью API или храните их в объекте VideoInfo
        // intent.putExtra("comments", videoInfo.comments);
        startActivity(intent22);
    }


    private void performSearch(String searchQuery) {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String savedLink = sharedPreferences.getString(KEY_INSTANCE_INVIDIOUS_LINK, "https://yt.cdaut.de/");
            String encodedQuery = URLEncoder.encode(searchQuery, "UTF-8");
            String url = savedLink + "api/v1/search?q=" + encodedQuery;

            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                    Request.Method.GET,
                    url,
                    null,
                    response -> handleSearchResponse(response),
                    error -> Log.e("SearchActivity", "Error performing search: " + error.toString())
            );

            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(jsonArrayRequest);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void handleSearchResponse(JSONArray response) {
        try {
            searchResults.clear();

            for (int i = 0; i < response.length(); i++) {
                JSONObject videoObject = response.getJSONObject(i);

                if (videoObject.has("videoId")) {
                    MainActivity.VideoInfo videoInfo = new MainActivity.VideoInfo();
                    videoInfo.videoId = videoObject.getString("videoId");
                    SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    String savedLink = sharedPreferences.getString(KEY_INSTANCE_INVIDIOUS_LINK, "https://yt.cdaut.de/");
                    videoInfo.videoUrl = savedLink + "latest_version?id=" + videoInfo.videoId + "&itag=18&region=RU";
                    videoInfo.thumbnailUrl = "https://i.ytimg.com/vi/" + videoInfo.videoId + "/hqdefault.jpg";

                    if (videoObject.has("title")) {
                        videoInfo.title = videoObject.getString("title");
                    }
                    if (videoObject.has("publishedText")) {
                        videoInfo.publishedText = videoObject.getString("publishedText");
                    }
                    if (videoObject.has("viewCount")) {
                        videoInfo.viewCount = videoObject.getInt("viewCount");
                    }

                    videoInfoList.add(videoInfo); // Add videoInfo to videoInfoList
                    searchResults.add(videoInfo);
                }
            }

            updateListView();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("SearchActivity", "Error parsing JSON response: " + e.toString());
        }
    }


    private void getVideoStats(String videoId) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedLink = sharedPreferences.getString(KEY_INSTANCE_INVIDIOUS_LINK, "https://yt.cdaut.de/");
        String apiUrl = savedLink + "api/v1/videos/" + videoId;

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, apiUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Parse the JSON response and get the desired statistics
                            int likeCount = response.getInt("likeCount");
                            int dislikeCount = response.getInt("viewCount");
                            int timeCount = response.getInt("lengthSeconds");
                            String subCountText = response.getString("subCountText");
                            String genre = response.getString("genre");

                            String author = response.getString("author");
                            String publishedText = response.getString("publishedText");

                            // Update the UI or handle the statistics as needed
                            showStatsDialog(likeCount, dislikeCount, author, subCountText, publishedText, timeCount, genre);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            // Handle the case when there is an error parsing the JSON response
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
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonRequest);
    }

    private void updateListView() {
        adapter.clear();

        for (MainActivity.VideoInfo videoInfo : searchResults) {
            adapter.add(videoInfo);
        }

        adapter.notifyDataSetChanged();

    }


    private void showStatsDialog(int likeCount, int viewCount, String author, String subCountText, String publishedText, int timeCount, String genre) {
        // Construct the message
        String viewsText = getString(R.string.views);
        String channelNameText = getString(R.string.Channel_name);
        String CountText = getString(R.string.Number_of_subscribers);
        String genreText = getString(R.string.Genre);

        String statsMessage =
                "<b>" + viewsText + "</b>" + viewCount + "<br/>" +
                        "<b>" + channelNameText + "</b>" + author + "<br/>" +
                        "<b>" + CountText + "</b>" + subCountText + "<br/>" +
                        "<b>" + genreText + "</b>" + genre;

// Show the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View customLayout = getLayoutInflater().inflate(R.layout.progress_dialog_layout, null);
        builder.setView(customLayout);
        builder.setTitle(R.string.Video_statistics);


        // Set the message using fromHtml() to enable HTML formatting
        TextView messageTextView = customLayout.findViewById(R.id.messageTextView);
        messageTextView.setText(Html.fromHtml(statsMessage));

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Show the AlertDialog
        builder.create().show();
    }

    public static class VideoInfo {
        String videoId;
        String videoUrl;
        String thumbnailUrl;
        String title;
        String description;
    }
    private void playVideoWithResolution(MainActivity.VideoInfo videoInfo, int selectedItag) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedLink = sharedPreferences.getString(KEY_INSTANCE_INVIDIOUS_LINK, "https://yt.cdaut.de/");
        String videoUrl = savedLink + "latest_version?id=" + videoInfo.videoId + "&itag=" + selectedItag;

        // Здесь вставьте код для воспроизведения видео с использованием выбранного URL
        // Например, вы можете использовать Intent для открытия встроенного видеоплеера
        Uri uri = Uri.parse(videoUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "video/*");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.e("Video Error", "No activity to handle video playback");
        }
    }

    private void openVideoPlayer(String videoUrl) {
        Uri uri = Uri.parse(videoUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "video/*");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.e("Video Error", "No activity to handle video playback");
        }


    }
    public class CustomArrayAdapter extends ArrayAdapter<MainActivity.VideoInfo> {

        private int layoutResource; // Добавлено поле для хранения макета

        public CustomArrayAdapter(@NonNull SearchActivity context, ArrayList<VideoInfo> videoInfoList, int layoutResource) {
            super(context, 0);
            this.layoutResource = layoutResource; // Устанавливаем макет
        }

        private void getLikeCountForVideo(String videoId) {
            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String savedLink = sharedPreferences.getString(KEY_INSTANCE_INVIDIOUS_LINK, "https://yt.cdaut.de/");
            String apiUrl = savedLink + "api/v1/videos/" + videoId;

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
        private void showVideoStatsDialog(MainActivity.VideoInfo videoInfo) {
            // Call the method to fetch and display video stats
            getVideoStats(videoInfo.videoId);
        }
        private void updateLikeCount(int likeCount) {
            // Update the UI, for example, set text in the corresponding TextView
            notifyDataSetChanged();
        }
        private void showResolutionDialog(final MainActivity.VideoInfo videoInfo) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
            CharSequence[] qualityOptions = {getString(R.string.medium_quality), getString(R.string.high_quality)};
            builder.setTitle(R.string.select_video_quality);

            builder.setItems(qualityOptions, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int selectedItag = (which == 0) ? 18 : 22;
                    playVideoWithResolution(videoInfo, selectedItag);
                }
            });
            builder.create().show();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                // Используем нужный макет в зависимости от устройства
                convertView = LayoutInflater.from(getContext()).inflate(layoutResource, parent, false);
            }

            ImageView thumbnailImageView = convertView.findViewById(R.id.thumbnailImageView);
            TextView titleTextView = convertView.findViewById(R.id.titleTextView);

            Button playButton = convertView.findViewById(R.id.playButton);
            MainActivity.VideoInfo videoInfo = getItem(position);

            if (videoInfo != null && videoInfo.thumbnailUrl != null) {
                String thumbnailUrl = videoInfo.thumbnailUrl.replace("mqdefault.jpg", "maxresdefault.jpg");
                Picasso.get().load(thumbnailUrl).into(thumbnailImageView);
            }

            if (videoInfo != null && videoInfo.title != null) {
                titleTextView.setText(videoInfo.title);
            }

            // Внутри CustomArrayAdapter, в методе getView

            Button staticsButton = convertView.findViewById(R.id.staticsButton);

            staticsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.VideoInfo videoInfo = getItem(position);
                    if (videoInfo != null) {
                        // Call the method to fetch and display video stats
                        showVideoStatsDialog(videoInfo);
                    }
                }
            });


            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedVideoInfo = videoInfoList.get(position);

                    // Покажите окно выбора разрешения
                    showResolutionDialog(selectedVideoInfo);
                }
            });


            return convertView;
        }
    }



    private void showResolutionDialog(final MainActivity.VideoInfo videoInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        CharSequence[] qualityOptions = {getString(R.string.medium_quality), getString(R.string.high_quality)};
        builder.setTitle(R.string.select_video_quality);

        builder.setItems(qualityOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selectedItag = (which == 0) ? 18 : 22;
                playVideoWithResolution(videoInfo, selectedItag);
            }
        });
        builder.create().show();
    }

    private void showVideoStatsDialog(VideoInfo videoInfo) {
            // Call the method to fetch and display video stats
            getVideoStats(videoInfo.videoId);
        }

    private void showVideoCommentsDialog(Context context, MainActivity.VideoInfo videoInfo) {
        Intent intent = new Intent(context, CommentsActivity.class);
        intent.putExtra(CommentsActivity.EXTRA_VIDEO_ID, videoInfo.videoId);
        context.startActivity(intent);
    }


    // Update UI with received like count



    // ... Ваш другой код ...

}




