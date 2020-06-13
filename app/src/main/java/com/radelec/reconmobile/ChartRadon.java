package com.radelec.reconmobile;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Utils;

import java.util.ArrayList;

import static com.radelec.reconmobile.Globals.chartdataRadon;

public class ChartRadon extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("ChartRadon","OnCreateView() called!");
        View view = inflater.inflate(R.layout.chart_radon, container, false);
        if(Globals.connected == Globals.ReconConnected.Loaded && !Globals.globalLoadedFileName.isEmpty()) {
            Log.d("ChartRadon","Recon Data File is loaded -- attempting to populate radon chart.");
            view = populateRadonChart (view);
        }
        return view;
    }

    public View populateRadonChart(View view) {
        Log.d("ChartRadon","populateRadonChart() called!");

        LineChart lcRadon;
        LineData lineData;
        XAxis xAxis;
        final YAxis yAxis;
        float yMin = 0;

        //General graph settings
        lcRadon = view.findViewById(R.id.chartRadon);
        lcRadon.setDrawBorders(false);
        lcRadon.setDrawGridBackground(false);
        lcRadon.setTouchEnabled(true);
        lcRadon.setPinchZoom(true);
        lcRadon.setScaleEnabled(true);
        lcRadon.setDragEnabled(true);
        //lcRadon.setAutoScaleMinMaxEnabled(false);
        lcRadon.getAxisRight().setEnabled(false);
        lcRadon.getDescription().setEnabled(true);
        //lcRadon.setVisibleXRangeMaximum(24);

        //Y-Axis formatting
        yAxis = lcRadon.getAxisLeft();
        yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        yAxis.setLabelCount(4,true);
        yAxis.setDrawGridLines(false);
        yAxis.setValueFormatter(new DefaultValueFormatter(1));
        yAxis.setAxisMinimum(yMin);
        //yAxis.setAxisMaximum(yAxis.getAxisMaximum()*(float)1.25);

        //X-Axis formatting
        xAxis = lcRadon.getXAxis();
        xAxis.setLabelRotationAngle(45);
        //xAxis.setLabelCount(24);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new GraphAxisFormatter());
        xAxis.setAvoidFirstLastClipping(true);

        LineDataSet lineDataSet = new LineDataSet(chartdataRadon,"pCi/L");

        //lineDataSet.setColors(ColorTemplate.getHoloBlue());
        lineDataSet.setFillAlpha(110);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setCircleColor(R.color.colorPrimary);
        lineDataSet.setColor(R.color.colorPrimary);
        lineDataSet.setLineWidth(5f);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setValueTextSize(10f);
        lineDataSet.setValueFormatter(new DefaultValueFormatter(1));
        lineDataSet.setFillColor(Color.argb(200,99,2,10));
        lineData = new LineData(lineDataSet);
        //Draw the actual graph with lineData
        //lcRadon.fitScreen();
        lcRadon.setData(lineData);

        //Modifiers if SI units are selected.
        if(Globals.globalUnitType=="SI") {
            lineDataSet.setLabel("Bq/m³");
            yAxis.setValueFormatter(new DefaultValueFormatter(0));
        }

        return view;
    }

}
