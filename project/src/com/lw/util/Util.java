package com.lw.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.net.Uri;
import android.os.Environment;

public class Util {

	public static File APK_DIRECTORY;
	
	public static String readString(InputStream in) throws IOException {
		int len = 0;
		byte buffer[] = new byte[8*1024];
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		while((len=in.read(buffer)) != -1) {
			bo.write(buffer, 0, len);
		}
		return bo.toString("utf-8");
	}
	
	public static String getName(String url) {
		Uri uri = Uri.parse(url);
		return uri.getLastPathSegment();
	}
	
	public static File getApkDirectory() {
		if(APK_DIRECTORY != null && APK_DIRECTORY.exists())
			return APK_DIRECTORY;
		File file = Environment.getExternalStorageDirectory();
		APK_DIRECTORY = new File(file, "demo");
	    if(APK_DIRECTORY.isFile())
	         APK_DIRECTORY.delete();
		if(!APK_DIRECTORY.exists())
			APK_DIRECTORY.mkdirs();
		return APK_DIRECTORY;
	}
	
	public static File getOutFile(String url) {
		String name = getName(url);
		File file = new File(getApkDirectory(),name);
		return file;
	}
	
	public static void printStackTrace() {
        StackTraceElement[] stackElements = new Throwable().getStackTrace();
        if(stackElements != null)
        {
            for(int i = 0; i < stackElements.length; i++)
            {
                System.out.println(""+ stackElements[i]); 
            }
        }
    }
}
