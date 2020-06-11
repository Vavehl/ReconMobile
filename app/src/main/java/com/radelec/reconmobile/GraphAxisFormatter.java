package com.radelec.reconmobile;

import android.util.Log;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GraphAxisFormatter extends ValueFormatter {
    @Override
    public String getFormattedValue (float value) {
        try {
            Date date = new Date((long) value * 1000); //value will be in Epoch seconds, which we need to convert to Epoch millis (by multiplying by 1000).
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy hh:mm, ");
            //Log.d("GraphAxisFormatter","Converting Epoch Time (" + value + ") to human-readable time (" + date.toString() + ").");
            return sdf.format(date);
        } catch (Exception ex) {
            return Float.toString(value);
        }
    }
}
