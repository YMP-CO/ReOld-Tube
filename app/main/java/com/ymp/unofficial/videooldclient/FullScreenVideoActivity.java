package com.ymp.unofficial.videooldclient;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.InputStream;
import java.util.List;
import java.util.Scanner;
//BETA (UNUSED) ACTIVITY
public class FullScreenVideoActivity extends Activity {


    private VideoView videoView;
    ;

    @Override
    public void onBackPressed() {
        videoView.stopPlayback();
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fullscreen_activity);


        videoView = findViewById(R.id.videoView2);


        videoView.requestFocus();

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.start();
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                }

            }
        });


        // Получите данные из Intent
        Intent intent = getIntent();
        String videoUrl = intent.getStringExtra("videoUrl");
        String videoTitle = intent.getStringExtra("title");
        String videoId = intent.getStringExtra("videoId");
        List<MainActivity.VideoInfo> comments = (List<MainActivity.VideoInfo>) intent.getSerializableExtra("comments");


        // Воспроизведение видео
        playVideo(videoUrl);


        // Теперь используйте videoId, чтобы загрузить полное описание видео и установить его в descriptionTextView
        // Например, вы можете использовать ваш метод handleVideoDetailsLoading(videoId)


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        } else {
            showSystemUI();
        }
    }

    private String convertStreamToString(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }
    // Ваш метод для отображения системного интерфейса


    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }


    private void playVideo(String videoUrl) {
        Uri uri = Uri.parse(videoUrl);

        // Создайте объект MediaController
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);

        // Установите MediaController для VideoView
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(uri);
        videoView.requestFocus();
        videoView.start();
    }

}

