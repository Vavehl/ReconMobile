package com.radelec.reconmobile;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import static com.radelec.reconmobile.Globals.*;

public class LoadSavedFile {

        public static double LoadedReconCF1 = 6;
        public static double LoadedReconCF2 = 6;
        public static String strTestSiteInfo = "";
        public static String strCustomerInfo = "";
        public static String strLocation = "";
        public static String strStartDate = "Unknown Start Date";
        public static String strEndDate = "Unknown End Date";
        public static String strUnitSystem = "US";
        public static String strInstrumentSerial = "Unknown";
        public static String strDeployedBy = "Unknown";
        public static String strRetrievedBy = "Unknown";
        public static String strAnalyzedBy = "Unknown";
        public static String strCalDate = "Unknown";
        public static String strReportProtocol = "Unknown";
        public static String strReportTampering = "Unknown";
        public static String strReportWeather = "Unknown";
        public static String strReportMitigation = "Unknown";
        public static String strReportComment = "Unknown";
        public static String strRoomDeployed = "Unknown";
        public static String strEmail = "";

        public static void main(String ReconTXTFile, String strFileName) {
            Logging.main("LoadSavedFile","LoadSavedFile() called!");
            Logging.main("LoadSavedFile","Full Path = " + ReconTXTFile + " // FileName = " + strFileName);

            //Variable declarations
            ArrayList<String> arrLine = new ArrayList<>();
            ArrayList<String> arrLine_temp = new ArrayList<>();
            String[] strLine_parsed;
            boolean testSiteFlag = false;
            boolean customerInfoFlag = false;
            int i = 0;

            // clear customer and test site strings every time a new file is opened
            strTestSiteInfo = "";
            strCustomerInfo = "";
            strLocation = "";

            try {
                //Config getUnits = new Config();
                strUnitSystem = Globals.globalUnitType;
                strInstrumentSerial = getReconSerialFromFileName(strFileName); //We should still call this, as it will work with older text files.

                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(ReconTXTFile)));

                Globals.LoadedReconTXTFile.clear(); //We should definitely clear this each time a file is loaded, or else it will continue to grow...

