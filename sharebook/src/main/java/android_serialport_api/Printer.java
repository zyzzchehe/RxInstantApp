package android_serialport_api;

public abstract class Printer {
	/**
	 * 是否有纸
	 */
	public abstract void hasPaper(); 
	
	/**
	 * 是否纸将近
	 */
	public abstract void hasPaperMore();
	
	/**
	 * 打印
	 */
	public abstract void print(String str); 
	
	/**
	 * 纸张尺寸 3寸-0 4寸-1
	 */
	public abstract void paperSize();
}
