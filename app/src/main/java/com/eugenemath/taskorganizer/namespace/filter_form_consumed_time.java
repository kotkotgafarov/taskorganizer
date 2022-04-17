package com.eugenemath.taskorganizer.namespace;


import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.eugenemath.taskorganizer.namespace.R;
import com.eugenemath.taskorganizer.namespace.taskForm.MyOnItemSelectedListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Contacts.People;
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
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

public class filter_form_consumed_time extends Activity {

	private Cursor  c_category;
	private Filter filter; 
	private static SQLiteDatabase tasksDB;
	SimpleDateFormat sdf_ddMM = new SimpleDateFormat("dd MMM yy");
	Button button_date,button_duedate,button_releasedate;

	int current_dialog;
	private int REQUEST_CONTEXT_MENU = 1;
	private int REQUEST_NEW_CODE = 2;
	private int  RQS_PICK_CONTACT =3;
	private int  REQUEST_DATE =4;
	int RQS_PICK_CONTACT_TYPE = 0;//executor = 0, responsible = 1


	static final private int  dialog_choose_status = 7;
	static final private int   dialog_choose_category = 8;

	Date date01011970 = new Date(70,1,1);


	String[] compare_types_strings;	
	String[] compare_types_list;
	String[] compare_types_executor = {"=", "<>"};	


	ArrayAdapter<String> spinnerArrayAdapter_strings;
	//ArrayAdapter<String> spinnerArrayAdapter_date;
	ArrayAdapter<String> spinnerArrayAdapter_list;
	ArrayAdapter<String> spinnerArrayAdapter_executor;

	CharSequence[] statuses;
	boolean[] checked_statuses;

	CharSequence[] categories;
	boolean[] checked_categories;

	Long CurrentChoosenId;
	String name_of_filter;

