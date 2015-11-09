package com.lw.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.Handler;
import android.os.Message;

public class FileDownload extends Thread {

	private long mTotal;
	private long mDown;
	private Handler mHandler;
	private String mUrl;
	private File mOutFile;
	
	public static final int HTTP_SUCESS = 0;
	public static final int HTTP_FAIL = -1;

	public FileDownload(Handler handler) {
		mHandler = handler;
	}
	
	public FileDownload() {
	}

	public void download(String url, File out)  {
		System.out.println("download");
		mUrl = url;
		mOutFile = out;
		start();
	}

	@Override
	public void run() {
		try {
			System.out.println("run");
			FileOutputStream fos = new FileOutputStream(mOutFile);
			URL u = new URL(mUrl);
			HttpURLConnection http = (HttpURLConnection) u.openConnection();
			mTotal = http.getContentLength();
			mDown = 0;
			InputStream in = http.getInputStream();
			byte buffer[] = new byte[8 * 1024];

			int len = 0;
			while ((len = in.read(buffer)) != -1) {
				mDown += len;
				fos.write(buffer, 0, len);
				if(mHandler != null && !mHandler.hasMessages(0)) {
					System.out.println("send");
					Message msg = new Message();
					msg.what = HTTP_SUCESS;
					msg.arg1 = (int) mTotal;
					msg.arg2 = (int) mDown;
					mHandler.sendMessageDelayed(msg, 1000);
				}
			}
			fos.close();
			in.close();
			System.out.println("close");
			mHandler.removeMessages(0);
			Message msg = new Message();
			msg.what = HTTP_SUCESS;
			msg.arg1 = (int) mTotal;
			msg.arg2 = (int) mDown;
			mHandler.sendMessageDelayed(msg, 10);
		} catch (Exception e) {
			e.printStackTrace();
			Message msg = new Message();
			msg.what = HTTP_FAIL;
			msg.arg1 = (int) mTotal;
			msg.arg2 = (int) mDown;
			msg.obj = mOutFile.getPath();
			mHandler.sendMessage(msg);
		}
	}
}
