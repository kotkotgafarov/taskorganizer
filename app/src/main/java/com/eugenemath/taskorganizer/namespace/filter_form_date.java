package com.eugenemath.taskorganizer.namespace;

 

import java.util.Calendar;
import java.util.Date;

import com.eugenemath.taskorganizer.namespace.R;
import com.eugenemath.taskorganizer.namespace.filter_form.MyOnItemSelectedListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

public class filter_form_date extends Activity {

	Date date01011970 = new Date(70,1,1);
	String[] date_filters;
	String[] compare_types_dates;	

	ArrayAdapter<String> spinnerArrayAdapter_date_filters;
	ArrayAdapter<String> spinnerArrayAdapter_date;	
	
	int CurrentField;
	
	
	public class MyOnItemSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent,
				View view, int pos, long id) {
			//if (view.getId() ==button_date_to.getId() )
			ShowHideElements();
		}
		public void onNothingSelected(AdapterView parent) {
			// Do nothing.
		}
	}			
	
	public void ShowHideElements()
	{
		Spinner spinner_date = (Spinner)findViewById(R.id.spinner_date_form_date);
		Spinner spinner_date_from = (Spinner)findViewById(R.id.spinner_date_from);
		Spinner spinner_date_to = (Spinner)findViewById(R.id.spinner_date_to);

		TableRow TableRow3 = (TableRow)findViewById(R.id.tableRow3);
		TableRow TableRow4 = (TableRow)findViewById(R.id.tableRow4);	
		TableRow TableRow5 = (TableRow)findViewById(R.id.tableRow5);

		int spinner_position = spinner_date.getSelectedItemPosition();
		int spinner_position_date_from = spinner_date_from.getSelectedItemPosition();
		int spinner_position_date_to = spinner_date_to.getSelectedItemPosition();

		if (spinner_position <6)
		{
			TableRow4.setVisibility(View.GONE);
			TableRow5.setVisibility(View.GONE);
		}
		else
		{
			TableRow4.setVisibility(View.VISIBLE);
			if (spinner_position_date_to == 0)
			{
				TableRow5.setVisibility(View.VISIBLE);	
			}
			else
			{
				TableRow5.setVisibility(View.GONE);
			}
		}


		if (spinner_position_date_from == 0)
		{
			TableRow3.setVisibility(View.VISIBLE);	
		}
		else
		{
			TableRow3.setVisibility(View.GONE);
		}

	}
	
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		compare_types_dates = new String[] {"=", "<>", "<", ">","<=",">=",getString(R.string.from_code10),getString(R.string.from_code11)};	
		requestWindowFeature(Window.FEATURE_NO_TITLE);	
		// Inflate your view
		setContentView(R.layout.filter_form_date);

		Resources res = getResources();
		date_filters = res.getStringArray(R.array.date_filters);		
		
		
		Intent i = getIntent();
		int spinner_position = i.getExtras().getInt("spinner_position");
		Long date_from = i.getExtras().getLong("date_from");
		Long date_to = i.getExtras().getLong("date_to");
		CurrentField = i.getExtras().getInt("field");
		
		if (date_from<1)
		{
			date_from = (new Date()).getTime();
		}
		
		if (date_to<1)
		{
			date_to = (new Date()).getTime();
		}
			
		/*String CurrentName = i.getExtras().getString("Name");
		  		final EditText myEditText = (EditText)findViewById(R.id.editnametextfield);
		TextView myTextView = (TextView)findViewById(R.id.textView1);
		String Title = i.getExtras().getString("Title");
		
		myTextView.setText(Title);
		
		myEditText.setText(CurrentName);*/
		
		
		spinnerArrayAdapter_date_filters = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, date_filters);
		spinnerArrayAdapter_date = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, compare_types_dates);
	
		Spinner spinner_date = (Spinner)findViewById(R.id.spinner_date_form_date);
		spinner_date.setAdapter(spinnerArrayAdapter_date);
		spinner_date.setSelection(spinner_position);
		spinner_date.setOnItemSelectedListener(new MyOnItemSelectedListener());		
		
		
		DatePicker DatePicker_from = (DatePicker)findViewById(R.id.datePicker_from);
		DatePicker DatePicker_to = (DatePicker)findViewById(R.id.datePicker_to);
		
		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);		
		
		Spinner spinner_date_from = (Spinner)findViewById(R.id.spinner_date_from);
		spinner_date_from.setAdapter(spinnerArrayAdapter_date_filters);

		if (date_from<8 && date_from>0)//postition of spinner
		{
			 spinner_date_from.setSelection(date_from.intValue());
		}
		else
		{
			Date date0 = new Date(date_from);
			year = 1900+date0.getYear();
			month = date0.getMonth();
			day = date0.getDate();				
		}
		DatePicker_from.init(year, month, day, null);
		spinner_date_from.setOnItemSelectedListener(new MyOnItemSelectedListener());
		
		
		
				
		Spinner spinner_date_to = (Spinner)findViewById(R.id.spinner_date_to);
		spinner_date_to.setAdapter(spinnerArrayAdapter_date_filters);
		
		if (date_to<8 && date_to>0)//postition of spinner
		{
			spinner_date_to.setSelection(date_to.intValue());
		}
		else
		{
			Date date0 = new Date(date_to);
			year = 1900+date0.getYear();
			month = date0.getMonth();
			day = date0.getDate();				
		}
		DatePicker_to.init(year, month, day, null);
		spinner_date_to.setOnItemSelectedListener(new MyOnItemSelectedListener());
		
			
		
		
		ShowHideElements();
		
		LinearLayout layoutapply = (LinearLayout)this.findViewById(R.id.layoutapply);
		LinearLayout layoutcancel = (LinearLayout)this.findViewById(R.id.layoutcancel);

		layoutapply.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
			
				Spinner spinner_date = (Spinner)findViewById(R.id.spinner_date_form_date);
				Spinner spinner_date_from = (Spinner)findViewById(R.id.spinner_date_from);
				Spinner spinner_date_to = (Spinner)findViewById(R.id.spinner_date_to);

				int spinner_position = spinner_date.getSelectedItemPosition();
				int spinner_position_date_from = spinner_date_from.getSelectedItemPosition();
				int spinner_position_date_to = spinner_date_to.getSelectedItemPosition();	
				
				DatePicker DatePicker_from = (DatePicker)findViewById(R.id.datePicker_from);
				DatePicker DatePicker_to = (DatePicker)findViewById(R.id.datePicker_to);		
				
				
				intent.putExtra("spinner_position", spinner_position);
				intent.putExtra("spinner_position_date_from", spinner_position_date_from);
				intent.putExtra("spinner_position_date_to", spinner_position_date_to);
				
				Date date0 = new Date(DatePicker_from.getYear()-1900,DatePicker_from.getMonth(),DatePicker_from.getDayOfMonth());
				intent.putExtra("date_from",date0.getTime());				
				date0 = new Date(DatePicker_to.getYear()-1900,DatePicker_to.getMonth(),DatePicker_to.getDayOfMonth());
				intent.putExtra("date_to",date0.getTime());
				intent.putExtra("field", CurrentField);
				
				setResult(RESULT_OK,intent);
				finish();					
				}});

		layoutcancel.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				setResult(RESULT_CANCELED,intent);
				finish();					
				}});		


	}
	

	
}