                for (String strLine = br.readLine(); strLine != null; strLine = br.readLine()) {
                    if(strLine.length()>0) { //If the line is blank, then we definitely don't want to try to split the string.
                        strLine = strLine.replace("[", ""); //Remove left brackets from strLine (introduced with new text file format)
                        strLine = strLine.replace("]", ""); //Remove right brackets from strLine (introduced with new text file format)
                        strLine_parsed = StringUtils.split(strLine, ","); //splits strLine into the strLine_parsed[] string array.
                        if(strLine_parsed[0].equals("=DB")) { //make sure that we only add valid data files into our two-dimensional string array (LoadedReconTXTFile)...
                            for(int arrayCounter = 0; arrayCounter <= strLine_parsed.length -1; arrayCounter++) {
                                arrLine.add(arrayCounter, strLine_parsed[arrayCounter].trim()); //This will add each element in strLine_parsed to the temporary arrLine ArrayList.
                            }
                            arrLine_temp = (ArrayList<String>) arrLine.clone(); //This seems really stupid, but if you don't clone the ArrayList to a temporary holder, it'll be lost after arrLine.clear() below.
                            Globals.LoadedReconTXTFile.add(arrLine_temp); //This will add the temporary arrLine into the primary LoadedReconTXTFile ArrayList.
                            Logging.main("LoadSavedFile",Arrays.toString(Globals.LoadedReconTXTFile.get(i).toArray()));
                            Logging.main("LoadSavedFile","Adding record #"+Globals.LoadedReconTXTFile.get(i).get(1)+" to ArrayList, whose new size is now "+Globals.LoadedReconTXTFile.size()+".");
                            arrLine.clear(); //If we don't clear arrLine, it will turn into one massive, single-dimensional string array...
                            Logging.main("LoadSavedFile","Checking Status: "+ Arrays.toString(Globals.LoadedReconTXTFile.get(i).toArray()));
                            i++;
                        }
                        if(strLine.contains("Instrument Serial: ")) {
                            strLine_parsed = StringUtils.split(strLine, " "); //Need to parse again to segregate spaces, not commas.
                            try {
                                strInstrumentSerial = strLine_parsed[2];
                                Globals.globalReconSerial = strInstrumentSerial;
                                Logging.main("LoadSavedFile","Serial# found and parsed: " + strInstrumentSerial);
                            } catch(ArrayIndexOutOfBoundsException ex) {
                                strInstrumentSerial = "Unknown";
                                Logging.main("LoadSavedFile","WARNING Out Of Bounds: Serial# not parsed.");
                            }
                        }
                        if(strLine.contains("Chamber 1 CF: ")) {
                            strLine_parsed = StringUtils.split(strLine, " "); //Need to parse again to segregate spaces, not commas.
                            try {
                                LoadedReconCF1 = Double.parseDouble(strLine_parsed[3]);
                                Globals.globalReconCF1 = LoadedReconCF1;
                                Logging.main("LoadSavedFile", "CF1 found and parsed: " + LoadedReconCF1);
                            } catch(ArrayIndexOutOfBoundsException ex) {
                                LoadedReconCF1 = 6;
                                Globals.globalReconCF1 = LoadedReconCF1;
                                Logging.main("LoadSavedFile","WARNING Out of Bounds: CF1 not parsed. Defaulting to 6...");
                            }
                        }
                        if(strLine.contains("Chamber 2 CF: ")) {
                            strLine_parsed = StringUtils.split(strLine, " "); //Need to parse again to segregate spaces, not commas.
                            try {
                                LoadedReconCF2 = Double.parseDouble(strLine_parsed[3]);
                                Globals.globalReconCF2 = LoadedReconCF2;
                                Logging.main("LoadSavedFile", "CF2 found and parsed: " + LoadedReconCF2);
                            } catch(ArrayIndexOutOfBoundsException ex) {
                                LoadedReconCF2 = 6;
                                Globals.globalReconCF2 = LoadedReconCF2;
                                Logging.main("LoadSavedFile","WARNING Out of Bounds: CF2 not parsed. Defaulting to 6...");
                            }
                        }
                        if(strLine.contains("Start Date/Time:")) {
                            strLine_parsed = StringUtils.split(strLine, " ");
                            try {
                                strStartDate = strLine_parsed[2] + " " + strLine_parsed[3];
                            } catch(Exception ex) {
                                strStartDate = "Unknown Start Date";
                                Logging.main("LoadSavedFile","WARNING: Start Date not parsed...");
                            }
                        }
                        if(strLine.contains("End Date/Time:")) {
                            strLine_parsed = StringUtils.split(strLine, " ");
                            try {
                                strEndDate = strLine_parsed[2] + " " + strLine_parsed[3];
                            } catch(Exception ex) {
                                strEndDate = "Unknown End Date";
                                Logging.main("LoadSavedFile","WARNING: End Date not parsed...");
                            }
                        }
                        if(strLine.contains("Deployed By:")) {
                            strDeployedBy = strLine.substring(12);
                            loadedDeployedBy = strDeployedBy;
                            Logging.main("LoadSavedFile","DeployedBy found and parsed: " + loadedDeployedBy);
                        }
                        if(strLine.contains("Retrieved By:")) {
                            strRetrievedBy = strLine.substring(13);
                            loadedRetrievedBy = strRetrievedBy;
                            Logging.main("LoadSavedFile","RetrievedBy found and parsed: " + loadedRetrievedBy);
                        }
                        if(strLine.contains("Analyzed By:")) {
                            strAnalyzedBy = strLine.substring(12);
                            loadedAnalyzedBy = strAnalyzedBy;
                            Logging.main("LoadSavedFile","AnalyzedBy found and parsed: " + loadedAnalyzedBy);
                        }
                        if(strLine.contains("Calibration Date =")) {
                            strLine_parsed = StringUtils.split(strLine, "=");
                            strCalDate = strLine_parsed[1].trim();
                            loadedCalibrationDate = strCalDate;
                            Globals.globalReconCalibrationDate = loadedCalibrationDate;
                            Logging.main("LoadSavedFile","Calibration Date found and parsed: " + loadedCalibrationDate);
                        }
                        if(strLine.length() > 8 && strLine.substring(0,9).contains("Protocol:")) {
                            strReportProtocol = strLine.substring(9).trim(); //Should robustly parse protocol.
                            loadedReportProtocol = strReportProtocol;
                            Logging.main("LoadSavedFile","Protocol found and parsed: " + loadedReportProtocol);
                        } else if(strLine.length() > 9 && strLine.substring(0,10).contains("Tampering:")) {
                            strReportTampering = strLine.substring(10).trim(); //Should robustly parse tampering.
                            loadedReportTampering = strReportTampering;
                            Logging.main("LoadSavedFile","Tampering found and parsed: " + loadedReportTampering);
                        } else if(strLine.length() > 7 && strLine.substring(0,8).contains("Weather:")) {
                            strReportWeather = strLine.substring(8).trim(); //Should robustly parse weather.
                            loadedReportWeather = strReportWeather;
                            Logging.main("LoadSavedFile","Weather found and parsed: " + loadedReportWeather);
                        } else if(strLine.length() > 10 && strLine.substring(0,11).contains("Mitigation:")) {
                            strReportMitigation = strLine.substring(11).trim(); //Should robustly parse mitigation.
                            loadedReportMitigation = strReportMitigation;
                            Logging.main("LoadSavedFile","Mitigation found and parsed: " + loadedReportMitigation);
                        } else if(strLine.length() > 7 && strLine.substring(0,8).contains("Comment:")) {
                            strReportComment = strLine.substring(8).trim(); //Should robustly parse comment.
                            loadedReportComment = strReportComment;
                            Logging.main("LoadSavedFile","Comment found and parsed: " + loadedReportComment);
                        } else if((strLine.length() > 8 && strLine.substring(0,9).contains("Location:"))||(strLine.length()>5 && strLine.substring(0,5).contains("Room:"))) {
                            if(strLine.length() > 8 && strLine.substring(0,9).contains("Location:")) strRoomDeployed = strLine.substring(10).trim();
                            if(strLine.substring(0,5).contains("Room:")) strRoomDeployed = strLine.substring(6).trim();
                            loadedLocationDeployed = strRoomDeployed;
                            Logging.main("LoadSavedFile","Location found and parsed: " + loadedLocationDeployed);
                        } else if(strLine.length() > 5 && strLine.substring(0,6).contains("Email:")) {
                            strEmail = strLine.substring(6).trim();
                            loadedEmail = strEmail;
                            Logging.main("LoadSavedFile","Email found and parsed: " + loadedEmail);
                        }
                        //BEGIN: Test Site Parsing Block
                        if(testSiteFlag) {
                            if(strLine.contains("Instrument Serial:") || strLine.contains("SUMMARY:")) {
                                testSiteFlag = false;
                                if (strTestSiteInfo.length() > 1) {
                                    strTestSiteInfo = strTestSiteInfo.trim(); //trim any anteceding or succeeding line-feeds...
                                    loadedTestSiteInfo = strTestSiteInfo;
                                    Logging.main("LoadSavedFile","Test Site Info: " + strTestSiteInfo);
                                } else {
                                    Logging.main("LoadSavedFile","Unable to find any Test Site Info in " + strFileName + "!");
                                }
                            } else {
                                strTestSiteInfo = strTestSiteInfo + "\n" + strLine;
                                loadedTestSiteInfo = strTestSiteInfo;
                            }
                        }
                        if(strLine.contains("Test site information:")) { //if we find this, then we know that our test site info will be in the next line.
                            testSiteFlag = true;
                        }
                        //END: Test Site Parsing Block

                        //BEGIN: Customer Info Parsing Block
                        if(customerInfoFlag) {
                            if(strLine.contains("Test site information:")) { //If we find this, we know we're past our customer info block.
                                customerInfoFlag = false;
                                if (strCustomerInfo.length() > 1) {
                                    strCustomerInfo = strCustomerInfo.trim(); //trim any anteceding or succeeding line-feeds...
                                    loadedCustomerInfo = strCustomerInfo;
                                    Logging.main("LoadSavedFile","Customer Info: " + strCustomerInfo);
                                } else {
                                    Logging.main("LoadSavedFile","Unable to find any Customer Info in " + strFileName + "!");
                                }
                            } else {
                                strCustomerInfo = strCustomerInfo + "\n" + strLine;
                                loadedCustomerInfo = strCustomerInfo;
                            }
                        }
                        if(strLine.contains("Customer information:")) { //if we find this, then we know that our customer info will be in the next line.
                            customerInfoFlag = true;
                        }
                        //END: Customer Info Parsing Block

                        //BEGIN: Parse test Location string
                        if(strLine.contains("Location:") && strLine.length() > 11) {
                            strLocation = strLine;
                            strLocation = strLocation.trim();
                            strLocation = strLocation.substring(10);
                            loadedLocationDeployed = strLocation;
                        }
                        //END: Parse test Location string
                    }
                }

