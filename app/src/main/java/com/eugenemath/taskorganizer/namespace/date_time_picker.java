package com.eugenemath.taskorganizer.namespace;



import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import com.eugenemath.taskorganizer.namespace.R;
import com.eugenemath.taskorganizer.namespace.taskorganizerActivity.GridCellAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.SingleLineTransformationMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TimePicker;
import android.widget.Toast;

public class date_time_picker extends Activity {


	public int monthview_currentmonth,monthview_currentyear;

	public Calendar _calendar;

	private int choose_time = 0, begin_of_the_day_or_end=0;
	private String button;

	//private DatePicker myDatePicker;
	private TimePicker myTimePicker;
	private int regim = 0;
	private int y;
	private Date ChosenDate = new Date(0);
	private Button ChosenButton;




	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		Intent i = getIntent();
		Bundle extras =  i.getExtras();


		_calendar = Calendar.getInstance(Locale.getDefault());
		monthview_currentmonth = _calendar.get(Calendar.MONTH) + 1;
		monthview_currentyear = _calendar.get(Calendar.YEAR);



		// Inflate your view

		//setContentView(R.layout.datetimepicker);

		WindowManager winMan = (WindowManager) getBaseContext().getSystemService(Context.WINDOW_SERVICE);


		/*WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
	    lp.copyFrom(getWindow().getAttributes());
	    lp.dimAmount = 0.6f;
	    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
	    lp.height = WindowManager.LayoutParams.WRAP_CONTENT;*/
		// d.getWindow().setAttributes(lp);


		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		if (extras.containsKey("y"))
		{
			y = extras.getInt("y");
		}
		else
		{
			y = (metrics.heightPixels - metrics.widthPixels)/2;
			//y = 200;
		}

		if (extras.containsKey("mseconds"))
		{
			ChosenDate = new Date(extras.getLong("mseconds"));
		}



		/*int width = (int)(metrics.widthPixels/7);
		int height = (int)(metrics.heightPixels/7);

	    int length_of_side = (int)Math.min(width*0.8f, height*0.8f)-2;
	    lp.width = (length_of_side+2)*7;
	    lp.height = LayoutParams.WRAP_CONTENT;
	    lp.horizontalMargin = 0;
	    lp.verticalMargin = 0;
	    lp.horizontalWeight = 1;*/
		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		//getWindow().setDimAmount(0.6f);
		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		//getWindow().addFlags(flags)
		//getWindow().setDimAmount(0.6f);
		//getWindow().setAttributes(lp);

		//((Intent) getWindow().getAttributes()).addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);  
		//getWindow().getAttributes().dimAmount = 0.6f;
		//Toast.makeText(this, "on create", Toast.LENGTH_SHORT).show();

		//setContentView(R.layout.datetimepicker);



		if (winMan != null)
		{
			int orientation = winMan.getDefaultDisplay().getOrientation();

			
			if (metrics.heightPixels<metrics.widthPixels)
			{
				orientation = 1;
			}
			else
			{
				orientation = 0;
			}
			
			if (orientation == 0) {
				// Portrait
				setContentView(R.layout.datetimepicker);
				getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				getWindow().setGravity(Gravity.TOP);
				WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
				lp.copyFrom(getWindow().getAttributes());

				lp.y = y;
				getWindow().setAttributes(lp);
			}
			else if (orientation == 1) {
				// Landscape
				//setContentView(R.layout.datetimepicker_landscape);
				setContentView(R.layout.datetimepicker);
				getWindow().setLayout(metrics.heightPixels, LayoutParams.WRAP_CONTENT);
			}       
			
		}	

		if (extras.containsKey("regim"))
		{
			regim = extras.getInt("regim");
		}

		FillMonth();



		button = i.getExtras().getString("button");
		choose_time = i.getExtras().getInt("choose_time");
		begin_of_the_day_or_end = i.getExtras().getInt("begin_of_the_day_or_end");
		Long mseconds = i.getExtras().getLong("mseconds");
		Date date0 = new Date(mseconds);





