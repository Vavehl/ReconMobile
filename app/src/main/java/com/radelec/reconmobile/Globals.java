package com.radelec.reconmobile;

import android.content.res.AssetManager;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

class Globals {

    //Connection Globals
    public enum ReconConnected { False, Pending, True, Loaded }
    public static ReconConnected connected = ReconConnected.False;
    public static int deviceId;
    public static int portNum;
    public static SerialSocket socket;
    public static SerialService service;
    public static boolean initialStart = true;

    //Connected Recon Variables
    public static String globalReconSerial = "";
    public static double globalReconFirmwareRevision = 0;
    public static String globalReconCalibrationDate = null;
    public static double globalReconCF1 = 6;
    public static double globalReconCF2 = 6;
    public static String globalDataSessions = "";

    public static String globalLastWrite = "";
    public static String globalLastResponse = "";
    public static String globalLastSystemConsole = "";

    //Database
    public static DatabaseOperations db;

    //Data Session Variables
    public static LinkedList<String[]> arrayDataSession = new LinkedList<>();
    public static Boolean boolRecordHeaderFound = false;
    public static Boolean boolRecordTrailerFound = false;
    public static int intDataSessionPointer= 0;

    //Options
    public static boolean boolDiagnosticMode = false;
    public static boolean boolExcludeFirst4Hours = true;
    public static boolean boolLongTermMode = false;
    public static boolean boolAutoLoadFile = true;
    public static String globalUnitType = "US";

    //File Directory
    public static File fileDir = null;
    public static File imageDir = null;

    //PDF File
    public static File filePDF = null;

    //File Loading
    public static boolean boolClickToLoad = false;
    public static String globalLoadedFileName = "";
    public static ArrayList<ArrayList<String>> HourlyReconData = new ArrayList<>();
    public static ArrayList<ArrayList<String>> LoadedReconTXTFile = new ArrayList<>(); //This ArrayList will be used to build the chart, and is populated when loading the file.
    public static double LoadedReconCF1 = 6;
    public static double LoadedReconCF2 = 6;
    public static String loadedTestSiteInfo = "";
    public static String loadedCustomerInfo = "";
    public static String loadedReportProtocol = "";
    public static String loadedReportTampering = "";
    public static String loadedReportWeather = "";
    public static String loadedReportMitigation = "";
    public static String loadedReportComment = "";
    public static String loadedLocationDeployed = "";
    public static String loadedDeployedBy = "";
    public static String loadedRetrievedBy = "";
    public static String loadedAnalyzedBy = "";
    public static String loadedCalibrationDate = "";

    //Database
    public static DatabaseOperations globalDBDefaults;

    //Graph Arrays
    public static ArrayList<Entry> chartdataHumidity;
    public static ArrayList<Entry> chartdataPressure;
    public static ArrayList<Entry> chartdataRadon;
    public static ArrayList<Entry> chartdataTemp;
    public static ArrayList<BarEntry> chartdataTilts;

    //Use new filenaming method?
    public static boolean boolUseTestSiteFileName = true;
    public static boolean boolInvalidFileName = true;

    public static AssetManager assetManager;
}
