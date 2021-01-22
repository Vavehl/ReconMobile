package com.radelec.reconmobile;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class GraphAxisFormatter extends ValueFormatter {
    @Override
    public String getFormattedValue (float value) {
        try {
            long longEpochMilliseconds = (long)value * 1000L;
            SimpleDateFormat sdf = new SimpleDateFormat(" dd-MMM-yyyy HH:mm");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = new Date(longEpochMilliseconds); //This result needs to be in Epoch milliseconds.
            Logging.main("GraphAxisFormatter","Converting Epoch Time (" + longEpochMilliseconds + ") to human-readable time (" + date.toString() + ").");
            return sdf.format(date);
        } catch (Exception ex) {
            Logging.main("GraphAxisFormatter","Exception! Returning value as string...");
            return Float.toString(value);
        }
    }
}