                br.close();

                Globals.globalLoadedFileName = strFileName;
                Globals.LoadedReconCF1 = LoadedReconCF1;
                Globals.LoadedReconCF2 = LoadedReconCF2;
                Logging.main("LoadSavedFile", "Assigning LoadedReconCF1 to Globals.LoadedReconCF1 = " + Globals.LoadedReconCF1);
                Logging.main("LoadSavedFile", "Assigning LoadedReconCF2 to Globals.LoadedReconCF2 = " + Globals.LoadedReconCF2);
                Logging.main("LoadSavedFile","File (" + Globals.globalLoadedFileName + ") successfully loaded!");

                //Creates graph
                //Reset photodiode failure booleans...
                //CreateGraph.photodiodeFailure_Ch1 = false;
                //CreateGraph.photodiodeFailure_Ch2 = false;
                //CreateGraph.main(test_args);
                CreateGraphArrays.main();

            } catch (FileNotFoundException ex) {
                Logging.main("LoadSavedFile","ERROR: Unable to find the requested Recon TXT file in LoadSavedFile.java!");
                Logger.getLogger(LoadSavedFile.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logging.main("LoadSavedFile","ERROR: Fundamental IO Error encountered when parsing Recon TXT file.");
                Logger.getLogger(LoadSavedFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public static String getReconSerialFromFileName(String strFileName) {
            //The instrument serial is now stored in the TXT file, as of v0.8.2.
            //This method only exists to maintain backwards compatibility with
            //older text files and to serve as a fallback if no serial# is found.
            if(strFileName.length() > 0) {
                String[] str_Parsed = strFileName.split("_");
                if(str_Parsed.length > 2) {
                    return str_Parsed[1];
                } else {
                    return "Unknown Serial";
                }
            }
            return "Unknown Serial";
        }
}
