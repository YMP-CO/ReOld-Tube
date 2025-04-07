package com.ymp.reoldproject.reoldtube;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class InvidiousApiService {
    private static final String TAG = "InvidiousApiService";
    private String baseUrl;

    public InvidiousApiService(String instanceUrl) {
        this.baseUrl = instanceUrl;
    }

    public void setInstanceUrl(String instanceUrl) {
        this.baseUrl = instanceUrl;
    }

    public void getTrendingVideos(final ApiCallback<List<TrendingVideo>> callback, String region, String type) {
        String apiUrl = baseUrl + "/api/v1/trending";

        boolean hasQueryParams = false;
        if (region != null && !region.isEmpty()) {
            apiUrl += "?region=" + region;
            hasQueryParams = true;
        }

        if (type != null && !type.isEmpty()) {
            apiUrl += hasQueryParams ? "&type=" + type : "?type=" + type;
        }

        new FetchDataTask<List<TrendingVideo>>(callback) {
            @Override
            protected List<TrendingVideo> parseJson(String jsonString) throws JSONException {
                List<TrendingVideo> videos = new ArrayList<TrendingVideo>();
                JSONArray jsonArray = new JSONArray(jsonString);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    TrendingVideo video = new TrendingVideo();
                    video.setTitle(obj.getString("title"));
                    video.setVideoId(obj.getString("videoId"));
                    video.setLengthSeconds(obj.getInt("lengthSeconds"));
                    video.setViewCount(obj.getLong("viewCount"));
                    video.setAuthor(obj.getString("author"));
                    video.setAuthorId(obj.getString("authorId"));
                    video.setAuthorUrl(obj.getString("authorUrl"));
                    video.setPublished(obj.getLong("published"));
                    video.setPublishedText(obj.getString("publishedText"));

                    if (!obj.isNull("description")) {
                        video.setDescription(obj.getString("description"));
                    }

                    if (!obj.isNull("liveNow")) {
                        video.setLiveNow(obj.getBoolean("liveNow"));
                    }

                    if (!obj.isNull("paid")) {
                        video.setPaid(obj.getBoolean("paid"));
                    }

                    if (!obj.isNull("premium")) {
                        video.setPremium(obj.getBoolean("premium"));
                    }

                    JSONArray thumbnails = obj.getJSONArray("videoThumbnails");
                    List<Thumbnail> videoThumbnails = new ArrayList<Thumbnail>();
                    for (int j = 0; j < thumbnails.length(); j++) {
                        JSONObject thumbObj = thumbnails.getJSONObject(j);
                        Thumbnail thumbnail = new Thumbnail();
                        thumbnail.setQuality(thumbObj.getString("quality"));
                        thumbnail.setUrl(thumbObj.getString("url"));
                        thumbnail.setWidth(thumbObj.getInt("width"));
                        thumbnail.setHeight(thumbObj.getInt("height"));
                        videoThumbnails.add(thumbnail);
                    }
                    video.setVideoThumbnails(videoThumbnails);

                    videos.add(video);
                }

                return videos;
            }
        }.execute(apiUrl);
    }

    public void getVideoDetails(final String videoId, final ApiCallback<Video> callback) {
        String apiUrl = baseUrl + "/api/v1/videos/" + videoId;

        new FetchDataTask<Video>(callback) {
            @Override
            protected Video parseJson(String jsonString) throws JSONException {
                JSONObject obj = new JSONObject(jsonString);
                Video video = new Video();

                if (!obj.isNull("type")) video.setType(obj.getString("type"));
                video.setTitle(obj.getString("title"));
                video.setVideoId(obj.getString("videoId"));

                if (!obj.isNull("description")) video.setDescription(obj.getString("description"));
                if (!obj.isNull("descriptionHtml"))
                    video.setDescriptionHtml(obj.getString("descriptionHtml"));

                if (!obj.isNull("published")) video.setPublished(obj.getLong("published"));
                if (!obj.isNull("publishedText"))
                    video.setPublishedText(obj.getString("publishedText"));

                if (!obj.isNull("viewCount")) video.setViewCount(obj.getLong("viewCount"));
                if (!obj.isNull("likeCount")) video.setLikeCount(obj.getInt("likeCount"));
                if (!obj.isNull("dislikeCount")) video.setDislikeCount(obj.getInt("dislikeCount"));

                if (!obj.isNull("paid")) video.setPaid(obj.getBoolean("paid"));
                if (!obj.isNull("premium")) video.setPremium(obj.getBoolean("premium"));
                if (!obj.isNull("isFamilyFriendly"))
                    video.setFamilyFriendly(obj.getBoolean("isFamilyFriendly"));

                if (!obj.isNull("genre")) video.setGenre(obj.getString("genre"));
                if (!obj.isNull("genreUrl")) video.setGenreUrl(obj.getString("genreUrl"));

                video.setAuthor(obj.getString("author"));
                video.setAuthorId(obj.getString("authorId"));
                video.setAuthorUrl(obj.getString("authorUrl"));

                if (!obj.isNull("subCountText"))
                    video.setSubCountText(obj.getString("subCountText"));
                video.setLengthSeconds(obj.getInt("lengthSeconds"));
                if (!obj.isNull("allowRatings"))
                    video.setAllowRatings(obj.getBoolean("allowRatings"));
                if (!obj.isNull("rating")) video.setRating((float) obj.getDouble("rating"));
                if (!obj.isNull("isListed")) video.setListed(obj.getBoolean("isListed"));
                if (!obj.isNull("liveNow")) video.setLiveNow(obj.getBoolean("liveNow"));

                if (!obj.isNull("dashUrl")) video.setDashUrl(obj.getString("dashUrl"));
                if (!obj.isNull("hlsUrl")) video.setHlsUrl(obj.getString("hlsUrl"));

                if (!obj.isNull("videoThumbnails")) {
                    JSONArray thumbnails = obj.getJSONArray("videoThumbnails");
                    List<Thumbnail> videoThumbnails = new ArrayList<Thumbnail>();
                    for (int j = 0; j < thumbnails.length(); j++) {
                        JSONObject thumbObj = thumbnails.getJSONObject(j);
                        Thumbnail thumbnail = new Thumbnail();
                        thumbnail.setQuality(thumbObj.getString("quality"));
                        thumbnail.setUrl(thumbObj.getString("url"));
                        thumbnail.setWidth(thumbObj.getInt("width"));
                        thumbnail.setHeight(thumbObj.getInt("height"));
                        videoThumbnails.add(thumbnail);
                    }
                    video.setVideoThumbnails(videoThumbnails);
                }

                if (!obj.isNull("authorThumbnails")) {
                    JSONArray thumbnails = obj.getJSONArray("authorThumbnails");
                    List<Thumbnail> authorThumbnails = new ArrayList<Thumbnail>();
                    for (int j = 0; j < thumbnails.length(); j++) {
                        JSONObject thumbObj = thumbnails.getJSONObject(j);
                        Thumbnail thumbnail = new Thumbnail();
                        if (!thumbObj.isNull("url")) thumbnail.setUrl(thumbObj.getString("url"));
                        if (!thumbObj.isNull("width")) thumbnail.setWidth(thumbObj.getInt("width"));
                        if (!thumbObj.isNull("height"))
                            thumbnail.setHeight(thumbObj.getInt("height"));
                        authorThumbnails.add(thumbnail);
                    }
                    video.setAuthorThumbnails(authorThumbnails);
                }

                if (!obj.isNull("formatStreams")) {
                    JSONArray streams = obj.getJSONArray("formatStreams");
                    List<FormatStream> formatStreams = new ArrayList<FormatStream>();
                    for (int j = 0; j < streams.length(); j++) {
                        JSONObject streamObj = streams.getJSONObject(j);
                        FormatStream stream = new FormatStream();
                        stream.setUrl(streamObj.getString("url"));
                        stream.setItag(streamObj.getString("itag"));
                        stream.setType(streamObj.getString("type"));
                        stream.setQuality(streamObj.getString("quality"));
                        if (!streamObj.isNull("bitrate"))
                            stream.setBitrate(streamObj.getString("bitrate"));
                        stream.setContainer(streamObj.getString("container"));
                        stream.setEncoding(streamObj.getString("encoding"));
                        stream.setQualityLabel(streamObj.getString("qualityLabel"));
                        stream.setResolution(streamObj.getString("resolution"));
                        stream.setSize(streamObj.getString("size"));
                        formatStreams.add(stream);
                    }
                    video.setFormatStreams(formatStreams);
                }

                if (!obj.isNull("adaptiveFormats")) {
                    JSONArray formats = obj.getJSONArray("adaptiveFormats");
                    List<AdaptiveFormat> adaptiveFormats = new ArrayList<AdaptiveFormat>();
                    for (int j = 0; j < formats.length(); j++) {
                        JSONObject formatObj = formats.getJSONObject(j);
                        AdaptiveFormat format = new AdaptiveFormat();

                        if (!formatObj.isNull("index"))
                            format.setIndex(formatObj.getString("index"));
                        if (!formatObj.isNull("bitrate"))
                            format.setBitrate(formatObj.getString("bitrate"));
                        if (!formatObj.isNull("init")) format.setInit(formatObj.getString("init"));
                        format.setUrl(formatObj.getString("url"));
                        format.setItag(formatObj.getString("itag"));
                        format.setType(formatObj.getString("type"));
                        if (!formatObj.isNull("clen")) format.setClen(formatObj.getString("clen"));
                        if (!formatObj.isNull("lmt")) format.setLmt(formatObj.getString("lmt"));
                        if (!formatObj.isNull("projectionType"))
                            format.setProjectionType(formatObj.getString("projectionType"));
                        if (!formatObj.isNull("container"))
                            format.setContainer(formatObj.getString("container"));
                        if (!formatObj.isNull("encoding"))
                            format.setEncoding(formatObj.getString("encoding"));
                        if (!formatObj.isNull("qualityLabel"))
                            format.setQualityLabel(formatObj.getString("qualityLabel"));
                        if (!formatObj.isNull("resolution"))
                            format.setResolution(formatObj.getString("resolution"));
                        if (!formatObj.isNull("fps")) format.setFps(formatObj.getInt("fps"));
                        if (!formatObj.isNull("size")) format.setSize(formatObj.getString("size"));

                        adaptiveFormats.add(format);
                    }
                    video.setAdaptiveFormats(adaptiveFormats);
                }

                if (!obj.isNull("captions")) {
                    JSONArray captionsArray = obj.getJSONArray("captions");
                    List<Caption> captions = new ArrayList<Caption>();
                    for (int j = 0; j < captionsArray.length(); j++) {
                        JSONObject captionObj = captionsArray.getJSONObject(j);
                        Caption caption = new Caption();
                        caption.setLabel(captionObj.getString("label"));
                        caption.setLanguage_code(captionObj.getString("language_code"));
                        caption.setUrl(captionObj.getString("url"));
                        captions.add(caption);
                    }
                    video.setCaptions(captions);
                }

                if (!obj.isNull("recommendedVideos")) {
                    JSONArray recommendedArray = obj.getJSONArray("recommendedVideos");
                    List<RecommendedVideo> recommendedVideos = new ArrayList<RecommendedVideo>();
                    for (int j = 0; j < recommendedArray.length(); j++) {
                        JSONObject recObj = recommendedArray.getJSONObject(j);
                        RecommendedVideo recommendedVideo = new RecommendedVideo();
                        recommendedVideo.setVideoId(recObj.getString("videoId"));
                        recommendedVideo.setTitle(recObj.getString("title"));

                        if (!recObj.isNull("videoThumbnails")) {
                            JSONArray thumbs = recObj.getJSONArray("videoThumbnails");
                            List<Thumbnail> thumbnails = new ArrayList<Thumbnail>();
                            for (int k = 0; k < thumbs.length(); k++) {
                                JSONObject thumbObj = thumbs.getJSONObject(k);
                                Thumbnail thumbnail = new Thumbnail();
                                thumbnail.setQuality(thumbObj.getString("quality"));
                                thumbnail.setUrl(thumbObj.getString("url"));
                                thumbnail.setWidth(thumbObj.getInt("width"));
                                thumbnail.setHeight(thumbObj.getInt("height"));
                                thumbnails.add(thumbnail);
                            }
                            recommendedVideo.setVideoThumbnails(thumbnails);
                        }

                        recommendedVideo.setAuthor(recObj.getString("author"));
                        recommendedVideo.setAuthorUrl(recObj.getString("authorUrl"));
                        if (!recObj.isNull("authorId"))
                            recommendedVideo.setAuthorId(recObj.getString("authorId"));
                        if (!recObj.isNull("authorVerified"))
                            recommendedVideo.setAuthorVerified(recObj.getBoolean("authorVerified"));

                        if (!recObj.isNull("authorThumbnails")) {
                            JSONArray thumbs = recObj.getJSONArray("authorThumbnails");
                            List<Thumbnail> thumbnails = new ArrayList<Thumbnail>();
                            for (int k = 0; k < thumbs.length(); k++) {
                                JSONObject thumbObj = thumbs.getJSONObject(k);
                                Thumbnail thumbnail = new Thumbnail();
                                thumbnail.setUrl(thumbObj.getString("url"));
                                thumbnail.setWidth(thumbObj.getInt("width"));
                                thumbnail.setHeight(thumbObj.getInt("height"));
                                thumbnails.add(thumbnail);
                            }
                            recommendedVideo.setAuthorThumbnails(thumbnails);
                        }

                        recommendedVideo.setLengthSeconds(recObj.getInt("lengthSeconds"));
                        if (!recObj.isNull("viewCount"))
                            recommendedVideo.setViewCount(recObj.getLong("viewCount"));
                        if (!recObj.isNull("viewCountText"))
                            recommendedVideo.setViewCountText(recObj.getString("viewCountText"));

                        recommendedVideos.add(recommendedVideo);
                    }
                    video.setRecommendedVideos(recommendedVideos);
                }

                return video;
            }
        }.execute(apiUrl);
    }

    public void getVideoComments(final String videoId, final ApiCallback<CommentsResponse> callback) {
        String apiUrl = baseUrl + "/api/v1/comments/" + videoId;

        new FetchDataTask<CommentsResponse>(callback) {
            @Override
            protected CommentsResponse parseJson(String jsonString) throws JSONException {
                JSONObject obj = new JSONObject(jsonString);
                CommentsResponse response = new CommentsResponse();

                response.setVideoId(obj.getString("videoId"));
                if (!obj.isNull("commentCount"))
                    response.setCommentCount(obj.getInt("commentCount"));
                if (!obj.isNull("continuation"))
                    response.setContinuation(obj.getString("continuation"));

                if (!obj.isNull("comments")) {
                    JSONArray commentsArray = obj.getJSONArray("comments");
                    List<Comment> comments = new ArrayList<Comment>();

                    for (int i = 0; i < commentsArray.length(); i++) {
                        JSONObject commentObj = commentsArray.getJSONObject(i);
                        Comment comment = new Comment();

                        comment.setAuthor(commentObj.getString("author"));

                        if (!commentObj.isNull("authorThumbnails")) {
                            JSONArray thumbs = commentObj.getJSONArray("authorThumbnails");
                            List<Thumbnail> thumbnails = new ArrayList<Thumbnail>();
                            for (int j = 0; j < thumbs.length(); j++) {
                                JSONObject thumbObj = thumbs.getJSONObject(j);
                                Thumbnail thumbnail = new Thumbnail();
                                thumbnail.setUrl(thumbObj.getString("url"));
                                thumbnail.setWidth(thumbObj.getInt("width"));
                                thumbnail.setHeight(thumbObj.getInt("height"));
                                thumbnails.add(thumbnail);
                            }
                            comment.setAuthorThumbnails(thumbnails);
                        }

                        comment.setAuthorId(commentObj.getString("authorId"));
                        comment.setAuthorUrl(commentObj.getString("authorUrl"));

                        if (!commentObj.isNull("isEdited"))
                            comment.setEdited(commentObj.getBoolean("isEdited"));
                        if (!commentObj.isNull("isPinned"))
                            comment.setPinned(commentObj.getBoolean("isPinned"));
                        if (!commentObj.isNull("isSponsor"))
                            comment.setSponsor(commentObj.getBoolean("isSponsor"));
                        if (!commentObj.isNull("sponsorIconUrl"))
                            comment.setSponsorIconUrl(commentObj.getString("sponsorIconUrl"));

                        comment.setContent(commentObj.getString("content"));
                        comment.setContentHtml(commentObj.getString("contentHtml"));
                        comment.setPublished(commentObj.getLong("published"));
                        comment.setPublishedText(commentObj.getString("publishedText"));
                        comment.setLikeCount(commentObj.getInt("likeCount"));
                        comment.setCommentId(commentObj.getString("commentId"));

                        if (!commentObj.isNull("authorIsChannelOwner")) {
                            comment.setAuthorIsChannelOwner(commentObj.getBoolean("authorIsChannelOwner"));
                        }

                        if (!commentObj.isNull("creatorHeart")) {
                            JSONObject heartObj = commentObj.getJSONObject("creatorHeart");
                            CreatorHeart heart = new CreatorHeart();
                            heart.setCreatorThumbnail(heartObj.getString("creatorThumbnail"));
                            heart.setCreatorName(heartObj.getString("creatorName"));
                            comment.setCreatorHeart(heart);
                        }

                        if (!commentObj.isNull("replies")) {
                            JSONObject repliesObj = commentObj.getJSONObject("replies");
                            CommentReplies replies = new CommentReplies();
                            replies.setReplyCount(repliesObj.getInt("replyCount"));
                            if (!repliesObj.isNull("continuation")) {
                                replies.setContinuation(repliesObj.getString("continuation"));
                            }
                            comment.setReplies(replies);
                        }

                        comments.add(comment);
                    }

                    response.setComments(comments);
                }

                return response;
            }
        }.execute(apiUrl);
    }

    public void searchVideos(final String query, final ApiCallback<List<SearchResult>> callback) {
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String apiUrl = baseUrl + "/api/v1/search?q=" + encodedQuery;

            new FetchDataTask<List<SearchResult>>(callback) {
                @Override
                protected List<SearchResult> parseJson(String jsonString) throws JSONException {
                    List<SearchResult> results = new ArrayList<SearchResult>();
                    JSONArray jsonArray = new JSONArray(jsonString);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        SearchResult result = new SearchResult();

                        result.setType(obj.getString("type"));

                        if (!obj.isNull("title")) result.setTitle(obj.getString("title"));
                        if (!obj.isNull("author")) result.setAuthor(obj.getString("author"));
                        if (!obj.isNull("authorId")) result.setAuthorId(obj.getString("authorId"));
                        if (!obj.isNull("authorUrl"))
                            result.setAuthorUrl(obj.getString("authorUrl"));

                        String type = obj.getString("type");

                        if ("video".equals(type)) {
                            result.setVideoId(obj.getString("videoId"));
                            if (!obj.isNull("description"))
                                result.setDescription(obj.getString("description"));
                            if (!obj.isNull("descriptionHtml"))
                                result.setDescriptionHtml(obj.getString("descriptionHtml"));
                            if (!obj.isNull("viewCount"))
                                result.setViewCount(obj.getLong("viewCount"));
                            if (!obj.isNull("published"))
                                result.setPublished(obj.getLong("published"));
                            if (!obj.isNull("publishedText"))
                                result.setPublishedText(obj.getString("publishedText"));
                            if (!obj.isNull("lengthSeconds"))
                                result.setLengthSeconds(obj.getInt("lengthSeconds"));
                            if (!obj.isNull("liveNow"))
                                result.setLiveNow(obj.getBoolean("liveNow"));
                            if (!obj.isNull("paid")) result.setPaid(obj.getBoolean("paid"));
                            if (!obj.isNull("premium"))
                                result.setPremium(obj.getBoolean("premium"));

                            if (!obj.isNull("videoThumbnails")) {
                                JSONArray thumbs = obj.getJSONArray("videoThumbnails");
                                List<Thumbnail> thumbnails = new ArrayList<Thumbnail>();
                                for (int j = 0; j < thumbs.length(); j++) {
                                    JSONObject thumbObj = thumbs.getJSONObject(j);
                                    Thumbnail thumbnail = new Thumbnail();
                                    thumbnail.setQuality(thumbObj.getString("quality"));
                                    thumbnail.setUrl(thumbObj.getString("url"));
                                    thumbnail.setWidth(thumbObj.getInt("width"));
                                    thumbnail.setHeight(thumbObj.getInt("height"));
                                    thumbnails.add(thumbnail);
                                }
                                result.setVideoThumbnails(thumbnails);
                            }
                        } else if ("playlist".equals(type)) {
                            result.setPlaylistId(obj.getString("playlistId"));
                            result.setPlaylistThumbnail(obj.getString("playlistThumbnail"));
                            if (!obj.isNull("authorVerified"))
                                result.setAuthorVerified(obj.getBoolean("authorVerified"));
                            if (!obj.isNull("videoCount"))
                                result.setVideoCount(obj.getInt("videoCount"));

                            if (!obj.isNull("videos")) {
                                JSONArray videosArray = obj.getJSONArray("videos");
                                List<PlaylistVideo> videos = new ArrayList<PlaylistVideo>();
                                for (int j = 0; j < videosArray.length(); j++) {
                                    JSONObject videoObj = videosArray.getJSONObject(j);
                                    PlaylistVideo video = new PlaylistVideo();
                                    video.setTitle(videoObj.getString("title"));
                                    video.setVideoId(videoObj.getString("videoId"));
                                    video.setLengthSeconds(videoObj.getInt("lengthSeconds"));

                                    JSONArray thumbs = videoObj.getJSONArray("videoThumbnails");
                                    List<Thumbnail> thumbnails = new ArrayList<Thumbnail>();
                                    for (int k = 0; k < thumbs.length(); k++) {
                                        JSONObject thumbObj = thumbs.getJSONObject(k);
                                        Thumbnail thumbnail = new Thumbnail();
                                        thumbnail.setQuality(thumbObj.getString("quality"));
                                        thumbnail.setUrl(thumbObj.getString("url"));
                                        thumbnail.setWidth(thumbObj.getInt("width"));
                                        thumbnail.setHeight(thumbObj.getInt("height"));
                                        thumbnails.add(thumbnail);
                                    }
                                    video.setVideoThumbnails(thumbnails);

                                    videos.add(video);
                                }
                                result.setVideos(videos);
                            }
                        } else if ("channel".equals(type)) {
                            if (!obj.isNull("authorThumbnails")) {
                                JSONArray thumbs = obj.getJSONArray("authorThumbnails");
                                List<Thumbnail> thumbnails = new ArrayList<Thumbnail>();
                                for (int j = 0; j < thumbs.length(); j++) {
                                    JSONObject thumbObj = thumbs.getJSONObject(j);
                                    Thumbnail thumbnail = new Thumbnail();
                                    thumbnail.setUrl(thumbObj.getString("url"));
                                    thumbnail.setWidth(thumbObj.getInt("width"));
                                    thumbnail.setHeight(thumbObj.getInt("height"));
                                    thumbnails.add(thumbnail);
                                }
                                result.setAuthorThumbnails(thumbnails);
                            }

                            if (!obj.isNull("autoGenerated"))
                                result.setAutoGenerated(obj.getBoolean("autoGenerated"));
                            if (!obj.isNull("subCount")) result.setSubCount(obj.getInt("subCount"));
                            if (!obj.isNull("videoCount"))
                                result.setVideoCount(obj.getInt("videoCount"));
                            if (!obj.isNull("description"))
                                result.setDescription(obj.getString("description"));
                            if (!obj.isNull("descriptionHtml"))
                                result.setDescriptionHtml(obj.getString("descriptionHtml"));
                        } else if ("hashtag".equals(type)) {
                            result.setUrl(obj.getString("url"));
                            if (!obj.isNull("channelCount"))
                                result.setChannelCount(obj.getInt("channelCount"));
                            if (!obj.isNull("videoCount"))
                                result.setVideoCount(obj.getInt("videoCount"));
                        }

                        results.add(result);
                    }

                    return results;
                }
            }.execute(apiUrl);
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    public void getSearchSuggestions(final String query, final ApiCallback<List<String>> callback) {
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String apiUrl = baseUrl + "/api/v1/search/suggestions?q=" + encodedQuery;

            new FetchDataTask<List<String>>(callback) {
                @Override
                protected List<String> parseJson(String jsonString) throws JSONException {
                    List<String> suggestions = new ArrayList<String>();
                    JSONArray jsonArray = new JSONArray(jsonString);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        suggestions.add(jsonArray.getString(i));
                    }

                    return suggestions;
                }
            }.execute(apiUrl);
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    public interface ApiCallback<T> {
        void onSuccess(T result);

        void onError(Exception e);
    }

    private abstract class FetchDataTask<T> extends AsyncTask<String, Void, T> {
        private Exception exception;
        private final ApiCallback<T> callback;

        public FetchDataTask(ApiCallback<T> callback) {
            this.callback = callback;
        }

        @Override
        protected T doInBackground(String... urls) {
            try {
                String jsonString = getJsonFromUrl(urls[0]);
                return parseJson(jsonString);
            } catch (Exception e) {
                this.exception = e;
                Log.e(TAG, "Error fetching data: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(T result) {
            if (exception != null) {
                callback.onError(exception);
            } else {
                callback.onSuccess(result);
            }
        }

        protected abstract T parseJson(String jsonString) throws JSONException;

        private String getJsonFromUrl(String urlString) throws IOException {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonString;

            try {
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                jsonString = buffer.toString();
                return jsonString;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
        }
    }
}