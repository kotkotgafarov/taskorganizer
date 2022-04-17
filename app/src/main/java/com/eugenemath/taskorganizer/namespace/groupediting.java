package com.eugenemath.taskorganizer.namespace;



import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.eugenemath.taskorganizer.namespace.R;
import com.eugenemath.taskorganizer.namespace.taskForm.MyOnItemSelectedListener;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Color;
import android.graphics.Paint;
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

public class groupediting extends Activity {

	private String choosenid;
	private static SQLiteDatabase tasksDB;
	SimpleDateFormat sdf_ddMM = new SimpleDateFormat("dd MMM yy"); 
	SimpleDateFormat sdf_ddMMhhmm = new SimpleDateFormat("dd MMM yyyy HH:mm"); 
	SimpleDateFormat sdf_hhmm;
	private Cursor c_status, c_category;


	public int status,category;
	
	public String responsible = "",executor = "";
	
	public Date date = new Date();
	public Date duedate = new Date();
	public Date releasedate = new Date();	

	EditText  editText_executor, editText_responsible , editText_percent_of_completion, editText_processingtime, editText_processingtime_fact;
    TextView textViewDate;


	Button buttonreleasedate; 	
	Button buttonduedate; 
	Button buttondate; 	  
	private int   REQUEST_CONTEXT_MENU = 1;
	private int  RQS_PICK_CONTACT =3;
	private int REQUEST_DATE_TIME=4;
	int RQS_PICK_CONTACT_TYPE = 0;//executor = 0, responsible = 1	
	Date date01011970 = new Date(0);	


	static final private int dialog_button_release_date = 1; 	
	static final private int dialog_button_due_date = 2; 
	static final private int dialog_buttondate =7 ; 
	int current_dialog;


