package com.radelec.reconmobile;

import com.github.mikephil.charting.data.Entry;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

class Globals {

    //Recon Globals
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

    //Data Session Variables
    public static LinkedList<String[]> arrayDataSession = new LinkedList<>();
    public static Boolean boolRecordHeaderFound = false;
    public static Boolean boolRecordTrailerFound = false;
    public static int intDataSessionPointer= 0;

    //Options
    public static boolean boolDiagnosticMode = false;
    public static boolean boolExcludeFirst4Hours = true;
    public static boolean boolLongTermMode = false;
    public static String globalUnitType = "US";

    //File Directory
    public static File fileDir = null;

    //File Loading
    public static boolean boolClickToLoad = false;
    public static String globalLoadedFileName = "";
    public static ArrayList<ArrayList<String>> LoadedReconTXTFile = new ArrayList<>(); //This ArrayList will be used to build the chart, and is populated when loading the file.
    public static double LoadedReconCF1 = 6;
    public static double LoadedReconCF2 = 6;

    //Graph Arrays
    public static ArrayList<Entry> chartdataHumidity;
    public static ArrayList<Entry> chartdataPressure;
    public static ArrayList<Entry> chartdataRadon;
    public static ArrayList<Entry> chartdataTemp;
    public static ArrayList<Entry> chartdataTilts;

}
