package com.lw.code;

import java.io.File;

import com.lw.util.FileDownload;
import com.lw.util.Util;
import com.panwrona.downloadprogressbar.library.DownloadProgressBar;
import com.panwrona.downloadprogressbar.library.DownloadProgressBar.SuccessType;
import com.ryg.dynamicload.internal.DLIntent;
import com.ryg.dynamicload.internal.DLPluginManager;
import com.ryg.dynamicload.internal.DLPluginPackage;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class DetailActivity extends Activity{

	private DemoEntry mEntry;
	private TextView mDetail;
	private TextView mTitle;
	private TextView mMinVersion;
	private TextView mLink;
	private DownloadProgressBar mStart;
	private boolean isDownload;
	private DLPluginPackage mDLPackage;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.project_detail);
		mEntry = (DemoEntry) getIntent().getSerializableExtra("data");
		if(mEntry == null)
			return;
		mTitle = (TextView) findViewById(R.id.title);
		mDetail = (TextView) findViewById(R.id.detail);
		mMinVersion = (TextView) findViewById(R.id.minv);
		mLink = (TextView) findViewById(R.id.link);
		mStart = (DownloadProgressBar) findViewById(R.id.start);
		mStart.setSuccessAnimaType(SuccessType.TYPE_START);
		
		mTitle.setText(mEntry.title);
		mDetail.setText(mEntry.detail);
		mMinVersion.setText(">="+mEntry.minversion);
		mLink.setText(mEntry.openlink);
		checkDownload();
		System.out.println("isdown="+isDownload);
		if(isDownload) {
		    System.out.println("setSuceeceState");
		    mStart.setSuceeceState();
		}
		mStart.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				downloadOrstart();
			}
		});
	}
	
	private void checkDownload() {
		String name = Util.getName(mEntry.apk);
		File file = new File(Util.getApkDirectory(),name);
		if(file.exists() && file.length() > 0) {
			mDLPackage = DLPluginManager.getInstance(this).loadApk(file.getPath());
			if(mDLPackage != null)
				isDownload = true;
		}
	}

	protected void downloadOrstart() {
		if(isDownload) {
	        DLPluginManager pluginManager = DLPluginManager.getInstance(this);
	        pluginManager.startPluginActivity(this, new DLIntent(mDLPackage.packageName, mDLPackage.packageInfo.activities[0].name));
		} else {
		    mStart.playManualProgressAnimation();
			FileDownload fileDownload = new FileDownload(handler) ;
			fileDownload.download(mEntry.apk,Util.getOutFile(mEntry.apk));
			Toast.makeText(this, "begin download", Toast.LENGTH_SHORT).show();
		}
	}

	private Handler handler = new Handler(new Handler.Callback() {
		
		@Override
		public boolean handleMessage(Message msg) {
			int code = msg.what;
			if(code == FileDownload.HTTP_FAIL) {
				String path = (String) msg.obj;
				File file = new File(path);
				if(file.exists()) {
				    file.delete();
				}
				mStart.abortDownload();
				Toast.makeText(DetailActivity.this, "download Fail", Toast.LENGTH_SHORT).show();
			} else if(code == FileDownload.HTTP_SUCESS) {
			    mStart.setProgress(msg.arg2*100/msg.arg1);
				if(msg.arg1 == msg.arg2) {
					checkDownload();
					Toast.makeText(DetailActivity.this, "download sucess", Toast.LENGTH_SHORT).show();
				}
			}
			return false;
		}
	});
}
