package com.toda.todamoon_v1;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    public static String getCurrentDate() {
        // Get the current date and time
        Date currentDate = Calendar.getInstance().getTime();

        // Define the desired date format
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        // Format the current date
        return formatter.format(currentDate);
    }
}
