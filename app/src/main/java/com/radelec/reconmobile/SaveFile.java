// Simple class to handle updating of text file if the user wants to save/update/edit in-app
package com.radelec.reconmobile;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SaveFile {

    public static String main(File oldFile) throws IOException {
        BufferedReader br = null;
        String currentLine = null;
        BufferedWriter bw = null;
        boolean custInfoFound = false, testSiteInfoFound = false;
        boolean custLinesCounted = false, testSiteLinesCounted = false;
        boolean emailFound = false;
        int customerLineCounter = 0;
        int testSiteLineCounter = 0;
        String oldFileName = oldFile.getCanonicalPath();
        String updatedFileName = oldFile.getName();
        String newline = Constants.newline;
        ArrayList<String> workingFile = new ArrayList<String>();

        Logging.main("SaveFile","Creating buffered writer and beginning to save file...");

        try {

            // LOAD ORIGINAL INTO RAM FOR EVALUATION
            br = new BufferedReader(new InputStreamReader(new FileInputStream(oldFileName)));

            while ((currentLine = br.readLine()) != null)
                workingFile.add(currentLine);

            br.close();

            // TRIM OUT ALL EXISTING DATA UNDER CUSTOMER AND TEST SITE INFO!
            // Count how many lines to remove...
            for (int i = 0; i < workingFile.size(); i++) {
                if (custInfoFound && !custLinesCounted) {
                    if (workingFile.get(i).contains("Test site"))
                        custLinesCounted = true;
                    else
                        customerLineCounter++;
                }

                if (testSiteInfoFound && !testSiteLinesCounted) {
                    if (workingFile.get(i).contains("SUMMARY") || workingFile.get(i).contains("Instrument"))
                        testSiteLinesCounted = true;
                    else
                        testSiteLineCounter++;
                }

                if (workingFile.get(i).contains("Customer info"))
                    custInfoFound = true;

                if (workingFile.get(i).contains("Test site info"))
                    testSiteInfoFound = true;

                if(workingFile.get(i).contains("Email:"))
                    emailFound = true;
            }

            // Let's go through one final time to remove the customer and test site lines...
            // Let's also add whatever deployment and technician values are presently set in Config
            for (int i = 0; i < workingFile.size(); i++) {
                if (i > 0) {
                    if (workingFile.get(i - 1).contains("Customer info")) {
                        for (int j = 0; j < customerLineCounter; j++)
                            workingFile.remove(i);
                    }

                    if (workingFile.get(i - 1).contains("Test site")) {
                        for (int k = 0; k < testSiteLineCounter; k++)
                            workingFile.remove(i);
                    }

                    if (workingFile.get(i).contains("Location:") || workingFile.get(i).contains("Room:")) {
                        workingFile.remove(i);
                        workingFile.add(i, "Location: " + Globals.loadedLocationDeployed);
                    }

                    if (workingFile.get(i).contains("Protocol:")) {
                        workingFile.remove(i);
                        workingFile.add(i, "Protocol: " + Globals.loadedReportProtocol);
                    }

                    if (workingFile.get(i).contains("Tampering:")) {
                        workingFile.remove(i);
                        workingFile.add(i, "Tampering: " + Globals.loadedReportTampering);
                    }

                    if (workingFile.get(i).contains("Weather:")) {
                        workingFile.remove(i);
                        workingFile.add(i, "Weather: " + Globals.loadedReportWeather);
                    }

                    if (workingFile.get(i).contains("Mitigation:")) {
                        workingFile.remove(i);
                        workingFile.add(i, "Mitigation: " + Globals.loadedReportMitigation);
                    }

                    if (workingFile.get(i).contains("Comment:")) {
                        workingFile.remove(i);
                        workingFile.add(i, "Comment: " + Globals.loadedReportComment);
                    }

                    if (workingFile.get(i).contains("Analyzed By")) {
                        workingFile.remove(i);
                        workingFile.add(i, "Analyzed By: " + Globals.loadedAnalyzedBy);
                    }

                    if (workingFile.get(i).contains("Deployed By")) {
                        workingFile.remove(i);
                        workingFile.add(i, "Deployed By: " + Globals.loadedDeployedBy);
                    }

                    if (workingFile.get(i).contains("Retrieved By")) {
                        workingFile.remove(i);
                        workingFile.add(i, "Retrieved By: " + Globals.loadedRetrievedBy);
                    }

                    if(workingFile.get(i).contains("Email:")) {
                        workingFile.remove(i);
                        workingFile.add(i,"Email: " + Globals.loadedEmail);
                    }
                }
            }

            Logging.main("SaveFile","Creating buffered writer and beginning to save file...");
            bw = new BufferedWriter(new FileWriter(new File(Globals.fileDir + File.separator + updatedFileName)));

            // Write out to new file.
            for (int i = 0; i < workingFile.size(); i++) {
                currentLine = workingFile.get(i);
                bw.write(currentLine + newline);

                if (currentLine.contains("Customer information:")) {
                    if (Globals.loadedCustomerInfo.length() > 0)
                        bw.write(Globals.loadedCustomerInfo + newline + newline + newline);
                    else
                        bw.write(newline + newline + newline);
                }

                if (currentLine.contains("Test site information:")) {
                    if (Globals.loadedTestSiteInfo.length() > 0)
                        bw.write(Globals.loadedTestSiteInfo + newline + newline + newline);
                    else
                        bw.write(newline + newline + newline);
                }
            }

            bw.close();
            Logging.main("SaveFile","File (" + Globals.globalLoadedFileName + ") has been manually saved by user!");
            return "Saving file...";
        }

        catch (Exception anyEx) {
            Logging.main("SaveFile","EXCEPTION: " + (anyEx.toString()));
            return "Error saving file...";
        }
    }
}