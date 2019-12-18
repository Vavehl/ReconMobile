package com.example.reconmobile;

class Constants {

    //Recon commands
    public static final String cmdReconConfirm = ":RV";
    public static final String cmdReadProtocol = ":RP";
    public static final String cmdCheckNewRecord = ":RB";
    public static final String cmdReadNextRecord = ":RN";
    public static final String cmdReadNextDiagnosticRecord = ":DN";
    public static final String cmdReadFirstDiagnosticRecord = "DN0";
    public static final String cmdClearMemory = ":CM";
    public static final String cmdClearSession = ":CD";
    public static final String cmdReadCalibrationFactors = ":RL";
    public static final String cmdReadTime = ":RT";
    public static final String cmdResetTamperFlag = ":WX";
    public static final String cmdWriteOptionsFlag = "WF";
    public static final String cmdReadOptionsFlag = ":RF";

    //New Line
    public static final String newline = "\r\n"; //Should we change this?

    // values have to be globally unique
    static final String INTENT_ACTION_DISCONNECT = BuildConfig.APPLICATION_ID + ".Disconnect";
    static final String NOTIFICATION_CHANNEL = BuildConfig.APPLICATION_ID + ".Channel";
    static final String INTENT_CLASS_MAIN_ACTIVITY = BuildConfig.APPLICATION_ID + ".MainActivity";

    //This should be here, too.
    static final String INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";

    // values have to be unique within each app
    static final int NOTIFY_MANAGER_START_FOREGROUND_SERVICE = 1001;
}
