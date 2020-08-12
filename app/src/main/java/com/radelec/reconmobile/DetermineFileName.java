package com.radelec.reconmobile;

import android.database.Cursor;
import android.util.Log;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.radelec.reconmobile.Globals.*;

public class DetermineFileName {

    // Loads filenames into strings here so that it does not need to be done
    // in CreateTXT and CreateXLS individually
    public static String determineFileName() {

        Log.d("DetermineFileName","determineFileName() called!");

        String TXT_name = null;			// filename we want to use for the txt
        String defaultFilename = DetermineFileName.SetDefaultFilename(); //default filename
        long fileIteration = 1;			// digit to be added to end of filename if the file exists
        boolean DoesReconFileExist = true;	// used to control the loop below
        boolean TXT_exists = false;		// self explanatory
        File TXT_file = null;			// File object for determining if txt exists
        int loopCounter = 0;			// count how many times we've gone through the loop
        Pattern acceptableFilenameChars = Pattern.compile("[a-zA-Z0-9\\-\\_\\.]*");
        Matcher patternMatcher;

        Cursor cursorReportDefaults;
        cursorReportDefaults = globalDBDefaults.getReportDefaultData();
        cursorReportDefaults.moveToFirst(); //Critical to moveToFirst() here, or else we're sitting at an invalid index.

        // Get the first line of Test Site Info in case we need it:
        String[] testSiteArray = cursorReportDefaults.getString(3).split("\\r?\\n");
        String testSite = testSiteArray[0];
        testSite = testSite.replaceAll("[\\n\\r+]", "");
        testSite = testSite.replaceAll(" ",""); //Let's get rid of spaces, too... just in case.
        testSite = testSite.replaceAll("\\.",""); //And periods, just for good measure.

        //This while loop will check for user input and determine if we need to tack a number onto the end of the filename to avoid overwrites.
        while(DoesReconFileExist) {
            Log.d("DetermineFileName","DoesReconFileExist = " + DoesReconFileExist + " // loopCounter = " + loopCounter);
            // Are we using the new filenaming method?
            if (boolUseTestSiteFileName && testSite.length() > 0) {
                Log.d("DetermineFileName","Attempting to dynamically generate filename...");
                Log.d("DetermineFileName","preliminaryFileName = " + testSite);
                // check for invalid characters in TestSiteInfo
                patternMatcher = acceptableFilenameChars.matcher(testSite);
                if (!patternMatcher.matches()) {
                    boolInvalidFileName = true;

                    // use the default this time
                    if (loopCounter > 0) {
                        TXT_name = fileDir + File.separator + defaultFilename + "-" + fileIteration + ".txt";
                    }
                    else {
                        TXT_name = fileDir + File.separator + defaultFilename + ".txt";
                    }
                }
                else {
                    // Don't tack on the digit if the files don't already exist
                    if (loopCounter > 0) {
                        TXT_name = fileDir + File.separator + testSite + "-" + fileIteration + ".txt";
                        boolInvalidFileName = false;
                    }
                    else {
                        TXT_name = fileDir + File.separator + testSite + ".txt";
                        boolInvalidFileName = false;
                    }
                }
            }
            // Otherwise, use the default file naming method.
            else {
                if (loopCounter > 0) {
                    TXT_name = fileDir + File.separator + defaultFilename + "-" + fileIteration + ".txt";
            }
                else {
                    TXT_name = fileDir + File.separator + defaultFilename + ".txt";
                }
                boolInvalidFileName = false;
            }

            // Determine if the files exist with the names we want to use.
            TXT_file = new File(TXT_name);
            TXT_exists = TXT_file.exists();

            // If nothing exists, we're done. Break the loop.
            if(!TXT_exists) {
                DoesReconFileExist = false;
            }
            // Or increment our end digit if we have to.
            if(DoesReconFileExist) {
                fileIteration++;
                loopCounter++;
            }
        }

        // Finally, set the names that are going to be used.
        Log.d("DetermineFileName","FileName = " + TXT_name);
        return TXT_name;
    }

    public static String SetDefaultFilename() {
        // Get serial number and start date in case we need to use the default filename:
        Log.d("DetermineFileName","SetDefaultFileName() called!");
        DateFormat dateFormat = new SimpleDateFormat("MMddyyyy");
        Calendar cal = Calendar.getInstance();
        String ConfirmSN = globalReconSerial;
        dateFormat.setCalendar(cal);
        String strFileName = "Recon_" + ConfirmSN + "_" + dateFormat.format(cal.getTime());
        Log.d("DetermineFileName","Default FileName (if employed) = " + strFileName);
        return strFileName;
    }

}
