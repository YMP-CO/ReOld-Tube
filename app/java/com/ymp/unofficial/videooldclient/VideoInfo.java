// Внутри VideoInfo.java
package com.ymp.unofficial.videooldclient;

import java.io.Serializable;
import java.util.List;

public class VideoInfo implements Serializable {
    public String videoId;
    public String videoUrl;
    public String thumbnailUrl;
    public String title;
    List<String> comments;

    // Новые поля для похожих видео
    public List<VideoInfo> recommendedVideos;
}
