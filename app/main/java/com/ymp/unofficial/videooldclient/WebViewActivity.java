package com.ymp.unofficial.videooldclient;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

//UNUSED ACTIVITY

public class WebViewActivity extends Activity {

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        String query = getIntent().getStringExtra("SEARCH_QUERY");
        String url = "https://inv.tux.pizza/search?q=" + query;
        webView = findViewById(R.id.webView);

        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Проверка, является ли URL ссылкой на поиск
                if (url != null && url.contains("https://inv.tux.pizza/search?q=")) {
                    // Просто продолжаем загрузку в WebView
                    view.loadUrl(url);
                    return false;
                } else if (url != null && url.contains("https://inv.tux.pizza/watch?v=")) {
                    // Извлекаем ID видео
                    String videoId = url.substring(url.lastIndexOf("=") + 1);

                    // Строим новый URL с форматом https://inv.tux.pizza/latest_version?id=[ID Видео]&itag=22
                    String newUrl = "https://inv.tux.pizza/latest_version?id=" + videoId + "&itag=22";

                    // Открываем новый URL
                    openVideoPlayer(newUrl);
                    return true;
                } else {
                    // В противном случае продолжаем загрузку в WebView
                    view.loadUrl(url);
                    return false;
                }
            }
        });

        // Ваш URL для загрузки страницы
        String initialUrl = "https://inv.tux.pizza/search?q=" ;
        webView.loadUrl(initialUrl);
    }

    private void openVideoPlayer(String videoUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(videoUrl), "video/mp4");
        startActivity(intent);
    }
}
