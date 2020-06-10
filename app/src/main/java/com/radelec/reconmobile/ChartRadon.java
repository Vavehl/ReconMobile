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

        lcRadon = view.findViewById(R.id.chartRadon);
        lcRadon.setTouchEnabled(true);
        lcRadon.setPinchZoom(true);

        LineDataSet lineDataSet = new LineDataSet(chartdataRadon,"Radon");
        lineDataSet.setColors(ColorTemplate.getHoloBlue());
        lineDataSet.setFillAlpha(110);
        lineData = new LineData(lineDataSet);
        lcRadon.setData(lineData);
        //lcRadon.setVisibleYRangeMinimum(Float.valueOf(0));
        //lcRadon.setVisibleXRangeMinimum(0);
        lcRadon.setVisibleXRangeMaximum(24);
        lcRadon.moveViewToX(20);
        //lcRadon.invalidate();
        //lineDataSet = new LineDataSet(Globals.LoadedReconTXTFile, "Radon");
        lcRadon.setMinimumHeight(400); //Minimum height, just in case.
        return view;
    }

}
