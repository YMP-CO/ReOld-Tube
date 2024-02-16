package com.ymp.unofficial.videooldclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

import org.conscrypt.Conscrypt;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;

public class MainActivity extends Activity {
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_INSTANCE_INVIDIOUS_LINK = "https://yt.cdaut.de/";


    private List<VideoInfo> videoInfoList = new ArrayList<>();
    private Button settingsButton;
    private TextView textView3;
    private VideoInfo selectedVideoInfo;
    private ListView listView;
    private CustomArrayAdapter adapter;
    private int selectedItag = 18;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String selectedInterface = preferences.getString(getString(R.string.pref_key_interface), "");
        // Определение типа устройства (планшет или мобильное)
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);




        // Выбор макета в зависимости от типа устройства
        int layoutResId = isTablet ? R.layout.activity_main : R.layout.activity_main_mobile;

        // Установка разметки для активности
        setContentView(layoutResId);

        // Инициализация ListView
        listView = findViewById(isTablet ? R.id.listViewTablet : R.id.listViewMobile);

        // Если настройка не была установлена, то это первый запуск
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            if (selectedInterface.isEmpty()) {
                // Определяем, является ли устройство планшетом


                // Устанавливаем соответствующий интерфейс
                if (isTablet) {
                    setContentView(R.layout.activity_main);
                    selectedInterface = getString(R.string.interface_tablet);
                } else {
                    setContentView(R.layout.activity_main_mobile);
                    selectedInterface = getString(R.string.interface_mobile);
                }

                // Сохраняем выбор в настройках
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(getString(R.string.pref_key_interface), selectedInterface);
                editor.apply();
            } else {
                // Загружаем ранее выбранный интерфейс
                if (selectedInterface.equals(getString(R.string.interface_tablet))) {
                    setContentView(R.layout.activity_main);
                } else {
                    setContentView(R.layout.activity_main_mobile);
                }
            }
        }

        // Init Conscrypt
        Provider conscrypt = Conscrypt.newProvider();

        // Add as provider
        Security.insertProviderAt(conscrypt, 1);

        // Init OkHttp
        OkHttpClient.Builder okHttpBuilder = new OkHttpClient()
                .newBuilder()
                .connectionSpecs(Collections.singletonList(ConnectionSpec.RESTRICTED_TLS));


        listView = findViewById(isTablet ? R.id.listViewTablet : R.id.listViewMobile);


        textView3 = findViewById(R.id.textView3);

        adapter = new CustomArrayAdapter(MainActivity.this, new ArrayList<>(), isTablet ? R.layout.custom_list_view_tablet : R.layout.custom_list_view_mobile);


        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedVideoInfo = videoInfoList.get(position);

                openVideoDetails(selectedVideoInfo);
            }
        });

        try {
            X509TrustManager tm = Conscrypt.getDefaultX509TrustManager();
            SSLContext sslContext = SSLContext.getInstance("TLS", conscrypt);
            sslContext.init(null, new TrustManager[]{tm}, null);
            okHttpBuilder.sslSocketFactory(new InternalSSLSocketFactory(sslContext.getSocketFactory()), tm);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Button exitButton = findViewById(R.id.ButtonExit);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
Button TrendsButton = findViewById(R.id.PopularButton);
TrendsButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Toast.makeText(MainActivity.this, R.string.Opened_alert, Toast.LENGTH_SHORT);
    }
});
        Button searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSearchActivity();
            }
        });
        Button settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettingsActivity();
            }

        });

        fetchPopularVideos();
    }



    private void openVideoDetails(VideoInfo videoInfo) {
        Intent intent = new Intent(this, VideoDashboardActivity.class);
        intent.putExtra("videoUrl", videoInfo.videoUrl);
        intent.putExtra("title", videoInfo.title);
        intent.putExtra("pictureUrl", videoInfo.thumbnailUrl);
        intent.putExtra("publishedText", videoInfo.publishedText);
        intent.putExtra("videoId", videoInfo.videoId);
        intent.putExtra("viewCount", videoInfo.viewCount);
        intent.putExtra("author", videoInfo.author);
        // Загрузите комментарии с помощью API или храните их в объекте VideoInfo
        // intent.putExtra("comments", videoInfo.comments);
        startActivity(intent);
    }
    private void openSearchActivity() {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }
    private void openSettingsActivity() {
        Intent intent2 = new Intent(this, Settings.class);
        startActivity(intent2);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            finish();
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    private void fetchPopularVideos() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedLink = sharedPreferences.getString(KEY_INSTANCE_INVIDIOUS_LINK, "https://yt.cdaut.de/");
        String url = savedLink + "api/v1/trending?region=" + getString(R.string.lang);


        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        handleVideoResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley Error", "Error fetching popular videos: " + error.getMessage());
                    }
                }
        );

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest);
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
    private void showResolutionDialog(final VideoInfo videoInfo) {
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

    private void playVideoWithResolution(VideoInfo videoInfo, int selectedItag) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedLink = sharedPreferences.getString(KEY_INSTANCE_INVIDIOUS_LINK, "https://yt.cdaut.de/");
        String videoUrl = savedLink + "latest_version?id=" + videoInfo.videoId + "&itag=" + selectedItag;

        Uri uri = Uri.parse(videoUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "video/*");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.e("Video Error", "No activity to handle video playback");
        }
    }

    private void handleVideoResponse(JSONArray response) {
        try {
            videoInfoList.clear();

            for (int i = 0; i < response.length(); i++) {
                JSONObject videoObject = response.getJSONObject(i);
                SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                String savedLink = sharedPreferences.getString(KEY_INSTANCE_INVIDIOUS_LINK, "https://yt.cdaut.de/");
                if (videoObject.has("videoId")) {
                    VideoInfo videoInfo = new VideoInfo();
                    videoInfo.videoId = videoObject.getString("videoId");
                    videoInfo.videoUrl = savedLink + "latest_version?id=" + videoInfo.videoId + "&itag=22";
                    videoInfo.thumbnailUrl = "https://img.youtube.com/vi/" + videoInfo.videoId + "/mqdefault.jpg";

                    if (videoObject.has("title")) {
                        videoInfo.title = videoObject.getString("title");
                    }

                    if (videoObject.has("description")) {
                        videoInfo.description = videoObject.getString("description");
                    }
                    if (videoObject.has("publishedText")) {
                        videoInfo.publishedText = videoObject.getString("publishedText");
                    }

                    if (videoObject.has("viewCount")) {
                        videoInfo.viewCount = videoObject.getInt("viewCount");
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

        for (VideoInfo videoInfo : videoInfoList) {
            adapter.add(videoInfo);
        }

        adapter.notifyDataSetChanged();
    }

    public static class VideoInfo {
        String videoId;
        String videoUrl;
        String thumbnailUrl;
        String title;
        String author;
        String description;
        String channelThumbnailUrl;
String publishedText;
        int viewCount;
    }


    public class CustomArrayAdapter extends ArrayAdapter<VideoInfo> {

        private int layoutResource; // Добавлено поле для хранения макета

        public CustomArrayAdapter(@NonNull MainActivity context, ArrayList<VideoInfo> videoInfoList, int layoutResource) {
            super(context, 0, videoInfoList);
            this.layoutResource = layoutResource; // Устанавливаем макет
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
            VideoInfo videoInfo = getItem(position);

            if (videoInfo != null && videoInfo.thumbnailUrl != null) {
                String thumbnailUrl = videoInfo.thumbnailUrl.replace("mqdefault.jpg", "mqdefault.jpg");
            Picasso.get().load(thumbnailUrl).into(thumbnailImageView);

            }

            if (videoInfo != null && videoInfo.title != null) {
                titleTextView.setText(videoInfo.title);
            }
            Button staticsButton = convertView.findViewById(R.id.staticsButton);

            staticsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VideoInfo videoInfo = getItem(position);
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


    private void shareVideo(String videoId) {
        // Создаем ссылку с использованием только ID видео
        String videoUrl = "https://youtube.com/watch?" + videoId;

        // Создаем Intent для отправки данных
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, videoUrl);

        // Открываем диалоговое окно для выбора приложения для отправки
        startActivity(Intent.createChooser(shareIntent, getString(R.string.Share)));

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
    private void showVideoStatsDialog(VideoInfo videoInfo) {
        // Call the method to fetch and display video stats
        getVideoStats(videoInfo.videoId);
    }

    private void showVideoCommentsDialog(Context context, VideoInfo videoInfo) {
        Intent intent = new Intent(context, CommentsActivity.class);
        intent.putExtra(CommentsActivity.EXTRA_VIDEO_ID, videoInfo.videoId);
        context.startActivity(intent);
    }
}
