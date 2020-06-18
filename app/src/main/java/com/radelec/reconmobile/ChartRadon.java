package com.radelec.reconmobile;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;

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

        //Assign layout element to the linechart lcRadon
        lcRadon = view.findViewById(R.id.chartRadon);

        LineDataSet lineDataSet = new LineDataSet(chartdataRadon,"pCi/L");

        lineDataSet.setFillAlpha(110);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setLineWidth(5f);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setValueTextSize(10f);
        lineDataSet.setValueFormatter(new DefaultValueFormatter(1));
        lineData = new LineData(lineDataSet);

        Paint paint = lcRadon.getRenderer().getPaintRender();
        int height = lcRadon.getHeight();

        LinearGradient linGrad = new LinearGradient(0, 0, 0, height,
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_blue_dark),
                Shader.TileMode.REPEAT);
        paint.setShader(linGrad);

        //Draw the actual graph with lineData
        lcRadon.setData(lineData);

        //General graph settings (applied after setData)
        lcRadon.fitScreen();
        lcRadon.setDrawBorders(false);
        lcRadon.setDrawGridBackground(false);
        lcRadon.setTouchEnabled(true);
        lcRadon.setPinchZoom(true);
        lcRadon.setScaleEnabled(true);
        lcRadon.setDragEnabled(true);
        lcRadon.setAutoScaleMinMaxEnabled(true);
        lcRadon.getAxisRight().setEnabled(false);
        lcRadon.getDescription().setEnabled(false);

        //Y-Axis formatting
        yAxis = lcRadon.getAxisLeft();
        yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        yAxis.setLabelCount(4,true);
        yAxis.setDrawGridLines(false);
        yAxis.setValueFormatter(new DefaultValueFormatter(1));
        yAxis.setAxisMaximum(yAxis.getAxisMaximum() * (float)1.25);
        yAxis.setAxisMinimum(yMin);

        //X-Axis formatting
        xAxis = lcRadon.getXAxis();
        xAxis.setLabelRotationAngle(90);
        xAxis.setCenterAxisLabels(true);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new GraphAxisFormatter());
        xAxis.setAvoidFirstLastClipping(true);

        //Modifiers if SI units are selected.
        if(Globals.globalUnitType=="SI") {
            lineDataSet.setLabel("Bq/m³");
            yAxis.setValueFormatter(new DefaultValueFormatter(0));
            lcRadon.invalidate(); //Is this needed?
        }

        return view;
    }

}
