package jp.nw.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WorkReportEntity {
    private long reportId;
    private String authorId;
    private String authorName;
    private LocalDate reportDate;
    private String title;
    private String body;
    private String status;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String reviewedById;
    private String reviewerName;
    private String reviewComment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isEditable() { return "DRAFT".equals(status) || "REJECTED".equals(status); }
    public String getStatusLabel() {
        return switch (status == null ? "" : status) {
            case "DRAFT" -> "下書き"; case "SUBMITTED" -> "承認待ち";
            case "APPROVED" -> "承認済み"; case "REJECTED" -> "差戻し"; default -> status;
        };
    }
    public String getReportDateValue() { return reportDate == null ? "" : reportDate.toString(); }
    public String getUpdatedAtLabel() { return format(updatedAt); }
    public String getSubmittedAtLabel() { return format(submittedAt); }
    public String getReviewedAtLabel() { return format(reviewedAt); }
    private String format(LocalDateTime value) { return value == null ? "-" : value.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")); }
}
