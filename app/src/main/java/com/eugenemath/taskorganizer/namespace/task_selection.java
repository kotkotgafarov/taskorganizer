package com.eugenemath.taskorganizer.namespace;

import com.eugenemath.taskorganizer.namespace.R;
import com.eugenemath.taskorganizer.namespace.Task;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.preference.PreferenceManager;
import android.provider.Contacts.People;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.method.SingleLineTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.MenuItem;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import android.graphics.Color;
import android.graphics.Paint;

public class task_selection extends Activity{
	/** Called when the activity is first created. */
	static final private int MENU_ADD = Menu.FIRST;
	static final private int MENU_FILTER = Menu.FIRST+1;
	static final private int MENU_FILTER_SAVED = Menu.FIRST+2;	
	static final private int MENU_PREFERENCES = Menu.FIRST+3;
	static final private int MENU_CATALOGS = Menu.FIRST+4;
	static final private int MENU_CATEGORIES = Menu.FIRST+5;
	static final private int MENU_STATUSES = Menu.FIRST+6;



	static final private int MENU_FROM_CALENDAR = Menu.FIRST+7;
	static final private int MENU_TO_CALENDAR = Menu.FIRST+8;
	static final private int MENU_EXPORT = Menu.FIRST+9;
	static final private int MENU_IMPORT = Menu.FIRST+10;  	
	static final private int MENU_GROUP_EDITING = Menu.FIRST+11;  
	static final private int MENU_RECEIVE_EMAILS = Menu.FIRST+12;

	static final private int MENU_ADDITIONAL = Menu.FIRST+13;


	static final  int Task_DIALOG = 1;
	static final int SHOW_PREFERENCES = 2;
	static final int SHOW_LIST_OF_CATALOGS = 3;
	static final int REQUEST_CONTEXT_MENU = 4;
	static final int DIALOG_DELETE_TASK = 5;
	static final int TASK_FORM_OPENED = 6;
	static final int FILTER_FORM_OPENED = 7;
	static final int FILTER_CHOOSE_SAVED = 8;
	static final int PREFERENCES = 9;
	static final int dialog_filter_description = 10;
	static final int  GROUP_EDITING = 11;
	static final int CHOOSE_ADDITIONAL = 12;
	static final int REQUEST_CONTEXT_MENU_SMS_EMAIL = 13;

	Long CurrentChoosenId;
	ListView tasksListView;
	SimpleDateFormat sdf_ddMM = new SimpleDateFormat("dd MMM yyyy");
	private Task CurrentTaskToSendStatus;
	private int CurrentPage = 0;

	public Filter filter;
	private Cursor c;
	private Cursor c_statuses;
	private static SQLiteDatabase tasksDB;
	//public MySimpleCursorAdapter ll;
	CursorAdapter ll;
	
	
	//private String STATUS_ACTIVE = "Active";
	//private String STATUS_COMPLETED = "Completed";

	public Map<String,String> ExecutorMap = new LinkedHashMap<String, String>();

	private GestureDetector myGestureDetector;  
	private View.OnTouchListener myGestureListener;

	private static final String PREFS_NAME = "PrefsTaskOrganizer";
	private Long id_default_filter = (long)-1;
	private boolean send_sms_automatically = true;
	private boolean  first_send_email = true;




