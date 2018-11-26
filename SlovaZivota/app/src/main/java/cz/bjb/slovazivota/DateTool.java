package cz.bjb.slovazivota;

import java.util.Calendar;
import java.util.Date;

public class DateTool {
    final private String[] months = {"ledna", "února", "března", "dubna", "května", "června",
            "července", "srpna", "září", "října", "listopadu", "prosince"};
    private Calendar calendar;

    public DateTool() {
        calendar = Calendar.getInstance();
        calendar.setTime(new Date());
    }

    public int getDay() {
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public int getMonth() {
        return calendar.get(Calendar.MONTH);
    }

    public int getYear() {
        return calendar.get(Calendar.YEAR);
    }

    public void add(int day) {
        calendar.add(Calendar.DAY_OF_MONTH, day);
    }

    public String getFileName() {
        return Integer.toString(getYear()) + "/" +
                Integer.toString(getMonth()) + "/" +
                Integer.toString(getDay()) + ".txt";
    }

    public String toString() {
        return Integer.toString(getDay()) + ". " + months[getMonth()];
    }
}