package com.eugenemath.taskorganizer.namespace;




import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.eugenemath.taskorganizer.namespace.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Toast;


public class ConsumedTime extends Activity {


	//ListView tasksListView;  
	//CTLIstViewAdapter ll;
	private Cursor c, c_category;

	private SQLiteDatabase tasksDB;	
	private myDBHelper dbHelper;

	private int REQUEST_DATE_TIME = 1; 
	private int FILTER_FORM_OPENED = 2;
	static final int DIALOG_CHOOSE_CATEGORY = 3;
	static final int REQUEST_CONTEXT_MENU = 4;
	private Filter filter;
	private int CurrentPosition = -1;

	private int CurrentPage=0;
	CharSequence[] categories;
	int[] idcategories;
	int[] bgcolors;
	public int CurrentChosenPosition = 0;
	public float scale;
	
	SimpleDateFormat sdf_ddMM = new SimpleDateFormat("dd MMM yyyy");
	private List<CTRow> list;


	private void DeleteItem(int _index) {

		/*    //Toast.makeText(getApplicationContext(), "Index = "+_index, Toast.LENGTH_SHORT).show();
			if (CurrentTable == 1 && CurrentChoosenId<3) 
			{
				Toast.makeText(getApplicationContext(), R.string.denied_to_delete_catalog, Toast.LENGTH_SHORT).show();
				return;
			}
			if (CurrentTable == 2 && CurrentChoosenId<6) 
			{
				Toast.makeText(getApplicationContext(), R.string.denied_to_delete_catalog, Toast.LENGTH_SHORT).show();
				return;
			}	

			int count =tasksDB.delete(CurrentTableName,KEY_ID+"="+_index,null);
		    c.requery();
		    ll.notifyDataSetChanged();	*/		
	}


