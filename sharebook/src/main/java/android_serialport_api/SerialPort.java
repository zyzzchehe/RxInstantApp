package android_serialport_api;

import android.util.Log;
import com.rocktech.sharebookcase.tool.Constant;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class SerialPort {
	private static final String TAG = "SerialPort";

	private FileDescriptor mFd = null;
	private FileInputStream mFileInputStream = null;
	private FileOutputStream mFileOutputStream = null;
	private boolean isFileOutputStreamWriting = false;
	public ReadThread mReadThread;
	private onDataReceivedListener mListener = null;
	private boolean mIsSerialPortOpenSuccessed = false;

	private byte[] mData = new byte[50];
	private int mCount = 0;

	// 超时时间 单位ms
	private int overTime;

	public interface onDataReceivedListener {
		public void onDataReceived(final byte[] buffer, final int size);
	}

	class ReadThread extends Thread {

		private boolean mRunning = false;

		public ReadThread() {
			mRunning = true;
		}

		public void cancel() {
			mRunning = false;
			interrupt();
		}

		@Override
		public void run() {
			super.run();
			int cnt = 0, nCount = 0;
			try {
				while (mRunning && !isInterrupted()) {

					if (mFileInputStream == null) {
						return;
					}
					if (mFileInputStream.available() <= 0) {

						if (0 == overTime) {
							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							continue;
						} else {
							if (nCount > (overTime / 10)) {
								if (mListener != null) {
									mListener.onDataReceived(new byte[] { 0x5 }, 1);
									Log.i(TAG, "wz -----break");
								}
								break;
							} else {
								nCount++;
								try {
									Thread.sleep(10);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								continue;
							}
						}
					}
					cnt = mFileInputStream.read(mData);
					Constant.byteToHex("SerialPort, run(), === Recv data", cnt, mData);
					if (mListener != null && mData != null) {
						mListener.onDataReceived(mData, cnt);
					}
					break;
				}
			} catch (IOException e) {
				Log.e(TAG, "run() ==> @@@ ReadThread IOException");
				e.printStackTrace();
			}
		}
	}

	public SerialPort(File device, int baudrate, int flags, onDataReceivedListener listener, int overtime)
			throws SecurityException, IOException {
		mIsSerialPortOpenSuccessed = false;

		this.overTime = overtime;

		mCount = 0;
		for (int i = 0; i < mData.length; i++) {
			mData[i] = 0x00;
		}

		/* Check access permission */
		if (!device.canRead() || !device.canWrite()) {
			try {
				/* Missing read/write permission, trying to chmod the file */
				Process su;

				su = Runtime.getRuntime().exec("/system/bin/su");
				String cmd = "chmod 666 " + device.getAbsolutePath() + "\n" + "exit\n";
				su.getOutputStream().write(cmd.getBytes());
				if ((su.waitFor() != 0) || !device.canRead() || !device.canWrite()) {
					throw new SecurityException();
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new SecurityException();
			}
		}

		mFd = open(device.getAbsolutePath(), baudrate, flags);
		if (mFd == null) {
			Log.e(TAG, "native open returns null");
			throw new IOException();
		}

		mListener = listener;
		mFileInputStream = new FileInputStream(mFd);
		mFileOutputStream = new FileOutputStream(mFd);

		mIsSerialPortOpenSuccessed = true;
	}

	public boolean isOpenedSuccessed() {
		return mIsSerialPortOpenSuccessed;
	}

	public void start() {

		if (mReadThread != null) {
			mReadThread.cancel();
			mReadThread = null;
		}

		mReadThread = new ReadThread();
		mReadThread.start();
	}

	public void stop() {
		Log.e(TAG, "stop");

		if (mReadThread != null) {
			mReadThread.cancel();
			mReadThread = null;
		}

		try {
			if (mFileInputStream != null) {
				mFileInputStream.close();
				mFileInputStream = null;
			}

			if (mFileOutputStream != null && isFileOutputStreamWriting == false) {
				mFileOutputStream.close();
				mFileOutputStream = null;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		close();
	}

	public boolean sendData(byte[] data) {
		if (mIsSerialPortOpenSuccessed == false) {
			return false;
		}
		if (mFileOutputStream != null) {
			try {
				isFileOutputStreamWriting = true;
				mFileOutputStream.write(data);
//				Constant.byteToHex(" Send ", data.length, data);
				mFileOutputStream.flush();
				isFileOutputStreamWriting = false;
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return false;
	}

	// JNI
	private native static FileDescriptor open(String path, int baudrate, int flags);

	public native void close();

	static {
		System.loadLibrary("serial_port2");
	}
}