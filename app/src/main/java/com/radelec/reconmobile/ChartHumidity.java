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

import static com.radelec.reconmobile.Globals.chartdataHumidity;

public class ChartHumidity extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logging.main("ChartHumidity","OnCreateView() called!");
        View view = inflater.inflate(R.layout.chart_humidity, container, false);
        if(Globals.connected == Globals.ReconConnected.Loaded && !Globals.globalLoadedFileName.isEmpty()) {
            Logging.main("ChartHumidity","Recon Data File is loaded -- attempting to populate humidity chart.");
            view = populateHumidityChart (view);
        }
        return view;
    }

    public View populateHumidityChart(View view) {
        Logging.main("ChartHumidity","populateHumidityChart() called!");

        LineChart lcHumidity;
        LineData lineData;
        XAxis xAxis;
        final YAxis yAxis;
        float yMin = 0;

        //Assign layout element to the linechart lcHumidity
        lcHumidity = view.findViewById(R.id.chartHumidity);

        LineDataSet lineDataSet = new LineDataSet(chartdataHumidity,"%");

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
        lineDataSet.setFillColor(Color.argb(200,52,155,235));
        lineData = new LineData(lineDataSet);

        //Draw the actual graph with lineData
        lcHumidity.setData(lineData);

        //General graph settings (applied after setData)
        lcHumidity.fitScreen();
        lcHumidity.setDrawBorders(false);
        lcHumidity.setDrawGridBackground(false);
        lcHumidity.setTouchEnabled(true);
        lcHumidity.setPinchZoom(true);
        lcHumidity.setScaleEnabled(true);
        lcHumidity.setDragEnabled(true);
        lcHumidity.setAutoScaleMinMaxEnabled(false);
        lcHumidity.getAxisRight().setEnabled(false);
        lcHumidity.getDescription().setEnabled(false);

        //Y-Axis formatting
        yAxis = lcHumidity.getAxisLeft();
        yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        yAxis.setLabelCount(4,true);
        yAxis.setDrawGridLines(false);
        yAxis.setValueFormatter(new DefaultValueFormatter(0));
        yAxis.setAxisMinimum(yMin);
        yAxis.setAxisMaximum(100);

        //X-Axis formatting
        xAxis = lcHumidity.getXAxis();
        xAxis.setLabelRotationAngle(90);
        xAxis.setCenterAxisLabels(true);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new GraphAxisFormatter());
        xAxis.setAvoidFirstLastClipping(true);

        return view;
    }

}