	public class MyOnItemSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent,
				View view, int pos, long id) {

		}
		public void onNothingSelected(AdapterView parent) {
			// Do nothing.
		}
	}		


	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		
		compare_types_strings = new String[]{"=", "<>", getString(R.string.from_code39), getString(R.string.from_code40)};
		compare_types_list = new String[]{getString(R.string.from_code41), getString(R.string.from_code42)};
		
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Inflate your view
		setContentView(R.layout.filter_consumed_time);

		taskOrganizerDatabaseHelper dbHelper = new taskOrganizerDatabaseHelper(this,  
				taskOrganizerDatabaseHelper.DATABASE_NAME, 
				null, 
				taskOrganizerDatabaseHelper.DATABASE_VERSION);
		tasksDB = dbHelper.getWritableDatabase();		

		Intent i = getIntent();
		String xml = i.getExtras().getString("filter");	
		CurrentChoosenId = i.getExtras().getLong("id");			

		filter = new Filter(xml);
		//filter.name_of_filter= i.getExtras().getString("name_of_filter");
		//filter.CustomizeExecutorResponsible(this);



		//spinnerArrayAdapter_strings = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, compare_types_strings);
		spinnerArrayAdapter_list = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, compare_types_list);
		//spinnerArrayAdapter_executor = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, compare_types_executor);

		sdf_ddMM = new SimpleDateFormat(getString(R.string.sdf1));

		c_category =  query_categories();
		FillCategoriesToChoose();


		FillFields();
	}



	public void ChangeDateInButton(View v){

		Intent i = new Intent(filter_form_consumed_time.this,filter_form_date.class);

			i.putExtra("field",  1);
			i.putExtra("spinner_position",  filter.spinner_date);

			if (filter.spinner_date_from>0 && filter.spinner_date_from<8){i.putExtra("date_from",  Long.valueOf(""+filter.spinner_date_from));}
			else {i.putExtra("date_from",filter.date_from.getTime());}

			if (filter.spinner_date_to>0 && filter.spinner_date_to<8){i.putExtra("date_to",  Long.valueOf(""+filter.spinner_date_to));}
			else{i.putExtra("date_to",filter.date_to.getTime());}		
		



		startActivityForResult(i, REQUEST_DATE);
	}

	public void ShowCategoriesChosen()
	{
		String _category = "";
		String _category_name = "";
		long _id;

		for( int i = 0; i < checked_categories.length; i++ ){
			if (checked_categories[i])
			{
				c_category.moveToPosition(i);
				_id = c_category.getLong(c_category.getColumnIndex("_id"));
				_category_name += c_category.getString(c_category.getColumnIndex(taskOrganizerDatabaseHelper.Categories_NAME))+";";
				_category +=_id+";";
			}
			//Log.i( "ME", checked_statuses[ i ] + " selected: " + _selections[i] );
		}	
		filter.category = _category;
		filter.categoryname = _category_name;
		TextView textView_value_category = (TextView)findViewById(R.id.textView_value_category); 
		textView_value_category.setText(filter.categoryname);	
	}

	public void FillCategoriesToChoose()
	{
		//ArrayList<String> temp1 = new ArrayList<String>();
		checked_categories = new boolean[c_category.getCount()];
		categories = new String[c_category.getCount()]; 


		long _id;
		String _category_name = "";

		int pos = 0;
		if (c_category.moveToFirst()) {
			do { 

				//temp1.add(c_status.getString(c_status.getColumnIndex(taskOrganizerDatabaseHelper.Status_NAME)));
				_id = c_category.getLong(c_category.getColumnIndex("_id"));

				categories[pos] = c_category.getString(c_category.getColumnIndex(taskOrganizerDatabaseHelper.Categories_NAME));


				if (filter.category.contains(""+_id+";"))
				{
					checked_categories[pos] = true;
					_category_name += categories[pos];
				}
				pos++;
			} while(c_category.moveToNext());
		}		
		filter.categoryname = _category_name;
		TextView textView_value_category = (TextView)findViewById(R.id.textView_value_category); 
		textView_value_category.setText(filter.categoryname);	
		//statuses = temp1.toArray(statuses);
	}


	@Override
	protected Dialog onCreateDialog(int id) {
		current_dialog = id;

		switch (id){
		case (dialog_choose_category):
		{
			return new AlertDialog.Builder(this)
			.setTitle("Categories")
			.setMultiChoiceItems(categories,
					checked_categories,
					new DialogInterface.OnMultiChoiceClickListener() {
				public void onClick(DialogInterface dialog, int whichButton,
						boolean isChecked) {

					/* User clicked on a check box do some stuff */
				}
			})
			.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					ShowCategoriesChosen();

				}
			})
			.setNegativeButton("CANCEL",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

					/* User clicked No so do some stuff */
				}
			})
			.create();
		}}





		return null;
	}	



	public Cursor query_categories() {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String SelectQuery = "SELECT "
				+ taskOrganizerDatabaseHelper.Categories_ID+" as _id,"
				+ taskOrganizerDatabaseHelper.Categories_NAME+" as name"
				+" FROM "+ taskOrganizerDatabaseHelper.Categories_Table;

		Cursor _c = tasksDB.rawQuery(SelectQuery, null);
		return _c;
	}		

	public void SetChangesToFilter()
	{


		CheckBox checkBox_category = (CheckBox)findViewById(R.id.checkBox_category); 
		filter.category_checked = checkBox_category.isChecked();				


		CheckBox checkBox_date = (CheckBox)findViewById(R.id.checkBox_date); 
		filter.date_checked = checkBox_date.isChecked();				


		Spinner spinner_category = (Spinner)findViewById(R.id.spinner_category);
		filter.spinner_category = spinner_category.getSelectedItemPosition();



	}

	public String GetTextForDateButton(int ButtonNumber)
	{
		String textforbutton = "";


		//SimpleDateFormat sdf_yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
		//sdf_yyyyMMdd.format(duedate_to)
		String[] date_filters;
		Resources res = getResources();
		date_filters = res.getStringArray(R.array.date_filters);	

		String[] compare_types_dates = {"=", "<>", "<", ">","<=",">=",getString(R.string.from_code10),getString(R.string.from_code11)};				

		String date_from = "";
		String date_to = "";
		int spinner_position = 0;

		if (ButtonNumber == 1)
		{
			if (filter.spinner_date_from == 0){date_from = sdf_ddMM.format(filter.date_from);}
			else{date_from = date_filters[filter.spinner_date_from];}				
			if (filter.spinner_date_to == 0){date_to = sdf_ddMM.format(filter.date_to);}
			else{date_to = date_filters[filter.spinner_date_to];}
			spinner_position = filter.spinner_date;
		}


		textforbutton += compare_types_dates[spinner_position]+" "+date_from;

		if (spinner_position > 5)
		{
			textforbutton += " - "+date_to;
		}



		return textforbutton;
	}

	public void FillFields() {

		OnClickListener myDateOnClickListener = new Button.OnClickListener() {
			public void onClick(View v) {
				ChangeDateInButton(v);
			}};

			TextView textView_value_category = (TextView)findViewById(R.id.textView_value_category); 
			textView_value_category.setText(filter.categoryname);	
			CheckBox checkBox_category = (CheckBox)findViewById(R.id.checkBox_category); 
			checkBox_category.setChecked(filter.category_checked);			

			button_date = (Button)findViewById(R.id.button_date); 
			//button_date.setText(GetTextForDateButton(1));
			button_date.setOnClickListener(myDateOnClickListener);


			TextView textViewDate = (TextView)this.findViewById(R.id.textViewDate); 
			textViewDate.setOnClickListener(myDateOnClickListener);	
			textViewDate.setText(GetTextForDateButton(1));
			
			

			CheckBox checkBox_date = (CheckBox)findViewById(R.id.checkBox_date); 
			checkBox_date.setChecked(filter.date_checked);						

			//---------------------------------ALL SPINNERS-----------------------------------------



			Spinner spinner_category = (Spinner)findViewById(R.id.spinner_category);
			spinner_category.setAdapter(spinnerArrayAdapter_list);
			spinner_category.setSelection(filter.spinner_category);
			spinner_category.setOnItemSelectedListener(new MyOnItemSelectedListener());



			Button button_category = (Button)findViewById(R.id.button_category); 		
			button_category.setOnClickListener( new Button.OnClickListener() {
				public void onClick(View v) {
					showDialog(dialog_choose_category);    
				}});					




			LinearLayout layoutapply = (LinearLayout)this.findViewById(R.id.layoutapply); 		
			layoutapply.setOnClickListener( new Button.OnClickListener() {
				public void onClick(View v) {  
					Intent intent = new Intent();
					SetChangesToFilter();
					filter.ToXML();
					intent.putExtra("filter", filter.xmlstring);
					setResult(RESULT_OK,intent);	
					tasksDB.close();
					finish();      
				}});



	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);


		if (requestCode == REQUEST_DATE && resultCode == Activity.RESULT_OK)	
		{
			int field = data.getExtras().getInt("field");
			Date date_from = new Date(data.getExtras().getLong("date_from"));
			Date date_to = new Date(data.getExtras().getLong("date_to"));

			int spinner_position = data.getExtras().getInt("spinner_position");
			int spinner_position_date_from = data.getExtras().getInt("spinner_position_date_from");
			int spinner_position_date_to = data.getExtras().getInt("spinner_position_date_to");

			if (field == 1)
			{
				filter.spinner_date = spinner_position;
				filter.spinner_date_from = spinner_position_date_from;
				filter.spinner_date_to = spinner_position_date_to;
				filter.date_from = date_from;
				filter.date_to = date_to;
				
				TextView textViewDate = (TextView)this.findViewById(R.id.textViewDate); 
				textViewDate.setText(GetTextForDateButton(1));
			}
		}
	} 
}