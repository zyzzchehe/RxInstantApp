package android_serialport_api;

import android.util.Log;

import com.example.openlocktest.Constant;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class SerialPort {
	private static final String TAG = "zyz";

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
		int cnt = 0, nCount = 0;
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
			Log.i(TAG, "run() ==> start !!!!! ");
			try {
				while (mRunning && !isInterrupted()) {
					if (mFileInputStream == null) {
						return;
					}
					Log.i("zyz","mFileInputStream.available()=="+mFileInputStream.available());
					if (mFileInputStream.available() <= 0) {
						if (0 == overTime) {
							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							continue;
						} else {
							Log.d("zyz","sp nCount== "+nCount);
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
					}else{
						nCount = 0;
						cnt = mFileInputStream.read(mData);
						Constant.byteToHex(" Recv ", cnt, mData);
						if (mListener != null && mData != null) {
							mListener.onDataReceived(mData, cnt);
						}
					}
					if (mFileInputStream.available() > 0) {
						cnt = mFileInputStream.read(mData);
						if (cnt > 0) {
							mListener.onDataReceived(mData, cnt);
						}
					}
					break;
				}
			} catch (IOException e) {
				Log.e(TAG, "run() ==> IOException");
				e.printStackTrace();
			}
			Log.e(TAG, "run() ==> stop !!!!! ");
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
		Log.e(TAG, "start");
		if (mReadThread != null) {
			mReadThread.cancel();
			mReadThread = null;
		}
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
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
		Log.i("zyz","got to sendData method");
		if (mIsSerialPortOpenSuccessed == false) {
			return false;
		}
		if (mFileOutputStream != null) {
			try {
				isFileOutputStreamWriting = true;
				mFileOutputStream.write(data);
				Constant.byteToHex(" Send ", data.length, data);
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
		System.loadLibrary("serial_port");
	}
}