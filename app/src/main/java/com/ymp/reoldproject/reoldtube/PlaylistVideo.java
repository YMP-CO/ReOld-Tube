package com.ymp.reoldproject.reoldtube;

import java.util.List;

public class PlaylistVideo {
    private String title;
    private String videoId;
    private int lengthSeconds;
    private List<Thumbnail> videoThumbnails;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public int getLengthSeconds() {
        return lengthSeconds;
    }

    public void setLengthSeconds(int lengthSeconds) {
        this.lengthSeconds = lengthSeconds;
    }

    public List<Thumbnail> getVideoThumbnails() {
        return videoThumbnails;
    }

    public void setVideoThumbnails(List<Thumbnail> videoThumbnails) {
        this.videoThumbnails = videoThumbnails;
    }
}
