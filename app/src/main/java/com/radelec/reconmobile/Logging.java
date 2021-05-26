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

    public static void main(String strTag, String strLog) {
        try {
            if (!Globals.boolInitializedLogging) {
                prepareLogging();
                Globals.boolInitializedLogging = true;
            }
            log(strTag, strLog);
        } catch (Exception ex) {
            Log.d("Logging","ERROR: Unhandled exception in Logging.main()!"); //No need to write to a log if we can't even create the damn thing...
            Log.d("Logging",ex.toString());
        }
    }

    public static void log(String strTag, String strLog) {
        try {
            logger.setUseParentHandlers(false);
            logger.info(strTag + ": " + strLog);
            Log.d(strTag, strLog);
        } catch (Exception ex) {
            Log.d("Logging","ERROR: Unable to log!"); //No need to write to a log if we can't even create the damn thing...
            Log.d("Logging",ex.toString());
        }
    }

    public static void createLogFile() {
        Log.d("Logging","Logging.createLogFile() called!");
        File logFile = new File(Globals.logsDir + File.separator + "ReconMobile.log");
        try {
            PrintWriter pw;
            if (!(logFile.exists())) {
                pw = new PrintWriter(logFile);
                pw.close();
            }
        } catch (FileNotFoundException ex) {
            Log.d("Logging","ERROR: Unable to create logging file!"); //No need to write to a log if we can't even create the damn thing...
            Log.d("Logging",ex.toString());
        }
    }

    public static void exportLogFile(String strSuffix) throws IOException {

        Log.d("Logging","Logging.exportLogFile() called!");
        String strPublicLogPath = Globals.cacheDir + File.separator + "ReconMobile" + (strSuffix.length() > 0 ? strSuffix + ".log" : ".log");

        File src = new File(Globals.logsDir + File.separator + "ReconMobile.log");
        File dst = new File(strPublicLogPath);
        try {
            if (!(src.exists())) {
                createLogFile();
                Log.d("Logging", "Internal ReconMobile.log not found! Creating now...");
            } else {
                Log.d("Logging", "Internal ReconMobile.log found!");
            }
        } catch (Exception ex) {
            Log.d("Logging","ERROR: Unhandled error when locating internal ReconMobile.log!");
            Log.d("Logging",ex.toString());
            return;
        }

        try {
            if (!(dst.exists())) {
                PrintWriter pw;
                pw = new PrintWriter(dst);
                pw.close();
            } else {
                Log.d("Logging", "Public-accessible ReconMobile" + (strSuffix.length() > 0 ? strSuffix : "") + ".log already exists!");
            }
        } catch (FileNotFoundException ex) {
            Log.d("Logging","ERROR: Unable to create public logging file!");
            Log.d("Logging",ex.toString());
            return;
        }

        Log.d("Logging","SRC = " + src.getAbsolutePath() + " (" + src.length()/1024 + " kb) // DST = " + dst.getAbsolutePath());

        File expFile = new File(strPublicLogPath);

        FileChannel inChannel;
        FileChannel outChannel;

        try {
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(expFile).getChannel();
        } catch (FileNotFoundException ex) {
            Log.d("Logging","ERROR: Unable to establish inChannel / outChannel!");
            Log.d("Logging",ex.toString());
            return;
        }

        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (FileNotFoundException ex) {
            Log.d("Logging","ERROR: Unable to transfer contents from internal ReconMobile.log to public log!");
            Log.d("Logging",ex.toString());
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
            exportLogFile("_last");
            createLogFile();
            FileHandler logHandler = new FileHandler(Globals.logsDir + File.separator + "ReconMobile.log");
            logger.addHandler(logHandler);
            SimpleFormatter formatter = new SimpleFormatter();
            logHandler.setFormatter(formatter);
        } catch (IOException | SecurityException ex) {
            System.out.println("ERROR: Unhandled exception in Logging::prepareLogging()...");
            System.out.println(ex);
        }
    }
}
