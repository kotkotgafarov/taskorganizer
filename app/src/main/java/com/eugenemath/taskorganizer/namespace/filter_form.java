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

public class filter_form extends Activity {

	private Cursor c_status, c_category;
	private Filter filter; 
	private static SQLiteDatabase tasksDB;
	SimpleDateFormat sdf_ddMM = new SimpleDateFormat("dd MMM yy");
	Button button_date,button_duedate,button_releasedate;
	TextView textViewDueDate, textViewDate, textViewReleaseDate;
	
	

	int current_dialog;
	private int REQUEST_CONTEXT_MENU = 1;
	private int REQUEST_NEW_CODE = 2;
	private int  RQS_PICK_CONTACT =3;
	private int  REQUEST_DATE =4;
	int RQS_PICK_CONTACT_TYPE = 0;//executor = 0, responsible = 1

	/*static final private int dialog_button_date_to = 1; 	
	static final private int dialog_button_date_from = 2; 
	static final private int dialog_button_duedate_to = 3; 
	static final private int dialog_button_duedate_from = 4; 
	static final private int dialog_button_releasedate_to = 5; 
	static final private int dialog_button_releasedate_from = 6; 	*/	

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

		compare_types_strings = new String[] {"=", "<>", getString(R.string.from_code39), getString(R.string.from_code40)};
		compare_types_list = new String[] {getString(R.string.from_code41), getString(R.string.from_code42)};


		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Inflate your view
		setContentView(R.layout.filter);

		taskOrganizerDatabaseHelper dbHelper = new taskOrganizerDatabaseHelper(this,  
				taskOrganizerDatabaseHelper.DATABASE_NAME, 
				null, 
				taskOrganizerDatabaseHelper.DATABASE_VERSION);
		tasksDB = dbHelper.getWritableDatabase();		

		Intent i = getIntent();
		String xml = i.getExtras().getString("filter");	
		CurrentChoosenId = i.getExtras().getLong("id");			

		filter = new Filter(xml);
		filter.name_of_filter= i.getExtras().getString("name_of_filter");
		filter.CustomizeExecutorResponsible(this);



		/*spinnerArrayAdapter_strings = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, compare_types_strings);
		spinnerArrayAdapter_list = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, compare_types_list);
		spinnerArrayAdapter_executor = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, compare_types_executor);*/

