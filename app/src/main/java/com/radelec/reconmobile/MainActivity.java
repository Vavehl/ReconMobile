package com.radelec.reconmobile;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;

import static com.radelec.reconmobile.Constants.*;
import static com.radelec.reconmobile.Globals.*;

public class MainActivity extends AppCompatActivity
        implements  FragmentManager.OnBackStackChangedListener,
                    FragmentSearch.OnFragmentInteractionListener {

    //Create various dialog windows. Hopefully there won't be too many of them, or this is gonna become a mess...
    public Dialog dialogAbout;

    LineChart lcRadon;
    LineChart lcHumidity;
    LineChart lcPressure;
    BarChart bcTilts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //At the very least, let's broadcast a few info log messages detailing the version info.
        Log.i("VersionInfo","Version Build = " + version_build);
        Log.i("VersionInfo", "Version Date = " + version_date);

        setContentView(R.layout.activity_main);
        TabLayout tabLayout = findViewById(R.id.tabLayout_Main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initialize database (in DatabaseOperations)
        db = new DatabaseOperations(this);

        //Get file directory
        fileDir = getApplicationContext().getFilesDir();
        imageDir = getApplicationContext().getDir("images",MODE_PRIVATE);

        //Programmatically initialize fragmentConnect tab -- there has to be a better way to implement this??
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.simpleConstraintContainer, new FragmentConnect(),"fragConnect");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
        //End initial implementation of fragmentConnect

        //Initialize Asset Manager
        assetManager = this.getAssets();

        Permissions.verifyStoragePermissions(this);

        FloatingActionButton fab = findViewById(R.id.fabEmail);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "TODO: Email PDF Report", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                if(connected == ReconConnected.Loaded) {
                    populateRadonChart();
                    populateHumidityChart();
                    populatePressureChart();
                    populateTiltsChart();
                    emailPDF();
                } else {
                    //If no file is loaded, let's try to prompt the user to load one...
                    Toast no_file_loaded = Toast.makeText(getApplicationContext(),"You must load a file before emailing it!",Toast.LENGTH_SHORT);
                    no_file_loaded.show();
                    openFile();
                }
            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d("MainActivity","onTabSelected(TabLayout.Tab " + tab.toString() + ") called!");
                Fragment fragment = null;
                switch (tab.getPosition()) {
                    case 0:
                        fragment = new FragmentConnect();
                        break;
                    case 1:
                        fragment = new FragmentGraphs();
                        break;
                    case 2:
                        fragment = new FragmentReport();
                        break;
                }
                if(fragment != null){
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.simpleConstraintContainer, fragment);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.commit();
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Log.d("MainActivity","onTabUnselected(TabLayout.Tab " + tab.toString() + ") called!");
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Fragment fragment = null;
                switch (tab.getPosition()) {
                    case 0:
                        fragment = new FragmentConnect();
                        break;
                    case 1:
                        fragment = new FragmentGraphs();
                        break;
                    case 2:
                        fragment = new FragmentReport();
                        break;
                }
                if(fragment != null) {
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.simpleConstraintContainer, fragment);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.commit();
                }
            }

        });
    }

    public void onBackStackChanged() {
        Log.d("MainActivity","onBackStackChanged() called!");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount()>0);
    }

    //Creates Recon Main Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("MainActivity","onCreateOptionsMenu() called!");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("MainActivity","onOptionsItemSelected(MenuItem " + item.toString() + ") called!");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch(item.getItemId()) {
            case R.id.menu_about:
                showAboutPopUp();
                return true;
            case R.id.open_file:
                openFile();
                return true;
            case R.id.save_file:
                saveFile();
                return true;
            case R.id.settings:
                showSettings();
                return true;
            case R.id.menu_company:
                showMyCompany();
                return true;
            case R.id.menu_exit:
                System.exit(0);
        }

        return super.onOptionsItemSelected(item);
    }

    /*
    Assign version and build dates to the About dialog / pop-up window
    ...and then display it!
    */
    protected void showAboutPopUp() {

        Log.d("MainActivity","showAboutPopUp() called!");

        //Initialize the "About" dialog window in the menu...
        dialogAbout = new Dialog(this);

        //Need to setContentView *before* setting the version and date textviews!
        dialogAbout.setContentView(R.layout.menu_about);

        //Initialize TextViews...
        final TextView tvAboutVersionBuild = dialogAbout.findViewById(R.id.aboutVersionBuild);
        final TextView tvAboutVersionDate = dialogAbout.findViewById(R.id.aboutVersionDate);

        //Initialize imgCloseAbout "button"
        final ImageView btnCloseAbout;
        btnCloseAbout = dialogAbout.findViewById(R.id.imgCloseAbout);

        //Initialize website link
        final TextView btnWebsite;
        btnWebsite = dialogAbout.findViewById(R.id.website_radelec);

        //Set the version and date textviews to their proper values.
        try {
            tvAboutVersionBuild.setText(version_build);
            tvAboutVersionDate.setText(version_date);
        } catch (NullPointerException ex) {
            Toast error = Toast.makeText(getApplicationContext(),"Unable to initialize version build and date!",Toast.LENGTH_SHORT);
            error.show();
        }

        //Allow user to close About window by clicking on the X in the upper right
        btnCloseAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogAbout.dismiss();
            }
        });

        //Allow user to open Rad Elec website by clicking on the link
        btnWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intentRadElecWebsite = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.radelec.com"));
                    startActivity(intentRadElecWebsite);
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(getApplicationContext(), "No web browser found.", Toast.LENGTH_LONG).show();
                }
            }
        });

        //Show what we have!
        dialogAbout.show();
    }

    //This will load/show the Settings fragment
    protected void showSettings() {
        Log.d("MainActivity","showSettings() called!");
        Fragment fragment;
        fragment = new FragmentSettings();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.simpleConstraintContainer, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }

    //This will load/show the Company fragment
    protected void showMyCompany() {
        Log.d("MainActivity","showMyCompany() called!");
        Fragment fragment;
        fragment = new FragmentCompany();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.simpleConstraintContainer, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }

    //We should offload this to its own class...
    protected void saveFile() {
        Log.d("MainActivity","saveFile() called!");
        Toast msgSave = Toast.makeText(getApplicationContext(),"Saving file...",Toast.LENGTH_SHORT);
        msgSave.show();
    }

    //We should offload this to its own class, too...
    protected void openFile() {
        Log.d("MainActivity","openFile() called!");
        Globals.boolClickToLoad = false;
        FragmentOpen fragmentOpen = new FragmentOpen();
        fragmentOpen.setRetainInstance(true);
        fragmentOpen.show(getSupportFragmentManager(),"");

        //Programmatically initialize fragmentConnect tab -- there has to be a better way to implement this??
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.simpleConstraintContainer, new FragmentConnect(),"fragConnect");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
        //End initial implementation of fragmentConnect

        if(fragmentOpen.isDetached()) {
            Log.d("MainActivity","FRAGMENT_OPEN DETACHED!");
        }
    }

    protected void emailPDF() {
        try {
            Log.d("MainActivity", "emailPDF() called!");

            //Generate PDF when the email button is pressed.

            CreatePDF generate_pdf = new CreatePDF();
            try {
                createImagesFromChart();
                generate_pdf.main();
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }

            String[] recipient_email = {"info@radelec.com"};
            String[] self_email = {"info@radelec.com"};
            String subject = "Radon Test Report";
            String body = "Please find attached your radon test results!";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri data = Uri.parse("mailto:");
            intent.putExtra(Intent.EXTRA_EMAIL, recipient_email);
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, body);
            intent.putExtra(Intent.EXTRA_BCC, self_email);
            if (filePDF != null) {
                if (filePDF.exists()) {
                    Log.d("MainActivity", "emailPDF(): Attempting to attach PDF to email!");
                    //filePDF.setReadable(true, false);
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(filePDF));
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } else {
                    Log.d("MainActivity", "emailPDF(): No PDF found for attachment!");
                }
            }
            intent.setData(data);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(Intent.createChooser(intent, "Creating email..."), 12);
            } else {
                Toast msgEmail = Toast.makeText(getApplicationContext(), "Unable to launch email client...", Toast.LENGTH_SHORT);
                msgEmail.show();
            }
        } catch (ActivityNotFoundException ex) {
            Log.d("MainActivity","emailPDF(): Email client not found?");
        }
    }

    public void populateRadonChart() {
        Log.d("MainActivity","populateRadonChart() called!");

        LineData lineDataRadon;
        XAxis xAxis;
        final YAxis yAxis;
        float yMin = 0;

        //Assign layout element to the linechart lcRadon
        lcRadon = findViewById(R.id.chartRadon);
        LineDataSet lineDataSet = new LineDataSet(chartdataRadon,"pCi/L");

        lineDataSet.setFillAlpha(110);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setLineWidth(5f);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setValueTextSize(10f);
        lineDataSet.setValueFormatter(new DefaultValueFormatter(1));
        lineDataRadon = new LineData(lineDataSet);

        //Draw the actual graph with lineData
        lcRadon.setData(lineDataRadon);

        // Gradient Stuff Begin (...which isn't even working!)
        //It's not working because the height is zero!
        Paint paint = lcRadon.getRenderer().getPaintRender();
        int height = lcRadon.getHeight();
        LinearGradient linGrad = new LinearGradient(0, 0, 0, height, getResources().getColor(android.R.color.holo_blue_bright), getResources().getColor(android.R.color.holo_blue_dark), Shader.TileMode.REPEAT);
        paint.setShader(linGrad);
        // Gradient Stuff End

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

        Log.d("MainActivity","Chart Height = " + lcRadon.getHeight() + " // Chart Width = " + lcRadon.getWidth());
    }

    public void populateHumidityChart() {
        Log.d("MainActivity","populateHumidityChart() called!");

        LineData lineDataHumidity;
        XAxis xAxis;
        final YAxis yAxis;
        float yMin = 0;

        //Assign layout element to the linechart lcHumidity
        lcHumidity = findViewById(R.id.chartHumidity);

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
        lineDataHumidity = new LineData(lineDataSet);

        //Draw the actual graph with lineData
        lcHumidity.setData(lineDataHumidity);

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

    }

    public void populatePressureChart() {
        Log.d("MainActivity","populatePressureChart() called!");

        LineData lineDataPressure;
        XAxis xAxis;
        final YAxis yAxis;

        //Assign layout element to the linechart lcPressure
        lcPressure = findViewById(R.id.chartPressure);

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
        lineDataPressure = new LineData(lineDataSet);

        //Draw the actual graph with lineData
        lcPressure.setData(lineDataPressure);

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
    }

    public void populateTiltsChart() {
        Log.d("MainActivity","populateTiltsChart() called!");

        BarData barDataTilts;
        XAxis xAxis;
        final YAxis yAxis;

        //Assign layout element to the barchart bcTilts
        bcTilts = findViewById(R.id.chartTilts);

        BarDataSet barDataSet = new BarDataSet(chartdataTilts,"Tilts");

        barDataSet.setColor(R.color.colorPrimary);
        barDataSet.setDrawValues(true);
        barDataSet.setValueFormatter(new DefaultValueFormatter(0));
        barDataSet.setGradientColor(Color.argb(100,171,157,242),Color.argb(200,52,139,195));
        barDataTilts = new BarData(barDataSet);
        barDataTilts.setBarWidth(20f);
        barDataTilts.setValueTextSize(10f);

        //Draw the actual graph with barData
        bcTilts.setData(barDataTilts);

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
    }


    public void createImagesFromChart() {
        Log.d("MainActivity","createImagesFromChart() called!");
        if(lcRadon.getHeight() > 0 && lcRadon.getWidth() > 0) {
            Log.d("MainActivity","Attempting to create PNG from Radon Chart!");
            Bitmap bmpRadon;
            Bitmap bmpHumidity;
            Bitmap bmpPressure;
            Bitmap bmpTilts;

            bmpRadon = lcRadon.getChartBitmap();
            File output = new File(imageDir,"chartRadon.png");
            try {
                OutputStream outputStream = new FileOutputStream(output);
                bmpRadon.compress(Bitmap.CompressFormat.PNG,100,outputStream);
                outputStream.flush();
                outputStream.close();
            } catch (Exception ex){
                Log.d("MainActivity","createImagesFromChart():: Exception while creating Radon PNG!");
                ex.printStackTrace();
            }

            bmpHumidity = lcHumidity.getChartBitmap();
            output = new File(imageDir,"chartHumidity.png");
            try {
                OutputStream outputStream = new FileOutputStream(output);
                bmpHumidity.compress(Bitmap.CompressFormat.PNG,100,outputStream);
                outputStream.flush();
                outputStream.close();
            } catch (Exception ex){
                Log.d("MainActivity","createImagesFromChart():: Exception while creating Humidity PNG!");
                ex.printStackTrace();
            }

            bmpPressure = lcPressure.getChartBitmap();
            output = new File(imageDir,"chartPressure.png");
            try {
                OutputStream outputStream = new FileOutputStream(output);
                bmpPressure.compress(Bitmap.CompressFormat.PNG,100,outputStream);
                outputStream.flush();
                outputStream.close();
            } catch (Exception ex){
                Log.d("MainActivity","createImagesFromChart():: Exception while creating Pressure PNG!");
                ex.printStackTrace();
            }

            bmpTilts = bcTilts.getChartBitmap();
            output = new File(imageDir,"chartTilts.png");
            try {
                OutputStream outputStream = new FileOutputStream(output);
                bmpTilts.compress(Bitmap.CompressFormat.PNG,100,outputStream);
                outputStream.flush();
                outputStream.close();
            } catch (Exception ex){
                Log.d("MainActivity","createImagesFromChart():: Exception while creating Tilts PNG!");
                ex.printStackTrace();
            }

        } else {
            Log.d("MainActivity","createImagesFromChart():: Height and/or Width is zero!");
        }
    }

}
