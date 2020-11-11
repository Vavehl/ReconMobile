package com.radelec.reconmobile;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.util.Objects;

import static com.radelec.reconmobile.Constants.INTENT_ACTION_GRANT_USB;
import static com.radelec.reconmobile.Constants.baudRate;
import static com.radelec.reconmobile.Constants.cmdReadProtocol;
import static com.radelec.reconmobile.Globals.*;
import static com.radelec.reconmobile.SerialSocket.WRITE_WAIT_MILLIS;

public class FragmentConnect extends Fragment implements ConsoleCallback, ServiceConnection, SerialListener {

    private Button btnConnect;
    private Button btnDownload;
    private Button btnClear;
    private Button btnCloseFile;
    private TextView txtReconSerial;
    private TextView txtSystemConsole;
    private Space spaceConnect_1;

    private BroadcastReceiver broadcastReceiver;
    private boolean initialStart = true;

    public FragmentConnect() {
        Logging.main("FragmentConnect","FragmentConnect() called!");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Logging.main("FragmentConnect","broadcastReceiver.onReceive() called!");
                if(Objects.requireNonNull(intent.getAction()).equals(INTENT_ACTION_GRANT_USB)) {
                    Boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                    Logging.main("ReconSearchList","Permission Granted? [" + granted + "]");
                    if(granted) connect(granted);
                }
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connect, container, false);

        btnConnect = view.findViewById(R.id.buttonConnect);
        btnDownload = view.findViewById(R.id.buttonDownload);
        btnClear = view.findViewById(R.id.buttonClear);
        btnCloseFile = view.findViewById(R.id.buttonCloseFile);
        txtReconSerial = view.findViewById(R.id.txtReconSerial);
        txtSystemConsole = view.findViewById(R.id.txtConsole);
        spaceConnect_1 = view.findViewById(R.id.spaceConnect_1);

        switch(connected) {
            case True:
                Logging.main("FragmentConnect","Setting button to Disconnect [connected = " + connected + "]");
                btnConnect.setText("Disconnect");
                break;
            case False:
                Logging.main("FragmentConnect","Setting button to Connect [connected = " + connected + "]");
                btnConnect.setText("Connect");
                break;
            case Pending:
                Logging.main("FragmentConnect","Setting button to Wait [connected = " + connected + "]");
                btnConnect.setText("Wait");
                break;
            case Loaded:
                Logging.main("FragmentConnect","Setting status to Loaded File [connected = " + connected + "]");
        }
        checkConnectionStatus();

