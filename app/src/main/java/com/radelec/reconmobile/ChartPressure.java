package com.radelec.reconmobile;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;

import static com.radelec.reconmobile.Globals.chartdataPressure;

public class ChartPressure extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("ChartPressure","OnCreateView() called!");
        View view = inflater.inflate(R.layout.chart_pressure, container, false);
        if(Globals.connected == Globals.ReconConnected.Loaded && !Globals.globalLoadedFileName.isEmpty()) {
            Log.d("ChartPressure","Recon Data File is loaded -- attempting to populate pressure chart.");
            view = populatePressureChart (view);
        }
        return view;
    }

    public View populatePressureChart(View view) {
        Log.d("ChartPressure","populatePressureChart() called!");

        LineChart lcPressure;
        LineData lineData;
        XAxis xAxis;
        final YAxis yAxis;

        //Assign layout element to the linechart lcPressure
        lcPressure = view.findViewById(R.id.chartPressure);

        LineDataSet lineDataSet = new LineDataSet(chartdataPressure,"inHg");

        lineDataSet.setFillAlpha(110);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setCircleColor(R.color.colorPrimary);
        lineDataSet.setColor(R.color.colorPrimary);
        lineDataSet.setLineWidth(5f);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawValues(false);
        lineDataSet.setValueFormatter(new DefaultValueFormatter(2));
        lineDataSet.setFillColor(Color.argb(200,192,192,192));
        lineData = new LineData(lineDataSet);

        //Draw the actual graph with lineData
        lcPressure.setData(lineData);

        //General graph settings (applied after setData)
        lcPressure.fitScreen();
        lcPressure.setDrawBorders(false);
        lcPressure.setDrawGridBackground(false);
        lcPressure.setTouchEnabled(true);
        lcPressure.setPinchZoom(true);
        lcPressure.setScaleEnabled(true);
        lcPressure.setDragEnabled(true);
        lcPressure.setAutoScaleMinMaxEnabled(false);
        lcPressure.getAxisRight().setEnabled(false);
        lcPressure.getDescription().setEnabled(false);

        //Y-Axis formatting
        yAxis = lcPressure.getAxisLeft();
        yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        yAxis.setLabelCount(4,true);
        yAxis.setDrawGridLines(false);
        yAxis.setValueFormatter(new DefaultValueFormatter(1));
        yAxis.setAxisMinimum(yAxis.getAxisMinimum() * (float)0.65);
        yAxis.setAxisMaximum(yAxis.getAxisMaximum() * (float)1.25);

        //X-Axis formatting
        xAxis = lcPressure.getXAxis();
        xAxis.setLabelRotationAngle(90);
        xAxis.setCenterAxisLabels(true);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new GraphAxisFormatter());
        xAxis.setAvoidFirstLastClipping(true);

        //Modifiers if SI units are selected.
        if(Globals.globalUnitType=="SI") {
            lineDataSet.setLabel("mbar");
            lcPressure.invalidate(); //Is this needed?
        }

        return view;
    }

}
