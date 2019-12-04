package android_serialport_api;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import com.rocktech.sharebookcase.tool.Constant;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import android_serialport_api.SerialPort.onDataReceivedListener;

public class Printer_QR extends Printer {
	private Context mBase;
	protected android_serialport_api.SerialPort printer = null;
	private static final String TAG = "Printer_QR";
	private static int n = -1;
	private final static String cmd = "READSTA " + "\r\n";

	public Printer_QR(Context context) throws Exception {
		mBase = context;
		Log.v(TAG, "====初始化打印机====");
		printer = new android_serialport_api.SerialPort(new File(Constant.COM_PRINTER), Constant.PRINTER_baudrate, 0,
				new onDataReceivedListener() {

					@Override
					public void onDataReceived(byte[] buffer, int size) {
						// TODO Auto-generated method stub
						if (size == 1 && buffer[0] == 5) {
							return;
						}
						String str = null;
						try {
							str = new String(buffer, "gb2312");
							Constant.writeLog("打印机反馈： " + str);
						} catch (UnsupportedEncodingException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						if (1 == n) {
							// 是否有纸
							if (str != null) {
								String[] state = str.split("[ ,]");
								Intent intent = new Intent("android.intent.action.hal.printer.result.haspaper");
								if (state[1].equals("PAPEREND") || state[1].equals("PAPER")) {
									intent.putExtra("haspaper", true);
									mBase.sendBroadcast(intent);
								} else if (state[1].equals("NOPAPER")) {
									// 缺纸
									intent.putExtra("haspaper", false);
									mBase.sendBroadcast(intent);
								}
							}
						} else if (2 == n) {
							// 打印
							if (str != null) {
								// String[] state = str.split("[ ,]");
								// if (state[3].equals("IDLE")) {
								// }
								Intent intent = new Intent("android.intent.action.hal.printer.result.status");
								intent.putExtra("status", true);
								mBase.sendBroadcast(intent);
							}
						} else if (3 == n) {
							// 检查快递单是否取走
							if (str != null) {
								List<byte[]> data = QiRui_ParserFCboxJsonToQR(data2Print);
								for (byte[] b : data) {
									printer.sendData(b);
								}
								try {
									Thread.sleep(5500);
								} catch (InterruptedException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								n = 2;
								printer.start();
								try {
									printer.sendData(cmd.getBytes("gb2312"));
								} catch (UnsupportedEncodingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						} else if (n == 4) {
							if (str != null) {
								String[] state = str.split("[ ,]");
								if (state[1].equals("PAPEREND") || state[1].equals("NOPAPER")) {
									Intent intent = new Intent("android.intent.action.hal.printer.result.needmore");
									intent.putExtra("needmore", true);
									mBase.sendBroadcast(intent);
								} else {
									Intent intent = new Intent("android.intent.action.hal.printer.result.needmore");
									intent.putExtra("needmore", false);
									mBase.sendBroadcast(intent);
								}
							}
						}
					}

				}, 5000);
		Log.v(TAG, "====初始化成功!====");
	}

	private String data2Print;

	public void printData(String str) {
		n = 3;
		data2Print = str;
		printer.start();
		try {
			printer.sendData(cmd.getBytes("gb2312"));
		} catch (UnsupportedEncodingException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}

	/**
	 * 检查运单是否被取走
	 */
	public void QiRui_Query_Status() {
		n = 3;
		printer.start();
		try {
			printer.sendData(cmd.getBytes("gb2312"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 检查纸张状态
	 * 
	 * @return 0-缺纸；1-有纸；2-纸将近；3-纸舱盖打开
	 */
	public void QiRui_CheckPaper() {
		n = 1;
		printer.start();
		try {
			printer.sendData(cmd.getBytes("gb2312"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/**
	 * 检查纸张是否将近
	 */
	public void QiRui_CheckPaper2() {
		n = 4;
		printer.start();
		try {
			printer.sendData(cmd.getBytes("gb2312"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Override
	public void hasPaper() {
		// TODO Auto-generated method stub
		n = 1;
		printer.start();
		try {
			printer.sendData(cmd.getBytes("gb2312"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Override
	public void hasPaperMore() {
		// TODO Auto-generated method stub
		n = 4;
		printer.start();
		try {
			printer.sendData(cmd.getBytes("gb2312"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Override
	public void print(String str) {
		// TODO Auto-generated method stub
		n = 3;
		data2Print = str;
		printer.start();
		try {
			printer.sendData(cmd.getBytes("gb2312"));
		} catch (UnsupportedEncodingException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}

	@Override
	public void paperSize() {
		// TODO Auto-generated method stub
		Intent intent2 = new Intent("android.intent.action.hal.printer.supportsize.result");
		intent2.putExtra("papersize", 1); // 0-3寸，1-4寸
		mBase.sendBroadcast(intent2);
	}

	public String Version = "20160722_Ver3.3";
	public String Author = "LJF-Studio.COM";

	private int PaperState = 0;
	private boolean Picked = false;
	private boolean IsBusy = true;
	private boolean CutState = false;

	/**
	 * ������ǩҳ���С
	 * 
	 * @param width
	 *            ��ǩ���,��λ mm
	 * @param hight
	 *            ��ǩ�߶�,��λ mm
	 * @return ����byte[]
	 */
	public byte[] QiRui_CreatePage(int width, int height) {

		String cmd = "SIZE " + width + " mm," + height + " mm\r\n";

		try {
			return cmd.getBytes("gb2312");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private String sQiRui_CreatePage(int width, int height) {

		String cmd = "SIZE " + width + " mm," + height + " mm\r\n";
		return cmd;
	}

	/**
	 * ��ӡ��ǩ
	 * 
	 * @param count
	 *            ��ӡ����
	 * @return ����byte[]
	 */
	public byte[] QiRui_PrintPage(int count) {

		String cmd = "PRINT " + count + "\r\n";

		try {
			return cmd.getBytes("gb2312");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private String sQiRui_PrintPage(int count) {

		String cmd = "PRINT " + count + "\r\n";
		return cmd;
	}

	/**
	 * ��ӡ�ı�����
	 * 
	 * @param x
	 *            ��ʼx����
	 * @param y
	 *            ��ʼy����
	 * @param font
	 *            ���壺Ŀǰ֧��TSS16.BF2��16���� TSS24.BF2��24���� TSS32.BF2��32����
	 * @param xmulti
	 *            ����Ŵ���
	 * @param ymulti
	 *            ����Ŵ���
	 * @param rotation
	 *            ��ת�Ƕȣ���ѡ���� 0��90��180��270
	 * @param content
	 *            ����
	 * @return ����byte[]
	 */
	public byte[] QiRui_Text(int x, int y, String font, int rotation, int xmulti, int ymulti, String content) {

		String _content = content.replace("\"", "\\[\"]");
		String cmd = "TEXT " + x + "," + y + "," + "\"" + font + "\"" + "," + rotation + "," + xmulti + "," + ymulti
				+ "," + "\"" + _content + "\"\r\n";

		try {
			return cmd.getBytes("gb2312");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private String sQiRui_Text(int x, int y, String font, int rotation, int xmulti, int ymulti, String content) {

		String _content = content.replace("\"", "\\[\"]");
		String cmd = "TEXT " + x + "," + y + "," + "\"" + font + "\"" + "," + rotation + "," + xmulti + "," + ymulti
				+ "," + "\"" + _content + "\"\r\n";

		return cmd;
	}

	/**
	 * ��ӡ�ı����ݣ����أ������ӼӴ�
	 * 
	 * @param x
	 *            ��ʼx����
	 * @param y
	 *            ��ʼy����
	 * @param font
	 *            ���壺Ŀǰ֧��TSS16.BF2��16���� TSS24.BF2��24���� TSS32.BF2��32����
	 * @param rotation
	 *            ��ת�Ƕȣ���ѡ���� 0��90��180��270
	 * @param xmulti
	 *            ����Ŵ���
	 * @param ymulti
	 *            ����Ŵ���
	 * @param isbold
	 *            ����ѡ������ �Ƿ�Ӵ�
	 * @param content
	 *            ����
	 * @return ����byte[]
	 */
	public byte[] QiRui_Text(int x, int y, String font, int rotation, int xmulti, int ymulti, boolean isBold,
			String content) {
		String cmd = null;
		String _content = content.replace("\"", "\\[\"]");
		if (isBold) {
			cmd = "TEXT " + x + "," + y + "," + "\"" + font + "\"" + "," + rotation + "," + xmulti + "," + ymulti
					+ ",B1," + "\"" + _content + "\"\r\n";
		} else {
			cmd = "TEXT " + x + "," + y + "," + "\"" + font + "\"" + "," + rotation + "," + xmulti + "," + ymulti + ","
					+ "\"" + _content + "\"\r\n";
		}
		try {
			return cmd.getBytes("gb2312");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private String sQiRui_Text(int x, int y, String font, int rotation, int xmulti, int ymulti, boolean isBold,
			String content) {
		String _content = content.replace("\"", "\\[\"]");
		String cmd = null;
		if (isBold) {
			cmd = "TEXT " + x + "," + y + "," + "\"" + font + "\"" + "," + rotation + "," + xmulti + "," + ymulti
					+ ",B1," + "\"" + _content + "\"\r\n";
		} else {
			cmd = "TEXT " + x + "," + y + "," + "\"" + font + "\"" + "," + rotation + "," + xmulti + "," + ymulti + ","
					+ "\"" + _content + "\"\r\n";
		}
		return cmd;
	}

	/**
	 * �ı���
	 * 
	 * @param x
	 *            ��ʼx����
	 * @param y
	 *            ��ʼy����
	 * @param font
	 *            ���壺Ŀǰ֧��TSS16.BF2��16���� TSS24.BF2��24���� TSS32.BF2��32����
	 * @param xmulti
	 *            ����Ŵ���
	 * @param ymulti
	 *            ����Ŵ���
	 * @param rotation
	 *            ��ת�Ƕȣ���ѡ���� 0��90��180��270
	 * @param width
	 *            �ı�����
	 * @param linespace
	 *            ����ѡ������ �м��
	 * @param content
	 *            ����
	 * @return ����byte[]
	 */
	public byte[] QiRui_Textbox(int x, int y, String font, int rotation, int xmulti, int ymulti, int width,
			int linespace, String content) {
		String _content = content.replace("\"", "\\[\"]");
		// TEXTBOX 30,1200,"TSS24.BF2",0,1,1,700, L24,
		// "��衵��Ӹ���������ҵ�Ե����浥��ӡ���������Ƴ�ȫ�µĵ����浥��ӡ������������浥��ӡ�������������ҵ�Ե����浥�ĸ߱�׼���û������˴����ĵ����ʹ��£��Ƴ�������ͬ�������浥��ӡ��û�е�ȫ�¹��ܣ�ͬʱҲ�����д�ӡ�����ڵ����������ת��Ľ���"
		String cmd = null;
		if (linespace >= 24) {
			cmd = "TEXTBOX " + x + "," + y + "," + "\"" + font + "\"" + "," + rotation + "," + xmulti + "," + ymulti
					+ "," + width + ", L" + linespace + "," + "\"" + _content + "\"\r\n";
		} else {
			cmd = "TEXTBOX " + x + "," + y + "," + "\"" + font + "\"" + "," + rotation + "," + xmulti + "," + ymulti
					+ "," + width + "," + "\"" + _content + "\"\r\n";
		}
		try {
			return cmd.getBytes("gb2312");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * ����
	 * 
	 * @param x
	 *            ��ʼx����
	 * @param y
	 *            ��ʼy����
	 * @param width
	 *            �߿�
	 * @param height
	 *            �߸�
	 * @param dotted
	 *            ʵ��/���� 0-ʵ�ߣ�1-����
	 * @return ����byte[]
	 */
	public byte[] QiRui_DrawLine(int x, int y, int width, int height, int dotted) {

		String cmd = "BAR " + x + "," + y + "," + width + "," + height + "," + dotted + "\r\n";

		try {
			return cmd.getBytes("gb2312");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private String sQiRui_DrawLine(int x, int y, int width, int height, int dotted) {

		String cmd = "BAR " + x + "," + y + "," + width + "," + height + "," + dotted + "\r\n";

		return cmd;
	}

	/**
	 * ����(����)����Ӧcpcl��line������ҿ��Ի�б��
	 * 
	 * @param x
	 *            ��ʼx����
	 * @param y
	 *            ��ʼy����
	 * @param width
	 *            �߿�
	 * @param height
	 *            �߸�
	 * @param dottedType
	 *            ʵ��/���� 0-ʵ�ߣ�1-4����
	 * @return ����byte[]
	 */
	public byte[] QiRui_DrawLine(int start_x, int start_y, int end_x, int end_y, int width, int dottedType) {
		// LINE 320,460,600,780,8,M3
		String cmd = null;
		switch (dottedType) {
		case 0:
			cmd = "LINE " + start_x + "," + start_y + "," + end_x + "," + end_y + "," + width + "\r\n";
			break;
		case 1:
			cmd = "LINE " + start_x + "," + start_y + "," + end_x + "," + end_y + "," + width + ",M1\r\n";
			break;
		case 2:
			cmd = "LINE " + start_x + "," + start_y + "," + end_x + "," + end_y + "," + width + ",M2\r\n";
			break;
		case 3:
			cmd = "LINE " + start_x + "," + start_y + "," + end_x + "," + end_y + "," + width + ",M3\r\n";
			break;
		case 4:
			cmd = "LINE " + start_x + "," + start_y + "," + end_x + "," + end_y + "," + width + ",M4\r\n";
			break;

		}

		try {
			return cmd.getBytes("gb2312");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private String sQiRui_DrawLine(int start_x, int start_y, int end_x, int end_y, int width, int dottedType) {
		// LINE 320,460,600,780,8,M3
		String cmd = null;
		switch (dottedType) {
		case 0:
			cmd = "LINE " + start_x + "," + start_y + "," + end_x + "," + end_y + "," + width + "\r\n";
			break;
		case 1:
			cmd = "LINE " + start_x + "," + start_y + "," + end_x + "," + end_y + "," + width + ",M1\r\n";
			break;
		case 2:
			cmd = "LINE " + start_x + "," + start_y + "," + end_x + "," + end_y + "," + width + ",M2\r\n";
			break;
		case 3:
			cmd = "LINE " + start_x + "," + start_y + "," + end_x + "," + end_y + "," + width + ",M3\r\n";
			break;
		case 4:
			cmd = "LINE " + start_x + "," + start_y + "," + end_x + "," + end_y + "," + width + ",M4\r\n";
			break;

		}

		return cmd;
	}

	/**
	 * ��ӡλͼ(����)
	 * 
	 * @param x
	 *            ��ʼx����
	 * @param y
	 *            ��ʼy����
	 * @param width
	 *            ͼƬ��� ��Ӧjson�е� width
	 * @param height
	 *            ͼƬ�߶� ��Ӧjson�е� height
	 * @param octetStr
	 *            ͼƬ���� ��Ӧjson�е� octetStr
	 * @return ����byte[]
	 */
	public byte[] QiRui_DrawPic(int x, int y, int width, int height, String octetStr) {
		String cmd = "BITMAP " + x + "," + y + "," + (width / 8) + "," + height + ",1,";
		byte[] pixel = new byte[octetStr.length() / 2];
		for (int i = 0; i < pixel.length; i++) {
			try {
				pixel[i] = (byte) ~(0xff & Integer.parseInt(octetStr.substring(i * 2, i * 2 + 2), 16));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		byte[] bcmd;
		try {
			bcmd = cmd.getBytes("gb2312");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			bcmd = null;
		}
		byte[] totalcmd = new byte[bcmd.length + pixel.length];
		System.arraycopy(bcmd, 0, totalcmd, 0, bcmd.length);
		System.arraycopy(pixel, 0, totalcmd, bcmd.length, pixel.length);

		return totalcmd;

	}

	private String sQiRui_DrawPic(int x, int y, int width, int height, String octetStr) {
		String cmd = "BITMAP " + x + "," + y + "," + (width / 8) + "," + height + ",1,";
		byte[] pixel = new byte[octetStr.length() / 2];
		for (int i = 0; i < pixel.length; i++) {
			try {
				pixel[i] = (byte) ~(0xff & Integer.parseInt(octetStr.substring(i * 2, i * 2 + 2), 16));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		String isoString = null;
		try {
			isoString = new String(pixel, "gb2312");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return cmd + isoString + "\r\n";

	}

	/**
	 * ��ӡһά����
	 * 
	 * @param x
	 *            ��ʼx����
	 * @param y
	 *            ��ʼy����
	 * @param type
	 *            �������ͣ� 0�� code128 1�� code39 2- code93 3- itf 4�� upca 5��
	 *            upce 6- ean8 7- ean13
	 * @param height
	 *            ����߶�
	 * @param hri
	 *            hri�ַ���ʵ��ʽ��0-����ʵ��1-������ʾ��2-������ʾ��3-������ʾ��
	 * @param rotation
	 *            ��ת�Ƕȣ���ѡ���� 0��90��180��270
	 * @param cellwidth
	 *            ��Ԫ���
	 * @param algin
	 *            ���뷽ʽ
	 * @param content
	 *            ��������
	 * @return ����byte[]
	 */
	public byte[] QiRui_DrawBar(int x, int y, int type, int height, int hri, int rotation, int cellwidth,
			String content) {
		String CodeType = "128";
		switch (type) {

		case 0:
			CodeType = "128";
			break;
		case 1:
			CodeType = "39";
			break;
		case 2:
			CodeType = "93";
			break;
		case 3:
			CodeType = "ITF";
			break;
		case 4:
			CodeType = "UPCA";
			break;
		case 5:
			CodeType = "UPCE";
			break;
		case 6:
			CodeType = "CODABAR";
			break;
		case 7:
			CodeType = "EAN8";
			break;
		case 8:
			CodeType = "EAN13";
			break;
		// case 9:
		// CodeType="128M";
		// break;

		}
		String cmd = "BARCODE " + x + "," + y + "," + "\"" + CodeType + "\"" + "," + height + "," + hri + "," + rotation
				+ "," + cellwidth + "," + cellwidth + "," + "\"" + content + "\"\r\n";

		try {
			return cmd.getBytes("gb2312");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private String sQiRui_DrawBar(int x, int y, int type, int height, int hri, int rotation, int cellwidth,
			String content) {
		String CodeType = "128";
		switch (type) {

		case 0:
			CodeType = "128";
			break;
		case 1:
			CodeType = "39";
			break;
		case 2:
			CodeType = "93";
			break;
		case 3:
			CodeType = "ITF";
			break;
		case 4:
			CodeType = "UPCA";
			break;
		case 5:
			CodeType = "UPCE";
			break;
		case 6:
			CodeType = "CODABAR";
			break;
		case 7:
			CodeType = "EAN8";
			break;
		case 8:
			CodeType = "EAN13";
			break;
		// case 9:
		// CodeType="128M";
		// break;

		}
		String cmd = "BARCODE " + x + "," + y + "," + "\"" + CodeType + "\"" + "," + height + "," + hri + "," + rotation
				+ "," + cellwidth + "," + cellwidth + "," + "\"" + content + "\"\r\n";

		return cmd;
	}

	/**
	 * ��ӡ��ά����
	 * 
	 * @param x
	 *            ��ʼx����
	 * @param y
	 *            ��ʼy����
	 * @param ecc
	 *            ����ȼ���0��L��1��M��2-Q��3-H
	 * @param rotation
	 *            ��ת�Ƕȣ���ѡ���� 0��90��180��270
	 * @param cellwidth
	 *            ��Ԫ���
	 * @param content
	 *            ��������
	 * @return ����byte[]
	 */
	public byte[] QiRui_DrawQRCode(int x, int y, int ecc, int rotation, int cellwidth, String content) {
		String strECC = "Q";
		switch (ecc) {
		case 0:
			strECC = "L";
			break;
		case 1:
			strECC = "M";
			break;
		case 2:
			strECC = "Q";
			break;
		case 3:
			strECC = "H";
			break;

		}
		String cmd = "QRCODE " + x + "," + y + "," + strECC + "," + cellwidth + ",A," + rotation + ",M2,S7," + "\""
				+ content + "\"\r\n";

		try {
			return cmd.getBytes("gb2312");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	// QRCODE 420,420, H, 4, A, 0, M1, S0, V8, "www.qrprt.com"
	/**
	 * ��ӡ��ά����(����)
	 * 
	 * @param x
	 *            ��ʼx����
	 * @param y
	 *            ��ʼy����
	 * @param ecc
	 *            ����ȼ���0��L��1��M��2-Q��3-H
	 * @param rotation
	 *            ��ת�Ƕȣ���ѡ���� 0��90��180��270
	 * @param cellwidth
	 *            ��Ԫ���
	 * @param Ver
	 *            ��ά��汾�ţ���ΧΪ0��40
	 * @param content
	 *            ��������
	 * @return ����byte[]
	 */
	public byte[] QiRui_DrawQRCode(int x, int y, int ecc, int rotation, int cellwidth, int Version, String content) {
		String strECC = "Q";
		if ((Version < 0) || (Version > 40)) {
			return null;
		}
		switch (ecc) {
		case 0:
			strECC = "L";
			break;
		case 1:
			strECC = "M";
			break;
		case 2:
			strECC = "Q";
			break;
		case 3:
			strECC = "H";
			break;

		}
		String cmd = "QRCODE " + x + "," + y + "," + strECC + "," + cellwidth + ",A," + rotation + ",M2,S7,V" + Version
				+ "," + "\"" + content + "\"\r\n";

		try {
			return cmd.getBytes("gb2312");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private String sQiRui_DrawQRCode(int x, int y, int ecc, int rotation, int cellwidth, String content) {
		String strECC = "Q";
		switch (ecc) {
		case 0:
			strECC = "L";
			break;
		case 1:
			strECC = "M";
			break;
		case 2:
			strECC = "Q";
			break;
		case 3:
			strECC = "H";
			break;

		}
		String cmd = "QRCODE " + x + "," + y + "," + strECC + "," + cellwidth + ",A," + rotation + ",M2,S7," + "\""
				+ content + "\"\r\n";

		return cmd;
	}

	/**
	 * ��ӡλͼ
	 * 
	 * @param x
	 *            ��ʼx����
	 * @param y
	 *            ��ʼy����
	 * @param bitmap
	 *            bitmapͼƬ
	 * @return ����byte[]
	 */
	public byte[] QiRui_DrawPic(int x, int y, Bitmap bitmap) {
		int bytewidth;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		if (width % 8 != 0)
			bytewidth = width / 8 + 1;
		else
			bytewidth = width / 8;

		String cmd = "BITMAP " + x + "," + y + "," + bytewidth + "," + height + ",1,";
		byte[] bcmd;
		try {
			bcmd = cmd.getBytes("gb2312");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			bcmd = null;
		}

		byte[] pixel = new byte[width * height];
		int comlen = 0;
		byte temp;

		// ѭ����
		for (int i = 0; i < height; i++) {
			// ѭ�����
			for (int j = 0; j < bytewidth; j++) {

				temp = 0;

				for (int k = 0; k < 8; k++) {
					if ((j * 8 + k) < width) {
						int pixelColor = bitmap.getPixel(j * 8 + k, i);
						if ((pixelColor != -1) && pixelColor != 0)
							temp |= (byte) (128 >> k);
					}

				}
				pixel[comlen++] = temp;

			}
		}

		byte[] totalcmd = new byte[bcmd.length + pixel.length];
		System.arraycopy(bcmd, 0, totalcmd, 0, bcmd.length);
		System.arraycopy(pixel, 0, totalcmd, bcmd.length, pixel.length);

		return totalcmd;
	}

	/**
	 * ��ӡ�߿�
	 * 
	 * @param sx
	 *            ���Ͻ���ʼ��x����
	 * @param sy
	 *            ���Ͻ���ʼ��y����
	 * @param ex
	 *            ���½ǽ�����x����
	 * @param ey
	 *            ���½ǽ�����y����
	 * @param thickness
	 *            �߿�
	 * @param radius
	 *            Բ�ǰ뾶
	 * @return ����byte[]
	 */
	public byte[] QiRui_DrawBox(int sx, int sy, int ex, int ey, int thickness, int radius) {

		String cmd = "BOX " + sx + "," + sy + "," + ex + "," + ey + "," + thickness + "," + radius + "\r\n";

		try {
			return cmd.getBytes("gb2312");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * ��Բ
	 * 
	 * @param x
	 *            Բ��x����
	 * @param y
	 *            Բ��y����
	 * @param radius
	 *            �뾶
	 * @param thinkness
	 *            �߿�
	 * @return ����byte[]
	 */
	public byte[] QiRui_DrawCircle(int x, int y, int radius, int thinkness) {

		String cmd = "CIRCLE " + x + "," + y + "," + radius + "," + thinkness + "\r\n";

		try {
			return cmd.getBytes("gb2312");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * ����
	 * 
	 * @param filename
	 *            �ļ�·��
	 * @return ����byte[]
	 */
	public byte[] QiRui_Update(String filename) {

		try {
			return filename.getBytes("gb2312");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * ����Ũ��
	 * 
	 * @param density
	 *            Ũ��ֵ ��0~15��
	 * @return ����byte[]
	 */
	public byte[] QiRui_Density(int density) {
		String sDensity = "DENSITY " + density + "\r\n";
		try {
			return sDensity.getBytes("gb2312");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * �����ٶ�
	 * 
	 * @param speed
	 *            �ٶ�ֵ ����Χ1��1.5��2��2.5��6��,��������3��4����
	 * @return ����byte[]
	 */
	public byte[] QiRui_Speed(int speed) {
		String sDensity = "SPEED " + speed + "\r\n";
		try {
			return sDensity.getBytes("gb2312");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * ���ҳ�滺����
	 * 
	 * @return ����byte[]
	 */
	public byte[] QiRui_Cls() {
		String cmd = "CLS" + "\r\n";
		try {
			return cmd.getBytes("gb2312");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private String sQiRui_Cls() {
		String cmd = "CLS" + "\r\n";
		return cmd;
	}

	/**
	 * ʼ���е�
	 * 
	 * @param isCut
	 *            true������һ������ֽ��false������ֽ
	 * @return ����byte[]
	 */
	public byte[] QiRui_Cut(boolean isCut) {
		String cmd;
		if (isCut) {
			cmd = "SET CUTTER 1" + "\r\n";
			try {
				return cmd.getBytes("gb2312");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

		} else {
			cmd = "SET CUTTER OFF" + "\r\n";
			try {
				return cmd.getBytes("gb2312");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

		}

	}

	private String sQiRui_Cut(boolean isCut) {
		String cmd;
		if (isCut) {
			cmd = "SET CUTTER 1" + "\r\n";
		} else {
			cmd = "SET CUTTER OFF" + "\r\n";
		}
		return cmd;

	}

	/**
	 * ���ô�ӡ����
	 * 
	 * @param direction
	 *            ��ӡ����0-�϶��ȳ���1���¶��ȳ�
	 * @param mirror
	 *            ���þ���0-������1-����
	 * @return ����byte[]
	 */
	public byte[] QiRui_Direction(int direction, int mirror) {
		String cmd = "DIRECTION " + direction + "," + mirror + "\r\n";
		try {
			return cmd.getBytes("gb2312");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private String sQiRui_Direction(int direction, int mirror) {
		String cmd = "DIRECTION " + direction + "," + mirror + "\r\n";
		return cmd;
	}

	/**
	 * ���ô�ӡ��϶�λ����϶
	 * 
	 * @param isEnable
	 *            true����λ����϶��false������λ
	 * @return ����byte[]
	 */
	public byte[] QiRui_SetGap(boolean isEnable) {
		String cmd;
		if (isEnable) {
			cmd = "SET GAP ON" + "\r\n";
			try {
				return cmd.getBytes("gb2312");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		} else {
			cmd = "SET GAP OFF" + "\r\n";
			try {
				return cmd.getBytes("gb2312");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}

	}

	private String sQiRui_SetGap(boolean isEnable) {
		String cmd;
		if (isEnable) {
			cmd = "SET GAP ON" + "\r\n";
		} else {
			cmd = "SET GAP OFF" + "\r\n";

		}
		return cmd;

	}

	/**
	 * ��ӡ��ά����DATAMATRIX
	 * 
	 * @param x
	 *            ��ʼx����
	 * @param y
	 *            ��ʼy����
	 * @param width
	 *            ������
	 * @param hight
	 *            ����߶�
	 * @param content
	 *            ��������
	 * @return ����byte[]
	 */
	public byte[] QiRui_DrawDataMatrix(int x, int y, int width, int hight, String content) {
		String cmd = "DMATRIX " + x + "," + y + "," + width + "," + hight + "," + "\"" + content + "\"\r\n";
		try {
			return cmd.getBytes("gb2312");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * ��ѯ��ӡ��״̬
	 * 
	 * @param mFileInputStream
	 *            ������
	 * @param FileOutputStream
	 *            �����
	 * @return 0-��ѯʧ�� 1-��ѯ�ɹ�
	 */
	public int QiRui_PrinterState(FileInputStream mFileInputStream, FileOutputStream mFileOutputStream) {
		try {

			int count = mFileInputStream.available();
			if (count > 0) {
				byte[] tt;
				tt = new byte[count];
				int z;
				while ((z = mFileInputStream.read(tt, 0, tt.length)) != -1) {
					break;
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.PaperState = 0;
		this.IsBusy = true;
		this.Picked = false;
		this.CutState = false;

		String cmd = "READSTA " + "\r\n";

		try {
			mFileOutputStream.write(cmd.getBytes("gb2312"));
			Thread.sleep(500);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			int _count = mFileInputStream.available();
			if (_count > 0) {
				byte[] _tt;
				_tt = new byte[_count];
				int _z;
				while ((_z = mFileInputStream.read(_tt, 0, _tt.length)) != -1) {
					String str = new String(_tt, "gb2312");
					String[] state = str.split("[ ,]");
					// ���ֽ��

					if (state[1].equals("LIBOPEN")) {
						this.PaperState = 3;

					} else if (state[1].equals("NOPAPER")) {
						this.PaperState = 0;

					} else if (state[1].equals("PAPEREND")) {
						this.PaperState = 2;

					} else if (state[1].equals("PAPER")) {
						this.PaperState = 1;

					} else if (state[1].equals("PAPERERR")) {
						this.PaperState = 4;

					}

					// �����ȡ
					if (state[2].equals("WAITPICK")) {
						this.Picked = false;
					} else if (state[2].equals("PICK")) {
						this.Picked = true;
					}
					// ����е�
					if (state[4].equals("CUTERERR")) {
						this.CutState = false;
					} else if (state[4].equals("CUTEROK")) {
						this.CutState = true;
					}
					// ���busy
					if (state[3].equals("IDLE")) {
						this.IsBusy = false;
					} else if (state[3].equals("BUSY")) {
						this.IsBusy = true;
					}

					return 1;
				}
			} else {
				return 0;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;

	}

	/**
	 * ����˵��Ƿ�ȡ�ߣ����أ�,������ִ��QiRui_PrinterState����
	 * 
	 * @return boolean���ͣ� true��ʾ��ȡ�ߣ�false��ʾδȡ��
	 */
	public boolean QiRui_isPicked() {
		return this.Picked;
	}

	/**
	 * ����е�״̬�����أ�,������ִ��QiRui_PrinterState����
	 * 
	 * @return boolean���ͣ� true��ʾOK��false��ʾERROR
	 */
	public boolean QiRui_CheckCut() {
		return this.CutState;
	}

	/**
	 * ����ӡ���Ƿ�Ϊæ�����أ�,������ִ��QiRui_PrinterState����
	 * 
	 * @return boolean���ͣ� true��ʾæ��false��ʾnone
	 */
	public boolean QiRui_isBusy() {
		return this.IsBusy;
	}

	/**
	 * ��ѯ��ӡ��SN���
	 * 
	 * @return String
	 */
	public String QiRui_GetSN(FileInputStream mFileInputStream, FileOutputStream mFileOutputStream) {

		try {

			int _count = mFileInputStream.available();
			if (_count > 0) {
				byte[] _tt;
				_tt = new byte[_count];
				int _z;
				while ((_z = mFileInputStream.read(_tt, 0, _tt.length)) != -1) {
					break;
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String cmd = "READC PRDOCUTID" + "\r\n";

		try {
			mFileOutputStream.write(cmd.getBytes("gb2312"));

			Thread.sleep(1000);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {

			int count = mFileInputStream.available();
			if (count > 0) {
				byte[] tt;

				tt = new byte[count];
				int z;
				while ((z = mFileInputStream.read(tt, 0, tt.length)) != -1) {
					String str = new String(tt, "gb2312");
					return str;

				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * ��ѯ��ӡ������汾��
	 * 
	 * @return String
	 */
	public String QiRui_GetVersion(FileInputStream mFileInputStream, FileOutputStream mFileOutputStream) {

		try {

			int _count = mFileInputStream.available();
			if (_count > 0) {
				byte[] _tt;
				_tt = new byte[_count];
				int _z;
				while ((_z = mFileInputStream.read(_tt, 0, _tt.length)) != -1) {
					break;
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String cmd = "READC VERSION " + "\r\n";

		try {
			mFileOutputStream.write(cmd.getBytes("gb2312"));

			Thread.sleep(1000);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {

			int count = mFileInputStream.available();
			if (count > 0) {
				byte[] tt;

				tt = new byte[count];
				int z;
				while ((z = mFileInputStream.read(tt, 0, tt.length)) != -1) {
					String str = new String(tt, "gb2312");
					return str;

				}
			} else {
				return null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * ���ֽ��״̬
	 * 
	 * @return 0-ȱֽ��1-��ֽ��2-ֽ������3-ֽ�ոǴ�;4-��ֽ��ֽ�Ŵ���
	 */
	public int QiRui_CheckPaper(FileInputStream mFileInputStream, FileOutputStream mFileOutputStream) {

		try {

			int _count = mFileInputStream.available();
			if (_count > 0) {
				byte[] _tt;
				_tt = new byte[_count];
				int _z;
				while ((_z = mFileInputStream.read(_tt, 0, _tt.length)) != -1) {
					break;
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String cmd = "READSTA " + "\r\n";

		try {
			mFileOutputStream.write(cmd.getBytes("gb2312"));
			Thread.sleep(200);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			int count = mFileInputStream.available();
			if (count > 0) {
				byte[] tt;
				tt = new byte[count];
				int z;
				while ((z = mFileInputStream.read(tt, 0, tt.length)) != -1) {
					String str = new String(tt, "gb2312");
					String[] state = str.split("[ ,]");
					if (state[1].equals("LIBOPEN")) {
						return 3;
					} else if (state[1].equals("NOPAPER")) {
						return 0;
					} else if (state[1].equals("PAPEREND")) {
						return 2;
					} else if (state[1].equals("PAPER")) {
						return 1;
					} else if (state[1].equals("PAPERERR")) {
						return 4;
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;

	}

	/**
	 * ����˵��Ƿ�ȡ��
	 * 
	 * @return boolean���ͣ� true��ʾ��ȡ�ߣ�false��ʾδȡ��
	 */
	public boolean QiRui_isPicked(FileInputStream mFileInputStream, FileOutputStream mFileOutputStream) {

		try {

			int _count = mFileInputStream.available();
			if (_count > 0) {
				byte[] _tt;
				_tt = new byte[_count];
				int _z;
				while ((_z = mFileInputStream.read(_tt, 0, _tt.length)) != -1) {
					break;
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String cmd = "READSTA " + "\r\n";

		try {
			mFileOutputStream.write(cmd.getBytes("gb2312"));

			Thread.sleep(200);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {

			int count = mFileInputStream.available();
			if (count > 0) {
				byte[] tt;

				tt = new byte[count];
				int z;
				while ((z = mFileInputStream.read(tt, 0, tt.length)) != -1) {
					String str = new String(tt, "gb2312");
					String[] state = str.split("[ ,]");
					if (state[2].equals("WAITPICK")) {
						return false;
					} else if (state[2].equals("PICK")) {
						return true;
					}

				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;

	}

	/**
	 * ����е�״̬
	 * 
	 * @return boolean���ͣ� true��ʾOK��false��ʾERROR
	 */
	public boolean QiRui_CheckCut(FileInputStream mFileInputStream, FileOutputStream mFileOutputStream) {

		try {

			int _count = mFileInputStream.available();
			if (_count > 0) {
				byte[] _tt;
				_tt = new byte[_count];
				int _z;
				while ((_z = mFileInputStream.read(_tt, 0, _tt.length)) != -1) {
					break;
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String cmd = "READSTA " + "\r\n";

		try {
			mFileOutputStream.write(cmd.getBytes("gb2312"));

			Thread.sleep(200);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {

			int count = mFileInputStream.available();
			if (count > 0) {
				byte[] tt;

				tt = new byte[count];
				int z;
				while ((z = mFileInputStream.read(tt, 0, tt.length)) != -1) {
					String str = new String(tt, "gb2312");
					String[] state = str.split("[ ,]");
					if (state[4].equals("CUTERERR")) {
						return false;
					} else if (state[4].equals("CUTEROK")) {
						return true;
					}

				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;

	}

	/**
	 * ����ӡ���Ƿ�Ϊæ
	 * 
	 * @return boolean���ͣ� true��ʾæ��false��ʾnone
	 */
	public boolean QiRui_isBusy(FileInputStream mFileInputStream, FileOutputStream mFileOutputStream) {

		try {

			int _count = mFileInputStream.available();
			if (_count > 0) {
				byte[] _tt;
				_tt = new byte[_count];
				int _z;
				while ((_z = mFileInputStream.read(_tt, 0, _tt.length)) != -1) {
					break;
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String cmd = "READSTA " + "\r\n";

		try {
			mFileOutputStream.write(cmd.getBytes("gb2312"));

			Thread.sleep(200);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {

			int count = mFileInputStream.available();
			if (count > 0) {
				byte[] tt;

				tt = new byte[count];
				int z;
				while ((z = mFileInputStream.read(tt, 0, tt.length)) != -1) {
					String str = new String(tt, "gb2312");
					String[] state = str.split("[ ,]");
					if (state[3].equals("IDLE")) {
						return false;
					} else if (state[3].equals("BUSY")) {
						return true;
					}

				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;

	}

	// optCodeֵ ��д ָ��˵��
	// 0 OPT_CODE_NOP ��ָ��, ��ʵ�ʺ���
	// 10 OPT_CODE_READY ׼��ָ��, ����Ϊһ��ָ��list�е�firstָ��,��ʾԤ����ӡ
	// 11 OPT_CODE_OVER ���ָ��,
	// ����Ϊһ��ָ��list�е�lastָ��,��ʾ�����ָ���,�ȴ���ӡ����ӡ
	// 12 OPT_CODE_TEXT ����
	// 13 OPT_CODE_LINE �߶�
	// 14 OPT_CODE_BARCODE һά����
	// 15 OPT_CODE_QRCODE ��ά����
	// 16 OPT_CODE_BITMAP ����ͼ��

	private static final int OPT_CODE_NOP = 0;
	private static final int OPT_CODE_READY = 10;
	private static final int OPT_CODE_OVER = 11;
	private static final int OPT_CODE_TEXT = 12;
	private static final int OPT_CODE_LINE = 13;
	private static final int OPT_CODE_BARCODE = 14;
	private static final int OPT_CODE_QRCODE = 15;
	private static final int OPT_CODE_BITMAP = 16;

	/**
	 * json����ת��
	 * 
	 * @param String
	 *            jsonstring json�ַ�
	 * @return List<byte[]>
	 */
	public List<byte[]> QiRui_ParserFCboxJsonToQR(String jsonstring) {
		List<byte[]> dataList = new ArrayList<byte[]>();
		String TSPLresult = "";
		int LabelEndFlag = 0;
		try {
			JSONObject result = new JSONObject(jsonstring);// ת��ΪJSONObject
			// int num = result.length();
			JSONArray nameList = result.getJSONArray("opts");// ��ȡJSONArray
			int length = nameList.length();
			int optCode, fontType, beginX, beginY, endX, endY, width, ratio, height, qrVersion, unitHeight, level;
			String content;
			boolean isBold;
			for (int i = 0; i < length; i++) {// ����JSONArray
				content = "";
				optCode = fontType = beginX = beginY = endX = endY = width = ratio = height = qrVersion = unitHeight = level = 0;
				isBold = false;
				JSONObject oj = nameList.getJSONObject(i);
				optCode = oj.getInt("optCode");
				switch (optCode) {
				case OPT_CODE_NOP: // ��ָ��, ��ʵ�ʺ���
					break;
				case OPT_CODE_READY:// ׼��ָ��,
									// ����Ϊһ��ָ��list�е�firstָ��,��ʾԤ����ӡ
					// TSPLresult+=sQiRui_CreatePage(100,180);
					// TSPLresult+=sQiRui_Direction(0,0);
					// TSPLresult+=sQiRui_SetGap(true);
					// TSPLresult+=sQiRui_Cut(true);

					dataList.add(QiRui_CreatePage(100, 180));
					dataList.add(QiRui_Direction(0, 0));
					dataList.add(QiRui_SetGap(true));
					dataList.add(QiRui_Cut(true));
					break;
				case OPT_CODE_OVER: // ���ָ��,
									// ����Ϊһ��ָ��list�е�lastָ��,��ʾ�����ָ���,�ȴ���ӡ����ӡ
					LabelEndFlag = 1;
					// TSPLresult+=sQiRui_PrintPage(1);
					dataList.add(QiRui_PrintPage(1));
					break;
				// ֵ ��д ˵��
				// 1 FONT_TYPE_MATRX_16x16 16x16���ĵ��� ����Ӣ��Ϊ8x16
				// 2 FONT_TYPE_MATRX_24x24 24x24���ĵ��� ����Ӣ��Ϊ12x24
				// 3 FONT_TYPE_MATRX_24x48 24x48����Ӣ�ĵ���
				// 4 FONT_TYPE_MATRX_36x36 36x36���ĵ��� ����Ӣ��Ϊ18x36
				// 5 FONT_TYPE_MATRX_48x48 48x48���ĵ��� ����Ӣ��Ϊ24x48
				// 6 FONT_TYPE_MATRX_72x72 72x72���ĵ��� ����Ӣ��Ϊ36x72
				// {TEXT
				// "optCode": 12,
				// "x": 56,
				// "y": 150,
				// "fontType": 2,
				// "isBold": true,
				// "content": "ʼ������:"
				// },
				case OPT_CODE_TEXT:// ����
					beginX = oj.getInt("x");
					beginY = oj.getInt("y");
					fontType = oj.getInt("fontType");
					isBold = oj.getBoolean("isBold");
					content = oj.getString("content");
					String font = "TSS24.BF2";
					int xmulti = 0, ymulti = 0;
					if (2 == fontType) {
						font = "TSS24.BF2";
					} else if (1 == fontType) {
						font = "TSS16.BF2";
					} else if (3 == fontType) {
						font = "TSS24.BF2";
						xmulti = 1;
						ymulti = 2;
					} else if (4 == fontType) {
						font = "TSS32.BF2";
					} else if (5 == fontType) {
						font = "TSS24.BF2";
						xmulti = 2;
						ymulti = 2;
					} else if (6 == fontType) {
						font = "TSS24.BF2";
						xmulti = 3;
						ymulti = 3;
					}
					// TSPLresult+=sQiRui_Text(beginX,beginY,font,0,xmulti,ymulti,isBold,content);
					dataList.add(QiRui_Text(beginX, beginY, font, 0, xmulti, ymulti, isBold, content));
					break;
				// ���� ���� ˵��
				// beginX int x����ʼ����
				// beginY int y����ʼ����
				// endX int x���������
				// endY bool y���������
				// width string �߶ο��

				// {//LINE
				// "optCode": 13,
				// "beginX": 40,
				// "beginY": 40,
				// "endX": 760,
				// "endY": 40,
				// "width": 1
				// },
				case OPT_CODE_LINE:// �߶�
					beginX = oj.getInt("beginX");
					beginY = oj.getInt("beginY");
					endX = oj.getInt("endX");
					endY = oj.getInt("endY");
					width = oj.getInt("width");
					// TSPLresult+=sQiRui_DrawLine(beginX,beginY,endX,endY,width+1,0);
					dataList.add(QiRui_DrawLine(beginX, beginY, endX, endY, width + 1, 0));
					break;

				// ���� ���� ˵��
				// barcodeType int 1��ʾCode 128 (Auto) ������
				// width int խ����Ŀ�ȵ���
				// ratio double �������խ����ı���
				// height int ����ĸ߶ȵ���
				// x int ���뿪ʼ��X������
				// y int ���뿪ʼ��Y������
				// number string ��������
				// {
				// "optCode": 14,
				// "barcodeType": 1,
				// "width": 2,
				// "ratio": 2,
				// "height": 36,
				// "x": 472,
				// "y": 340,
				// "number": "023000"
				// },
				case OPT_CODE_BARCODE:// һά����
					beginX = oj.getInt("x");
					beginY = oj.getInt("y");
					width = oj.getInt("width");
					ratio = oj.getInt("ratio");
					height = oj.getInt("height");
					content = oj.getString("number");
					// TSPLresult+=sQiRui_DrawBar(beginX,beginY,0,height,0,0,3,content);
					dataList.add(QiRui_DrawBar(beginX, beginY, 0, height, 0, 0, 3, content));
					break;
				// ���� ���� ˵��
				// x int ���뿪ʼ��X������
				// y int ���뿪ʼ��Y������
				// qrVersion int QR�����ͺŰ汾
				// �汾1��QR��1����21����Ԫ���ɡ��汾ÿ��һ��������QR��һ�ߵ���Ԫ������4��
				// unitHeight int QR��Ԫ�ĸ߶ȵ��� Ҳ�ǿ�ȵ���
				// level int �ݴ��ʵȼ�
				// 1��ʾ7% �ݴ���
				// 2��ʾ15% �ݴ���
				// 3��ʾ25% �ݴ���
				// 4��ʾ30% �ݴ���
				// content string ��ά������
				// {
				// "optCode": 15,
				// "x": 56,
				// "y": 428,
				// "qrVersion": 2,
				// "unitHeight": 6,
				// "level": 2,
				// "content": "3100584506450530001"
				// },
				case OPT_CODE_QRCODE:// ��ά����
					beginX = oj.getInt("x");
					beginY = oj.getInt("y");
					qrVersion = oj.getInt("qrVersion");
					unitHeight = oj.getInt("unitHeight");
					level = oj.getInt("level");
					content = oj.getString("content");
					// TSPLresult+=sQiRui_DrawQRCode(beginX,beginY,level-1,0,4,content);
					dataList.add(QiRui_DrawQRCode(beginX, beginY, level, 0, unitHeight, qrVersion, content));
					break;
				// ���� ���� ˵��
				// width int ����ͼ���ȵ���
				// ����Ϊ8��������
				// height int ����ͼ��߶ȵ���
				// x int ����ͼ��ʼ��X������
				// y int ����ͼ��ʼ��Y������
				// octetStr int ͼ��ʮ�����Ƶ�������
				// {
				// "optCode": 16,
				// "width": 160,
				// "height": 55,
				// "x": 72,
				// "y": 900,
				// "octetStr": "0000000000....."
				// }
				case OPT_CODE_BITMAP:// ����ͼ��
					beginX = oj.getInt("x");
					beginY = oj.getInt("y");
					width = oj.getInt("width");
					height = oj.getInt("height");
					content = oj.getString("octetStr");
					TSPLresult += sQiRui_DrawPic(beginX, beginY, width, height, content);
					dataList.add(QiRui_DrawPic(beginX, beginY, width, height, content));
					break;

				}

			}

		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		if (LabelEndFlag == 1) {
			return dataList;
		} else {
			return null;
		}
	}

	/**
	 * json����ת��
	 * 
	 * @param JSONObject
	 *            result json����
	 * @return List<byte[]>
	 */
	public List<byte[]> QiRui_ParserFCboxJsonToQR(JSONObject result) {
		List<byte[]> dataList = new ArrayList<byte[]>();
		String TSPLresult = "";
		int LabelEndFlag = 0;
		try {
			// JSONObject result = new JSONObject(jsonstring);//ת��ΪJSONObject
			// int num = result.length();
			JSONArray nameList = result.getJSONArray("opts");// ��ȡJSONArray
			int length = nameList.length();
			int optCode, fontType, beginX, beginY, endX, endY, width, ratio, height, qrVersion, unitHeight, level;
			String content;
			boolean isBold;
			for (int i = 0; i < length; i++) {// ����JSONArray
				content = "";
				optCode = fontType = beginX = beginY = endX = endY = width = ratio = height = qrVersion = unitHeight = level = 0;
				isBold = false;
				JSONObject oj = nameList.getJSONObject(i);
				optCode = oj.getInt("optCode");
				switch (optCode) {
				case OPT_CODE_NOP: // ��ָ��, ��ʵ�ʺ���
					break;
				case OPT_CODE_READY:// ׼��ָ��,
									// ����Ϊһ��ָ��list�е�firstָ��,��ʾԤ����ӡ
					// TSPLresult+=sQiRui_CreatePage(100,180);
					// TSPLresult+=sQiRui_Direction(0,0);
					// TSPLresult+=sQiRui_SetGap(true);
					// TSPLresult+=sQiRui_Cut(true);

					dataList.add(QiRui_CreatePage(100, 180));
					dataList.add(QiRui_Direction(0, 0));
					dataList.add(QiRui_SetGap(true));
					dataList.add(QiRui_Cut(true));
					break;
				case OPT_CODE_OVER: // ���ָ��,
									// ����Ϊһ��ָ��list�е�lastָ��,��ʾ�����ָ���,�ȴ���ӡ����ӡ
					LabelEndFlag = 1;
					// TSPLresult+=sQiRui_PrintPage(1);
					dataList.add(QiRui_PrintPage(1));
					break;
				// ֵ ��д ˵��
				// 1 FONT_TYPE_MATRX_16x16 16x16���ĵ��� ����Ӣ��Ϊ8x16
				// 2 FONT_TYPE_MATRX_24x24 24x24���ĵ��� ����Ӣ��Ϊ12x24
				// 3 FONT_TYPE_MATRX_24x48 24x48����Ӣ�ĵ���
				// 4 FONT_TYPE_MATRX_36x36 36x36���ĵ��� ����Ӣ��Ϊ18x36
				// 5 FONT_TYPE_MATRX_48x48 48x48���ĵ��� ����Ӣ��Ϊ24x48
				// 6 FONT_TYPE_MATRX_72x72 72x72���ĵ��� ����Ӣ��Ϊ36x72
				// {TEXT
				// "optCode": 12,
				// "x": 56,
				// "y": 150,
				// "fontType": 2,
				// "isBold": true,
				// "content": "ʼ������:"
				// },
				case OPT_CODE_TEXT:// ����
					beginX = oj.getInt("x");
					beginY = oj.getInt("y");
					fontType = oj.getInt("fontType");
					isBold = oj.getBoolean("isBold");
					content = oj.getString("content");
					String font = "TSS24.BF2";
					int xmulti = 0, ymulti = 0;
					if (2 == fontType) {
						font = "TSS24.BF2";
					} else if (1 == fontType) {
						font = "TSS16.BF2";
					} else if (3 == fontType) {
						font = "TSS24.BF2";
						xmulti = 1;
						ymulti = 2;
					} else if (4 == fontType) {
						font = "TSS32.BF2";
					} else if (5 == fontType) {
						font = "TSS24.BF2";
						xmulti = 2;
						ymulti = 2;
					} else if (6 == fontType) {
						font = "TSS24.BF2";
						xmulti = 3;
						ymulti = 3;
					}
					// TSPLresult+=sQiRui_Text(beginX,beginY,font,0,xmulti,ymulti,isBold,content);
					dataList.add(QiRui_Text(beginX, beginY, font, 0, xmulti, ymulti, isBold, content));
					break;
				// ���� ���� ˵��
				// beginX int x����ʼ����
				// beginY int y����ʼ����
				// endX int x���������
				// endY bool y���������
				// width string �߶ο��

				// {//LINE
				// "optCode": 13,
				// "beginX": 40,
				// "beginY": 40,
				// "endX": 760,
				// "endY": 40,
				// "width": 1
				// },
				case OPT_CODE_LINE:// �߶�
					beginX = oj.getInt("beginX");
					beginY = oj.getInt("beginY");
					endX = oj.getInt("endX");
					endY = oj.getInt("endY");
					width = oj.getInt("width");
					// TSPLresult+=sQiRui_DrawLine(beginX,beginY,endX,endY,width+1,0);
					dataList.add(QiRui_DrawLine(beginX, beginY, endX, endY, width + 1, 0));
					break;

				// ���� ���� ˵��
				// barcodeType int 1��ʾCode 128 (Auto) ������
				// width int խ����Ŀ�ȵ���
				// ratio double �������խ����ı���
				// height int ����ĸ߶ȵ���
				// x int ���뿪ʼ��X������
				// y int ���뿪ʼ��Y������
				// number string ��������
				// {
				// "optCode": 14,
				// "barcodeType": 1,
				// "width": 2,
				// "ratio": 2,
				// "height": 36,
				// "x": 472,
				// "y": 340,
				// "number": "023000"
				// },
				case OPT_CODE_BARCODE:// һά����
					beginX = oj.getInt("x");
					beginY = oj.getInt("y");
					width = oj.getInt("width");
					ratio = oj.getInt("ratio");
					height = oj.getInt("height");
					content = oj.getString("number");
					// TSPLresult+=sQiRui_DrawBar(beginX,beginY,0,height,0,0,3,content);
					dataList.add(QiRui_DrawBar(beginX, beginY, 0, height, 0, 0, 3, content));
					break;
				// ���� ���� ˵��
				// x int ���뿪ʼ��X������
				// y int ���뿪ʼ��Y������
				// qrVersion int QR�����ͺŰ汾
				// �汾1��QR��1����21����Ԫ���ɡ��汾ÿ��һ��������QR��һ�ߵ���Ԫ������4��
				// unitHeight int QR��Ԫ�ĸ߶ȵ��� Ҳ�ǿ�ȵ���
				// level int �ݴ��ʵȼ�
				// 1��ʾ7% �ݴ���
				// 2��ʾ15% �ݴ���
				// 3��ʾ25% �ݴ���
				// 4��ʾ30% �ݴ���
				// content string ��ά������
				// {
				// "optCode": 15,
				// "x": 56,
				// "y": 428,
				// "qrVersion": 2,
				// "unitHeight": 6,
				// "level": 2,
				// "content": "3100584506450530001"
				// },
				case OPT_CODE_QRCODE:// ��ά����
					beginX = oj.getInt("x");
					beginY = oj.getInt("y");
					qrVersion = oj.getInt("qrVersion");
					unitHeight = oj.getInt("unitHeight");
					level = oj.getInt("level");
					content = oj.getString("content");
					// TSPLresult+=sQiRui_DrawQRCode(beginX,beginY,level-1,0,4,content);
					dataList.add(QiRui_DrawQRCode(beginX, beginY, level, 0, unitHeight, qrVersion, content));
					break;
				// ���� ���� ˵��
				// width int ����ͼ���ȵ���
				// ����Ϊ8��������
				// height int ����ͼ��߶ȵ���
				// x int ����ͼ��ʼ��X������
				// y int ����ͼ��ʼ��Y������
				// octetStr int ͼ��ʮ�����Ƶ�������
				// {
				// "optCode": 16,
				// "width": 160,
				// "height": 55,
				// "x": 72,
				// "y": 900,
				// "octetStr": "0000000000....."
				// }
				case OPT_CODE_BITMAP:// ����ͼ��
					beginX = oj.getInt("x");
					beginY = oj.getInt("y");
					width = oj.getInt("width");
					height = oj.getInt("height");
					content = oj.getString("octetStr");
					TSPLresult += sQiRui_DrawPic(beginX, beginY, width, height, content);
					dataList.add(QiRui_DrawPic(beginX, beginY, width, height, content));
					break;

				}

			}

		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		if (LabelEndFlag == 1) {
			return dataList;
		} else {
			return null;
		}
	}

}
