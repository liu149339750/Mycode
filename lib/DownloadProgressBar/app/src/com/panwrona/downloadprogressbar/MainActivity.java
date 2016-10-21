package com.panwrona.downloadprogressbar;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.panwrona.downloadprogressbar.library.DownloadProgressBar;
import com.panwrona.downloadprogressbar.library.DownloadProgressBar.SuccessType;

public class MainActivity extends Activity {

    private int val = 0;

    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final DownloadProgressBar downloadProgressView = (DownloadProgressBar)findViewById(R.id.dpv3);
        downloadProgressView.setSuccessAnimaType(SuccessType.TYPE_START);
        final TextView successTextView = (TextView)findViewById(R.id.success_text_view);
        successTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                val = val + 10;
                downloadProgressView.setProgress(val);
            }
        });
        Typeface robotoFont=Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        successTextView.setTypeface(robotoFont);

        downloadProgressView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	if(!downloadProgressView.isPlay())
            		downloadProgressView.playManualProgressAnimation();
            	else
            		downloadProgressView.abortDownload();
            }
        });
        downloadProgressView.setOnProgressUpdateListener(new DownloadProgressBar.OnProgressUpdateListener() {
            @Override
            public void onProgressUpdate(float currentPlayTime) {

            }

            @Override
            public void onAnimationStarted() {
//                downloadProgressView.setEnabled(false);
            }

            @Override
            public void onAnimationEnded() {
                val = 0;
                successTextView.setText("Click to download");
                downloadProgressView.setEnabled(true);
            }

            @Override
            public void onAnimationSuccess() {
                successTextView.setText("Downloaded!");
            }

            @Override
            public void onAnimationError() {
                successTextView.setText("Aborted!");
            }

            @Override
            public void onManualProgressStarted() {

            }

            @Override
            public void onManualProgressEnded() {

            }
        });
        
//        ValueAnimator animator = ValueAnimator.ofFloat(0,5,9,2);
//        animator.setDuration(2000);
//        animator.setEvaluator(new TypeEvaluator<Float>() {
//
//			@Override
//			public Float evaluate(float fraction, Float s, Float e) {
//				System.out.println("s="+s+",e="+e+",fraction="+fraction);
//				return e*fraction+s;
//			}
//		});
//        animator.addUpdateListener(new AnimatorUpdateListener() {
//			
//			@Override
//			public void onAnimationUpdate(ValueAnimator animation) {
////				System.out.println("f="+animation.getAnimatedFraction()+",v="+animation.getAnimatedValue());
//			}
//		});
//        animator.start();
    }
}