		Date emptyDate = new Date(0);
		//myDatePicker = (DatePicker)findViewById(R.id.datePicker1);
		myTimePicker = (TimePicker)findViewById(R.id.timePicker1);
		myTimePicker.setIs24HourView(true);	


		LinearLayout linearLayout_temp = (LinearLayout)this.findViewById(R.id.linearLayout_temp);


		if (choose_time == 0)
		{
			linearLayout_temp.setVisibility(View.GONE);
		}	
		
		LinearLayout linearLayout_day_of_week = (LinearLayout)this.findViewById(R.id.linearLayout_day_of_week);


		if (regim == 1)
		{
			linearLayout_day_of_week.setVisibility(View.GONE);
		}	

		if (date0 != emptyDate)
		{
			//myDatePicker.updateDate(date0.getYear()+1900, date0.getMonth(), date0.getDate());
			myTimePicker.setCurrentHour(date0.getHours());	
			myTimePicker.setCurrentMinute(date0.getMinutes());			
		}		
		else
		{
			Date currentDate = new Date();
			//myDatePicker.updateDate(currentDate.getYear(), currentDate.getMonth(), currentDate.getDate());
			if (begin_of_the_day_or_end==0)
			{
				myTimePicker.setCurrentHour(0);	
				myTimePicker.setCurrentMinute(0);		
			}
			else
			{
				myTimePicker.setCurrentHour(23);	
				myTimePicker.setCurrentMinute(59);			
			}
		}

