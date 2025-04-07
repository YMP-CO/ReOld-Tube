package com.ymp.reoldproject.reoldtube;

import android.app.Application;
import android.graphics.BitmapFactory;


public class InvidiousApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ImageLoader.getInstance().setPlaceholder(R.drawable.placeholder_image);
    }
}