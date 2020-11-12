package com.radelec.reconmobile;

import android.graphics.Color;
import android.os.Bundle;
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

import java.util.Objects;

import static com.radelec.reconmobile.Globals.chartdataTemp;

public class ChartTemp extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logging.main("ChartTemp","OnCreateView() called!");
        View view = inflater.inflate(R.layout.chart_temp, container, false);
        if(Globals.connected == Globals.ReconConnected.Loaded && !Globals.globalLoadedFileName.isEmpty()) {
            Logging.main("ChartTemp","Recon Data File is loaded -- attempting to populate temperature chart.");
            view = populateTempChart (view);
        }
        return view;
    }

    public View populateTempChart(View view) {
        Logging.main("ChartTemp","populateTempChart() called!");

        LineChart lcTemp;
        LineData lineData;
        XAxis xAxis;
        final YAxis yAxis;

        //Assign layout element to the linechart lcTemp
        lcTemp = view.findViewById(R.id.chartTemp);

        LineDataSet lineDataSet = new LineDataSet(chartdataTemp,"°F");

        lineDataSet.setFillAlpha(110);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setCircleColor(R.color.colorPrimary);
        lineDataSet.setColor(R.color.colorPrimary);
        lineDataSet.setLineWidth(5f);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawValues(false);
        lineDataSet.setValueFormatter(new DefaultValueFormatter(0));
        lineDataSet.setFillColor(Color.argb(200,235,52,52));
        lineData = new LineData(lineDataSet);

        //Draw the actual graph with lineData
        lcTemp.setData(lineData);

        //General graph settings (applied after setData)
        lcTemp.fitScreen();
        lcTemp.setDrawBorders(false);
        lcTemp.setDrawGridBackground(false);
        lcTemp.setTouchEnabled(true);
        lcTemp.setPinchZoom(true);
        lcTemp.setScaleEnabled(true);
        lcTemp.setDragEnabled(true);
        lcTemp.setAutoScaleMinMaxEnabled(false);
        lcTemp.getAxisRight().setEnabled(false);
        lcTemp.getDescription().setEnabled(false);

        //Y-Axis formatting
        yAxis = lcTemp.getAxisLeft();
        yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        yAxis.setLabelCount(4,true);
        yAxis.setDrawGridLines(false);
        yAxis.setValueFormatter(new DefaultValueFormatter(1));
        yAxis.setAxisMinimum(yAxis.getAxisMinimum() * (float)0.50);
        yAxis.setAxisMaximum(yAxis.getAxisMaximum() * (float)1.25);

        //X-Axis formatting
        xAxis = lcTemp.getXAxis();
        xAxis.setLabelRotationAngle(90);
        xAxis.setCenterAxisLabels(true);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new GraphAxisFormatter());
        xAxis.setAvoidFirstLastClipping(true);

        //Modifiers if SI units are selected.
        if(Objects.equals(Globals.globalUnitType, "SI")) {
            lineDataSet.setLabel("°C");
            lineDataSet.setValueFormatter(new DefaultValueFormatter(1));
            lcTemp.invalidate(); //Is this needed?
        }

        return view;
    }

}
