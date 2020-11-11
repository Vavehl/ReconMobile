package com.radelec.reconmobile;

import android.util.Log;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GraphAxisFormatter extends ValueFormatter {
    @Override
    public String getFormattedValue (float value) {
        try {
            long longEpochMilliseconds = (long)value * 60000;
            Date date = new Date(longEpochMilliseconds); //This result needs to be in Epoch milliseconds.
            SimpleDateFormat sdf = new SimpleDateFormat(" dd-MMM-yyyy HH:mm ");
            Logging.main("GraphAxisFormatter","Converting Epoch Time (" + value + ") to human-readable time (" + date.toString() + ").");
            return sdf.format(date);
        } catch (Exception ex) {
            return Float.toString(value);
        }
    }
}
