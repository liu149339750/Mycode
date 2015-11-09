package com.lw.code;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;

import org.w3c.dom.Comment;

import com.etsy.android.grid.StaggeredGridView;
import com.lw.util.DataCache;
import com.lw.util.FileDownload;
import com.ryg.dynamicload.internal.DLIntent;
import com.ryg.dynamicload.internal.DLPluginManager;
import com.ryg.utils.DLUtils;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

public class MainActivity extends Activity implements LoaderCallbacks<List<DemoEntry>>{
	
	private StaggeredGridView mGridView;
	private BaseAdapter mAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mGridView = (StaggeredGridView) findViewById(R.id.staggeredGridView1);
			
		mAdapter = new StaggeredAdapter(this, DataCache.getInstance(this).getData());
		mGridView.setAdapter(mAdapter);
		
		getLoaderManager().initLoader(0, null, this);
        
		
		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent();
				DemoEntry entry = (DemoEntry) parent.getItemAtPosition(position);
				intent.putExtra("data", entry);
				intent.setClass(MainActivity.this, DetailActivity.class);
				startActivity(intent);
			}
		});
		
	}
	
	
	@Override
	public Loader<List<DemoEntry>> onCreateLoader(int id, Bundle args) {
		System.out.println("onCreateLoader");
		return DataCache.getInstance(this);
	}

	@Override
	public void onLoadFinished(Loader<List<DemoEntry>> loader, List<DemoEntry> data) {
		System.out.println("onLoadFinished>"+data.size());
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<List<DemoEntry>> loader) {
		
	}
}
