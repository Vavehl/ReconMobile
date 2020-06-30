package com.radelec.reconmobile;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import static com.radelec.reconmobile.Constants.*;
import static com.radelec.reconmobile.Globals.*;

class ReconFunctions {

    private ConsoleCallback consoleCallback = null;

    ReconFunctions(ConsoleCallback callback) {
        this.consoleCallback = callback;
    }

    void checkNewRecord() {
        Log.d("ReconFunctions","checkNewRecord() called!");
        intDataSessionPointer = 0;
        boolRecordHeaderFound = false;
        boolRecordTrailerFound = false;
        arrayDataSession.clear();
        Log.d("ReconFunctions","boolRecordHeaderFound=" + boolRecordHeaderFound + " / boolRecordTrailerFound=" + boolRecordTrailerFound);
        send(cmdCheckNewRecord);
    }

    void clearCurrentSession() {
        Log.d("ReconFunctions","clearCurrentSession() called!");
        String strSystemConsole;
        send(cmdClearSession);
        strSystemConsole = "Clearing current session...";
        if(consoleCallback != null) {
            consoleCallback.updateSystemConsole(strSystemConsole);
        }
    }

    void downloadDataSession(String response) {
        Log.d("ReconFunctions","downloadDataSession() called!");
        String strSystemConsole = "System Console";
        String[] parsedResponse = null;
        parsedResponse = response.split(",");
        if(parsedResponse.length<15) {
            Log.d("ReconFunctions","downloadDataSession: Session point at null record. Aborting download.");
            return;
        } else if(!parsedResponse[0].equals("=DB")) {
            return;
        }
        switch(parsedResponse[2]) {
            case "H":
                if(boolRecordHeaderFound) {
                    Log.d("ReconFunctions","WARNING! MULTIPLE HEADER FILES ENCOUNTERED -- ABORTING DOWNLOAD!");
                    break;
                } else {
                    boolRecordHeaderFound = true;
                    Log.d("ReconFunctions","Header found!");
                    intDataSessionPointer++;
                    send(cmdReadNextRecord);
                    break;
                }
            case "W":
                if(boolRecordHeaderFound) {
                    intDataSessionPointer++;
                    strSystemConsole = "Downloading Record #" + intDataSessionPointer;
                    send(cmdReadNextRecord);
                    break;
                } else {
                    Log.d("ReconFunctions","WARNING! WAIT RECORD FOUND, BUT HEADER FILE NOT FOUND -- ABORTING DOWNLOAD!");
                    break;
                }
            case "S":
                if(boolRecordHeaderFound) {
                    intDataSessionPointer++;
                    strSystemConsole = "Downloading Record #" + intDataSessionPointer;
                    send(cmdReadNextRecord);
                    break;
                } else {
                    Log.d("ReconFunctions","WARNING! START RECORD FOUND, BUT HEADER FILE NOT FOUND -- ABORTING DOWNLOAD!");
                    break;
                }
            case "I":
                if(boolRecordHeaderFound) {
                    intDataSessionPointer++;
                    strSystemConsole = "Downloading Record #" + intDataSessionPointer;
                    send(cmdReadNextRecord);
                    break;
                } else {
                    Log.d("ReconFunctions","WARNING! INTERIM RECORD FOUND, BUT HEADER FILE NOT FOUND -- ABORTING DOWNLOAD!");
                    break;
                }
            case "E":
                if(boolRecordHeaderFound) {
                    intDataSessionPointer++;
                    strSystemConsole = "Downloading Record #" + intDataSessionPointer;
                    send(cmdReadNextRecord);
                    break;
                } else {
                    Log.d("ReconFunctions","WARNING! END RECORD FOUND, BUT HEADER FILE NOT FOUND -- ABORTING DOWNLOAD!");
                    break;
                }
            case "Z":
                boolRecordTrailerFound = true;
                if(boolRecordHeaderFound) {
                    Log.d("ReconFunctions","Record Trailer found! Data session downloaded.");
                    Log.d("ReconFunctions","Record Length (intDataSessionPointer) = " + intDataSessionPointer);
                    strSystemConsole = "Download Success!";
                    CreateTXT.main();
                    break;
                } else {
                    Log.d("ReconFunctions","WARNING! RECORD TRAILER FOUND, BUT NO RECORD HEADER FOUND.");
                }
            default:
                break;
        }
        if(consoleCallback != null) {
            consoleCallback.updateSystemConsole(strSystemConsole);
        }
    }

