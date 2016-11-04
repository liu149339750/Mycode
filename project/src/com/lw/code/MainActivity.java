package com.lw.code;

import java.util.List;

import com.etsy.android.grid.StaggeredGridView;
import com.lw.util.DataCache;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Toast;

public class MainActivity extends Activity implements LoaderCallbacks<List<DemoEntry>>{
	
	private StaggeredGridView mGridView;
	private StaggeredAdapter mAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		System.out.println("onCreate");
		initSystemBar();
		mGridView = (StaggeredGridView) findViewById(R.id.staggeredGridView1);
		mGridView.setEmptyView(getLayoutInflater().inflate(R.layout.empty_layout, null));
			
		mAdapter = new StaggeredAdapter(this, DataCache.getInstance(this).getData());
		mGridView.setAdapter(mAdapter);
		
		getLoaderManager().initLoader(0, null, this);
		LoaderManager.enableDebugLogging(true);
        
		
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
	
	private void initSystemBar() {  
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {  
            setTranslucentStatus(true);  
        }  
        SystemBarTintManager tintManager = new SystemBarTintManager(this);  
        tintManager.setStatusBarTintEnabled(true);  
        //ʹ����ɫ��Դ   
        //tintManager.setStatusBarTintResource(R.color.systemBar_color);   
        //ʹ��ͼƬ��Դ   
        tintManager.setStatusBarTintDrawable(getResources().getDrawable(R.drawable.ic_launcher));  
          
    }  

	private void setTranslucentStatus(boolean on) {    
        Window win = getWindow();    
        WindowManager.LayoutParams winParams = win.getAttributes();    
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;    
        if (on) {    
            winParams.flags |= bits;    
        } else {    
            winParams.flags &= ~bits;    
        }    
        win.setAttributes(winParams);  
	}
	
	@Override
	public Loader<List<DemoEntry>> onCreateLoader(int id, Bundle args) {
		System.out.println("onCreateLoader");
		return DataCache.getInstance(this);
	}

	@Override
	public void onLoadFinished(Loader<List<DemoEntry>> loader, List<DemoEntry> data) {
		System.out.println("onLoadFinished>"+data.size());
		Toast.makeText(this, "Load finish", Toast.LENGTH_SHORT).show();
		mAdapter.setDatas(data);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<List<DemoEntry>> loader) {
		System.out.println("onLoaderReset");
	}
}
