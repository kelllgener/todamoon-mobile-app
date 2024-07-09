package com.toda.todamoon_v1;

import android.os.Handler;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTime {

    public static void startDateTimeUpdater(final TextView view) {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                // Get current date and time
                String currentDateTime = getCurrentDateTime();
                // Update TextView with current date and time
                view.setText(currentDateTime);
                // Call this runnable again after 1 second
                handler.postDelayed(this, 1000);
            }
        });
    }

    private static String getCurrentDateTime() {
        // Get current date and time
        Date currentDate = new Date();
        // Format date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("E | MMM dd, yyyy h:mm:ss a", Locale.getDefault());
        return dateFormat.format(currentDate);
    }
}
