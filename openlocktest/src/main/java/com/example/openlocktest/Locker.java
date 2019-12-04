package com.example.openlocktest;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import android_serialport_api.SerialPort;

public class Locker {
	private Context mBase;
	private static final String TAG = "zyz";
	private SerialPort lock;
	private boolean bb;

	private static final int FLAG_TEST_BIN = 20;

	private static Locker instance = null;

//	public static Locker getInstance() {
//		if (instance == null) {
//			instance = new Locker();
//		}
//		return instance;
//	}

	// 锁控版命令
	private byte[] lockOrder = null;

	private String boxId = "";

	private String[] batchBoxId;
	private boolean[] HasOpened;

	private int n = -1;

	private int iNum;

	// 是否正在进行其他操作
	private boolean isBusy = false;

	private ArrayList<String> list = new ArrayList<String>();
	private String[] boxOpened;

	private ArrayList<String> code1 = new ArrayList<String>();
	private ArrayList<String> code2 = new ArrayList<String>();

	private byte[] LISHIZHI = null;

	// 485抄表
	private final static String Query485 = "pj.xing.meter";

	// 查询温度
	private final static String Temp = "pj.xing.temp";
	// 查询当前温度补偿
	private final static String queryTemp = "pj.xing.queryTemp";

	// 查询湿度
	// private final static String Shidu = "pj.xing.shidu";
	// 查询当前湿度补偿
	private final static String queryShidu = "pj.xing.queryShidu";

	// 查询抄表485设置
	private final static String query485SET = "pj.xing.query485Setting";

	// 查询风扇A设置温度
	private final static String query12SET = "pj.xing.query12Setting";
	// 查询风扇B设置温度
	private final static String query25SET = "pj.xing.query25Setting";
	// 查询加热器设置温度
	private final static String query26SET = "pj.xing.query26Setting";
	// 门磁监测设置
	private final static String queryMCSET = "pj.xing.queryMCSetting";

	// 485抄表
	private static byte[] LINSHI485 = null;

	// 检查log
	private static boolean checkLogFile = true;

