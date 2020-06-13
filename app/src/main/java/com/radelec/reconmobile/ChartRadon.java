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

        Float minAxisRange = Float.valueOf(0);
        LineChart lcRadon;
        LineData lineData;
        XAxis xAxis;
        final YAxis yAxis;
        float yMin = 0;
        GraphAxisFormatter gafXAxis = new GraphAxisFormatter();
        HorizontalScrollView svChartRadon = view.findViewById(R.id.svChartRadon);
        //ConstraintLayout layoutFragmentGraphs = view.findViewById(R.id.chartConstraintContainer);

        //System.out.println("chartConstraintContainer Height = " + layoutFragmentGraphs.getHeight());
        System.out.println("svChartRadon Height = " + svChartRadon.getHeight());

        //General graph settings
        lcRadon = view.findViewById(R.id.chartRadon);
        lcRadon.setDrawBorders(false);
        lcRadon.setDrawGridBackground(false);
        lcRadon.setTouchEnabled(true);
        lcRadon.setPinchZoom(true);
        lcRadon.setScaleEnabled(true);
        lcRadon.setDragEnabled(true);
        lcRadon.setAutoScaleMinMaxEnabled(false);
        lcRadon.getAxisRight().setEnabled(false);
        lcRadon.getDescription().setEnabled(false);
        lcRadon.setVisibleXRangeMaximum(24);

        //Y-Axis formatting
        yAxis = lcRadon.getAxisLeft();
        yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        yAxis.setLabelCount(4,true);
        yAxis.setAxisMinimum(yMin);

        //X-Axis formatting
        xAxis = lcRadon.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new GraphAxisFormatter());

        LineDataSet lineDataSet = new LineDataSet(chartdataRadon,"pCi/L");

        if(Globals.globalUnitType=="SI") {
            lineDataSet.setLabel("Bq/m³");
        }

        //lineDataSet.setColors(ColorTemplate.getHoloBlue());
        lineDataSet.setFillAlpha(110);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setCircleColor(R.color.colorPrimary);
        lineDataSet.setColor(R.color.colorPrimary);
        lineDataSet.setLineWidth(3f);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineData = new LineData(lineDataSet);
        lineDataSet.setFillColor(Color.RED);

        //Draw the actual graph with lineData
        //lcRadon.fitScreen();
        lcRadon.setData(lineData);

        return view;
    }

}
