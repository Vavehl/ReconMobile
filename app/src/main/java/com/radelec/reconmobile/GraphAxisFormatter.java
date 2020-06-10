package com.radelec.reconmobile;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GraphAxisFormatter extends ValueFormatter {
    @Override
    public String getFormattedValue (float value) {
        Date date = new Date((long)value);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy hh:mm, ");
        return sdf.format(date);
    }
}
