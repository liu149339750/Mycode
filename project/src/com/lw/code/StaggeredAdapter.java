package com.lw.code;

import java.lang.ref.SoftReference;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import edu.mit.mobile.android.imagecache.ImageCache;

public class StaggeredAdapter extends BaseAdapter implements ImageCache.OnImageLoadListener{
	
	private List<DemoEntry> mEntries;
	private LayoutInflater mInflater;
	private ImageCache mCache;
    private boolean mAutosize;
    private SparseArray<ViewDimensionCache> mViewDimensionCache;
    
    private int mDefaultWidth = 100;
    private int mDefaultHeight = 100;
	
    private final SparseArray<SoftReference<ImageView>> mImageViewsToLoad = new SparseArray<SoftReference<ImageView>>();
    
	public StaggeredAdapter(Context context,List<DemoEntry> entries) {
		mEntries = entries;
		mInflater = LayoutInflater.from(context);
		mCache =	ImageCache.getInstance(context);
		mCache.registerOnImageLoadListener(this);
		System.out.println(entries.size());
        if (mAutosize) {
            mViewDimensionCache = new SparseArray<ViewDimensionCache>();
        } else {
            mViewDimensionCache = null;
        }
	}
	
	public void setDatas(List<DemoEntry> datas) {
	    mEntries = datas;
	}

	@Override
	public int getCount() {
		System.out.println("getCount>"+mEntries.size());
		return mEntries.size();
	}

	@Override
	public Object getItem(int position) {
		return mEntries.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		try{
		if(convertView == null) {
			convertView = mInflater.inflate(R.layout.staggerd_item, null);
			holder = new ViewHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.image);
			holder.text = (TextView) convertView.findViewById(R.id.title);
			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();
		DemoEntry entry = mEntries.get(position);
		holder.text.setText(entry.title);
		holder.image.setTag(R.id.ic__uri, Uri.parse(entry.icon));
		
		int imageId = mCache.getNewID();
        Drawable d = null;
        try {
            d = mCache.loadImage(imageId, Uri.parse(entry.icon), mDefaultWidth, mDefaultHeight);
        } catch ( Exception e) {
            e.printStackTrace();
        }
        if (d != null) {
        	holder.image.setImageDrawable(d);
        } else {
            mImageViewsToLoad.put(imageId, new SoftReference<ImageView>(holder.image));
        }
	}catch(Exception e) {
		e.printStackTrace();
	}
		return convertView;
	}
	
	class ViewHolder {
		ImageView image;
		TextView text;
	}

	@Override
	public void onImageLoaded(int id, Uri imageUri, Drawable image) {
		try{
        final SoftReference<ImageView> ivRef = mImageViewsToLoad.get(id);
        if (ivRef == null) {
            return;
        }
        final ImageView iv = ivRef.get();
        if (iv == null) {
            mImageViewsToLoad.remove(id);
            return;
        }
        if (imageUri.equals(iv.getTag(R.id.ic__uri))) {
            iv.setImageDrawable(image);
        }
        mImageViewsToLoad.remove(id);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

    private static class ViewDimensionCache {
        int width;
        int height;
    }
}