	// 测试开锁
	private final String[] sBox = { "Z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "0" };
	private String sBox2 = null;
	private StringBuffer sBuffer = new StringBuffer();
	private int currentI = 0;
	private String currentBox = null;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case 1: {
					new Thread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							for (String str : sBox) {
								currentI++;
								currentBox = str;
								Log.v(TAG, "currentI = " + currentI);
								queryLockTest(str + "01");
								try {
									Thread.sleep(1300);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}).start();
					break;
				}
//				case 2: {
//					try {
//						new Thread(new Runnable() {
//							@Override
//							public void run() {
//								// TODO Auto-generated method stub
//								while (sBox2.length() >= 1) {
//									Log.v(TAG, "========== + " + sBox2);
//									String s2 = sBox2.substring(0, 1);
//									if (s2.equals("Z")) {
//										for (int inum = 1; inum < 9; inum++) {
//											openLock(s2 + "0" + inum);
//											try {
//												Thread.sleep(1300);
//											} catch (InterruptedException e) {
//												// TODO Auto-generated catch block
//												e.printStackTrace();
//											}
//										}
//									} else {
//										for (int inum = 1; inum < 23; inum++) {
//											if (inum < 10) {
//												openLock(s2 + "0" + inum);
//											} else {
//												openLock(s2 + inum);
//											}
//											try {
//												Thread.sleep(1300);
//											} catch (InterruptedException e) {
//												// TODO Auto-generated catch block
//												e.printStackTrace();
//											}
//										}
//									}
//									sBox2 = sBox2.substring(1);
//								}
//								Intent intent = new Intent("pj.xing.locktest");
//								mBase.sendBroadcast(intent);
//								isBusy = false;
//							}
//						}).start();
//					} catch (Exception e) {
//						Intent intent = new Intent("pj.xing.locktest");
//						mBase.sendBroadcast(intent);
//						isBusy = false;
//					}
//
//					break;
//				}

				case -1: {
					break;
				}

				default:
					break;
			}
		};
	};


	public Locker(Context context,String node) {
		Constant.COM_LOCKER = node;
		mBase = context;
		try {
			lock = new SerialPort(new File(Constant.COM_LOCKER), Constant.LOCKER_baudrate, 0,
					new SerialPort.onDataReceivedListener() {
						@Override
						public void onDataReceived(byte[] buffer, int size) {
							Log.v(TAG,"n=="+n);

							if (LISHIZHI == null) {
								LISHIZHI = subBytes(buffer, 0, size);
							} else {
								LISHIZHI = byteMerger(LISHIZHI, subBytes(buffer, 0, size));
							}
                            //Constant.byteToHex("TEST 接收",LISHIZHI.length,LISHIZHI);
							/*如果接收到数据*/
							if(LISHIZHI.length>0){
								Constant.aFlag = true;
							}

							/*if (LISHIZHI.length == 1 && buffer[0] == 5) {
								// 如果接收的是超时反馈值
							} else {

							}*/

							/*{
								while (true) {
									if(LISHIZHI == null){
										break;
									}
									// 首字节不等于0xAA 丢弃
									while ((LISHIZHI.length > 2) && (LISHIZHI[0] != -86) && (LISHIZHI[1] != 0x55)) {
										LISHIZHI = subBytes(LISHIZHI, 1, LISHIZHI.length - 1);
									}

									if (LISHIZHI.length < 3) {
										// 接收的长度小于3
										lock.start();
										break;
									} else {

										if (n == 11) {
											while ((LISHIZHI.length >= 7) && (LISHIZHI.length >= (LISHIZHI[2] + 4))) {
												byte[] linshi = subBytes(LISHIZHI, 5, LISHIZHI[2] - 2);
												LISHIZHI = subBytes(LISHIZHI, (LISHIZHI[2] + 4),
														LISHIZHI.length - (LISHIZHI[2] + 4));

												if (LINSHI485 == null) {
													LINSHI485 = new byte[linshi.length];
													for (int i = 0; i < linshi.length; i++) {
														LINSHI485[i] = linshi[i];
													}
												} else {
													LINSHI485 = byteMerger(LINSHI485, linshi);
												}
											}
											if (LINSHI485 == null || LINSHI485.length != 22) {
												lock.start();
												return;
											} else {
												toAnalysis(n);
												break;
											}
										} else {
											// 接收到完整指令则解析
											if (LISHIZHI[2] == (LISHIZHI.length - 4)) {
												// 计算CRC8校验，并进行校验
												byte crc8 = Constant.calcCrc8(LISHIZHI, 0, LISHIZHI.length - 1);
												if (LISHIZHI[LISHIZHI.length - 1] == crc8) {
													// crc8校验通过
													toAnalysis(n);
													break;
												} else {
													// crc8校验错误，丢弃接收数据的第一个字节，并重新扫描
													LISHIZHI = subBytes(LISHIZHI, 1, LISHIZHI.length - 1);
												}
											} else if (LISHIZHI[2] > (LISHIZHI.length - 4)) {
												// 指令不完整，继续读取
												lock.start();
												break;
											} else {
												// 接收的数据长度超过完整指令长度
												LISHIZHI = subBytes(LISHIZHI, 0, LISHIZHI[2] + 4);
											}
										}
									}
								}
							}*/
						}
					}, 1300);
			Log.v(TAG, "===锁控版初始化成功!====");

		} catch (Exception e) {
			if(LISHIZHI != null){
				Constant.byteToHex("发生异常 ：", LISHIZHI.length, LISHIZHI);
				lock.stop();
				LISHIZHI = null;
			}
		}

	}

	private void toAnalysis(int n) {
		switch (n) {
			case FLAG_TEST_BIN:

				break;
			case 1: {
				/*if (LISHIZHI.length != 1) {
					Intent intent = new Intent("android.intent.action.hal.iocontroller.querydata");
					intent.putExtra("boxid", boxId);
					bb = doCheckResult(LISHIZHI);
					Log.i("zyz","bb=="+bb);
					intent.putExtra("isopened", bb);
					Constant.byteToHex(boxId + (bb ? " : 打开" : " : 关闭"), LISHIZHI.length, LISHIZHI);
					//mBase.sendBroadcast(intent);
				}
				LISHIZHI = null;
				isBusy = false;*/
//				String temp = new String(LISHIZHI);
//				Log.i("zyz","temp=="+temp);
				break;
			}
			case 3: {
				// 多路开锁/查询
				Log.v("pj", "======== iNum = " + iNum);
				try {
					if (doCheckResult(LISHIZHI)) {
						HasOpened[iNum] = true;
					} else {
						HasOpened[iNum] = false;
					}
				} catch (Exception e) {
					// TODO: handle exception
					e.getStackTrace();
				}
				LISHIZHI = null;
				if (iNum >= batchBoxId.length - 1) {
					Intent intent = new Intent("android.intent.action.hal.iocontroller.batchopen.result");
					intent.putExtra("batchboxid", batchBoxId);
					intent.putExtra("opened", HasOpened);
					mBase.sendBroadcast(intent);
					LISHIZHI = null;
					isBusy = false;
				}
				break;
			}
			case 5: {
				// 已打开格口
				System.out.println("i = " + iNum);
				if (LISHIZHI.length != 1) {
					if (doCheckResult(LISHIZHI)) {
						Log.v(TAG, " = ");
						list.add(batchBoxId[iNum]);
					}
				}
				LISHIZHI = null;
				if (iNum >= batchBoxId.length - 1) {
					boxOpened = list.toArray(new String[list.size()]);
					Intent intent = new Intent("android.intent.action.hal.iocontroller.queryAllData");
					intent.putExtra("openedBoxes", boxOpened);
					mBase.sendBroadcast(intent);
					isBusy = false;
					LISHIZHI = null;
				}
				break;
			}
			case 6: {
				// 查询资产编码
				if (LISHIZHI.length == 30) {
					try {
						String sss = new String(subBytes(LISHIZHI, 8, 21), "UTF-8");
						if (sss.contains("N")) {
							sss = sss.substring(1, 20);
						}
						code1.add(sss);
						code2.add(new String(subBytes(LISHIZHI, 5, 3), "UTF-8"));
						Log.v("PJ", code1.size() + ":" + code2.size());
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
				LISHIZHI = null;

				if (iNum >= 8) {
					assetCodeArrays = code1.toArray(new String[code1.size()]);
					boxIdArrays = code2.toArray(new String[code2.size()]);
					Intent intent = new Intent("android.intent.action.hal.boxinfo.result");
					if (assetCodeArrays != null && assetCodeArrays.length != 0) {
						intent.putExtra("array.assetCode", assetCodeArrays);
					}
					if (boxIdArrays != null && boxIdArrays.length != 0) {
						intent.putExtra("array.boxId", boxIdArrays);
						mBase.sendBroadcast(intent);
					}
					isBusy = false;
					LISHIZHI = null;
				}
				break;
			}
			case 7: {
				// 写入资产编码
				break;
			}
			case 8: {
				// 门磁监测
				if (LISHIZHI.length != 1) {
					if (isReceivedDataEquals(LISHIZHI)) {
						Intent intent = new Intent("android.intent.action.hal.guard.event");
						intent.putExtra("eventType", "BoxCoreOpened");
						intent.putExtra("eventArg", "");
						mBase.sendBroadcast(intent);
					}
				}

				LISHIZHI = null;

				break;
			}

			case 11: {
				// 485抄表
				Intent intent = new Intent(Query485);
				if ((LINSHI485 != null) && (LINSHI485.length == 22)) {
					if ((LINSHI485[0] != -2) && (LINSHI485[21] != 0x16)) {
						intent.putExtra("meter", "**");
					} else {
						intent.putExtra("meter", bcd2Str(subBytes(LINSHI485, 16, 4)));
					}
				} else {
					intent.putExtra("meter", "**");
				}
				mBase.sendBroadcast(intent);
				isBusy = false;
				LINSHI485 = null;
				LISHIZHI = null;
				break;
			}

			case 12: {
				// 获取温度
				if (LISHIZHI.length == 8) {
					byte t1 = LISHIZHI[5];
					byte t2 = LISHIZHI[6];
					int i1, i2;
					if (t1 < 0) {
						i1 = (256 + t1) << 8;
					} else {
						i1 = t1 << 8;
					}
					if (t2 < 0) {
						i2 = 256 + t2;
					} else {
						i2 = t2;
					}

					Log.v(TAG, i1 + "/" + i2);

					double f = (i1 + i2 - 500) / 10.0;
					BigDecimal b = new BigDecimal(f);
					double f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
					Log.v(TAG, "温度 = " + f1);
					Intent intent = new Intent(Temp);
					intent.putExtra("temp", f1 + "");
					mBase.sendBroadcast(intent);
				}
				isBusy = false;
				LISHIZHI = null;
				break;
			}

			case 13: {
				// 湿度
				isBusy = false;
				LISHIZHI = null;
				break;
			}

			case 14: {
				// 查询当前温度补偿
				if (LISHIZHI.length == 7) {
					byte b = LISHIZHI[5];
					Intent intent = new Intent(queryTemp);
					if ((b & 0x80) == 0x80) {
						intent.putExtra("temp", "-" + ((b & 0x7F) / 10));
					} else {
						intent.putExtra("temp", (b / 10) + "");
					}
					mBase.sendBroadcast(intent);
				}
				isBusy = false;
				LISHIZHI = null;
				break;
			}

			case 15: {
				// 查询当前湿度补偿
				if (LISHIZHI.length == 7) {
					byte b = LISHIZHI[5];
					Intent intent = new Intent(queryShidu);
					intent.putExtra("shidu", b + "");
					mBase.sendBroadcast(intent);
				}
				isBusy = false;
				LISHIZHI = null;
				break;
			}

			case 16: {
				if (LISHIZHI.length != 1) {
					sBuffer.append(currentBox);
					Log.v(TAG, "sBuffers = " + sBuffer.toString());
				}
				if (currentI > 15) {
					if (sBuffer.length() > 0) {
						sBox2 = sBuffer.toString();
						sBuffer.setLength(0);
						currentI = 0;
						Log.v(TAG, "发送开锁命令");
//						mHandler.sendEmptyMessage(2);
					} else {
						Intent intent = new Intent("pj.xing.locktest");
						isBusy = false;
						mBase.sendBroadcast(intent);
					}
				}
				LISHIZHI = null;
				break;
			}

			case 17: {
				if (LISHIZHI.length == 9) {
					Constant.Scom_parity = LISHIZHI[5] + 1;
					Constant.Scom_baudrate = LISHIZHI[6] + 1;
					Intent intent = new Intent(query485SET);
					mBase.sendBroadcast(intent);
				}
				isBusy = false;
				LISHIZHI = null;
				break;
			}

			case 18: {
				if (LISHIZHI.length == 10) {
					Intent intent = null;
					DecimalFormat df = new DecimalFormat("###.0");
					String temOpen = df
							.format((Integer.parseInt(byteToHexStr(2, subBytes(LISHIZHI, 5, 2)), 16) - 500) / 10.0);
					String temClose = df
							.format((Integer.parseInt(byteToHexStr(2, subBytes(LISHIZHI, 7, 2)), 16) - 500) / 10.0);
					switch (LISHIZHI[4]) {
						case 0x69:
							intent = new Intent(query12SET);
							intent.putExtra("temOpen", temOpen);
							intent.putExtra("temClose", temClose);
							mBase.sendBroadcast(intent);
							break;
						case 0x6A:
							intent = new Intent(query25SET);
							intent.putExtra("temOpen", temOpen);
							intent.putExtra("temClose", temClose);
							mBase.sendBroadcast(intent);
							break;
						case 0x6B:
							intent = new Intent(query26SET);
							intent.putExtra("temOpen", temOpen);
							intent.putExtra("temClose", temClose);
							mBase.sendBroadcast(intent);
							break;
						default:
							break;
					}
				}
				isBusy = false;
				LISHIZHI = null;
				break;
			}

			case 19: {
				if (LISHIZHI.length == 7) {
					Intent intent = new Intent(queryMCSET);
					if (LISHIZHI[5] == 0x00) {
						intent.putExtra("mcSet", true);
					} else {
						intent.putExtra("mcSet", false);
					}
					mBase.sendBroadcast(intent);
				}
				isBusy = false;
				LISHIZHI = null;
				break;
			}
			case -1: {
				isBusy = false;
				LISHIZHI = null;
			}
			default:
				isBusy = false;
				LISHIZHI = null;
				break;
		}
	}

	/**
	 * @功能: BCD码转为10进制串(阿拉伯数据)
	 * @参数: BCD码
	 * @结果: 10进制串
	 */
	public static String bcd2Str(byte[] bytes) {
		StringBuffer temp = new StringBuffer(bytes.length * 2);
		for (int i = bytes.length - 1; i >= 0; i--) {
			temp.append((byte) (((bytes[i] - 0x33) & 0xf0) >>> 4));
			temp.append((byte) ((bytes[i] - 0x33) & 0x0f));
			if (i == 1) {
				temp.append(".");
			}
		}
		String str2 = temp.toString();
		DecimalFormat df = new DecimalFormat("0.00");
		double d = Double.parseDouble(str2);
		return df.format(d);
	}

	private String byteToHexStr(int length, byte[] msg) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			byte b = msg[i];
			String str = Integer.toHexString(0xFF & b);
			if (str.length() == 1) {
				str = "0" + str;
			} else {
				str = "" + str;
			}

			sb.append(str);
		}
		// Log.i(TAG, "length = " + length + "," + sb.toString());
		return sb.toString();
	}

	/**
	 * 处理门磁查询返回值
	 *
	 * @param -1为开，0为关
	 * @return 开返回true,关返回false
	 */
	private boolean isReceivedDataEquals(byte[] data) {
		if (!Constant.buzEnableOne) {
			if (((data[6] >> 7) & 1) == 1) {
				return true;
			}
		} else {
			if (((data[6] >> 7) & 1) == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 设置门磁监测，高/低电平 默认00 低电平 蜂鸣器叫, 01 高电平
	 */
	public void setMCcheck(boolean b) {
		// aa 55 03 00 71 00 2e
		n = -1;

		lockOrder = new byte[7];

		lockOrder[0] = (byte) 0xAA;
		lockOrder[1] = 0x55;
		lockOrder[2] = 0x03;
		lockOrder[3] = 0x00;
		lockOrder[4] = 0x71;
		if (b) {
			lockOrder[5] = 0x00;
		} else {
			lockOrder[5] = 0x01;
		}
		lockOrder[6] = Constant.calcCrc8(lockOrder, 0, 6);

		lock.start();
		lock.sendData(lockOrder);
	}

	//add yazhou
	public void doLockTest() {
		isBusy = true;
		mHandler.sendEmptyMessage(1);
	}


	/**
	 * 解析锁状态
	 *
	 * @param bt
	 * @return 0为开-true,1为关-false
	 */
	private boolean doCheckResult(byte[] bt) {
		boolean b = Constant.lockStats;
		int num = calculateLock(boxId) - 1; // 计算锁编号
		Log.i("zyz","num=="+num);
		if (boxId.startsWith("Z")) {
			if (boxId.equalsIgnoreCase("Z00")) {
				if (!Constant.buzEnableOne) {
					if (((bt[6] >> 7) & 1) == 1) {
						return true;
					}
				} else {
					if (((bt[6] >> 7) & 1) == 0) {
						return true;
					}
				}
				return false;
			} else {
				return (((bt[5] >> num) & 1) == 0) ? (!b) : b;
			}
		} else {
			Log.i("zyz","num / 8=="+num / 8);
			switch (num / 8) {
				case 0:
					return (((bt[5] >> (num % 8)) & 1) == 0) ? (!b) : b;
				case 1:
					return (((bt[6] >> (num % 8)) & 1) == 0) ? (!b) : b;
				case 2:
					return (((bt[7] >> (num % 8)) & 1) == 0) ? (!b) : b;
				default:
					return false;
			}
		}
	}

	/**
	 * 门磁小灯控制，
	 *
	 * @param b=true时，开启小灯
	 */
	public void setMCLamp(boolean b) {
		// aa 55 03 00 55 00 D4
		n = -1;

		lockOrder = new byte[7];

		lockOrder[0] = (byte) 0xAA;
		lockOrder[1] = 0x55;
		lockOrder[2] = 0x03;
		lockOrder[3] = 0x00;
		lockOrder[4] = 0x55;
		if (b) {
			lockOrder[5] = 0x00;
		} else {
			lockOrder[5] = 0x01;
		}
		lockOrder[6] = Constant.calcCrc8(lockOrder, 0, 6);

		lock.start();
		lock.sendData(lockOrder);
	}

	/**
	 * 设置485抄表校验位和波特率
	 *
	 * @param check
	 * @param baudrate
	 */
	public void set485(String check, String baudrate) {
		n = -1;

		lockOrder = new byte[8];

		lockOrder[0] = (byte) 0xAA;
		lockOrder[1] = 0x55;
		lockOrder[2] = 0x04;
		lockOrder[3] = 0x00;
		lockOrder[4] = 0x61;
		lockOrder[5] = (byte) (Byte.parseByte(check) - 1);
		lockOrder[6] = (byte) (Byte.parseByte(baudrate) - 1);
		lockOrder[7] = Constant.calcCrc8(lockOrder, 0, 7);

		lock.start();
		lock.sendData(lockOrder);
	}

	/**
	 * 获取当前湿度
	 */
	public void getHumidity() {
		n = 13;
		isBusy = true;
		lockOrder = new byte[6];
		lockOrder[0] = (byte) 0xAA;
		lockOrder[1] = 0x55;
		lockOrder[2] = 0x02;
		lockOrder[3] = 0x00;
		lockOrder[4] = 0x5C;
		lockOrder[5] = Constant.calcCrc8(lockOrder, 0, 5);
		lock.start();
		lock.sendData(lockOrder);
	}

	/**
	 * 获取当前温度值
	 */
	public void getTemp() {
		n = 12;
		isBusy = true;
		lockOrder = new byte[6];
		lockOrder[0] = (byte) 0xAA;
		lockOrder[1] = 0x55;
		lockOrder[2] = 0x02;
		lockOrder[3] = 0x00;
		lockOrder[4] = 0x5B;
		lockOrder[5] = Constant.calcCrc8(lockOrder, 0, 5);
		lock.start();
		lock.sendData(lockOrder);
	}

	/**
	 * 设置温度补偿
	 */
	public void setWDBC(byte b) {
		// aa 55 03 00 6C 01 15
		n = -1;
		isBusy = true;
		lockOrder = new byte[7];
		lockOrder[0] = (byte) 0xAA;
		lockOrder[1] = 0x55;
		lockOrder[2] = 0x03;
		lockOrder[3] = 0x00;
		lockOrder[4] = 0x6C;
		lockOrder[5] = b;
		lockOrder[6] = Constant.calcCrc8(lockOrder, 0, 6);
		lock.start();
		lock.sendData(lockOrder);
	}

	/**
	 * 查询温度补偿
	 */
	public void querWDBC() {
		// aa 55 02 00 6E E2;
		n = 14;
		isBusy = true;
		lockOrder = new byte[6];
		lockOrder[0] = (byte) 0xAA;
		lockOrder[1] = 0x55;
		lockOrder[2] = 0x02;
		lockOrder[3] = 0x00;
		lockOrder[4] = 0x6E;
		lockOrder[5] = Constant.calcCrc8(lockOrder, 0, 5);
		lock.start();
		lock.sendData(lockOrder);
	}

	/**
	 * 查询当前适度补偿
	 */
	public void querySDBC() {
		// AA 55 02 00 6F BC
		n = 15;
		isBusy = true;
		lockOrder = new byte[6];
		lockOrder[0] = (byte) 0xAA;
		lockOrder[1] = 0x55;
		lockOrder[2] = 0x02;
		lockOrder[3] = 0x00;
		lockOrder[4] = 0x6F;
		lockOrder[5] = Constant.calcCrc8(lockOrder, 0, 5);
		lock.start();
		lock.sendData(lockOrder);
	}

	/**
	 * 设置湿度补偿
	 */
	public void setSDBC(byte b) {
		// aa 55 03 00 6C 01 15
		n = -1;
		isBusy = true;
		lockOrder = new byte[7];
		lockOrder[0] = (byte) 0xAA;
		lockOrder[1] = 0x55;
		lockOrder[2] = 0x03;
		lockOrder[3] = 0x00;
		lockOrder[4] = 0x6D;
		lockOrder[5] = b;
		lockOrder[6] = Constant.calcCrc8(lockOrder, 0, 6);
		lock.start();
		lock.sendData(lockOrder);
	}

	/**
	 * 获取锁孔板风扇，加热器控制权，用于调试这些接口
	 *
	 * @param b=true时，获取控制权
	 */
	public void getControl(boolean b) {
		n = -1;
		lockOrder = new byte[7];
		lockOrder[0] = (byte) 0xAA;
		lockOrder[1] = 0x55;
		lockOrder[2] = 0x03;
		lockOrder[3] = 0x00;
		lockOrder[4] = 0x5D;
		if (b) {
			lockOrder[5] = 0x00; // 0 = 获取控制权  命令控制
		} else {
			lockOrder[5] = 0x01;// 自动控制
		}
		lockOrder[6] = Constant.calcCrc8(lockOrder, 0, 6);

		lock.start();
		lock.sendData(lockOrder);
	}

	/**
	 * 风扇A，风扇B，加热器 手动控制
	 *
	 * @param channal
	 *            风扇A=12，风扇B=25，加热器=123
	 */
	public void doDebug(int channal, boolean b) {
		n = -1;
		lockOrder = new byte[7];

		lockOrder[0] = (byte) 0xAA;
		lockOrder[1] = 0x55;
		lockOrder[2] = 0x03;
		lockOrder[3] = 0x00;
		switch (channal) {
			case 12:
				lockOrder[4] = 0x52;
				break;
			case 25:
				lockOrder[4] = 0x53;
				break;
			case 123:
				lockOrder[4] = 0x57;
				break;
			default:
				return;
		}
		lockOrder[5] = (byte) (b ? 0x00 : 0x01); // 0 代表 打开
		lockOrder[6] = Constant.calcCrc8(lockOrder, 0, 6);

		lock.start();
		lock.sendData(lockOrder);
	}

	/**
	 * 485抄表
	 */
	public void queryPower() {
		isBusy = true;
		n = 11;
		byte[] byte1 = { (byte) 0xAA, 0x55, 0x15, 0x00, 0x64 };
		byte[] byte2 = { (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, 0x68, (byte) 0xAA,
				(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, 0x68, 0x01, 0x02, 0x43, (byte) 0xC3,
				(byte) 0xD5, 0x16 };
		byte[] byte3 = byteMerger(byte1, byte2);
		byte[] byte4 = { 0x00 };
		byte[] byte5 = byteMerger(byte3, byte4);
		byte5[byte5.length - 1] = Constant.calcCrc8(byte5, 0, byte5.length - 1);

		lock.start();
		lock.sendData(byte5);

	}

	public void close485Transfor() {
		isBusy = true;
		n = -1;

		lockOrder = new byte[6];

		lockOrder[0] = (byte) 0xAA;
		lockOrder[1] = 0x55;
		lockOrder[2] = 0x02;
		lockOrder[3] = 0x00;
		lockOrder[4] = 0x65;
		lockOrder[5] = Constant.calcCrc8(lockOrder, 0, 5);

		lock.start();
		lock.sendData(lockOrder);
	}


	public void openLock(String boardStr,String lockStr) throws InterruptedException {
		n = 1;
		boxId = lockStr;
		isBusy = true;
		lockOrder = new byte[7];

		lockOrder[0] = (byte) 0xAA;
		lockOrder[1] = 0x55;
		lockOrder[2] = 0x03;
		//lockOrder[3] = calculateBoard(str);
		lockOrder[3] = Integer.valueOf(boardStr).byteValue();
		//lockOrder[4] = 0x7d;//wukang 改为7d
		lockOrder[4] = 0x50;
		//lockOrder[5] = (byte) (calculateLock(str) - 1);
		lockOrder[5] = (byte) (Integer.valueOf(lockStr).byteValue()-1);
		Log.i("zyz","lockOrder[5]=="+lockOrder[5]);
		lockOrder[6] = Constant.calcCrc8(lockOrder, 0, 6);
		lock.sendData(lockOrder);
		lock.start();
//		while (LISHIZHI == null) {
//			Thread.sleep(100);
//		}
//
//		if (LISHIZHI.length == 1 && LISHIZHI[0] == 5) {
//			LISHIZHI = null;
//			return false;
//		} else {
//			boolean result = doCheckResult(LISHIZHI);
//			LISHIZHI = null;
//			return result;
//		}
	}
	int[] tmp = null;
	/*add by yazhou for wukang begin*/
	public void openLockForWukang(int totalCount,int currentCount,int[] data){
		n = FLAG_TEST_BIN;//处理数据范回for吴康
		tmp = data;
		lockOrder = new byte[71];
		lockOrder[0] = 0x48;
		lockOrder[1] = 0x42;
		lockOrder[2] = (byte) 0xaa;
		lockOrder[3] = 0x40;
		lockOrder[4] = (byte) totalCount;
		lockOrder[5] = (byte) currentCount;
		getData();
		lockOrder[70] = (byte) customCrc(lockOrder,71);
		lock.start();
		lock.sendData(lockOrder);
	}

	private void getData(){
		for (int i=0;i<tmp.length;i++){
			lockOrder[i+6] = (byte) tmp[i];
		}
	}
	private int customCrc(byte[] a,int count){
		int result;
		if(count == 0) {
			result = 0;
		} else {
			result = a[count-1]^customCrc(a,--count);
		}
		return result;//count应当是数组元素的个数
	}
	/*add by yazhou for wukang end*/
	// 多路开锁
	public void openLock(String[] batchboxid) {
		n = 3;
		isBusy = true;
		batchBoxId = null;
		batchBoxId = new String[batchboxid.length];

		HasOpened = null;
		HasOpened = new boolean[batchboxid.length];
		iNum = 0;
		for (String s : batchboxid) {
			boxId = s;
			batchBoxId[iNum] = s;
			HasOpened[iNum] = false;

			lockOrder = new byte[7];

			lockOrder[0] = (byte) 0xAA;
			lockOrder[1] = 0x55;
			lockOrder[2] = 0x03;
			lockOrder[3] = calculateBoard(s);
			lockOrder[4] = 0x50;
			lockOrder[5] = (byte) (calculateLock(s) - 1);
			lockOrder[6] = Constant.calcCrc8(lockOrder, 0, 6);

			if (!s.equals("Z08")) {
				lock.start();
				lock.sendData(lockOrder);
			}

			try {
				Thread.sleep(1300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				Log.v(TAG, "sleep(1000)异常");
			}
			// if (iNum < batchboxid.length - 1) {
			// iNum++;
			// }
			iNum++;
		}
	}



	// 查询锁状态
	public boolean queryLock(String str) throws InterruptedException {
		isBusy = true;
		boxId = str;

		lockOrder = new byte[6];

		lockOrder[0] = (byte) 0xAA;
		lockOrder[1] = 0x55;
		lockOrder[2] = 0x02;
		lockOrder[3] = calculateBoard(str);
		lockOrder[4] = 0x51;
		lockOrder[5] = Constant.calcCrc8(lockOrder, 0, 5);

		lock.start();
		lock.sendData(lockOrder);
		Log.i("zyz","LISHIZHI=="+LISHIZHI);
		return bb;
//		while (LISHIZHI == null) {
//			Thread.sleep(100);
//		}

//		if (LISHIZHI.length == 1 && LISHIZHI[0] == 5) {
//			LISHIZHI = null;
//			return false;
//		} else {
//			boolean result = doCheckResult(LISHIZHI);
//			LISHIZHI = null;
//			return result;
//		}
	}

	public void queryLockTest(String str) {
		n = 16;
		isBusy = true;
		boxId = str;

		lockOrder = new byte[6];

		lockOrder[0] = (byte) 0xAA;
		lockOrder[1] = 0x55;
		lockOrder[2] = 0x02;
		lockOrder[3] = calculateBoard(str);
		lockOrder[4] = 0x51;
		lockOrder[5] = Constant.calcCrc8(lockOrder, 0, 5);

		lock.start();
		lock.sendData(lockOrder);
	}

	// 多路查询
	public void queryLock(String[] batchboxid) {
		n = 3;
		isBusy = true;
		batchBoxId = null;
		batchBoxId = new String[batchboxid.length];

		HasOpened = null;
		HasOpened = new boolean[batchboxid.length];

		iNum = 0;
		for (String s : batchboxid) {
			boxId = s;
			Constant.writeLog("多路查询----> " + s);
			batchBoxId[iNum] = s;
			HasOpened[iNum] = false;

			lockOrder = new byte[6];

			lockOrder[0] = (byte) 0xAA;
			lockOrder[1] = 0x55;
			lockOrder[2] = 0x02;
			lockOrder[3] = calculateBoard(s);
			lockOrder[4] = 0x51;
			lockOrder[5] = Constant.calcCrc8(lockOrder, 0, 5);

			lock.start();
			lock.sendData(lockOrder);
			try {
				Thread.sleep(130);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				Log.v(TAG, "sleep(1000)异常");
			}
			iNum++;
		}
	}

	// 已打开格口测试
	public void queryAll(String s) {
		n = 5;
		isBusy = true;
		list.clear();
		String str[] = { "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16",
				"17", "18", "19", "20", "21", "22" };

		ArrayList<String> listBoxId = new ArrayList<String>();

		if (s.length() % 3 != 0) {
			return;
		}

		while (true) {
			String s1 = s.substring(0, 1);
			if (s1.equalsIgnoreCase("Z")) {
				for (int s5 = 1; s5 < 8; s5++) {
					listBoxId.add("Z0" + s5);
				}
			} else {
				for (int s5 = 0; s5 < 22; s5++) {
					listBoxId.add(s1 + str[s5]);
				}
			}
			if (s.length() == 3) {
				break;
			} else {
				s = s.substring(3, s.length());
			}
		}

		batchBoxId = null;
		batchBoxId = listBoxId.toArray(new String[listBoxId.size()]);

		for (iNum = 0; iNum <= batchBoxId.length - 1; iNum++) {
			boxId = batchBoxId[iNum];

			lockOrder = new byte[6];

			lockOrder[0] = (byte) 0xAA;
			lockOrder[1] = 0x55;
			lockOrder[2] = 0x02;
			lockOrder[3] = calculateBoard(batchBoxId[iNum]);
			lockOrder[4] = 0x51;
			lockOrder[5] = Constant.calcCrc8(lockOrder, 0, 5);

			lock.start();
			lock.sendData(lockOrder);
			try {
				Thread.sleep(130);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				Log.v(TAG, "sleep(1000)异常");
			}
		}
	}

	// 门磁状态监测
	public static boolean status = false;

	public void queryStatus() {
		status = true;
	}

	public void concelQueryStatus() {
		status = false;
	}

	public void ifDoorOpened() {
		n = 8;

		lockOrder = new byte[6];

		lockOrder[0] = (byte) 0xAA;
		lockOrder[1] = 0x55;
		lockOrder[2] = 0x02;
		lockOrder[3] = 0x00; // 板地址
		lockOrder[4] = 0x56;
		lockOrder[5] = Constant.calcCrc8(lockOrder, 0, 5);

		lock.start();
		lock.sendData(lockOrder);
	}

	// 主柜 00
	public void openZlamp() {
		isBusy = true;
		n = -1;

		lockOrder = new byte[7];

		lockOrder[0] = (byte) 0xAA;
		lockOrder[1] = 0x55;
		lockOrder[2] = 0x03;
		lockOrder[3] = 0x00; // 板地址
		lockOrder[4] = 0x54;
		lockOrder[5] = 0x00; // 0为开灯，1为关灯
		lockOrder[6] = Constant.calcCrc8(lockOrder, 0, 6);

		lock.start();
		lock.sendData(lockOrder);
	}

	public void closeZlamp() {
		isBusy = true;
		n = -1;

		lockOrder = new byte[7];

		lockOrder[0] = (byte) 0xAA;
		lockOrder[1] = 0x55;
		lockOrder[2] = 0x03;
		lockOrder[3] = 0x00; // 板地址
		lockOrder[4] = 0x54;
		lockOrder[5] = 0x01; // 0为开灯，1为关灯
		lockOrder[6] = Constant.calcCrc8(lockOrder, 0, 6);

		lock.start();
		lock.sendData(lockOrder);
	}

	private byte calculateBoard(String string) {
		String str = string.substring(0, 1);
		if (str.equals("Z")) {
			return 0x00;
		} else if (str.equals("A")) {
			return 0x01;
		} else if (str.equals("B")) {
			return 0x02;
		} else if (str.equals("C")) {
			return 0x03;
		} else if (str.equals("D")) {
			return 0x04;
		} else if (str.equals("E")) {
			return 0x05;
		} else if (str.equals("F")) {
			return 0x06;
		} else if (str.equals("G")) {
			return 0x07;
		} else if (str.equals("H")) {
			return 0x08;
		} else {
			return 0x0f;
		}
	}

	private byte calculateLock(String string) {
		if (string.substring(0, 1).equalsIgnoreCase("Z")) {
			if (Integer.parseInt(string.substring(1, 3)) == 99) {
				return 8;
			} else if (Integer.parseInt(string.substring(1, 3)) == 0) {
				return 0x0d;
			} else {
				return (byte) Integer.parseInt(string.substring(1, 3));
			}
		} else {
			return (byte) Integer.parseInt(string.substring(1, 3));
		}
	}

	public boolean getIsBusy() {
		return isBusy;
	}

	// 开灯
	public boolean openLamp() {
		n = -1;
		isBusy = true;
		boolean isOpenSuccess = false;
		for (iNum = 1; iNum < 9; iNum++) {
			lockOrder = new byte[7];
			lockOrder[0] = (byte) 0xAA;
			lockOrder[1] = 0x55;
			lockOrder[2] = 0x03;
			lockOrder[3] = (byte) iNum; // 板地址
			lockOrder[4] = 0x54;
			lockOrder[5] = 0x00;
			lockOrder[6] = Constant.calcCrc8(lockOrder, 0, 6);
			lock.start();
			isOpenSuccess =  lock.sendData(lockOrder);
			try {
				Thread.sleep(120);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		isBusy = false;

		// 开副柜灯的时候，检查log文件，超过15天的删除
		/*if (checkLogFile) {
			checkLogFile = false;
			try {
				new Thread(new Runnable() {

					@Override
					public void run() {
						// 删除过期的串口log
						deleteExpirationFile("/storage/sdcard0/rocktech_log");

						// 删除过期的crash信息
						deleteExpirationFile("/storage/sdcard0/rocktech_crash");

						// 如果老版本路径下的carsh信息，删除
						File crashLog = new File("/storage/sdcard0/rocktech_log/crash");
						if (crashLog.exists()) {
							if (crashLog.isDirectory()) {
								File[] files = crashLog.listFiles();
								for (File f : files) {
									f.delete();
								}
							}
							crashLog.delete();
						}
					}
				}).start();
			} catch (Exception e) {
				// TODO: handle exception
				checkLogFile = true;
			}
			checkLogFile = true;
		}*/

		return isOpenSuccess;
	}

	/**
	 * 文件排序
	 *
	 * @param fs
	 */
	public static void orderByDate(File[] fs) {
		Arrays.sort(fs, new Comparator<File>() {
			public int compare(File f1, File f2) {
				long diff = f1.lastModified() - f2.lastModified();
				if (diff > 0)
					return 1;
				else if (diff == 0)
					return 0;
				else
					return -1;
			}

			public boolean equals(Object obj) {
				return true;
			}

		});
	}

	/*
	 * 只保留15天的log信息
	 */
	private static void deleteExpirationFile(String str) {
		File dir = new File(str);
		File[] files = dir.listFiles();
		long expirationTime = 15 * 24 * 3600 * 1000;
		long nowTime = System.currentTimeMillis();
		List<File> deleteList = new ArrayList<File>();

		if (getFileSize(dir) >= 1024) {
			while (true) {
				orderByDate(files);
				files[0].delete();
				if (getFileSize(dir) < 800) {
					break;
				}
			}
		}

		if (dir.isDirectory()) {
			for (File f : files) {
				if (f.exists()) {
					long time = f.lastModified();
					if (nowTime - time >= expirationTime) {
						deleteList.add(f);
					}
				}
			}
			for (File f : deleteList) {
				f.delete();
			}
		}

	}

	/**
	 * 获取文件夹大小
	 *
	 * @param f
	 * @return MB
	 */
	private static long getFileSize(File f) {
		long size = 0;
		File flist[] = f.listFiles();
		if (flist != null) {
			for (int i = 0; i < flist.length; i++) {
				if (flist[i].isDirectory()) {
					size = size + getFileSize(flist[i]);
				} else {
					size = size + flist[i].length();
				}
			}
			return size / 1048576;
		} else {
			return 0;
		}
	}

	// 关灯
	public void closeLamp() {
		n = -1;
		isBusy = true;
		for (iNum = 1; iNum < 9; iNum++) {

			lockOrder = new byte[7];

			lockOrder[0] = (byte) 0xAA;
			lockOrder[1] = 0x55;
			lockOrder[2] = 0x03;
			lockOrder[3] = (byte) iNum; // 板地址
			lockOrder[4] = 0x54;
			lockOrder[5] = 0x01;
			lockOrder[6] = Constant.calcCrc8(lockOrder, 0, 6);

			lock.start();
			lock.sendData(lockOrder);
			try {
				Thread.sleep(120);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
			}
		}
	}

	String[] assetCodeArrays;
	String[] boxIdArrays;

	public void writeCode(byte[] codes) {
		n = -1;
		isBusy = true;
		lock.start();
		lock.sendData(codes);
	}

	// 查询资产编码
	public void readCodes() {
		n = 6;
		isBusy = true;
		code1.clear();
		code2.clear();
		assetCodeArrays = null;
		boxIdArrays = null;

		for (iNum = 0; iNum < 9; iNum++) {

			lockOrder = new byte[7];

			lockOrder[0] = (byte) 0xAA;
			lockOrder[1] = 0x55;
			lockOrder[2] = 0x03;
			lockOrder[3] = (byte) iNum;
			lockOrder[4] = 0x60;
			lockOrder[5] = 0x18;
			lockOrder[6] = Constant.calcCrc8(lockOrder, 0, 6);

			lock.start();
			lock.sendData(lockOrder);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static byte[] subBytes(byte[] src, int begin, int count) {
		Log.d("zyz","subBytes begin"+";src.length=="+src.length+";count=="+count);
		if (count <= 0) {
			return null;
		}
		byte[] bs = new byte[count];
		for (int i = begin; i < begin + count; i++) {
			bs[i - begin] = src[i];
		}
		return bs;
	}

	public void writeCodes(String[] assetCodeArrays2, String[] boxIdArrays2) {
		n = 7;
		isBusy = true;
		int j = 1;
		code1.clear();
		code2.clear();
		assetCodeArrays = null;
		boxIdArrays = null;

		String[] assetCodeArrays3 = assetCodeArrays2;
		String[] boxIdArrays3 = boxIdArrays2;

		for (int i = 0; i < boxIdArrays3.length; i++) {
			if (Integer.parseInt(assetCodeArrays3[i].substring(1, 3)) != 0x00) {
				byte[] code1 = { (byte) 0xAA, (byte) j, 0x1D, 0x07, 0x00, 0x18,
						boxIdArrays3[i].substring(0, 1).getBytes()[0], boxIdArrays3[i].substring(1, 2).getBytes()[0],
						boxIdArrays3[i].substring(2, 3).getBytes()[0] };
				byte[] code2 = assetCodeArrays3[i].getBytes();
				byte[] codes = byteMerger(code1, code2);
				byte[] code3 = { Constant.calcCrc8(codes, 0, 29, (byte) 0), (byte) 0xff };
				byte[] codes_fl = byteMerger(codes, code3);
				// Constant.byteToHex("send", codes_fl.length, codes_fl);
				lock.start();
				lock.sendData(codes_fl);
				try {
					System.out.println(new String(subBytes(codes_fl, 6, 3), "UTF-8"));
					System.out.println(new String(subBytes(codes_fl, 9, 21), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				j++;
			} else {
				byte[] code1 = { (byte) 0xAA, 0x00, 0x1D, 0x08, 0x18, boxIdArrays3[i].substring(0, 1).getBytes()[0],
						boxIdArrays3[i].substring(1, 2).getBytes()[0], boxIdArrays3[i].substring(2, 3).getBytes()[0] };
				byte[] code2 = assetCodeArrays3[i].getBytes();
				byte[] codes = byteMerger(code1, code2);
				byte[] code3 = { Constant.calcCrc8(codes, 0, 29, (byte) 0), (byte) 0xff };
				byte[] codes_fl = byteMerger(codes, code3);
				// Constant.byteToHex("send", codes_fl.length, codes_fl);
				lock.start();
				lock.sendData(codes_fl);
				try {
					System.out.println(new String(subBytes(codes_fl, 5, 3), "UTF-8"));
					System.out.println(new String(subBytes(codes_fl, 8, 21), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}

			try {
				Thread.sleep(120);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
		byte[] byte_3 = new byte[byte_1.length + byte_2.length];
		System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
		System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
		return byte_3;
	}

	/**
	 * 打开/关闭 蜂鸣器
	 *
	 * @param -b为true，打开
	 *
	 */
	public void setBuzzer(boolean b) {
		n = -1;

		lockOrder = new byte[7];

		lockOrder[0] = (byte) 0xAA;
		lockOrder[1] = 0x55;
		lockOrder[2] = 0x03;
		lockOrder[3] = 0x00;
		lockOrder[4] = 0x5E;
		lockOrder[5] = (byte) (b ? 1 : 0);
		lockOrder[6] = Constant.calcCrc8(lockOrder, 0, 6);

		lock.start();
		lock.sendData(lockOrder);
	}

	/**
	 * 设置 温度上阀值，下阀值
	 *
	 * @param i
	 *            58=ch12风扇,59=ch25风扇,5A=加热器
	 * @param open
	 *            2个字节的打开温度
	 * @param close
	 *            2个字节的关闭温度
	 */
	public void setControl(int i, String open, String close) throws Exception {
		n = -1;
		int btO = (int) (Double.parseDouble(open) * 10 + 500);
		int btC = (int) (Double.parseDouble(close) * 10 + 500);

		if (btO >= 0 && btO <= 1500 && btC >= 0 && btC <= 1500) {
			lockOrder = new byte[10];

			lockOrder[0] = (byte) 0xAA;
			lockOrder[1] = 0x55;
			lockOrder[2] = 0x06;
			lockOrder[3] = 00;
			lockOrder[4] = (byte) i;
			lockOrder[5] = (byte) (btO >> 8);
			lockOrder[6] = (byte) (btO & 0xFF);
			lockOrder[7] = (byte) (btC >> 8);
			lockOrder[8] = (byte) (btC & 0xFF);

			lockOrder[9] = Constant.calcCrc8(lockOrder, 0, 9);

			lock.start();
			lock.sendData(lockOrder);
		} else {
			throw new Exception();
		}

	}

	public void query485Setting() {
		n = 17;

		lockOrder = new byte[6];

		lockOrder[0] = (byte) 0xAA;
		lockOrder[1] = 0x55;
		lockOrder[2] = 0x02;
		lockOrder[3] = 0x00;
		lockOrder[4] = 0x70;
		lockOrder[5] = Constant.calcCrc8(lockOrder, 0, 5);

		lock.start();
		lock.sendData(lockOrder);
	}

	public void querySetting(int i) {
		n = 18;

		lockOrder = new byte[6];

		lockOrder[0] = (byte) 0xAA;
		lockOrder[1] = 0x55;
		lockOrder[2] = 0x02;
		lockOrder[3] = 0x00;
		lockOrder[4] = (byte) i;
		lockOrder[5] = Constant.calcCrc8(lockOrder, 0, 5);

		lock.start();
		lock.sendData(lockOrder);
	}

	public void queryMCsetting() {
		n = 19;

		lockOrder = new byte[6];

		lockOrder[0] = (byte) 0xAA;
		lockOrder[1] = 0x55;
		lockOrder[2] = 0x02;
		lockOrder[3] = 0x00;
		lockOrder[4] = 0x72;
		lockOrder[5] = Constant.calcCrc8(lockOrder, 0, 5);

		lock.start();
		lock.sendData(lockOrder);
	}
}
