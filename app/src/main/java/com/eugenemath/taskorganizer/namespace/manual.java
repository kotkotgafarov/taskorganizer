package com.eugenemath.taskorganizer.namespace;



import com.eugenemath.taskorganizer.namespace.R;
import com.eugenemath.taskorganizer.namespace.taskorganizerActivity.MyGestureDetector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class manual extends Activity {

	public int CurrentPage = 1, TotalPages = 8;
	private GestureDetector myGestureDetector; 
	private View.OnTouchListener myGestureListener;
	//boolean itWasMowing = false;

	@Override  
	public boolean onTouchEvent(MotionEvent event) {  
		if (myGestureDetector.onTouchEvent(event))
		{
			//tasksListView.cancelLongPress();
			return true;
		}
		else
		{
			return false;
		}
		//return myGestureDetector.onTouchEvent(event);   
	} 	


	public class MyGestureDetector extends SimpleOnGestureListener{ 

		private static final int SWIPE_MIN_DISTANCE = 120;
		private static final int SWIPE_MAX_OFF_PATH = 250;
		private static final int SWIPE_THRESHOLD_VELOCITY = 50;

		@Override
		public void onLongPress(MotionEvent e) {
			//if (longClickedItem != -1) {
			//itWasMowing = false;
			//Toast.makeText(getBaseContext(), "Long click!", Toast.LENGTH_LONG).show();
			//}           
		}	  

		@Override 
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
		{

				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH){
					//Toast.makeText(getBaseContext(), "Fling"+(e1.getY() - e2.getY()) , Toast.LENGTH_SHORT).show();
					//itWasMowing = false;
					return false;
				}
				// right to left swipe
				if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
				{
					if (CurrentPage<TotalPages)
					{
						CurrentPage ++;
						SetCurrentPage();
					}

					return true;
				}
				else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)//right
				{
					if (CurrentPage>1)
					{
						CurrentPage --;
						SetCurrentPage();
					}
					return true;
				}		
			



			return false;
		}
	}

	public void SetCurrentPage()
	{
		TextView textView_manualhead = (TextView)findViewById(R.id.textView_manualhead);
		TextView textView_manual = (TextView)findViewById(R.id.textView_manual);
		
		CharSequence styledText = "";
		String head = "";
		
		if (CurrentPage ==1)
		{
			styledText = Html.fromHtml(getString(R.string.page1));
			head = getString(R.string.titlepage1);
		}
		else  if (CurrentPage ==2)
		{
			styledText = Html.fromHtml(getString(R.string.page2));
			head = getString(R.string.titlepage2);
		}
		else  if (CurrentPage ==3)
		{
			styledText = Html.fromHtml(getString(R.string.page3));
			head = getString(R.string.titlepage3);
		}
		else  if (CurrentPage ==4)
		{
			styledText = Html.fromHtml(getString(R.string.page4));
			head = getString(R.string.titlepage4);
		}
		else  if (CurrentPage ==5)
		{
			styledText = Html.fromHtml(getString(R.string.page5));
			head = getString(R.string.titlepage5);
		}	
		else  if (CurrentPage ==6)
		{
			styledText = Html.fromHtml(getString(R.string.page6));
			head = getString(R.string.titlepage6);
		}
		else  if (CurrentPage ==7)
		{
			styledText = Html.fromHtml(getString(R.string.page7));
			head = getString(R.string.titlepage7);
		}		 		 
		else  if (CurrentPage ==8)
		{
			styledText = Html.fromHtml(getString(R.string.page8));
			head = getString(R.string.titlepage8);
		}	
		
		textView_manualhead.setText("("+CurrentPage+"/"+TotalPages+") "+head);
		textView_manual.setText(styledText);
		//WebView myWebView = (WebView) findViewById(R.id.webview);
		//myWebView.loadData("<html><body>"+getString(R.string.page1)+"</body></html>", "text/html", "UTF-8");
		
	}



	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Inflate your view
		setContentView(R.layout.manual);
		//final EditText myEditText = (EditText)findViewById(R.id.editnametextfield);
		TextView textView_manualhead = (TextView)findViewById(R.id.textView_manualhead);
		TextView textView_manual = (TextView)findViewById(R.id.textView_manual);
		//ImageView imageview_separator1 = (ImageView)findViewById(R.id.imageview_separator1);

		//textView_manualhead.setTextColor(Color.DKGRAY);
		textView_manual.setTextColor(Color.DKGRAY);

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int width = metrics.widthPixels;

		LinearLayout.LayoutParams params;
		params =  new LinearLayout.LayoutParams(width, 2);
		//imageview_separator1.setLayoutParams(params);		
		//imageview_separator1.setBackgroundColor(Color.DKGRAY);


		CurrentPage = 1;
		SetCurrentPage();

		myGestureDetector = new GestureDetector(new MyGestureDetector());
		// myGestureDetector.setIsLongpressEnabled(false);
		myGestureListener = new View.OnTouchListener()
		{
			//@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				return myGestureDetector.onTouchEvent(event);
			}
		};   

		LinearLayout linearLayout_manual =(LinearLayout)findViewById(R.id.linearLayout_manual);	
		linearLayout_manual.setOnTouchListener(myGestureListener); 

		ScrollView scrollView_manual =(ScrollView)findViewById(R.id.scrollView_manual);	
		scrollView_manual.setOnTouchListener(myGestureListener); 


	}

}