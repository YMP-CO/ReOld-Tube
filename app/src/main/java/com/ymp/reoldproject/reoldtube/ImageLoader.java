package com.ymp.reoldproject.reoldtube;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ImageLoader {
    private static final String TAG = "ImageLoader";
    private static ImageLoader instance;
    private LinkedHashMap<String, Bitmap> memoryCache;
    private static final int CACHE_SIZE = 20;

    private Map<String, LoaderTask> tasks;

    private Bitmap placeholderBitmap;
    private int placeholderResId;

    public static synchronized ImageLoader getInstance() {
        if (instance == null) {
            instance = new ImageLoader();
        }
        return instance;
    }

    private ImageLoader() {
        memoryCache = new LinkedHashMap<String, Bitmap>(CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Bitmap> eldest) {
                return size() > CACHE_SIZE;
            }
        };

        tasks = new HashMap<String, LoaderTask>();
    }

    public void setPlaceholder(int resId) {
        this.placeholderResId = resId;
        this.placeholderBitmap = null;
    }

    public void setPlaceholder(Bitmap bitmap) {
        this.placeholderBitmap = bitmap;
        this.placeholderResId = 0;
    }

    public void loadImage(String url, ImageView imageView) {
        if (url == null || url.isEmpty()) {

            if (placeholderResId != 0) {
                imageView.setImageResource(placeholderResId);
            } else if (placeholderBitmap != null) {
                imageView.setImageBitmap(placeholderBitmap);
            }
            return;
        }

        final String cacheKey = getCacheKey(url);

        final Bitmap cachedBitmap = getBitmapFromCache(cacheKey);
        if (cachedBitmap != null) {
            imageView.setImageBitmap(cachedBitmap);
            return;
        }

        cancelPotentialTask(url, imageView);

        imageView.setTag(url);

        if (placeholderResId != 0) {
            imageView.setImageResource(placeholderResId);
        } else if (placeholderBitmap != null) {
            imageView.setImageBitmap(placeholderBitmap);
        }


        LoaderTask task = new LoaderTask(url, imageView);
        tasks.put(cacheKey, task);
        task.execute(url);
    }

    private boolean cancelPotentialTask(String url, ImageView imageView) {
        LoaderTask task = getLoaderTask(imageView);

        if (task != null) {
            String taskUrl = task.getUrl();
            if (taskUrl == null || !taskUrl.equals(url)) {
                task.cancel(true);
                return true;
            }
        }
        return false;
    }

    private LoaderTask getLoaderTask(ImageView imageView) {
        if (imageView != null) {
            Object tag = imageView.getTag();
            if (tag instanceof String) {
                String url = (String) tag;
                String cacheKey = getCacheKey(url);
                return tasks.get(cacheKey);
            }
        }
        return null;
    }

    private void addBitmapToCache(String key, Bitmap bitmap) {
        if (key != null && bitmap != null && getBitmapFromCache(key) == null) {
            memoryCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromCache(String key) {
        return memoryCache.get(key);
    }

    private String getCacheKey(String url) {
        return String.valueOf(url.hashCode());
    }

    public void clearCache() {
        memoryCache.clear();
    }

    private class LoaderTask extends AsyncTask<String, Void, Bitmap> {
        private String url;
        private final WeakReference<ImageView> imageViewReference;

        public LoaderTask(String url, ImageView imageView) {
            this.url = url;
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        public String getUrl() {
            return url;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {

                return downloadImage(params[0]);
            } catch (Exception e) {
                Log.e(TAG, "Error downloading image: " + params[0], e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled() || bitmap == null) {
                return;
            }

            String cacheKey = getCacheKey(url);
            addBitmapToCache(cacheKey, bitmap);
            tasks.remove(cacheKey);

            ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                Object tag = imageView.getTag();
                if (tag != null && tag.equals(url)) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }

        private Bitmap downloadImage(String urlString) {
            HttpURLConnection connection = null;
            InputStream inputStream = null;

            try {
                URL imageUrl = new URL(urlString);
                connection = (HttpURLConnection) imageUrl.openConnection();
                connection.setDoInput(true);
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return null;
                }

                inputStream = connection.getInputStream();
                return BitmapFactory.decodeStream(inputStream);
            } catch (Exception e) {
                Log.e(TAG, "Error downloading image", e);
                return null;
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error closing streams", e);
                }
            }
        }
    }
}