package jp.nw.dto;

import java.sql.Timestamp;

public class ThreadCommentDto {

    private int commentId;
    private int threadId;
    private String authorId;
    private String commentText;
    private Timestamp createdAt;
    private String authorAccountType;

    public int getCommentId() {
        return commentId;
    }

    public void setCommentId(int commentId) {
        this.commentId = commentId;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    public String getAuthorAccountType() { return authorAccountType; }
    public void setAuthorAccountType(String authorAccountType) { this.authorAccountType = authorAccountType; }
}
