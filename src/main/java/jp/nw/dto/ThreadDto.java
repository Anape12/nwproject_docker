package jp.nw.dto;

import java.sql.Timestamp;

public class ThreadDto {

    private int threadId;
    private String title;
    private String authorId;
    private String content;
    private String threadContent;
    private String status;
    private String closedById;
    private Timestamp closedAt;

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getThreadContent() {
        return threadContent;
    }

    public void setThreadContent(String threadContent) {
        this.threadContent = threadContent;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getClosedById() { return closedById; }
    public void setClosedById(String closedById) { this.closedById = closedById; }
    public Timestamp getClosedAt() { return closedAt; }
    public void setClosedAt(Timestamp closedAt) { this.closedAt = closedAt; }
    public boolean isClosed() { return "CLOSED".equals(status); }
}
