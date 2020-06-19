// CreateTXT.java
// Similarly to the RDT, the purpose of this class is to create the Recon TXT file.

package com.radelec.reconmobile;

import android.database.Cursor;
import android.os.Build;
import android.util.Log;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedList;
import java.util.Arrays;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import static com.radelec.reconmobile.Globals.*;
import static com.radelec.reconmobile.Constants.*;

public class CreateTXT {

    public static PrintWriter writer; // declaration - is defined later

    public static void main() {

        Log.d("CreateTXT","CreateTXT() called!");
        String ReconWaitTime = "Unknown";
        String ReconDurationSetting = "Unknown";
        String ReconCalDate = "Unknown";
        String strInstrumentSerial = "Unknown";

        double AvgHumidity = 0;
        double AvgTemperature = 0;
        double AvgPressure = 0;
        long TotalMovements = 0;
        long TotalChamber1Counts = 0;
        long TotalChamber2Counts = 0;
        double CF1 = 0;
        double CF2 = 0;
        boolean BeginAveraging = false;
        long ActiveRecordCounts = 0;
        int TempYear = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
        LocalDateTime StartDate = null;
        LocalDateTime EndDate = null;
        Date deprecatedStartDate = null;
        Date deprecatedEndDate = null;
        DecimalFormat RoundAvg = new DecimalFormat("####0.00");
        long totalSeconds;
        long testHours;
        long testMinutes;
        long testSeconds;

        // variables needed for tallying hourly counts and calculating radon
        int tenMinuteCounter = 0; //3 // used to determine when to stow away hourly count values
        int ch1Counter = 0;
        int ch2Counter = 0;
        double avgResult1 = 0;
        double avgResult2 = 0;

        // used when traversing session list
        int sessionCounter = 0;

        //Used to determine if a photodiode failure has occurred
        int consecutiveZeroTally_Ch1 = 0; //This will tally the consecutive number of hourly zero counts on chamber 1
        int consecutiveZeroTally_Ch2 = 0; //This will tally the consecutive number of hourly zero counts on chamber 2
        boolean photodiodeFailure_Ch1 = false; //If consecutiveZeroTally_Ch1 >= MainMenuUI.ConsecutiveZeroLimit (default=5), this becomes true.
        boolean photodiodeFailure_Ch2 = false; //If consecutiveZeroTally_Ch2 >= MainMenuUI.ConsecutiveZeroLimit (default=5), this becomes true.

        // these are used for numerical formats at the final stage of writing to text
        // the DecimalFormat object has the ability to set properties such as rounding up or down
        // investigate later if we need more precision or if something is wrong
        DecimalFormat cfDec = new DecimalFormat("0.000"); // decimal format for calibration factors

        // decimal formats for radon concentration
        DecimalFormat df = new DecimalFormat("0.0");
        DecimalFormat si = new DecimalFormat("0");

        LinkedList<CountContainer> AllHourlyCounts = new LinkedList(); // list which will hold groups of hourly counts

        //List<Entry> listRadon = new ArrayList<Entry>();

        // pull CF's
        CF1 = globalReconCF1; //We need to add error-handling for this...
        CF2 = globalReconCF2; //We need to add error-handling for this, too...
        ReconCalDate = globalReconCalibrationDate;
        strInstrumentSerial = globalReconSerial;

        ReconWaitTime = arrayDataSession.get(0)[12]; //Needs error handling!
        ReconDurationSetting = arrayDataSession.get(0)[13]; //Needs error handling!

        Cursor cursorReportDefaults;
        cursorReportDefaults = globalDBDefaults.getReportDefaultData();
        cursorReportDefaults.moveToFirst(); //Critical to moveToFirst() here, or else we're sitting at an invalid index.

        Log.d("CreateTXT","All variables initialized!");
        Log.d("CreateTXT","CF1 = " + CF1 + " / CF2 = " + CF2);
        // create text file
        try {
            Log.d("CreateTXT","Beginning to create TXT file. Array size = " + arrayDataSession.size());
            String strFileName = DetermineFileName.determineFileName();
            writer = new PrintWriter(strFileName, "UTF-8");
            Log.d("CreateTXT","FileName = " + strFileName);

            // print first line of data (start of test)
            writer.println(Arrays.toString(arrayDataSession.get(sessionCounter)));

            while (sessionCounter < arrayDataSession.size()) {
                Log.d("CreateTXT","Creating TXT file with Record #" + sessionCounter);
                if (arrayDataSession.get(sessionCounter)[2].equals("S")) {
                    Log.d("CreateTXT", "Found S flag! Beginning to average...");
                    BeginAveraging = true;
                    TempYear = 2000 + Integer.parseInt(arrayDataSession.get(sessionCounter)[3]);
                    if (Build.VERSION.SDK_INT >= 26) {
                        StartDate = LocalDateTime.of(TempYear, Integer.parseInt(arrayDataSession.get(sessionCounter)[4]), Integer.parseInt(arrayDataSession.get(sessionCounter)[5]), Integer.parseInt(arrayDataSession.get(sessionCounter)[6]), Integer.parseInt(arrayDataSession.get(sessionCounter)[7]), Integer.parseInt(arrayDataSession.get(sessionCounter)[8]));
                    } else {
                        deprecatedStartDate = new Date();
                        TempYear = Integer.parseInt(arrayDataSession.get(sessionCounter)[3])+100; //deprecatedHourCounter years start at 1900, not zero!
                        deprecatedStartDate.setYear(TempYear);
                        deprecatedStartDate.setMonth(Integer.parseInt(arrayDataSession.get(sessionCounter)[4])-1); //Old java.util.date has date range of 0 (January) to 11 (December). Let's subtract one to match this.
                        deprecatedStartDate.setDate(Integer.parseInt(arrayDataSession.get(sessionCounter)[5]));
                        deprecatedStartDate.setHours(Integer.parseInt(arrayDataSession.get(sessionCounter)[6]));
                        deprecatedStartDate.setMinutes(Integer.parseInt(arrayDataSession.get(sessionCounter)[7]));
                        deprecatedStartDate.setSeconds(Integer.parseInt(arrayDataSession.get(sessionCounter)[8]));
                    }
                }
                if (arrayDataSession.get(sessionCounter)[2].equals("I") && BeginAveraging == true) {
                    tenMinuteCounter++;
                }

                if (arrayDataSession.get(sessionCounter)[2].equals("E")) {
                    TempYear = 2000 + Integer.parseInt(arrayDataSession.get(sessionCounter)[3]);
                    Log.d("CreateTXT","Found E flag! Wrapping up...");
                    if (Build.VERSION.SDK_INT >= 26) {
                        EndDate = LocalDateTime.of(TempYear, Integer.parseInt(arrayDataSession.get(sessionCounter)[4]), Integer.parseInt(arrayDataSession.get(sessionCounter)[5]), Integer.parseInt(arrayDataSession.get(sessionCounter)[6]), Integer.parseInt(arrayDataSession.get(sessionCounter)[7]), Integer.parseInt(arrayDataSession.get(sessionCounter)[8]));
                    } else {
                        deprecatedEndDate = new Date();
                        TempYear = Integer.parseInt(arrayDataSession.get(sessionCounter)[3])+100; //deprecatedHourCounter years start at 1900, not zero!
                        deprecatedEndDate.setYear(TempYear);
                        deprecatedEndDate.setMonth(Integer.parseInt(arrayDataSession.get(sessionCounter)[4])-1); //Old java.util.date has date range of 0 (January) to 11 (December). Let's subtract one to match this.
                        deprecatedEndDate.setDate(Integer.parseInt(arrayDataSession.get(sessionCounter)[5]));
                        deprecatedEndDate.setHours(Integer.parseInt(arrayDataSession.get(sessionCounter)[6]));
                        deprecatedEndDate.setMinutes(Integer.parseInt(arrayDataSession.get(sessionCounter)[7]));
                        deprecatedEndDate.setSeconds(Integer.parseInt(arrayDataSession.get(sessionCounter)[8]));
                    }
                }
                if (arrayDataSession.get(sessionCounter)[2].equals("Z")) {
                    Log.d("CreateTXT","Found Z flag! Let's get out of here...");
                }
                if (BeginAveraging && !arrayDataSession.get(sessionCounter)[2].equals("Z")) {
                    ActiveRecordCounts++;
                    TotalMovements = TotalMovements + Long.parseLong(arrayDataSession.get(sessionCounter)[9]);
                    TotalChamber1Counts = TotalChamber1Counts + Long.parseLong(arrayDataSession.get(sessionCounter)[10]);
                    TotalChamber2Counts = TotalChamber2Counts + Long.parseLong(arrayDataSession.get(sessionCounter)[11]);
                    AvgHumidity = (AvgHumidity + Double.parseDouble(arrayDataSession.get(sessionCounter)[15]));
                    AvgPressure = (AvgPressure + Double.parseDouble(arrayDataSession.get(sessionCounter)[18]));
                    AvgTemperature = (AvgTemperature + Double.parseDouble(arrayDataSession.get(sessionCounter)[21]));

                    // section of code to tally counts and push hourly values into linked list for later analysis
                    // if (!LTMode) - do not forget this won't work for LT mode!
                    ch1Counter += Integer.parseInt(arrayDataSession.get(sessionCounter)[10]);
                    ch2Counter += Integer.parseInt(arrayDataSession.get(sessionCounter)[11]);

                    //Consecutive Zero Tally, for determining possible photodiode failure...
                    if(ch1Counter==0) {
                        consecutiveZeroTally_Ch1++;
                        if(consecutiveZeroTally_Ch1>=ConsecutiveZeroLimit) {
                            Log.d("CreateTXT","WARNING: " + ConsecutiveZeroLimit + " consecutive zero counts read on Chamber 1!");
                            photodiodeFailure_Ch1 = true;
                        }
                    } else {
                        consecutiveZeroTally_Ch1=0;
                    }
                    if(ch2Counter==0) {
                        consecutiveZeroTally_Ch2++;
                        if(consecutiveZeroTally_Ch2>=ConsecutiveZeroLimit) {
                            Log.d("CreateTXT","WARNING: " + ConsecutiveZeroLimit + " consecutive zero counts read on Chamber 2!");
                            photodiodeFailure_Ch2 = true;
                        }
                    } else {
                        consecutiveZeroTally_Ch2=0;
                    }


                    if (boolLongTermMode) {
                        if (tenMinuteCounter == 2) {
                            AllHourlyCounts.addLast(new CountContainer(ch1Counter, ch2Counter));
                            ch1Counter = 0;
                            ch2Counter = 0;
                            tenMinuteCounter = 0;
                        }
                    }
                    else {
                        if (tenMinuteCounter == 6) {
                            AllHourlyCounts.addLast(new CountContainer(ch1Counter, ch2Counter)); // add new grouping of hourly totals to the list
                            // clear chamber counters
                            ch1Counter = 0;
                            ch2Counter = 0;
                            tenMinuteCounter = 0;
                        }
                    }
                }

                if (sessionCounter > 0) // we've already written that one outside the loop
                    writer.println(Arrays.toString(arrayDataSession.get(sessionCounter)));

                sessionCounter++;
            }  // end while loop

            Log.d("CreateTXT","BeginAveraging= " + BeginAveraging);
            // do this if we're in diagnostic mode
            if (BeginAveraging && boolDiagnosticMode) {
                Log.d("CreateTXT","Creating TXT details in diagnostic mode.");
                // write customer info to file
                writer.println(newline);
                writer.println("Customer information:");
                writer.println(cursorReportDefaults.getString(2));
                writer.println(newline);

                // write test site info to file
                writer.println("Test site information:");
                writer.println(cursorReportDefaults.getString(3));
                writer.println(newline);

                if (photodiodeFailure_Ch1 || photodiodeFailure_Ch2) {
                    if (photodiodeFailure_Ch1) {
                        writer.println("POSSIBLE DETECTOR FAILURE IN CHAMBER 1!");
                    }
                    if (photodiodeFailure_Ch2) {
                        writer.println("POSSIBLE DETECTOR FAILURE IN CHAMBER 2!");
                    }
                    writer.println(newline);
                }

                writer.println("SUMMARY:");
                writer.println("Instrument Serial: " + strInstrumentSerial);
                if (Build.VERSION.SDK_INT >= 26) {
                    writer.println("Start Date/Time: " + sdf.format(StartDate));
                    writer.println("End Date/Time: " + sdf.format(EndDate));
                    Duration radonDuration = Duration.between(StartDate, EndDate);
                    totalSeconds = radonDuration.getSeconds();
                } else {
                    writer.println("Start Date/Time: " + sdf.format(deprecatedStartDate));
                    writer.println("End Date/Time: " + sdf.format(deprecatedEndDate));
                    long diffInMilliseconds = Math.abs(deprecatedEndDate.getTime() - deprecatedStartDate.getTime());
                    totalSeconds = diffInMilliseconds / 1000;
                }
                testHours = totalSeconds / 3600;
                testMinutes = ((totalSeconds % 3600) / 60);
                testSeconds = (totalSeconds % 60);
                writer.println("Total Test Duration: " + testHours + " hours, " + testMinutes + " minutes, " + testSeconds + " seconds");
                writer.println("Chamber 1 Total Counts: " + TotalChamber1Counts);
                writer.println("Chamber 2 Total Counts: " + TotalChamber2Counts);
                writer.println("Total Movements: " + TotalMovements);
                writer.println("Avg. Humidity = " + RoundAvg.format(AvgHumidity / ActiveRecordCounts) + "%");
                writer.println("Avg. Pressure = " + RoundAvg.format(AvgPressure / ActiveRecordCounts) + " mmHg");
                writer.println("Avg. Temperature = " + RoundAvg.format(AvgTemperature / ActiveRecordCounts) + "C");
                writer.println("Instrument Wait Setting = " + ReconWaitTime);
                writer.println("Instrument Duration Setting = " + ReconDurationSetting);
                writer.println("Chamber 1 CF: " + cfDec.format(CF1));
                writer.println("Chamber 2 CF: " + cfDec.format(CF2));
                writer.println("Calibration Date = " + ReconCalDate);
                writer.println("Protocol: " + cursorReportDefaults.getString(7));
                writer.println("Tampering: " + cursorReportDefaults.getString(8));
                writer.println("Weather: " + cursorReportDefaults.getString(9));
                writer.println("Mitigation: " + cursorReportDefaults.getString(10));
                writer.println("Comment: " + cursorReportDefaults.getString(11));
                writer.println("Location: " + cursorReportDefaults.getString(1));
                writer.println(newline);
                writer.println("Analyzed By: " + cursorReportDefaults.getString(6));
                writer.println("Deployed By: " + cursorReportDefaults.getString(4));
                writer.println("Retrieved By: " + cursorReportDefaults.getString(5));
                writer.println(newline);
            } else if (BeginAveraging) { // or this if we're in regular user mode
                Log.d("CreateTXT","Creating TXT details...");
                // write customer info to file
                writer.println(newline);
                writer.println("Customer information:");
                writer.println(cursorReportDefaults.getString(2));
                writer.println(newline);

                // write test site info to file
                writer.println("Test site information:");
                writer.println(cursorReportDefaults.getString(3));
                writer.println(newline);

                writer.println("Instrument Serial: " + strInstrumentSerial);
                if (Build.VERSION.SDK_INT >= 26) {
                    DateTimeFormatter DateTimeDisplay = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
                    writer.println("Start Date/Time: " + DateTimeDisplay.format(StartDate));
                    writer.println("End Date/Time: " + DateTimeDisplay.format(EndDate));
                    Duration radonDuration = Duration.between(StartDate, EndDate);
                    totalSeconds = radonDuration.getSeconds();
                } else {
                    writer.println("Start Date/Time: " + sdf.format(deprecatedStartDate));
                    writer.println("End Date/Time: " + sdf.format(deprecatedEndDate));
                    long diffInMilliseconds = Math.abs(deprecatedEndDate.getTime() - deprecatedStartDate.getTime());
                    totalSeconds = diffInMilliseconds / 1000;
                }
                testHours = totalSeconds / 3600;
                testMinutes = ((totalSeconds % 3600) / 60);
                testSeconds = (totalSeconds % 60);

                writer.println("Total Test Duration: " + testHours + " hours, " + testMinutes + " minutes, " + testSeconds + " seconds");
                writer.println("Total Movements: " + TotalMovements);
                writer.println("Avg. Humidity = " + RoundAvg.format(AvgHumidity / ActiveRecordCounts) + "%");
                writer.println("Avg. Pressure = " + RoundAvg.format(AvgPressure / ActiveRecordCounts) + " mmHg");
                writer.println("Avg. Temperature = " + RoundAvg.format(AvgTemperature / ActiveRecordCounts) + "C");
                writer.println("Chamber 1 CF: " + cfDec.format(CF1));
                writer.println("Chamber 2 CF: " + cfDec.format(CF2));
                writer.println("Calibration Date = " + ReconCalDate);
                writer.println("Protocol: " + cursorReportDefaults.getString(7));
                writer.println("Tampering: " + cursorReportDefaults.getString(8));
                writer.println("Weather: " + cursorReportDefaults.getString(9));
                writer.println("Mitigation: " + cursorReportDefaults.getString(10));
                writer.println("Comment: " + cursorReportDefaults.getString(11));
                writer.println("Location: " + cursorReportDefaults.getString(1));
                writer.println(newline);
                writer.println("Analyzed By: " + cursorReportDefaults.getString(6));
                writer.println("Deployed By: " + cursorReportDefaults.getString(4));
                writer.println("Retrieved By: " + cursorReportDefaults.getString(5));
                writer.println(newline);
            }
            // do following regardless of mode
            writer.println("Radon Concentration");

            if (globalUnitType.equals("US")) {
                writer.println("Unit: pCi/L");
            } else {
                writer.println("Unit: Bq/m3");
            }

            // write hourly radon values
            for (int loopCount1 = 0; loopCount1 < AllHourlyCounts.size(); loopCount1++) {
                if (globalUnitType.equals("US")) {
                    writer.println("Hour: " + (loopCount1));
                    writer.println("Ch1: " + df.format((double) AllHourlyCounts.get(loopCount1).getCh1HourlyCount() / CF1) + "\tCh2: " + df.format((double) AllHourlyCounts.get(loopCount1).getCh2HourlyCount() / CF2));
                } else { // assuming SI
                    writer.println("Hour: " + (loopCount1));
                    writer.println("Ch1: " + si.format((double) AllHourlyCounts.get(loopCount1).getCh1HourlyCount() / CF1 * 37) + "\tCh2: " + si.format((double) AllHourlyCounts.get(loopCount1).getCh2HourlyCount() / CF2 * 37));
                }
            }

            // perform averaging of results
            if (boolExcludeFirst4Hours) {
                for (int loopCount2 = 4; loopCount2 < AllHourlyCounts.size(); loopCount2++) {
                    avgResult1 += (AllHourlyCounts.get(loopCount2).getCh1HourlyCount() / CF1);
                    avgResult2 += (AllHourlyCounts.get(loopCount2).getCh2HourlyCount() / CF2);
                }

                avgResult1 = avgResult1 / (double)(AllHourlyCounts.size() - 4);
                avgResult2 = avgResult2 / (double)(AllHourlyCounts.size() - 4);
            }
            else {
                for (int loopCount2 = 0; loopCount2 < AllHourlyCounts.size(); loopCount2++) {
                    avgResult1 += (AllHourlyCounts.get(loopCount2).getCh1HourlyCount() / CF1);
                    avgResult2 += (AllHourlyCounts.get(loopCount2).getCh2HourlyCount() / CF2);
                }

                avgResult1 = avgResult1 / (double)AllHourlyCounts.size();
                avgResult2 = avgResult2 / (double)AllHourlyCounts.size();
            }

            writer.println(newline);

            if (globalUnitType.equals("US")) {
                writer.println("Chamber 1 Avg pCi/L = " + df.format(avgResult1));
                writer.println("Chamber 2 Avg pCi/L = " + df.format(avgResult2));
                writer.println("Average pCi/L = " + df.format((avgResult1 + avgResult2) / 2));
            } else {
                writer.println("Chamber 1 Avg Bq/m3 = " + si.format(avgResult1 * 37));
                writer.println("Chamber 2 Avg Bq/m3 = " + si.format(avgResult2 * 37));
                writer.println("Average Bq/m3 = " + si.format((avgResult1 + avgResult2) / 2 * 37));
            }
        } catch (UnsupportedEncodingException e) {
            Log.d("CreateTXT","Unhandled UnsupportedEncodingException!");
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            writer.close();
        }
    }
}
