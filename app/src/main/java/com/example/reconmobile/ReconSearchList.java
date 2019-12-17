package com.example.reconmobile;

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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.ListFragment;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import static com.example.reconmobile.Constants.*;
import static com.example.reconmobile.Globals.*;
import static com.example.reconmobile.SerialSocket.WRITE_WAIT_MILLIS;

public class ReconSearchList extends ListFragment implements ServiceConnection, SerialListener {

    private int deviceId;
    private int portNum;

    public static final String INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";

    private SerialSocket socket;
    private SerialService service;
    private ReconConnected connected = ReconConnected.False;
    private boolean initialStart = true;
    private BroadcastReceiver broadcastReceiver;

    private String newline = "\r\n"; //Should we change this?

    class ListItem {
        UsbDevice device;
        int port;
        UsbSerialDriver driver;

        ListItem(UsbDevice device, int port, UsbSerialDriver driver) {
            this.device = device;
            this.port = port;
            this.driver = driver;
        }
    }

    private ArrayList<ListItem> listItems = new ArrayList<>();
    private ArrayAdapter<ListItem> listAdapter;
    private int baudRate = 9600;

    public ReconSearchList() {
        Log.d("ReconSearchList","ReconSearchList() called!");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("ReconSearchList","broadcastReceiver.onReceive() called!");
                if(intent.getAction().equals(INTENT_ACTION_GRANT_USB)) {
                    Log.d("ReconSearchList","Permission Granted! Attempting to call connect()...");
                    Boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                    connect(granted);
                }
            }
        };
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ReconSearchList","onCreate() called!");
        Log.d("ReconSearchList", "onCreate():: initialStart = " + initialStart);

        listAdapter = new ArrayAdapter<ListItem>(getActivity(), 0, listItems) {
            @Override
            public View getView(int position, View view, ViewGroup parent) {

                ListItem item = listItems.get(position);

                if (view == null) {
                    view = getActivity().getLayoutInflater().inflate(R.layout.device_list_item, parent, false);
                }

                TextView ReconSerial = view.findViewById(R.id.txtFoundRecon_Serial);
                TextView ReconFirmware = view.findViewById(R.id.txtFoundRecon_Firmware);

                if(item.driver == null)
                    ReconSerial.setText("<Device Not Recognized>");
                else if(item.driver.getPorts().size() == 1) {
                    //Here we need to issue a command to pull the serial number.
                    if((item.device.getVendorId()==0x15A2) && (item.device.getProductId()==0x8143)){
                        Log.d("ReconSearchList","onCreate():: Broadcasting / tethered device meeting initial parameters found!");
                        if(connected == ReconConnected.True) {
                            ReconSerial.setText(item.driver.getClass().getSimpleName().replace("CdcAcmSerialDriver", "Rad Elec Recon #" + globalReconSerial));
                            ReconFirmware.setText(String.format(Locale.US, "Firmware v" + globalReconFirmwareRevision));
                        } else {
                            ReconSerial.setText(item.driver.getClass().getSimpleName().replace("CdcAcmSerialDriver", "Rad Elec Recon"));
                            ReconFirmware.setText(String.format(Locale.US, "Tap to Connect"));
                        }
                    }
                } else { //...an unexpected port. Will this proc on nearby Bluetooth devices?
                    if((item.device.getVendorId()==0x15A2) && (item.device.getProductId()==0x8143)){
                        if(connected == ReconConnected.True) {
                            ReconSerial.setText(item.driver.getClass().getSimpleName().replace("CdcAcmSerialDriver", "Rad Elec Recon #" + globalReconSerial));
                            ReconFirmware.setText(String.format(Locale.US, "Firmware v" + globalReconFirmwareRevision));
                        } else {
                            ReconSerial.setText(item.driver.getClass().getSimpleName().replace("CdcAcmSerialDriver", "Rad Elec Recon"));
                            ReconFirmware.setText(String.format(Locale.US, "Tap to Connect"));
                        }
                    }
                }
                return view;
            }
        };
    }

    @Override
    public void onStart() {
        Log.d("ReconSearchList","onStart() called!");
        super.onStart();
        if(service != null)
            service.attach(this);
        else
            getActivity().startService(new Intent(getActivity(), SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
        refresh();
    }

    @Override
    public void onPause() {
        Log.d("ReconSearchList","onPause() called!");
        getActivity().unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d("ReconSearchList","onStop() called!");
        if(service != null && !getActivity().isChangingConfigurations())
            service.detach();
        super.onStop();
    }

    @SuppressWarnings("deprecation") // onAttach(context) was added with API 23. onAttach(activity) works for all API versions
    @Override
    public void onAttach(Activity activity) {
        Log.d("ReconSearchList","onAttach() called!");
        super.onAttach(activity);
        getActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        Log.d("ReconSearchList","onDetach() called!");
        try { getActivity().unbindService(this); } catch(Exception ignored) {}
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        Log.d("ReconSearchList","onDestroy() called!");
        if (connected != ReconConnected.False)
            disconnect();
        getActivity().stopService(new Intent(getActivity(), SerialService.class));
        super.onDestroy();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d("ReconSearchList","onActivityCreated() called!");
        super.onActivityCreated(savedInstanceState);
        setListAdapter(null);
        setListAdapter(listAdapter);
    }

    @Override
    public void onResume() {
        Log.d("ReconSearchList","onResume() called!");
        super.onResume();
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(INTENT_ACTION_GRANT_USB));
        Log.d("ReconSearchList", "onResume():: initialStart = " + initialStart);
        if(initialStart && service !=null) {
            Log.d("ReconSearchList", "onResume() :: initialStart = true && service != null");
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    void refresh() {
        Log.d("ReconSearchList","refresh() called!");
        UsbManager usbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
        UsbSerialProber usbDefaultProber = UsbSerialProber.getDefaultProber();
        UsbSerialProber usbCustomProber = CustomProber.getCustomProber();
        listItems.clear();
        for(UsbDevice device : usbManager.getDeviceList().values()) {
            UsbSerialDriver driver = usbDefaultProber.probeDevice(device);
            if(driver == null) {
                driver = usbCustomProber.probeDevice(device);
            }
            if(driver != null) {
                for(int port = 0; port < driver.getPorts().size(); port++)
                    listItems.add(new ListItem(device, port, driver));
            } else {
                listItems.add(new ListItem(device, 0, null));
            }
        }
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.d("ReconSearchList","onListItemClick() called!");
        Toast.makeText(getActivity(), "Connecting...", Toast.LENGTH_SHORT).show();
        ListItem item = listItems.get(position);
        if(item.driver == null) { //...this should never happen.
            Toast.makeText(getActivity(), "No Recon Driver!", Toast.LENGTH_SHORT).show();
        } else {
            Bundle args = new Bundle();
            args.putInt("device", item.device.getDeviceId());
            args.putInt("port", item.port);
            args.putInt("baud", baudRate);
            Log.d("ReconSearchList","DeviceId = " + item.device.getDeviceId() + " / Port = " + item.port + " / Baud = " + baudRate);
            this.setArguments(args);
            deviceId = getArguments().getInt("device");
            portNum = getArguments().getInt("port");
            baudRate = getArguments().getInt("baud");

            //This will check permissions...
            UsbManager usbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
            PendingIntent pi = PendingIntent.getBroadcast(getContext(),0,new Intent(INTENT_ACTION_GRANT_USB),0);
            if(!usbManager.hasPermission(item.device)) {
                Log.d("ReconSearchList","Requesting permission to access USB device...");
                usbManager.requestPermission(item.device, pi);
            }

            //If permissions are true, let's pull the serial number and firmware revision.
            if(usbManager.hasPermission(item.device) && connected != ReconConnected.True) {
                Log.d("ReconSearchList","Attempting to Connect... [Current Connected State = " + connected + ")");
                connect(true);
                send(cmdReconConfirm);
            }
        }
    }

    private void send(String str) {
        Log.d("ReconSearchList","send() called!");
        if(connected != ReconConnected.True) {
            Toast.makeText(getActivity(), "No Recon Connection!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            byte[] data = (str + newline).getBytes();
            socket.write(data);
            globalLastWrite = str;
            Log.d("ReconSearchList","Writing " + str + " " + Arrays.toString(data));
        } catch (Exception e) {
            onSerialIoError(e);
        }
    }

    private String receive(byte[] data) {
        Log.d("ReconSearchList","receive() called!");
        return (new String(data));
    }

    private void connect() {
        connect(null);
    }

    private void connect(Boolean permissionGranted) {
        Log.d("ReconSearchList","connect(" + permissionGranted + ") called!");
        UsbDevice device = null;
        UsbManager usbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
        for(UsbDevice v : usbManager.getDeviceList().values())
            if (v.getDeviceId() == deviceId) {
                device = v;
                Log.d("ReconSearchList","connect(): Scanning Connected Devices = " + v.toString());
            }
        if(device == null) {
            Log.d("ReconSearchList","connect(): Connection Failed / Recon Not Found or No Recon Selected!");
            return;
        }
        Log.d("ReconSearchList","Device = " + device.toString());
        UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device);
        if(driver == null) {
            driver = CustomProber.getCustomProber().probeDevice(device);
        }
        if(driver == null) {
            Log.d("ReconSearchList","connect(): Connection Failed / No Recon Driver Found!");
            return;
        }
        if(driver.getPorts().size() < portNum) {
            Log.d("ReconSearchList","connect(): Connection Failed / No Free Ports!");
            return;
        }
        UsbSerialPort usbSerialPort = driver.getPorts().get(portNum);
        UsbDeviceConnection usbConnection = usbManager.openDevice(driver.getDevice());
        if(usbConnection == null && permissionGranted == null && !usbManager.hasPermission(driver.getDevice())) {
            Log.d("ReconSearchList", "connect(): No Permission Granted -- attempting to request!");
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(getActivity(), 0, new Intent(INTENT_ACTION_GRANT_USB), 0);
            usbManager.requestPermission(driver.getDevice(), usbPermissionIntent);
            return;
        }
        if(usbConnection == null) {
            if (!usbManager.hasPermission(driver.getDevice())) {
                Log.d("ReconSearchList", "connect(): Connection Failed / Permission Denied!");
            } else {
                Log.d("ReconSearchList", "connect(): Connection Failed / Open Failed!");
            }
            return;
        }

        connected = ReconConnected.Pending;
        Log.d("ReconSearchList","connect(): Connection Pending...");
        try {
            Log.d("ReconSearchList","connect(): socket = new SerialSocket();");
            socket = new SerialSocket();
            Log.d("ReconSearchList","connect(): service.connect(this, Connected);");
            service.connect((SerialListener) this, "Connected");
            Log.d("ReconSearchList","connect(): socket.connect(getContext(), service, usbConnection, usbSerialPort, baudRate);");
            Log.d("ReconSearchList","connect(): usbSerialPort = " + usbSerialPort.toString() + " / baudRate = " + baudRate);
            socket.connect(getContext(), service, usbConnection, usbSerialPort, baudRate);
            // usb connect is not asynchronous. connect-success and connect-error are returned immediately from socket.connect
            // for consistency to bluetooth/bluetooth-LE app use same SerialListener and SerialService classes
            onSerialConnect();
            Log.d("ReconSearchList", "WRITE_WAIT_MILLIS = " + WRITE_WAIT_MILLIS);
        } catch (Exception e) {
            onSerialConnectError(e);
            Log.d("ReconSearchList","connect(): Exception!");
        }
    }

    public void onSerialConnect() {
        Log.d("ReconSearchList","onSerialConnect() called!");
        connected = ReconConnected.True;
        Log.d("ReconSearchList","onSerialConnect(): Connected!");
    }

    public void onSerialRead(byte[] data) {
        Log.d("ReconSearchList","onSerialRead() called!");
        receive(data);
        String s = new String(data);
        globalLastResponse = s;
        Log.d("ReconSearchList","Receiving " + s);
        switch(globalLastWrite) {
            case ":RV":
                getSerialAndFirmware();
                break;
        }
    }

    private void disconnect() {
        Log.d("ReconSearchList","disconnect() called!");
        connected = ReconConnected.False;
        service.disconnect();
        socket.disconnect();
        socket = null;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        Log.d("ReconSearchList","onServiceConnected() called!");
        service = ((SerialService.SerialBinder) binder).getService();
        if(initialStart && isResumed()) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d("ReconSearchList","onServiceDisconnected() called!");
        service = null;
    }

    public void onSerialIoError(Exception e) {
        Log.d("ReconSearchList","onSerialIoError() called!");
        Log.d("ReconSearchList", e.toString());
        disconnect();
    }

    public void onSerialConnectError(Exception e) {
        Log.d("ReconSearchList","onSerialConnectError() called!");
        Log.d("ReconSearchList", e.toString());
        disconnect();
    }

    private void getSerialAndFirmware() {
        boolean boolReconConnected = false;
        String[] parsedResponse = null;
        Log.d("ReconSearchList","getSerialAndFirmware() called!");
        if(connected == ReconConnected.True) {
            if (!globalLastWrite.equals(cmdReconConfirm)) send(cmdReconConfirm);
            Log.d("ReconSearchList", "getSerialAndFirmware():: LastResponse = " + globalLastResponse);
            if(globalLastResponse != null) {
                parsedResponse = globalLastResponse.split(",");
                if(parsedResponse.length==4) {
                    if (parsedResponse[0].equals("=DV") && parsedResponse[1].equals("CRM")) {
                        boolReconConnected = true;
                    } else {
                        boolReconConnected = false;
                    }
                } else {
                    boolReconConnected = false;
                }
            }
        } else {
            Log.d("ReconSearchList", "getSerialAndFirmware():: Not Connected to Recon!");
        }
        if(boolReconConnected) {
            globalReconSerial = parsedResponse[3];
            globalReconFirmwareRevision = Double.parseDouble(parsedResponse[2]);
        } else {
            globalReconSerial = "";
            globalReconFirmwareRevision = 0;
        }
        View view = getView();
        TextView ReconSerial = view.findViewById(R.id.txtFoundRecon_Serial);
        TextView ReconFirmware = view.findViewById(R.id.txtFoundRecon_Firmware);
        ReconSerial.setText("Rad Elec Recon #" + globalReconSerial);
        ReconFirmware.setText("Firmware v" + globalReconFirmwareRevision);
    }

}