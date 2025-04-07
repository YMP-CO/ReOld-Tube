package com.ymp.reoldproject.reoldtube;

import java.util.List;

public class Video {
    private String type;
    private String title;
    private String videoId;
    private List<Thumbnail> videoThumbnails;
    private List<Storyboard> storyboards;
    private String description;
    private String descriptionHtml;
    private long published;
    private String publishedText;
    private List<String> keywords;
    private long viewCount;
    private int likeCount;
    private int dislikeCount;
    private boolean paid;
    private boolean premium;
    private boolean isFamilyFriendly;
    private List<String> allowedRegions;
    private String genre;
    private String genreUrl;
    private String author;
    private String authorId;
    private String authorUrl;
    private List<Thumbnail> authorThumbnails;
    private String subCountText;
    private int lengthSeconds;
    private boolean allowRatings;
    private float rating;
    private boolean isListed;
    private boolean liveNow;
    private boolean isPostLiveDvr;
    private boolean isUpcoming;
    private String dashUrl;
    private Long premiereTimestamp;
    private String hlsUrl;
    private List<AdaptiveFormat> adaptiveFormats;
    private List<FormatStream> formatStreams;
    private List<Caption> captions;
    private List<MusicTrack> musicTracks;
    private List<RecommendedVideo> recommendedVideos;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

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

    public List<Thumbnail> getVideoThumbnails() {
        return videoThumbnails;
    }

    public void setVideoThumbnails(List<Thumbnail> videoThumbnails) {
        this.videoThumbnails = videoThumbnails;
    }

    public List<Storyboard> getStoryboards() {
        return storyboards;
    }

    public void setStoryboards(List<Storyboard> storyboards) {
        this.storyboards = storyboards;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionHtml() {
        return descriptionHtml;
    }

    public void setDescriptionHtml(String descriptionHtml) {
        this.descriptionHtml = descriptionHtml;
    }

    public long getPublished() {
        return published;
    }

    public void setPublished(long published) {
        this.published = published;
    }

    public String getPublishedText() {
        return publishedText;
    }

    public void setPublishedText(String publishedText) {
        this.publishedText = publishedText;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getDislikeCount() {
        return dislikeCount;
    }

    public void setDislikeCount(int dislikeCount) {
        this.dislikeCount = dislikeCount;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    public boolean isFamilyFriendly() {
        return isFamilyFriendly;
    }

    public void setFamilyFriendly(boolean familyFriendly) {
        isFamilyFriendly = familyFriendly;
    }

    public List<String> getAllowedRegions() {
        return allowedRegions;
    }

    public void setAllowedRegions(List<String> allowedRegions) {
        this.allowedRegions = allowedRegions;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getGenreUrl() {
        return genreUrl;
    }

    public void setGenreUrl(String genreUrl) {
        this.genreUrl = genreUrl;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorUrl() {
        return authorUrl;
    }

    public void setAuthorUrl(String authorUrl) {
        this.authorUrl = authorUrl;
    }

    public List<Thumbnail> getAuthorThumbnails() {
        return authorThumbnails;
    }

    public void setAuthorThumbnails(List<Thumbnail> authorThumbnails) {
        this.authorThumbnails = authorThumbnails;
    }

    public String getSubCountText() {
        return subCountText;
    }

    public void setSubCountText(String subCountText) {
        this.subCountText = subCountText;
    }

    public int getLengthSeconds() {
        return lengthSeconds;
    }

    public void setLengthSeconds(int lengthSeconds) {
        this.lengthSeconds = lengthSeconds;
    }

    public boolean isAllowRatings() {
        return allowRatings;
    }

    public void setAllowRatings(boolean allowRatings) {
        this.allowRatings = allowRatings;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public boolean isListed() {
        return isListed;
    }

    public void setListed(boolean listed) {
        isListed = listed;
    }

    public boolean isLiveNow() {
        return liveNow;
    }

    public void setLiveNow(boolean liveNow) {
        this.liveNow = liveNow;
    }

    public boolean isPostLiveDvr() {
        return isPostLiveDvr;
    }

    public void setPostLiveDvr(boolean postLiveDvr) {
        isPostLiveDvr = postLiveDvr;
    }

    public boolean isUpcoming() {
        return isUpcoming;
    }

    public void setUpcoming(boolean upcoming) {
        isUpcoming = upcoming;
    }

    public String getDashUrl() {
        return dashUrl;
    }

    public void setDashUrl(String dashUrl) {
        this.dashUrl = dashUrl;
    }

    public Long getPremiereTimestamp() {
        return premiereTimestamp;
    }

    public void setPremiereTimestamp(Long premiereTimestamp) {
        this.premiereTimestamp = premiereTimestamp;
    }

    public String getHlsUrl() {
        return hlsUrl;
    }

    public void setHlsUrl(String hlsUrl) {
        this.hlsUrl = hlsUrl;
    }

    public List<AdaptiveFormat> getAdaptiveFormats() {
        return adaptiveFormats;
    }

    public void setAdaptiveFormats(List<AdaptiveFormat> adaptiveFormats) {
        this.adaptiveFormats = adaptiveFormats;
    }

    public List<FormatStream> getFormatStreams() {
        return formatStreams;
    }

    public void setFormatStreams(List<FormatStream> formatStreams) {
        this.formatStreams = formatStreams;
    }

    public List<Caption> getCaptions() {
        return captions;
    }

    public void setCaptions(List<Caption> captions) {
        this.captions = captions;
    }

    public List<MusicTrack> getMusicTracks() {
        return musicTracks;
    }

    public void setMusicTracks(List<MusicTrack> musicTracks) {
        this.musicTracks = musicTracks;
    }

    public List<RecommendedVideo> getRecommendedVideos() {
        return recommendedVideos;
    }

    public void setRecommendedVideos(List<RecommendedVideo> recommendedVideos) {
        this.recommendedVideos = recommendedVideos;
    }
}

