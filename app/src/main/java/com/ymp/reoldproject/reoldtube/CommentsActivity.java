package com.ymp.reoldproject.reoldtube;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

public class CommentsActivity extends Activity {
    private static final String TAG = "CommentsActivity";

    private ListView commentsList;
    private ProgressBar loadingIndicator;
    private TextView errorMessage;
    private ImageButton backButton;
    private TextView toolbarTitle;

    private String videoId;
    private InvidiousApiService apiService;
    private List<Comment> comments = new ArrayList<Comment>();
    private CommentsAdapter adapter;
    private String continuation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        videoId = getIntent().getStringExtra("video_id");
        if (videoId == null || videoId.isEmpty()) {
            finish();
            return;
        }

        String instanceUrl = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
                .getString(MainActivity.PREF_INSTANCE_URL, MainActivity.DEFAULT_INSTANCE_URL);
        apiService = new InvidiousApiService(instanceUrl);

        initViews();
        setupListeners();

        adapter = new CommentsAdapter();
        commentsList.setAdapter(adapter);

        loadComments();
    }

    private void initViews() {
        commentsList = (ListView) findViewById(R.id.comments_list);
        loadingIndicator = (ProgressBar) findViewById(R.id.comments_loading_indicator);
        errorMessage = (TextView) findViewById(R.id.comments_error_message);
        backButton = (ImageButton) findViewById(R.id.comments_back_button);
        toolbarTitle = (TextView) findViewById(R.id.comments_toolbar_title);
    }

    private void setupListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadComments() {
        loadingIndicator.setVisibility(View.VISIBLE);
        errorMessage.setVisibility(View.GONE);

        apiService.getVideoComments(videoId, new InvidiousApiService.ApiCallback<CommentsResponse>() {
            @Override
            public void onSuccess(final CommentsResponse response) {
                if (response.getComments() != null) {
                    comments.clear();
                    comments.addAll(response.getComments());
                    continuation = response.getContinuation();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.getCommentCount() != null) {
                                toolbarTitle.setText("Comments (" + response.getCommentCount() + ")");
                            } else {
                                toolbarTitle.setText("Comments");
                            }

                            adapter.notifyDataSetChanged();
                            loadingIndicator.setVisibility(View.GONE);
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
                        errorMessage.setVisibility(View.VISIBLE);
                        errorMessage.setText("Error loading comments: " + e.getMessage());
                    }
                });
            }
        });
    }

    private class CommentsAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        public CommentsAdapter() {
            inflater = LayoutInflater.from(CommentsActivity.this);
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