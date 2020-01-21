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

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import static com.radelec.reconmobile.Constants.INTENT_ACTION_GRANT_USB;
import static com.radelec.reconmobile.Constants.baudRate;
import static com.radelec.reconmobile.Globals.*;
import static com.radelec.reconmobile.SerialSocket.WRITE_WAIT_MILLIS;

public class FragmentConnect extends Fragment implements ConsoleCallback, ServiceConnection, SerialListener {

    private Button btnConnect;
    private Button btnDownload;
    private Button btnClear;
    private TextView txtReconSerial;
    private TextView txtSystemConsole;
    private Space spaceConnect_1;

    private BroadcastReceiver broadcastReceiver;
    private boolean initialStart = true;

    public FragmentConnect() {
        Log.d("FragmentConnect","FragmentConnect() called!");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("FragmentConnect","broadcastReceiver.onReceive() called!");
                if(intent.getAction().equals(INTENT_ACTION_GRANT_USB)) {
                    Log.d("FragmentConnect","Permission Granted! Attempting to call connect()...");
                    Boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                    connect(granted);
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
        txtReconSerial = view.findViewById(R.id.txtReconSerial);
        txtSystemConsole = view.findViewById(R.id.txtConsole);
        spaceConnect_1 = view.findViewById(R.id.spaceConnect_1);

        switch(connected) {
            case True:
                Log.d("FragmentConnect","Setting button to Disconnect [connected = " + connected + "]");
                btnConnect.setText("Disconnect");
                break;
            case False:
                Log.d("FragmentConnect","Setting button to Connect [connected = " + connected + "]");
                btnConnect.setText("Connect");
                break;
            case Pending:
                Log.d("FragmentConnect","Setting button to Wait [connected = " + connected + "]");
                btnConnect.setText("Wait");
                break;
        }
        checkConnectionStatus();

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(connected) {
                    case True:
                        Log.d("FragmentConnect","Disconnect button pressed!");
                        Toast txtOnClick_Disconnect = Toast.makeText(getContext(),"Disconnecting...",Toast.LENGTH_SHORT);
                        txtOnClick_Disconnect.show();
                        ReconFunctions rfRecon = new ReconFunctions(null);
                        rfRecon.disconnect();
                        checkConnectionStatus();
                        break;
                    case False:
                        Log.d("FragmentConnect","Connect button pressed!");
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

        btnDownload.setOnClickListener(v -> {
            if (connected == ReconConnected.True) {
                Log.d("FragmentConnect", "Download button pressed!");
                if(service != null)
                    service.attach(this);
                else
                    getActivity().startService(new Intent(getActivity(), SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
                ReconFunctions rfRecon = new ReconFunctions(null);
                rfRecon.checkNewRecord();
            } else {
                Log.d("FragmentConnect", "Download button pressed, but not connected!?");
            }
        });

        return view;
    }

    @Override
    public void updateSystemConsole(String strConsole) {
        txtSystemConsole.setText(strConsole);
    }

    //Show Device Search popup
    private void showSearchList() {
        Log.d("FragmentConnect", "showSearchList() called!");
        FragmentSearch dialogSearch = new FragmentSearch();
        dialogSearch.setRetainInstance(true);
        dialogSearch.show(getFragmentManager(),"");

        if(dialogSearch.isDetached()) {
            Log.d("FragmentConnect","SEARCH FRAGMENT DETACHED!");
            checkConnectionStatus();
        }
    }

    public void onStart() {
        Log.d("FragmentConnect", "onStart() called!");
        super.onStart();
        if(service != null)
            service.attach(this);
        else
            getActivity().startService(new Intent(getActivity(), SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    @Override
    public void onStop() {
        Log.d("FragmentConnect","onStop() called!");
        if(service != null && !getActivity().isChangingConfigurations())
            service.detach();
        super.onStop();
    }

    public void onPause() {
        Log.d("FragmentConnect", "onPause() called!");
        getActivity().unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    public void onResume() {
        Log.d("FragmentConnect", "onResume() called!");
        super.onResume();
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(INTENT_ACTION_GRANT_USB));
        Log.d("FragmentConnect", "onResume():: initialStart = " + initialStart);
        if(initialStart && service !=null) {
            Log.d("FragmentConnect", "onResume() :: initialStart = true && service != null");
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
        checkConnectionStatus(); //This is needed to retain button configuration, system console, etc. when the screen orientation changes.
    }

    @SuppressWarnings("deprecation") // onAttach(context) was added with API 23. onAttach(activity) works for all API versions
    @Override
    public void onAttach(Activity activity) {
        Log.d("FragmentConnect","onAttach() called!");
        super.onAttach(activity);
        getActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        Log.d("FragmentConnect","onDetach() called!");
        try { getActivity().unbindService(this); } catch(Exception ignored) {}
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        Log.d("FragmentConnect","onDestroy() called!");
        getActivity().stopService(new Intent(getActivity(), SerialService.class));
        super.onDestroy();
    }

    public void checkConnectionStatus() {
        Log.d("FragmentConnect", "checkConnectionStatus() called!");
        switch(connected) {
            case True:
                Log.d("FragmentConnect","Setting button to Disconnect [connected = " + connected + "]");
                btnClear.setVisibility(View.VISIBLE);
                btnConnect.setText("Disconnect");
                btnDownload.setVisibility(View.VISIBLE);
                spaceConnect_1.setVisibility(View.GONE);
                txtReconSerial.setText(String.format("Recon #%s", globalReconSerial));
                txtReconSerial.setVisibility(View.VISIBLE);
                txtSystemConsole.setVisibility(View.VISIBLE);
                break;
            case False:
                Log.d("FragmentConnect","Setting button to Connect [connected = " + connected + "]");
                btnClear.setVisibility(View.GONE);
                btnConnect.setText("Connect");
                btnDownload.setVisibility(View.GONE);
                spaceConnect_1.setVisibility(View.VISIBLE);
                txtReconSerial.setText("No Recon Connected");
                txtReconSerial.setVisibility(View.GONE);
                txtSystemConsole.setVisibility(View.GONE);
                break;
            case Pending: //This should never proc...
                Log.d("FragmentConnect","Pending connection... [connected = " + connected + "]");
                btnClear.setVisibility(View.GONE);
                btnConnect.setText("Disconnect");
                btnDownload.setVisibility(View.GONE);
                spaceConnect_1.setVisibility(View.GONE);
                txtReconSerial.setText("Please Wait...");
                txtReconSerial.setVisibility(View.GONE);
                txtSystemConsole.setVisibility(View.GONE);
                break;
        }
    }

    public void onServiceConnected(ComponentName name, IBinder binder) {
        Log.d("FragmentConnect","onServiceConnected() called!");
        service = ((SerialService.SerialBinder) binder).getService();
        if(initialStart && isResumed()) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d("FragmentConnect","onServiceDisconnected() called!");
        service = null;
    }

    private void connect() {
        connect(null);
    }

    private void connect(Boolean permissionGranted) {
        Log.d("FragmentConnect","connect(" + permissionGranted + ") called!");
        UsbDevice device = null;
        UsbManager usbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
        for(UsbDevice v : usbManager.getDeviceList().values())
            if (v.getDeviceId() == deviceId) {
                device = v;
                Log.d("FragmentConnect","connect(): Scanning Connected Devices = " + v.toString());
            }
        if(device == null) {
            Log.d("FragmentConnect","connect(): Connection Failed / Recon Not Found or No Recon Selected!");
            return;
        }
        Log.d("FragmentConnect","Device = " + device.toString());
        UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device);
        if(driver == null) {
            driver = CustomProber.getCustomProber().probeDevice(device);
        }
        if(driver == null) {
            Log.d("FragmentConnect","connect(): Connection Failed / No Recon Driver Found!");
            return;
        }
        if(driver.getPorts().size() < portNum) {
            Log.d("FragmentConnect","connect(): Connection Failed / No Free Ports!");
            return;
        }
        UsbSerialPort usbSerialPort = driver.getPorts().get(portNum);
        UsbDeviceConnection usbConnection = usbManager.openDevice(driver.getDevice());
        if(usbConnection == null && permissionGranted == null && !usbManager.hasPermission(driver.getDevice())) {
            Log.d("FragmentConnect", "connect(): No Permission Granted -- attempting to request!");
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(getActivity(), 0, new Intent(INTENT_ACTION_GRANT_USB), 0);
            usbManager.requestPermission(driver.getDevice(), usbPermissionIntent);
            return;
        }
        if(usbConnection == null) {
            if (!usbManager.hasPermission(driver.getDevice())) {
                Log.d("FragmentConnect", "connect(): Connection Failed / Permission Denied!");
            } else {
                Log.d("FragmentConnect", "connect(): Connection Failed / Open Failed!");
            }
            return;
        }

        connected = ReconConnected.Pending;
        Log.d("FragmentConnect","connect(): Connection Pending...");
        try {
            Log.d("FragmentConnect","connect(): socket = new SerialSocket();");
            socket = new SerialSocket();
            Log.d("FragmentConnect","connect(): service.connect(this, Connected);");
            service.connect((SerialListener) this, "Connected");
            Log.d("FragmentConnect","connect(): socket.connect(getContext(), service, usbConnection, usbSerialPort, baudRate);");
            Log.d("FragmentConnect","connect(): usbSerialPort = " + usbSerialPort.toString() + " / baudRate = " + baudRate);
            socket.connect(getContext(), service, usbConnection, usbSerialPort, baudRate);
            // usb connect is not asynchronous. connect-success and connect-error are returned immediately from socket.connect
            // for consistency to bluetooth/bluetooth-LE app use same SerialListener and SerialService classes
            onSerialConnect();
            Log.d("FragmentConnect", "WRITE_WAIT_MILLIS = " + WRITE_WAIT_MILLIS);
        } catch (Exception e) {
            onSerialConnectError(e);
            Log.d("FragmentConnect","connect(): Exception!");
        }
    }

    public void onSerialConnect() {
        Log.d("FragmentConnect","onSerialConnect() called!");
        connected = ReconConnected.True;
        Log.d("FragmentConnect","onSerialConnect(): Connected!");
    }

    public void onSerialConnectError(Exception e) {
        Log.d("FragmentConnect","onSerialConnectError() called!");
        Log.d("FragmentConnect", e.toString());
        ReconFunctions rfRecon = new ReconFunctions(null);
        rfRecon.disconnect();
    }

    public void onSerialRead(byte[] data) {
        Log.d("FragmentConnect","onSerialRead() called!");
        receive(data);
        String response = new String(data);
        globalLastResponse = response;
        Log.d("FragmentConnect","Receiving " + response);
        String[] parsedResponse = null;
        parsedResponse = response.split(",");
        if(parsedResponse.length<1) return;
        ReconFunctions rfRecon = new ReconFunctions(this);
        switch(parsedResponse[0]) {
            case "=DB":
                for(int arrayCounter= 0; arrayCounter <= parsedResponse.length -1; arrayCounter++) {
                    arrayDataSession.add(intDataSessionPointer, parsedResponse[arrayCounter]);
                }
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
            case "=RL":
                rfRecon.getCalibrationFactors(response);
                break;
            case "=BD":
                Log.d("ReconFunctions","onSerialRead():: =BD Response from Recon... invalid request?");
                break;
        }
    }

    public void onSerialIoError(Exception e) {
        Log.d("FragmentConnect","onSerialIoError() called!");
        Log.d("FragmentConnect", e.toString());
        ReconFunctions rfRecon = new ReconFunctions(null);
        rfRecon.disconnect();
    }

    private String receive(byte[] data) {
        Log.d("FragmentConnect","receive() called!");
        return (new String(data));
    }

}
