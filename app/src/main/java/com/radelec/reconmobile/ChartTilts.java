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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;

import static com.radelec.reconmobile.Globals.chartdataTilts;

public class ChartTilts extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logging.main("ChartTilts","OnCreateView() called!");
        View view = inflater.inflate(R.layout.chart_tilts, container, false);
        if(Globals.connected == Globals.ReconConnected.Loaded && !Globals.globalLoadedFileName.isEmpty()) {
            Logging.main("ChartTilts","Recon Data File is loaded -- attempting to populate tilts chart.");
            view = populateTiltsChart (view);
        }
        return view;
    }

    public View populateTiltsChart(View view) {
        Logging.main("ChartTilts","populateTiltsChart() called!");

        BarChart bcTilts;
        BarData barData;
        XAxis xAxis;
        final YAxis yAxis;

        //Assign layout element to the barchart bcTilts
        bcTilts = view.findViewById(R.id.chartTilts);

        BarDataSet barDataSet = new BarDataSet(chartdataTilts,"Tilts");

        barDataSet.setColor(R.color.colorPrimary);
        barDataSet.setDrawValues(true);
        barDataSet.setValueFormatter(new DefaultValueFormatter(0));
        barDataSet.setGradientColor(Color.argb(100,171,157,242),Color.argb(200,52,139,195));
        barData = new BarData(barDataSet);
        barData.setBarWidth(20f);
        barData.setValueTextSize(10f);

        //Draw the actual graph with barData
        bcTilts.setData(barData);

        //General graph settings (applied after setData)
        bcTilts.fitScreen();
        bcTilts.setDrawBorders(false);
        bcTilts.setDrawGridBackground(false);
        bcTilts.setTouchEnabled(true);
        bcTilts.setPinchZoom(true);
        bcTilts.setScaleEnabled(true);
        bcTilts.setDragEnabled(true);
        bcTilts.setAutoScaleMinMaxEnabled(false);
        bcTilts.getAxisRight().setEnabled(false);
        bcTilts.getDescription().setEnabled(false);

        //Y-Axis formatting
        yAxis = bcTilts.getAxisLeft();
        yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        yAxis.setLabelCount(10,true);
        yAxis.setDrawGridLines(false);
        yAxis.setValueFormatter(new DefaultValueFormatter(0));
        yAxis.setAxisMinimum(0);
        yAxis.setAxisMaximum(yAxis.getAxisMaximum() * (float)1.25);
        if(yAxis.getAxisMaximum()<10) yAxis.setAxisMaximum(10); //If the maximum y-axis isn't at least 10, let's set it to 10.

        //X-Axis formatting
        xAxis = bcTilts.getXAxis();
        xAxis.setLabelRotationAngle(90);
        xAxis.setCenterAxisLabels(true);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new GraphAxisFormatter());
        xAxis.setAvoidFirstLastClipping(true);

        return view;
    }

}