	private static class myDBHelper extends SQLiteOpenHelper {	
		public myDBHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {	}
		@Override
		public void onCreate(SQLiteDatabase db) {
		}  
	}


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (CurrentPage==1)
		{
			FlatReport();
		}
	}



	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sdf_ddMM = new SimpleDateFormat(getString(R.string.sdf1));

		dbHelper = new myDBHelper(this,  
				taskOrganizerDatabaseHelper.DATABASE_NAME, 
				null, 
				taskOrganizerDatabaseHelper.DATABASE_VERSION);
		tasksDB = dbHelper.getWritableDatabase();

		// Inflate your view
		setContentView(R.layout.consumed_time);

		//tasksListView = (ListView)this.findViewById(R.id.ListView_ct);

		c_category =  query_categories();
		FillCategoriesToChoose();


		//textview_head = (TextView)this.findViewById(R.id.button_ct_report);
		

		filter = new Filter("");

		c = query(null,null,null,"");

		//ll = new CTLIstViewAdapter(getBaseContext());

		//tasksListView.setAdapter(ll);


		ImageButton button_ct_new = (ImageButton)this.findViewById(R.id.button_ct_new);

		button_ct_new.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				CTRow ctrow = new CTRow();
				CurrentPosition = -1;
				list.add(ctrow);
				int position = list.size()-1;
				
				LinearLayout ll_ct = (LinearLayout)findViewById(R.id.linearLayout_ct3);
				
				View vv1= getRowView(position,ll_ct);
				ll_ct.addView(vv1);
				
				list.get(position).rowView = (LinearLayout) vv1;

				ScrollView scrollview_ct_empty = (ScrollView)ll_ct.getParent().getParent();

				scrollview_ct_empty.post(new Runnable() {
					public void run() {
						LinearLayout ll_ct = (LinearLayout)findViewById(R.id.linearLayout_ct3);
						ScrollView scrollview_ct_empty = (ScrollView)ll_ct.getParent().getParent();
							scrollview_ct_empty.fullScroll(ScrollView.FOCUS_DOWN);
					}
				});
				
				
			}	    
		}); 

		ImageButton button_ct_filter = (ImageButton)this.findViewById(R.id.button_ct_filter);

		button_ct_filter.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(ConsumedTime.this, filter_form_consumed_time.class);
				filter.ToXML();
				i.putExtra("filter", filter.xmlstring);
				i.putExtra("id", -1);
				i.putExtra("name_of_filter", "");
				startActivityForResult(i,FILTER_FORM_OPENED);	
			}	    
		}); 


		ImageButton button_ct_save = (ImageButton)this.findViewById(R.id.button_ct_save);

		button_ct_save.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				SaveChanges();
			}	    
		}); 


		ImageButton button_ct_report = (ImageButton)this.findViewById(R.id.button_ct_report);

		button_ct_report.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(ConsumedTime.this,flexiblemenu.class);
				i.putExtra("Regim",  "ConsumedTime");
				startActivityForResult(i, REQUEST_CONTEXT_MENU);
			}	    
		}); 

		scale =  getBaseContext().getResources().getDisplayMetrics().density;
		
		sdf_ddMM = new SimpleDateFormat(getString(R.string.sdf1)); 
		FillTable();
	}


	public void SaveChanges()
	{
		for (int i=0;i<list.size();i++)
		{
			CTRow ctrow = list.get(i);
			if (!ctrow.tosave)
			{
				continue;
			}

			/*String stringvalue = ctrow.etValue.toString();
			if (stringvalue.length()==0){
				stringvalue = "0";
			}
			ctrow.value = Integer.valueOf(stringvalue);
			ctrow.comment = ctrow.etComment.toString();*/


			ContentValues values = new ContentValues(); 
			values.put(taskOrganizerDatabaseHelper.Consumed_Time_Date, ctrow.date.getTime());
			values.put(taskOrganizerDatabaseHelper.Consumed_Time_Value, ctrow.value);
			values.put(taskOrganizerDatabaseHelper.Consumed_Time_Comment, ctrow.comment);
			values.put(taskOrganizerDatabaseHelper.Consumed_Time_Category, ctrow.category_id);


			if (ctrow.id!=-1)
			{
				String where = taskOrganizerDatabaseHelper.Consumed_Time_ID + "=" + ctrow.id;
				long rowID = tasksDB.update(taskOrganizerDatabaseHelper.Consumed_Time_Table,values,where,null);
				//long rowID = tasksDB.insert(taskOrganizerDatabaseHelper.Tasks_TABLE, "item", values);
			}
			else
			{
				long rowID = tasksDB.insert(taskOrganizerDatabaseHelper.Consumed_Time_Table, "item", values);
				ctrow.id = (int) rowID;
			}

			if (ctrow.idtask!=-1)
			{
				String where = taskOrganizerDatabaseHelper.KEY_ID + "=" + ctrow.idtask;
				values = new ContentValues(); 
				values.put(taskOrganizerDatabaseHelper.KEY_PROCESSINGTIMEFACT, ctrow.value);//minutes
				long rowID = tasksDB.update(taskOrganizerDatabaseHelper.Tasks_TABLE,values,where,null);		
			}

			ctrow.tosave = false; 
			ctrow.rowView.setBackgroundColor(Color.WHITE);
		}
		//ll.notifyDataSetChanged();
		//tasksListView.setAdapter(ll);
	}

	public Cursor query(String[] projection, 
			String selection, 
			String[] selectionArgs, 
			String sort) {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String SelectQuery = "SELECT "
				+ taskOrganizerDatabaseHelper.Consumed_Time_Table+"."+taskOrganizerDatabaseHelper.Consumed_Time_ID+" as _id,"
				+ taskOrganizerDatabaseHelper.Consumed_Time_Table+"."+taskOrganizerDatabaseHelper.Consumed_Time_Category+","
				+ taskOrganizerDatabaseHelper.Consumed_Time_Table+"."+taskOrganizerDatabaseHelper.Consumed_Time_Comment+","
				+ taskOrganizerDatabaseHelper.Consumed_Time_Table+"."+taskOrganizerDatabaseHelper.Consumed_Time_Date+","
				+ taskOrganizerDatabaseHelper.Consumed_Time_Table+"."+taskOrganizerDatabaseHelper.Consumed_Time_Task+","
				+ taskOrganizerDatabaseHelper.Consumed_Time_Table+"."+taskOrganizerDatabaseHelper.Consumed_Time_Value+","
				+ taskOrganizerDatabaseHelper.Tasks_TABLE+"."+ taskOrganizerDatabaseHelper.KEY_NAME+" as taskname,"
			    + taskOrganizerDatabaseHelper.Categories_Table+"."+ taskOrganizerDatabaseHelper.Categories_BG_COLOR+" as categorybgcolor,"
				+ taskOrganizerDatabaseHelper.Categories_Table+"."+ taskOrganizerDatabaseHelper.Categories_NAME+" as categoryname"
				+" FROM "+ taskOrganizerDatabaseHelper.Consumed_Time_Table
				+" LEFT JOIN "+ taskOrganizerDatabaseHelper.Categories_Table+" ON "+ taskOrganizerDatabaseHelper.Consumed_Time_Table+"."+ taskOrganizerDatabaseHelper.Consumed_Time_Category+" = "+ taskOrganizerDatabaseHelper.Categories_Table+"."+ taskOrganizerDatabaseHelper.Categories_ID
				+" LEFT JOIN "+ taskOrganizerDatabaseHelper.Tasks_TABLE+" as tasks ON "+ taskOrganizerDatabaseHelper.Consumed_Time_Table+"."+ taskOrganizerDatabaseHelper.Consumed_Time_Task+" = "+ taskOrganizerDatabaseHelper.Tasks_TABLE+"."+ taskOrganizerDatabaseHelper.KEY_ID;



		SelectQuery += ConstructWhereClause();

		String orderBy;
		if (TextUtils.isEmpty(sort)) {
			orderBy = taskOrganizerDatabaseHelper.Consumed_Time_Table+"."+ taskOrganizerDatabaseHelper.Consumed_Time_Date;
		} else {
			orderBy = sort;
		}

		//SelectQuery +=" ORDER BY "+orderBy;	


		Cursor c = tasksDB.rawQuery(SelectQuery, selectionArgs);
		return c;
	}	

	public String ConstructWhereClause()
	{
		String Where = " WHERE ";
		int number_of_conditions = 0;



		if (filter.category_checked)
		{
			String Category = "("+filter.category.replaceAll(";", ",")+"0)";

			if (filter.spinner_category == 0) {Where += ((number_of_conditions==0)?"":" AND ")+"consumedtime."+taskOrganizerDatabaseHelper.Consumed_Time_Category+" IN "+Category;}
			if (filter.spinner_category == 1) {Where += ((number_of_conditions==0)?"":" AND ")+"(NOT consumedtime."+taskOrganizerDatabaseHelper.Consumed_Time_Category+" IN "+Category+")";}
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

			/*if (filter.show_empty_date_checked){
				Date date0 = new Date(0);
				if (filter.spinner_date == 0 || filter.spinner_date == 6) {Where += ((number_of_conditions==0)?"":" AND ")+"(consumedtime."+taskOrganizerDatabaseHelper.KEY_DATE+" = "+date0.getTime()+" OR (consumedtime."+taskOrganizerDatabaseHelper.KEY_DATE+" >= "+date_from.getTime()+" AND consumedtime."+taskOrganizerDatabaseHelper.KEY_DATE+" <= "+date_to.getTime()+"))";}
				if (filter.spinner_date == 1 || filter.spinner_date == 7) {Where += ((number_of_conditions==0)?"":" AND ")+"(consumedtime."+taskOrganizerDatabaseHelper.KEY_DATE+" < "+date_from.getTime()+" OR tasks."+taskOrganizerDatabaseHelper.KEY_DATE+" > "+date_to.getTime()+"))";}
				if (filter.spinner_date == 2) {Where += ((number_of_conditions==0)?"":" AND ")+"(consumedtime."+taskOrganizerDatabaseHelper.KEY_DATE+" = "+date0.getTime()+" OR (tasks."+taskOrganizerDatabaseHelper.KEY_DATE+" < "+date_from.getTime()+"))";}
				if (filter.spinner_date == 3) {Where += ((number_of_conditions==0)?"":" AND ")+"(consumedtime."+taskOrganizerDatabaseHelper.KEY_DATE+" = "+date0.getTime()+" OR (tasks."+taskOrganizerDatabaseHelper.KEY_DATE+" > "+date_to.getTime()+"))";}
				if (filter.spinner_date == 4) {Where += ((number_of_conditions==0)?"":" AND ")+"(consumedtime."+taskOrganizerDatabaseHelper.KEY_DATE+" = "+date0.getTime()+" OR (tasks."+taskOrganizerDatabaseHelper.KEY_DATE+" <= "+date_to.getTime()+"))";}
				if (filter.spinner_date == 5) {Where += ((number_of_conditions==0)?"":" AND ")+"(consumedtime."+taskOrganizerDatabaseHelper.KEY_DATE+" = "+date0.getTime()+" OR (tasks."+taskOrganizerDatabaseHelper.KEY_DATE+" >= "+date_from.getTime()+"))";}			
			}
			else{*/
			if (filter.spinner_date == 0 || filter.spinner_date == 6) {Where += ((number_of_conditions==0)?"":" AND ")+"(consumedtime."+taskOrganizerDatabaseHelper.Consumed_Time_Date+" >= "+date_from.getTime()+" AND consumedtime."+taskOrganizerDatabaseHelper.Consumed_Time_Date+" <= "+date_to.getTime()+")";}
			if (filter.spinner_date == 1 || filter.spinner_date == 7) {Where += ((number_of_conditions==0)?"":" AND ")+"(consumedtime."+taskOrganizerDatabaseHelper.Consumed_Time_Date+" < "+date_from.getTime()+" OR consumedtime."+taskOrganizerDatabaseHelper.Consumed_Time_Date+" > "+date_to.getTime()+")";}
			if (filter.spinner_date == 2) {Where += ((number_of_conditions==0)?"":" AND ")+"(consumedtime."+taskOrganizerDatabaseHelper.Consumed_Time_Date+" < "+date_from.getTime()+")";}
			if (filter.spinner_date == 3) {Where += ((number_of_conditions==0)?"":" AND ")+"(consumedtime."+taskOrganizerDatabaseHelper.Consumed_Time_Date+" > "+date_to.getTime()+")";}
			if (filter.spinner_date == 4) {Where += ((number_of_conditions==0)?"":" AND ")+"(consumedtime."+taskOrganizerDatabaseHelper.Consumed_Time_Date+" <= "+date_to.getTime()+")";}
			if (filter.spinner_date == 5) {Where += ((number_of_conditions==0)?"":" AND ")+"(consumedtime."+taskOrganizerDatabaseHelper.Consumed_Time_Date+" >= "+date_from.getTime()+")";}
			//}
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

	public void FillCategoriesToChoose()
	{

		categories = new String[c_category.getCount()]; 
		idcategories = new int[c_category.getCount()]; 
		bgcolors = new int[c_category.getCount()]; 
		
		
		long _id;
		String _category_name = "";

		int pos = 0;
		if (c_category.moveToFirst()) {
			do { 
				//_id = c_category.getLong(c_category.getColumnIndex("_id"));
				categories[pos] = c_category.getString(c_category.getColumnIndex(taskOrganizerDatabaseHelper.Categories_NAME));
				idcategories[pos] = (int) c_category.getLong(c_category.getColumnIndex("_id"));
				bgcolors[pos] =  c_category.getInt(c_category.getColumnIndex("categorybgcolor"));
				pos++;
			} while(c_category.moveToNext());
		}		
	}

	public Cursor query_categories() {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String SelectQuery = "SELECT "
				+ taskOrganizerDatabaseHelper.Categories_ID+" as _id,"
				+ taskOrganizerDatabaseHelper.Categories_NAME+" as name,"
				+ taskOrganizerDatabaseHelper.Categories_BG_COLOR+" as categorybgcolor"
				+" FROM "+ taskOrganizerDatabaseHelper.Categories_Table;

		Cursor _c = tasksDB.rawQuery(SelectQuery, null);
		return _c;
	}		

	
	public void FillTable()
	{
		int position = -1;
		LinearLayout ll_ct = (LinearLayout)this.findViewById(R.id.linearLayout_ct3);
		ll_ct.removeAllViews();
		this.list = new ArrayList<CTRow>();
		
		if (c.moveToFirst())
		{
			do {         
				CTRow ctrow = new CTRow(c);
				list.add(ctrow);					
				position++;
				View vv1= getRowView(position,ll_ct);
				ll_ct.addView(vv1);
				
				list.get(position).rowView = (LinearLayout) vv1;
			} while(c.moveToNext());			
		}	
		
		
		ScrollView scrollview_ct_empty = (ScrollView)ll_ct.getParent().getParent();
		
		
		scrollview_ct_empty.post(new Runnable() {
			public void run() {
				LinearLayout ll_ct = (LinearLayout)findViewById(R.id.linearLayout_ct3);
				ScrollView scrollview_ct_empty = (ScrollView)ll_ct.getParent().getParent();
					scrollview_ct_empty.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
		
		
	}
	
	private class GenericTextWatcher implements TextWatcher{

		private int position;
		private int field;
		private LinearLayout rowView;
		private GenericTextWatcher(int _position, int _field, LinearLayout _rowView) {
			this.position = _position;
			this.field = _field;
			this.rowView = _rowView;
		}

		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
		public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

		public void afterTextChanged(Editable editable) {	       
			CTRow ctrow = list.get(position);


			if ((CurrentPosition != position) && CurrentPosition!=-1)
			{
				return;
			}

			//Log.d(tag, "Position: " + position);


			String stringvalue = editable.toString();
			if (field == 1)
			{

				if (!(""+ctrow.value).equals(stringvalue))
				{
					if (stringvalue.length()==0 || stringvalue.equals("-")){
						stringvalue = "0";
					}

				    ctrow.value = Integer.valueOf(stringvalue);
					ctrow.tosave = true;
					rowView.setBackgroundColor(Color.LTGRAY);

				}
			}
			else
			{

				if (ctrow.comment == null)
				{
					ctrow.comment = "";
				}

				if (!ctrow.comment.equals(stringvalue))
				{
					ctrow.comment = stringvalue;
					ctrow.tosave = true;
					rowView.setBackgroundColor(Color.LTGRAY);
				}
			}
			//CurrentPosition = -1;
		}
	}

	public View getRowView(int position,ViewGroup parent)
	{

		LinearLayout rowView = null;
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		rowView = (LinearLayout) inflater.inflate(R.layout.row_consumed_time, parent, false);

		TextView tvDate = (TextView) rowView.findViewById(R.id.textView_ct_date);
		TextView tvCategory = (TextView) rowView.findViewById(R.id.textView_ct_category);
		EditText etValue = (EditText) rowView.findViewById(R.id.edittext_ct_value);
		EditText etComment = (EditText) rowView.findViewById(R.id.edittext_ct_comment2);				


		list.get(position).etValue = etValue;
		list.get(position).etComment = etComment;

		OnClickListener myDateOnClickListener = new Button.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(ConsumedTime.this,date_time_picker.class);

				CTRow ctrow = list.get(Integer.valueOf((String) v.getTag()));

				i.putExtra("choose_time",  0);//0 or 1
				i.putExtra("button",  (String) v.getTag());
				i.putExtra("begin_of_the_day_or_end",  0);//0 or 1
				i.putExtra("mseconds",ctrow.date.getTime());
				startActivityForResult(i, REQUEST_DATE_TIME);	
			}
		};


		OnClickListener myCategoryOnClickListener = new Button.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(ConsumedTime.this,date_time_picker.class);

				CTRow ctrow = list.get(Integer.valueOf((String) v.getTag()));

				CurrentChosenPosition = Integer.valueOf((String) v.getTag());

				showDialog(DIALOG_CHOOSE_CATEGORY);
			}
		};
		
		
		TextView tvTask = (TextView) rowView.findViewById(R.id.textView_ct_task);

		tvDate.setText(sdf_ddMM.format(list.get(position).date));
		tvDate.setTag(""+position);
		tvDate.setOnClickListener(myDateOnClickListener);

		GenericTextWatcher gtw = new GenericTextWatcher(position,1,rowView);
		etValue.setText(""+list.get(position).value);
		etValue.setTag(""+position);

		etValue.addTextChangedListener(gtw);

		GenericTextWatcher gtw2 = new GenericTextWatcher(position,2,rowView);
		String comment = list.get(position).comment;
		String category = list.get(position).categoryname;


		if (category.length()>0)
		{
			int bgcolor = list.get(position).bgcolor;
			tvCategory.setPadding(5, 0, 5, 0);
			if (bgcolor != 0) {
				tvCategory.setBackgroundDrawable(CommonFunctions.GetDrawable(bgcolor));
			}
			else
			{
				tvCategory.setBackgroundDrawable(CommonFunctions.GetDrawable(Color.DKGRAY));			
			}
			tvCategory.setTextColor(Color.WHITE);
			tvCategory.setText(category);
		}
		else
		{
			tvCategory.setBackgroundColor(Color.WHITE);
			tvCategory.setTextColor(Color.RED);
			tvCategory.setText("Pick a category");
		}	
		
		
		tvCategory.setTag(""+position);
		tvCategory.setOnClickListener(myCategoryOnClickListener);


		String taskname = list.get(position).taskname;
		if (list.get(position).taskname.length()>0)
		{
			tvTask.setText(taskname);
			tvTask.setVisibility(View.VISIBLE);
		}
		else
		{
			tvTask.setText("");
			tvTask.setVisibility(View.GONE);
		}

		return rowView;
	}

	private class CTRow{
		public int id=-1, category_id=-1, idtask=-1;
		public int value = 0;
		public String taskname="",categoryname="";
		public Date date = new Date();
		public String comment = "";
		public boolean tosave = false;
		public EditText etValue;
		public EditText etComment;
		public int bgcolor;
		public LinearLayout rowView;


		public CTRow(Cursor c)	
		{
			//int idtask = 	c.getInt(c.getColumnIndex("_id"));
			//int parent =  c.getInt(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_PARENT));
			id = 	c.getInt(c.getColumnIndex("_id"));
			idtask = 	c.getInt(c.getColumnIndex(taskOrganizerDatabaseHelper.Consumed_Time_Task));
			value = 	c.getInt(c.getColumnIndex(taskOrganizerDatabaseHelper.Consumed_Time_Value));
			category_id = c.getInt(c.getColumnIndex(taskOrganizerDatabaseHelper.Consumed_Time_Category));
			date  =new Date(c.getLong(c.getColumnIndex(taskOrganizerDatabaseHelper.Consumed_Time_Date)));
			comment  =c.getString(c.getColumnIndex(taskOrganizerDatabaseHelper.Consumed_Time_Comment));
			taskname  =c.getString(c.getColumnIndex("taskname"));
			categoryname  =c.getString(c.getColumnIndex("categoryname"));
			bgcolor = c.getInt(c.getColumnIndex("categorybgcolor"));
			

			if (taskname == null)
				taskname = "";

			if (categoryname == null)
				categoryname = "";		

		}

		public CTRow()	
		{
			id=-1;
			category_id=-1;
			idtask=-1;
			tosave = false;
		}


		@Override
		public String toString() {
			return ""+id;
		}
	}

	public void afterChooseOfCategories(int item)
	{
		CTRow ctrow = list.get(CurrentChosenPosition);
		ctrow.categoryname = (String) categories[item];
		ctrow.category_id = idcategories[item];
		ctrow.tosave = true;
		ctrow.rowView.setBackgroundColor(Color.LTGRAY);
		ctrow.bgcolor = bgcolors[item];
		
		TextView tvCategory = (TextView) ctrow.rowView.findViewById(R.id.textView_ct_category);

		int bgcolor = ctrow.bgcolor ;
		tvCategory.setPadding(5, 0, 5, 0);
		if (bgcolor != 0) {
			tvCategory.setBackgroundDrawable(CommonFunctions.GetDrawable(bgcolor));
		}
		else
		{
			tvCategory.setBackgroundDrawable(CommonFunctions.GetDrawable(Color.DKGRAY));			
		}
		tvCategory.setTextColor(Color.WHITE);
		tvCategory.setText(ctrow.categoryname);


		
		//ll.notifyDataSetChanged();
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch(id) {
		case DIALOG_CHOOSE_CATEGORY:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.from_code38));
			builder.setItems(categories, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					afterChooseOfCategories(item);
					//Toast.makeText(getApplicationContext(), categories[item], Toast.LENGTH_SHORT).show();
				}
			});
			return builder.create();
			//break;

		default:
			dialog = null;
		}
		return dialog;
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);


		if (requestCode == REQUEST_DATE_TIME && resultCode == Activity.RESULT_OK)
		{
			int  position= Integer.valueOf(data.getExtras().getString("button"));
			CTRow ctrow = list.get(position);
			ctrow.date = new Date(data.getExtras().getLong("mseconds"));
			ctrow.tosave = true;
			ctrow.rowView.setBackgroundColor(Color.LTGRAY);
			
			TextView tvDate = (TextView) ctrow.rowView.findViewById(R.id.textView_ct_date);
			tvDate.setText(sdf_ddMM.format(list.get(position).date));
			//FillTable();
			//ll.notifyDataSetChanged();
		}

		if (requestCode == FILTER_FORM_OPENED && resultCode == Activity.RESULT_OK)	
		{
			String xml = data.getExtras().getString("filter");
			filter = new Filter(xml);
			c = query(null,null,null,null);
			FillTable();
			//ll = new CTLIstViewAdapter(getBaseContext());
			//tasksListView.setAdapter(ll);
		}


		if (requestCode == REQUEST_CONTEXT_MENU)
		{
			if (resultCode == Activity.RESULT_OK) 
			{
				if (data.getExtras().getInt("Result") == 0)
				{				
					SaveToCSV();
				}
				else if (data.getExtras().getInt("Result") == 1)
				{
					FlatReport();
				}

			}			
		}	


	}

	public void SaveToCSV()
	{

		SimpleDateFormat sdf_ddMM0 = new SimpleDateFormat("yyyyMMdd_HHmmss");
		SimpleDateFormat sdf_ddMM1 = new SimpleDateFormat("dd.MM.yyyy");
		String FILENAME = "consumed_time_"+(sdf_ddMM0.format(new Date()))+".csv";

		boolean filefound = false;
		FileOutputStream fos = null;
		try {	
			File sdCard = Environment.getExternalStorageDirectory();
			File dir = new File(sdCard.getAbsolutePath());
			dir.mkdirs();
			fos = new FileOutputStream(new File(dir, FILENAME));	
			filefound = true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Toast.makeText(this, this.getString(R.string.from_code23)+" "+FILENAME+" "+this.getString(R.string.from_code24), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}		

		try {	
			if (!filefound)
			{
				fos = this.openFileOutput(FILENAME, Context.MODE_WORLD_READABLE);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Toast.makeText(this, this.getString(R.string.from_code23)+" "+FILENAME+" "+this.getString(R.string.from_code25), Toast.LENGTH_LONG).show();
			e.printStackTrace();
			return;
		}		


		String tosave  = this.getString(R.string.from_code67)+"\n";
		CTRow ctrow;

		for (int i=0;i<list.size();i++)
		{
			ctrow = list.get(i);
			tosave+=(sdf_ddMM1.format(ctrow.date))+";"+ctrow.categoryname+";"+ctrow.value+";"+ctrow.comment+";"+ctrow.taskname+"\n";
		}


		try {
			fos.write(tosave.getBytes());
			Toast.makeText(this, this.getString(R.string.from_code23)+" "+FILENAME+" "+this.getString(R.string.from_code26)+((filefound)?"":(" "+this.getString(R.string.from_code27))), Toast.LENGTH_LONG).show();
			return;						
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	public int getPixels(float dip)
	{
		return Math.round(dip*scale);
	}

	public void FlatReport()
	{
		CurrentPage = 1;
		ShowOrHidePages();
		
		
		Map<String,Integer> ReportMap = new LinkedHashMap<String,Integer>();
		
		
		RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeLayout_ct3);
		rl.removeAllViews();

		RelativeLayout.LayoutParams params;

		/*ImageView iv;
		iv = new ImageView(this);
		iv.setBackgroundColor(Color.BLACK);
		params =  new RelativeLayout.LayoutParams(1, getPixels(30f)*27);
		iv.setLayoutParams(params);
		rl.addView(iv, params);	*/
		
		
		CTRow ctrow;
		int maximalvalue = 0;
		int maximallength = 1;
		
		List<String> listofnames = new ArrayList<String>();
		List<Integer> bgcolors = new ArrayList<Integer>();
		
		for (int i=0;i<list.size();i++)
		{
			ctrow = list.get(i);
			
			String categoryname = ctrow.categoryname.trim();
			int bgcolor = ctrow.bgcolor;
			if (categoryname.length() ==0)
			{
				categoryname = "-";
			}
			
			int value = 0;
			if (ReportMap.containsKey(categoryname))
			{				
				value = ReportMap.get(categoryname)+ctrow.value;
			}
			else
			{
				value = ctrow.value;
			}
			
			if (!listofnames.contains(categoryname))
			{
				listofnames.add(categoryname);
				bgcolors.add(bgcolor);
				if (maximallength<categoryname.length())
				{
					maximallength = categoryname.length();
				}
			}
			
			ReportMap.put(categoryname, value);
			if (maximalvalue<value)
			{
				maximalvalue = value;
			}
			
			
			//tosave+=(sdf_ddMM1.format(ctrow.date))+";"+ctrow.categoryname+";"+ctrow.value+";"+ctrow.comment+";"+ctrow.taskname+"\n";
		}
		
		maximallength = Math.min(maximallength,15);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);	

		int pixelscategory = (int)getPixels(maximallength*8f);
		pixelscategory = Math.max(pixelscategory,metrics.widthPixels/4);
		
		int width = metrics.widthPixels - 2*pixelscategory-20;
		
		
		
		//String[] arrayofnames = (String[]) ReportMap.keySet().toArray();
		
		
		
		
		for (int i=0;i<ReportMap.size();i++)
		{
			TextView tv = new TextView(this);
			tv.setTextColor(Color.BLACK);
			params = new RelativeLayout.LayoutParams(pixelscategory, getPixels(30f));
			tv.setText(listofnames.get(i));
			params.leftMargin = 5;
			params.topMargin = getPixels(20f)+getPixels(30f)*(i)+5*i;
			rl.addView(tv, params);	
			
			int value = ReportMap.get(listofnames.get(i));
			
			int width0 = Math.max(10,(int)(width*((float)value/maximalvalue)));
			ImageView iv = new ImageView(this);
			//iv.setBackgroundColor(Color.parseColor("#FE9A2E"));
			int bgcolor = bgcolors.get(i);
			
			if (bgcolor == 0) {bgcolor = Color.parseColor("#FE9A2E");}
			iv.setBackgroundDrawable(CommonFunctions.GetDrawableNoCorner(bgcolor));
			
			params = new RelativeLayout.LayoutParams(width0, getPixels(30f));
			params.leftMargin = pixelscategory+8;
			params.topMargin = getPixels(20f)+getPixels(30f)*(i)+5*i;
			rl.addView(iv, params);		
			
			
			float hours = ((float)Math.round((float)value/6))/10;
			
			tv = new TextView(this);
			tv.setTextColor(Color.BLACK);
			params = new RelativeLayout.LayoutParams(pixelscategory, getPixels(30f));
			tv.setText(""+hours+" h");
			params.leftMargin = 15+width0+pixelscategory;
			params.topMargin = getPixels(20f)+getPixels(30f)*(i)+5*i;
			rl.addView(tv, params);
			
		}
	
		
		
	}


	private void ShowOrHidePages()
	{
		LinearLayout ct1 = (LinearLayout)findViewById(R.id.linearLayout_ct1);
		LinearLayout ct2 = (LinearLayout)findViewById(R.id.linearLayout_ct2);
		LinearLayout linearLayout_ct_buttons = (LinearLayout)findViewById(R.id.linearLayout_ct_buttons);


		if (CurrentPage == 0)
		{
			ct1.setVisibility(View.VISIBLE);
			ct2.setVisibility(View.GONE);
			linearLayout_ct_buttons.setVisibility(View.VISIBLE);
		}
		else
		{
			ct1.setVisibility(View.GONE);
			ct2.setVisibility(View.VISIBLE);
			linearLayout_ct_buttons.setVisibility(View.GONE);	
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 && CurrentPage==1) {
			CurrentPage = 0;
			ShowOrHidePages();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	
	//TO DELETE

	public class CTLIstViewAdapter extends BaseAdapter 
	{

		private static final String tag = "CTLIstViewAdapter";
		private final Context _context;

		private final List<CTRow> list;



		public CTLIstViewAdapter(Context context)
		{
			//Calendar calendar = Calendar.getInstance();
			this.list = new ArrayList<CTRow>();
			this._context = context;
			CurrentPosition = -1;


			if (c.moveToFirst())
			{
				do {         
					//int idtask = 	c.getInt(c.getColumnIndex("_id"));
					//int parent =  c.getInt(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_PARENT));
					CTRow ctrow = new CTRow(c);
					list.add(ctrow);					

				} while(c.moveToNext());			
			}		

		}


		public long getItemId(int position) {
			return Long.valueOf(list.get(position).id);
		}

		public CTRow getItem(int position)
		{
			return list.get(position);
		}

		public int getCount()
		{
			return list.size();
		}


		OnClickListener myDateOnClickListener = new Button.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(ConsumedTime.this,date_time_picker.class);

				CTRow ctrow = list.get(Integer.valueOf((String) v.getTag()));

				i.putExtra("choose_time",  0);//0 or 1
				i.putExtra("button",  (String) v.getTag());
				i.putExtra("begin_of_the_day_or_end",  0);//0 or 1
				i.putExtra("mseconds",ctrow.date.getTime());
				startActivityForResult(i, REQUEST_DATE_TIME);	
			}
		};


		OnClickListener myCategoryOnClickListener = new Button.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(ConsumedTime.this,date_time_picker.class);

				CTRow ctrow = list.get(Integer.valueOf((String) v.getTag()));

				CurrentChosenPosition = Integer.valueOf((String) v.getTag());

				showDialog(DIALOG_CHOOSE_CATEGORY);
			}
		};

		//Declaration
		private class GenericTextWatcher implements TextWatcher{

			private int position;
			private int field;
			private LinearLayout rowView;
			private GenericTextWatcher(int _position, int _field, LinearLayout _rowView) {
				this.position = _position;
				this.field = _field;
				this.rowView = _rowView;
			}

			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

			public void afterTextChanged(Editable editable) {	       
				CTRow ctrow = list.get(position);


				if ((CurrentPosition != position) && CurrentPosition!=-1)
				{
					return;
				}

				//Log.d(tag, "Position: " + position);


				String stringvalue = editable.toString();
				if (field == 1)
				{

					if (!(""+ctrow.value).equals(stringvalue))
					{
						if (stringvalue.length()==0 || stringvalue.equals("-")){
							stringvalue = "0";
						}

					    ctrow.value = Integer.valueOf(stringvalue);
						ctrow.tosave = true;
						rowView.setBackgroundColor(Color.DKGRAY);

					}
				}
				else
				{

					if (ctrow.comment == null)
					{
						ctrow.comment = "";
					}

					if (!ctrow.comment.equals(stringvalue))
					{
						ctrow.comment = stringvalue;
						ctrow.tosave = true;
						rowView.setBackgroundColor(Color.DKGRAY);
					}
				}
				//CurrentPosition = -1;
			}
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{

			LinearLayout rowView = null;
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = (LinearLayout) inflater.inflate(R.layout.row_consumed_time, parent, false);
				//rowView = (LinearLayout) inflater.inflate(R.layout.row, parent, false);
			} else {
				rowView = (LinearLayout) convertView;
			}




			TextView tvDate = (TextView) rowView.findViewById(R.id.textView_ct_date);
			TextView tvCategory = (TextView) rowView.findViewById(R.id.textView_ct_category);
			EditText etValue = (EditText) rowView.findViewById(R.id.edittext_ct_value);
			//EditText etComment = (EditText) rowView.findViewById(R.id.edittext_ct_comment);				


			list.get(position).etValue = etValue;
			//list.get(position).etComment = etComment;


			TextView tvTask = (TextView) rowView.findViewById(R.id.textView_ct_task);

			tvDate.setText(sdf_ddMM.format(list.get(position).date));
			tvDate.setTag(""+position);
			tvDate.setOnClickListener(myDateOnClickListener);

			GenericTextWatcher gtw = new GenericTextWatcher(position,1,rowView);
			etValue.setText(""+list.get(position).value);
			etValue.setTag(""+position);
			/*etValue.setOnKeyListener(new EditText.OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					//CurrentPosition = Integer.valueOf((String)v.getTag());
					//Toast.makeText(getBaseContext(), "3-"+CurrentPosition, Toast.LENGTH_SHORT).show();
					//CTRow ctrow = list.get(Integer.valueOf((String)v.getTag()));
					//Toast.makeText(getBaseContext(), "3-"+(String)v.getTag(), Toast.LENGTH_SHORT).show();
					//String stringvalue = ((EditText) v).getText().toString();
					//Toast.makeText(getBaseContext(), "3-"+(String)v.getTag()+"-"+stringvalue, Toast.LENGTH_SHORT).show();
					/*ctrow.value = Float.parseFloat(stringvalue);
					ctrow.tosave = true;
					Toast.makeText(getBaseContext(), "1-"+stringvalue, Toast.LENGTH_SHORT).show();
					ll.notifyDataSetChanged();
					return false;
				}
			});*/
			etValue.addTextChangedListener(gtw);

			GenericTextWatcher gtw2 = new GenericTextWatcher(position,2,rowView);
			String comment = list.get(position).comment;
			
			/*etComment.setText(comment);			
			etComment.setTag(""+position);
			etComment.addTextChangedListener(gtw2);*/

			if (list.get(position).tosave)
			{
				rowView.setBackgroundColor(Color.LTGRAY);
			}
			else
			{
				rowView.setBackgroundColor(Color.WHITE);
			}


			String category = list.get(position).categoryname;


			if (category.length()>0)
			{
				int bgcolor = list.get(position).bgcolor;
				tvCategory.setPadding(5, 0, 5, 0);
				if (bgcolor != 0) {
					tvCategory.setBackgroundDrawable(CommonFunctions.GetDrawable(bgcolor));
				}
				else
				{
					tvCategory.setBackgroundDrawable(CommonFunctions.GetDrawable(Color.DKGRAY));			
				}
				tvCategory.setTextColor(Color.WHITE);
				tvCategory.setText(category);
			}
			else
			{
				//tvCategory.setBackgroundDrawable(CommonFunctions.GetDrawable(Color.DKGRAY));
				tvCategory.setBackgroundColor(Color.WHITE);
				tvCategory.setTextColor(Color.RED);
				tvCategory.setText("Pick a category");
			}	
			
			
			tvCategory.setTag(""+position);
			tvCategory.setOnClickListener(myCategoryOnClickListener);











			String taskname = "Task: "+list.get(position).taskname;
			if (list.get(position).taskname.length()>0)
			{
				tvTask.setText(taskname);
				tvTask.setVisibility(View.VISIBLE);
			}
			else
			{
				tvTask.setText("");
				tvTask.setVisibility(View.GONE);
			}


			//MyCheckBox.setTag(id);	

			return rowView;
		}





	}


}