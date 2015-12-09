package com.yzd.smartgarage.util;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

public class MyImageView extends ImageView
{

	private static final String TAG = "MyImageView";

	private static final int SCALE_REDUCE_INIT = 0;
	private static final int SCALING = 1;
	private static final int SCALE_ADD_INIT = 6;

	/**
	 * 控件的宽
	 */
	private int mWidth;
	/**
	 * 控件的高
	 */
	private int mHeight;
	/**
	 * 控件的宽1/2
	 */
	private int mCenterWidth;
	/**
	 * 控件的高 1/2
	 */
	private int mCenterHeight;
	/**
	 * 设置�?个缩放的常量
	 */
	private float mMinScale = 0.85f;
	/**
	 * 缩放是否结束
	 */
	private boolean isFinish = true;

	public MyImageView(Context context)
	{
		this(context, null);
	}

	public MyImageView(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public MyImageView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	/**
	 * 必要的初始化
	 */
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom)
	{
		super.onLayout(changed, left, top, right, bottom);
		if (changed)
		{
			mWidth = getWidth() - getPaddingLeft() - getPaddingRight();
			mHeight = getHeight() - getPaddingTop() - getPaddingBottom();

			mCenterWidth = mWidth / 2;
			mCenterHeight = mHeight / 2;

			Drawable drawable = getDrawable();
			BitmapDrawable bd = (BitmapDrawable) drawable;
			bd.setAntiAlias(true);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		switch (event.getAction())
		{
		case MotionEvent.ACTION_DOWN:
			float X = event.getX();
			float Y = event.getY();
			mScaleHandler.sendEmptyMessage(SCALE_REDUCE_INIT);
			break;
		case MotionEvent.ACTION_UP:
			mScaleHandler.sendEmptyMessage(SCALE_ADD_INIT);
			break;
		}
		return true;
	}

	/**
	 * 控制缩放的Handler
	 */
	private Handler mScaleHandler = new Handler()
	{
		private Matrix matrix = new Matrix();
		private int count = 0;
		private float s;
		/**
		 * 是否已经调用了点击事�?
		 */
		private boolean isClicked;

		public void handleMessage(android.os.Message msg)
		{
			matrix.set(getImageMatrix());
			switch (msg.what)
			{
			case SCALE_REDUCE_INIT:
				if (!isFinish)
				{
					mScaleHandler.sendEmptyMessage(SCALE_REDUCE_INIT);
				} else
				{
					isFinish = false;
					count = 0;
					s = (float) Math.sqrt(Math.sqrt(mMinScale));
					beginScale(matrix, s);
					mScaleHandler.sendEmptyMessage(SCALING);
				}
				break;
			case SCALING:
				beginScale(matrix, s);
				if (count < 4)
				{
					mScaleHandler.sendEmptyMessage(SCALING);
				} else
				{
					isFinish = true;
					if (MyImageView.this.mOnViewClickListener != null && !isClicked)
					{
						isClicked = true;
						MyImageView.this.mOnViewClickListener.onViewClick(MyImageView.this);
					} else
					{
						isClicked = false;
					}
				}
				count++;

				break;
			case 6:
				if (!isFinish)
				{
					mScaleHandler.sendEmptyMessage(SCALE_ADD_INIT);
				} else
				{
					isFinish = false;
					count = 0;
					s = (float) Math.sqrt(Math.sqrt(1.0f / mMinScale));
					beginScale(matrix, s);
					mScaleHandler.sendEmptyMessage(SCALING);
				}
				break;
			}
		}
	};

	protected void sleep(int i)
	{
		try
		{
			Thread.sleep(i);
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 缩放
	 * 
	 * @param matrix
	 * @param scale
	 */
	private synchronized void beginScale(Matrix matrix, float scale)
	{
		matrix.postScale(scale, scale, mCenterWidth, mCenterHeight);
		setImageMatrix(matrix);
	}

	/**
	 * 回调接口
	 */
	private OnViewClickListener mOnViewClickListener;

	public void setOnClickIntent(OnViewClickListener onViewClickListener)
	{
		this.mOnViewClickListener = onViewClickListener;
	}

	public interface OnViewClickListener
	{
		void onViewClick(MyImageView view);
	}

}
