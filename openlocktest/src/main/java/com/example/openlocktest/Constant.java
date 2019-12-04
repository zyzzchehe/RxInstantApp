package com.example.openlocktest;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class Constant {

	public static String data;//add zyz
	public static boolean aFlag = true;
	public static boolean mFlag;
	// 锁控版
	// public final static String COM_LOCKER = "/dev/ttyUSB0";
	public final static int LOCKER_baudrate = 9600;
	public static String COM_LOCKER = null;

	// 扫码枪
	// public final static String COM_SCANNER = "/dev/ttymxc2";
	public final static int SCANNER_baudrate = 115200;
	public static String COM_SCANNER = null;
	public static String scanType = null;

	// 打印机
	public final static String COM_PRINTER = "/dev/ttymxc4";
	public final static int PRINTER_baudrate = 115200;
	public static String printType = null;

	// 抄表485配置
	public static int Scom_parity = -1; // 校验
	public static int Scom_baudrate = -1; // 波特率

	// 开锁状态值
	public static boolean lockStats = false;

	// 蜂鸣器使能状态 默认1
	public static boolean buzEnableOne = false;

	public static void byteToHex(String string, int length, byte[] msg) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			byte b = msg[i];
			String str = Integer.toHexString(0xFF & b);
			if (str.length() == 1) {
				// str = " 0x0" + str;
				str = " 0" + str;
			} else {
				// str = " 0x" + str;
				str = " " + str;
			}
			sb.append(str);
		}

		Log.v("zyz", string + " : " +sb.toString());
		if(string.equals(" Recv ")){
			Constant.mFlag = true;
			Constant.data = sb.toString();
			Log.i("zyz","Constant.mFlag=="+Constant.mFlag+";Constant.data=="+Constant.data);
		}
		// 解析串口收发的所有数据
		writeLog(string + sb.toString());
	}

	public static String byteToStr(int length, byte[] msg) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			byte b = msg[i];
			String str = Integer.toHexString(0xFF & b);
			if (str.length() == 1) {
				str = " 0" + str;
			} else {
				str = " " + str;
			}
			sb.append(str);
		}
		return sb.toString();
	}

	// 保存log
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd" + " " + "HH:mm:ss:SSS",
			Locale.getDefault());
	private static DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

	public static void writeLog(String string) {
		String time = formatter.format(new Date());
		String fileName = time + ".log";
		String path  = Environment.getExternalStorageDirectory()+"/rocktech";
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			File dir = new File(path);
			if (!dir.exists()) {
				Log.d("zyz","create ok");
				boolean b = dir.mkdir();
				if (b == false) {
					boolean suc = dir.mkdirs();
					Log.d("zyz","suc == "+suc);
				}
			}
		}
		try {
			FileOutputStream fos = new FileOutputStream(path + File.separator + fileName, true);
			fos.write((dateFormat.format(new Date(System.currentTimeMillis())) + ":" + string + "\n").getBytes());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static byte[] CRC8_TAB = { 0x00, 0x5e, (byte) 0xbc, (byte) 0xe2, 0x61, 0x3f, (byte) 0xdd, (byte) 0x83,
			(byte) 0xc2, (byte) 0x9c, 0x7e, 0x20, (byte) 0xa3, (byte) 0xfd, 0x1f, 0x41, (byte) 0x9d, (byte) 0xc3, 0x21,
			0x7f, (byte) 0xfc, (byte) 0xa2, 0x40, 0x1e, 0x5f, 0x01, (byte) 0xe3, (byte) 0xbd, 0x3e, 0x60, (byte) 0x82,
			(byte) 0xdc, 0x23, 0x7d, (byte) 0x9f, (byte) 0xc1, 0x42, 0x1c, (byte) 0xfe, (byte) 0xa0, (byte) 0xe1,
			(byte) 0xbf, 0x5d, 0x03, (byte) 0x80, (byte) 0xde, 0x3c, 0x62, (byte) 0xbe, (byte) 0xe0, 0x02, 0x5c,
			(byte) 0xdf, (byte) 0x81, 0x63, 0x3d, 0x7c, 0x22, (byte) 0xc0, (byte) 0x9e, 0x1d, 0x43, (byte) 0xa1,
			(byte) 0xff, 0x46, 0x18, (byte) 0xfa, (byte) 0xa4, 0x27, 0x79, (byte) 0x9b, (byte) 0xc5, (byte) 0x84,
			(byte) 0xda, 0x38, 0x66, (byte) 0xe5, (byte) 0xbb, 0x59, 0x07, (byte) 0xdb, (byte) 0x85, 0x67, 0x39,
			(byte) 0xba, (byte) 0xe4, 0x06, 0x58, 0x19, 0x47, (byte) 0xa5, (byte) 0xfb, 0x78, 0x26, (byte) 0xc4,
			(byte) 0x9a, 0x65, 0x3b, (byte) 0xd9, (byte) 0x87, 0x04, 0x5a, (byte) 0xb8, (byte) 0xe6, (byte) 0xa7,
			(byte) 0xf9, 0x1b, 0x45, (byte) 0xc6, (byte) 0x98, 0x7a, 0x24, (byte) 0xf8, (byte) 0xa6, 0x44, 0x1a,
			(byte) 0x99, (byte) 0xc7, 0x25, 0x7b, 0x3a, 0x64, (byte) 0x86, (byte) 0xd8, 0x5b, 0x05, (byte) 0xe7,
			(byte) 0xb9, (byte) 0x8c, (byte) 0xd2, 0x30, 0x6e, (byte) 0xed, (byte) 0xb3, 0x51, 0x0f, 0x4e, 0x10,
			(byte) 0xf2, (byte) 0xac, 0x2f, 0x71, (byte) 0x93, (byte) 0xcd, 0x11, 0x4f, (byte) 0xad, (byte) 0xf3, 0x70,
			0x2e, (byte) 0xcc, (byte) 0x92, (byte) 0xd3, (byte) 0x8d, 0x6f, 0x31, (byte) 0xb2, (byte) 0xec, 0x0e, 0x50,
			(byte) 0xaf, (byte) 0xf1, 0x13, 0x4d, (byte) 0xce, (byte) 0x90, 0x72, 0x2c, 0x6d, 0x33, (byte) 0xd1,
			(byte) 0x8f, 0x0c, 0x52, (byte) 0xb0, (byte) 0xee, 0x32, 0x6c, (byte) 0x8e, (byte) 0xd0, 0x53, 0x0d,
			(byte) 0xef, (byte) 0xb1, (byte) 0xf0, (byte) 0xae, 0x4c, 0x12, (byte) 0x91, (byte) 0xcf, 0x2d, 0x73,
			(byte) 0xca, (byte) 0x94, 0x76, 0x28, (byte) 0xab, (byte) 0xf5, 0x17, 0x49, 0x08, 0x56, (byte) 0xb4,
			(byte) 0xea, 0x69, 0x37, (byte) 0xd5, (byte) 0x8b, 0x57, 0x09, (byte) 0xeb, (byte) 0xb5, 0x36, 0x68,
			(byte) 0x8a, (byte) 0xd4, (byte) 0x95, (byte) 0xcb, 0x29, 0x77, (byte) 0xf4, (byte) 0xaa, 0x48, 0x16,
			(byte) 0xe9, (byte) 0xb7, 0x55, 0x0b, (byte) 0x88, (byte) 0xd6, 0x34, 0x6a, 0x2b, 0x75, (byte) 0x97,
			(byte) 0xc9, 0x4a, 0x14, (byte) 0xf6, (byte) 0xa8, 0x74, 0x2a, (byte) 0xc8, (byte) 0x96, 0x15, 0x4b,
			(byte) 0xa9, (byte) 0xf7, (byte) 0xb6, (byte) 0xe8, 0x0a, 0x54, (byte) 0xd7, (byte) 0x89, 0x6b, 0x35 };

	public static byte calcCrc8(byte[] data, int offset, int len) {
		return calcCrc8(data, offset, len, (byte) 0);
	}
	public static byte calcCrc8(byte[] data, int offset, int len, byte preval) {
		byte ret = preval;
		for (int i = offset; i < (offset + len); ++i) {
			ret = CRC8_TAB[(0x00ff & (ret ^ data[i]))];
		}
		return ret;
	}

	/**
	 * 根据系统版本号（gelanda、panji）设置锁控板和扫描头串口节点
	 *
	 * @return void;
	 */
	public static void setPorts() {
		if (COM_LOCKER == null || COM_SCANNER == null) {
			String str = getDisplayId();
			Log.i("zyz","setPorts str=="+str);
			if (str.contains("panji")) {
				COM_LOCKER = "/dev/ttyUSB0";
			} else if (str.contains("gelanda")) {
				COM_LOCKER = "/dev/ttymxc1";
			} else {
				COM_LOCKER = "/dev/ttyUSB0";
			}
			Log.i("zyz","setPorts COM_LOCKER=="+COM_LOCKER);
		}
	}

	/**
	 * 获取版本号
	 *
	 * @return String or null
	 */
	private static String getDisplayId() {
		Process process;
		try {
			process = Runtime.getRuntime().exec("getprop ro.build.display.id");
			InputStream in = process.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder sb = new StringBuilder();
			String line = null;
			try {
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				in.close();
			}
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private final static String[] USB_DISPLAY_PATHS = { "/storage/udisk/", "/storage/udisk1/", "/storage/udisk2/",
			"/storage/udisk3/", "/storage/udisk4/", "/storage/udisk5/" };

	/**
	 * 在U盘搜索lockSets.xml配置文件
	 *
	 * @return 存在返回文件路径 反之NULL
	 */
	public static String getUSBSetXML() {

		String setPath = null;

		for (String usbpath : USB_DISPLAY_PATHS) {
			File dir = new File(usbpath);
			String regex = ".+\\.[xX][mM][lL]";

			if (dir.exists() && dir.isDirectory()) {
				File[] files = dir.listFiles(new MyFilenameFilter(regex));
				if (files != null && files.length > 0) {
					for (File file : files) {
						Log.v("PJ", file.getName() + " : " + file.getPath());
						if (file.getName().equals("lockSets.xml")) {
							setPath = file.getPath();
							break;
						}
					}
				}
			}
		}

		return setPath;
	}

	private static class MyFilenameFilter implements FilenameFilter {
		private Pattern p;

		public MyFilenameFilter(String regex) {
			p = Pattern.compile(regex);
		}

		public boolean accept(File file, String name) {
			return p.matcher(name).matches();
		}
	}

}
