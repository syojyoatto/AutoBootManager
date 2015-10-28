package com.syojyoatto.autobootmanager;

import java.io.DataOutputStream;

public class ExeCmd {


	public static void execmd(String cmd){

		try{
			Process process = Runtime.getRuntime().exec("su");

			DataOutputStream dataOutputStream = new DataOutputStream(process.getOutputStream());

			dataOutputStream.writeBytes(cmd+"\n");
			dataOutputStream.writeBytes("exit\n");
			dataOutputStream.flush();

			process.waitFor();

		}catch (Exception e) {
			// TODO: handle exception
		}

	}
}