		Button button_back = (Button)this.findViewById(R.id.button_back);
		button_back.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (regim == 1)
				{
					monthview_currentyear--;
				}
				else {
					if (monthview_currentmonth==1)
					{
						monthview_currentmonth=12;
						monthview_currentyear--;
					}
					else
					{
						monthview_currentmonth--;					
					}	
				}
				FillMonth();
			}});



		Button button_forward = (Button)this.findViewById(R.id.button_forward);
		button_forward.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (regim == 1)
				{
					monthview_currentyear++;
				}
				else {
					if (monthview_currentmonth==12)
					{
						monthview_currentmonth=1;
						monthview_currentyear++;
					}
					else
					{
						monthview_currentmonth++;					
					}		
				}
				FillMonth();
			}});



		TextView textview_back = (TextView)this.findViewById(R.id.textview_back);
		textview_back.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (regim == 1)
				{
					monthview_currentyear--;
				}
				else {
					if (monthview_currentmonth==1)
					{
						monthview_currentmonth=12;
						monthview_currentyear--;
					}
					else
					{
						monthview_currentmonth--;					
					}	
				}
				FillMonth();
			}});


		TextView textview_forward = (TextView)this.findViewById(R.id.textview_forward);
		textview_forward.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (regim == 1)
				{
					monthview_currentyear++;
				}
				else {
					if (monthview_currentmonth==12)
					{
						monthview_currentmonth=1;
						monthview_currentyear++;
					}
					else
					{
						monthview_currentmonth++;					
					}		
				}
				FillMonth();
			}});




		Button button_date_time_picker_ok = (Button)findViewById(R.id.button_date_time_picker_ok);


		button_date_time_picker_ok.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("button", button);

				Date date0 = new Date();

				if (ChosenDate.equals(new Date(0)))
				{
					ChosenDate = date0;
				}


				if (choose_time == 0)
				{
					date0 = new Date(ChosenDate.getYear(),ChosenDate.getMonth(),ChosenDate.getDate());
				}
				else
				{
					date0 = new Date(ChosenDate.getYear(),ChosenDate.getMonth(),ChosenDate.getDate(),myTimePicker.getCurrentHour(),myTimePicker.getCurrentMinute(),0);
				}

				intent.putExtra("mseconds", date0.getTime());
				setResult(RESULT_OK,intent);
				finish();	
			}});	

		

	}

	public void FillMonth()
	{
		/*calendarView = (GridView) this.findViewById(R.id.calendar);
		//calendarView.removeAllViews();

		monthadapter = new GridCellAdapter(getApplicationContext(), monthview_currentmonth, monthview_currentyear);
		//monthadapter.notifyDataSetChanged();
		calendarView.setAdapter(monthadapter);	*/





		if (regim !=1)
		{

			printButtonsInMonth();
			Date date0 = new Date(monthview_currentyear-1900,monthview_currentmonth-1,1);
			TextView calendar_period = (TextView)this.findViewById(R.id.filter_description);
			int version = Integer.valueOf(android.os.Build.VERSION.SDK);
			SimpleDateFormat sdf_yyyyMMdd;
			if (version<9)
			{
				sdf_yyyyMMdd = new SimpleDateFormat("MMMM yyyy");
			}
			else
			{
				sdf_yyyyMMdd = new SimpleDateFormat("LLLL yyyy");
			}
			
			
			String textforbutton  = sdf_yyyyMMdd.format(date0);
			calendar_period.setText(textforbutton);			


			SimpleDateFormat sdf = new SimpleDateFormat("MMMM");
			if (version<9)
			{
				sdf = new SimpleDateFormat("MMMM");
			}
			else
			{
				sdf = new SimpleDateFormat("LLLL");
			}
			TextView textview_back = (TextView)this.findViewById(R.id.textview_back);
			textview_back.setText(sdf.format(new Date(date0.getTime()-24*3600*1000)));

			TextView textview_forward = (TextView)this.findViewById(R.id.textview_forward);
			if (monthview_currentmonth==12)
			{
			    textview_forward.setText(sdf.format(new Date(monthview_currentyear-1900,0,1)));
			}
			else
			{
				 textview_forward.setText(sdf.format(new Date(monthview_currentyear-1900,monthview_currentmonth,1)));
			}
			//textview_forward.setText(sdf.format(new Date((new Date(monthview_currentyear-1900,monthview_currentmonth-1,28).getTime()+30*24*3600*1000))));
			//textview_forward.setText(sdf.format(new Date(date0.getTime()+33*24*3600*1000)));
		}
		else
		{
			printMonthsInMonth();
			Date date0 = new Date(monthview_currentyear-1900,monthview_currentmonth-1,1);
			TextView calendar_period = (TextView)this.findViewById(R.id.filter_description);
			SimpleDateFormat sdf_yyyyMMdd = new SimpleDateFormat("yyyy");
			String textforbutton  = sdf_yyyyMMdd.format(date0);
			calendar_period.setText(textforbutton);			

			TextView textview_back = (TextView)this.findViewById(R.id.textview_back);
			textview_back.setText(""+(monthview_currentyear-1));

			TextView textview_forward = (TextView)this.findViewById(R.id.textview_forward);
			textview_forward.setText(""+(monthview_currentyear+1));
		}

	}

	private int getNumberOfDaysOfMonth(int month,int year)
	{

		Date date0 =  new Date();

		if (month==12)
		{
			date0 = new Date(year-1900,11,31,0,0,0);//11 = december
		}
		else
		{
			date0 = new Date(date0.getYear()-1900,month,1,0,0,0);
			date0 = new Date(date0.getTime()-1);
		}		

		return date0.getDate();
	}

	private void printButtonsInMonth()
	{
		// The number of days to leave blank at
		// the start of this month.
		int trailingSpaces = 0;
		int leadSpaces = 0;
		int daysInPrevMonth = 0;
		int prevMonth = 0;
		int prevYear = 0;
		int nextMonth = 0;
		int nextYear = 0;


		RelativeLayout rl = (RelativeLayout) this.findViewById(R.id.relativelayout_for_month);
		rl.removeAllViews();


		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int width = (int)(metrics.widthPixels/7);
		int height = (int)(metrics.heightPixels/7);

		//int length_of_side = (int)Math.min(width, height)-2;
		int length_of_side = Math.min(width, height);
		// int length_of_side = (int)(getWindow().getAttributes().width/7)-2;


		//Button btn;



		int month = monthview_currentmonth;
		int year = monthview_currentyear;

		int daysInMonth = getNumberOfDaysOfMonth(month,year);

		if (month == 12)
		{
			prevMonth = month-1;
			//nextMonth = 0;
			nextMonth = 1;
			prevYear = year;
			nextYear = year + 1;
			daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth,prevYear);
		}
		else if (month == 1)
		{
			prevMonth = 12;
			prevYear = year - 1;
			nextYear = year;
			daysInPrevMonth = getNumberOfDaysOfMonth(12,prevYear);
			//nextMonth = 1;
			nextMonth = 2;
		}
		else
		{
			prevMonth = month - 1;
			nextMonth = month + 1;
			nextYear = year;
			prevYear = year;
			daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth,prevYear);
		}

		// Compute how much to leave before before the first day of the
		// month.
		// getDay() returns 0 for Sunday.

		// Gregorian Calendar : MINUS 1, set to FIRST OF MONTH
		GregorianCalendar cal = new GregorianCalendar(year, month-1, 1);	
		Date date0 = new Date(year-1900,month-1,1);
		int currentWeekDay = date0.getDay();
		int currentDayOfMonth = _calendar.get(Calendar.DATE);	
		//Toast.makeText(getBaseContext(), ""+date0.toString()+" "+currentWeekDay, Toast.LENGTH_SHORT).show();
		//int currentWeekDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
		int currentmonth = _calendar.get(Calendar.MONTH) + 1;
		int currentyear = _calendar.get(Calendar.YEAR);			

		if (currentWeekDay == 0)
		{
			currentWeekDay = 7;
		}
		trailingSpaces = currentWeekDay;

		//if (cal.isLeapYear(cal.get(Calendar.YEAR)) && month == 1)
		//{
		//	++daysInMonth;
		//}


		int row = 1;
		int column = 1;

		Resources res = getResources();

		Drawable button1_month_datepicker = res.getDrawable(R.drawable.button_date_time_picker_1_selector);
		Drawable button2_month_datepicker = res.getDrawable(R.drawable.button2_month_datepicker);
		Drawable button4_month_datepicker = res.getDrawable(R.drawable.button4_month_datepicker);
		RelativeLayout.LayoutParams params;


		// Trailing Month days
		for (int i = 1; i < trailingSpaces; i++)
		{
			//list.add(String.valueOf((daysInPrevMonth - trailingSpaces + 1) + i) + "-GREY" +  "-" +prevMonth+"-"+ prevYear);
			Button btn = new Button(this);
			btn.setTextAppearance(this, android.R.style.TextAppearance_Large);
			btn.setBackgroundDrawable(button2_month_datepicker);
			btn.setTag(String.valueOf((daysInPrevMonth - trailingSpaces + 1) + i) + "-GREY" +  "-" +prevMonth+"-"+ prevYear);
			btn.setText(""+(daysInPrevMonth - trailingSpaces + 1 + i));
			btn.setTextColor(Color.parseColor("#b2b2b2"));
			params =  new RelativeLayout.LayoutParams(length_of_side,length_of_side);
			params.leftMargin = length_of_side*(column-1);
			params.topMargin = length_of_side*(row-1);
			btn.setLayoutParams(params);
			rl.addView(btn, params);	
			if (column==7){column=1;row++;}else{column++;}
			btn.setOnClickListener(myOnClickListener);
		}

		// Current Month Days
		for (int i = 1; i <= daysInMonth; i++)
		{

			Button btn = new Button(this);
			btn.setBackgroundDrawable(button1_month_datepicker);
			btn.setTextAppearance(this, android.R.style.TextAppearance_Large);
			btn.setTextColor(Color.parseColor("#5f5f5f"));
			btn.setTag(""+String.valueOf(i) + "-BLUE" +  "-" +month+"-"+ year);
			btn.setText(""+i);
			params =  new RelativeLayout.LayoutParams(length_of_side,length_of_side);
			params.leftMargin = length_of_side*(column-1);
			params.topMargin = length_of_side*(row-1);
			btn.setLayoutParams(params);
			rl.addView(btn, params);	
			if (column==7){column=1;row++;}else{column++;}

			btn.setOnClickListener(myOnClickListener);
			//Log.d(currentMonthName, String.valueOf(i) + " " + getMonthAsString(currentMonth) + " " + year);
			if (i == currentDayOfMonth && currentmonth==month && currentyear ==year)
			{
				btn.setTextColor(Color.parseColor("#31b5e5"));
				//list.add(String.valueOf(i) + "-BLUE" +  "-" +month+"-"+ year);
			}
			else
			{
				//list.add(String.valueOf(i) + "-WHITE" +"-" +month+ "-" + year);
			}

			if (ChosenDate.getDate() == i && (ChosenDate.getMonth()+1) == currentmonth && (1900+ChosenDate.getYear())==currentyear)
			{
				btn.setBackgroundDrawable(button4_month_datepicker);
				btn.setTextColor(Color.parseColor("#ffffff"));
				ChosenButton = btn;
			}

		}

		int column0 = column;

		// Leading Month days
		for (int i = 0; i < (7-column0+1); i++)
		{
			Button btn = new Button(this);
			btn.setBackgroundDrawable(button2_month_datepicker);
			btn.setTextAppearance(this, android.R.style.TextAppearance_Large);
			btn.setTag(""+String.valueOf(i + 1) + "-GREY" +"-"+ nextMonth+"-"+ nextYear);
			btn.setText(""+(i+1));
			btn.setTextColor(Color.parseColor("#b2b2b2"));
			params =  new RelativeLayout.LayoutParams(length_of_side,length_of_side);
			params.leftMargin = length_of_side*(column-1);
			params.topMargin = length_of_side*(row-1);
			btn.setLayoutParams(params);
			rl.addView(btn, params);	
			if (column==7){column=1;row++;}else{column++;}
			btn.setOnClickListener(myOnClickListener);
			//Log.d(tag, "NEXT MONTH:= " + getMonthAsString(nextMonth));
			//list.add(String.valueOf(i + 1) + "-GREY" +"-"+ nextMonth+"-"+ nextYear);
		}
	}

	private void printMonthsInMonth()
	{



		RelativeLayout rl = (RelativeLayout) this.findViewById(R.id.relativelayout_for_month);
		rl.removeAllViews();


		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int width = (int)(metrics.widthPixels/4);
		int height = (int)(metrics.heightPixels/4);

		int length_of_side = Math.min(width, height);


		int month = monthview_currentmonth;
		int year = monthview_currentyear;



		// Gregorian Calendar : MINUS 1, set to FIRST OF MONTH
		GregorianCalendar cal = new GregorianCalendar(year, month-1, 1);	
		Date date0 = new Date(year-1900,month-1,1);
		int currentmonth = _calendar.get(Calendar.MONTH) + 1;
		int currentyear = _calendar.get(Calendar.YEAR);			



		int row = 1;
		int column = 1;

		Resources res = getResources();

		Drawable button1_month_datepicker = res.getDrawable(R.drawable.button_date_time_picker_1_selector);
		Drawable button2_month_datepicker = res.getDrawable(R.drawable.button2_month_datepicker);
		Drawable button4_month_datepicker = res.getDrawable(R.drawable.button4_month_datepicker);
		RelativeLayout.LayoutParams params;
 
		int version = Integer.valueOf(android.os.Build.VERSION.SDK);
		// Current Month Days
		for (int i = 1; i <= 12; i++)
		{
			Button btn = new Button(this);
			btn.setBackgroundDrawable(button1_month_datepicker);
			//btn.setTextAppearance(this, android.R.style.TextAppearance_Large);
			btn.setTextColor(Color.parseColor("#5f5f5f"));
			btn.setTag(""+1 + "-BLUE" +  "-" +i+"-"+ year);
			SimpleDateFormat sdf = new SimpleDateFormat("MMMM");
			if (version<9)
			{
				sdf = new SimpleDateFormat("MMMM");
			}
			else
			{
				sdf = new SimpleDateFormat("LLLL");
			}
			btn.setText(sdf.format(new Date(year,i-1,1)));
			params =  new RelativeLayout.LayoutParams(length_of_side,length_of_side);
			params.leftMargin = length_of_side*(column-1);
			params.topMargin = length_of_side*(row-1);
			btn.setLayoutParams(params);
			rl.addView(btn, params);	
			if (column==4){column=1;row++;}else{column++;}

			btn.setOnClickListener(myOnClickListener);
			if (currentmonth==i && currentyear ==year)
			{
				btn.setTextColor(Color.parseColor("#31b5e5"));
			}


			if ((ChosenDate.getMonth()+1) == i && (1900+ChosenDate.getYear())==currentyear)
			{
				btn.setBackgroundDrawable(button4_month_datepicker);
				btn.setTextColor(Color.parseColor("#ffffff"));
				ChosenButton = btn;
			}

		}

	}

	
	
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		//Toast.makeText(this, "onConfigurationChanged", Toast.LENGTH_SHORT).show();
		// Checks the orientation of the screen
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			//Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
			//Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
		}
	}

	OnClickListener myOnClickListener = new OnClickListener() {
		public void onClick(View view)
		{
			String date_month_year = (String) view.getTag();
			String[] date_month_year_array = date_month_year.split("-");



			if (choose_time == 0)
			{
				Date parsedDate = new Date(Integer.valueOf(date_month_year_array[3])-1900,Integer.valueOf(date_month_year_array[2])-1,Integer.valueOf(date_month_year_array[0]));

				Intent intent = new Intent();
				intent.putExtra("button", button);
				intent.putExtra("mseconds", parsedDate.getTime());
				setResult(RESULT_OK,intent);
				finish();	
			}
			else
			{
				Resources res = getResources();
				Drawable button1_month_datepicker = res.getDrawable(R.drawable.button_date_time_picker_1_selector);
				Drawable button2_month_datepicker = res.getDrawable(R.drawable.button2_month_datepicker);
				Drawable button4_month_datepicker = res.getDrawable(R.drawable.button4_month_datepicker);

				if (ChosenButton!=null)
				{
					
					if (date_month_year_array[1].equals("GREY"))
					{
						ChosenButton.setBackgroundDrawable(button2_month_datepicker);
						ChosenButton.setTextColor(Color.parseColor("#b2b2b2"));
					}
					else
					{
						ChosenButton.setBackgroundDrawable(button1_month_datepicker);
						ChosenButton.setTextColor(Color.parseColor("#5f5f5f"));
					}
					
					int currentDayOfMonth = _calendar.get(Calendar.DATE);	
					int currentmonth = _calendar.get(Calendar.MONTH) + 1;
					int currentyear = _calendar.get(Calendar.YEAR);			
					String[] date_month_year_array2 = ((String) ChosenButton.getTag()).split("-");
					if (Integer.valueOf(date_month_year_array2[0]) == currentDayOfMonth && currentmonth==Integer.valueOf(date_month_year_array2[2]) && currentyear ==Integer.valueOf(date_month_year_array2[3]))
					{
						ChosenButton.setTextColor(Color.parseColor("#31b5e5"));
					}
				}

				ChosenButton = (Button) view;

				ChosenButton.setBackgroundDrawable(button4_month_datepicker);
				ChosenButton.setTextColor(Color.parseColor("#ffffff"));

				Date parsedDate = new Date(Integer.valueOf(date_month_year_array[3])-1900,Integer.valueOf(date_month_year_array[2])-1,Integer.valueOf(date_month_year_array[0]));
				//myDatePicker.updateDate(parsedDate.getYear(), parsedDate.getMonth(), parsedDate.getDate());
				ChosenDate = parsedDate;
			}


			//Toast.makeText(getBaseContext(), ""+parsedDate.toString(), Toast.LENGTH_SHORT).show();

		}
	};




}