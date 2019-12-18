package com.example.reconmobile;

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

    public static String globalLastWrite = "";
    public static String globalLastResponse = "";

}