	public class MyOnItemSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent,
				View view, int pos, long id) {

			Spinner spinnerstatus = (Spinner)findViewById(R.id.spinnerstatus); 
			Spinner spinnercategories = (Spinner)findViewById(R.id.spinnercategories); 
			if (parent.getId()==spinnerstatus.getId()){
				status = (int)id;
				CheckBox checkBox_status = (CheckBox)findViewById(R.id.checkBox_status);
				//checkBox_status.setChecked(true);
			}
			else{
				category = (int)id;   
				CheckBox checkBox_category = (CheckBox)findViewById(R.id.checkBox_category);
				//checkBox_category.setChecked(true);
				
				
				Cursor cc = (Cursor)(spinnercategories.getSelectedItem());
				
				int bgcolor = cc.getInt(cc.getColumnIndex("categorybgcolor"));
				if (bgcolor == 0) {bgcolor = Color.GRAY;}
				spinnercategories.setBackgroundDrawable(CommonFunctions.GetDrawable(bgcolor));
				
			}
		}
		public void onNothingSelected(AdapterView parent) {
			// Do nothing.
		}
	}		



	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Inflate your view
		setContentView(R.layout.groupediting);


		Intent i = getIntent();
		choosenid = i.getExtras().getString("choosenid");

		taskOrganizerDatabaseHelper dbHelper = new taskOrganizerDatabaseHelper(this,  
				taskOrganizerDatabaseHelper.DATABASE_NAME, 
				null, 
				taskOrganizerDatabaseHelper.DATABASE_VERSION);
		tasksDB = dbHelper.getWritableDatabase();	

		c_status = query_statuses();
		c_category =  query_categories();	


		Button myButtonSave = (Button)findViewById(R.id.buttonsave);
		Button myButtonCancel = (Button)findViewById(R.id.buttoncancel);




		
		
		LinearLayout layoutsave = (LinearLayout)this.findViewById(R.id.layoutsave); 		
		layoutsave.setOnClickListener( new Button.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				setResult(RESULT_OK,intent);
				SaveChanges();
				finish();					
			}});
		
		
		LinearLayout layoutcancel = (LinearLayout)this.findViewById(R.id.layoutcancel); 		
		layoutcancel.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				setResult(RESULT_CANCELED,intent);
				finish();					
			}});
	
		
		
		
		sdf_ddMM = new SimpleDateFormat(getString(R.string.sdf1)); 
		sdf_ddMMhhmm = new SimpleDateFormat(getString(R.string.sdf2));
		sdf_hhmm = new SimpleDateFormat(getString(R.string.sdf3));
		
		FillFields();
		
	}

	public void ChangeDateInButton(View v){

		Intent i = new Intent(groupediting.this,date_time_picker.class);


		Date edate = new Date(0);
		Date currentDate = new Date();


		LinearLayout ll_duedate = (LinearLayout)findViewById(R.id.ll_duedate); 
		LinearLayout ll_releasedate = (LinearLayout)findViewById(R.id.ll_releasedate); 
		ImageView ivdate   = (ImageView)findViewById(R.id.buttondate); 
		
		
		if (v.getId() ==ivdate.getId() || v.getId() ==textViewDate.getId()){
			i.putExtra("choose_time",  0);//0 or 1
			i.putExtra("button",  "buttondate");
			i.putExtra("begin_of_the_day_or_end",  0);//0 or 1
			//i.putExtra("mseconds",CurrentTask.date.getTime());
			if (edate.equals(date))
			{
				i.putExtra("mseconds",currentDate.getTime());
			}
			else
			{
				i.putExtra("mseconds",date.getTime());
			}
		}
		if (v.getId() ==ll_releasedate.getId() ){
			i.putExtra("choose_time",  1);//0 or 1
			i.putExtra("button",  "button_release_date");
			i.putExtra("begin_of_the_day_or_end",  0);//0 or 1
			//i.putExtra("mseconds",CurrentTask.releasedate.getTime());
			if (edate.equals(releasedate))
			{
				i.putExtra("mseconds",currentDate.getTime());
			}
			else
			{
				i.putExtra("mseconds",releasedate.getTime());
			}		
		}
		if (v.getId() ==ll_duedate.getId() ){
			i.putExtra("choose_time",  1);//0 or 1
			i.putExtra("button",  "button_due_date");
			i.putExtra("begin_of_the_day_or_end",  1);//0 or 1
			//i.putExtra("mseconds",CurrentTask.duedate.getTime());
			if (edate.equals(duedate))
			{
				i.putExtra("mseconds",currentDate.getTime());
			}
			else
			{
				i.putExtra("mseconds",duedate.getTime());
			}		
		}


		startActivityForResult(i, REQUEST_DATE_TIME);	

	}

	public void UpdateDatesInViews()
	{
		OnClickListener myDateOnClickListener = new Button.OnClickListener() {
			public void onClick(View v) {
				ChangeDateInButton(v);
			}};
			
			
		
		
		
		LinearLayout ll_duedate = (LinearLayout)findViewById(R.id.ll_duedate); 
		ll_duedate.setOnClickListener(myDateOnClickListener);			
		TextView tv_duedate_date = (TextView)this.findViewById(R.id.tv_duedate_date); 
		TextView tv_duedate_time = (TextView)this.findViewById(R.id.tv_duedate_time); 	
		if (!duedate.equals(date01011970)){
			tv_duedate_date.setText(sdf_ddMM.format(duedate));
			tv_duedate_time.setText(sdf_hhmm.format(duedate));
		}
		else{
			tv_duedate_date.setText("-");
			tv_duedate_time.setText("-");
		}
		
		
		
		
		LinearLayout ll_releasedate = (LinearLayout)findViewById(R.id.ll_releasedate); 
		ll_releasedate.setOnClickListener(myDateOnClickListener);			
		TextView tv_releasedate_date = (TextView)this.findViewById(R.id.tv_releasedate_date); 
		TextView tv_releasedate_time = (TextView)this.findViewById(R.id.tv_releasedate_time); 
		if (!releasedate.equals(date01011970)){
			tv_releasedate_date.setText(sdf_ddMM.format(releasedate));
			tv_releasedate_time.setText(sdf_hhmm.format(releasedate));
		}
		else{
			tv_releasedate_date.setText("-");
			tv_releasedate_time.setText("-");
		}	
		
	}
	
	
	
	
	
	public void FillFields() {

		OnClickListener myDateOnClickListener = new Button.OnClickListener() {
			public void onClick(View v) {
				ChangeDateInButton(v);
			}};
			
			Button button_menu_executor = (Button)findViewById(R.id.button_menu_executor); 
			
			button_menu_executor.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					Intent i = new Intent(groupediting.this,flexiblemenu.class);
					i.putExtra("Regim",  "ContactClear");
					i.putExtra("executorresponsible",  0);
			        startActivityForResult(i, REQUEST_CONTEXT_MENU);			
				}});	
			
			
			Button button_menu_responsible = (Button)findViewById(R.id.button_menu_responsible); 
			
			button_menu_responsible.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					Intent i = new Intent(groupediting.this,flexiblemenu.class);
					i.putExtra("Regim",  "ContactClear");
					i.putExtra("executorresponsible",  1);
			        startActivityForResult(i, REQUEST_CONTEXT_MENU);			
				}});		

			
			editText_executor = (EditText)findViewById(R.id.editText_executor); 
			editText_responsible = (EditText)findViewById(R.id.editText_responsible); 
			editText_percent_of_completion = (EditText)findViewById(R.id.editText_percent_of_completion); 
			editText_processingtime = (EditText)findViewById(R.id.editText_processingtime); 
			editText_processingtime_fact = (EditText)findViewById(R.id.editText_processingtime_fact); 

			
		
			UpdateDatesInViews();
			
			
			ImageView ivdate = (ImageView)findViewById(R.id.buttondate); 	
			textViewDate = (TextView)this.findViewById(R.id.textViewDate); 

			textViewDate.setOnClickListener(myDateOnClickListener);
			ivdate.setOnClickListener(myDateOnClickListener);
			if (!date.equals(date01011970)){
				textViewDate.setText(sdf_ddMM.format(date));}	
			else{textViewDate.setText(getString(R.string.from_code7));}	
			

			
			Spinner spinnerstatus = (Spinner)findViewById(R.id.spinnerstatus); 
			SimpleCursorAdapter AdapterStatuses = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, c_status, new String[] { "name" },  new int[] { android.R.id.text1 });
			AdapterStatuses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinnerstatus.setAdapter(AdapterStatuses);		
			//spinnerstatus.setOnItemSelectedListener(new MyOnItemSelectedListener());
			spinnerstatus.setSelection(0);
			spinnerstatus.setOnItemSelectedListener(new MyOnItemSelectedListener());
			
			
			Spinner spinnercategories = (Spinner)findViewById(R.id.spinnercategories); 
			//SimpleCursorAdapter AdapterCategories = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, c_category, new String[] { "name" },  new int[] { android.R.id.text1 });
			SimpleCursorAdapter AdapterCategories = new SimpleCursorAdapter(this, R.layout.simple_spinner_item_with_triangle, c_category, new String[] { "name" },  new int[] { android.R.id.text1 });
			AdapterCategories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinnercategories.setAdapter(AdapterCategories);			
			//spinnercategories.setOnItemSelectedListener(new MyOnItemSelectedListener());
			spinnercategories.setSelection(0);
			spinnercategories.setOnItemSelectedListener(new MyOnItemSelectedListener());
			
			
			
	}
	


	public void SaveChanges()
	{
		List<String> listofchoosenid = new ArrayList<String>();
		
		//listofchoosenid.add(""+id);
		
		String chosenid0 = choosenid;
		
		int pos = chosenid0.indexOf(",");
		while(pos !=-1)
		{
			listofchoosenid.add(chosenid0.substring(0, pos));
			chosenid0 = chosenid0.substring(pos+1);	
			pos = chosenid0.indexOf(",");
		}
		listofchoosenid.add(chosenid0);
		
		boolean issomethingchecked = false;

		
		Date CurrentDate = new Date();
		SimpleDateFormat sdf_history = new SimpleDateFormat(getString(R.string.sdf2)); 		
		String TextForHistory = ""+sdf_history.format(CurrentDate)+" Group editing ";
		
		ContentValues values = new ContentValues(); 
		String where = taskOrganizerDatabaseHelper.KEY_ID + " IN (" + choosenid+")"; 
		
		CheckBox checkBox_date = (CheckBox)findViewById(R.id.checkBox_date);
		if (checkBox_date.isChecked())
		{
			values.put(taskOrganizerDatabaseHelper.KEY_DATE, date.getTime());
			TextForHistory+="[date="+sdf_ddMM.format(date)+"]";
			issomethingchecked = true;
		}

		CheckBox checkBox_duedate = (CheckBox)findViewById(R.id.checkBox_duedate);
		if (checkBox_duedate.isChecked())
		{
			values.put(taskOrganizerDatabaseHelper.KEY_DUEDATE, duedate.getTime());
			TextForHistory+="[due date="+sdf_ddMM.format(duedate)+"]";
			issomethingchecked = true;
		}

		CheckBox checkBox_releasedate = (CheckBox)findViewById(R.id.checkBox_releasedate);
		if (checkBox_releasedate.isChecked())
		{
			values.put(taskOrganizerDatabaseHelper.KEY_RELEASEDATE, releasedate.getTime());
			TextForHistory+="[release date="+sdf_ddMM.format(releasedate)+"]";
			issomethingchecked = true;
		}	
		
		CheckBox checkBox_status = (CheckBox)findViewById(R.id.checkBox_status);
		if (checkBox_status.isChecked())
		{
			values.put(taskOrganizerDatabaseHelper.KEY_STATUS, status);
			TextForHistory+="[id status="+status+"]";
			issomethingchecked = true;
		}	
		
		CheckBox checkBox_category = (CheckBox)findViewById(R.id.checkBox_category);
		if (checkBox_category.isChecked())
		{
			values.put(taskOrganizerDatabaseHelper.KEY_CATEGORY, category);
			TextForHistory+="[id category="+category+"]";
			issomethingchecked = true;
		}			
		
		
		CheckBox checkBox_responsible = (CheckBox)findViewById(R.id.checkBox_responsible);
		if (checkBox_responsible.isChecked())
		{
			if (responsible.length()== 0)
			{
				responsible = ((EditText)findViewById(R.id.editText_responsible)).getText().toString();
			}
			values.put(taskOrganizerDatabaseHelper.KEY_RESPONSIBLE, responsible);
			TextForHistory+="[responsible="+((EditText)findViewById(R.id.editText_responsible)).getText().toString()+"]";
			issomethingchecked = true;
		}			
		
		CheckBox checkBox_executor = (CheckBox)findViewById(R.id.checkBox_executor);
		if (checkBox_executor.isChecked())
		{
			if (executor.length()== 0)
			{
				executor = ((EditText)findViewById(R.id.editText_executor)).getText().toString();
			}			
			values.put(taskOrganizerDatabaseHelper.KEY_EXECUTOR, executor);
			TextForHistory+="[executor="+((EditText)findViewById(R.id.editText_executor)).getText().toString()+"]";
			issomethingchecked = true;
		}			
		
		CheckBox checkBox_percentofcompletion = (CheckBox)findViewById(R.id.checkBox_percentofcompletion);
		if (checkBox_percentofcompletion.isChecked())
		{
			String text0 = ((EditText)findViewById(R.id.editText_percent_of_completion)).getText().toString();
			if (text0.length()==0){
				text0 = "0";
			}	
			int value = Integer.valueOf(text0);
			values.put(taskOrganizerDatabaseHelper.KEY_PERCENTOFCOMPLETION, value);
			TextForHistory+="[% of compl.="+value+"]";
			issomethingchecked = true;
		}			
		

		
		CheckBox checkBox_processingtime = (CheckBox)findViewById(R.id.checkBox_processingtime);
		if (checkBox_processingtime.isChecked())
		{
			String text0 = ((EditText)findViewById(R.id.editText_processingtime)).getText().toString();
			if (text0.length()==0){
				text0 = "0";
			}	
			int value = Integer.valueOf(text0);		
			values.put(taskOrganizerDatabaseHelper.KEY_PROCESSINGTIME, value);
			TextForHistory+="[processing time="+value+"]";
			issomethingchecked = true;
		}			
		
		CheckBox checkBox_processingtimefact = (CheckBox)findViewById(R.id.checkBox_processingtime_fact);
		if (checkBox_processingtimefact.isChecked())
		{
			String text0 = ((EditText)findViewById(R.id.editText_processingtimefact)).getText().toString();
			if (text0.length()==0){
				text0 = "0";
			}	
			int value = Integer.valueOf(text0);			
			values.put(taskOrganizerDatabaseHelper.KEY_PROCESSINGTIMEFACT, value);
			TextForHistory+="[processing time fact="+value+"]";
			issomethingchecked = true;
		}					
		

		if (!issomethingchecked)
		{
			return;
		}
			
		values.put(taskOrganizerDatabaseHelper.KEY_DATEMODIFIED, (new Date()).getTime());
		
		long rowID = tasksDB.update(taskOrganizerDatabaseHelper.Tasks_TABLE,values,where,null);
			
		
		Iterator it=listofchoosenid.iterator();
		while(it.hasNext())
		{
			values = new ContentValues();
			values.put(taskOrganizerDatabaseHelper.History_task_id, Long.valueOf((String)it.next()));
			values.put(taskOrganizerDatabaseHelper.History_NAME, TextForHistory);
			rowID = tasksDB.insert(taskOrganizerDatabaseHelper.History_Table, "item", values);				
		}
	
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
				+ taskOrganizerDatabaseHelper.Categories_NAME+" as name,"
				+ taskOrganizerDatabaseHelper.Categories_BG_COLOR+" as categorybgcolor,"
				+ taskOrganizerDatabaseHelper.Categories_TEXT_COLOR+" as categorytextcolor"
				+" FROM "+ taskOrganizerDatabaseHelper.Categories_Table;
		
		Cursor _c = tasksDB.rawQuery(SelectQuery, null);
		return _c;
	}	


	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			int _year = year-1900;
			switch (current_dialog){
			case (dialog_buttondate):{
				date = new Date(_year, monthOfYear, dayOfMonth);
				buttondate.setText(sdf_ddMM.format(date));	
				break;}
			case (dialog_button_release_date):
			{releasedate = new Date(_year, monthOfYear, dayOfMonth);
			buttonreleasedate.setText(sdf_ddMM.format(releasedate)); 
			break;}
			case (dialog_button_due_date):
			{duedate = new Date(_year, monthOfYear, dayOfMonth);
			buttonduedate.setText(sdf_ddMM.format(duedate));
			break;}	
			}

		}
	};	

	@Override
	protected Dialog onCreateDialog(int id) {
		current_dialog = id;
		switch (id){
		case (dialog_buttondate):
			return new DatePickerDialog(this, mDateSetListener, 1900+date.getYear(), date.getMonth(), date.getDate());
		case (dialog_button_release_date):
			return new DatePickerDialog(this,  mDateSetListener, 1900+releasedate.getYear(), releasedate.getMonth(), releasedate.getDate());	    
		case (dialog_button_due_date):
			return new DatePickerDialog(this,  mDateSetListener, 1900+duedate.getYear(), duedate.getMonth(), duedate.getDate());	    
		}

		return null;
	}		


	public void CustomizeExecutorResponsible()
	{
		

		
		if (executor.length()==0)
		{
			executor = editText_executor.getText().toString();
		}
	
		
		if (executor.startsWith("content:"))
		{
			//Uri contactData = Uri.parse(executor);
			//Cursor cursor =  managedQuery(contactData, null, null, null, null);
			//cursor.moveToFirst();	
			//String contactname = cursor.getString(cursor.getColumnIndexOrThrow(People.NAME));
			String contactname = CommonFunctions.GetContactNameFromString(groupediting.this,executor);
			editText_executor.setText(contactname);
			editText_executor.setEnabled(false);
		    editText_executor.setFocusable(false);
		    editText_executor.setClickable(false);			
		}		
		else
		{
			editText_executor.setText(executor);
		    editText_executor.setEnabled(true);		
		    editText_executor.setFocusableInTouchMode(true);
		    editText_executor.setClickable(true);
		}
		
		//CurrentExecutor = editText_executor.getText().toString();
		
		
		

		if (responsible.length()==0)
		{
			responsible = editText_responsible.getText().toString();
		}
			
		
		
		if (responsible.startsWith("content:"))
		{
			//Uri contactData = Uri.parse(responsible);
			//Cursor cursor =  managedQuery(contactData, null, null, null, null);
			//cursor.moveToFirst();	
			//String contactname = cursor.getString(cursor.getColumnIndexOrThrow(People.NAME));	
			String contactname = CommonFunctions.GetContactNameFromString(groupediting.this,responsible);
			editText_responsible.setText(contactname);
			editText_responsible.setEnabled(false);
			editText_responsible.setFocusable(false);
			editText_responsible.setClickable(false);			
		}		
		else
		{
			editText_responsible.setText(responsible);
			editText_responsible.setEnabled(true);		
			editText_responsible.setFocusableInTouchMode(true);
			editText_responsible.setClickable(true);
		}		
		
		//CurrentResponsible = editText_responsible.getText().toString();
	}
	
	
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);


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
    					editText_executor.setText("");		
    					executor = "";
    				}
    				else
    				{
    					editText_responsible.setText("");		
    					responsible = "";  					
    				}
    				CustomizeExecutorResponsible();
    			}
    	}	
    	
    	if(requestCode == RQS_PICK_CONTACT){
    		if(resultCode == RESULT_OK){
    			Uri contactData = data.getData();
    			
    			if (RQS_PICK_CONTACT_TYPE ==0)
    			{
    				executor = contactData.toString();
    				CheckBox checkBox_executor = (CheckBox)findViewById(R.id.checkBox_executor);
    				checkBox_executor.setChecked(true);
    			}
    			else
    			{
    				responsible = contactData.toString();
    				CheckBox checkBox_responsible = (CheckBox)findViewById(R.id.checkBox_responsible);
    				checkBox_responsible.setChecked(true);
    			}
    			
    			CustomizeExecutorResponsible();
    		}
    	}
    	
		if (requestCode == REQUEST_DATE_TIME && resultCode == Activity.RESULT_OK)
		{	
			String button= data.getExtras().getString("button");

			if (button.equals("buttondate"))
			{
				date = new Date(data.getExtras().getLong("mseconds"));
				textViewDate.setText(sdf_ddMM.format(date));
				CheckBox checkBox_date = (CheckBox)findViewById(R.id.checkBox_date);
				checkBox_date.setChecked(true);
			}

			if (button.equals("button_release_date"))
			{
				releasedate = new Date(data.getExtras().getLong("mseconds"));
				CheckBox checkBox_releasedate = (CheckBox)findViewById(R.id.checkBox_releasedate);
				checkBox_releasedate.setChecked(true);
			}


			if (button.equals("button_due_date"))
			{
				duedate = new Date(data.getExtras().getLong("mseconds"));
				CheckBox checkBox_duedate = (CheckBox)findViewById(R.id.checkBox_duedate);
				checkBox_duedate.setChecked(true);
			}			

			UpdateDatesInViews();
		}

    		
    } 	

}