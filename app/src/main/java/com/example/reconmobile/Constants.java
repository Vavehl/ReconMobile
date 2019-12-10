package com.example.reconmobile;

class Constants {

    //Recon commands
    public static String cmdReconConfirm = ":RV";
    public static String cmdReadProtocol = ":RP";
    public static String cmdCheckNewRecord = ":RB";
    public static String cmdReadNextRecord = ":RN";
    public static String cmdReadNextDiagnosticRecord = ":DN";
    public static String cmdReadFirstDiagnosticRecord = "DN0";
    public static String cmdClearMemory = ":CM";
    public static String cmdClearSession = ":CD";
    public static String cmdReadCalibrationFactors = ":RL";
    public static String cmdReadTime = ":RT";
    public static String cmdResetTamperFlag = ":WX";
    public static String cmdWriteOptionsFlag = "WF";
    public static String cmdReadOptionsFlag = ":RF";

    // values have to be globally unique
    static final String INTENT_ACTION_DISCONNECT = BuildConfig.APPLICATION_ID + ".Disconnect";
    static final String NOTIFICATION_CHANNEL = BuildConfig.APPLICATION_ID + ".Channel";
    static final String INTENT_CLASS_MAIN_ACTIVITY = BuildConfig.APPLICATION_ID + ".MainActivity";

    // values have to be unique within each app
    static final int NOTIFY_MANAGER_START_FOREGROUND_SERVICE = 1001;

    //...if we need any private constants, we can put them here.
    private Constants() {}

    public enum ReconConnected { False, Pending, True }
}
