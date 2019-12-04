package android_serialport_api;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.rocktech.sharebookcase.tool.Constant;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import android_serialport_api.SerialPort.onDataReceivedListener;

public class Printer_SPRT extends Printer {

	private Context mBase;
	private byte[] cmd = { 0x10, 0x4, 0x4 };

	protected SerialPort printer = null;

	private static final String TAG = "Printer_SPRT";

	private static int n = -1;

	private boolean status = false;

	public Printer_SPRT(final Context context) throws Exception {
		mBase = context;
		Log.v(TAG, "====初始化打印机====");
		printer = new SerialPort(new File(Constant.COM_PRINTER), Constant.PRINTER_baudrate, 0,
				new onDataReceivedListener() {
					@Override
					public void onDataReceived(byte[] buffer, int size) {
						if (size == 1 && buffer[0] == 5) {
							return;
						}
						byte[] strData = new byte[size];
						for (int i = 0; i < size; i++) {
							strData[i] = buffer[i];
						}
						if (n == 1) {
							if ((getByte(strData[0], 5)) == 3) {
								Log.e("查询是否有纸", "----------纸尽----------");
								// 发送没纸广播
								Intent intent = new Intent("android.intent.action.hal.printer.result.haspaper");
								intent.putExtra("haspaper", false);
								context.sendBroadcast(intent);
								return;
							}
							if ((getByte(strData[0], 5)) == 0) {
								Log.e("查询是否有纸", "----------有纸----------");
								// 发送有纸广播
								Intent intent = new Intent("android.intent.action.hal.printer.result.haspaper");
								intent.putExtra("haspaper", true);
								context.sendBroadcast(intent);
							}
						} else if (n == 2) {
							if (getByte(strData[0], 2) == 3) {
								Log.e("查询纸将近", "----------纸将近----------");
								// 发送纸将近广播
								Intent intent = new Intent("android.intent.action.hal.printer.result.needmore");
								intent.putExtra("needmore", true);
								context.sendBroadcast(intent);
								return;
							}
							if (getByte(strData[0], 2) == 0) {
								Log.e("查询纸将近", "----------有足够的纸---------");
								// 不发生纸将近广播
								Intent intent = new Intent("android.intent.action.hal.printer.result.needmore");
								intent.putExtra("needmore", false);
								context.sendBroadcast(intent);
								return;
							}

						} else if (n == 3) {
							if ((1 & (strData[0] >> 4)) == 0) {
								status = true;
							}
						}
					}
				}, 5000);
		Log.v(TAG, "====初始化成功!====");
	}

	private byte getByte(byte paramByte, int paramInt) {
		return (byte) (0x3 & (paramByte >> paramInt));
	}

	// 关闭打印机
	public void closeSerialPort() {
		printer.stop();
		Log.v(TAG, "====关闭打印机====");
		return;
	}

	@Override
	public void hasPaper() {
		// TODO Auto-generated method stub
		n = 1;
		printer.start();
		printer.sendData(cmd);
	}

	@Override
	public void hasPaperMore() {
		// TODO Auto-generated method stub
		n = 2;
		printer.start();
		printer.sendData(cmd);
	}

	@Override
	public void print(final String str) {
		// TODO Auto-generated method stub
		n = 3;
		final byte[] query = { 16, 6, 1 };
		printer.start();
		printer.sendData(query);

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (status) {
					byte[] b = null;
					byte[] bnew = null;
					try {
						b = str.getBytes("GBK");
						if (b[b.length - 3] == b[b.length - 1]) {
							bnew = Arrays.copyOfRange(b, 0, b.length - 2);
						} else {
							bnew = b;
						}
					} catch (UnsupportedEncodingException e1) {
						return;
					}

					status = false;
					printer.sendData(bnew);
					try {
						Thread.sleep(3000);
						printer.start();
						printer.sendData(query);
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (status) {
						Intent intent = new Intent("android.intent.action.hal.printer.result.status");
						intent.putExtra("status", true);
						mBase.sendBroadcast(intent);
					} else {
						Intent intent = new Intent("android.intent.action.hal.printer.result.status");
						intent.putExtra("status", false);
						mBase.sendBroadcast(intent);
					}
				}
			}
		}).start();
	}

	@Override
	public void paperSize() {
		// TODO Auto-generated method stub
		Intent intent2 = new Intent("android.intent.action.hal.printer.supportsize.result");
		intent2.putExtra("papersize", 0); // 0-3寸，1-4寸
		mBase.sendBroadcast(intent2);
	}

	// 查询是否有纸
	public void onQuaryHasPaper() {
		n = 1;
		printer.start();
		printer.sendData(cmd);
	}

	// 查询纸将近
	public void onQuaryNeedMore() {
		n = 2;
		printer.start();
		printer.sendData(cmd);
	}

	public void printData(final String string) {
		n = 3;
		final byte[] query = { 16, 6, 1 };
		printer.start();
		printer.sendData(query);

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (status) {
					byte[] arrayOfByte = null;
					try {
						arrayOfByte = string.getBytes("GBK");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						return;
					}
					status = false;
					printer.sendData(arrayOfByte);
					try {
						Thread.sleep(3000);
						printer.start();
						printer.sendData(query);
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (status) {
						Intent intent = new Intent("android.intent.action.hal.printer.result.status");
						intent.putExtra("status", true);
						mBase.sendBroadcast(intent);
					} else {
						Intent intent = new Intent("android.intent.action.hal.printer.result.status");
						intent.putExtra("status", false);
						mBase.sendBroadcast(intent);
					}
				}
			}
		}).start();
	}

}
