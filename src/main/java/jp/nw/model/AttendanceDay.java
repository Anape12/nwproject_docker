package jp.nw.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import jp.nw.entity.AttendanceEntity;
import lombok.Getter;

@Getter
public class AttendanceDay {
    private final LocalDate date; private final AttendanceEntity attendance; private final boolean today;
    public AttendanceDay(LocalDate date,AttendanceEntity attendance){this.date=date;this.attendance=attendance;this.today=date.equals(LocalDate.now());}
    public int getDay(){return date.getDayOfMonth();} public String getDateValue(){return date.toString();}
    public String getWeekday(){return switch(date.getDayOfWeek()){case MONDAY->"月";case TUESDAY->"火";case WEDNESDAY->"水";case THURSDAY->"木";case FRIDAY->"金";case SATURDAY->"土";case SUNDAY->"日";};}
    public boolean isWeekend(){return date.getDayOfWeek()==DayOfWeek.SATURDAY||date.getDayOfWeek()==DayOfWeek.SUNDAY;}
}
