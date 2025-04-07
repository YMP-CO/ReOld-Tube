package com.ymp.reoldproject.reoldtube;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SearchActivity extends Activity {
    private static final String TAG = "SearchActivity";
    private static final long SUGGESTIONS_DELAY = 300;

    private EditText searchInput;
    private ImageButton backButton;
    private ImageButton clearSearchButton;
    private ListView suggestionsListView;
    private ListView searchResultsListView;
    private ProgressBar loadingIndicator;
    private TextView errorMessage;
    private TextView initialMessage;

    private InvidiousApiService apiService;
    private List<String> suggestionsList;
    private List<SearchResult> searchResults;
    private SuggestionsAdapter suggestionsAdapter;
    private SearchResultsAdapter searchResultsAdapter;

    private Timer searchTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        String instanceUrl = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
                .getString(MainActivity.PREF_INSTANCE_URL, MainActivity.DEFAULT_INSTANCE_URL);
        apiService = new InvidiousApiService(instanceUrl);

        initViews();
        setupListeners();

        suggestionsList = new ArrayList<>();
        searchResults = new ArrayList<>();

        suggestionsAdapter = new SuggestionsAdapter();
        suggestionsListView.setAdapter(suggestionsAdapter);

        searchResultsAdapter = new SearchResultsAdapter();
        searchResultsListView.setAdapter(searchResultsAdapter);

        searchInput.requestFocus();
    }

    private void initViews() {
        searchInput = findViewById(R.id.search_input);
        backButton = findViewById(R.id.back_button);
        clearSearchButton = findViewById(R.id.clear_search);
        suggestionsListView = findViewById(R.id.suggestions_list);
        searchResultsListView = findViewById(R.id.search_results_list);
        loadingIndicator = findViewById(R.id.search_loading_indicator);
        errorMessage = findViewById(R.id.search_error_message);
        initialMessage = findViewById(R.id.search_initial_message);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        clearSearchButton.setOnClickListener(v -> {
            searchInput.setText("");
            clearSearchButton.setVisibility(View.GONE);
            showInitialState();
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    clearSearchButton.setVisibility(View.VISIBLE);
                } else {
                    clearSearchButton.setVisibility(View.GONE);
                    showInitialState();
                }

                if (searchTimer != null) {
                    searchTimer.cancel();
                }

                if (s.length() > 2) {
                    searchTimer = new Timer();
                    searchTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            fetchSuggestions(s.toString());
                        }
                    }, SUGGESTIONS_DELAY);
                } else {
                    runOnUiThread(() -> suggestionsListView.setVisibility(View.GONE));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(searchInput.getText().toString());
                return true;
            }
            return false;
        });

        suggestionsListView.setOnItemClickListener((parent, view, position, id) -> {
            String suggestion = suggestionsList.get(position);
            searchInput.setText(suggestion);
            searchInput.setSelection(suggestion.length());
            performSearch(suggestion);
        });

        searchResultsListView.setOnItemClickListener((parent, view, position, id) -> {
            SearchResult result = searchResults.get(position);
            if ("video".equals(result.getType())) {
                openVideoDetails(result.getVideoId());
            }
        });
    }

    private void showInitialState() {
        initialMessage.setVisibility(View.VISIBLE);
        suggestionsListView.setVisibility(View.GONE);
        searchResultsListView.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.GONE);
        errorMessage.setVisibility(View.GONE);
    }

    private void fetchSuggestions(final String query) {
        if (query.length() < 3) return;

        apiService.getSearchSuggestions(query, new InvidiousApiService.ApiCallback<List<String>>() {
            @Override
            public void onSuccess(final List<String> result) {
                runOnUiThread(() -> {
                    if (result != null && !result.isEmpty()) {
                        suggestionsList.clear();
                        suggestionsList.addAll(result);
                        suggestionsAdapter.notifyDataSetChanged();

                        suggestionsListView.setVisibility(View.VISIBLE);
                        searchResultsListView.setVisibility(View.GONE);
                        initialMessage.setVisibility(View.GONE);
                        errorMessage.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error fetching suggestions", e);
            }
        });
    }

    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }

        suggestionsListView.setVisibility(View.GONE);
        initialMessage.setVisibility(View.GONE);
        errorMessage.setVisibility(View.GONE);
        searchResultsListView.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.VISIBLE);

        apiService.searchVideos(query, new InvidiousApiService.ApiCallback<List<SearchResult>>() {
            @Override
            public void onSuccess(final List<SearchResult> result) {
                runOnUiThread(() -> {
                    loadingIndicator.setVisibility(View.GONE);

                    if (result != null && !result.isEmpty()) {
                        searchResults.clear();
                        searchResults.addAll(result);
                        searchResultsAdapter.notifyDataSetChanged();
                        searchResultsListView.setVisibility(View.VISIBLE);
                    } else {
                        errorMessage.setText("Error. Not found.");
                        errorMessage.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onError(final Exception e) {
                Log.e(TAG, "Error searching videos", e);
                runOnUiThread(() -> {
                    loadingIndicator.setVisibility(View.GONE);
                    errorMessage.setText("Search error: " + e.getMessage());
                    errorMessage.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void openVideoDetails(String videoId) {
        Intent intent = new Intent(SearchActivity.this, VideoDetailActivity.class);
        intent.putExtra("video_id", videoId);
        startActivity(intent);
    }

    private class SuggestionsAdapter extends BaseAdapter {
        private final LayoutInflater inflater;

        public SuggestionsAdapter() {
            inflater = LayoutInflater.from(SearchActivity.this);
        }

        @Override
        public int getCount() {
            return suggestionsList.size();
        }

        @Override
        public Object getItem(int position) {
            return suggestionsList.get(position);
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
                view = inflater.inflate(R.layout.search_suggestion_item, parent, false);
                holder = new ViewHolder();
                holder.text = view.findViewById(R.id.suggestion_text);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            }

            holder.text.setText(suggestionsList.get(position));

            return view;
        }

        private class ViewHolder {
            TextView text;
        }
    }

    private class SearchResultsAdapter extends BaseAdapter {
        private final LayoutInflater inflater;

        public SearchResultsAdapter() {
            inflater = LayoutInflater.from(SearchActivity.this);
        }

        @Override
        public int getCount() {
            return searchResults.size();
        }

        @Override
        public Object getItem(int position) {
            return searchResults.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SearchResult result = searchResults.get(position);

            switch (result.getType()) {
                case "video":
                    return getVideoView(position, convertView, parent);
                case "channel":
                    return getChannelView(position, convertView, parent);
                case "playlist":
                    return getPlaylistView(position, convertView, parent);
                default:
                    return getVideoView(position, convertView, parent);
            }
        }

        private View getVideoView(int position, View convertView, ViewGroup parent) {
            View view;
            VideoViewHolder holder;

            if (convertView == null || !(convertView.getTag() instanceof VideoViewHolder)) {
                view = inflater.inflate(R.layout.video_list_item, parent, false);
                holder = new VideoViewHolder();
                holder.thumbnail = view.findViewById(R.id.video_thumbnail);
                holder.title = view.findViewById(R.id.video_title);
                holder.channelName = view.findViewById(R.id.channel_name);
                holder.videoInfo = view.findViewById(R.id.video_info);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (VideoViewHolder) view.getTag();
            }

            SearchResult result = searchResults.get(position);
            holder.title.setText(result.getTitle());
            holder.channelName.setText(result.getAuthor());

            String viewsText = formatViewCount(result.getViewCount());
            String timeText = formatDuration(result.getLengthSeconds());
            holder.videoInfo.setText(viewsText + " • " + timeText);

            loadThumbnail(holder.thumbnail, result.getVideoThumbnails());

            return view;
        }

        private View getChannelView(int position, View convertView, ViewGroup parent) {
            View view;
            ChannelViewHolder holder;

            if (convertView == null || !(convertView.getTag() instanceof ChannelViewHolder)) {
                view = inflater.inflate(R.layout.video_list_item, parent, false);
                holder = new ChannelViewHolder();
                holder.thumbnail = view.findViewById(R.id.video_thumbnail);
                holder.title = view.findViewById(R.id.video_title);
                holder.channelInfo = view.findViewById(R.id.channel_name);
                holder.videoInfo = view.findViewById(R.id.video_info);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ChannelViewHolder) view.getTag();
            }

            SearchResult result = searchResults.get(position);
            holder.title.setText(result.getAuthor());

            if (result.getSubCount() != null) {
                holder.channelInfo.setText(formatSubscriberCount(result.getSubCount()));
            } else {
                holder.channelInfo.setText("Channel");
            }

            if (result.getVideoCount() != null) {
                holder.videoInfo.setText(result.getVideoCount() + " videos");
            } else {
                holder.videoInfo.setText("");
            }

            loadThumbnail(holder.thumbnail, result.getAuthorThumbnails());

            return view;
        }

        private View getPlaylistView(int position, View convertView, ViewGroup parent) {
            View view;
            PlaylistViewHolder holder;

            if (convertView == null || !(convertView.getTag() instanceof PlaylistViewHolder)) {
                view = inflater.inflate(R.layout.video_list_item, parent, false);
                holder = new PlaylistViewHolder();
                holder.thumbnail = view.findViewById(R.id.video_thumbnail);
                holder.title = view.findViewById(R.id.video_title);
                holder.channelName = view.findViewById(R.id.channel_name);
                holder.videoInfo = view.findViewById(R.id.video_info);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (PlaylistViewHolder) view.getTag();
            }

            SearchResult result = searchResults.get(position);
            holder.title.setText(result.getTitle());
            holder.channelName.setText(result.getAuthor());

            if (result.getVideoCount() != null) {
                holder.videoInfo.setText("Playlist • " + result.getVideoCount() + " videos");
            } else {
                holder.videoInfo.setText("Playlist");
            }

            if (result.getVideos() != null && !result.getVideos().isEmpty()
                    && result.getVideos().get(0).getVideoThumbnails() != null) {
                loadThumbnail(holder.thumbnail, result.getVideos().get(0).getVideoThumbnails());
            } else if (result.getPlaylistThumbnail() != null) {
                new ImageLoaderTask(holder.thumbnail).execute(result.getPlaylistThumbnail());
            }

            return view;
        }

        private String formatViewCount(long viewCount) {
            if (viewCount < 1000) {
                return viewCount + " views";
            } else if (viewCount < 1000000) {
                return String.format("%.1fK views", viewCount / 1000.0);
            } else {
                return String.format("%.1fM views", viewCount / 1000000.0);
            }
        }

        private String formatSubscriberCount(int subCount) {
            if (subCount < 1000) {
                return subCount + " subscribers";
            } else if (subCount < 1000000) {
                return String.format("%.1fK subscribers", subCount / 1000.0);
            } else {
                return String.format("%.1fM subscribers", subCount / 1000000.0);
            }
        }

        private String formatDuration(int seconds) {
            if (seconds <= 0) {
                return "";
            }

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

        private class VideoViewHolder {
            ImageView thumbnail;
            TextView title;
            TextView channelName;
            TextView videoInfo;
        }

        private class ChannelViewHolder {
            ImageView thumbnail;
            TextView title;
            TextView channelInfo;
            TextView videoInfo;
        }

        private class PlaylistViewHolder {
            ImageView thumbnail;
            TextView title;
            TextView channelName;
            TextView videoInfo;
        }
    }

    private class ImageLoaderTask extends android.os.AsyncTask<String, Void, android.graphics.Bitmap> {
        private final java.lang.ref.WeakReference<ImageView> imageViewReference;

        public ImageLoaderTask(ImageView imageView) {
            imageViewReference = new java.lang.ref.WeakReference<>(imageView);
        }

        @Override
        protected android.graphics.Bitmap doInBackground(String... params) {
            String url = params[0];
            return downloadImage(url);
        }

        @Override
        protected void onPostExecute(android.graphics.Bitmap bitmap) {
            if (isCancelled() || bitmap == null) {
                return;
            }

            ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }

        private android.graphics.Bitmap downloadImage(String url) {
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