		spinnerArrayAdapter_strings = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, compare_types_strings);
		spinnerArrayAdapter_list = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, compare_types_list);
		spinnerArrayAdapter_executor = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, compare_types_executor);

		c_status = query_statuses();
		FillStatusesToChoose();

		c_category =  query_categories();
		FillCategoriesToChoose();


		sdf_ddMM = new SimpleDateFormat(getString(R.string.sdf1));

		FillFields();
	}

	public void UpdateOrSaveItem()
	{
		ContentValues _initialValues = new ContentValues();    


		_initialValues.put(taskOrganizerDatabaseHelper.Filter_NAME, filter.name_of_filter);	
		_initialValues.put(taskOrganizerDatabaseHelper.Filter_XML, filter.xmlstring);


		if (CurrentChoosenId !=0)
		{
			String where = taskOrganizerDatabaseHelper.Filter_ID + "=" + CurrentChoosenId;
			long rowID = tasksDB.update(taskOrganizerDatabaseHelper.Filters_Table,_initialValues,where,null);    
		}
		else
		{
			CurrentChoosenId = tasksDB.insert(taskOrganizerDatabaseHelper.Filters_Table, "item", _initialValues);
		}


	}	


	public void ChangeDateInButton(View v){

		Intent i = new Intent(filter_form.this,filter_form_date.class);
		if (v.getId() ==button_date.getId() || v.getId() ==textViewDate.getId())
		{
			i.putExtra("field",  1);
			i.putExtra("spinner_position",  filter.spinner_date);

			if (filter.spinner_date_from>0 && filter.spinner_date_from<8){i.putExtra("date_from",  Long.valueOf(""+filter.spinner_date_from));}
			else {i.putExtra("date_from",filter.date_from.getTime());}

			if (filter.spinner_date_to>0 && filter.spinner_date_to<8){i.putExtra("date_to",  Long.valueOf(""+filter.spinner_date_to));}
			else{i.putExtra("date_to",filter.date_to.getTime());}		
		}


		if (v.getId() ==button_duedate.getId() || v.getId() ==textViewDueDate.getId())
		{
			i.putExtra("field",  2);
			i.putExtra("spinner_position",  filter.spinner_duedate);

			if (filter.spinner_duedate_from>0 && filter.spinner_duedate_from<8){i.putExtra("date_from",  Long.valueOf(""+filter.spinner_duedate_from));}
			else {i.putExtra("date_from",filter.duedate_from.getTime());}

			if (filter.spinner_duedate_to>0 && filter.spinner_duedate_to<8){i.putExtra("date_to",  Long.valueOf(""+filter.spinner_duedate_to));}
			else{i.putExtra("date_to",filter.duedate_to.getTime());}		
		}		

		if (v.getId() ==button_releasedate.getId() || v.getId() ==textViewReleaseDate.getId())
		{
			i.putExtra("field",  3);
			i.putExtra("spinner_position",  filter.spinner_releasedate);

			if (filter.spinner_releasedate_from>0 && filter.spinner_releasedate_from<8){i.putExtra("date_from",  Long.valueOf(""+filter.spinner_releasedate_from));}
			else {i.putExtra("date_from",filter.releasedate_from.getTime());}

			if (filter.spinner_releasedate_to>0 && filter.spinner_releasedate_to<8){i.putExtra("date_to",  Long.valueOf(""+filter.spinner_releasedate_to));}
			else{i.putExtra("date_to",filter.releasedate_to.getTime());}		
		}			


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

	public void ShowStatusesChosen()
	{
		String _status = "";
		String _status_name = "";
		long _id;

		for( int i = 0; i < checked_statuses.length; i++ ){
			if (checked_statuses[i])
			{
				c_status.moveToPosition(i);
				_id = c_status.getLong(c_status.getColumnIndex("_id"));
				_status_name += c_status.getString(c_status.getColumnIndex(taskOrganizerDatabaseHelper.Status_NAME))+";";
				_status +=_id+";";
			}
			//Log.i( "ME", checked_statuses[ i ] + " selected: " + _selections[i] );
		}	
		filter.status = _status;
		filter.statusname = _status_name;
		TextView textView_value_status = (TextView)findViewById(R.id.textView_value_status); 
		textView_value_status.setText(filter.statusname);	
	}

	public void FillStatusesToChoose()
	{
		//ArrayList<String> temp1 = new ArrayList<String>();
		checked_statuses = new boolean[c_status.getCount()];
		statuses = new String[c_status.getCount()]; 
		String _status_name = "";

		long _id;

		int pos = 0;
		if (c_status.moveToFirst()) {
			do { 

				//temp1.add(c_status.getString(c_status.getColumnIndex(taskOrganizerDatabaseHelper.Status_NAME)));
				_id = c_status.getLong(c_status.getColumnIndex("_id"));

				statuses[pos] = c_status.getString(c_status.getColumnIndex(taskOrganizerDatabaseHelper.Status_NAME));
				if (filter.status.contains(""+_id+";"))
				{
					checked_statuses[pos] = true;
					_status_name +=statuses[pos];
				}
				pos++;
			} while(c_status.moveToNext());
		}		
		filter.statusname = _status_name;
		TextView textView_value_status = (TextView)findViewById(R.id.textView_value_status); 
		textView_value_status.setText(filter.statusname);	
		//statuses = temp1.toArray(statuses);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		current_dialog = id;

		switch (id){
		/*case (dialog_button_date_to):
				return new DatePickerDialog(this, mDateSetListener, 1900+filter.date_to.getYear(), filter.date_to.getMonth(), filter.date_to.getDate());
			case (dialog_button_date_from):
				return new DatePickerDialog(this,  mDateSetListener, 1900+filter.date_from.getYear(), filter.date_from.getMonth(), filter.date_from.getDate());	    
			case (dialog_button_duedate_to):
				return new DatePickerDialog(this,  mDateSetListener, 1900+filter.duedate_to.getYear(), filter.duedate_to.getMonth(), filter.duedate_to.getDate());	    
			case (dialog_button_duedate_from):
				return new DatePickerDialog(this,  mDateSetListener, 1900+filter.duedate_from.getYear(), filter.duedate_from.getMonth(), filter.duedate_from.getDate());	    
			case (dialog_button_releasedate_to):
				return new DatePickerDialog(this,  mDateSetListener, 1900+filter.releasedate_to.getYear(), filter.releasedate_to.getMonth(), filter.releasedate_to.getDate());	    
			case (dialog_button_releasedate_from):
				return new DatePickerDialog(this,  mDateSetListener, 1900+filter.releasedate_from.getYear(), filter.releasedate_from.getMonth(), filter.releasedate_from.getDate());*/	    
		case (dialog_choose_status):
		{
			return new AlertDialog.Builder(this)
			.setTitle(getString(R.string.menu_statuses))
			.setMultiChoiceItems(statuses,
					checked_statuses,
					new DialogInterface.OnMultiChoiceClickListener() {
				public void onClick(DialogInterface dialog, int whichButton,
						boolean isChecked) {

					/* User clicked on a check box do some stuff */
				}
			})
			.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					ShowStatusesChosen();

				}
			})
			.setNegativeButton("CANCEL",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

					/* User clicked No so do some stuff */
				}
			})
			.create();
		}
		case (dialog_choose_category):
		{
			return new AlertDialog.Builder(this)
			.setTitle(getString(R.string.menu_categories))
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

	public Cursor query_statuses() {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String SelectQuery = "SELECT "
				+ taskOrganizerDatabaseHelper.Status_ID+" as _id,"
				+ taskOrganizerDatabaseHelper.Status_NAME+" as name"
				+" FROM "+ taskOrganizerDatabaseHelper.Statuses_Table;

		Cursor _c = tasksDB.rawQuery(SelectQuery, null);
		return _c;
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
		EditText editText_name_of_filter = (EditText)findViewById(R.id.editText_name_of_filter); 	
		filter.name_of_filter = editText_name_of_filter.getText().toString();	


		EditText editText_code = (EditText)findViewById(R.id.editText_code); 
		filter.code = editText_code.getText().toString();	
		CheckBox checkBox_code = (CheckBox)findViewById(R.id.checkBox_code); 
		filter.code_checked = checkBox_code.isChecked();

		EditText editText_name = (EditText)findViewById(R.id.editText_name); 
		filter.name = editText_name.getText().toString();	
		CheckBox checkBox_name = (CheckBox)findViewById(R.id.checkBox_name); 
		filter.name_checked = checkBox_name.isChecked();


		EditText editText_description = (EditText)findViewById(R.id.editText_description); 
		filter.description = editText_description.getText().toString();	
		CheckBox checkBox_description = (CheckBox)findViewById(R.id.checkBox_description); 
		filter.description_checked = checkBox_description.isChecked();


		CheckBox checkBox_status = (CheckBox)findViewById(R.id.checkBox_status); 
		filter.status_checked = checkBox_status.isChecked();			


		CheckBox checkBox_category = (CheckBox)findViewById(R.id.checkBox_category); 
		filter.category_checked = checkBox_category.isChecked();				

		CheckBox checkBox_executor = (CheckBox)findViewById(R.id.checkBox_executor); 
		filter.executor_checked = checkBox_executor.isChecked();			

		CheckBox checkBox_responsible = (CheckBox)findViewById(R.id.checkBox_responsible); 
		filter.responsible_checked = checkBox_responsible.isChecked();			


		CheckBox checkBox_date = (CheckBox)findViewById(R.id.checkBox_date); 
		filter.date_checked = checkBox_date.isChecked();				

		CheckBox checkBox_duedate = (CheckBox)findViewById(R.id.checkBox_duedate); 
		filter.duedate_checked = checkBox_duedate.isChecked();				

		CheckBox checkBox_releasedate = (CheckBox)findViewById(R.id.checkBox_releasedate); 
		filter.releasedate_checked = checkBox_releasedate.isChecked();				

		Spinner spinner_code = (Spinner)findViewById(R.id.spinner_code);
		filter.spinner_code = spinner_code.getSelectedItemPosition();


		Spinner spinner_sort = (Spinner)findViewById(R.id.spinner_sort);
		filter.spinner_sort = spinner_sort.getSelectedItemPosition();

		Spinner spinner_name = (Spinner)findViewById(R.id.spinner_name);
		filter.spinner_name = spinner_name.getSelectedItemPosition();

		Spinner spinner_description = (Spinner)findViewById(R.id.spinner_description);
		filter.spinner_description = spinner_description.getSelectedItemPosition();

		Spinner spinner_status = (Spinner)findViewById(R.id.spinner_status);
		filter.spinner_status = spinner_status.getSelectedItemPosition();

		Spinner spinner_category = (Spinner)findViewById(R.id.spinner_category);
		filter.spinner_category = spinner_category.getSelectedItemPosition();

		Spinner spinner_executor = (Spinner)findViewById(R.id.spinner_executor);
		filter.spinner_executor = spinner_executor.getSelectedItemPosition();

		Spinner spinner_responsible = (Spinner)findViewById(R.id.spinner_responsible);
		filter.spinner_responsible = spinner_responsible.getSelectedItemPosition();			

		CheckBox CheckBox_show_empty_date_checked = (CheckBox)findViewById(R.id.show_empty_date_checked); 
		filter.show_empty_date_checked = CheckBox_show_empty_date_checked.isChecked();		


	}

	public String GetTextForDateButton(int ButtonNumber)
	{
		String textforbutton = "";


		//SimpleDateFormat sdf_yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");

		SimpleDateFormat sdf_yyyyMMdd = new SimpleDateFormat(getString(R.string.sdf1));
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
			if (filter.spinner_date_from == 0){date_from = sdf_yyyyMMdd.format(filter.date_from);}
			else{date_from = date_filters[filter.spinner_date_from];}				
			if (filter.spinner_date_to == 0){date_to = sdf_yyyyMMdd.format(filter.date_to);}
			else{date_to = date_filters[filter.spinner_date_to];}
			spinner_position = filter.spinner_date;
		}

		if (ButtonNumber == 2)
		{
			if (filter.spinner_duedate_from == 0){date_from = sdf_yyyyMMdd.format(filter.duedate_from);}
			else{date_from = date_filters[filter.spinner_duedate_from];}				
			if (filter.spinner_duedate_to == 0){date_to = sdf_yyyyMMdd.format(filter.duedate_to);}
			else{date_to = date_filters[filter.spinner_duedate_to];}
			spinner_position = filter.spinner_duedate;
		}

		if (ButtonNumber == 3)
		{
			if (filter.spinner_releasedate_from == 0){date_from = sdf_yyyyMMdd.format(filter.releasedate_from);}
			else{date_from = date_filters[filter.spinner_releasedate_from];}				
			if (filter.spinner_date_to == 0){date_to = sdf_yyyyMMdd.format(filter.releasedate_to);}
			else{date_to = date_filters[filter.spinner_releasedate_to];}
			spinner_position = filter.spinner_releasedate;
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



			Resources res = getResources();
			String[] sort = res.getStringArray(R.array.sort);		
			ArrayAdapter<String> spinnerArrayAdapter_sort = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, sort);
			Spinner spinner_sort = (Spinner)this.findViewById(R.id.spinner_sort);
			spinner_sort.setAdapter(spinnerArrayAdapter_sort);
			spinner_sort.setSelection(filter.spinner_sort);


			EditText editText_name_of_filter = (EditText)findViewById(R.id.editText_name_of_filter); 	
			editText_name_of_filter.setText(filter.name_of_filter);	


			EditText editText_code = (EditText)findViewById(R.id.editText_code); 
			editText_code.setText(filter.code);	
			CheckBox checkBox_code = (CheckBox)findViewById(R.id.checkBox_code); 
			checkBox_code.setChecked(filter.code_checked);

			EditText editText_name = (EditText)findViewById(R.id.editText_name); 
			editText_name.setText(filter.name);	
			CheckBox checkBox_name = (CheckBox)findViewById(R.id.checkBox_name); 
			checkBox_name.setChecked(filter.name_checked);		

			EditText editText_description = (EditText)findViewById(R.id.editText_description); 
			editText_description.setText(filter.description);	
			CheckBox checkBox_description = (CheckBox)findViewById(R.id.checkBox_description); 
			checkBox_description.setChecked(filter.description_checked);				



			TextView textView_value_status = (TextView)findViewById(R.id.textView_value_status); 
			textView_value_status.setText(filter.statusname);	
			CheckBox checkBox_status = (CheckBox)findViewById(R.id.checkBox_status); 
			checkBox_status.setChecked(filter.status_checked);				

			TextView textView_value_category = (TextView)findViewById(R.id.textView_value_category); 
			textView_value_category.setText(filter.categoryname);	
			CheckBox checkBox_category = (CheckBox)findViewById(R.id.checkBox_category); 
			checkBox_category.setChecked(filter.category_checked);			


			TextView textView_value_executor = (TextView)findViewById(R.id.textView_value_executor); 
			textView_value_executor.setText(filter.executorname);	
			CheckBox checkBox_executor = (CheckBox)findViewById(R.id.checkBox_executor); 
			checkBox_executor.setChecked(filter.executor_checked);			

			Button button_menu_executor = (Button)findViewById(R.id.button_executor); 
			button_menu_executor.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					Intent i = new Intent(filter_form.this,flexiblemenu.class);
					i.putExtra("Regim",  "ContactClear");
					i.putExtra("executorresponsible",  0);
					startActivityForResult(i, REQUEST_CONTEXT_MENU);			
				}});			


			TextView textView_value_responsible = (TextView)findViewById(R.id.textView_value_responsible); 
			textView_value_responsible.setText(filter.responsiblename);	
			CheckBox checkBox_responsible = (CheckBox)findViewById(R.id.checkBox_responsible); 
			checkBox_responsible.setChecked(filter.responsible_checked);			

			Button button_menu_responsible = (Button)findViewById(R.id.button_responsible); 
			button_menu_responsible.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					Intent i = new Intent(filter_form.this,flexiblemenu.class);
					i.putExtra("Regim",  "ContactClear");
					i.putExtra("executorresponsible",  1);
					startActivityForResult(i, REQUEST_CONTEXT_MENU);			
				}});					




			button_date = (Button)this.findViewById(R.id.button_date); 
			button_date.setOnClickListener(myDateOnClickListener);

			button_duedate = (Button)this.findViewById(R.id.button_duedate); 
			button_duedate.setOnClickListener(myDateOnClickListener);				

			textViewDueDate = (TextView)this.findViewById(R.id.textViewDueDate); 
			textViewDate = (TextView)this.findViewById(R.id.textViewDate); 
			textViewReleaseDate = (TextView)this.findViewById(R.id.textViewReleaseDate); 
			textViewDueDate.setOnClickListener(myDateOnClickListener);	
			textViewDate.setOnClickListener(myDateOnClickListener);	
			textViewReleaseDate.setOnClickListener(myDateOnClickListener);	
			textViewDate.setText(GetTextForDateButton(1));
			textViewDueDate.setText(GetTextForDateButton(2));
			textViewReleaseDate.setText(GetTextForDateButton(3));
			
			button_releasedate = (Button)this.findViewById(R.id.button_releasedate); 
			button_releasedate.setOnClickListener(myDateOnClickListener);						


			CheckBox checkBox_date = (CheckBox)findViewById(R.id.checkBox_date); 
			checkBox_date.setChecked(filter.date_checked);						

			CheckBox checkBox_duedate = (CheckBox)findViewById(R.id.checkBox_duedate); 
			checkBox_duedate.setChecked(filter.duedate_checked);	

			CheckBox checkBox_releasedate = (CheckBox)findViewById(R.id.checkBox_releasedate); 
			checkBox_releasedate.setChecked(filter.releasedate_checked);					


			CheckBox CheckBox_show_empty_date_checked = (CheckBox)findViewById(R.id.show_empty_date_checked); 
			CheckBox_show_empty_date_checked.setChecked(filter.show_empty_date_checked);

			//---------------------------------ALL SPINNERS-----------------------------------------



			Spinner spinner_code = (Spinner)findViewById(R.id.spinner_code);
			spinner_code.setAdapter(spinnerArrayAdapter_strings);
			spinner_code.setSelection(filter.spinner_code);
			spinner_code.setOnItemSelectedListener(new MyOnItemSelectedListener());

			Spinner spinner_name = (Spinner)findViewById(R.id.spinner_name);
			spinner_name.setAdapter(spinnerArrayAdapter_strings);
			spinner_name.setSelection(filter.spinner_name);
			spinner_name.setOnItemSelectedListener(new MyOnItemSelectedListener());

			Spinner spinner_description = (Spinner)findViewById(R.id.spinner_description);
			spinner_description.setAdapter(spinnerArrayAdapter_strings);
			spinner_description.setSelection(filter.spinner_description);
			spinner_description.setOnItemSelectedListener(new MyOnItemSelectedListener());


			Spinner spinner_status = (Spinner)findViewById(R.id.spinner_status);
			spinner_status.setAdapter(spinnerArrayAdapter_list);
			spinner_status.setSelection(filter.spinner_status);
			spinner_status.setOnItemSelectedListener(new MyOnItemSelectedListener());

			Spinner spinner_category = (Spinner)findViewById(R.id.spinner_category);
			spinner_category.setAdapter(spinnerArrayAdapter_list);
			spinner_category.setSelection(filter.spinner_category);
			spinner_category.setOnItemSelectedListener(new MyOnItemSelectedListener());


			Spinner spinner_executor = (Spinner)findViewById(R.id.spinner_executor);
			spinner_executor.setAdapter(spinnerArrayAdapter_executor);
			spinner_executor.setSelection(filter.spinner_executor);
			spinner_executor.setOnItemSelectedListener(new MyOnItemSelectedListener());

			Spinner spinner_responsible = (Spinner)findViewById(R.id.spinner_responsible);
			spinner_responsible.setAdapter(spinnerArrayAdapter_executor);
			spinner_responsible.setSelection(filter.spinner_executor);
			spinner_responsible.setOnItemSelectedListener(new MyOnItemSelectedListener());

			Button button_status = (Button)findViewById(R.id.button_status); 		
			button_status.setOnClickListener( new Button.OnClickListener() {
				public void onClick(View v) {
					showDialog(dialog_choose_status);    
				}});	

			Button button_category = (Button)findViewById(R.id.button_category); 		
			button_category.setOnClickListener( new Button.OnClickListener() {
				public void onClick(View v) {
					showDialog(dialog_choose_category);    
				}});					

			LinearLayout layoutsave = (LinearLayout)this.findViewById(R.id.layoutsave); 	
			layoutsave.setOnClickListener( new Button.OnClickListener() {
				public void onClick(View v) {
					SetChangesToFilter();

					if (filter.name_of_filter.length() == 0)
					{
						Toast.makeText(getApplicationContext(), "Fill name of the filter", Toast.LENGTH_SHORT).show();
						return;
					}
					filter.ToXML();
					UpdateOrSaveItem();

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

			
			LinearLayout layoutsaveapply = (LinearLayout)this.findViewById(R.id.layoutsaveapply); 	
			layoutsaveapply.setOnClickListener( new Button.OnClickListener() {
				public void onClick(View v) {  
					Intent intent = new Intent();
					SetChangesToFilter();


					if (filter.name_of_filter.length() == 0)
					{
						Toast.makeText(getApplicationContext(), getString(R.string.from_code43), Toast.LENGTH_SHORT).show();
						return;
					}
					filter.ToXML();
					UpdateOrSaveItem();				
					//filter.ToXML();
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
				textViewDate.setText(GetTextForDateButton(1));
			}

			if (field == 2)
			{
				filter.spinner_duedate = spinner_position;
				filter.spinner_duedate_from = spinner_position_date_from;
				filter.spinner_duedate_to = spinner_position_date_to;
				filter.duedate_from = date_from;
				filter.duedate_to = date_to;
				textViewDueDate.setText(GetTextForDateButton(2));
			}		

			if (field == 3)
			{
				filter.spinner_releasedate = spinner_position;
				filter.spinner_releasedate_from = spinner_position_date_from;
				filter.spinner_releasedate_to = spinner_position_date_to;
				filter.releasedate_from = date_from;
				filter.releasedate_to = date_to;
				textViewReleaseDate.setText(GetTextForDateButton(3));
			}				

		}

		if (requestCode == REQUEST_CONTEXT_MENU)
		{
			if (resultCode == Activity.RESULT_OK) 
				if (data.getExtras().getInt("Result") == 0)
				{   				

					//contactUri --> content://com.android.contacts/data/1557
					Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
					startActivityForResult(intent, RQS_PICK_CONTACT);	
					if (data.getExtras().getInt("executorresponsible") == 0){RQS_PICK_CONTACT_TYPE = 0;}
					else{RQS_PICK_CONTACT_TYPE = 1;}
				}
				else
				{    				

					if (data.getExtras().getInt("executorresponsible") == 0){	
						filter.executor = "";
						filter.executorname = "";

						Intent i = new Intent(filter_form.this,editname.class);
						i.putExtra("Name",  "");
						i.putExtra("Title",  getString(R.string.from_code73));
						startActivityForResult(i, REQUEST_NEW_CODE); 		    					
						RQS_PICK_CONTACT_TYPE = 0;	    					

						TextView textView_value_executor = (TextView)findViewById(R.id.textView_value_executor); 
						textView_value_executor.setText(filter.executorname);	

					}
					else
					{
						filter.responsible = ""; 	
						filter.responsiblename = "";  

						Intent i = new Intent(filter_form.this,editname.class);
						i.putExtra("Name",  "");
						i.putExtra("Title",  getString(R.string.from_code74));
						startActivityForResult(i, REQUEST_NEW_CODE); 		    					
						RQS_PICK_CONTACT_TYPE = 1;

						TextView textView_value_responsible = (TextView)findViewById(R.id.textView_value_responsible); 
						textView_value_responsible.setText(filter.responsiblename);	   
					}
				}
		}	

		if(requestCode == RQS_PICK_CONTACT){
			if(resultCode == RESULT_OK){
				Uri contactData = data.getData();

				if (RQS_PICK_CONTACT_TYPE ==0)
				{
					filter.executor = contactData.toString();
					filter.executor_checked = true;

					CheckBox checkBox_executor = (CheckBox)findViewById(R.id.checkBox_executor); 
					checkBox_executor.setChecked(filter.executor_checked);			    				
				}
				else
				{
					filter.responsible = contactData.toString();
					filter.responsible_checked = true;
					CheckBox checkBox_responsible = (CheckBox)findViewById(R.id.checkBox_responsible); 
					checkBox_responsible.setChecked(filter.responsible_checked);	    				
				}
				filter.CustomizeExecutorResponsible(this);
				TextView textView_value_executor = (TextView)findViewById(R.id.textView_value_executor); 
				textView_value_executor.setText(filter.executorname);	
				TextView textView_value_responsible = (TextView)findViewById(R.id.textView_value_responsible); 
				textView_value_responsible.setText(filter.responsiblename);	   

			}
		}			

		if (requestCode == REQUEST_NEW_CODE && resultCode == Activity.RESULT_OK)
			if (data.getExtras().getInt("Result") == 1)
			{
				String str = data.getExtras().getString("Name");

				if (RQS_PICK_CONTACT_TYPE ==0)
				{
					filter.executor = str;
					filter.executorname = str;
					filter.executor_checked = true;

					CheckBox checkBox_executor = (CheckBox)findViewById(R.id.checkBox_executor); 
					checkBox_executor.setChecked(filter.executor_checked);			    				
				}
				else
				{
					filter.responsible = str; 	
					filter.responsiblename = str;  
					filter.responsible_checked = true;

					CheckBox checkBox_responsible = (CheckBox)findViewById(R.id.checkBox_responsible); 
					checkBox_responsible.setChecked(filter.responsible_checked);	    				
				}
				TextView textView_value_executor = (TextView)findViewById(R.id.textView_value_executor); 
				textView_value_executor.setText(filter.executorname);	
				TextView textView_value_responsible = (TextView)findViewById(R.id.textView_value_responsible); 
				textView_value_responsible.setText(filter.responsiblename);	  			 
			}	    	



	} 
}