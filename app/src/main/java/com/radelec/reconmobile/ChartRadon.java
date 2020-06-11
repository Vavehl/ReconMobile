package com.radelec.reconmobile;

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
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

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
        GraphAxisFormatter gafXAxis = new GraphAxisFormatter();

        lcRadon = view.findViewById(R.id.chartRadon);
        lcRadon.setTouchEnabled(true);
        lcRadon.setPinchZoom(true);

        LineDataSet lineDataSet = new LineDataSet(chartdataRadon,"");
        lineDataSet.setColors(ColorTemplate.getHoloBlue());
        lineDataSet.setFillAlpha(110);
        lineData = new LineData(lineDataSet);
        lcRadon.setData(lineData);
        xAxis = lcRadon.getXAxis();
        xAxis.setValueFormatter(new GraphAxisFormatter());
        if(lcRadon.getHeight()<400) {
            Log.d("ChartRadon","chartRadon has a current height of " + lcRadon.getHeight() + " -- this is too small! Setting minimum height to 400...");
            lcRadon.setMinimumHeight(400); //Minimum height, just in case.
        }
        return view;
    }

}
