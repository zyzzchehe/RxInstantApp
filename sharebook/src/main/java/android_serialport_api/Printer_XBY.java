package android_serialport_api;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.rocktech.sharebookcase.opt.MyCpclCtrl;
import com.rocktech.sharebookcase.tool.Constant;
import java.io.File;
import java.io.UnsupportedEncodingException;
import android_serialport_api.SerialPort.onDataReceivedListener;

public class Printer_XBY extends Printer {
	private Context mBase;
	protected SerialPort printer = null;
	private static final String TAG = "Printer_XBY";
	private static int n = -1;
	private Intent intent;

	public Printer_XBY(Context context) throws Exception {
		mBase = context;
		printer = new SerialPort(new File(Constant.COM_PRINTER), 19200, 0, new onDataReceivedListener() {

			@Override
			public void onDataReceived(byte[] buffer, int size) {
				// TODO Auto-generated method stub
				if (size == 1 && buffer[0] == 5) {
					return;
				}
				switch (n) {
				case 1:
					// 是否有纸
					intent = new Intent("android.intent.action.hal.printer.result.haspaper");
					intent.putExtra("haspaper", (buffer[0] & 0x60) != 0x60);
					mBase.sendBroadcast(intent);
					break;
				case 2:
					// 打印结果
					intent = new Intent("android.intent.action.hal.printer.result.status");
					intent.putExtra("status", (buffer[0] & 0x01) == 0x00);
					mBase.sendBroadcast(intent);
					break;
				case 3:
					// 检查快递单是否取走
				case 4:
					// 是否纸将近
					intent = new Intent("android.intent.action.hal.printer.result.needmore");
					intent.putExtra("needmore", (buffer[0] & 0x0C) == 0x0C);
					mBase.sendBroadcast(intent);
					break;
				default:
					break;
				}
			}

		}, 5000);
		Log.v(TAG, "====初始化成功!====");
	}

	@Override
	public void hasPaper() {
		n = 1;
		byte[] szCmd = { 0x10, 0x04, 0x04 }; // 传输纸传感器状态
		printer.start();
		printer.sendData(szCmd);
	}

	@Override
	public void hasPaperMore() {
		n = 4;
		byte[] szCmd = { 0x10, 0x04, 0x04 }; // 传输纸传感器状态
		printer.start();
		printer.sendData(szCmd);
	}

	private byte[] GetPrintText(String strText) {
		byte[] strRst = null;
		try {
			strRst = strText.getBytes("GB18030");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return strRst;
	}

	@Override
	public void print(String str) {
		n = 2;
		byte[] szCmd = { 0x10, 0x04, 0x05 };// 传输打印结果
		byte[] qzCmd = { 0x1D, 0x56, 0x42, 0x10 }; // 切纸指令

		if (str != null && str.length() > 0) {
			byte[] total = GetPrintText(MyCpclCtrl.getCpclFromJson(str));
			int j = total.length / 800;
			for (int i = 0; i < j; i++) {
				printer.sendData(subBytes(total, 0, 800));
				total = subBytes(total, 800, total.length - 800);
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (total != null) {
				printer.sendData(byteMerger(total, qzCmd));
			} else {
				printer.sendData(qzCmd);
			}

			printer.start();
			printer.sendData(szCmd);
		}
	}

	private byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
		byte[] byte_3 = new byte[byte_1.length + byte_2.length];
		System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
		System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
		return byte_3;
	}

	private byte[] subBytes(byte[] src, int begin, int count) {
		if (count <= 0) {
			return null;
		}
		byte[] bs = new byte[count];
		for (int i = begin; i < begin + count; i++) {
			bs[i - begin] = src[i];
		}
		return bs;
	}

	@Override
	public void paperSize() {
		// n = 1;
		// byte[] szCmd = { 0x1B, 0x6A, 0x26 };// 查询纸张宽度
		// printer.start();
		// printer.sendData(szCmd);
		Intent intent2 = new Intent("android.intent.action.hal.printer.supportsize.result");
		intent2.putExtra("papersize", 1); // 0-3寸，1-4寸
		mBase.sendBroadcast(intent2);
	}

}
