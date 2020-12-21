package com.radelec.reconmobile;

import android.text.format.DateFormat;
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

    private final ConsoleCallback consoleCallback;

    ReconFunctions(ConsoleCallback callback) {
        this.consoleCallback = callback;
    }

    void checkNewRecord() {
        Logging.main("ReconFunctions","checkNewRecord() called!");
        intDataSessionPointer = 0;
        boolRecordHeaderFound = false;
        boolRecordTrailerFound = false;
        arrayDataSession.clear();
        Logging.main("ReconFunctions","boolRecordHeaderFound=" + boolRecordHeaderFound + " / boolRecordTrailerFound=" + boolRecordTrailerFound);
        send(cmdCheckNewRecord);
    }

    void clearCurrentSession() {
        Logging.main("ReconFunctions","clearCurrentSession() called!");
        String strSystemConsole;
        send(cmdClearSession);
        strSystemConsole = "Clearing current session...";
        if(consoleCallback != null) {
            consoleCallback.updateSystemConsole(strSystemConsole);
        } else {
            Logging.main("ReconFunctions","consoleCallback() is null.");
            globalLastSystemConsole = strSystemConsole;
        }
    }

    void downloadDataSession(String response) {
        Logging.main("ReconFunctions","downloadDataSession() called!");
        String strSystemConsole = "System Console";
        String[] parsedResponse;
        parsedResponse = response.split(",");
        if(parsedResponse.length<15) {
            Logging.main("ReconFunctions","downloadDataSession: Session point at null record. Aborting download.");
            return;
        } else if(!parsedResponse[0].equals("=DB")) {
            return;
        }
        switch(parsedResponse[2]) {
            case "H":
                if(boolRecordHeaderFound) {
                    Logging.main("ReconFunctions","WARNING! MULTIPLE HEADER FILES ENCOUNTERED -- ABORTING DOWNLOAD!");
                } else {
                    boolRecordHeaderFound = true;
                    Logging.main("ReconFunctions","Header found!");
                    intDataSessionPointer++;
                    send(cmdReadNextRecord);
                }
                break;
            case "W":
                if(boolRecordHeaderFound) {
                    intDataSessionPointer++;
                    strSystemConsole = "Downloading Record #" + intDataSessionPointer;
                    send(cmdReadNextRecord);
                } else {
                    Logging.main("ReconFunctions","WARNING! WAIT RECORD FOUND, BUT HEADER FILE NOT FOUND -- ABORTING DOWNLOAD!");
                }
                break;
            case "S":
                if(boolRecordHeaderFound) {
                    intDataSessionPointer++;
                    strSystemConsole = "Downloading Record #" + intDataSessionPointer;
                    send(cmdReadNextRecord);
                } else {
                    Logging.main("ReconFunctions","WARNING! START RECORD FOUND, BUT HEADER FILE NOT FOUND -- ABORTING DOWNLOAD!");
                }
                break;
            case "I":
                if(boolRecordHeaderFound) {
                    intDataSessionPointer++;
                    strSystemConsole = "Downloading Record #" + intDataSessionPointer;
                    send(cmdReadNextRecord);
                } else {
                    Logging.main("ReconFunctions","WARNING! INTERIM RECORD FOUND, BUT HEADER FILE NOT FOUND -- ABORTING DOWNLOAD!");
                }
                break;
            case "E":
                if(boolRecordHeaderFound) {
                    intDataSessionPointer++;
                    strSystemConsole = "Downloading Record #" + intDataSessionPointer;
                    send(cmdReadNextRecord);
                } else {
                    Logging.main("ReconFunctions","WARNING! END RECORD FOUND, BUT HEADER FILE NOT FOUND -- ABORTING DOWNLOAD!");
                }
                break;
            case "Z":
                boolRecordTrailerFound = true;
                if(boolRecordHeaderFound) {
                    Logging.main("ReconFunctions","Record Trailer found! Data session downloaded.");
                    Logging.main("ReconFunctions","Record Length (intDataSessionPointer) = " + intDataSessionPointer);
                    strSystemConsole = "Download Success!";
                    CreateTXT.main();
                } else {
                    Logging.main("ReconFunctions","WARNING! RECORD TRAILER FOUND, BUT NO RECORD HEADER FOUND.");
                }
                break;
            default:
                break;
        }
        if(consoleCallback != null) {
            consoleCallback.updateSystemConsole(strSystemConsole);
        } else {
            Logging.main("ReconFunctions","consoleCallback is null, so system console won't be updated.");
            globalLastSystemConsole = strSystemConsole;
        }
    }

    void getCalibrationFactors(String response) {
        Logging.main("ReconFunctions","getCalibrationFactors() called!");
        Calendar.getInstance().getTime(); //Do we need this call?
        String[] parsedResponse;
        parsedResponse = response.split(",");
        if(parsedResponse[0].equals("=RL") && parsedResponse.length==9 && connected== Globals.ReconConnected.True) {
            if(!parsedResponse[1].trim().isEmpty()) {
                try {
                    globalReconCF1 = Double.parseDouble(parsedResponse[1].trim())/1000;
                } catch (NumberFormatException ex) {
                    Logging.main("ReconFunctions","Unable to parse Recon CF1 as a double! Reverting to default CF1...");
                    globalReconCF1 = 6;
                }
                Logging.main("ReconFunctions","Recon CF1 = " + globalReconCF1);
            }
            if(!parsedResponse[2].trim().isEmpty()) {
                try {
                    globalReconCF2 = Double.parseDouble(parsedResponse[2].trim())/1000;
                } catch (NumberFormatException ex) {
                    Logging.main("ReconFunctions","Unable to parse Recon CF2 as a double! Reverting to default CF2...");
                    globalReconCF2 = 6;
                }
                Logging.main("ReconFunctions","Recon CF2 = " + globalReconCF2);
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
                Logging.main("ReconFunctions","Recon Calibration Date = " + globalReconCalibrationDate);
            } catch (ParseException e) {
                Logging.main("ReconFunctions","Unable to parse Recon calibration date!");
                e.printStackTrace();
            }
        }
    }

    void getDataSessions(String response) {
        String[] parsedResponse;
        boolean boolUnexpectedResponse = true;
        Logging.main("ReconFunctions","getDataSessions() called!");
        if(connected == Globals.ReconConnected.True) {
            Logging.main("ReconFunctions", "getDataSessions():: LastResponse = " + response);
            if(response != null) {
                parsedResponse = response.split(",");
                if(parsedResponse.length==4) {
                    if (parsedResponse[0].equals("=DP")) {
                        if(!parsedResponse[3].trim().isEmpty()) {
                            globalDataSessions = parsedResponse[3].trim();
                            boolUnexpectedResponse = false;
                            if(consoleCallback != null) {
                                consoleCallback.updateSystemConsole("# of Data Sets: " + globalDataSessions);
                            } else {
                                Logging.main("ReconFunctions","consoleCallback is null; not able to update the system console with data sessions.");
                                globalLastSystemConsole = "# of Data Sets: " + globalDataSessions;
                            }
                            Logging.main("ReconFunctions","getDataSessions() Data Sessions on Recon = " + globalDataSessions);
                        }
                    }
                }
            }
            if(boolUnexpectedResponse) {
                Logging.main("ReconFunctions","getDataSessions() Unexpected Response from Recon! [" + response + "]");
            }
        } else {
            Logging.main("ReconFunctions", "getDataSessions():: Not Connected to Recon!");
        }
    }

    void getSerialAndFirmware(String response, View view) {
        boolean boolReconConnected = false;
        String[] parsedResponse = null;
        Logging.main("ReconFunctions","getSerialAndFirmware() called!");
        if(connected == Globals.ReconConnected.True) {
            Logging.main("ReconFunctions", "getSerialAndFirmware():: LastResponse = " + response);
            if(response != null) {
                parsedResponse = response.split(",");
                if(parsedResponse.length==4) {
                    boolReconConnected = parsedResponse[0].equals("=DV") && parsedResponse[1].equals("CRM");
                } else {
                    boolReconConnected = false;
                }
            }
        } else {
            Logging.main("ReconFunctions", "getSerialAndFirmware():: Not Connected to Recon!");
        }
        TextView ReconSerial = view.findViewById(R.id.txtFoundRecon_Serial);
        TextView ReconFirmware = view.findViewById(R.id.txtFoundRecon_Firmware);
        if(boolReconConnected) {
            globalReconSerial = parsedResponse[3];
            globalReconFirmwareRevision = Double.parseDouble(parsedResponse[2]);
            try {
                ReconSerial.setText(String.format("Rad Elec Recon #%s", globalReconSerial != null ? globalReconSerial : "??"));
                ReconFirmware.setText(String.format("Firmware v%s", globalReconFirmwareRevision));
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
        } else {
            globalReconSerial = "";
            globalReconFirmwareRevision = 0;
        }
    }

    void SyncDateTime(String response) {
        Logging.main("ReconFunctions","SyncDateTime() called!");
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
            Logging.main("ReconFunctions","Current DateTime = " + currentDateTime.toString());
            if (reconDateTime != null) {
                long diffSeconds = Math.abs((reconDateTime.getTime() - currentDateTime.getTime())/1000);
                Logging.main("ReconFunctions","Recon DateTime = " + reconDateTime.toString());
                Logging.main("ReconFunctions", "Difference between two times = " + diffSeconds + " seconds.");
                if(diffSeconds>10) {
                    Logging.main("ReconFunctions","Difference between time exceeds threshold of 10 seconds. Issuing :WT command to synchronize Recon with phone...");
                    String strWriteNewDateTime = ":WT," + strYear_Phone + "," + strMonth_Phone + "," + strDay_Phone + "," + strHour_Phone + ","+ strMinute_Phone + "," + strSecond_Phone;
                    synchronized(socket) {
                        send(strWriteNewDateTime);
                    }
                }
            } else {
                Logging.main("ReconFunctions","Unable to parse Recon DateTime!");
            }

        } else {
            Logging.main("ReconFunctions","Unexpected instrument response in SyncDateTime(). Synchronization not performed!");
        }
    }

    void send(String str) {
        Logging.main("ReconFunctions","send() called!");
        if(connected != Globals.ReconConnected.True) {
            Logging.main("ReconFunctions","send() was called, but no Recon is connected!");
            return;
        }
        try {
            synchronized(socket) {
                socket.wait(50);
                byte[] data = (str + newline).getBytes();
                socket.write(data);
                socket.wait(50);
                globalLastWrite = str;
                Logging.main("ReconFunctions", "Writing " + str + " " + Arrays.toString(data));
            }
        } catch (Exception e) {
            onSerialIoError(e);
        }
    }

    void disconnect() {
        Logging.main("ReconFunctions","disconnect() called!");
        connected = Globals.ReconConnected.False;
        if(service != null) service.disconnect();
        if(socket != null) socket.disconnect();
        service = null;
        socket = null;
        globalReconSerial = "";
        globalReconFirmwareRevision = 0;
        globalReconCalibrationDate = null;
        initialStart = false;
        Logging.main("ReconFunctions","[connected = " + connected + "]");
    }

    private void onSerialIoError(Exception e) {
        Logging.main("ReconFunctions","onSerialIoError() called!");
        Logging.main("ReconFunctions", e.toString());
        disconnect();
    }
}
