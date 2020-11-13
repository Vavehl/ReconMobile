package com.radelec.reconmobile;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Logging {

    private static final Logger logger = Logger.getLogger("ReconMobile");
    private static FileHandler logHandler;

    public static void main(String strTag, String strLog) {
        if(!Globals.boolInitializedLogging) {
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

    public static void exportLogFile(String strSuffix) throws IOException {

        Log.d("Logging","exportLogFile() called!");
        String strPublicLogPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "ReconMobile" + (strSuffix.length() > 0 ? strSuffix + ".log" : ".log");

        File src = new File(Globals.logsDir + File.separator + "ReconMobile.log");
        File dst = new File(strPublicLogPath);

        Log.d("Logging","SRC = " + src.getAbsolutePath() + " (" + src.length()/1024 + " kb) // DST = " + dst.getAbsolutePath());

        if (!(dst.exists())) {
            PrintWriter pw = null;
            pw = new PrintWriter(dst);
            pw.close();
        } else {
            Log.d("Logging","Public-accessible ReconMobile" + (strSuffix.length() > 0 ? strSuffix : "") + ".log already exists!");
        }

        File expFile = new File(strPublicLogPath);

        FileChannel inChannel = null;
        FileChannel outChannel = null;

        try {
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(expFile).getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }

    public static void prepareLogging() {
        try {
            System.out.println("Initializing logging system...");
            //copyLogFile("_last");
            exportLogFile("_last");
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
