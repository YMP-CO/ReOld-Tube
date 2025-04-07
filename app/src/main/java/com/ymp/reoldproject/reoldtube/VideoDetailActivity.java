package com.ymp.reoldproject.reoldtube;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VideoDetailActivity extends Activity {
    private static final String TAG = "VideoDetailActivity";

    private ProgressBar loadingIndicator;
    private TextView errorMessage;
    private LinearLayout videoContent;
    private ImageButton backButton;
    private TextView toolbarTitle;

    private LinearLayout mobileLayout;
    private ImageView videoThumbnail;
    private ImageButton playButton;
    private TextView videoTitle;
    private TextView videoInfo;
    private TextView videoLikes;
    private TextView videoGenre;
    private ImageView channelAvatar;
    private TextView channelName;
    private TextView channelSubscribers;
    private WebView descriptionWebView;
    private LinearLayout commentsContainerMobile;

    private LinearLayout tabletLayout;
    private ImageView videoThumbnailTablet;
    private ImageButton playButtonTablet;
    private TextView videoTitleTablet;
    private TextView videoInfoTablet;
    private TextView videoLikesTablet;
    private TextView videoGenreTablet;
    private ImageView channelAvatarTablet;
    private TextView channelNameTablet;
    private TextView channelSubscribersTablet;
    private WebView descriptionWebViewTablet;
    private ListView commentsListTablet;

    private String videoId;
    private InvidiousApiService apiService;
    private Video videoDetails;
    private CommentsResponse commentsResponse;
    private List<Comment> comments = new ArrayList<Comment>();
    private CommentsAdapter commentsAdapter;
    private boolean isTablet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_detail);

        videoId = getIntent().getStringExtra("video_id");
        if (videoId == null || videoId.isEmpty()) {
            finish();
            return;
        }

        isTablet = (getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;

        String instanceUrl = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
                .getString(MainActivity.PREF_INSTANCE_URL, MainActivity.DEFAULT_INSTANCE_URL);
        apiService = new InvidiousApiService(instanceUrl);

        initViews();
        setupListeners();

        commentsAdapter = new CommentsAdapter();
        if (commentsListTablet != null) {
            commentsListTablet.setAdapter(commentsAdapter);
        }

        loadVideoDetails();
    }

    private void initViews() {
        loadingIndicator = (ProgressBar) findViewById(R.id.video_loading_indicator);
        errorMessage = (TextView) findViewById(R.id.video_error_message);
        videoContent = (LinearLayout) findViewById(R.id.video_content);
        backButton = (ImageButton) findViewById(R.id.video_back_button);
        toolbarTitle = (TextView) findViewById(R.id.video_toolbar_title);

        mobileLayout = (LinearLayout) findViewById(R.id.mobile_layout);
        videoThumbnail = (ImageView) findViewById(R.id.video_thumbnail);
        playButton = (ImageButton) findViewById(R.id.play_button);
        videoTitle = (TextView) findViewById(R.id.video_title);
        videoInfo = (TextView) findViewById(R.id.video_info);
        videoLikes = (TextView) findViewById(R.id.video_likes);
        videoGenre = (TextView) findViewById(R.id.video_genre);
        channelAvatar = (ImageView) findViewById(R.id.channel_avatar);
        channelName = (TextView) findViewById(R.id.channel_name);
        channelSubscribers = (TextView) findViewById(R.id.channel_subscribers);
        descriptionWebView = (WebView) findViewById(R.id.description_web_view);
        commentsContainerMobile = (LinearLayout) findViewById(R.id.comments_container_mobile);

        configureWebView(descriptionWebView);

        tabletLayout = (LinearLayout) findViewById(R.id.tablet_layout);
        if (isTablet) {
            videoThumbnailTablet = (ImageView) findViewById(R.id.video_thumbnail_tablet);
            playButtonTablet = (ImageButton) findViewById(R.id.play_button_tablet);
            videoTitleTablet = (TextView) findViewById(R.id.video_title_tablet);
            videoInfoTablet = (TextView) findViewById(R.id.video_info_tablet);
            videoLikesTablet = (TextView) findViewById(R.id.video_likes_tablet);
            videoGenreTablet = (TextView) findViewById(R.id.video_genre_tablet);
            channelAvatarTablet = (ImageView) findViewById(R.id.channel_avatar_tablet);
            channelNameTablet = (TextView) findViewById(R.id.channel_name_tablet);
            channelSubscribersTablet = (TextView) findViewById(R.id.channel_subscribers_tablet);
            descriptionWebViewTablet = (WebView) findViewById(R.id.description_web_view_tablet);
            commentsListTablet = (ListView) findViewById(R.id.comments_list_tablet);

            configureWebView(descriptionWebViewTablet);
        }

        if (isTablet) {
            mobileLayout.setVisibility(View.GONE);
            tabletLayout.setVisibility(View.VISIBLE);
        } else {
            mobileLayout.setVisibility(View.VISIBLE);
            tabletLayout.setVisibility(View.GONE);
        }
    }

    private void configureWebView(WebView webView) {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(false);
        settings.setLoadsImagesAutomatically(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.setBackgroundColor(0x00000000);
    }

    private void setupListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playVideo();
            }
        });

        if (isTablet && playButtonTablet != null) {
            playButtonTablet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playVideo();
                }
            });
        }
    }

    private void loadVideoDetails() {
        loadingIndicator.setVisibility(View.VISIBLE);
        errorMessage.setVisibility(View.GONE);
        videoContent.setVisibility(View.GONE);

        apiService.getVideoDetails(videoId, new InvidiousApiService.ApiCallback<Video>() {
            @Override
            public void onSuccess(final Video video) {
                videoDetails = video;

                loadComments();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateVideoDetails(video);
                    }
                });
            }

            @Override
            public void onError(final Exception e) {
                Log.e(TAG, "Error loading video details", e);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingIndicator.setVisibility(View.GONE);
                        errorMessage.setVisibility(View.VISIBLE);
                        errorMessage.setText("Error loading video: " + e.getMessage());
                    }
                });
            }
        });
    }

    private void loadComments() {
        apiService.getVideoComments(videoId, new InvidiousApiService.ApiCallback<CommentsResponse>() {
            @Override
            public void onSuccess(final CommentsResponse response) {
                commentsResponse = response;

                if (response.getComments() != null) {
                    comments.clear();
                    comments.addAll(response.getComments());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateComments();

                            loadingIndicator.setVisibility(View.GONE);
                            videoContent.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }

            @Override
            public void onError(final Exception e) {
                Log.e(TAG, "Error loading comments", e);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingIndicator.setVisibility(View.GONE);
                        videoContent.setVisibility(View.VISIBLE);

                        TextView commentsError = new TextView(VideoDetailActivity.this);
                        commentsError.setText("Failed to load comments");
                        commentsError.setPadding(0, 16, 0, 16);

                        if (isTablet) {
                            comments.clear();
                            commentsAdapter.notifyDataSetChanged();
                        } else {
                            commentsContainerMobile.addView(commentsError);
                        }
                    }
                });
            }
        });
    }

    private void updateVideoDetails(Video video) {
        toolbarTitle.setText(video.getTitle());

        videoTitle.setText(video.getTitle());
        videoInfo.setText(formatViewCount(video.getViewCount()) + " • " + video.getPublishedText());
        videoLikes.setText(formatNumber(video.getLikeCount()));

        if (video.getGenre() != null && !video.getGenre().isEmpty()) {
            videoGenre.setText(video.getGenre());
            videoGenre.setVisibility(View.VISIBLE);
        } else {
            videoGenre.setVisibility(View.GONE);
        }

        channelName.setText(video.getAuthor());
        if (video.getSubCountText() != null) {
            channelSubscribers.setText(video.getSubCountText());
        } else {
            channelSubscribers.setVisibility(View.GONE);
        }

        if (video.getAuthorThumbnails() != null && !video.getAuthorThumbnails().isEmpty()) {
            ImageLoader.getInstance().loadImage(
                    video.getAuthorThumbnails().get(0).getUrl(), channelAvatar);
        }

        if (video.getVideoThumbnails() != null && !video.getVideoThumbnails().isEmpty()) {
            String thumbnailUrl = null;
            for (Thumbnail thumbnail : video.getVideoThumbnails()) {
                if ("maxres".equals(thumbnail.getQuality()) || "high".equals(thumbnail.getQuality())) {
                    thumbnailUrl = thumbnail.getUrl();
                    break;
                }
            }

            if (thumbnailUrl == null && !video.getVideoThumbnails().isEmpty()) {
                thumbnailUrl = video.getVideoThumbnails().get(0).getUrl();
            }

            if (thumbnailUrl != null) {
                ImageLoader.getInstance().loadImage(thumbnailUrl, videoThumbnail);
            }
        }

        loadDescription(descriptionWebView, video.getDescriptionHtml());

        if (isTablet) {
            videoTitleTablet.setText(video.getTitle());
            videoInfoTablet.setText(formatViewCount(video.getViewCount()) + " • " + video.getPublishedText());
            videoLikesTablet.setText(formatNumber(video.getLikeCount()));

            if (video.getGenre() != null && !video.getGenre().isEmpty()) {
                videoGenreTablet.setText(video.getGenre());
                videoGenreTablet.setVisibility(View.VISIBLE);
            } else {
                videoGenreTablet.setVisibility(View.GONE);
            }

            channelNameTablet.setText(video.getAuthor());
            if (video.getSubCountText() != null) {
                channelSubscribersTablet.setText(video.getSubCountText());
            } else {
                channelSubscribersTablet.setVisibility(View.GONE);
            }

            if (video.getAuthorThumbnails() != null && !video.getAuthorThumbnails().isEmpty()) {
                ImageLoader.getInstance().loadImage(
                        video.getAuthorThumbnails().get(0).getUrl(), channelAvatarTablet);
            }

            if (video.getVideoThumbnails() != null && !video.getVideoThumbnails().isEmpty()) {
                String thumbnailUrl = null;
                for (Thumbnail thumbnail : video.getVideoThumbnails()) {
                    if ("maxres".equals(thumbnail.getQuality()) || "high".equals(thumbnail.getQuality())) {
                        thumbnailUrl = thumbnail.getUrl();
                        break;
                    }
                }

                if (thumbnailUrl == null && !video.getVideoThumbnails().isEmpty()) {
                    thumbnailUrl = video.getVideoThumbnails().get(0).getUrl();
                }

                if (thumbnailUrl != null) {
                    ImageLoader.getInstance().loadImage(thumbnailUrl, videoThumbnailTablet);
                }
            }

            loadDescription(descriptionWebViewTablet, video.getDescriptionHtml());
        }
    }

    private void loadDescription(WebView webView, String html) {
        if (html != null && !html.isEmpty()) {
            String formattedHtml = "<html><head>"
                    + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                    + "<style>body{color:#333; font-family:sans-serif; font-size:14px; margin:0; padding:0;} "
                    + "a{color:#3F51B5;} img{max-width:100%;}</style>"
                    + "</head><body>" + html + "</body></html>";

            webView.loadDataWithBaseURL(null, formattedHtml, "text/html", "UTF-8", null);
        } else {
            webView.loadDataWithBaseURL(null, "<html><body>No description</body></html>",
                    "text/html", "UTF-8", null);
        }
    }

    private void updateComments() {
        if (comments.isEmpty()) {
            return;
        }

        if (isTablet) {
            commentsAdapter.notifyDataSetChanged();
        } else {
            commentsContainerMobile.removeAllViews();

            int maxComments = Math.min(comments.size(), 10);

            for (int i = 0; i < maxComments; i++) {
                Comment comment = comments.get(i);
                View commentView = createCommentView(comment);
                commentsContainerMobile.addView(commentView);

                if (i < maxComments - 1) {
                    View divider = new View(this);
                    divider.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 1));
                    divider.setBackgroundColor(0xFFE0E0E0);
                    commentsContainerMobile.addView(divider);
                }
            }

            if (comments.size() > maxComments) {
                TextView showMoreButton = new TextView(this);
                showMoreButton.setText("Show more comments ("
                        + (comments.size() - maxComments) + ")");
                showMoreButton.setPadding(0, 16, 0, 16);
                showMoreButton.setTextColor(0xFF3F51B5);
                showMoreButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(VideoDetailActivity.this, CommentsActivity.class);
                        intent.putExtra("video_id", videoId);
                        startActivity(intent);
                    }
                });
                commentsContainerMobile.addView(showMoreButton);
            }
        }
    }

    private View createCommentView(Comment comment) {
        View view = getLayoutInflater().inflate(R.layout.comment_item, null);

        ImageView avatar = (ImageView) view.findViewById(R.id.comment_avatar);
        TextView authorText = (TextView) view.findViewById(R.id.comment_author);
        TextView timeText = (TextView) view.findViewById(R.id.comment_time);
        TextView contentText = (TextView) view.findViewById(R.id.comment_content);
        TextView likesText = (TextView) view.findViewById(R.id.comment_likes);
        TextView repliesText = (TextView) view.findViewById(R.id.comment_replies);
        ImageView ownerIcon = (ImageView) view.findViewById(R.id.comment_owner_icon);
        ImageView heartIcon = (ImageView) view.findViewById(R.id.comment_heart_icon);

        if (comment.getAuthorThumbnails() != null && !comment.getAuthorThumbnails().isEmpty()) {
            ImageLoader.getInstance().loadImage(
                    comment.getAuthorThumbnails().get(0).getUrl(), avatar);
        }

        authorText.setText(comment.getAuthor());
        timeText.setText(comment.getPublishedText());
        contentText.setText(Html.fromHtml(comment.getContentHtml()));
        likesText.setText(String.valueOf(comment.getLikeCount()));

        if (comment.isAuthorIsChannelOwner()) {
            ownerIcon.setVisibility(View.VISIBLE);
        }

        if (comment.getCreatorHeart() != null) {
            heartIcon.setVisibility(View.VISIBLE);
        }

        if (comment.getReplies() != null && comment.getReplies().getReplyCount() > 0) {
            repliesText.setVisibility(View.VISIBLE);
            repliesText.setText(comment.getReplies().getReplyCount() + " replies");
        }

        return view;
    }

    private void playVideo() {
        if (videoDetails == null || videoDetails.getFormatStreams() == null
                || videoDetails.getFormatStreams().isEmpty()) {
            return;
        }

        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        String preferredQuality = prefs.getString(SettingsActivity.PREF_VIDEO_QUALITY, "Automatic");

        FormatStream selectedStream = null;

        if ("Automatic".equals(preferredQuality)) {
            selectedStream = videoDetails.getFormatStreams().get(0);
        } else {
            for (FormatStream stream : videoDetails.getFormatStreams()) {
                if (preferredQuality.equals(stream.getQualityLabel())) {
                    selectedStream = stream;
                    break;
                }
            }

            if (selectedStream == null && !videoDetails.getFormatStreams().isEmpty()) {
                selectedStream = videoDetails.getFormatStreams().get(0);
            }
        }

        if (selectedStream != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(selectedStream.getUrl()), "video/mp4");
            startActivity(intent);
        }
    }

    private String formatViewCount(long count) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("en", "US"));
        return formatter.format(count) + " views";
    }

    private String formatNumber(int number) {
        if (number < 1000) {
            return String.valueOf(number);
        } else if (number < 1000000) {
            return String.format("%.1fK", number / 1000.0);
        } else {
            return String.format("%.1fM", number / 1000000.0);
        }
    }

    private class CommentsAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        public CommentsAdapter() {
            inflater = LayoutInflater.from(VideoDetailActivity.this);
        }

        @Override
        public int getCount() {
            return comments.size();
        }

        @Override
        public Object getItem(int position) {
            return comments.get(position);
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
                view = inflater.inflate(R.layout.comment_item, parent, false);
                holder = new ViewHolder();
                holder.avatar = (ImageView) view.findViewById(R.id.comment_avatar);
                holder.author = (TextView) view.findViewById(R.id.comment_author);
                holder.time = (TextView) view.findViewById(R.id.comment_time);
                holder.content = (TextView) view.findViewById(R.id.comment_content);
                holder.likes = (TextView) view.findViewById(R.id.comment_likes);
                holder.replies = (TextView) view.findViewById(R.id.comment_replies);
                holder.ownerIcon = (ImageView) view.findViewById(R.id.comment_owner_icon);
                holder.heartIcon = (ImageView) view.findViewById(R.id.comment_heart_icon);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            }

            Comment comment = comments.get(position);

            if (comment.getAuthorThumbnails() != null && !comment.getAuthorThumbnails().isEmpty()) {
                ImageLoader.getInstance().loadImage(
                        comment.getAuthorThumbnails().get(0).getUrl(), holder.avatar);
            }

            holder.author.setText(comment.getAuthor());
            holder.time.setText(comment.getPublishedText());
            holder.content.setText(Html.fromHtml(comment.getContentHtml()));
            holder.likes.setText(String.valueOf(comment.getLikeCount()));

            if (comment.isAuthorIsChannelOwner()) {
                holder.ownerIcon.setVisibility(View.VISIBLE);
            } else {
                holder.ownerIcon.setVisibility(View.GONE);
            }

            if (comment.getCreatorHeart() != null) {
                holder.heartIcon.setVisibility(View.VISIBLE);
            } else {
                holder.heartIcon.setVisibility(View.GONE);
            }

            if (comment.getReplies() != null && comment.getReplies().getReplyCount() > 0) {
                holder.replies.setVisibility(View.VISIBLE);
                holder.replies.setText(comment.getReplies().getReplyCount() + " replies");
            } else {
                holder.replies.setVisibility(View.GONE);
            }

            return view;
        }

        private class ViewHolder {
            ImageView avatar;
            TextView author;
            TextView time;
            TextView content;
            TextView likes;
            TextView replies;
            ImageView ownerIcon;
            ImageView heartIcon;
        }
    }
}