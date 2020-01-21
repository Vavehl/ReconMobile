package com.radelec.reconmobile;

import java.util.ArrayList;

class Globals {

    //Recon Globals
    public enum ReconConnected { False, Pending, True }
    public static ReconConnected connected = ReconConnected.False;
    public static int deviceId;
    public static int portNum;
    public static SerialSocket socket;
    public static SerialService service;

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
    public static ArrayList<String> arrayDataSession = new ArrayList<>();
    public static Boolean boolRecordHeaderFound = false;
    public static Boolean boolRecordTrailerFound = false;
    public static int intDataSessionPointer= 0;
}
