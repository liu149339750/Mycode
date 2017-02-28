package com.panwrona.downloadprogressbar.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.TypeEvaluator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

public class DownloadProgressBar extends View {

	private static final String TAG = DownloadProgressBar.class.getSimpleName();
	private static final int DEFAULT_PROGRESS_DURATION = 1000;
	private static final int DEFAULT_RESULT_DURATION = 500;
	private static final float DEFAULT_OVERSHOOT_VALUE = 2.5f;
	private static final int DEFAULT_TEXT_COLOR = Color.BLACK;

	private Paint mCirclePaint;
	private Paint mDrawingPaint;
	private Paint mProgressPaint;
	private Paint mProgressTextPaint;

	private float mRadius;
	private float mStrokeWidth;
	private float mLineWidth;
	private float mLengthFix;
	private float mArrowLineToDotAnimatedValue;
	private float mArrowLineToHorizontalLineAnimatedValue;
	private float mDotToProgressAnimatedValue;
	private float mCurrentGlobalProgressValue;
	private float mSuccessValue;
	// private float mExpandCollapseValue;
	private float mErrorValue;
	private float mOvershootValue;
	private float mOvershootPx;

	private float mCenterX;
	private float mCenterY;

	private int mTextColor;
	private int mCircleBackgroundColor;
	private int mDrawingColor;
	private int mProgressColor;
	private int mProgressDuration;
	private int mResultDuration;

	private AnimatorSet mArrowToLineAnimatorSet;
	private AnimatorSet mProgressAnimationSet;

	private OvershootInterpolator mOvershootInterpolator;

	private ValueAnimator mDotToProgressAnimation;
	private ValueAnimator mProgressAnimation;
	private ValueAnimator mSuccessAnimation;
	private AnimatorSet mSucessAnimatorSet;
	// private ValueAnimator mExpandAnimation;
	// private ValueAnimator mCollapseAnimation;
	private ValueAnimator mErrorAnimation;
	private ValueAnimator mArrowLineToDot;
	private ValueAnimator mArrowLineToHorizontalLine;
	private ValueAnimator mManualProgressAnimation;

	private Path mPath;
	private RectF mCircleBounds;

	private OnProgressUpdateListener mOnProgressUpdateListener;
	private AnimationSet mAbortAnimationSet;
	private AnimatorSet mManualProgressAnimationSet;
	private float mFromArc = 0;
	private float mToArc = 0;
	private float mCurrentGlobalManualProgressValue;

	private enum State {
		ANIMATING_LINE_TO_DOT, IDLE, ANIMATING_SUCCESS, ANIMATING_ERROR, ANIMATING_PROGRESS, ANIMATING_MANUAL_PROGRESS
	}

	public enum SuccessType {
		TYPE_START, TYPE_OK
	};

	private State mState;
	private State mResultState;
	private State mWhichProgress;
	private SuccessType mSuccessType = SuccessType.TYPE_OK;

	public DownloadProgressBar(Context context) {
		super(context);
	}

