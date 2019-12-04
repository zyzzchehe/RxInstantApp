package android_serialport_api;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import com.rocktech.sharebookcase.tool.Constant;
import java.io.File;
import java.io.UnsupportedEncodingException;
import android_serialport_api.SerialPort.onDataReceivedListener;

public class Scanner {
	private Context mBase;
	private static final String TAG = "Scanner";
	private android_serialport_api.SerialPort scanner;

	// 霍尼韦尔
	// byte[] start = { 22, 84, 13 };
	// byte[] stop = { 22, 85, 13 };

	// 德沃
	// byte[] start = { 0x1a, 0x54, 0x0d };
	// byte[] stop = { 0x1a, 0x55, 0x0d };
	private static byte[] start = null;
	private static byte[] stop = null;

	private static byte[] LISHIZHI = null;

	private static byte[] subBytes(byte[] src, int begin, int count) {
		byte[] bs = new byte[count];
		for (int i = begin; i < begin + count; i++) {
			bs[i - begin] = src[i];
		}
		return bs;
	}

	private static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
		byte[] byte_3 = new byte[byte_1.length + byte_2.length];
		System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
		System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
		return byte_3;
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				Intent intent = new Intent("android.intent.action.hal.barcodescanner.scandata");
				try {
					intent.putExtra("scandata", new String(LISHIZHI, "GBK"));
					mBase.sendBroadcast(intent);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				LISHIZHI = null;
				// 关闭扫描
				cancelScanner();
				break;
			case 2: {
				scanner.start();
				break;
			}

			default:
				break;
			}
		};
	};

	public Scanner(int paramInt, Context context) throws Exception {
		mBase = context;

		if (Constant.scanType.equals("honeywell_Series") || Constant.scanType.equals("honeywell_Usb")) {
			// 霍尼韦尔
			start = new byte[] { 22, 84, 13 };
			stop = new byte[] { 22, 85, 13 };
		} else if (Constant.scanType.equals("dewo")) {
			// 德沃
			start = new byte[] { 0x1a, 0x54, 0x0d };
			stop = new byte[] { 0x1a, 0x55, 0x0d };
		} else if (Constant.scanType.equals("fm50")) {
			// start = new byte[] { 0x1B, 0x31 };
			// stop = new byte[] { 0x1B, 0x30 };
			start = new byte[] { 0x1B, 0x51 };
			stop = new byte[] { 0x1B, 0x50 };
		} else if (Constant.scanType.equals("4102s")) {
			start = new byte[] { 0x02, (byte) 0xF4, 0x03 };
			stop = new byte[] { 0x02, (byte) 0xF5, 0x03 };
		}

		scanner = new SerialPort(new File(Constant.COM_SCANNER), Constant.SCANNER_baudrate, 0,
				new onDataReceivedListener() {
					@Override
					public void onDataReceived(byte[] buffer, int size) {
						if (Constant.scanType.equals("fm50")) {
							if (LISHIZHI == null) {
								LISHIZHI = subBytes(buffer, 0, size);

								if (LISHIZHI.length == 1 && LISHIZHI[0] == 5) {
									return;
								} else {

									boolean b = true;

									for (int i = 0; i < LISHIZHI.length; i++) {
										if (LISHIZHI[i] != 6) {
											b = false;
											mHandler.sendEmptyMessageDelayed(1, 300);
											break;
										}
									}

									Constant.byteToHex("LISHIZHI1", LISHIZHI.length, LISHIZHI);

									if (b) {
										LISHIZHI = null;
										mHandler.sendEmptyMessage(2);
										return;
									} else {
										mHandler.sendEmptyMessage(2);
									}

								}
							} else {
								LISHIZHI = byteMerger(LISHIZHI, subBytes(buffer, 0, size));
							}
							Constant.byteToHex("LISHIZHI2", LISHIZHI.length, LISHIZHI);
							// 结尾不等于回车,继续接收
							if (LISHIZHI[LISHIZHI.length - 1] != 13) {
								mHandler.sendEmptyMessage(2);
							} else {
								// 结尾等于回车、删除回车
								LISHIZHI = subBytes(LISHIZHI, 0, LISHIZHI.length - 1);
								
								mHandler.removeMessages(1);
								mHandler.sendEmptyMessage(1);
							}

						} else {
							if (LISHIZHI == null) {
								LISHIZHI = subBytes(buffer, 0, size);

								if (LISHIZHI.length == 1 && LISHIZHI[0] == 5) {
									return;
								} else {
									// 0.3s之后反馈结果
									mHandler.sendEmptyMessageDelayed(1, 300);
								}
							} else {
								LISHIZHI = byteMerger(LISHIZHI, subBytes(buffer, 0, size));
							}
							// 结尾不等于回车,继续接收
							if (LISHIZHI[LISHIZHI.length - 1] != 13) {
								mHandler.sendEmptyMessage(2);
							} else {
								// 结尾等于回车、删除回车
								LISHIZHI = subBytes(LISHIZHI, 0, LISHIZHI.length - 1);
								
								mHandler.removeMessages(1);
								mHandler.sendEmptyMessage(1);
							}
						}

					}
				}, 35000);
		Log.v(TAG, "===初始化成功!====");

	}

	public void cancelScanner() {
		Log.v(TAG, "===停止发光====");
		if (scanner.mReadThread != null) {
			scanner.mReadThread.cancel();
		}
		scanner.sendData(stop);
		Log.v(TAG, "===取消扫码====");
		return;
	}

	public void closeSerialPort() {
		scanner.sendData(stop);
		scanner.stop();
		Log.v(TAG, "====关闭扫码====");
		return;
	}

	public void startScanner() {
		Log.v(TAG, "===开始发光====");
		scanner.start();
		scanner.sendData(start);
		Log.v(TAG, "===开始扫码====");
		return;
	}

}