	@Override
	public void onDestroy() {
		super.onDestroy();
		//Toast.makeText(getBaseContext(), "Destroy ", Toast.LENGTH_LONG).show();
		c.close();
	}


	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.task_selection);

		tasksListView = (ListView)this.findViewById(R.id.tasksListView_task_selection);

		final LayoutInflater inflater = LayoutInflater.from(this);
		final View rowView = inflater.inflate(android.R.layout.simple_list_item_1, null);


		final EditText myEditText = (EditText)findViewById(R.id.taskname_task_selection);
		final ImageView myImageView = (ImageView)findViewById(R.id.imageView_search_task_name);    


		myImageView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {       
				apply_filter_name(myEditText.getText().toString());
			}	    
		});


		taskOrganizerDatabaseHelper dbHelper = new taskOrganizerDatabaseHelper(this,  
				taskOrganizerDatabaseHelper.DATABASE_NAME, 
				null, 
				taskOrganizerDatabaseHelper.DATABASE_VERSION);
		tasksDB = dbHelper.getWritableDatabase();		


		filter = new Filter();
		filter.date_checked = false;
		ReadPreferences();
		loadTasksFromProvider();
		
		
		


		tasksListView.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				c.moveToPosition(position);
				//int id = c.getInt(c.getColumnIndexOrThrow("_id"));
				
				Intent intent = new Intent();

				intent.putExtra("id", (int) id);
				intent.putExtra("name", c.getString(c.getColumnIndexOrThrow("name")));
				setResult(RESULT_OK,intent);	
				tasksDB.close();
				finish();  
				
				
				
			}
		});		


	}

	public void apply_filter_name(String name)
	{
		filter.name = name;
		filter.spinner_name = 2;
		filter.name_checked = true;
		filter.show_empty_date_checked = true;
		loadTasksFromProvider();
	}

	
	public void ReadPreferences()
	{
		/*SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		id_default_filter = settings.getLong("id_default_filter", -1);

		
		if (id_default_filter!=-1)
		{
			SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

			String SelectQuery = "SELECT "+taskOrganizerDatabaseHelper.Filters_Table+"."
					+ taskOrganizerDatabaseHelper.Filter_ID+" as _id,"
					+taskOrganizerDatabaseHelper.Filters_Table+"."+ taskOrganizerDatabaseHelper.Filter_XML+" as _xml"
					+" FROM "+ taskOrganizerDatabaseHelper.Filters_Table;
			SelectQuery +=" where "+taskOrganizerDatabaseHelper.Filters_Table+"."+taskOrganizerDatabaseHelper.Filter_ID+" = "+id_default_filter;	

			Cursor c_filter = tasksDB.rawQuery(SelectQuery, null);
			if (c_filter.moveToFirst()){
				String xml = c_filter.getString(c_filter.getColumnIndex("_xml"));
				filter = new Filter(xml);
			}
		}*/

	}



	public Cursor query(String[] projection, 
			String selection, 
			String[] selectionArgs, 
			String sort) {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String SelectQuery = "SELECT tasks."
				+ taskOrganizerDatabaseHelper.KEY_ID+" as _id,"
				+ taskOrganizerDatabaseHelper.KEY_DATE+","
				+ taskOrganizerDatabaseHelper.KEY_CODE+","
				+ taskOrganizerDatabaseHelper.KEY_PARENT+",tasks."
				+ taskOrganizerDatabaseHelper.KEY_NAME+" as name,"
				+ taskOrganizerDatabaseHelper.KEY_CATEGORY+","
				+ taskOrganizerDatabaseHelper.KEY_STATUS
				+" FROM "+ taskOrganizerDatabaseHelper.Tasks_TABLE;


		//SelectQuery +=" where tasks."+taskOrganizerDatabaseHelper.KEY_CATEGORY+" <> 4";
		SelectQuery += ConstructWhereClause();
		//Toast.makeText(getBaseContext(), ConstructWhereClause(), Toast.LENGTH_SHORT).show();	

		String orderBy;
		if (TextUtils.isEmpty(sort)) {
			orderBy = "tasks."+ taskOrganizerDatabaseHelper.KEY_ID;
		} else {
			orderBy = sort;
		}

		SelectQuery +=" ORDER BY "+sort;	


		Cursor c = tasksDB.rawQuery(SelectQuery, selectionArgs);
		return c;
	}	


	public String ConstructWhereClause()
	{
		String Where = " WHERE ";

		/*String[] compare_types_strings = {"=", "<>", "contains", "not contains"};
		String[] compare_types_dates = {"=", "<>", "<", ">","<=",">=","in interval","out of interval"};		
		String[] compare_types_list = {"in list", "out of the list"};
		String[] compare_types_executor = {"=", "<>"};		*/	
		int number_of_conditions = 0;

		if (filter.code_checked)
		{
			if (filter.spinner_code == 0) {Where += ((number_of_conditions==0)?"":" AND ")+"tasks."+taskOrganizerDatabaseHelper.KEY_CODE+" = '"+filter.code+"'";}
			if (filter.spinner_code == 1) {Where += ((number_of_conditions==0)?"":" AND ")+"tasks."+taskOrganizerDatabaseHelper.KEY_CODE+" <> '"+filter.code+"'";}
			if (filter.spinner_code == 2) {Where += ((number_of_conditions==0)?"":" AND ")+"tasks."+taskOrganizerDatabaseHelper.KEY_CODE+" LIKE '%"+filter.code+"%'";}
			if (filter.spinner_code == 3) {Where += ((number_of_conditions==0)?"":" AND ") +"(NOT "+"tasks."+taskOrganizerDatabaseHelper.KEY_CODE+" LIKE '%"+filter.code+"%')";}
			number_of_conditions++;
		}


		if (filter.name_checked)
		{
			if (filter.spinner_name == 0) {Where += ((number_of_conditions==0)?"":" AND ")+"tasks."+taskOrganizerDatabaseHelper.KEY_NAME+" = '"+filter.name+"'";}
			if (filter.spinner_name == 1) {Where += ((number_of_conditions==0)?"":" AND ")+"tasks."+taskOrganizerDatabaseHelper.KEY_NAME+" <> '"+filter.name+"'";}
			if (filter.spinner_name == 2) {Where += ((number_of_conditions==0)?"":" AND ")+"tasks."+taskOrganizerDatabaseHelper.KEY_NAME+" LIKE '%"+filter.name+"%'";}
			if (filter.spinner_name == 3) {Where += ((number_of_conditions==0)?"":" AND ")+"(NOT "+"tasks."+taskOrganizerDatabaseHelper.KEY_NAME+" LIKE '%"+filter.name+"%')";}
			number_of_conditions++;
		}		

		if (filter.description_checked)
		{
			if (filter.spinner_description == 0) {Where += ((number_of_conditions==0)?"":" AND ")+"tasks."+taskOrganizerDatabaseHelper.KEY_DESCRIPTION+" = '"+filter.description+"'";}
			if (filter.spinner_description == 1) {Where += ((number_of_conditions==0)?"":" AND ")+"tasks."+taskOrganizerDatabaseHelper.KEY_DESCRIPTION+" <> '"+filter.description+"'";}
			if (filter.spinner_description == 2) {Where += ((number_of_conditions==0)?"":" AND ")+"tasks."+taskOrganizerDatabaseHelper.KEY_DESCRIPTION+" LIKE '%"+filter.description+"%'";}
			if (filter.spinner_description == 3) {Where += ((number_of_conditions==0)?"":" AND ")+ "(NOT "+"tasks."+taskOrganizerDatabaseHelper.KEY_DESCRIPTION+" LIKE '%"+filter.description+"%')";}
			number_of_conditions++;
		}		

		if (filter.executor_checked)
		{
			if (filter.spinner_executor == 0) {Where += ((number_of_conditions==0)?"":" AND ")+"tasks."+taskOrganizerDatabaseHelper.KEY_EXECUTOR+" = '"+filter.executor+"'";}
			if (filter.spinner_executor == 1) {Where += ((number_of_conditions==0)?"":" AND ")+"tasks."+taskOrganizerDatabaseHelper.KEY_EXECUTOR+" <> '"+filter.executor+"'";}
			number_of_conditions++;
		}		

		if (filter.responsible_checked)
		{
			if (filter.spinner_responsible == 0) {Where += ((number_of_conditions==0)?"":" AND ")+"tasks."+taskOrganizerDatabaseHelper.KEY_RESPONSIBLE+" = '"+filter.responsible+"'";}
			if (filter.spinner_responsible == 1) {Where += ((number_of_conditions==0)?"":" AND ")+"tasks."+taskOrganizerDatabaseHelper.KEY_RESPONSIBLE+" <> '"+filter.responsible+"'";}
			number_of_conditions++;
		}			


		if (filter.status_checked)
		{
			String Status = "("+filter.status.replaceAll(";", ",")+"0)";

			if (filter.spinner_status == 0) {Where += ((number_of_conditions==0)?"":" AND ")+"tasks."+taskOrganizerDatabaseHelper.KEY_STATUS+" IN "+Status;}
			if (filter.spinner_status == 1) {Where += ((number_of_conditions==0)?"":" AND ")+"(NOT tasks."+taskOrganizerDatabaseHelper.KEY_STATUS+" IN "+Status+")";}
			number_of_conditions++;
		}			


		if (filter.category_checked)
		{
			String Category = "("+filter.category.replaceAll(";", ",")+"0)";

			if (filter.spinner_category == 0) {Where += ((number_of_conditions==0)?"":" AND ")+"tasks."+taskOrganizerDatabaseHelper.KEY_CATEGORY+" IN "+Category;}
			if (filter.spinner_category == 1) {Where += ((number_of_conditions==0)?"":" AND ")+"(NOT tasks."+taskOrganizerDatabaseHelper.KEY_CATEGORY+" IN "+Category+")";}
			number_of_conditions++;
		}				


		if (filter.date_checked)
		{
			Date date_from;
			Date date_to;

			if (filter.spinner_date_from == 0){date_from = filter.date_from;}
			else{date_from = getDateFromCondition(filter.spinner_date_from);}

			if (filter.spinner_date_to == 0){date_to = filter.date_to;}
			else{date_to = getDateFromCondition(filter.spinner_date_to);}

			date_from = RoundDate(date_from,0);
			if (filter.spinner_date<6){date_to = RoundDate(date_from,1);}
			else{date_to = RoundDate(date_to,1);}

			if (filter.show_empty_date_checked){
				Date date0 = new Date(0);
				if (filter.spinner_date == 0 || filter.spinner_date == 6) {Where += ((number_of_conditions==0)?"":" AND ")+"(tasks."+taskOrganizerDatabaseHelper.KEY_DATE+" = "+date0.getTime()+" OR (tasks."+taskOrganizerDatabaseHelper.KEY_DATE+" >= "+date_from.getTime()+" AND tasks."+taskOrganizerDatabaseHelper.KEY_DATE+" <= "+date_to.getTime()+"))";}
				if (filter.spinner_date == 1 || filter.spinner_date == 7) {Where += ((number_of_conditions==0)?"":" AND ")+"(tasks."+taskOrganizerDatabaseHelper.KEY_DATE+" < "+date_from.getTime()+" OR tasks."+taskOrganizerDatabaseHelper.KEY_DATE+" > "+date_to.getTime()+"))";}
				if (filter.spinner_date == 2) {Where += ((number_of_conditions==0)?"":" AND ")+"(tasks."+taskOrganizerDatabaseHelper.KEY_DATE+" = "+date0.getTime()+" OR (tasks."+taskOrganizerDatabaseHelper.KEY_DATE+" < "+date_from.getTime()+"))";}
				if (filter.spinner_date == 3) {Where += ((number_of_conditions==0)?"":" AND ")+"(tasks."+taskOrganizerDatabaseHelper.KEY_DATE+" = "+date0.getTime()+" OR (tasks."+taskOrganizerDatabaseHelper.KEY_DATE+" > "+date_to.getTime()+"))";}
				if (filter.spinner_date == 4) {Where += ((number_of_conditions==0)?"":" AND ")+"(tasks."+taskOrganizerDatabaseHelper.KEY_DATE+" = "+date0.getTime()+" OR (tasks."+taskOrganizerDatabaseHelper.KEY_DATE+" <= "+date_to.getTime()+"))";}
				if (filter.spinner_date == 5) {Where += ((number_of_conditions==0)?"":" AND ")+"(tasks."+taskOrganizerDatabaseHelper.KEY_DATE+" = "+date0.getTime()+" OR (tasks."+taskOrganizerDatabaseHelper.KEY_DATE+" >= "+date_from.getTime()+"))";}			
			}
			else{
				if (filter.spinner_date == 0 || filter.spinner_date == 6) {Where += ((number_of_conditions==0)?"":" AND ")+"(tasks."+taskOrganizerDatabaseHelper.KEY_DATE+" >= "+date_from.getTime()+" AND tasks."+taskOrganizerDatabaseHelper.KEY_DATE+" <= "+date_to.getTime()+")";}
				if (filter.spinner_date == 1 || filter.spinner_date == 7) {Where += ((number_of_conditions==0)?"":" AND ")+"(tasks."+taskOrganizerDatabaseHelper.KEY_DATE+" < "+date_from.getTime()+" OR tasks."+taskOrganizerDatabaseHelper.KEY_DATE+" > "+date_to.getTime()+")";}
				if (filter.spinner_date == 2) {Where += ((number_of_conditions==0)?"":" AND ")+"(tasks."+taskOrganizerDatabaseHelper.KEY_DATE+" < "+date_from.getTime()+")";}
				if (filter.spinner_date == 3) {Where += ((number_of_conditions==0)?"":" AND ")+"(tasks."+taskOrganizerDatabaseHelper.KEY_DATE+" > "+date_to.getTime()+")";}
				if (filter.spinner_date == 4) {Where += ((number_of_conditions==0)?"":" AND ")+"(tasks."+taskOrganizerDatabaseHelper.KEY_DATE+" <= "+date_to.getTime()+")";}
				if (filter.spinner_date == 5) {Where += ((number_of_conditions==0)?"":" AND ")+"(tasks."+taskOrganizerDatabaseHelper.KEY_DATE+" >= "+date_from.getTime()+")";}
			}
			number_of_conditions++;
		}			

		if (filter.duedate_checked)
		{
			Date date_from;
			Date date_to;

			if (filter.spinner_duedate_from == 0){date_from = filter.duedate_from;}
			else{date_from = getDateFromCondition(filter.spinner_duedate_from);}

			if (filter.spinner_duedate_to == 0){date_to = filter.duedate_to;}
			else{date_to = getDateFromCondition(filter.spinner_duedate_to);}

			date_from = RoundDate(date_from,0);
			if (filter.spinner_duedate<6){date_to = RoundDate(date_from,1);}
			else{date_to = RoundDate(date_to,1);}		

			if (filter.spinner_duedate == 0 || filter.spinner_date == 6) {Where += ((number_of_conditions==0)?"":" AND ")+"(tasks."+taskOrganizerDatabaseHelper.KEY_DUEDATE+" >= "+date_from.getTime()+" AND tasks."+taskOrganizerDatabaseHelper.KEY_DATE+" <= "+date_to.getTime()+")";}
			if (filter.spinner_duedate == 1 || filter.spinner_date == 7) {Where += ((number_of_conditions==0)?"":" AND ")+"(tasks."+taskOrganizerDatabaseHelper.KEY_DUEDATE+" < "+date_from.getTime()+" OR tasks."+taskOrganizerDatabaseHelper.KEY_DATE+" > "+date_to.getTime()+")";}
			if (filter.spinner_duedate == 2) {Where += ((number_of_conditions==0)?"":" AND ")+"(tasks."+taskOrganizerDatabaseHelper.KEY_DUEDATE+" < "+date_from.getTime()+")";}
			if (filter.spinner_duedate == 3) {Where += ((number_of_conditions==0)?"":" AND ")+"(tasks."+taskOrganizerDatabaseHelper.KEY_DUEDATE+" > "+date_to.getTime()+")";}
			if (filter.spinner_duedate == 4) {Where += ((number_of_conditions==0)?"":" AND ")+"(tasks."+taskOrganizerDatabaseHelper.KEY_DUEDATE+" <= "+date_to.getTime()+")";}
			if (filter.spinner_duedate == 5) {Where += ((number_of_conditions==0)?"":" AND ")+"(tasks."+taskOrganizerDatabaseHelper.KEY_DUEDATE+" >= "+date_from.getTime()+")";}
			number_of_conditions++;
		}			

		if (filter.releasedate_checked)
		{
			Date date_from;
			Date date_to;

			if (filter.spinner_releasedate_from == 0){date_from = filter.releasedate_from;}
			else{date_from = getDateFromCondition(filter.spinner_releasedate_from);}

			if (filter.spinner_releasedate_to == 0){date_to = filter.releasedate_to;}
			else{date_to = getDateFromCondition(filter.spinner_releasedate_to);}

			date_from = RoundDate(date_from,0);
			if (filter.spinner_releasedate<6){date_to = RoundDate(date_from,1);}
			else{date_to = RoundDate(date_to,1);}		

			if (filter.spinner_releasedate == 0 || filter.spinner_releasedate == 6) {Where += ((number_of_conditions==0)?"":" AND ")+"(tasks."+taskOrganizerDatabaseHelper.KEY_RELEASEDATE+" >= "+date_from.getTime()+" AND tasks."+taskOrganizerDatabaseHelper.KEY_DATE+" <= "+date_to.getTime()+")";}
			if (filter.spinner_releasedate == 1 || filter.spinner_releasedate == 7) {Where += ((number_of_conditions==0)?"":" AND ")+"(tasks."+taskOrganizerDatabaseHelper.KEY_RELEASEDATE+" < "+date_from.getTime()+" OR tasks."+taskOrganizerDatabaseHelper.KEY_DATE+" > "+date_to.getTime()+")";}
			if (filter.spinner_releasedate == 2) {Where += ((number_of_conditions==0)?"":" AND ")+"(tasks."+taskOrganizerDatabaseHelper.KEY_RELEASEDATE+" < "+date_from.getTime()+")";}
			if (filter.spinner_releasedate == 3) {Where += ((number_of_conditions==0)?"":" AND ")+"(tasks."+taskOrganizerDatabaseHelper.KEY_RELEASEDATE+" > "+date_to.getTime()+")";}
			if (filter.spinner_releasedate == 4) {Where += ((number_of_conditions==0)?"":" AND ")+"(tasks."+taskOrganizerDatabaseHelper.KEY_RELEASEDATE+" <= "+date_to.getTime()+")";}
			if (filter.spinner_releasedate == 5) {Where += ((number_of_conditions==0)?"":" AND ")+"(tasks."+taskOrganizerDatabaseHelper.KEY_RELEASEDATE+" >= "+date_from.getTime()+")";}
			number_of_conditions++;
		}			

		if (number_of_conditions>0){
			return Where;
		}
		else 
		{
			return "";
		}
	}


	public Date RoundDate(Date date0, int UpOrDown)
	{
		if (UpOrDown == 1){
			return new Date(date0.getYear(),date0.getMonth(),date0.getDate(),23,59,59);}
		else{
			return new Date(date0.getYear(),date0.getMonth(),date0.getDate(),0,0,0);}

	}


	public Date getDateFromCondition(int position)
	{
		Date date0 = new Date();
		//Calendar cal = Calendar.getInstance(); 
		//cal.setTime(date0); 
		//int currentDOW = cal.get(Calendar.DAY_OF_WEEK);
		//cal.add(Calendar.DAY_OF_YEAR, (currentDOW * -1)+1);

		if (position == 2)//begin of the week
		{
			int numberoftheday = date0.getDay();
			numberoftheday = (numberoftheday==0)?7:numberoftheday;		
			long miliseconds = date0.getTime() - (numberoftheday-1)*24*3600*1000;
			date0 = new Date(miliseconds);
		} else if (position == 3)//end of the week
		{
			int numberoftheday = date0.getDay();
			numberoftheday = (numberoftheday==0)?7:numberoftheday;
			long miliseconds = date0.getTime() + (7-numberoftheday)*24*3600*1000;
			date0 = new Date(miliseconds);		
		} else if (position == 4)//beginning of the month
		{
			date0 = new Date(date0.getYear(),date0.getMonth(),1,0,0,0);

		} else if (position == 5)//end of the month
		{
			date0 = new Date(date0.getYear(),date0.getMonth(),1,0,0,0);

			if (date0.getMonth()==12)
			{
				date0 = new Date(date0.getYear(),12,31,0,0,0);
			}
			else
			{
				Date date1 = new Date(date0.getYear(),date0.getMonth()+1,1,0,0,0);
				date1 = new Date(date1.getTime()-1);
				date0 = new Date(date0.getYear(),date0.getMonth(),date1.getDate(),23,59,59);
			}
		} else if (position == 6)//beginning of the year
		{
			date0 = new Date(date0.getYear(),1,1,0,0,0);	
		} else if (position == 7)//beginning of the year
		{
			date0 = new Date(date0.getYear(),12,31,23,59,59);	
		}


		return date0;
	}

	private void loadTasksFromProvider() {

		c = query(null, null, null, null);

		//ll = new MySimpleCursorAdapter(this, R.layout.row, c, new String[]{taskOrganizerDatabaseHelper.KEY_NAME,
		//		taskOrganizerDatabaseHelper.KEY_DATE}, new int[]{R.id.nameoftask,R.id.date});
		ll = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, c, new String[]{"name"}, new int[]{android.R.id.text1});

		tasksListView.setAdapter(ll);

		startManagingCursor(c);

	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuItem myMenuItem;


		myMenuItem = menu.add(0, MENU_FILTER, Menu.NONE, R.string.menu_filter);
		myMenuItem.setIcon(R.drawable.ic_menu_filter);
		myMenuItem = menu.add(0, MENU_FILTER_SAVED, Menu.NONE, R.string.menu_filter_saved);
		myMenuItem.setIcon(R.drawable.ic_menu_set_as);		

		return true;
	}


	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {

		case (MENU_FILTER): {
			Intent i = new Intent(this, filter_form.class);
			filter.ToXML();
			i.putExtra("filter", filter.xmlstring);
			i.putExtra("id", -1);
			i.putExtra("name_of_filter", "");
			startActivityForResult(i,FILTER_FORM_OPENED);	
			return true;
		}	  		
		case (MENU_FILTER_SAVED): {
			Intent i = new Intent(this, ListOfCatalog.class);
			i.putExtra("Table", 3);
			startActivityForResult(i,FILTER_CHOOSE_SAVED);
			return true;
		}	  		

		} 
		return false;
	}



	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == FILTER_FORM_OPENED && resultCode == Activity.RESULT_OK)	
		{
			String xml = data.getExtras().getString("filter");
			filter = new Filter(xml);
			loadTasksFromProvider();
		}
		if (requestCode == FILTER_CHOOSE_SAVED && resultCode == Activity.RESULT_OK)	
		{
			String xml = data.getExtras().getString("filter");
			filter = new Filter(xml);
			loadTasksFromProvider();
		}		
	} 


}