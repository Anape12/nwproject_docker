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
public class ScheduleEventEntity {
    private long eventId;
    private String userId;
    private String title;
    private String description;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private boolean allDay;
    private String color;
    private String visibility;
    private String recurrenceRule;
    private LocalDate recurrenceUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getStartDateValue() { return startAt == null ? "" : startAt.toLocalDate().toString(); }
    public String getEndDateValue() { return endAt == null ? "" : (allDay ? endAt.minusDays(1) : endAt).toLocalDate().toString(); }
    public String getStartTimeValue() { return formatTime(startAt); }
    public String getEndTimeValue() { return formatTime(endAt); }
    public String getTimeLabel() { return allDay ? "終日" : formatTime(startAt); }
    public LocalDate getLastDisplayDate() { return endAt.minusNanos(1).toLocalDate(); }
    public String getVisibilityLabel(){return "SHARED".equals(visibility)?"共有":"非公開";}

    private String formatTime(LocalDateTime value) {
        return value == null ? "" : value.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
