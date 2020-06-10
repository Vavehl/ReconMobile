package com.radelec.reconmobile;

import android.util.Log;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import static com.radelec.reconmobile.Constants.ConsecutiveZeroLimit;
import static com.radelec.reconmobile.Constants.boolPhotodiodeFailureRecovery;
import static com.radelec.reconmobile.Globals.*;

public class CreateGraphArrays {

    public static void main() {

        double OverallAvgRnC = 0;
        boolean photodiodeFailure_Ch1 = false; //If CreateGraph.consecutiveZeroTally_Ch1 >= MainMenuUI.ConsecutiveZeroLimit (default=5), this becomes true.
        boolean photodiodeFailure_Ch2 = false; //If CreateGraph.consecutiveZeroTally_Ch2 >= MainMenuUI.ConsecutiveZeroLimit (default=5), this becomes true.

        ArrayList<ArrayList<String>> HourlyReconData = new ArrayList<>();
        ArrayList alEntries = new ArrayList<>();
        //DateTimeFormatter DateTimeDisplay = DateTimeFormatter.ofLocalizedDateTime("dd-mmm-yyyy");
        SimpleDateFormat DateTimeDisplay = new SimpleDateFormat("dd-mmm-yyyy hh:mm");
        LocalDateTime ReconDate = null;
        LocalDateTime HourCounter = null;

        //Number Format Stuff
        NumberFormat formatUS_RnC = new DecimalFormat("#0.0");
        NumberFormat formatSI_RnC = new DecimalFormat("#0");
        NumberFormat formatZero = new DecimalFormat("#0"); //redundant, but easier to read
        NumberFormat formatTenth = new DecimalFormat("#0.0");

        long Ch1Counts = 0;
        long Ch2Counts = 0;
        double hourlyAvgHumidity = 0;
        double hourlyAvgTemp = 0;
        double hourlyAvgPress = 0;
        double TotalAvgRnC = 0;
        double TotalAvgRnC_Ch1 =0;
        double TotalAvgRnC_Ch2 = 0;
        double TotalAvgRnC_Ch1_Raw = 0;
        double TotalAvgRnC_Ch2_Raw = 0;
        long TotalHourCounter = 0;
        long hourlyMovement = 0;
        int TempYear = 0;
        long diffMinutes = 0;
        long tempCounts_Ch1 = 0;
        long tempCounts_Ch2 = 0;
        long hourCounter = 0;
        int avgCounter = 0; //this will allow us to correctly calculate average temps, humidities, pressures, etc.

        //Used to determine if a photodiode failure has occurred!
        int consecutiveZeroTally_Ch1 = 0; //This will tally the consecutive number of hourly zero counts on chamber 1
        int consecutiveZeroTally_Ch2 = 0; //This will tally the consecutive number of hourly zero counts on chamber 2
        long rawCh1Counts = 0; //Raw, unlimited chamber 1 counts. Introduced in v1.0.0.
        long rawCh2Counts = 0; //Raw, unlimited chamber 2 counts. Introduced in v1.0.0.
        long rawTempCounts_Ch1 = 0;
        long rawTempCounts_Ch2 = 0;
        boolean rawCountsExist = false;

        //Needed for building HourlyReconData arraylist...
        ArrayList<String> arrLine = new ArrayList<>();
        ArrayList<String> arrLine_temp = new ArrayList<>();

        chartdataRadon = new ArrayList<Entry>();
        chartdataTemp = new ArrayList<Entry>();
        chartdataPressure = new ArrayList<Entry>();
        chartdataHumidity = new ArrayList<Entry>();
        chartdataTilts = new ArrayList<Entry>();

        for(int arrayCounter = 0; arrayCounter < LoadedReconTXTFile.size(); arrayCounter++) {
            if(LoadedReconTXTFile.get(arrayCounter).get(2).equals("S")||(LoadedReconTXTFile.get(arrayCounter).get(2).equals("I"))||(LoadedReconTXTFile.get(arrayCounter).get(2).equals("E"))) { //Only build data from S, I, and E flags.

                if (LoadedReconTXTFile.get(arrayCounter).get(2).equals("S")) { //Make sure we assign the hour counter to the first record.
                    TempYear = 2000 + Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(3));
                    HourCounter = LocalDateTime.of(TempYear,
                            Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(4)),
                            Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(5)),
                            Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(6)),
                            Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(7)),
                            Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(8)));
                }

                Ch1Counts = Long.parseLong(LoadedReconTXTFile.get(arrayCounter).get(10)); //pull Chamber #1 counts from LoadedReconTXTFile ArrayList
                Ch2Counts = Long.parseLong(LoadedReconTXTFile.get(arrayCounter).get(11)); //pull Chamber #2 counts from LoadedReconTXTFile ArrayList

                hourlyAvgHumidity = hourlyAvgHumidity + Double.parseDouble(LoadedReconTXTFile.get(arrayCounter).get(15)); //pull average humidity...
                hourlyAvgTemp = hourlyAvgTemp + Double.parseDouble(LoadedReconTXTFile.get(arrayCounter).get(21)); //pull average temperature (Celsius units)...
                hourlyAvgPress = hourlyAvgPress + Double.parseDouble(LoadedReconTXTFile.get(arrayCounter).get(18)); //pull average barometric pressure (mbar units)...
                hourlyMovement = hourlyMovement + Long.parseLong(LoadedReconTXTFile.get(arrayCounter).get(9)); //pull movements from ArrayList...

                avgCounter++; //we need this in order to calculate hourly averages for humidity, temperature, pressure, etc.

                //Add to our temporary chamber counters, which will be reset hourly.
                tempCounts_Ch1 = tempCounts_Ch1 + Ch1Counts;
                tempCounts_Ch2 = tempCounts_Ch2 + Ch2Counts;

                //Let's handle the raw counts here... and maintain backwards compatibility with TXT files created before v1.0.0.
                if(LoadedReconTXTFile.get(arrayCounter).size()>=28) {
                    if(rawCountsExist==false) rawCountsExist=true;
                    if((LoadedReconTXTFile.get(arrayCounter).get(26)).contains("false")) { //Check to see if count limiter proc'ed for Ch1...
                        rawCh1Counts = Long.parseLong(LoadedReconTXTFile.get(arrayCounter).get(10)); //If it didn't, then we pull the Ch1 counts from their normal place.
                    } else if((LoadedReconTXTFile.get(arrayCounter).get(26)).contains("true(") && (LoadedReconTXTFile.get(arrayCounter).get(26)).contains(")")) { //If it did, then we need to pull the raw value...
                        rawCh1Counts = Long.parseLong(LoadedReconTXTFile.get(arrayCounter).get(26).replaceAll("[^0-9]", ""));
                    } else {
                        rawCh1Counts = Long.parseLong(LoadedReconTXTFile.get(arrayCounter).get(10));
                    }
                    if((LoadedReconTXTFile.get(arrayCounter).get(27)).contains("false")) { //Check to see if count limiter proc'ed for Ch2...
                        rawCh2Counts = Long.parseLong(LoadedReconTXTFile.get(arrayCounter).get(11)); //If it didn't, then we pull the Ch2 counts from their normal place.
                    } else if((LoadedReconTXTFile.get(arrayCounter).get(27)).contains("true(") && (LoadedReconTXTFile.get(arrayCounter).get(27)).contains(")")) { //If it did, then we need to pull the raw value...
                        rawCh2Counts = Long.parseLong(LoadedReconTXTFile.get(arrayCounter).get(27).replaceAll("[^0-9]", ""));
                    } else {
                        rawCh2Counts = Long.parseLong(LoadedReconTXTFile.get(arrayCounter).get(11));
                    }
                } else {
                    rawCh1Counts = Long.parseLong(LoadedReconTXTFile.get(arrayCounter).get(10));
                    rawCh2Counts = Long.parseLong(LoadedReconTXTFile.get(arrayCounter).get(11));
                }
                rawTempCounts_Ch1 += rawCh1Counts;
                rawTempCounts_Ch2 += rawCh2Counts;

                //Keep an eye out for potential photodiode failure...
                if(Ch1Counts==0) {
                    if(Ch2Counts<5) { //If the other chamber is also measuring low counts, then it's possible we're in an extremely low radon environment...
                        consecutiveZeroTally_Ch1 += .1; //Therefore we should "weight" this encounter less.
                    } else {
                        consecutiveZeroTally_Ch1++; //If the other chamber counts are 5 or greater, then let's count this as a solid clue for potential photodiode failure...
                    }
                    if(consecutiveZeroTally_Ch1>=ConsecutiveZeroLimit && photodiodeFailure_Ch1==false) {
                        if(boolPhotodiodeFailureRecovery==true) {
                            Log.d("CreateGraphArrays", "Potential photodiode failure has been detected in chamber 1. The software will attempt to construct the graph and report using chamber 2.");
                        } else {
                            Log.d("CreateGraphArrays", "Potential photodiode failure has been detected in chamber 1.");
                        }
                        Log.d("CreateGraphsArray","WARNING: Possible photodiode failure detected in chamber 1 when creating graph.");
                        photodiodeFailure_Ch1 = true;
                    }
                } else {
                    consecutiveZeroTally_Ch1 = 0;
                }
                if(Ch2Counts==0) {
                    if(Ch1Counts<5) { //If the other chamber is also measuring low counts, then it's possible we're in an extremely low radon environment...
                        consecutiveZeroTally_Ch2 += .1; //Therefore we should "weight" this encounter less.
                    } else {
                        consecutiveZeroTally_Ch2++; //If the other chamber counts are 5 or greater, then let's count this as a solid clue for potential photodiode failure...
                    }
                    if(consecutiveZeroTally_Ch2>=ConsecutiveZeroLimit && photodiodeFailure_Ch2==false) {
                        if(boolPhotodiodeFailureRecovery==true) {
                            Log.d("CreateGraphArrays", "Potential photodiode failure has been detected in chamber 2. The software will attempt to construct the graph and report using chamber 1.");
                        } else {
                            Log.d("CreateGraphArrays", "Potential photodiode failure has been detected in chamber 2.");
                        }
                        Log.d("CreateGraphArrays","WARNING: Possible photodiode failure detected in chamber 2 when creating graph.");
                        photodiodeFailure_Ch2 = true;
                    }
                } else {
                    consecutiveZeroTally_Ch2 = 0;
                }

                TempYear = 2000 + Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(3));
                ReconDate = LocalDateTime.of(TempYear,
                        Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(4)),
                        Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(5)),
                        Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(6)),
                        Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(7)),
                        Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(8)));

                //logic to be implemented at later time...
                if(HourCounter != null && ReconDate != null) {
                    diffMinutes = ChronoUnit.MINUTES.between(HourCounter, ReconDate);
                    if (diffMinutes >= 60) { //Every time we have more than 60 minutes, let's calculate our hourly radon concentration
                        //Reset Hour Counter and display hourly average
                        TempYear = 2000 + Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(3));
                        HourCounter = LocalDateTime.of(TempYear,
                                Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(4)),
                                Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(5)),
                                Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(6)),
                                Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(7)),
                                Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(8)));

                        //Increase our hour counter (temporary until we figure out x-axis dates)
                        hourCounter++;

                        //Add values to series independent of unitType (i.e. humidity and movement)
                        //AvgHumidity_Series.add(hourCounter, hourlyAvgHumidity / avgCounter); //This will calculate hourly average humidity

                        //Movement / "Tilt" Logic Handling -- now located in TiltSensitivity.java class!
                        //hourlyMovement = TiltSensitivity.main(hourlyMovement);
                        //Movement_Series.add(hourCounter, hourlyMovement);

                        //Add values to series that are dependent upon unitType
                        if (globalUnitType.equals("SI")) {
                            //Ch1_Series.add(hourCounter, tempCounts_Ch1 / LoadedReconCF1 * 37);
                            //Ch2_Series.add(hourCounter, tempCounts_Ch2 / LoadedReconCF2 * 37);
                            //AvgRnC_Series.add(hourCounter, ((tempCounts_Ch1 / LoadedReconCF1 + tempCounts_Ch2 / LoadedReconCF2) / 2) * 37); //This will calculate hourly average of both chambers (in Bq/m3)
                            //AvgTemp_Series.add(hourCounter, (hourlyAvgTemp / avgCounter)); //This will calculate hourly average temperature (in Celsius)
                            //AvgPress_Series.add(hourCounter, (hourlyAvgPress / avgCounter)); //This will calculate hourly average temperature (in mbar)
                            //Ch1_Raw.add(hourCounter, rawTempCounts_Ch1 / LoadedReconCF1 * 37);
                            //Ch2_Raw.add(hourCounter, rawTempCounts_Ch2 / LoadedReconCF2 * 37);

                            //If we are excluding first four hours, let's not add them to TotalAvgRnC
                            if (((TotalHourCounter > 3) && boolExcludeFirst4Hours) || (!boolExcludeFirst4Hours)) {
                                TotalAvgRnC = TotalAvgRnC + (((tempCounts_Ch1 / LoadedReconCF1 + tempCounts_Ch2 / LoadedReconCF2) / 2) * 37); //Overall AvgRnC (in Bq/m3)
                                TotalAvgRnC_Ch1 = TotalAvgRnC_Ch1 + ((tempCounts_Ch1 / LoadedReconCF1) * 37); //Overall AvgRnC for Chamber 1 (in Bq/m3)
                                TotalAvgRnC_Ch2 = TotalAvgRnC_Ch2 + ((tempCounts_Ch2 / LoadedReconCF2) * 37); //Overall AvgRnC for Chamber 2 (in Bq/m3)
                                TotalAvgRnC_Ch1_Raw += ((rawTempCounts_Ch1 / LoadedReconCF1) * 37);
                                TotalAvgRnC_Ch2_Raw += ((rawTempCounts_Ch2 / LoadedReconCF2) * 37);
                            }

                            TotalHourCounter = TotalHourCounter + 1; //Overall Hour Counter

                            //Add to HourlyReconData array, to be used in our PDF (only SI-specific elements to be added)
                            arrLine.add(0, Long.toString(TotalHourCounter)); //Total Hour Counter Index = 0
                            arrLine.add(1, (ReconDate.toString())); //Datetime Index = 1;
                            arrLine.add(2, formatSI_RnC.format(((tempCounts_Ch1 / LoadedReconCF1 + tempCounts_Ch2 / LoadedReconCF2) / 2) * 37)); //Hourly Avg Radon Index = 2
                            arrLine.add(3, formatZero.format(hourlyAvgTemp / avgCounter)); //Hourly Avg Temperature (in Celsius) Index = 3
                            arrLine.add(4, formatTenth.format(hourlyAvgPress / avgCounter)); //Hourly Avg Pressure (in mbar) Index = 4
                            arrLine.add(5, formatZero.format(hourlyAvgHumidity / avgCounter)); //Humidity Index = 5
                            arrLine.add(6, formatZero.format(Math.round(hourlyMovement))); //Movement/Tilt Index = 6
                            arrLine.add(7, (formatUS_RnC.format((tempCounts_Ch1 / LoadedReconCF1 * 37)))); //Hourly Chamber 1 radon concentration Index = 7
                            arrLine.add(8, (formatUS_RnC.format((tempCounts_Ch2 / LoadedReconCF2 * 37)))); //Hourly Chamber 2 radon concentration Index = 8
                            arrLine.add(9, (rawCountsExist ? formatSI_RnC.format((rawTempCounts_Ch1 / LoadedReconCF1) * 37) : formatSI_RnC.format((tempCounts_Ch1 / LoadedReconCF1) * 37))); //Raw Hourly Chamber 1 radon concentration Index = 7
                            arrLine.add(10, (rawCountsExist ? formatSI_RnC.format((rawTempCounts_Ch2 / LoadedReconCF2) * 37) : formatSI_RnC.format((tempCounts_Ch2 / LoadedReconCF2) * 37))); //Raw Hourly Chamber 2 radon concentration Index = 8

                            chartdataRadon.add(new Entry(ReconDate.getHour(),Float.parseFloat(formatSI_RnC.format(((tempCounts_Ch1 / LoadedReconCF1 + tempCounts_Ch2 / LoadedReconCF2) / 2) * 37))));

                            System.out.println(arrLine);

                        } else {
                            //Ch1_Series.add(hourCounter, tempCounts_Ch1 / LoadedReconCF1);
                            //Ch2_Series.add(hourCounter, tempCounts_Ch2 / LoadedReconCF2);
                            //AvgRnC_Series.add(hourCounter, ((tempCounts_Ch1 / LoadedReconCF1 + tempCounts_Ch2 / LoadedReconCF2) / 2)); //This will calculate hourly average of both chambers
                            //AvgTemp_Series.add(hourCounter, (hourlyAvgTemp / avgCounter) * 9 / 5 + 32); //This will calculate hourly average temperature (in Fahrenheit)
                            //AvgPress_Series.add(hourCounter, (hourlyAvgPress / avgCounter) * 0.02952998751); //This will calculate hourly average temperature (in inHg)
                            //Ch1_Raw.add(hourCounter, rawTempCounts_Ch1 / LoadedReconCF1);
                            //Ch2_Raw.add(hourCounter, rawTempCounts_Ch2 / LoadedReconCF2);

                            //If we are excluding first four hours, let's not add them to TotalAvgRnC and TotalHourCounter
                            if (((TotalHourCounter > 3) && boolExcludeFirst4Hours) || (!boolExcludeFirst4Hours)) {
                                TotalAvgRnC = TotalAvgRnC + (((tempCounts_Ch1 / LoadedReconCF1) + (tempCounts_Ch2 / LoadedReconCF2)) / 2); //Overall AvgRnC (in pCi/L)
                                TotalAvgRnC_Ch1 = TotalAvgRnC_Ch1 + ((tempCounts_Ch1 / LoadedReconCF1)); //Overall AvgRnC for Chamber 1 (in pCi/L)
                                TotalAvgRnC_Ch2 = TotalAvgRnC_Ch2 + ((tempCounts_Ch2 / LoadedReconCF2)); //Overall AvgRnC for Chamber 2 (in pCi/L)
                                TotalAvgRnC_Ch1_Raw += ((rawTempCounts_Ch1 / LoadedReconCF1));
                                TotalAvgRnC_Ch2_Raw += ((rawTempCounts_Ch2 / LoadedReconCF2));
                            }

                            TotalHourCounter += 1; //Overall Hour Counter

                            //Add to HourlyReconData array, to be used in our PDF (only US-specific elements to be added)
                            arrLine.add(0, Long.toString(TotalHourCounter)); //Total Hour Counter Index = 0
                            arrLine.add(1, (ReconDate.toString())); //Datetime Index = 1;
                            arrLine.add(2, formatUS_RnC.format((tempCounts_Ch1 / LoadedReconCF1 + tempCounts_Ch2 / LoadedReconCF2) / 2)); //Hourly Avg Radon Index = 2
                            arrLine.add(3, formatZero.format((hourlyAvgTemp / avgCounter) * 9 / 5 + 32)); //Hourly Avg Temperature (in Fahrenheit) Index = 3
                            arrLine.add(4, formatTenth.format((hourlyAvgPress / avgCounter) * 0.02952998751)); //Hourly Avg Pressure (in inHg) Index = 4
                            arrLine.add(5, formatZero.format(hourlyAvgHumidity / avgCounter)); //Humidity Index = 5
                            arrLine.add(6, formatZero.format(Math.round(hourlyMovement))); //Movement/Tilt Index = 6
                            arrLine.add(7, (formatUS_RnC.format((tempCounts_Ch1 / LoadedReconCF1)))); //Hourly Chamber 1 radon concentration Index = 7
                            arrLine.add(8, (formatUS_RnC.format((tempCounts_Ch2 / LoadedReconCF2)))); //Hourly Chamber 2 radon concentration Index = 8
                            arrLine.add(9, (rawCountsExist ? formatUS_RnC.format((rawTempCounts_Ch1 / LoadedReconCF1)) : formatUS_RnC.format((tempCounts_Ch1 / LoadedReconCF1)))); //Raw hourly Chamber 1 radon concentration Index = 9
                            arrLine.add(10, (rawCountsExist ? formatUS_RnC.format((rawTempCounts_Ch2 / LoadedReconCF2)) : formatUS_RnC.format((tempCounts_Ch2 / LoadedReconCF2)))); //Raw Hourly Chamber 2 radon concentration Index = 10

                            chartdataRadon.add(new Entry(ReconDate.getHour(),Float.parseFloat(formatUS_RnC.format((tempCounts_Ch1 / LoadedReconCF1 + tempCounts_Ch2 / LoadedReconCF2) / 2))));

                            System.out.println(arrLine);
                        }

                        //Finalize HourlyReconData line, and add it to the ArrayList
                        arrLine_temp = (ArrayList<String>) arrLine.clone(); //This seems really stupid, but if you don't clone the ArrayList to a temporary holder, it'll be lost after arrLine.clear() below.
                        HourlyReconData.add(arrLine_temp); //This will add the temporary arrLine into the primary HourlyReconData ArrayList.
                        arrLine.clear();

                        //Reset the temporary chamber counts
                        tempCounts_Ch1 = 0;
                        tempCounts_Ch2 = 0;
                        rawTempCounts_Ch1 = 0;
                        rawTempCounts_Ch2 = 0;
                        hourlyAvgHumidity = 0;
                        hourlyAvgTemp = 0;
                        hourlyAvgPress = 0;
                        hourlyMovement = 0;
                        avgCounter = 0; //also reset avgCounter, as we just calculated average humidity, temperature, pressure, etc.
                    }
                }
            }
        }
    }
}
