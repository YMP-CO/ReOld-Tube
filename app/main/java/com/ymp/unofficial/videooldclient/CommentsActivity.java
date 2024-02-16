package com.ymp.unofficial.videooldclient;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;



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

public class CommentsActivity extends Activity {
    //UNUSED ACTIVITY
    private TextView errorTextView;
    private ListView commentsListView;
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_INSTANCE_INVIDIOUS_LINK = "https://yt.cdaut.de/";
    public static final String EXTRA_VIDEO_ID = "extra_video_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        errorTextView = findViewById(R.id.errorComTextView);
        commentsListView = findViewById(R.id.commentsListView);

        // Получите ID видео из Intent
        String videoId = getIntent().getStringExtra(EXTRA_VIDEO_ID);

        handleVideoCommentsLoading(videoId);
    }
    private void loadComments(String videoId) {
        // Здесь вы можете запустить AsyncTask для загрузки комментариев
        new LoadCommentsTask().execute(videoId);
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

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    Log.d("API Response", result);

                    try {
                        JSONObject response = new JSONObject(result);

                        if (response.has("comments")) {
                            JSONArray commentsArray = response.getJSONArray("comments");

                            List<String> commentList = new ArrayList<>();

                            for (int i = 0; i < commentsArray.length(); i++) {
                                JSONObject commentObject = commentsArray.getJSONObject(i);
                                String author = commentObject.getString("author");
                                String commentContent = commentObject.getString("content");

                                String formattedComment = author + ": " + commentContent;
                                commentList.add(formattedComment);
                            }

                            Log.d("Comment List", commentList.toString());

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    CommentsActivity.this,
                                    android.R.layout.simple_list_item_1,
                                    commentList
                            );

                            commentsListView.setAdapter(adapter);
                        } else {
                            Log.e("LoadCommentsTask", "No comments array found in the JSON response");
                            // Handle the case where "comments" array does not exist
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

    private String convertStreamToString(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
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