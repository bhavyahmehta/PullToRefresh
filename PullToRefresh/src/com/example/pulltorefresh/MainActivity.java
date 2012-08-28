package com.example.pulltorefresh;

/*
 * By Bhavya mehta
 * Version==1.0
 * */

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity  
{
	private ScrollView scroll = null;
	private int mLastMotionY=0,mRefreshState;
	private TextView mRefreshViewText;
    private ImageView mRefreshViewImage;
    private ProgressBar mRefreshViewProgress;
    private RotateAnimation mFlipAnimation;
    private RotateAnimation mReverseFlipAnimation;
    private static final int PULL_TO_REFRESH = 1;
    private static final int RELEASE_TO_REFRESH =2;
    private static final int REFRESHING = 3;
    String TAG=MainActivity.class.getCanonicalName();
    LinearLayout layout;
    LayoutInflater inflater;
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		setContentView(R.layout.main);
		super.onCreate(savedInstanceState);
		
		scroll = (ScrollView) findViewById(R.id.scroll);
		mRefreshViewText = (TextView) findViewById(R.id.pull_to_refresh_text);
        mRefreshViewImage = (ImageView) findViewById(R.id.pull_to_refresh_image);
        mRefreshViewProgress = (ProgressBar) findViewById(R.id.pull_to_refresh_progress);
        mRefreshViewProgress.setVisibility(View.GONE);
        mRefreshViewImage.setMinimumHeight(45);
        mRefreshState=PULL_TO_REFRESH;
      
        layout =(LinearLayout)findViewById(R.id.scroll_layout);
		inflater=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		
        // Load all of the animations we need in code rather than through XML
        mFlipAnimation = new RotateAnimation(0, -180,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mFlipAnimation.setInterpolator(new LinearInterpolator());
        mFlipAnimation.setDuration(250);
        mFlipAnimation.setFillAfter(true);
        
        mReverseFlipAnimation = new RotateAnimation(-180, 0,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
        mReverseFlipAnimation.setDuration(250);
        mReverseFlipAnimation.setFillAfter(true);
	
        scroll.setOnTouchListener(new TouchListen());
        
	}
	
	public class RefreshTask extends AsyncTask<Void, Void, Void>
	{
		
		@Override
		protected void onPreExecute() 
		{
			mRefreshViewImage.clearAnimation();
	        mRefreshViewText.setText(R.string.pull_to_refresh_refreshing_label);
	        mRefreshViewImage.setVisibility(View.GONE);
	        mRefreshViewProgress.setVisibility(View.VISIBLE);
			scroll.setEnabled(false);
	        super.onPreExecute();
		}
		
		@Override
		protected Void doInBackground(Void... params) 
		{
			try
			{
				Thread.sleep(2000);
				//Do your task
			} 
			catch (InterruptedException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) 
		{
			
			//added new view
			View row=inflater.inflate(R.layout.added_refresh, null);
			layout.addView(row);
			mRefreshViewProgress.setVisibility(View.GONE);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)scroll.getLayoutParams();
			params.setMargins(0,0, 0, 0); 
			scroll.setLayoutParams(params);					
			mRefreshViewText.setText(R.string.pull_to_refresh_pull_label); 
		    mRefreshViewImage.setVisibility(View.VISIBLE);		    
		    scroll.setEnabled(true);
		    mRefreshState=PULL_TO_REFRESH;
		    super.onPostExecute(result);
		}
		
	}

	public class TouchListen implements OnTouchListener
	{

		@Override
		public boolean onTouch(View v, MotionEvent event) 
		{
			Log.i(TAG,"inside ontouch"+" event.getAction()=="+event.getAction()+" v.getScrollY()=="+v.getScrollY());
    		
				final int y = (int) event.getY();
	    		switch(event.getAction())
				{
					case MotionEvent.ACTION_DOWN:
						mLastMotionY=y;
						break;
					case MotionEvent.ACTION_MOVE:
							
			                // Calculate the padding to apply, we divide by 1.7 to
			                // simulate a more resistant effect during pull.
			                int topPadding = (int) (((event.getY()-mLastMotionY)- 80) / 1.7);
							Log.e(TAG," topPadding=="+topPadding);
			                if(topPadding>=0)
			                {
			                	if(topPadding<=80)
								{
									if(mRefreshState==RELEASE_TO_REFRESH)
									{
										
										mRefreshState=PULL_TO_REFRESH ;
										mRefreshViewImage.clearAnimation();
										mRefreshViewText.setText(getString(R.string.pull_to_refresh_pull_label));
										mRefreshViewImage.startAnimation(mReverseFlipAnimation);
									}
								}
								else
								{
									if(mRefreshState==PULL_TO_REFRESH && v.getScrollY()>=0 && v.getScrollY()<=2)
									{
										mRefreshState=RELEASE_TO_REFRESH;
										mRefreshViewImage.clearAnimation();
										mRefreshViewText.setText(getString(R.string.pull_to_refresh_release_label));
										mRefreshViewImage.startAnimation(mFlipAnimation);
									}
									
								}
								
								RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)v.getLayoutParams();
								params.setMargins(0,topPadding, 0, 0); 
								v.setLayoutParams(params);
			                	
			                }
						
						break;
					case MotionEvent.ACTION_UP:
						Log.i(TAG,"Up event");
						if(mRefreshState==RELEASE_TO_REFRESH)
						{
							
							new RefreshTask().execute();
						}
						else
						{
							RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)v.getLayoutParams();
							params.setMargins(0,0, 0, 0); 
							v.setLayoutParams(params);
						
						}
						
						break;	
				}
			return false;
		}
		
	}
	
}
