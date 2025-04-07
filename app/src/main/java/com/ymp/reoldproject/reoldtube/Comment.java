package com.ymp.reoldproject.reoldtube;

import java.util.List;

public class Comment {
    private String author;
    private List<Thumbnail> authorThumbnails;
    private String authorId;
    private String authorUrl;
    private boolean isEdited;
    private boolean isPinned;
    private Boolean isSponsor;
    private String sponsorIconUrl;
    private String content;
    private String contentHtml;
    private long published;
    private String publishedText;
    private int likeCount;
    private String commentId;
    private boolean authorIsChannelOwner;
    private CreatorHeart creatorHeart;
    private CommentReplies replies;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<Thumbnail> getAuthorThumbnails() {
        return authorThumbnails;
    }

    public void setAuthorThumbnails(List<Thumbnail> authorThumbnails) {
        this.authorThumbnails = authorThumbnails;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public void setAuthorUrl(String authorUrl) {
        this.authorUrl = authorUrl;
    }
    public void setEdited(boolean edited) {
        isEdited = edited;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    public void setSponsor(Boolean sponsor) {
        isSponsor = sponsor;
    }

    public void setSponsorIconUrl(String sponsorIconUrl) {
        this.sponsorIconUrl = sponsorIconUrl;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentHtml() {
        return contentHtml;
    }

    public void setContentHtml(String contentHtml) {
        this.contentHtml = contentHtml;
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

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public boolean isAuthorIsChannelOwner() {
        return authorIsChannelOwner;
    }

    public void setAuthorIsChannelOwner(boolean authorIsChannelOwner) {
        this.authorIsChannelOwner = authorIsChannelOwner;
    }

    public CreatorHeart getCreatorHeart() {
        return creatorHeart;
    }

    public void setCreatorHeart(CreatorHeart creatorHeart) {
        this.creatorHeart = creatorHeart;
    }

    public CommentReplies getReplies() {
        return replies;
    }

    public void setReplies(CommentReplies replies) {
        this.replies = replies;
    }
}
