package com.example.reconmobile;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.ListFragment;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.util.ArrayList;
import java.util.Locale;

public class ReconSearchList extends ListFragment {

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ReconSearchList","ReconSearchList.onCreate() called!");
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
                        ReconSerial.setText(item.driver.getClass().getSimpleName().replace("CdcAcmSerialDriver", "Rad Elec Recon"));
                        ReconFirmware.setText(String.format(Locale.US, "Vendor %04X, Product %04X", item.device.getVendorId(), item.device.getProductId()));
                    }
                } else { //...an unexpected port. Will this proc on nearby Bluetooth devices?
                    if((item.device.getVendorId()==0x15A2) && (item.device.getProductId()==0x8143)){
                        ReconSerial.setText(item.driver.getClass().getSimpleName().replace("CdcAcmSerialDriver", "Rad Elec Recon") + " (Port " + item.port + ")");
                        ReconFirmware.setText(String.format(Locale.US, "Vendor %04X, Product %04X", item.device.getVendorId(), item.device.getProductId()));
                    }
                }
                return view;
            }
        };
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(null);
        setListAdapter(listAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    void refresh() {
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
        Log.d("ReconSearchList","ReconSearchList.onListItemClick() called!");
        Toast.makeText(getActivity(), "Connecting...", Toast.LENGTH_SHORT).show();
        ListItem item = listItems.get(position);
        if(item.driver == null) { //...this should never happen.
            Toast.makeText(getActivity(), "No Recon Driver!", Toast.LENGTH_SHORT).show();
        } else {
            Bundle args = new Bundle();
            args.putInt("device", item.device.getDeviceId());
            args.putInt("port", item.port);
            args.putInt("baud", baudRate);
        }
    }

}
