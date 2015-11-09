package com.lw.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lw.code.DemoEntry;

import android.content.AsyncTaskLoader;
import android.content.Context;

public class DataCache extends AsyncTaskLoader<List<DemoEntry>>{
	
	private static final String URL = "http://10.0.11.197:8080/23CodeServer/query";
	
	private List<DemoEntry> mDemoEntries = new ArrayList<DemoEntry>();

	private static DataCache dataCache;
	
	private Context mContext;
	
	private static final String CACHE_NAME = "cache";
	
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
			System.out.println("loadDataFromNet");
			String data = HttpUtil.readString(URL);
			Gson gson = new Gson();
			Type type = new TypeToken<List<DemoEntry>>(){}.getType();
			List<DemoEntry> des = gson.fromJson(data, type);
			mDemoEntries.clear();
			mDemoEntries.addAll(des);
			savaCache();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mDemoEntries;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	protected void onStartLoading() {
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
		super.deliverResult(mDemoEntries);
	}

}