        btnClear.setOnClickListener(v -> {
            if (connected == ReconConnected.True) {
                Logging.main("FragmentConnect", "Clear Session button pressed!");
                if(service != null)
                    service.attach(this);
                else
                    Objects.requireNonNull(getActivity()).startService(new Intent(getActivity(), SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change

                ReconFunctions rfRecon = new ReconFunctions(this);
                rfRecon.clearCurrentSession();
                rfRecon.send(cmdReadProtocol); //Let's get the number of data sessions by issuing :RP
            } else {
                Logging.main("FragmentConnect", "Clear Session button pressed, but not connected!?");
            }
        });

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(connected) {
                    case True:
                        Logging.main("FragmentConnect","Disconnect button pressed!");
                        Toast txtOnClick_Disconnect = Toast.makeText(getContext(),"Disconnecting...",Toast.LENGTH_SHORT);
                        txtOnClick_Disconnect.show();
                        ReconFunctions rfRecon = new ReconFunctions(null);
                        rfRecon.disconnect();
                        checkConnectionStatus();
                        break;
                    case False:
                        Logging.main("FragmentConnect","Connect button pressed!");
                        Toast txtOnClick_Connect = Toast.makeText(getContext(),"Searching for Recon...",Toast.LENGTH_SHORT);
                        txtOnClick_Connect.show();
                        showSearchList();
                        break;
                    case Pending:
                        Toast txtOnClick_Pending = Toast.makeText(getContext(),"Please wait...",Toast.LENGTH_SHORT);
                        txtOnClick_Pending.show();
                        break;
                }
            }
        });

        btnCloseFile.setOnClickListener(v -> {
            if (connected == ReconConnected.Loaded) {
                Logging.main("FragmentConnect", "Close File button pressed!");
            } else {
                Logging.main("FragmentConnect", "Close File button pressed, but no file was loaded!?");
            }
            Globals.globalLoadedFileName = "";
            Globals.globalReconCF1 = 6;
            Globals.globalReconCF2 = 6;
            connected = ReconConnected.False;
            loadedTestSiteInfo = "";
            loadedCustomerInfo = "";
            loadedReportProtocol = "";
            loadedReportTampering = "";
            loadedReportWeather = "";
            loadedReportMitigation = "";
            loadedReportComment = "";
            loadedLocationDeployed = "";
            loadedDeployedBy = "";
            loadedRetrievedBy = "";
            loadedAnalyzedBy = "";
            loadedCalibrationDate = "";
            loadedEmail = "";
            updateSystemConsole("System Console"); //Restore the default system console text
            checkConnectionStatus();

            TabLayout tabLayout = v.getRootView().findViewById(R.id.tabLayout_Main);
            if (connected == ReconConnected.Loaded) {
                tabLayout.getTabAt(2).setText("Report");
                Logging.main("MainActivity","Setting tab to Report...");
            } else {
                tabLayout.getTabAt(2).setText("Defaults");
                Logging.main("MainActivity","Setting tab to Defaults...");
            }

        });

        btnDownload.setOnClickListener(v -> {
            if (connected == ReconConnected.True) {
                Logging.main("FragmentConnect", "Download button pressed!");
                if(service != null)
                    service.attach(this);
                else
                    Objects.requireNonNull(getActivity()).startService(new Intent(getActivity(), SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change

                // pull Defaults for TXT file
                globalDBDefaults = new DatabaseOperations(getContext());

                ReconFunctions rfRecon = new ReconFunctions(null);
                rfRecon.checkNewRecord();
            } else {
                Logging.main("FragmentConnect", "Download button pressed, but not currently connected.");
            }
        });

        return view;
    }

    @Override
    public void updateSystemConsole(String strConsole) {
        try {
            Logging.main("FragmentConnect", "updateSystemConsole() called!");
            globalLastSystemConsole = strConsole;
            txtSystemConsole.setText(strConsole);
            Logging.main("FragmentConnect", "System Console updated to: " + strConsole);
        } catch (NullPointerException ex) {
            Logging.main("FragmentConnect","Unable to update system console -- it isn't instantiated!");
        }
    }

    //Show Device Search popup
    private void showSearchList() {
        Logging.main("FragmentConnect", "showSearchList() called!");
        FragmentSearch dialogSearch = new FragmentSearch();
        dialogSearch.setRetainInstance(true);
        dialogSearch.show(getFragmentManager(),"");

        if(dialogSearch.isDetached()) {
            Logging.main("FragmentConnect","SEARCH FRAGMENT DETACHED!");
            checkConnectionStatus();
        }
    }

    public void onStart() {
        Logging.main("FragmentConnect", "onStart() called!");
        super.onStart();
        if(service != null)
            service.attach(this);
        else
            Objects.requireNonNull(getActivity()).startService(new Intent(getActivity(), SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    @Override
    public void onStop() {
        Logging.main("FragmentConnect","onStop() called!");
        if(service != null && !Objects.requireNonNull(getActivity()).isChangingConfigurations())
            service.detach();
        super.onStop();
    }

    public void onPause() {
        Logging.main("FragmentConnect", "onPause() called!");
        Objects.requireNonNull(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    public void onResume() {
        Logging.main("FragmentConnect", "onResume() called!");
        super.onResume();
        Objects.requireNonNull(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(INTENT_ACTION_GRANT_USB));
        Logging.main("FragmentConnect", "onResume():: initialStart = " + initialStart);
        if(connected == ReconConnected.True) {
            Logging.main("FragmentConnect","onResume() :: Recon is already connected. Setting initialStart to false...");
            initialStart = false;
        } else if(initialStart && service !=null) {
            Logging.main("FragmentConnect", "onResume() :: initialStart = true && service != null");
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
        checkConnectionStatus(); //This is needed to retain button configuration, system console, etc. when the screen orientation changes.
    }

    @SuppressWarnings("deprecation") // onAttach(context) was added with API 23. onAttach(activity) works for all API versions
    @Override
    public void onAttach(@NonNull Activity activity) {
        Logging.main("FragmentConnect","onAttach() called!");
        super.onAttach(activity);
        Objects.requireNonNull(getActivity()).bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        Logging.main("FragmentConnect","onDetach() called!");
        try { Objects.requireNonNull(getActivity()).unbindService(this); } catch(Exception ignored) {}
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        Logging.main("FragmentConnect","onDestroy() called!");
        Objects.requireNonNull(getActivity()).stopService(new Intent(getActivity(), SerialService.class));
        super.onDestroy();
    }

    public void checkConnectionStatus() {
        Logging.main("FragmentConnect", "checkConnectionStatus() called!");
        switch(connected) {
            case True:
                Logging.main("FragmentConnect","Setting button to Disconnect [connected = " + connected + "]");
                if(btnClear != null) btnClear.setVisibility(View.VISIBLE);
                if(btnConnect != null) btnConnect.setText("Disconnect");
                if(btnDownload != null) btnDownload.setVisibility(View.VISIBLE);
                if(btnConnect != null) btnConnect.setVisibility(View.VISIBLE);
                if(btnCloseFile != null) btnCloseFile.setVisibility(View.GONE);
                if(spaceConnect_1 != null) spaceConnect_1.setVisibility(View.GONE);
                if(txtReconSerial != null) txtReconSerial.setText(String.format("Recon #%s", globalReconSerial));
                if(txtReconSerial != null) txtReconSerial.setVisibility(View.VISIBLE);
                if(txtSystemConsole != null) txtSystemConsole.setVisibility(View.VISIBLE);
                if(txtSystemConsole != null) txtSystemConsole.setText(Globals.globalLastSystemConsole);
                break;
            case False:
                Logging.main("FragmentConnect","Setting button to Connect [connected = " + connected + "]");
                if(btnClear != null) btnClear.setVisibility(View.GONE);
                if(btnConnect != null) btnConnect.setText("Connect");
                if(btnConnect != null) btnConnect.setVisibility(View.VISIBLE);
                if(btnDownload != null) btnDownload.setVisibility(View.GONE);
                if(btnCloseFile != null) btnCloseFile.setVisibility(View.GONE);
                if(spaceConnect_1 != null) spaceConnect_1.setVisibility(View.VISIBLE);
                if(txtReconSerial != null) txtReconSerial.setText("No Recon Connected");
                if(txtReconSerial != null) txtReconSerial.setVisibility(View.GONE);
                if(txtSystemConsole != null) txtSystemConsole.setVisibility(View.GONE);
                break;
            case Pending: //This should never proc...
                Logging.main("FragmentConnect","Pending connection... [connected = " + connected + "]");
                if(btnClear != null) btnClear.setVisibility(View.GONE);
                if(btnConnect != null) btnConnect.setText("Disconnect");
                if(btnConnect != null) btnConnect.setVisibility(View.VISIBLE);
                if(btnDownload != null) btnDownload.setVisibility(View.GONE);
                if(btnCloseFile != null) btnCloseFile.setVisibility(View.GONE);
                if(spaceConnect_1 != null) spaceConnect_1.setVisibility(View.GONE);
                if(txtReconSerial != null) txtReconSerial.setText("Please Wait...");
                if(txtReconSerial != null) txtReconSerial.setVisibility(View.GONE);
                if(txtSystemConsole != null) txtSystemConsole.setVisibility(View.GONE);
                break;
            case Loaded: //This should only be displayed when a file is loaded
                Logging.main("FragmentConnect","Loaded File " + globalLoadedFileName + "... [connected = " + connected + "]");
                if(btnClear != null) btnClear.setVisibility(View.GONE);
                if(btnConnect != null) btnConnect.setVisibility(View.GONE);
                if(btnDownload != null) btnDownload.setVisibility(View.GONE);
                if(btnCloseFile != null) btnCloseFile.setVisibility(View.VISIBLE);
                if(spaceConnect_1 != null) spaceConnect_1.setVisibility(View.VISIBLE);
                if(txtReconSerial != null) txtReconSerial.setText("No Recon Connected");
                if(txtReconSerial != null) txtReconSerial.setVisibility(View.GONE);
                if(txtSystemConsole != null) txtSystemConsole.setText(Globals.globalLoadedFileName);
        }
    }

    public void onServiceConnected(ComponentName name, IBinder binder) {
        Logging.main("FragmentConnect","onServiceConnected() called!");
        service = ((SerialService.SerialBinder) binder).getService();
        if(initialStart && isResumed()) {
            initialStart = false;
            Objects.requireNonNull(getActivity()).runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Logging.main("FragmentConnect","onServiceDisconnected() called!");
        service = null;
    }

    private void connect() {
        connect(null);
    }

    private void connect(Boolean permissionGranted) {
        Logging.main("FragmentConnect","connect(" + permissionGranted + ") called!");
        UsbDevice device = null;
        UsbManager usbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
        for(UsbDevice v : usbManager.getDeviceList().values())
            if (v.getDeviceId() == deviceId) {
                device = v;
                Logging.main("FragmentConnect","connect(): Scanning Connected Devices = " + v.toString());
            }
        if(device == null) {
            Logging.main("FragmentConnect","connect(): Connection Failed / Recon Not Found or No Recon Selected!");
            return;
        }
        Logging.main("FragmentConnect","Device = " + device.toString());
        UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device);
        if(driver == null) {
            driver = CustomProber.getCustomProber().probeDevice(device);
        }
        if(driver == null) {
            Logging.main("FragmentConnect","connect(): Connection Failed / No Recon Driver Found!");
            return;
        }
        if(driver.getPorts().size() < portNum) {
            Logging.main("FragmentConnect","connect(): Connection Failed / No Free Ports!");
            return;
        }
        UsbSerialPort usbSerialPort = driver.getPorts().get(portNum);
        UsbDeviceConnection usbConnection = usbManager.openDevice(driver.getDevice());
        if(usbConnection == null && permissionGranted == null && !usbManager.hasPermission(driver.getDevice())) {
            Logging.main("FragmentConnect", "connect(): No Permission Granted -- attempting to request!");
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(getActivity(), 0, new Intent(INTENT_ACTION_GRANT_USB), 0);
            usbManager.requestPermission(driver.getDevice(), usbPermissionIntent);
            return;
        }
        if(usbConnection == null) {
            if (!usbManager.hasPermission(driver.getDevice())) {
                Logging.main("FragmentConnect", "connect(): Connection Failed / Permission Denied!");
            } else {
                Logging.main("FragmentConnect", "connect(): Connection Failed / Open Failed!");
            }
            return;
        }

        connected = ReconConnected.Pending;
        Logging.main("FragmentConnect","connect(): Connection Pending...");
        try {
            Logging.main("FragmentConnect","connect(): socket = new SerialSocket();");
            socket = new SerialSocket();
            Logging.main("FragmentConnect","connect(): service.connect(this, Connected);");
            service.connect((SerialListener) this, "Connected");
            Logging.main("FragmentConnect","connect(): socket.connect(getContext(), service, usbConnection, usbSerialPort, baudRate);");
            Logging.main("FragmentConnect","connect(): usbSerialPort = " + usbSerialPort.toString() + " / baudRate = " + baudRate);
            socket.connect(getContext(), service, usbConnection, usbSerialPort, baudRate);
            // usb connect is not asynchronous. connect-success and connect-error are returned immediately from socket.connect
            // for consistency to bluetooth/bluetooth-LE app use same SerialListener and SerialService classes
            onSerialConnect();
            Logging.main("FragmentConnect", "WRITE_WAIT_MILLIS = " + WRITE_WAIT_MILLIS);
        } catch (Exception e) {
            onSerialConnectError(e);
            Logging.main("FragmentConnect","connect(): Exception!");
        }
    }

    public void onSerialConnect() {
        Logging.main("FragmentConnect","onSerialConnect() called!");
        connected = ReconConnected.True;
        Logging.main("FragmentConnect","onSerialConnect(): Connected!");
    }

    public void onSerialConnectError(Exception e) {
        Logging.main("FragmentConnect","onSerialConnectError() called!");
        Logging.main("FragmentConnect", e.toString());
        ReconFunctions rfRecon = new ReconFunctions(null);
        rfRecon.disconnect();
    }

    public void onSerialRead(byte[] data) {
        Logging.main("FragmentConnect","onSerialRead() called!");
        receive(data);
        String response = new String(data);
        Logging.main("FragmentConnect","Receiving " + response);
        response = response.replaceAll("[\\n\\r+]", ""); // strip line feeds
        globalLastResponse = response;
        String[] parsedResponse = null;
        parsedResponse = response.split(",");
        if(parsedResponse.length<1) return;
        ReconFunctions rfRecon = new ReconFunctions(this);
        switch(parsedResponse[0]) {
            case "=DB":
                arrayDataSession.add(intDataSessionPointer, parsedResponse);
                if(!boolRecordTrailerFound) rfRecon.downloadDataSession(response);
                break;
            case "=DV":
                rfRecon.getSerialAndFirmware(response,getView());
                break;
            case "=DP":
                rfRecon.getDataSessions(response);
                break;
            case "=DT":
                rfRecon.SyncDateTime(response);
                break;
            case "=OK":
                //Response for :CD clear current session
                Logging.main("FragmentConnect","onSerialRead():: =OK Response from Recon... presumably from :CD command?");
                break;
            case "=RL":
                rfRecon.getCalibrationFactors(response);
                break;
            case "=BD":
                Logging.main("FragmentConnect","onSerialRead():: =BD Response from Recon... invalid request OR zero sessions remaining after :CD?");
                break;
        }
    }

    public void onSerialIoError(Exception e) {
        Logging.main("FragmentConnect","onSerialIoError() called!");
        Logging.main("FragmentConnect", e.toString());
        ReconFunctions rfRecon = new ReconFunctions(null);
        rfRecon.disconnect();
    }

    private String receive(byte[] data) {
        Logging.main("FragmentConnect","receive() called!");
        return (new String(data));
    }

}
