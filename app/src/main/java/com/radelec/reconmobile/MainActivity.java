package com.radelec.reconmobile;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
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
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.util.Objects;

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

        //Get various directories used by Recon Mobile
        cacheDir = getApplicationContext().getCacheDir();
        fileDir = getApplicationContext().getFilesDir();
        imageDir = getApplicationContext().getDir("images",MODE_PRIVATE);
        logsDir = getApplicationContext().getDir("logs",MODE_PRIVATE);

        //Logging.prepareLogging();

        setContentView(R.layout.activity_main);
        TabLayout tabLayout = findViewById(R.id.tabLayout_Main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initialize database (in DatabaseOperations)
        db = DatabaseOperations.getInstance(this);

        //At the very least, let's broadcast a few info log messages detailing the version info.
        Logging.main("VersionInfo","Version Build = " + version_build);
        Logging.main("VersionInfo", "Version Date = " + version_date);

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

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Logging.main("MainActivity","onTabSelected(TabLayout.Tab " + tab.toString() + ") called!");
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
                Logging.main("MainActivity","onTabUnselected(TabLayout.Tab " + tab.toString() + ") called!");
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

    public void onDestroy() {
        Logging.main("MainActivity","onDestroy() called!");
        super.onDestroy();
        createLogcat();
    }

    public void onBackStackChanged() {
        Logging.main("MainActivity","onBackStackChanged() called!");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount()>0);
    }

    //Creates Recon Main Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Logging.main("MainActivity","onCreateOptionsMenu() called!");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Logging.main("MainActivity","onOptionsItemSelected(MenuItem " + item.toString() + ") called!");
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
            case R.id.email_pdf:
                if(connected == ReconConnected.Loaded) {
                    saveFile();
                    populateRadonChart();
                    populateHumidityChart();
                    populatePressureChart();
                    populateTiltsChart();
                    emailPDF();
                    try {
                        copyPDFToPublicDir();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    //If no file is loaded, let's try to prompt the user to load one...
                    Toast no_file_loaded = Toast.makeText(getApplicationContext(),"You must load a file before emailing it!",Toast.LENGTH_SHORT);
                    no_file_loaded.show();
                    openFile();
                }
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
            case R.id.report_bugs:
                emailLog();
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

        Logging.main("MainActivity","showAboutPopUp() called!");

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
        btnCloseAbout.setOnClickListener(view -> dialogAbout.dismiss());

        //Allow user to open Rad Elec website by clicking on the link
        btnWebsite.setOnClickListener(view -> {
            try {
                Intent intentRadElecWebsite = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.radelec.com"));
                startActivity(intentRadElecWebsite);
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(getApplicationContext(), "No web browser found.", Toast.LENGTH_LONG).show();
            }
        });

        //Show what we have!
        dialogAbout.show();
    }

    //This will load/show the Settings fragment
    protected void showSettings() {
        Logging.main("MainActivity","showSettings() called!");
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
        Logging.main("MainActivity","showMyCompany() called!");
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
        Logging.main("MainActivity","saveFile() called!");
        if(globalLoadedFileName.length()>0) {
            Logging.main("MainActivity","Currently Loaded File = " + globalLoadedFileName);
            Toast msgSave;
            File loadedFile = new File(fileDir + File.separator + globalLoadedFileName);
            try {
                msgSave = Toast.makeText(getApplicationContext(), SaveFile.main(loadedFile), Toast.LENGTH_SHORT);
                msgSave.show();
            } catch (IOException e) {
                msgSave = Toast.makeText(getApplicationContext(), "Error when saving file!", Toast.LENGTH_SHORT);
                msgSave.show();
                e.printStackTrace();
            }
        } else {
            Toast msgSave = Toast.makeText(getApplicationContext(), "No file currently loaded...", Toast.LENGTH_SHORT);
            msgSave.show();
        }
    }

    //We should offload this to its own class, too...
    protected void openFile() {
        Logging.main("MainActivity","openFile() called!");
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
            Logging.main("MainActivity","FRAGMENT_OPEN DETACHED!");
        }
    }

    protected void copyPDFToPublicDir() throws IOException {
        Log.d("MainActivity", "copyPDFToPublicDir() called!");

        File src = filePDF;
        String strPDF_Name = "RadonTestReport.pdf";

        //strips .txt from the filename and replaces it with .pdf
        if (globalLoadedFileName != null && globalLoadedFileName.length()>5) {
            strPDF_Name = StringUtils.left(globalLoadedFileName, globalLoadedFileName.length() - 4) + ".pdf";
        }

        String strPublicPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + strPDF_Name;
        File dst = new File(strPublicPath);

        if(src==null) {
            Log.d("MainActivity", "Source PDF file is null! Unable to copy to public directory...");
            return;
        }

        if (!(src.exists())) {
            Log.d("MainActivity", "No source PDF file found!? Unable to copy to public directory...");
            return;
        }

        try {
            if (!(dst.exists())) {
                PrintWriter pw;
                pw = new PrintWriter(dst);
                pw.close();
            } else {
                Log.d("MainActivity", "Public-accessible PDF with the desired name already exists. Attempting to overwrite.");
            }
        } catch (FileNotFoundException ex) {
            Log.d("MainActivity", "ERROR: Unable to create public PDF!");
            Log.d("MainActivity", ex.toString());
            return;
        }

        Log.d("MainActivity", "SRC = " + src.getAbsolutePath() + " (" + src.length() / 1024 + " kb) // DST = " + dst.getAbsolutePath());

        File expFile = new File(strPublicPath);

        FileChannel inChannel;
        FileChannel outChannel;

        try {
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(expFile).getChannel();
        } catch (FileNotFoundException ex) {
            Log.d("MainActivity", "ERROR: Unable to establish inChannel / outChannel!");
            Log.d("MainActivity", ex.toString());
            return;
        }

        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (FileNotFoundException ex) {
            Log.d("Logging", "ERROR: Unable to transfer contents from internal ReconMobile.log to public log!");
            Log.d("Logging", ex.toString());
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }

    protected void emailPDF() {
        try {
            Logging.main("MainActivity", "emailPDF() called!");

            //Generate PDF when the email button is pressed.

            CreatePDF generate_pdf = new CreatePDF();
            try {
                createImagesFromChart();
                generate_pdf.main();
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }

            String[] recipient_email = {loadedEmail};
            String[] self_email = {db.getCompanyEmail()};
            String subject = "Radon Test Report";
            String body = "Please find attached your radon test results!";
            if (filePDF != null) {
                if (android.os.Build.VERSION.SDK_INT < 29) { //Old method for attaching files (pre-Android <10 / SDK API LEVEL <29
                    if (filePDF.exists()) {
                        Logging.main("MainActivity", "emailPDF(): Attempting to attach PDF to email (SDK <29)!");
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            Uri data = Uri.parse("mailto:");
                            intent.putExtra(Intent.EXTRA_EMAIL, recipient_email);
                            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                            intent.putExtra(Intent.EXTRA_TEXT, body);
                            intent.putExtra(Intent.EXTRA_BCC, self_email);
                            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(filePDF));
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intent.setData(data);
                            try {
                                startActivityForResult(Intent.createChooser(intent, "Creating email..."), 12);
                            } catch (ActivityNotFoundException ex) {
                                Toast msgEmail = Toast.makeText(getApplicationContext(), "Unable to launch email client...", Toast.LENGTH_SHORT);
                                msgEmail.show();
                            }
                        } catch (Exception ex) {
                            Logging.main("MainActivity", "emailPDF(): Unable to attach PDF to email!");
                            Logging.main("MainActivity", "emailPDF(): " + ex.toString());
                        }
                    } else {
                        Logging.main("MainActivity", "emailPDF(): No PDF found for attachment!");
                    }
                } else { //New method for attaching files to email client in Android 10+ / SDK 29+
                    if (filePDF.exists()) {
                        Logging.main("MainActivity", "emailPDF(): Attempting to attach PDF to email (SDK 29+)!");
                        try {
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_EMAIL, recipient_email);
                            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                            intent.putExtra(Intent.EXTRA_TEXT, body);
                            intent.putExtra(Intent.EXTRA_BCC, self_email);

                            if (!filePDF.exists() || !filePDF.canRead()) {
                                Logging.main("MainActivity","emailLog(): PDF Attachment Error!");
                                Toast.makeText(this, "Attachment Error", Toast.LENGTH_SHORT).show();
                                finish();
                                return;
                            }

                            Uri uri = FileProvider.getUriForFile(MainActivity.this, "com.radelec.reconmobile.fileprovider", filePDF);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intent.setDataAndType(uri, getContentResolver().getType(uri));
                            intent.putExtra(Intent.EXTRA_STREAM, uri);
                            try {
                                startActivityForResult(Intent.createChooser(intent, "Creating email..."), 12);
                            } catch (ActivityNotFoundException ex) {
                                Toast msgEmail = Toast.makeText(getApplicationContext(), "Unable to launch email client...", Toast.LENGTH_SHORT);
                                msgEmail.show();
                            }
                        } catch (Exception ex) {
                            Logging.main("MainActivity", "emailPDF(): Unable to attach PDF to email!");
                            Logging.main("MainActivity", "emailPDF(): " + ex.toString());
                        }
                    }
                }
            }
        } catch (ActivityNotFoundException ex) {
            Logging.main("MainActivity","emailPDF(): Email client not found?");
            Toast msgEmail = Toast.makeText(getApplicationContext(), "No default email client found...", Toast.LENGTH_SHORT);
            msgEmail.show();
        }
    }

    public void emailLog(){
        Logging.main("MainActivity","emailLog() called!");
        File fileLastLog = new File(cacheDir + File.separator + "ReconMobile_last.log");
        String[] recipient_email = {"lcstieff@radelec.com"};
        String subject = "Recon Mobile Bug Report";
        String body = "Here is the latest bug report / log file!";
        if (fileLastLog.exists()) {
            if (android.os.Build.VERSION.SDK_INT < 29) { //Old method for attaching files (pre-Android <10 / SDK API LEVEL <29
                Logging.main("MainActivity", "emailLog(): Attempting to attach ReconMobile_last.log (SDK <29)!");
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri data = Uri.parse("mailto:");
                intent.putExtra(Intent.EXTRA_EMAIL, recipient_email);
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                intent.putExtra(Intent.EXTRA_TEXT, body);
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileLastLog));
                //intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileCurrentLog));
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setData(data);
                try {
                    startActivityForResult(Intent.createChooser(intent, "Creating email..."), 12);
                } catch (ActivityNotFoundException ex) {
                    Toast msgEmail = Toast.makeText(getApplicationContext(), "Unable to launch email client...", Toast.LENGTH_SHORT);
                    msgEmail.show();
                }
            } else { //Current method for attaching files (Android 10+ / SDK API LEVEL 29+)
                Logging.main("MainActivity", "emailLog(): Attempting to attach ReconMobile_last.log (SDK 29+)!");
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_EMAIL, recipient_email);
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                intent.putExtra(Intent.EXTRA_TEXT, body);

                if (!fileLastLog.exists() || !fileLastLog.canRead()) {
                    Logging.main("MainActivity","emailLog(): Attachment Error!");
                    Toast.makeText(this, "Log Attachment Error", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                Uri uri = FileProvider.getUriForFile(MainActivity.this, "com.radelec.reconmobile.fileprovider", fileLastLog);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(uri, getContentResolver().getType(uri));
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                try {
                    startActivityForResult(Intent.createChooser(intent, "Creating email..."), 12);
                    finish();
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this,"Unable to launch email client...", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(MainActivity.this,"No suitable log files available...", Toast.LENGTH_SHORT).show();
            Logging.main("MainActivity", "emailLog(): No accessible log file found for attachment!");
        }
    }

    public void createLogcat(){
        Logging.main("MainActivity","createLog() called!");
        File outputFile = new File(logsDir + File.separator, "logcat.txt");
        try {
            Runtime.getRuntime().exec(
                    "logcat -f " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            Logging.main("MainActivity","Exception when creating logcat.txt!");
            e.printStackTrace();
        }
    }

    public void populateRadonChart() {
        Logging.main("MainActivity","populateRadonChart() called!");

        LineData lineDataRadon;
        XAxis xAxis;
        final YAxis yAxis;
        float yMin = 0;

        //Assign layout element to the linechart lcRadon
        lcRadon = findViewById(R.id.chartRadonPDF);

        //Declare LineDataSet for the Radon Chart
        LineDataSet lineDataSet = new LineDataSet(chartdataRadon,"pCi/L");

        //X-Axis formatting (...don't understand why, but x-axis should be defined very early when setting up charts with labels)
        xAxis = lcRadon.getXAxis();
        xAxis.setLabelRotationAngle(90);
        xAxis.setCenterAxisLabels(true);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(12);
        xAxis.setValueFormatter(new GraphAxisFormatter());
        xAxis.setAvoidFirstLastClipping(true);

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

        //Modifiers if SI units are selected.
        if(Objects.equals(globalUnitType, "SI")) {
            lineDataSet.setLabel("Bq/m³");
            yAxis.setValueFormatter(new DefaultValueFormatter(0));
            lcRadon.invalidate(); //Is this needed?
        }

        Logging.main("MainActivity","Chart Height = " + lcRadon.getHeight() + " // Chart Width = " + lcRadon.getWidth());
    }

    public void populateHumidityChart() {
        Logging.main("MainActivity","populateHumidityChart() called!");

        LineData lineDataHumidity;
        XAxis xAxis;
        final YAxis yAxis;
        float yMin = 0;

        //Assign layout element to the linechart lcHumidity
        lcHumidity = findViewById(R.id.chartHumidityPDF);

        //Declare LineDataSet for the Radon Chart
        LineDataSet lineDataSet = new LineDataSet(chartdataHumidity,"%");

        //X-Axis formatting (...don't understand why, but x-axis should be defined very early when setting up charts with labels)
        xAxis = lcHumidity.getXAxis();
        xAxis.setDrawLabels(false);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(12);
        xAxis.setAvoidFirstLastClipping(true);

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

    }

    public void populatePressureChart() {
        Logging.main("MainActivity","populatePressureChart() called!");

        LineData lineDataPressure;
        XAxis xAxis;
        final YAxis yAxis;

        //Assign layout element to the linechart lcPressure
        lcPressure = findViewById(R.id.chartPressurePDF);

        //Declare LineDataSet for the Pressure Chart
        LineDataSet lineDataSet = new LineDataSet(chartdataPressure,"inHg");

        //X-Axis formatting (...don't understand why, but x-axis should be defined very early when setting up charts with labels)
        xAxis = lcPressure.getXAxis();
        xAxis.setDrawLabels(false);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(12);
        xAxis.setAvoidFirstLastClipping(true);

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

        //Modifiers if SI units are selected.
        if(Objects.equals(globalUnitType, "SI")) {
            lineDataSet.setLabel("mbar");
            lcPressure.invalidate(); //Is this needed?
        }
    }

    public void populateTiltsChart() {
        Logging.main("MainActivity","populateTiltsChart() called!");

        BarData barDataTilts;
        XAxis xAxis;
        final YAxis yAxis;

        //Assign layout element to the barchart bcTilts
        bcTilts = findViewById(R.id.chartTiltsPDF);

        //Declare barDataSet for the Tilts chart.
        BarDataSet barDataSet = new BarDataSet(chartdataTilts,"Tilts");

        //X-Axis formatting (...don't understand why, but x-axis should be defined very early when setting up charts with labels)
        xAxis = bcTilts.getXAxis();
        xAxis.setDrawLabels(false);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(12);
        xAxis.setAvoidFirstLastClipping(true);

        barDataSet.setColor(R.color.colorPrimary);
        barDataSet.setDrawValues(true);
        barDataSet.setValueFormatter(new DefaultValueFormatter(0));
        barDataSet.setGradientColor(Color.argb(100,171,157,242),Color.argb(200,52,139,195));
        barDataTilts = new BarData(barDataSet);
        barDataTilts.setBarWidth(2000f);
        barDataTilts.setValueTextSize(10f);

        //Draw the actual graph with barData
        bcTilts.setData(barDataTilts);

        //General graph settings (applied after setData)
        bcTilts.fitScreen();
        bcTilts.setDrawBorders(false);
        bcTilts.setDrawGridBackground(false);
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

    }
    
    public void createImagesFromChart() {
        Logging.main("MainActivity","createImagesFromChart() called!");
        if(lcRadon.getHeight() > 0 && lcRadon.getWidth() > 0) {
            Logging.main("MainActivity","Attempting to create PNG from Radon Chart!");
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
                Logging.main("MainActivity","createImagesFromChart():: Exception while creating Radon PNG!");
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
                Logging.main("MainActivity","createImagesFromChart():: Exception while creating Humidity PNG!");
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
                Logging.main("MainActivity","createImagesFromChart():: Exception while creating Pressure PNG!");
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
                Logging.main("MainActivity","createImagesFromChart():: Exception while creating Tilts PNG!");
                ex.printStackTrace();
            }

        } else {
            Logging.main("MainActivity","createImagesFromChart():: Height and/or Width is zero!");
        }
    }

}
