package com.example.usbdevicetest;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> map = usbManager.getDeviceList();
        System.out.println("......................befor....................................");
        for(UsbDevice device : map.values()){
            System.out.println(".......one..........dName: " + device.getDeviceName()+" productName = "+device.getProductName()+" manuName = "
            +device.getManufacturerName()+" serialNum = "+device.getSerialNumber());
            System.out.println(".......tow.........vid: " + device.getVendorId() + "\t pid: " + device.getProductId());
        }
        System.out.println("........................after..................................");
    }

    /*public String getProductName(){
        byte[] rawDescs = mUsbDeviceConnection.getRawDescriptors();
        String manufacturer = "", product = "";
        try
        {
            byte[] buffer = new byte[255];
            int idxMan = rawDescs[14];
            int idxPrd = rawDescs[15];
            Log.i("index",idxMan+"");

            int rdo = mUsbDeviceConnection.controlTransfer(UsbConstants.USB_DIR_IN
                            | UsbConstants.USB_TYPE_STANDARD, STD_USB_REQUEST_GET_DESCRIPTOR,
                    (LIBUSB_DT_STRING << 8) | idxMan, 0, buffer, 0xFF, 0);
            manufacturer = new String(buffer, 2, rdo - 2, "UTF-16LE");

            rdo = mUsbDeviceConnection.controlTransfer(UsbConstants.USB_DIR_IN
                            | UsbConstants.USB_TYPE_STANDARD, STD_USB_REQUEST_GET_DESCRIPTOR,
                    (LIBUSB_DT_STRING << 8) | idxPrd, 0, buffer, 0xFF, 0);
            product = new String(buffer, 2, rdo - 2, "UTF-16LE");


        } catch (Exception e)
        {
            e.printStackTrace();
            Log.e(e.getMessage());
        }

        Log.i("","Manufacturer:" + manufacturer + "\n");
        Log.i("","Product:" + product + "\n");
        Log.i("","Serial#:" + mUsbDeviceConnection.getSerial() + "\n");
        return product.trim()+manufacturer.trim();
    }*/
}
