package com.eugenemath.taskorganizer.namespace;

 

import com.eugenemath.taskorganizer.namespace.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class additionalfunctions extends Activity {

	private static final String PREFS_NAME = "PrefsTaskOrganizer";
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		// Inflate your view
		setContentView(R.layout.additionalfunctions);
		
		

		OnClickListener OnClickListener1 = new OnClickListener()
		{
			public void onClick(View v) {
				CommonFunctions.ReadCalendars(additionalfunctions.this);
				Intent intent = new Intent();
				intent.putExtra("Result", 1);
				setResult(RESULT_OK,intent);
				finish();					
			}
		};
		
		ImageView imageView_menu1 = (ImageView)findViewById(R.id.imageView_menu1);
		imageView_menu1.setOnClickListener(OnClickListener1);
		
		
		TextView textView_menu1 = (TextView)findViewById(R.id.textView_menu1);
		textView_menu1.setOnClickListener(OnClickListener1);
		
		

		OnClickListener OnClickListener2 = new OnClickListener()
		{
			public void onClick(View v) {
				CommonFunctions.WriteTasksToCalendar(additionalfunctions.this);
				Intent intent = new Intent();
				intent.putExtra("Result", 2);
				setResult(RESULT_OK,intent);
				finish();					
			}
		};
		
		ImageView imageView_menu2 = (ImageView)findViewById(R.id.imageView_menu2);
		imageView_menu2.setOnClickListener(OnClickListener2);
		
		
		TextView textView_menu2 = (TextView)findViewById(R.id.textView_menu2);
		textView_menu2.setOnClickListener(OnClickListener2);		
		
		
		OnClickListener OnClickListener3 = new OnClickListener()
		{
			public void onClick(View v) {
				CommonFunctions.ExportToXML(additionalfunctions.this);
				Intent intent = new Intent();
				intent.putExtra("Result", 3);
				setResult(RESULT_OK,intent);
				finish();					
			}
		};
		
		ImageView imageView_menu3 = (ImageView)findViewById(R.id.imageView_menu3);
		imageView_menu3.setOnClickListener(OnClickListener3);
		
		
		TextView textView_menu3 = (TextView)findViewById(R.id.textView_menu3);
		textView_menu3.setOnClickListener(OnClickListener3);		
		
		
		OnClickListener OnClickListener4 = new OnClickListener()
		{
			public void onClick(View v) {
				CommonFunctions.ImportFromXML(additionalfunctions.this);
				Intent intent = new Intent();
				intent.putExtra("Result", 4);
				setResult(RESULT_OK,intent);
				finish();					
			}
		};
		
		ImageView imageView_menu4 = (ImageView)findViewById(R.id.imageView_menu4);
		imageView_menu4.setOnClickListener(OnClickListener4);
		
		
		TextView textView_menu4 = (TextView)findViewById(R.id.textView_menu4);
		textView_menu4.setOnClickListener(OnClickListener4);		
		
				
		OnClickListener OnClickListener5 = new OnClickListener()
		{
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("Result", 5);
				setResult(RESULT_OK,intent);
				finish();					
			}
		};
		
		ImageView imageView_menu5 = (ImageView)findViewById(R.id.imageView_menu5);
		imageView_menu5.setOnClickListener(OnClickListener5);
		
		
		TextView textView_menu5 = (TextView)findViewById(R.id.textView_menu5);
		textView_menu5.setOnClickListener(OnClickListener5);		
		
		OnClickListener OnClickListener6 = new OnClickListener()
		{
			public void onClick(View v) {
				
				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
				
				MyMail mymail = new MyMail();

				mymail._host = settings.getString("imap", "imap.gmail.com");
				mymail._user =settings.getString("user", "");
				mymail._pass = settings.getString("pass", "");
				String lastemailid = settings.getString("lastemailid", "0");
				try {
				     mymail.lastnumber = Long.valueOf(lastemailid);
				}
				catch(NumberFormatException e)
				{
					mymail.lastnumber = 0;
				}

				mymail.act =  getBaseContext();
				mymail.istest = false;
				
				try {
					boolean success = mymail.receive();
					
					if (success){
						Toast.makeText(getBaseContext(), getString(R.string.from_code16), Toast.LENGTH_SHORT).show();
						
						if (mymail.newlastnumber> mymail.lastnumber)
						{
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("lastemailid", ""+mymail.newlastnumber);
							editor.commit();
						}
						
					}
					else
					{
						Toast.makeText(getBaseContext(), getString(R.string.from_code17), Toast.LENGTH_SHORT).show();
					}
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Toast.makeText(getBaseContext(), getString(R.string.from_code17), Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}				
				
				Intent intent = new Intent();
				intent.putExtra("Result", 6);
				setResult(RESULT_OK,intent);
				finish();					
			}
		};
		
		ImageView imageView_menu6 = (ImageView)findViewById(R.id.imageView_menu6);
		imageView_menu6.setOnClickListener(OnClickListener6);
		
		
		TextView textView_menu6 = (TextView)findViewById(R.id.textView_menu6);
		textView_menu6.setOnClickListener(OnClickListener6);						
			
		
		OnClickListener OnClickListener7 = new OnClickListener()
		{
			public void onClick(View v) {
				Intent intent = new Intent(additionalfunctions.this, ConsumedTime.class);
				startActivity(intent);
				finish();					
			}
		};
			
		
		
		ImageView imageView_menu7 = (ImageView)findViewById(R.id.imageView_menu7);
		imageView_menu7.setOnClickListener(OnClickListener7);
		
		
		TextView textView_menu7 = (TextView)findViewById(R.id.textView_menu7);
		textView_menu7.setOnClickListener(OnClickListener7);	
		
		
		
	}
}