	public DownloadProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		initAttrs(context, attrs);
		init();
	}

	private void initAttrs(Context context, AttributeSet attrs) {
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.DownloadProgressView, 0, 0);
		try {
			mRadius = array.getDimension(R.styleable.DownloadProgressView_circleRadius, 0);
			mStrokeWidth = array.getDimension(R.styleable.DownloadProgressView_strokeWidth, 0);
			mLineWidth = array.getDimension(R.styleable.DownloadProgressView_lineWidth, 0);
			mLengthFix = (float) (mLineWidth / (2 * Math.sqrt(2)));
			mProgressDuration = array.getInteger(R.styleable.DownloadProgressView_progressDuration,
					DEFAULT_PROGRESS_DURATION);
			mResultDuration = array.getInteger(R.styleable.DownloadProgressView_resultDuration,
					DEFAULT_RESULT_DURATION);
			mDrawingColor = array.getColor(R.styleable.DownloadProgressView_drawingColor, 0);
			mProgressColor = array.getColor(R.styleable.DownloadProgressView_progressColor, 0);
			mCircleBackgroundColor = array.getColor(R.styleable.DownloadProgressView_circleBackgroundColor, 0);
			mOvershootValue = array.getFloat(R.styleable.DownloadProgressView_overshootValue, DEFAULT_OVERSHOOT_VALUE);
			mTextColor = array.getColor(R.styleable.DownloadProgressView_textColor, DEFAULT_TEXT_COLOR);
		} finally {
			array.recycle();
		}
	}

	private void init() {
		mCirclePaint = new Paint();
		mCirclePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		mCirclePaint.setStyle(Paint.Style.STROKE);
		mCirclePaint.setColor(mCircleBackgroundColor);
		mCirclePaint.setStrokeWidth(mStrokeWidth);

		mDrawingPaint = new Paint();
		mDrawingPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		mDrawingPaint.setStyle(Paint.Style.STROKE);
		mDrawingPaint.setColor(mDrawingColor);
		mDrawingPaint.setStrokeWidth(mLineWidth);

		mProgressPaint = new Paint();
		mProgressPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		mProgressPaint.setColor(mProgressColor);
		mProgressPaint.setStyle(Paint.Style.STROKE);

		mProgressTextPaint = new Paint();
		mProgressTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		mProgressTextPaint.setColor(mTextColor);
		mProgressTextPaint.setTextAlign(Align.CENTER);
		mProgressTextPaint.setTextSize(mRadius/3);

		mState = State.IDLE;
		setupAnimations();
	}

	public void setSuccessAnimaType(SuccessType type) {
		mSuccessType = type;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		mCenterX = w / 2f;
		mCenterY = h / 2f;

		mCircleBounds = new RectF();
		float cx = mCenterX + getPaddingLeft() / 2 - getPaddingRight() / 2;
		float cy = mCenterY + getPaddingTop() / 2 - getPaddingBottom() / 2;
		mCircleBounds.top = cy - mRadius + mOvershootPx / 2;
		mCircleBounds.left = cx - mRadius;
		mCircleBounds.bottom = cy + mRadius + mOvershootPx / 2;
		mCircleBounds.right = cx + mRadius;

		mPath.reset();
		mPath.moveTo(0, 0);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = resolveSize((int) (mRadius * 2 + 0.5 + mStrokeWidth + getPaddingLeft() + getPaddingRight()),
				widthMeasureSpec);
		// the circle width + the strokewidth + the overshoot height + the dot
		// diameter
		int height = resolveSize((int) (mRadius * 2 + 0.5 + mStrokeWidth + mOvershootPx + mStrokeWidth + getPaddingTop()
				+ getPaddingBottom()), heightMeasureSpec);
		setMeasuredDimension(width, height);
	}

	private void setupAnimations() {
		mPath = new Path();
		mOvershootInterpolator = new OvershootInterpolator(mOvershootValue);
		mArrowLineToDot = ValueAnimator.ofFloat(0, mRadius / 4); 
		mArrowLineToDot.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				mArrowLineToDotAnimatedValue = (Float) valueAnimator.getAnimatedValue();
				invalidate();
			}
		});
		mArrowLineToDot.setDuration(100);
		mArrowLineToDot.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animator) {
				mState = State.ANIMATING_LINE_TO_DOT;
				if (mOnProgressUpdateListener != null) {
					mOnProgressUpdateListener.onAnimationStarted();
				}
			}

			@Override
			public void onAnimationEnd(Animator animator) {

			}

			@Override
			public void onAnimationCancel(Animator animator) {

			}

			@Override
			public void onAnimationRepeat(Animator animator) {

			}
		});
		mArrowLineToDot.setInterpolator(new AccelerateInterpolator());

		mArrowLineToHorizontalLine = ValueAnimator.ofFloat(0, mRadius / 2);
		mArrowLineToHorizontalLine.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				mArrowLineToHorizontalLineAnimatedValue = (Float) valueAnimator.getAnimatedValue();
				invalidate();
			}
		});
		mArrowLineToHorizontalLine.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animator) {

			}

			@Override
			public void onAnimationEnd(Animator animator) {
			}

			@Override
			public void onAnimationCancel(Animator animator) {

			}

			@Override
			public void onAnimationRepeat(Animator animator) {

			}
		});
		mArrowLineToHorizontalLine.setDuration(300);
		mArrowLineToHorizontalLine.setStartDelay(200);
		mArrowLineToHorizontalLine.setInterpolator(mOvershootInterpolator);

		mDotToProgressAnimation = ValueAnimator.ofFloat(0, mRadius);
		//粗略估算弹出部分的值。measure the over height
		mOvershootPx = (mOvershootInterpolator.getInterpolation(0.5f) - 1) * mRadius;
		System.out.println("mOvershootPx=" + mOvershootPx);
		mDotToProgressAnimation.setDuration(300);
		mDotToProgressAnimation.setStartDelay(300);
		mDotToProgressAnimation.setInterpolator(mOvershootInterpolator);
		mDotToProgressAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				mDotToProgressAnimatedValue = (Float) valueAnimator.getAnimatedValue();
				invalidate();
			}
		});
		mDotToProgressAnimation.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animator) {

			}

			@Override
			public void onAnimationEnd(Animator animator) {
				if (mWhichProgress == State.ANIMATING_PROGRESS)
					mProgressAnimationSet.start();
				else if (mWhichProgress == State.ANIMATING_MANUAL_PROGRESS)
					mManualProgressAnimationSet.start();

				mState = mWhichProgress;

			}

			@Override
			public void onAnimationCancel(Animator animator) {

			}

			@Override
			public void onAnimationRepeat(Animator animator) {

			}
		});

		mArrowToLineAnimatorSet = new AnimatorSet();
		mArrowToLineAnimatorSet.playTogether(mArrowLineToDot, mArrowLineToHorizontalLine, mDotToProgressAnimation);

		mProgressAnimation = ValueAnimator.ofFloat(0, 360f);
		mProgressAnimation.setStartDelay(200);
		mProgressAnimation.setInterpolator(new LinearInterpolator());
		mProgressAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				mCurrentGlobalProgressValue = (Float) valueAnimator.getAnimatedValue();
				if (mOnProgressUpdateListener != null) {
					mOnProgressUpdateListener.onProgressUpdate(mCurrentGlobalProgressValue);
				}
				invalidate();
			}
		});
		mProgressAnimation.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animator) {
				mDotToProgressAnimatedValue = 0;
			}

			@Override
			public void onAnimationEnd(Animator animator) {
			}

			@Override
			public void onAnimationCancel(Animator animator) {

			}

			@Override
			public void onAnimationRepeat(Animator animator) {

			}
		});
		mProgressAnimation.setDuration(mProgressDuration);

		mManualProgressAnimation = ValueAnimator.ofFloat(mFromArc, mToArc);
		mManualProgressAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				mCurrentGlobalManualProgressValue = (Float) valueAnimator.getAnimatedValue();
				invalidate();
			}
		});
		mManualProgressAnimation.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animator) {
				if (mOnProgressUpdateListener != null) {
					mOnProgressUpdateListener.onManualProgressStarted();
				}
				mDotToProgressAnimatedValue = 0;
			}

			@Override
			public void onAnimationEnd(Animator animator) {
				if (mOnProgressUpdateListener != null) {
					mOnProgressUpdateListener.onManualProgressEnded();
				}
				if (mToArc > 359) {
					// mCollapseAnimation.start();

					if (mState == State.ANIMATING_MANUAL_PROGRESS) {
						if (mResultState == State.ANIMATING_ERROR) {
							mErrorAnimation.start();
						} else if (mResultState == State.ANIMATING_SUCCESS) {
							if (mSuccessType == SuccessType.TYPE_OK)
								mSuccessAnimation.start();
							else if (mSuccessType == SuccessType.TYPE_START) {
							    mPath.reset();
							    mPath.moveTo(0, 0);
								mSucessAnimatorSet.start();
							}
						}
					}
				}

			}

			@Override
			public void onAnimationCancel(Animator animator) {

			}

			@Override
			public void onAnimationRepeat(Animator animator) {

			}
		});

		// mExpandAnimation = ValueAnimator.ofFloat(0, mRadius / 6);
		// mExpandAnimation.setDuration(300);
		// mExpandAnimation.setInterpolator(new DecelerateInterpolator());
		// mExpandAnimation.addUpdateListener(new
		// ValueAnimator.AnimatorUpdateListener() {
		// @Override
		// public void onAnimationUpdate(ValueAnimator animation) {
		// mExpandCollapseValue = (Float) animation.getAnimatedValue();
		// invalidate();
		// }
		// });

		// mCollapseAnimation = ValueAnimator.ofFloat(mRadius / 6, mStrokeWidth
		// / 2);
		// mCollapseAnimation.setDuration(300);
		// mCollapseAnimation.setStartDelay(300);
		// mCollapseAnimation.addListener(new Animator.AnimatorListener() {
		// @Override
		// public void onAnimationStart(Animator animator) {
		//
		// }
		//
		// @Override
		// public void onAnimationEnd(Animator animator) {
		// if(mState == State.ANIMATING_MANUAL_PROGRESS) {
		// if (mResultState == State.ANIMATING_ERROR) {
		// mErrorAnimation.start();
		// } else if (mResultState == State.ANIMATING_SUCCESS) {
		// mSuccessAnimation.start();
		// }
		// }
		// }
		//
		// @Override
		// public void onAnimationCancel(Animator animator) {
		//
		// }
		//
		// @Override
		// public void onAnimationRepeat(Animator animator) {
		//
		// }
		// });
		// mCollapseAnimation.setInterpolator(new
		// AccelerateDecelerateInterpolator());
		// mCollapseAnimation.addUpdateListener(new
		// ValueAnimator.AnimatorUpdateListener() {
		// @Override
		// public void onAnimationUpdate(ValueAnimator animation) {
		// mExpandCollapseValue = (Float) animation.getAnimatedValue();
		// invalidate();
		// }
		// });
		mManualProgressAnimationSet = new AnimatorSet();
		// mManualProgressAnimationSet.playSequentially(mExpandAnimation,
		// mManualProgressAnimation);
		mManualProgressAnimationSet.playSequentially(mManualProgressAnimation);

		mProgressAnimationSet = new AnimatorSet();
		mProgressAnimationSet.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (mResultState == State.ANIMATING_ERROR) {
					mErrorAnimation.start();
				} else if (mResultState == State.ANIMATING_SUCCESS) {
					if (mSuccessType == SuccessType.TYPE_OK)
						mSuccessAnimation.start();
					else if (mSuccessType == SuccessType.TYPE_START)
						mSucessAnimatorSet.start();
				}
			}

			@Override
			public void onAnimationCancel(Animator animation) {

			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}
		});
		// mProgressAnimationSet.playSequentially(mExpandAnimation,
		// mProgressAnimation, mCollapseAnimation);
		mProgressAnimationSet.playSequentially(mProgressAnimation);

		mErrorAnimation = ValueAnimator.ofFloat(0, mRadius / 4);
		mErrorAnimation.setDuration(300);
		mErrorAnimation.setStartDelay(200);
		mErrorAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
		mErrorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				mErrorValue = (Float) valueAnimator.getAnimatedValue();
				invalidate();
			}
		});
		mErrorAnimation.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animator) {
				mState = State.ANIMATING_ERROR;
				if (mOnProgressUpdateListener != null) {
					mOnProgressUpdateListener.onAnimationError();
				}
			}

			@Override
			public void onAnimationEnd(Animator animator) {

				postDelayed(new Runnable() {
					@Override
					public void run() {
						if (mOnProgressUpdateListener != null) {
							mOnProgressUpdateListener.onAnimationEnded();
						}
						mState = State.IDLE;
						resetValues();
						invalidate();
					}
				}, mResultDuration);
			}

			@Override
			public void onAnimationCancel(Animator animator) {

			}

			@Override
			public void onAnimationRepeat(Animator animator) {

			}
		});
		//TYPE_OK
		mSuccessAnimation = ValueAnimator.ofFloat(0, mRadius / 4);
		mSuccessAnimation.setDuration(300);
		mSuccessAnimation.setStartDelay(200);
		mSuccessAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
		mSuccessAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				mSuccessValue = (Float) valueAnimator.getAnimatedValue();
				invalidate();
			}
		});
		mSuccessAnimation.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animator) {
				mState = State.ANIMATING_SUCCESS;
				if (mOnProgressUpdateListener != null) {
					mOnProgressUpdateListener.onAnimationSuccess();
				}
			}

			@Override
			public void onAnimationEnd(Animator animator) {
                if (mOnProgressUpdateListener != null) {
                    mOnProgressUpdateListener.onAnimationEnded();
                }
                if (mWhichProgress == State.ANIMATING_PROGRESS) {
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            mState = State.IDLE;
                            resetValues();
                            invalidate();
                        }
                    }, mResultDuration);
                }
			}

			@Override
			public void onAnimationCancel(Animator animator) {

			}

			@Override
			public void onAnimationRepeat(Animator animator) {

			}
		});

		// ---------success one------------------
		// mSuccessAnimation = new ValueAnimator();
		PointF p1 = new PointF(0, 0);
		PointF p2 = new PointF(3 * mRadius / 4, -(float) Math.sin(Math.toRadians(60)) * mRadius / 2);
		PointF p3 = new PointF(0, -mRadius * (float) Math.sin(Math.toRadians(60)));
		/*
		 * mSuccessAnimation.setObjectValues(p1,p2,p3,p1);
		 * mSuccessAnimation.setDuration(500);
		 * mSuccessAnimation.setStartDelay(200);
		 * mSuccessAnimation.setInterpolator(new
		 * AccelerateDecelerateInterpolator());
		 * mSuccessAnimation.addUpdateListener(new AnimatorUpdateListener() {
		 * 
		 * @Override public void onAnimationUpdate(ValueAnimator arg0) { PointF
		 * value = (PointF) arg0.getAnimatedValue(); mPath.lineTo(value.x,
		 * value.y); invalidate(); } }); mSuccessAnimation.setEvaluator(new
		 * TypeEvaluator<PointF>() {
		 * 
		 * @Override public PointF evaluate(float fraction, PointF start, PointF
		 * end) { PointF p = new PointF(); p.x = start.x + (end.x - start.x) *
		 * fraction; p.y = start.y + (end.y - start.y) * fraction;
		 * System.out.println("("+p.x+","+p.y+")" + "fraction="+fraction);
		 * return p; } }); mSuccessAnimation.addListener(new AnimatorListener()
		 * {
		 * 
		 * @Override public void onAnimationStart(Animator arg0) { mState =
		 * State.ANIMATING_SUCCESS; if (mOnProgressUpdateListener != null) {
		 * mOnProgressUpdateListener.onAnimationSuccess(); } }
		 * 
		 * @Override public void onAnimationRepeat(Animator arg0) { }
		 * 
		 * @Override public void onAnimationEnd(Animator arg0) { }
		 * 
		 * @Override public void onAnimationCancel(Animator arg0) { } });
		 */

		// -----------------success two----------------
		TypeEvaluator<PointF> te = new TypeEvaluator<PointF>() {

			@Override
			public PointF evaluate(float fraction, PointF start, PointF end) {
				PointF p = new PointF();
				p.x = start.x + (end.x - start.x) * fraction;
				p.y = start.y + (end.y - start.y) * fraction;
				return p;
			}

		};
		AnimatorUpdateListener listener = new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				PointF value = (PointF) arg0.getAnimatedValue();
				mPath.lineTo(value.x, value.y);
				invalidate();
			}
		};
		ValueAnimator line1 = new ValueAnimator();
		line1.setObjectValues(p1, p2);
		line1.setEvaluator(te);
		line1.addUpdateListener(listener);

		// p2.x = p2.x + mLengthFix;
		// p2.y = p2.y + mLengthFix;
		// p3.x = p3.x - mLengthFix;
		// p3.y = p3.y + mLengthFix;
		ValueAnimator line2 = new ValueAnimator();
		line2.setObjectValues(p2, p3);
		line2.setEvaluator(te);
		line2.addUpdateListener(listener);

		// p3.y = p3.y - mLengthFix;
		// p3.x = p3.x + mLengthFix;
		p1.y = p1.y + mLengthFix;
		ValueAnimator line3 = new ValueAnimator();
		line3.setObjectValues(p3, p1);
		line3.setEvaluator(te);
		line3.addUpdateListener(listener);

		mSucessAnimatorSet = new AnimatorSet();
		mSucessAnimatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
		mSucessAnimatorSet.setDuration(500);
		mSucessAnimatorSet.playSequentially(line1, line2, line3);
		mSucessAnimatorSet.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator arg0) {
				mState = State.ANIMATING_SUCCESS;
				if (mOnProgressUpdateListener != null) {
					mOnProgressUpdateListener.onAnimationSuccess();
				}
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
                if (mOnProgressUpdateListener != null) {
                    mOnProgressUpdateListener.onAnimationEnded();
                }
                if(mWhichProgress == State.ANIMATING_PROGRESS) {
				postDelayed(new Runnable() {
					@Override
					public void run() {

						mState = State.IDLE;
						resetValues();
						invalidate();
					}
				}, mResultDuration);
                }
			}

			@Override
			public void onAnimationCancel(Animator arg0) {
			}
		});

	}

	private void resetValues() {
		mArrowLineToDotAnimatedValue = 0;
		mArrowLineToHorizontalLineAnimatedValue = 0;
		mCurrentGlobalProgressValue = 0;
		mCurrentGlobalManualProgressValue = 0;
		mManualProgressAnimation.setFloatValues(0, 0);
		mToArc = 0;
		mFromArc = 0;
	}

	private void drawing(Canvas canvas) {
		float cx = mCenterX + getPaddingLeft() / 2 - getPaddingRight() / 2;
		float cy = mCenterY + getPaddingTop() / 2 - getPaddingBottom() / 2;
		canvas.drawCircle(cx, cy + mOvershootPx / 2, mRadius, mCirclePaint);
		switch (mState) {
		case IDLE:
			canvas.drawLine(cx, cy - mRadius / 2, cx, cy + mRadius / 2, mDrawingPaint);
			canvas.drawLine(cx - mRadius / 2, cy, cx + mLengthFix, cy + mRadius / 2 + mLengthFix, mDrawingPaint);
			canvas.drawLine(cx - mLengthFix, cy + mRadius / 2 + mLengthFix, cx + mRadius / 2, cy, mDrawingPaint);
			break;
		case ANIMATING_LINE_TO_DOT:
			if (!mDotToProgressAnimation.isRunning()) {
				canvas.drawLine(cx, cy - mRadius / 2 + mArrowLineToDotAnimatedValue * 2 - mStrokeWidth / 2, cx,
						cy + mRadius / 2 - mArrowLineToDotAnimatedValue * 2 + mStrokeWidth / 2, mDrawingPaint);
			}
			canvas.drawLine(cx - mRadius / 2 - mArrowLineToHorizontalLineAnimatedValue / 2, cy, cx + mLengthFix,
					cy + mRadius / 2 - mArrowLineToHorizontalLineAnimatedValue + mLengthFix, mDrawingPaint);
			canvas.drawLine(cx - mLengthFix, cy + mRadius / 2 - mArrowLineToHorizontalLineAnimatedValue + mLengthFix,
					cx + mRadius / 2 + mArrowLineToHorizontalLineAnimatedValue / 2, cy, mDrawingPaint);
			break;
		case ANIMATING_PROGRESS:

			mProgressPaint.setStrokeWidth(mStrokeWidth);
			canvas.drawArc(mCircleBounds, -90, mCurrentGlobalProgressValue, false, mProgressPaint);

			float progress = (float) ((int) (mCurrentGlobalProgressValue * 10 / 3.6)) / 10;
			canvas.drawText("" + progress, cx,
					cy + mOvershootPx / 2 - (mProgressTextPaint.descent() + mProgressTextPaint.ascent()) / 2,
					mProgressTextPaint);

			break;
		case ANIMATING_MANUAL_PROGRESS:

			mProgressPaint.setStrokeWidth(mStrokeWidth);
			canvas.drawArc(mCircleBounds, -90, mCurrentGlobalManualProgressValue, false, mProgressPaint);

			progress = (float) ((int) (mCurrentGlobalManualProgressValue * 10 / 3.6)) / 10;
			canvas.drawText("" + progress, cx,
					cy + mOvershootPx / 2 - (mProgressTextPaint.descent() + mProgressTextPaint.ascent()) / 2,
					mProgressTextPaint);

			break;
		case ANIMATING_SUCCESS:
			mDrawingPaint.setStrokeWidth(mLineWidth);

			if (mSuccessType == SuccessType.TYPE_OK) {
				canvas.drawArc(mCircleBounds, 0, 360, false, mDrawingPaint);
				canvas.drawLine(cx - mRadius / 2 + mSuccessValue * 2 - mSuccessValue / (float) Math.sqrt(2f) / 2,
						cy + mSuccessValue,
						cx + mSuccessValue * 2 - mSuccessValue / (float) Math.sqrt(2f) / 2,
						cy - mSuccessValue, mDrawingPaint);
				canvas.drawLine(cx - mSuccessValue - 2 * mSuccessValue / (float) Math.sqrt(2f) / 2, cy,
						cx + mRadius / 2 - mSuccessValue * 2 - mSuccessValue / (float) Math.sqrt(2f) / 2,
						cy + mSuccessValue, mDrawingPaint);
			} else if (mSuccessType == SuccessType.TYPE_START) {

				canvas.save();
				canvas.translate(cx - mRadius / 4,
						cy + mOvershootPx / 2 + (float) Math.sin(Math.toRadians(60)) * mRadius / 2);
				canvas.drawPath(mPath, mDrawingPaint);
				canvas.restore();
			}
			break;
		case ANIMATING_ERROR:
			mDrawingPaint.setStrokeWidth(mLineWidth);
			canvas.drawArc(mCircleBounds, 0, 360, false, mDrawingPaint);

			canvas.drawLine(cx - mRadius / 2 - mRadius / 4 + mErrorValue * 2, cy + mErrorValue + mOvershootPx / 2,
					cx + mErrorValue, cy - mErrorValue + mOvershootPx / 2, mDrawingPaint);
			canvas.drawLine(cx - mErrorValue, cy - mErrorValue + mOvershootPx / 2,
					cx + mRadius / 2 + mRadius / 4 - mErrorValue * 2, cy + mErrorValue + mOvershootPx / 2,
					mDrawingPaint);
			break;
		}
		if (mDotToProgressAnimatedValue > 0) {
			canvas.drawCircle(cx, cy - mDotToProgressAnimatedValue + mOvershootPx / 2, mStrokeWidth / 2, mDrawingPaint);
		}

		if (mDotToProgressAnimation.isRunning() && !mArrowLineToHorizontalLine.isRunning()) {
			canvas.drawLine(cx - mRadius / 2 - mArrowLineToHorizontalLineAnimatedValue / 2, cy,
					cx + mRadius / 2 + mArrowLineToHorizontalLineAnimatedValue / 2, cy, mDrawingPaint);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawing(canvas);
	}

	public void playToSuccess() {
		mResultState = State.ANIMATING_SUCCESS;
		mWhichProgress = State.ANIMATING_PROGRESS;
		mArrowToLineAnimatorSet.start();
		invalidate();
	}

	public boolean playToError() {
		mWhichProgress = State.ANIMATING_PROGRESS;
		mResultState = State.ANIMATING_ERROR;
		mArrowToLineAnimatorSet.start();
		invalidate();
        return true;
	}

	public boolean playManualProgressAnimation() {
	    if(mState != State.IDLE)
	        return false;
		mWhichProgress = State.ANIMATING_MANUAL_PROGRESS;
		mResultState = State.ANIMATING_SUCCESS;
		mArrowToLineAnimatorSet.start();
		invalidate();
        return true;
	}
	
	public void setSuceeceState() {
	    if(mSuccessType == SuccessType.TYPE_OK) {
	        mSuccessAnimation.start();
	    } else if(mSuccessType == SuccessType.TYPE_START) {
            mPath.reset();
            mPath.moveTo(0, 0);
            mSucessAnimatorSet.start();
	    }
	}

	public boolean abortDownload() {
		// if(mExpandAnimation.isRunning() || mProgressAnimation.isRunning()) {
		// mProgressAnimationSet.cancel();
		// mCollapseAnimation.start();
		// invalidate();
		// }
        if(mState == State.IDLE || mState == State.ANIMATING_SUCCESS)
            return false;
        if(mArrowToLineAnimatorSet.isRunning()) {
            mArrowToLineAnimatorSet.cancel();
            invalidate();
        }
		if (mProgressAnimation.isRunning()) {
			mProgressAnimationSet.cancel();
			invalidate();
		}
		if(mManualProgressAnimation.isRunning()) {
			mManualProgressAnimation.cancel();
			invalidate();
		}
		mErrorAnimation.start();
        return true;
	}

	public void setErrorResultState() {
		if (mSucessAnimatorSet.isRunning() || mSuccessAnimation.isRunning() || mErrorAnimation.isRunning())
			return;
		mResultState = State.ANIMATING_ERROR;
		invalidate();
	}

	public void setSuccessResultState() {
		if (mSucessAnimatorSet.isRunning() || mSuccessAnimation.isRunning() || mErrorAnimation.isRunning())
			return;
		mResultState = State.ANIMATING_SUCCESS;
		invalidate();
	}

	public void setProgress(int value) {
		if (value < 1 || value > 100)
			return;
		mToArc = value * 3.6f;
		mManualProgressAnimation.setFloatValues(mFromArc, mToArc);
		mManualProgressAnimation.start();
		mFromArc = mToArc;
		invalidate();
	}

	public interface OnProgressUpdateListener {
		void onProgressUpdate(float currentPlayTime);

		void onAnimationStarted();

		void onAnimationEnded();

		void onAnimationSuccess();

		void onAnimationError();

		void onManualProgressStarted();

		void onManualProgressEnded();
	}

	public void setOnProgressUpdateListener(OnProgressUpdateListener listener) {
		mOnProgressUpdateListener = listener;
	}

	@Override
	protected Parcelable onSaveInstanceState() {
	    Log.v(TAG, "onSaveInstanceState");
		Parcelable superState = super.onSaveInstanceState();
		SavedState savedState = new SavedState(superState);
		savedState.mState = mState;
		savedState.mmCurrentPlayTime = getCurrentPlayTimeByState(mState);
		return savedState;
	}

	private long[] getCurrentPlayTimeByState(State mState) {
		long[] tab = new long[3];
		switch (mState) {
		case ANIMATING_LINE_TO_DOT:
			for (int i = 0; i < mArrowToLineAnimatorSet.getChildAnimations().size(); i++) {
				tab[i] = ((ValueAnimator) mArrowToLineAnimatorSet.getChildAnimations().get(i)).getCurrentPlayTime();
			}
			mArrowToLineAnimatorSet.cancel();
			break;
		case ANIMATING_PROGRESS:
			for (int i = 0; i < mProgressAnimationSet.getChildAnimations().size(); i++) {
				tab[i] = ((ValueAnimator) mProgressAnimationSet.getChildAnimations().get(i)).getCurrentPlayTime();
			}
			mProgressAnimationSet.cancel();
			break;
		case ANIMATING_ERROR:
			tab[0] = mErrorAnimation.getCurrentPlayTime();
			mErrorAnimation.cancel();
			break;
		case ANIMATING_SUCCESS:
			if (mSuccessType == SuccessType.TYPE_OK) {
				tab[0] = mSuccessAnimation.getCurrentPlayTime();
				mSuccessAnimation.cancel();
			} else if (mSuccessType == SuccessType.TYPE_START) {
				for (int i = 0; i < mSucessAnimatorSet.getChildAnimations().size(); i++) {
					tab[i] = ((ValueAnimator) mSucessAnimatorSet.getChildAnimations().get(i)).getCurrentPlayTime();
				}
				mSucessAnimatorSet.cancel();
			}
			break;
		}
		return tab;
	}

	private void setCurrentPlayTimeByStateAndPlay(long[] tab, State state) {
		switch (state) {
		case ANIMATING_LINE_TO_DOT:
			mArrowToLineAnimatorSet.start();
			for (int i = 0; i < mArrowToLineAnimatorSet.getChildAnimations().size(); i++) {
				((ValueAnimator) mArrowToLineAnimatorSet.getChildAnimations().get(i)).setCurrentPlayTime(tab[i]);
			}
			break;
		case ANIMATING_PROGRESS:
			mProgressAnimationSet.start();
			for (int i = 0; i < mProgressAnimationSet.getChildAnimations().size(); i++) {
				((ValueAnimator) mProgressAnimationSet.getChildAnimations().get(i)).setCurrentPlayTime(tab[i]);
			}
			break;
		case ANIMATING_ERROR:
			mErrorAnimation.start();
			mErrorAnimation.setCurrentPlayTime(tab[0]);
			break;
		case ANIMATING_SUCCESS:
			if (mSuccessType == SuccessType.TYPE_OK) {
				mSuccessAnimation.start();
				mSuccessAnimation.setCurrentPlayTime(tab[0]);
			} else if (mSuccessType == SuccessType.TYPE_START) {
				mSucessAnimatorSet.start();
				for (int i = 0; i < mSucessAnimatorSet.getChildAnimations().size(); i++) {
					((ValueAnimator) mSucessAnimatorSet.getChildAnimations().get(i)).setCurrentPlayTime(tab[i]);
				}
			}
			break;
		}
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state instanceof SavedState) {
			SavedState savedState = (SavedState) state;
			if(savedState.mState != null)
			    mState = savedState.mState;
			super.onRestoreInstanceState(savedState.getSuperState());
			if (mState != State.IDLE) {
				continueAnimation(mState, savedState.mmCurrentPlayTime);
			}
		} else {
			super.onRestoreInstanceState(state);
		}
	}

	private void continueAnimation(State state, long[] mmCurrentPlayTime) {
	    if(state != null)
	        setCurrentPlayTimeByStateAndPlay(mmCurrentPlayTime, mState);
	}

	static class SavedState extends BaseSavedState {

		private boolean isFlashing;
		private boolean isConfigurationChanged;
		private long[] mmCurrentPlayTime;
		private State mState;

		public SavedState(Parcel source) {
			super(source);
			isFlashing = source.readInt() == 1;
			isConfigurationChanged = source.readInt() == 1;
			mmCurrentPlayTime = source.createLongArray();
		}

		public SavedState(Parcelable superState) {
			super(superState);
		}

		@Override
		public void writeToParcel(@NonNull Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(isFlashing ? 1 : 0);
			dest.writeInt(isConfigurationChanged ? 1 : 0);
			dest.writeLongArray(mmCurrentPlayTime);

		}

		public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {

			@Override
			public SavedState createFromParcel(Parcel source) {
				return new SavedState(source);
			}

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}
	
	public boolean isPlay() {
		return mState != State.IDLE;
	}
}
