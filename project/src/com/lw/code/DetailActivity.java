package com.lw.code;

import java.io.File;

import com.lw.util.FileDownload;
import com.lw.util.Util;
import com.panwrona.downloadprogressbar.library.DownloadProgressBar;
import com.ryg.dynamicload.internal.DLIntent;
import com.ryg.dynamicload.internal.DLPluginManager;
import com.ryg.dynamicload.internal.DLPluginPackage;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
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
		
		mTitle.setText(mEntry.title);
		mDetail.setText(mEntry.detail);
		mMinVersion.setText(">="+mEntry.minversion);
		mLink.setText(mEntry.openlink);
		checkDownload();
		mStart.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				downloadOrstart();
			}
		});
	}
	
	private void checkDownload() {
		String name = Util.getName(mEntry.url);
		File file = new File(Util.getApkDirectory(),name);
		if(file.exists() && file.length() > 0) {
			mDLPackage = DLPluginManager.getInstance(this).loadApk(file.getPath());
			if(mDLPackage != null)
				isDownload = true;
		}
	}

	protected void downloadOrstart() {
		mStart.playToSuccess();
//		if(isDownload) {
//	        DLPluginManager pluginManager = DLPluginManager.getInstance(this);
//	        pluginManager.startPluginActivity(this, new DLIntent(mDLPackage.packageName, mDLPackage.packageInfo.activities[0].name));
//		} else {
//			FileDownload fileDownload = new FileDownload(handler) ;
//			fileDownload.download(mEntry.url,Util.getOutFile(mEntry.url));
//			Toast.makeText(this, "开始下载", Toast.LENGTH_SHORT).show();
//		}
	}

	private Handler handler = new Handler(new Handler.Callback() {
		
		@Override
		public boolean handleMessage(Message msg) {
			int code = msg.what;
			if(code == FileDownload.HTTP_FAIL) {
				String path = (String) msg.obj;
				new File(path).delete();
			} else if(code == FileDownload.HTTP_SUCESS) {
				if(msg.arg1 == msg.arg2) {
					checkDownload();
					Toast.makeText(DetailActivity.this, "下载成功", Toast.LENGTH_SHORT).show();
				}
			}
			return false;
		}
	});
}
