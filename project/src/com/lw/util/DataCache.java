package com.lw.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lw.code.DemoEntry;

import android.content.AsyncTaskLoader;
import android.content.Context;

public class DataCache extends AsyncTaskLoader<List<DemoEntry>>{
	
//	private static final String URL = "http://sunsonfly.synology.me:7070/CodeServer/query";
//	
//	private static final String FILE_BASE_URL = "http://sunsonfly.synology.me:7070/";
    
    private static final String URL = "http://122.114.157.70:8080/CodeServer/query";
    
    private static final String FILE_BASE_URL = "http://122.114.157.70:8080/";
	
	private List<DemoEntry> mDemoEntries = new ArrayList<DemoEntry>();

	private static DataCache dataCache;
	
	private Context mContext;
	
	private static final String CACHE_NAME = "cache";
	
	boolean loadfinish;
	
	public DataCache(Context context) {
		super(context);
		mContext = context;
		loadCache();
	}

	public static DataCache getInstance(Context context) {
		if(dataCache == null)
			dataCache = new DataCache(context);
		return dataCache;
	}
	
	public List<DemoEntry> loadDataFromNet() {
		try {
			String data = HttpUtil.readString(URL);
			System.out.println("loadDataFromNet data="+data);
			Gson gson = new Gson();
			Type type = new TypeToken<List<DemoEntry>>(){}.getType();
			List<DemoEntry> des = gson.fromJson(data, type);
			for(DemoEntry de : des) {
			    de.setIcon(FILE_BASE_URL+de.getIcon());
			    de.setApk(FILE_BASE_URL+de.getApk());
			}
			mDemoEntries.clear();
			mDemoEntries.addAll(des);
			loadfinish = true;
			savaCache();
		}  catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mDemoEntries;
	}
	
	@Override
	protected void onReset() {
	    loadfinish = false;
	}
	
	private void savaCache() {
		if(mDemoEntries.size() == 0)
			return;
		Gson gson = new Gson();
		String json = gson.toJson(mDemoEntries);
		System.out.println("save cache,json="+json);
		try {
			OutputStream out = mContext.openFileOutput(CACHE_NAME, Context.MODE_PRIVATE);
			out.write(json.getBytes());
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadCache() {
		try {
			InputStream in = mContext.openFileInput(CACHE_NAME);
			String json = Util.readString(in);
			Gson gson = new Gson();
			Type type = new TypeToken<List<DemoEntry>>(){}.getType();
			List<DemoEntry> des = gson.fromJson(json, type);
			mDemoEntries.clear();
			mDemoEntries.addAll(des);
			abandon();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	//when activity onstart,this will be invoked .this is for db query,for net not suited
	@Override
	protected void onStartLoading() {
	    System.out.println("onStartLoading:"+isStarted());
	    if(!loadfinish)
	        forceLoad();
	}
	
	public List<DemoEntry> getData() {
		return mDemoEntries;
	}

	@Override
	public List<DemoEntry> loadInBackground() {
		return loadDataFromNet();
	}
	
	@Override
	public void deliverResult(List<DemoEntry> data) {
	    System.out.println("deliverResult");
		super.deliverResult(mDemoEntries);
	}

}
