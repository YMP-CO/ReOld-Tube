package com.ymp.unofficial.videooldclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
//BETA ACTIVITY
public class VideoDashboardActivity extends Activity {
    private boolean isFullscreen = false;
    private LinearLayout LNL1;
    private LinearLayout LNL3;
    private LinearLayout LNL4;
    private MainActivity.CustomArrayAdapter adapter;
    private LinearLayout LNL2;
    private LinearLayout LNL5;
    private LinearLayout frameLayout;

private Button exitFullscreenButton;
    private List<MainActivity.VideoInfo> videoInfoList = new ArrayList<>();
    private TextView titleTextView;
    private ImageButton StaticsDashboardButton;
    private Button playButton;
private ImageView imageView2;
    private Button FullscreenButton;
    private Button button2;
    private static TextView descriptionTextView;
    private ListView commentsListView;
    private ImageView iconChannel;
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_INSTANCE_INVIDIOUS_LINK = "https://yt.cdaut.de/";
    private TextView viewCountText;
    private CustomArrayAdapter commentsAdapter;
    private List<MainActivity.VideoInfo> commentsList = new ArrayList<>();
private TextView channel;
private TextView date;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_video_beta);
frameLayout = findViewById(R.id.frameLayout);

       LNL1 = findViewById(R.id.LNL1);
        LNL2 = findViewById(R.id.LNL2);
        LNL3 = findViewById(R.id.LNL3);

        LNL5 = findViewById(R.id.LNL5);
imageView2 = findViewById(R.id.imageView2);

        titleTextView = findViewById(R.id.titleTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        commentsListView = findViewById(R.id.commentsListView);
        commentsAdapter = new CustomArrayAdapter(this, commentsList);
playButton = findViewById(R.id.playButton);
        viewCountText = findViewById(R.id.viewCountText);
        date = findViewById(R.id.date);
StaticsDashboardButton = findViewById(R.id.StaticsDashboardButton);


        Intent intent = getIntent();



        // Получите данные из Intent

        String videoUrl = intent.getStringExtra("videoUrl");
        String videoTitle = intent.getStringExtra("title");
        String videoId = intent.getStringExtra("videoId");
        String author = intent.getStringExtra("author");
        String publishedText = intent.getStringExtra("publishedText");
        String channelThumbnailUrl = intent.getStringExtra("channelThumbnailUrl");
        int viewCount = intent.getIntExtra("viewCount", 0);
        String pictureUrl = intent.getStringExtra("pictureUrl");
        if (videoInfoList != null && pictureUrl != null) {
            String thumbnailUrl = pictureUrl.replace("mqdefault.jpg", "sddefault.jpg");
            Picasso.get().load(thumbnailUrl).into(imageView2);

        }
        List<MainActivity.VideoInfo> comments = (List<MainActivity.VideoInfo>) intent.getSerializableExtra("comments");
        new LoadCommentsTask().execute(videoId);
        // Теперь используйте videoId, чтобы загрузить полное описание видео и установить его в descriptionTextView
        // Например, вы можете использовать ваш метод handleVideoDetailsLoading(videoId)

        handleVideoDetailsLoading(videoId);
        loadComments(comments);
        handleVideoCommentsLoading(videoId);

        // Загрузка комментариев
        StaticsDashboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                showVideoStatsDialog(videoId);
            }
        });
        String PublishedText = getString(R.string.Published);
        String viewsText = getString(R.string.views);
        date.setText(Html.fromHtml("<b>" +PublishedText+"</b>" + publishedText));


        // Воспроизведение видео
        viewCountText.setText(Html.fromHtml("<b>" +viewsText +"</b> " + String.valueOf(viewCount)));

playButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {

        openVideoPlayer(videoUrl);
    }
});

        // Отображение заголовка
        displayTitle(videoTitle);


    }
    private void openVideoInExternalPlayer(String videoUrl) {


        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
        intent.setDataAndType(Uri.parse(videoUrl), "video/*");
        startActivity(intent);
    }
    private void showVideoStatsDialog(String videoId) {
        // Call the method to fetch and display video stats
        getVideoStats(videoId);
    }
    private String convertStreamToString(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }
    // Ваш метод для отображения системного интерфейса
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showSystemUI() {
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            flags |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        getWindow().getDecorView().setSystemUiVisibility(flags);
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
    private void handleVideoDetailsLoading(String videoId) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedLink = sharedPreferences.getString(KEY_INSTANCE_INVIDIOUS_LINK, "https://yt.cdaut.de/");
        String apiUrl = savedLink + "api/v1/videos/" + videoId;
        // Извлекаем информацию об авторе и его обложке

        // Используйте AsyncTask или другие механизмы для выполнения HTTP-запроса в фоновом потоке
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    URL url = new URL(apiUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    try {
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        return convertStreamToString(in);
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                // Обработайте результат запроса
                if (result != null) {
                    // Разберите JSON-ответ и извлеките необходимые данные, например, описание видео
                    try {
                        JSONObject videoDetails = new JSONObject(result);
                        String videoDescription = videoDetails.getString("description");

                        // Теперь установите описание в TextView
                        TextView descriptionTextView = findViewById(R.id.descriptionTextView);
                        descriptionTextView.setText(videoDescription);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        }.execute();
    }


    private void handleVideoResponse(JSONArray response) {
        try {
            videoInfoList.clear();
            JSONObject videoObject = null;

            for (int i = 0; i < response.length(); i++) {
                videoObject = response.getJSONObject(i);

                if (videoObject.has("videoId")) {
                    MainActivity.VideoInfo videoInfo = new MainActivity.VideoInfo();
                    videoInfo.videoId = videoObject.getString("videoId");
                    SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    String savedLink = sharedPreferences.getString(KEY_INSTANCE_INVIDIOUS_LINK, "https://yt.cdaut.de/");
                    videoInfo.videoUrl = savedLink + "latest_version?id=" + videoInfo.videoId + "&itag=18";
                    videoInfo.thumbnailUrl = "https://img.youtube.com/vi/" + videoInfo.videoId + "/mqdefault.jpg";

                    if (videoObject.has("title")) {
                        videoInfo.title = videoObject.getString("title");
                    }

                    if (videoObject.has("description")) {
                        videoInfo.description = videoObject.getString("description");
                    }
                    videoInfoList.add(videoInfo);
                } else {
                    Log.e("Video Error", "Video object doesn't contain 'videoId' field");
                }
            }

            updateListView();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void updateListView() {
        adapter.clear();

        for (MainActivity.VideoInfo videoInfo : videoInfoList) {
            adapter.add(videoInfo);
        }

        adapter.notifyDataSetChanged();
    }
    public void hideToolBr(){

        // BEGIN_INCLUDE (get_current_ui_flags)
        // The UI options currently enabled are represented by a bitfield.
        // getSystemUiVisibility() gives us that bitfield.
        int uiOptions = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        }
        int newUiOptions = uiOptions;
        // END_INCLUDE (get_current_ui_flags)
        // BEGIN_INCLUDE (toggle_ui_flags)
        boolean isImmersiveModeEnabled =
                ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if (isImmersiveModeEnabled) {

        } else {

        }

        // Navigation bar hiding:  Backwards compatible to ICS.
        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        // Immersive mode: Backward compatible to KitKat.
        // Note that this flag doesn't do anything by itself, it only augments the behavior
        // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
        // all three flags are being toggled together.
        // Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
        // Sticky immersive mode differs in that it makes the navigation and status bars
        // semi-transparent, and the UI flag does not get cleared when the user interacts with
        // the screen.
        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
        }
        //END_INCLUDE (set_ui_flags)

    }

    private void displayTitle(String title) {
        titleTextView.setText(title);
    }

    private void loadComments(List<MainActivity.VideoInfo> comments) {
        commentsList.clear();
        if (comments != null) {
            commentsList.addAll(comments);
        }
        commentsAdapter.notifyDataSetChanged();
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

    private void handleVideoCommentsLoading(String videoId) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedLink = sharedPreferences.getString(KEY_INSTANCE_INVIDIOUS_LINK, "https://yt.cdaut.de/");
        String apiUrl = savedLink + "api/v1/comments/" + videoId;

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    URL url = new URL(apiUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    try {
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        return convertStreamToString(in);
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            protected void onPostExecute(String result) {
                if (result != null) {
                    Log.d("API Response", result);

                    try {
                        JSONObject response = new JSONObject(result);

                        if (response.has("comments")) {
                            JSONArray commentsArray = response.getJSONArray("comments");

                            List<SpannableString> commentList = new ArrayList<>();

                            for (int i = 0; i < commentsArray.length(); i++) {
                                JSONObject commentObject = commentsArray.getJSONObject(i);
                                String author = commentObject.getString("author");
                                String commentContent = commentObject.getString("content");

                                // Форматирование имени пользователя как жирного текста
                                SpannableString formattedComment = new SpannableString(author + ": " + commentContent);
                                formattedComment.setSpan(new StyleSpan(Typeface.BOLD), 0, author.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                commentList.add(formattedComment);
                            }

                            Log.d("Comment List", commentList.toString());

                            ArrayAdapter<SpannableString> adapter = new ArrayAdapter<>(
                                    VideoDashboardActivity.this,
                                    android.R.layout.simple_list_item_1,
                                    commentList
                            );

                            commentsListView.setAdapter(adapter);
                        } else {
                            Log.e("LoadCommentsTask", "No comments array found in the JSON response");
                            // Обработайте ситуацию, когда массив "comments" не существует
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("LoadCommentsTask", "Failed to retrieve comments. Result is null.");
                }
            }


        }.execute();
    }

    // ... остальной код в VideoDashboardActivity

    private class LoadCommentsTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            // Вы можете обработать предварительные задачи здесь, если это необходимо
        }

        @Override
        protected String doInBackground(String... params) {
            String videoId = params[0];
            try {
                return loadComments(videoId);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void onCancelled() {
            // Вы можете обработать отмененное состояние здесь, если это необходимо
        }

        private String loadComments(String videoId) throws InterruptedException {
            // Ваш код для загрузки комментариев здесь
            return String.valueOf(R.string.Loading);
        }
    }

}

