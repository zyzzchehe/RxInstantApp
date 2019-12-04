package com.hoho.android.usbserial;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.example.usbdeveloptest.R;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String TAG = "UsbSerialTest";
	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
	UsbManager manager = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		manager = (UsbManager) getSystemService(Context.USB_SERVICE);
		getUsbPermission();
	}

	private void getUsbPermission(){

		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		registerReceiver(mUsbPermissionActionReceiver, filter);
		PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

		for (final UsbDevice usbDevice : manager.getDeviceList().values()) {
			if(manager.hasPermission(usbDevice)){
				afterGetUsbPermission(usbDevice);
			}else{
				manager.requestPermission(usbDevice, mPermissionIntent);
			}
		}
	}


	private void afterGetUsbPermission(UsbDevice usbDevice){
		Log.d(TAG,"got permission for usb device: " + usbDevice);
		Log.d(TAG, "found USB device: VID = " + usbDevice.getVendorId() + " PID = " + usbDevice.getProductId());
		//获取到权限后
		init();
		//openUsbDevice(usbDevice);
	}

	private void openUsbDevice(UsbDevice usbDevice){
		UsbDeviceConnection connection = manager.openDevice(usbDevice);
		UsbInterface usbIface = usbDevice.getInterface(2);
		Log.d(TAG,"usbIface str = "+usbIface.toString());
		connection.claimInterface(usbIface,true);
	}

	private final BroadcastReceiver mUsbPermissionActionReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbDevice usbDevice = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						if(null != usbDevice) afterGetUsbPermission(usbDevice);
					} else {
						Toast.makeText(context, "Permission denied for device" + usbDevice, Toast.LENGTH_LONG).show();
					}
				}
			}
		}
	};

	private void init() {
		// 查找所有插入的设备
		List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
		if (availableDrivers.isEmpty()) {
			return;
		}
		Log.d(TAG,"availableDrivers size = "+availableDrivers.size());
		// 打开设备，建立通信连接
		UsbSerialDriver driver = availableDrivers.get(0);
		UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
		if (connection == null) {
			return;
		}
		Log.d(TAG,"driver.getPorts() = "+driver.getPorts());
		//打开端口，设置端口参数，读取数据
		UsbSerialPort port = driver.getPorts().get(0);
		try {
			Log.d(TAG, "connection serial = "+connection.getSerial());
			port.open(connection);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				port.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
