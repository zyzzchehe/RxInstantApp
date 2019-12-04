package com.rocktech.sharebookcase.tool;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.util.Log;

public class SilentInstall {
	/**
	 * 执行具体的静默安装逻辑，需要手机ROOT。
	 * 
	 * @param apkPath
	 *            要安装的apk文件的路径
	 * @return 安装成功返回true，安装失败返回false。
	 */
	public boolean install(String apkPath) {
		boolean result = false;
		BufferedReader errorStream = null;
		try {
			Runtime runtime = Runtime.getRuntime();
			String command = "pm install -r " + apkPath + "\n";
			Process process = runtime.exec(command);
			process.waitFor();
			errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String msg = "";
			String line;
			// 读取命令的执行结果
			while ((line = errorStream.readLine()) != null) {
				msg += line;
			}
			// 如果执行结果中包含Failure字样就认为是安装失败，否则就认为安装成功
			if (!msg.contains("Failure")) {
				result = true;
			}
		} catch (Exception e) {
			Log.e("TAG", e.getMessage(), e);
		} 
		return result;
	}

}
