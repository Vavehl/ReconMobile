package com.radelec.reconmobile;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Logging {

    private static final Logger logger = Logger.getLogger("ReconMobile");
    private static FileHandler logHandler;

    public static void main(String strTag, String strLog) {
        if(Globals.boolInitializedLogging == false) {
            prepareLogging();
            Globals.boolInitializedLogging = true;
        }
        log(strTag, strLog);
    }

    public static void log(String strTag, String strLog) {
        logger.setUseParentHandlers(false);
        logger.info(strTag + ": " + strLog);
        Log.d(strTag, strLog);
    }

    public static void createLogFile() {
        File logFile = new File(Globals.logsDir + File.separator + "ReconMobile.log");
        try {
            PrintWriter pw = null;
            if (!(logFile.exists())) {
                pw = new PrintWriter(logFile);
                pw.close();
            }
        } catch (FileNotFoundException ex) {
            System.out.println("ERROR: Unable to create logging file!"); //No need to write to a log if we can't even create the damn thing...
            System.out.println(ex);
        }
    }

    public static void prepareLogging() {
        try {
            System.out.println("Initiating logging system...");
            createLogFile();
            logHandler = new FileHandler(Globals.logsDir + File.separator + "ReconMobile.log");
            logger.addHandler(logHandler);
            SimpleFormatter formatter = new SimpleFormatter();
            logHandler.setFormatter(formatter);
        } catch (IOException | SecurityException ex) {
            System.out.println("ERROR: Unhandled exception in Logging::prepareLogging()...");
            System.out.println(ex);
        }
    }
}
