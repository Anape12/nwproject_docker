package jp.nw.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jp.nw.entity.ScheduleEventEntity;
import lombok.Getter;

@Getter
public class ScheduleCalendarDay {
    private final LocalDate date;
    private final boolean currentMonth;
    private final boolean today;
    private final List<ScheduleEventEntity> events = new ArrayList<>();

    public ScheduleCalendarDay(LocalDate date, boolean currentMonth, boolean today) {
        this.date = date;
        this.currentMonth = currentMonth;
        this.today = today;
    }

    public int getDayOfMonth() { return date.getDayOfMonth(); }
    public String getDateValue() { return date.toString(); }
}