    void getCalibrationFactors(String response) {
        Log.d("ReconFunctions","getCalibrationFactors() called!");
        Date reconDateTime = Calendar.getInstance().getTime();
        String[] parsedResponse = null;
        parsedResponse = response.split(",");
        if(parsedResponse[0].equals("=RL") && parsedResponse.length==9 && connected== Globals.ReconConnected.True) {
            if(!parsedResponse[1].trim().isEmpty()) {
                try {
                    globalReconCF1 = Double.parseDouble(parsedResponse[1].trim())/1000;
                } catch (NumberFormatException ex) {
                    Log.d("ReconFunctions","Unable to parse Recon CF1 as a double! Reverting to default CF1...");
                    globalReconCF1 = 6;
                }
                Log.d("ReconFunctions","Recon CF1 = " + globalReconCF1);
            }
            if(!parsedResponse[2].trim().isEmpty()) {
                try {
                    globalReconCF2 = Double.parseDouble(parsedResponse[2].trim())/1000;
                } catch (NumberFormatException ex) {
                    Log.d("ReconFunctions","Unable to parse Recon CF2 as a double! Reverting to default CF2...");
                    globalReconCF2 = 6;
                }
                Log.d("ReconFunctions","Recon CF2 = " + globalReconCF2);
            }

            String strYear_Recon = parsedResponse[3];
            String strMonth_Recon = parsedResponse[4];
            String strDay_Recon = parsedResponse[5];
            if(strMonth_Recon.length()==1) {
                strMonth_Recon = "0" + strMonth_Recon;
            }
            if(strDay_Recon.length()==1) {
                strDay_Recon = "0" + strDay_Recon;
            }
            SimpleDateFormat sdfMonthParse = new SimpleDateFormat("MM");
            SimpleDateFormat sdfMonthDisplay = new SimpleDateFormat("MMM");
            try {
                strMonth_Recon = sdfMonthDisplay.format(sdfMonthParse.parse(strMonth_Recon));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String strCalibrationDate = strDay_Recon + "-" + strMonth_Recon + "-20" + strYear_Recon;
            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
            Calendar calendar = Calendar.getInstance();
            try {
                calendar.setTime(Objects.requireNonNull(df.parse(strCalibrationDate)));
                strCalibrationDate = df.format(calendar.getTime());
                globalReconCalibrationDate = strCalibrationDate;
                Log.d("ReconFunctions","Recon Calibration Date = " + globalReconCalibrationDate);
            } catch (ParseException e) {
                Log.d("ReconFunctions","Unable to parse Recon calibration date!");
                e.printStackTrace();
            }
        }
    }

    void getDataSessions(String response) {
        String[] parsedResponse = null;
        boolean boolUnexpectedResponse = true;
        Log.d("ReconFunctions","getDataSessions() called!");
        if(connected == Globals.ReconConnected.True) {
            Log.d("ReconFunctions", "getDataSessions():: LastResponse = " + response);
            if(response != null) {
                parsedResponse = response.split(",");
                if(parsedResponse.length==4) {
                    if (parsedResponse[0].equals("=DP")) {
                        if(!parsedResponse[3].trim().isEmpty()) {
                            globalDataSessions = parsedResponse[3].trim();
                            boolUnexpectedResponse = false;
                            if(consoleCallback != null) {
                                consoleCallback.updateSystemConsole("# of Data Sessions: " + globalDataSessions);
                            }
                            Log.d("ReconFunctions","getDataSessions() Data Sessions on Recon = " + globalDataSessions);
                        }
                    }
                }
            }
            if(boolUnexpectedResponse) {
                Log.d("ReconFunctions","getDataSessions() Unexpected Response from Recon! [" + response + "]");
            }
        } else {
            Log.d("ReconFunctions", "getDataSessions():: Not Connected to Recon!");
        }
    }

    void getSerialAndFirmware(String response, View view) {
        boolean boolReconConnected = false;
        String[] parsedResponse = null;
        Log.d("ReconFunctions","getSerialAndFirmware() called!");
        if(connected == Globals.ReconConnected.True) {
            Log.d("ReconFunctions", "getSerialAndFirmware():: LastResponse = " + response);
            if(response != null) {
                parsedResponse = response.split(",");
                if(parsedResponse.length==4) {
                    boolReconConnected = parsedResponse[0].equals("=DV") && parsedResponse[1].equals("CRM");
                } else {
                    boolReconConnected = false;
                }
            }
        } else {
            Log.d("ReconFunctions", "getSerialAndFirmware():: Not Connected to Recon!");
        }
        TextView ReconSerial = view.findViewById(R.id.txtFoundRecon_Serial);
        TextView ReconFirmware = view.findViewById(R.id.txtFoundRecon_Firmware);
        if(boolReconConnected) {
            globalReconSerial = parsedResponse[3];
            globalReconFirmwareRevision = Double.parseDouble(parsedResponse[2]);
            ReconSerial.setText("Rad Elec Recon #" + (globalReconSerial != null ? globalReconSerial : "??"));
            ReconFirmware.setText("Firmware v" + globalReconFirmwareRevision);
        } else {
            globalReconSerial = "";
            globalReconFirmwareRevision = 0;
        }
    }

    void SyncDateTime(String response) {
        Log.d("ReconFunctions","SyncDateTime() called!");
        String[] parsedResponse = null;
        parsedResponse = response.split(",");
        if(parsedResponse[0].equals("=DT") && parsedResponse.length==7 && connected== Globals.ReconConnected.True) {

            boolean boolSyncSuccessful = false;
            Date currentDateTime = Calendar.getInstance().getTime();
            Date reconDateTime = Calendar.getInstance().getTime(); //Temporarily initialize Recon to current datetime.
            DateFormat.format("yy,MM,dd,HH,mm,ss",currentDateTime);
            DateFormat.format("yy,MM,dd,HH,mm,ss",reconDateTime);
            SimpleDateFormat formatReconDateTime = new SimpleDateFormat("yy,MM,dd,HH,mm,ss");

            String strMonth_Phone = (String) DateFormat.format("MM", currentDateTime);
            String strDay_Phone = (String) DateFormat.format("dd", currentDateTime);
            String strYear_Phone = (String) DateFormat.format("yy", currentDateTime);
            String strHour_Phone = (String) DateFormat.format("HH", currentDateTime);
            String strMinute_Phone = (String) DateFormat.format("mm", currentDateTime);
            String strSecond_Phone = (String) DateFormat.format("ss", currentDateTime);

            String strYear_Recon = parsedResponse[1];
            String strMonth_Recon = parsedResponse[2];
            String strDay_Recon = parsedResponse[3];
            String strHour_Recon = parsedResponse[4];
            String strMinute_Recon = parsedResponse[5];
            String strSecond_Recon = parsedResponse[6];
            String strReconDateTime = strYear_Recon + "," + strMonth_Recon + "," + strDay_Recon + "," + strHour_Recon + "," + strMinute_Recon + "," + strSecond_Recon;
            try {
                reconDateTime = formatReconDateTime.parse(strReconDateTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Log.d("ReconFunctions","Current DateTime = " + currentDateTime.toString());
            if (reconDateTime != null) {
                long diffSeconds = Math.abs((reconDateTime.getTime() - currentDateTime.getTime())/1000);
                Log.d("ReconFunctions","Recon DateTime = " + reconDateTime.toString());
                Log.d("ReconFunctions", "Difference between two times = " + diffSeconds + " seconds.");
                if(diffSeconds>10) {
                    Log.d("ReconFunctions","Difference between time exceeds threshold of 10 seconds. Issuing :WT command to synchronize Recon with phone...");
                    String strWriteNewDateTime = ":WT," + strYear_Phone + "," + strMonth_Phone + "," + strDay_Phone + "," + strHour_Phone + ","+ strMinute_Phone + "," + strSecond_Phone;
                    synchronized(socket) {
                        send(strWriteNewDateTime);
                    }
                }
            } else {
                Log.d("ReconFunctions","Unable to parse Recon DateTime!");
            }

        } else {
            Log.d("ReconFunctions","Unexpected instrument response in SyncDateTime(). Synchronization not performed!");
        }
    }

    void send(String str) {
        Log.d("ReconFunctions","send() called!");
        if(connected != Globals.ReconConnected.True) {
            Log.d("ReconFunctions","send() was called, but no Recon is connected!");
            return;
        }
        try {
            synchronized(socket) {
                socket.wait(50);
                byte[] data = (str + newline).getBytes();
                socket.write(data);
                socket.wait(50);
                globalLastWrite = str;
                Log.d("ReconFunctions", "Writing " + str + " " + Arrays.toString(data));
            }
        } catch (Exception e) {
            onSerialIoError(e);
        }
    }

    void disconnect() {
        Log.d("ReconFunctions","disconnect() called!");
        connected = Globals.ReconConnected.False;
        if(service != null) service.disconnect();
        if(socket != null) socket.disconnect();
        service = null;
        socket = null;
        globalReconSerial = "";
        globalReconFirmwareRevision = 0;
        globalReconCalibrationDate = null;
        initialStart = false;
        Log.d("ReconFunctions","[connected = " + connected + "]");
    }

    private void onSerialIoError(Exception e) {
        Log.d("ReconFunctions","onSerialIoError() called!");
        Log.d("ReconFunctions", e.toString());
        disconnect();
    }
}
