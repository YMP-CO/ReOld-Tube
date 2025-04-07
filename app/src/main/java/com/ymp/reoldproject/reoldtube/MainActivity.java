package com.ymp.reoldproject.reoldtube;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    public static final String PREFS_NAME = "InvidiousPrefs";
    public static final String PREF_INSTANCE_URL = "instance_url";
    public static final String DEFAULT_INSTANCE_URL = "https://invidious.snopyta.org";

    private FrameLayout drawerFrame;
    private ListView videosList;
    private GridView videosGrid;
    private ProgressBar loadingIndicator;
    private TextView errorMessage;
    private LinearLayout settingsContainer;
    private EditText instanceUrlInput;
    private LinearLayout categoriesContainer;

    private InvidiousApiService apiService;
    private List<TrendingVideo> trendingVideos;
    private VideoAdapter videoAdapter;
    private boolean isTablet;
    private boolean isDrawerOpen = false;
    private String currentCategory = "";

    private String[] categoryNames = {
            "movies", "music", "gaming", "news", "live", "sports",
            "education", "hobbies"
    };
    private String[] categoryTypes = {
            "movies", "music", "gaming", "news", "live", "sports",
            "education", "hobbies"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isTablet = (getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;

        String instanceUrl = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString(PREF_INSTANCE_URL, DEFAULT_INSTANCE_URL);
        apiService = new InvidiousApiService(instanceUrl);

        initViews();
        setupNavigationDrawer();
        createCategories();

        if (isTablet) {
            drawerFrame.setVisibility(View.VISIBLE);
            isDrawerOpen = true;
            videosGrid.setVisibility(View.VISIBLE);
            videosList.setVisibility(View.GONE);
        } else {
            drawerFrame.setVisibility(View.GONE);
            isDrawerOpen = false;
            videosGrid.setVisibility(View.GONE);
            videosList.setVisibility(View.VISIBLE);
        }

        trendingVideos = new ArrayList<TrendingVideo>();
        videoAdapter = new VideoAdapter();
        videosList.setAdapter(videoAdapter);
        videosGrid.setAdapter(videoAdapter);

        loadTrendingVideos(null);
    }

    private void initViews() {
        drawerFrame = (FrameLayout) findViewById(R.id.drawer_frame);
        videosList = (ListView) findViewById(R.id.videos_list);
        videosGrid = (GridView) findViewById(R.id.videos_grid);
        loadingIndicator = (ProgressBar) findViewById(R.id.loading_indicator);
        errorMessage = (TextView) findViewById(R.id.error_message);
        settingsContainer = (LinearLayout) findViewById(R.id.settings_container);
        instanceUrlInput = (EditText) findViewById(R.id.instance_url);
        categoriesContainer = (LinearLayout) findViewById(R.id.categories_container);

        ImageButton menuButton = (ImageButton) findViewById(R.id.menu_button);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDrawer();
            }
        });

        ImageButton searchButton = (ImageButton) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSearch();
            }
        });

        String instanceUrl = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString(PREF_INSTANCE_URL, DEFAULT_INSTANCE_URL);
        instanceUrlInput.setText(instanceUrl);

        Button applyInstanceButton = (Button) findViewById(R.id.apply_instance);
        applyInstanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyInstanceUrl();
            }
        });

        videosList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openVideoDetails(position);
            }
        });

        videosGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openVideoDetails(position);
            }
        });
    }

    private void setupNavigationDrawer() {
        LinearLayout navHome = (LinearLayout) findViewById(R.id.nav_home);
        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentCategory = "";
                ((TextView) findViewById(R.id.toolbar_title)).setText("Trends");
                loadTrendingVideos(null);
                if (!isTablet) {
                    toggleDrawer();
                }
            }
        });

        LinearLayout navSearch = (LinearLayout) findViewById(R.id.nav_search);
        navSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSearch();
                if (!isTablet) {
                    toggleDrawer();
                }
            }
        });

        LinearLayout navSettings = (LinearLayout) findViewById(R.id.nav_settings);
        navSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                if (!isTablet) {
                    toggleDrawer();
                }
            }
        });
    }

    private void createCategories() {
        for (int i = 0; i < categoryNames.length; i++) {
            final String categoryName = categoryNames[i];
            final String categoryType = categoryTypes[i];

            LinearLayout categoryItem = new LinearLayout(this);
            categoryItem.setOrientation(LinearLayout.HORIZONTAL);
            categoryItem.setPadding(16, 16, 16, 16);
            categoryItem.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            categoryItem.setBackgroundResource(android.R.drawable.list_selector_background);
            categoryItem.setClickable(true);
            categoryItem.setFocusable(true);

            TextView categoryText = new TextView(this);
            categoryText.setText(categoryName);
            categoryText.setTextSize(16);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            textParams.setMargins(32, 0, 0, 0);
            categoryText.setLayoutParams(textParams);

            categoryItem.addView(categoryText);
            categoriesContainer.addView(categoryItem);

            categoryItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentCategory = categoryType;
                    ((TextView) findViewById(R.id.toolbar_title)).setText(categoryName);
                    loadTrendingVideos(categoryType);
                    if (!isTablet) {
                        toggleDrawer();
                    }
                }
            });
        }
    }

    private void toggleDrawer() {
        if (isDrawerOpen) {
            drawerFrame.setVisibility(View.GONE);
            isDrawerOpen = false;
        } else {
            drawerFrame.setVisibility(View.VISIBLE);
            isDrawerOpen = true;
        }
    }

    private void openSearch() {
        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
        startActivity(intent);
    }

    private void openVideoDetails(int position) {
        TrendingVideo video = trendingVideos.get(position);
        Intent intent = new Intent(MainActivity.this, VideoDetailActivity.class);
        intent.putExtra("video_id", video.getVideoId());
        startActivity(intent);
    }

    private void applyInstanceUrl() {
        String newUrl = instanceUrlInput.getText().toString().trim();
        if (newUrl.isEmpty()) {
            Toast.makeText(this, "URL can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(PREF_INSTANCE_URL, newUrl);
        editor.commit();

        apiService.setInstanceUrl(newUrl);

        loadTrendingVideos(currentCategory.isEmpty() ? null : currentCategory);

        Toast.makeText(this, "URL updated", Toast.LENGTH_SHORT).show();
        settingsContainer.setVisibility(View.GONE);
    }

    private void loadTrendingVideos(final String type) {
        loadingIndicator.setVisibility(View.VISIBLE);
        errorMessage.setVisibility(View.GONE);

        apiService.getTrendingVideos(new InvidiousApiService.ApiCallback<List<TrendingVideo>>() {
            @Override
            public void onSuccess(List<TrendingVideo> result) {
                trendingVideos.clear();
                trendingVideos.addAll(result);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        videoAdapter.notifyDataSetChanged();
                        loadingIndicator.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading trending videos", e);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingIndicator.setVisibility(View.GONE);
                        errorMessage.setVisibility(View.VISIBLE);
                        errorMessage.setText("Error loading trending videos: " + e.getMessage());
                    }
                });
            }
        }, "US", type);
    }

    private class VideoAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        public VideoAdapter() {
            inflater = LayoutInflater.from(MainActivity.this);
        }

        @Override
        public int getCount() {
            return trendingVideos.size();
        }

        @Override
        public Object getItem(int position) {
            return trendingVideos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            ViewHolder holder;

            if (convertView == null) {
                if (isTablet && parent instanceof GridView) {
                    view = inflater.inflate(R.layout.video_grid_item, parent, false);
                } else {
                    view = inflater.inflate(R.layout.video_list_item, parent, false);
                }

                holder = new ViewHolder();
                holder.thumbnail = (ImageView) view.findViewById(R.id.video_thumbnail);
                holder.title = (TextView) view.findViewById(R.id.video_title);
                holder.channelName = (TextView) view.findViewById(R.id.channel_name);
                holder.videoInfo = (TextView) view.findViewById(R.id.video_info);

                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            }

            TrendingVideo video = trendingVideos.get(position);
            holder.title.setText(video.getTitle());
            holder.channelName.setText(video.getAuthor());

            String viewsText = formatViewCount(video.getViewCount());
            String timeText = formatDuration(video.getLengthSeconds());
            holder.videoInfo.setText(viewsText + " • " + timeText);

            loadThumbnail(holder.thumbnail, video.getVideoThumbnails());

            return view;
        }

        private String formatViewCount(long viewCount) {
            if (viewCount < 1000) {
                return String.valueOf(viewCount);
            } else if (viewCount < 1000000) {
                return String.format("%.1f k", viewCount / 1000.0);
            } else {
                return String.format("%.1f k", viewCount / 1000000.0);
            }
        }

        private String formatDuration(int seconds) {
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            int secs = seconds % 60;

            if (hours > 0) {
                return String.format("%d:%02d:%02d", hours, minutes, secs);
            } else {
                return String.format("%d:%02d", minutes, secs);
            }
        }

        private void loadThumbnail(ImageView imageView, List<Thumbnail> thumbnails) {
            if (thumbnails == null || thumbnails.isEmpty()) {
                return;
            }

            String thumbnailUrl = null;
            for (Thumbnail thumbnail : thumbnails) {
                if ("medium".equals(thumbnail.getQuality())) {
                    thumbnailUrl = thumbnail.getUrl();
                    break;
                }
            }

            if (thumbnailUrl == null && !thumbnails.isEmpty()) {
                thumbnailUrl = thumbnails.get(0).getUrl();
            }

            if (thumbnailUrl != null) {
                new ImageLoaderTask(imageView).execute(thumbnailUrl);
            }
        }

        private class ViewHolder {
            ImageView thumbnail;
            TextView title;
            TextView channelName;
            TextView videoInfo;
        }
    }

    private class ImageLoaderTask extends android.os.AsyncTask<String, Void, Bitmap> {
        private final java.lang.ref.WeakReference<ImageView> imageViewReference;

        public ImageLoaderTask(ImageView imageView) {
            imageViewReference = new java.lang.ref.WeakReference<>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String url = params[0];
            return downloadImage(url);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled() || bitmap == null) {
                return;
            }

            ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }

        private Bitmap downloadImage(String url) {
            try {
                java.net.URL imageUrl = new java.net.URL(url);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) imageUrl.openConnection();
                connection.setDoInput(true);
                connection.connect();
                java.io.InputStream input = connection.getInputStream();
                return android.graphics.BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                Log.e(TAG, "Error downloading image: " + url, e);
                return null;
            }
        }
    }
}