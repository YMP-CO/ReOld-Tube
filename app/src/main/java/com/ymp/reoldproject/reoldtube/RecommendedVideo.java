package com.ymp.reoldproject.reoldtube;

import java.util.List;

public class RecommendedVideo {
    private String videoId;
    private String title;
    private List<Thumbnail> videoThumbnails;
    private String author;
    private String authorUrl;
    private String authorId;
    private boolean authorVerified;
    private List<Thumbnail> authorThumbnails;
    private int lengthSeconds;
    private long viewCount;
    private String viewCountText;

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Thumbnail> getVideoThumbnails() {
        return videoThumbnails;
    }

    public void setVideoThumbnails(List<Thumbnail> videoThumbnails) {
        this.videoThumbnails = videoThumbnails;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorUrl() {
        return authorUrl;
    }

    public void setAuthorUrl(String authorUrl) {
        this.authorUrl = authorUrl;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public boolean isAuthorVerified() {
        return authorVerified;
    }

    public void setAuthorVerified(boolean authorVerified) {
        this.authorVerified = authorVerified;
    }

    public List<Thumbnail> getAuthorThumbnails() {
        return authorThumbnails;
    }

    public void setAuthorThumbnails(List<Thumbnail> authorThumbnails) {
        this.authorThumbnails = authorThumbnails;
    }

    public int getLengthSeconds() {
        return lengthSeconds;
    }

    public void setLengthSeconds(int lengthSeconds) {
        this.lengthSeconds = lengthSeconds;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

    public String getViewCountText() {
        return viewCountText;
    }

    public void setViewCountText(String viewCountText) {
        this.viewCountText = viewCountText;
    }
}
