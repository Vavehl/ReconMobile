package com.radelec.reconmobile;

import com.hoho.android.usbserial.driver.CdcAcmSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.UsbSerialProber;

class CustomProber {

    static UsbSerialProber getCustomProber() {
        ProbeTable tblRecon = new ProbeTable();
        tblRecon.addProduct(0x15A2,0x8143,CdcAcmSerialDriver.class);
        return new UsbSerialProber(tblRecon);
    }

}
