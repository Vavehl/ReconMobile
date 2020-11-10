package com.radelec.reconmobile;

class Constants {

    //Version information
    public static String version_build = "v0.7.2";
    public static String version_date = "10 Nov 2020";

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

    //Recon New Line
    public static final String newline = "\r\n";
    //System New Line (Android-specific)
    public static final String newlinePDF = "\\n";

    // values have to be globally unique
    static final String INTENT_ACTION_DISCONNECT = BuildConfig.APPLICATION_ID + ".Disconnect";
    static final String NOTIFICATION_CHANNEL = BuildConfig.APPLICATION_ID + ".Channel";
    static final String INTENT_CLASS_MAIN_ACTIVITY = BuildConfig.APPLICATION_ID + ".MainActivity";

    //This should be here, too.
    static final String INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";

    // values have to be unique within each app
    static final int NOTIFY_MANAGER_START_FOREGROUND_SERVICE = 1001;

    //Baud Rate
    static final int baudRate = 9600;

    //Consecutive Zero Limit (for determining photodiode failure)
    public static double ConsecutiveZeroLimit = 5; //If this number of consecutive zeros is met (or exceeded) by a chamber when creating a TXT or loading a file, we will alert the user to a potential photodiode failure.
    public static boolean boolPhotodiodeFailureRecovery=true;

}
