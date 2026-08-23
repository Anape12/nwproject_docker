package jp.nw.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor @AllArgsConstructor @Data @Builder
public class AttendanceEntity {
    private long attendanceId;
    private String userId;
    private LocalDate workDate;
    private LocalTime clockIn;
    private LocalTime clockOut;
    private int breakMinutes;
    private String workType;
    private String note;
    private Long reportId;
    private String reportTitle;
    private String reportStatus;
    private String approvalStatus;

    public String getClockInValue(){return clockIn==null?"":clockIn.toString();}
    public String getClockOutValue(){return clockOut==null?"":clockOut.toString();}
    public String getWorkTypeLabel(){return switch(workType==null?"":workType){case "OFFICE"->"出社";case "REMOTE"->"リモート";case "LEAVE"->"休暇";case "HOLIDAY"->"休日";default->workType;};}
    public String getWorkingTimeLabel(){
        if(clockIn==null||clockOut==null)return "-";long minutes=ChronoUnit.MINUTES.between(clockIn,clockOut)-breakMinutes;
        return String.format("%d:%02d",Math.max(0,minutes)/60,Math.max(0,minutes)%60);
    }
    public boolean isEditable(){return "DRAFT".equals(approvalStatus)||"REJECTED".equals(approvalStatus);}
    public String getApprovalStatusLabel(){return switch(approvalStatus==null?"DRAFT":approvalStatus){case "DRAFT"->"下書き";case "SUBMITTED"->"承認待ち";case "APPROVED"->"承認済み";case "REJECTED"->"差戻し";default->approvalStatus;};}
}
