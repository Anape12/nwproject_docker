package jp.nw.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor @AllArgsConstructor @Data @Builder
public class ApprovalRequestEntity {
    private long approvalId; private String applicationType; private long targetId; private String applicantId; private String applicantName;
    private String status; private LocalDate targetDate; private String title; private String detail; private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt; private String reviewerId; private String reviewerName; private String reviewComment;
    public String getTypeLabel(){return "REPORT".equals(applicationType)?"報告書":"勤怠";}
    public String getStatusLabel(){return switch(status==null?"":status){case "SUBMITTED"->"承認待ち";case "APPROVED"->"承認済み";case "REJECTED"->"差戻し";default->status;};}
    public String getSubmittedAtLabel(){return submittedAt==null?"-":submittedAt.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));}
    public String getReviewedAtLabel(){return reviewedAt==null?"-":reviewedAt.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));}
}
