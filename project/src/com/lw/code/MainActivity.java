package com.lw.code;

import java.util.ArrayList;
import java.util.List;

import com.origamilabs.library.views.StaggeredGridView;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
	
	private StaggeredGridView mGridView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
//		mGridView = (StaggeredGridView) findViewById(R.id.staggeredGridView1);
		mGridView = new StaggeredGridView(this);
		setContentView(mGridView);
		List<DemoEntry> data = new ArrayList<DemoEntry>();
		for(int i = 0;i<5;i++){
		DemoEntry de = new DemoEntry();
		de.title="helo";
		de.smallImage = "http://cms.csdnimg.cn/article/201305/21/519b60cc0b17f.jpg";
		data.add(de);
		}
		mGridView.setAdapter(new StaggeredAdapter(this, data));
	}
}
