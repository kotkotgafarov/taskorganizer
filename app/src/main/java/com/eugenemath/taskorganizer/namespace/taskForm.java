package com.eugenemath.taskorganizer.namespace;

import com.eugenemath.taskorganizer.namespace.R;
import com.eugenemath.taskorganizer.namespace.Task;
import com.eugenemath.taskorganizer.namespace.TaskOrganizerProvider;
import com.eugenemath.taskorganizer.namespace.ListOfCatalog.MySimpleCursorAdapterCatalogs;
import com.eugenemath.taskorganizer.namespace.filter_form_date.MyOnItemSelectedListener;
import com.eugenemath.taskorganizer.namespace.flexiblemenu.MySimpleArrayAdapter;

import android.app.Activity;
import android.media.ExifInterface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;

import java.util.Calendar;
import java.util.Date;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
//import android.app.TabActivity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.provider.ContactsContract;
import android.text.Html;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.view.MenuItem;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import android.view.KeyEvent;

public class taskForm extends Activity {


	private Cursor c;
	private Cursor c_status, c_category, c_history;
	public long taskid;
	private static SQLiteDatabase tasksDB;
	public Task OldTask;
	public Task CurrentTask;
	public String OldExecutor,CurrentExecutor,OldResponsible,CurrentResponsible;

	private static final String PREFS_NAME = "PrefsTaskOrganizer";
	SimpleDateFormat sdf_ddMM = new SimpleDateFormat("dd MMM yy"); 
	SimpleDateFormat sdf_ddMMhhmm = new SimpleDateFormat("dd MMM yyyy HH:mm"); 
	SimpleDateFormat sdf_hhmm;


	/*Button button_release_date; 	
	Button button_due_date; 
	Button button_date_from; 
	Button button_date_to; 
	Button button_date_from_fact; 
	Button button_date_to_fact; 
	Button buttondate; 	  */
	TextView textViewDate;
	Date date01011970 = new Date(0);
	
	int CurrentPage = 0;

	EditText EditTextName, editText_executor, editText_responsible, editText_description , editText_percent_of_completion, editText_processingtime, editText_processingtime_fact;

	static final private int MENU_SMS = Menu.FIRST;
	static final private int MENU_EMAIL = Menu.FIRST+1;	

	static final private int dialog_button_release_date = 1; 	
	static final private int dialog_button_due_date = 2; 
	static final private int dialog_button_date_from = 3; 
	static final private int dialog_button_date_to = 4; 
	static final private int dialog_button_date_from_fact = 5; 
	static final private int dialog_button_date_to_fact = 6; 
	static final private int dialog_buttondate =7 ; 	
	int current_dialog;
	private int REQUEST_CONTEXT_MENU = 1;
	private int REQUEST_NEW_CODE = 2;
	private int  RQS_PICK_CONTACT =3;
	private int REQUEST_DATE_TIME=4;
	private int REQUEST_PICK_PARENT = 5;
	private int REQUEST_PICK_IMAGE = 6;
	int RQS_PICK_CONTACT_TYPE = 0;//executor = 0, responsible = 1
	String[] alarms;
	ArrayAdapter<String> spinnerArrayAdapter_alarms;

	public class MyOnItemSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent,
				View view, int pos, long id) {

			Spinner spinnerstatus = (Spinner)findViewById(R.id.spinnerstatus); 
			Spinner spinnercategories = (Spinner)findViewById(R.id.spinnercategories); 
			if (parent.getId()==spinnerstatus.getId()){
				//Toast.makeText(parent.getContext(), "Selected category id =  "      id, Toast.LENGTH_LONG).show();
				CurrentTask.status = (int)id;
				Cursor cc = (Cursor)(spinnerstatus.getSelectedItem());
				CurrentTask.statusname = cc.getString(cc.getColumnIndex(taskOrganizerDatabaseHelper.Status_NAME));
			}
			else{
				//Toast.makeText(parent.getContext(), "Selected category id =  "      id, Toast.LENGTH_LONG).show();
				CurrentTask.category = (int)id;
				Cursor cc = (Cursor)(spinnercategories.getSelectedItem());
				CurrentTask.categoryname = cc.getString(cc.getColumnIndex(taskOrganizerDatabaseHelper.Categories_NAME));	
				CurrentTask.categorybgcolor = cc.getInt(cc.getColumnIndex("categorybgcolor"));
				
				
				int bgcolor = CurrentTask.categorybgcolor;
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
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		//sdf_ddMM = new SimpleDateFormat("dd MMM yy"); 


		setContentView(R.layout.taskform);
		/*TabHost tabs = (TabHost) this.findViewById(R.id.tabhost1);
		tabs.setup();
		TabSpec tspec1 = tabs.newTabSpec("tab1");
		tspec1.setIndicator(getString(R.string.from_code68));
		tspec1.setContent(R.id.taskformtab1layout);
		tabs.addTab(tspec1);
		TabSpec tspec2 = tabs.newTabSpec("tab2");
		tspec2.setIndicator(getString(R.string.from_code69));
		tspec2.setContent(R.id.taskformtab2layout);
		tabs.addTab(tspec2);
		TabSpec tspec3 = tabs.newTabSpec("tab3");
		tspec3.setIndicator(getString(R.string.from_code70));
		tspec3.setContent(R.id.taskformtab3layout);
		tabs.addTab(tspec3);*/

		taskOrganizerDatabaseHelper dbHelper = new taskOrganizerDatabaseHelper(this,  
				taskOrganizerDatabaseHelper.DATABASE_NAME, 
				null, 
				taskOrganizerDatabaseHelper.DATABASE_VERSION);
		tasksDB = dbHelper.getWritableDatabase();		
		if (!tasksDB.isOpen())
		{
			Toast.makeText(this, this.getString(R.string.from_code89),Toast.LENGTH_SHORT).show();
		}

		Intent i = getIntent();

		Bundle extras =  i.getExtras();


		taskid = extras.getLong("_id");		

		if (taskid ==0 || taskid==-1)
		{
			taskid = extras.getInt("_id");//for the case of notification

		}

		if (taskid>0)
		{
			c = query(taskid);
			c.moveToFirst();
			CurrentTask = new Task(c);
			OldTask = new Task(c);		
		}
		else
		{
			//a new task
			CurrentTask = new Task("");
			//CurrentTask.date = new Date(i.getExtras().getLong("date"));
			OldTask = new Task("");		

		}

		if (extras.containsKey("datefrom"))
		{
			CurrentTask.datefrom = new Date(extras.getLong("datefrom"));
			CurrentTask.dateto = new Date(extras.getLong("datefrom")+3600*1000);

			if (CurrentTask.datefrom.getHours()==23)
			{
				CurrentTask.dateto = new Date(extras.getLong("datefrom")+3600*1000-60);
			}

			CurrentTask.date = RoundDate(CurrentTask.datefrom,0);
		}


		if (extras.containsKey("alarm"))
		{
			Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
			r.play();
		}


		if (extras.getLong("_id") ==-1)
		{
			String parentname = extras.getString("parentname");
			int parentid = (int) extras.getLong("parentid");
			if (parentname.length()>0)
			{
				CurrentTask.parent = parentid;
				CurrentTask.parentname = parentname;
			}
		}

 
		c_status = query_statuses();
		c_category =  query_categories();

		c_history = query_history(taskid);
		ListView historyListView = (ListView)this.findViewById(R.id.historyListView);
		//SimpleCursorAdapter ll = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, c_history, new String[]{taskOrganizerDatabaseHelper.History_NAME}, new int[]{android.R.id.text1});
		SimpleCursorAdapter ll = new MySimpleCursorAdapterCatalogs(this, R.layout.history_list_item, c_history, new String[]{taskOrganizerDatabaseHelper.History_NAME}, new int[]{android.R.id.text1});
		
		historyListView.setAdapter(ll);



		Resources res = getResources();
		alarms = res.getStringArray(R.array.alarms);		
		spinnerArrayAdapter_alarms = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, alarms);
		Spinner spinner_alarm = (Spinner)this.findViewById(R.id.spinner_alarm);
		spinner_alarm.setAdapter(spinnerArrayAdapter_alarms);
		spinner_alarm.setSelection(OldTask.alarm);


		sdf_ddMM = new SimpleDateFormat(getString(R.string.sdf1)); 
		sdf_ddMMhhmm = new SimpleDateFormat(getString(R.string.sdf2));
		sdf_hhmm = new SimpleDateFormat(getString(R.string.sdf3));
		
		
		final Button button_main = (Button)findViewById(R.id.button_main);
		final Button button_additional = (Button)findViewById(R.id.button_additional);		
		final Button button_history = (Button)findViewById(R.id.button_history);
		
		
		button_main.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				CurrentPage = 0;
				ShowOrHidePages();
			}	    
		}); 		

		button_additional.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				CurrentPage = 1;
				ShowOrHidePages();
			}	    
		}); 		

		button_history.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				CurrentPage = 2;
				ShowOrHidePages();
			}	    
		}); 		

		
		
		FillFields();
		//ListView myListView = (ListView)findViewById(R.id.myListView);
		ShowOrHidePages();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		//Toast.makeText(getBaseContext(), "Destroy ", Toast.LENGTH_LONG).show();
		tasksDB.close();
		if (taskid>0)
		{
			//c.close();
		}
	}


	public void ChangeDateInButton(View v){

		Intent i = new Intent(taskForm.this,date_time_picker.class);


		Date edate = new Date(0);
		Date currentDate = new Date();

		LinearLayout ll_datefrom = (LinearLayout)findViewById(R.id.ll_datefrom); 
		LinearLayout ll_dateto = (LinearLayout)findViewById(R.id.ll_dateto); 
		LinearLayout ll_datefrom_fact = (LinearLayout)findViewById(R.id.ll_datefromfact); 
		LinearLayout ll_datetofact = (LinearLayout)findViewById(R.id.ll_datetofact); 
		LinearLayout ll_duedate = (LinearLayout)findViewById(R.id.ll_duedate); 
		LinearLayout ll_releasedate = (LinearLayout)findViewById(R.id.ll_releasedate); 
		ImageView ivdate   = (ImageView)findViewById(R.id.buttondate); 
		
		
		if (v.getId() ==ivdate.getId() || v.getId() ==textViewDate.getId()){
			i.putExtra("choose_time",  0);//0 or 1
			i.putExtra("button",  "buttondate");
			i.putExtra("begin_of_the_day_or_end",  0);//0 or 1
			//i.putExtra("mseconds",CurrentTask.date.getTime());
			if (edate.equals(CurrentTask.date))
			{
				i.putExtra("mseconds",currentDate.getTime());
			}
			else
			{
				i.putExtra("mseconds",CurrentTask.date.getTime());
			}
		}
		if (v.getId() ==ll_releasedate.getId() ){
			i.putExtra("choose_time",  1);//0 or 1
			i.putExtra("button",  "button_release_date");
			i.putExtra("begin_of_the_day_or_end",  0);//0 or 1
			//i.putExtra("mseconds",CurrentTask.releasedate.getTime());
			if (edate.equals(CurrentTask.releasedate))
			{
				i.putExtra("mseconds",currentDate.getTime());
			}
			else
			{
				i.putExtra("mseconds",CurrentTask.releasedate.getTime());
			}		
		}
		if (v.getId() ==ll_duedate.getId() ){
			i.putExtra("choose_time",  1);//0 or 1
			i.putExtra("button",  "button_due_date");
			i.putExtra("begin_of_the_day_or_end",  1);//0 or 1
			//i.putExtra("mseconds",CurrentTask.duedate.getTime());
			if (edate.equals(CurrentTask.duedate))
			{
				i.putExtra("mseconds",currentDate.getTime());
			}
			else
			{
				i.putExtra("mseconds",CurrentTask.duedate.getTime());
			}		
		}
		if (v.getId() ==ll_datefrom.getId() ){
			i.putExtra("choose_time",  1);//0 or 1
			i.putExtra("button",  "button_date_from");
			i.putExtra("begin_of_the_day_or_end",  0);//0 or 1
			//i.putExtra("mseconds",CurrentTask.datefrom.getTime());
			if (edate.equals(CurrentTask.datefrom))
			{
				i.putExtra("mseconds",currentDate.getTime());
			}
			else
			{
				i.putExtra("mseconds",CurrentTask.datefrom.getTime());
			}		
		}
		if (v.getId() ==ll_dateto.getId() ){
			i.putExtra("choose_time",  1);//0 or 1
			i.putExtra("button",  "button_date_to");
			i.putExtra("begin_of_the_day_or_end",  1);//0 or 1
			//i.putExtra("mseconds",CurrentTask.dateto.getTime());
			if (edate.equals(CurrentTask.dateto))
			{
				i.putExtra("mseconds",currentDate.getTime());
			}
			else
			{
				i.putExtra("mseconds",CurrentTask.dateto.getTime());
			}		
		}
		if (v.getId() ==ll_datefrom_fact.getId() ){
			i.putExtra("choose_time",  1);//0 or 1
			i.putExtra("button",  "button_date_from_fact");
			i.putExtra("begin_of_the_day_or_end",  0);//0 or 1
			//i.putExtra("mseconds",CurrentTask.datefromfact.getTime());
			if (edate.equals(CurrentTask.datefromfact))
			{
				i.putExtra("mseconds",currentDate.getTime());
			}
			else
			{
				i.putExtra("mseconds",CurrentTask.datefromfact.getTime());
			}		
		}
		if (v.getId() ==ll_datetofact.getId() ){
			i.putExtra("choose_time",  1);//0 or 1
			i.putExtra("button",  "button_date_to_fact");
			i.putExtra("begin_of_the_day_or_end",  1);//0 or 1
			//i.putExtra("mseconds",CurrentTask.datetofact.getTime());
			if (edate.equals(CurrentTask.datetofact))
			{
				i.putExtra("mseconds",currentDate.getTime());
			}
			else
			{
				i.putExtra("mseconds",CurrentTask.datetofact.getTime());
			}		
		}

		startActivityForResult(i, REQUEST_DATE_TIME);	

	}

	public void FixEditTextChanges(View v){

		if (v.getId() ==EditTextName.getId() ){CurrentTask.name = EditTextName.getText().toString();}
		if (v.getId() ==editText_description.getId() ){CurrentTask.description = editText_description.getText().toString();}
		if (v.getId() ==editText_percent_of_completion.getId() ){CurrentTask.percentofcompletion = new Integer(editText_percent_of_completion.getText().toString());}
		if (v.getId() ==editText_processingtime.getId() ){CurrentTask.processingtime =new Integer(editText_processingtime.getText().toString());}
		if (v.getId() ==editText_processingtime_fact.getId() ){CurrentTask.processingtimefact = new Integer(editText_processingtime_fact.getText().toString());}
	}

	public Date RoundDate(Date date0, int UpOrDown)
	{
		if (UpOrDown == 1){
			return new Date(date0.getYear(),date0.getMonth(),date0.getDate(),23,59,59);}
		else{
			return new Date(date0.getYear(),date0.getMonth(),date0.getDate(),0,0,0);}

	}


	public void ShowOrHidePages()
	{
		//linearLayout3.setVisibility(View.VISIBLE);
		LinearLayout linearLayout1 = (LinearLayout)this.findViewById(R.id.taskformtab1);
		LinearLayout linearLayout2 = (LinearLayout)this.findViewById(R.id.taskformtab2);
		LinearLayout linearLayout3 = (LinearLayout)findViewById(R.id.taskformtab3);

		
		
		final Button button_main = (Button)findViewById(R.id.button_main);
		final Button button_additional = (Button)findViewById(R.id.button_additional);		
		final Button button_history = (Button)findViewById(R.id.button_history);
		
		

		Resources res = getResources();
		
		Drawable dr_not_pressed = res.getDrawable(R.drawable.background_bottom_not_pressed2);
		Drawable dr_pressed = res.getDrawable(R.drawable.background_bottom_pressed2);
		
		button_main.setBackgroundDrawable(dr_not_pressed);
		button_additional.setBackgroundDrawable(dr_not_pressed);
		button_history.setBackgroundDrawable(dr_not_pressed);

		

		if (CurrentPage == 0)
		{
			linearLayout1.setVisibility(View.VISIBLE);
			linearLayout2.setVisibility(View.GONE);
			linearLayout3.setVisibility(View.GONE);
			button_main.setBackgroundDrawable(dr_pressed);
		}
		else if (CurrentPage == 1)
		{
			linearLayout1.setVisibility(View.GONE);
			linearLayout2.setVisibility(View.VISIBLE);
			linearLayout3.setVisibility(View.GONE);
			button_additional.setBackgroundDrawable(dr_pressed);
		}
		else if (CurrentPage == 2)
		{

			linearLayout1.setVisibility(View.GONE);
			linearLayout2.setVisibility(View.GONE);
			linearLayout3.setVisibility(View.VISIBLE);
			button_history.setBackgroundDrawable(dr_pressed);
		}

	

	}


	public void UpdateDatesInViews()
	{
		OnClickListener myDateOnClickListener = new Button.OnClickListener() {
			public void onClick(View v) {
				ChangeDateInButton(v);
			}};
			
			
		LinearLayout ll_datefrom = (LinearLayout)findViewById(R.id.ll_datefrom); 
		ll_datefrom.setOnClickListener(myDateOnClickListener);			
		TextView tv_datefrom_date = (TextView)this.findViewById(R.id.tv_datefrom_date); 
		TextView tv_datefrom_time = (TextView)this.findViewById(R.id.tv_datefrom_time); 	
		if (!CurrentTask.datefrom.equals(date01011970)){
			tv_datefrom_date.setText(sdf_ddMM.format(CurrentTask.datefrom));
			tv_datefrom_time.setText(sdf_hhmm.format(CurrentTask.datefrom));
		}
		else{
			tv_datefrom_date.setText("-");
			tv_datefrom_time.setText("-");
		}
		
		
		
		
		LinearLayout ll_dateto = (LinearLayout)findViewById(R.id.ll_dateto); 
		ll_dateto.setOnClickListener(myDateOnClickListener);			
		TextView tv_dateto_date = (TextView)this.findViewById(R.id.tv_dateto_date); 
		TextView tv_dateto_time = (TextView)this.findViewById(R.id.tv_dateto_time); 
		if (!CurrentTask.dateto.equals(date01011970)){
			tv_dateto_date.setText(sdf_ddMM.format(CurrentTask.dateto));
			tv_dateto_time.setText(sdf_hhmm.format(CurrentTask.dateto));
		}
		else{
			tv_dateto_date.setText("-");
			tv_dateto_time.setText("-");
		}

		
		
		LinearLayout ll_datefrom_fact = (LinearLayout)findViewById(R.id.ll_datefromfact); 
		ll_datefrom_fact.setOnClickListener(myDateOnClickListener);			
		TextView tv_datefromfact_date = (TextView)this.findViewById(R.id.tv_datefromfact_date); 
		TextView tv_datefromfact_time = (TextView)this.findViewById(R.id.tv_datefromfact_time); 	
		if (!CurrentTask.datefromfact.equals(date01011970)){
			tv_datefromfact_date.setText(sdf_ddMM.format(CurrentTask.datefromfact));
			tv_datefromfact_time.setText(sdf_hhmm.format(CurrentTask.datefromfact));
		}
		else{
			tv_datefromfact_date.setText("-");
			tv_datefromfact_time.setText("-");
		}
		
		
		
		
		LinearLayout ll_datetofact = (LinearLayout)findViewById(R.id.ll_datetofact); 
		ll_datetofact.setOnClickListener(myDateOnClickListener);			
		TextView tv_datetofact_date = (TextView)this.findViewById(R.id.tv_datetofact_date); 
		TextView tv_datetofact_time = (TextView)this.findViewById(R.id.tv_datetofact_time); 
		if (!CurrentTask.datetofact.equals(date01011970)){
			tv_datetofact_date.setText(sdf_ddMM.format(CurrentTask.datetofact));
			tv_datetofact_time.setText(sdf_hhmm.format(CurrentTask.datetofact));
		}
		else{
			tv_datetofact_date.setText("-");
			tv_datetofact_time.setText("-");
		}
	
		
		
		
		LinearLayout ll_duedate = (LinearLayout)findViewById(R.id.ll_duedate); 
		ll_duedate.setOnClickListener(myDateOnClickListener);			
		TextView tv_duedate_date = (TextView)this.findViewById(R.id.tv_duedate_date); 
		TextView tv_duedate_time = (TextView)this.findViewById(R.id.tv_duedate_time); 	
		if (!CurrentTask.duedate.equals(date01011970)){
			tv_duedate_date.setText(sdf_ddMM.format(CurrentTask.duedate));
			tv_duedate_time.setText(sdf_hhmm.format(CurrentTask.duedate));
		}
		else{
			tv_duedate_date.setText("-");
			tv_duedate_time.setText("-");
		}
		
		
		
		
		LinearLayout ll_releasedate = (LinearLayout)findViewById(R.id.ll_releasedate); 
		ll_releasedate.setOnClickListener(myDateOnClickListener);			
		TextView tv_releasedate_date = (TextView)this.findViewById(R.id.tv_releasedate_date); 
		TextView tv_releasedate_time = (TextView)this.findViewById(R.id.tv_releasedate_time); 
		if (!CurrentTask.releasedate.equals(date01011970)){
			tv_releasedate_date.setText(sdf_ddMM.format(CurrentTask.releasedate));
			tv_releasedate_time.setText(sdf_hhmm.format(CurrentTask.releasedate));
		}
		else{
			tv_releasedate_date.setText("-");
			tv_releasedate_time.setText("-");
		}	
		
	}
	
	public void ShowImage()
	{
		ImageView iv_image = (ImageView)findViewById(R.id.iv_image); 
	
        Uri selectedImageUri = Uri.parse(CurrentTask.image);

 		// OI FILE Manager
		String filemanagerstring = selectedImageUri.getPath();
		// MEDIA GALLERY
		String selectedImagePath = CommonFunctions.getPath(this, selectedImageUri);       
        
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		int targetW = metrics.widthPixels-50;
		int targetH = metrics.heightPixels-50;	
		//Bitmap bitmap = CommonFunctions.loadBitmap(selectedImagePath, int orientation,targetW, targetH)
		
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(selectedImagePath, bmOptions);
      

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.max(photoW/targetW, photoH/targetH);
      
        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;
      
        Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath, bmOptions);
        
        //rotation
        ExifInterface exif;
		try {
			exif = new ExifInterface(selectedImagePath);
			int orientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION, 1);
			Matrix matrix = new Matrix();
			if (orientation == 6) {
				matrix.postRotate(90);
			} else if (orientation == 3) {
				matrix.postRotate(180);
			} else if (orientation == 8) {
				matrix.postRotate(270);
			}
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
					bitmap.getHeight(), matrix, true);	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		iv_image.setImageBitmap(bitmap);
		iv_image.setVisibility(View.VISIBLE);
	}
	
	public void FillFields() {

		OnClickListener myDateOnClickListener = new Button.OnClickListener() {
			public void onClick(View v) {
				ChangeDateInButton(v);
			}};



			EditTextName = (EditText)findViewById(R.id.edit_text_name); 
			EditTextName.setText(CurrentTask.name);	
			//EditTextName.setOnKeyListener(myOnKeyListener);

			editText_description = (EditText)findViewById(R.id.editText_description); 
			editText_description.setText(CurrentTask.description);

			Button button_menu_parent = (Button)findViewById(R.id.button_menu_parent); 
			button_menu_parent.setText(CurrentTask.parentname);

			button_menu_parent.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					Intent i = new Intent(taskForm.this,flexiblemenu.class);
					i.putExtra("Regim",  "ParentClear");
					//i.putExtra("executorresponsible",  0);
					startActivityForResult(i, REQUEST_CONTEXT_MENU);			
				}});	

			//Button button_menu_parent = (Button)findViewById(R.id.button_menu_parent); 
			TextView tv_pick_picture = (TextView)findViewById(R.id.tv_pick_picture); 
			
			tv_pick_picture.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					 Intent intent = new Intent();
                     intent.setType("image/*");
                     intent.setAction(Intent.ACTION_GET_CONTENT);
                     startActivityForResult(Intent.createChooser(intent,
                             "Select Picture"), REQUEST_PICK_IMAGE);			
				}});	

			ImageView iv_image = (ImageView)findViewById(R.id.iv_image); 
			iv_image.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					Uri selectedImageUri = Uri.parse(CurrentTask.image);
					startActivity(new Intent(Intent.ACTION_VIEW, selectedImageUri)); 
				}});	
			

			//editText_description.setOnKeyListener(myOnKeyListener);

			editText_executor = (EditText)findViewById(R.id.editText_executor); 
			editText_responsible = (EditText)findViewById(R.id.editText_responsible); 

			CustomizeExecutorResponsible();
			OldExecutor = editText_executor.getText().toString();
			OldResponsible  = editText_responsible.getText().toString();

			if (CurrentTask.image.startsWith("content:"))
			{
				ShowImage();
			}

			SeekBar seekBar_percent_of_completion = (SeekBar)this.findViewById(R.id.seekBar_percent_of_completion); 
			
			seekBar_percent_of_completion.setProgress(CurrentTask.percentofcompletion);

			seekBar_percent_of_completion.setOnSeekBarChangeListener( new OnSeekBarChangeListener()
			{
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser)
				{
					CurrentTask.percentofcompletion = progress;
					TextView textview_percent_of_completion = (TextView)findViewById(R.id.textview_percent_of_completion); 
					textview_percent_of_completion.setText(""+CurrentTask.percentofcompletion);
				}

				public void onStartTrackingTouch(SeekBar seekBar)
				{
					// TODO Auto-generated method stub
				}

				public void onStopTrackingTouch(SeekBar seekBar)
				{
					// TODO Auto-generated method stub
				}
			});

			
			TextView textview_percent_of_completion = (TextView)this.findViewById(R.id.textview_percent_of_completion); 
			textview_percent_of_completion.setText(""+CurrentTask.percentofcompletion);
			///editText_percent_of_completion = (EditText)findViewById(R.id.editText_percent_of_completion); 
			//editText_percent_of_completion.setText(""+CurrentTask.percentofcompletion);	
			//editText_percent_of_completion.setOnKeyListener(myOnKeyListener);

			editText_processingtime = (EditText)findViewById(R.id.editText_processingtime); 
			editText_processingtime.setText(""+CurrentTask.processingtime);				
			//editText_processingtime.setOnKeyListener(myOnKeyListener);

			editText_processingtime_fact = (EditText)findViewById(R.id.editText_processingtime_fact); 
			editText_processingtime_fact.setText(""+CurrentTask.processingtimefact);	
			//editText_processingtime_fact.setOnKeyListener(myOnKeyListener);

			
			UpdateDatesInViews();

			Button btn_code = (Button)findViewById(R.id.buttoncode); 
			btn_code.setText(CurrentTask.code);

			if (CurrentTask.code.length() == 0){
				btn_code.setText(""+CurrentTask.idtask);
				if (CurrentTask.idtask==null)
				{
					btn_code.setText(getString(R.string.from_layout12));
				}
			}

			btn_code.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					Intent i = new Intent(taskForm.this,editname.class);
					i.putExtra("Name",  CurrentTask.code);
					i.putExtra("Title",  getString(R.string.from_code58));
					startActivityForResult(i, REQUEST_NEW_CODE); 			
				}});


			Button button_menu_executor = (Button)findViewById(R.id.button_menu_executor); 

			button_menu_executor.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					Intent i = new Intent(taskForm.this,flexiblemenu.class);
					i.putExtra("Regim",  "ContactClear");
					i.putExtra("executorresponsible",  0);
					startActivityForResult(i, REQUEST_CONTEXT_MENU);			
				}});	


			Button button_menu_responsible = (Button)findViewById(R.id.button_menu_responsible); 

			button_menu_responsible.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					Intent i = new Intent(taskForm.this,flexiblemenu.class);
					i.putExtra("Regim",  "ContactClear");
					i.putExtra("executorresponsible",  1);
					startActivityForResult(i, REQUEST_CONTEXT_MENU);			
				}});		



			ImageView ivdate = (ImageView)findViewById(R.id.buttondate); 	
			textViewDate = (TextView)this.findViewById(R.id.textViewDate); 

			textViewDate.setOnClickListener(myDateOnClickListener);
			ivdate.setOnClickListener(myDateOnClickListener);
			if (!CurrentTask.date.equals(date01011970)){
				textViewDate.setText(sdf_ddMM.format(CurrentTask.date));}	
			else{textViewDate.setText(getString(R.string.from_code7));}	





			Spinner spinnerstatus = (Spinner)findViewById(R.id.spinnerstatus); 
			SimpleCursorAdapter AdapterStatuses = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, c_status, new String[] { "name" },  new int[] { android.R.id.text1 });
			AdapterStatuses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinnerstatus.setAdapter(AdapterStatuses);		
			spinnerstatus.setOnItemSelectedListener(new MyOnItemSelectedListener());
			spinnerstatus.setSelection(CurrentTask.status);

			int pos = 0;
			if (c_status.moveToFirst()) {
				do { 
					if (c_status.getInt(c_status.getColumnIndex("_id")) == CurrentTask.status) {
						spinnerstatus.setSelection(pos);
						break;
					}
					pos++;
				} while(c_status.moveToNext());
			}


			Spinner spinnercategories = (Spinner)findViewById(R.id.spinnercategories); 
			SimpleCursorAdapter AdapterCategories = new SimpleCursorAdapter(this, R.layout.simple_spinner_item_with_triangle, c_category, new String[] { "name" },  new int[] { android.R.id.text1 });
			AdapterCategories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinnercategories.setAdapter(AdapterCategories);			
			spinnercategories.setOnItemSelectedListener(new MyOnItemSelectedListener());
			spinnercategories.setSelection(CurrentTask.category);

			pos = 0;
			if (c_category.moveToFirst()) {
				do { 
					if (c_category.getInt(c_category.getColumnIndex("_id")) == CurrentTask.category) {
						spinnercategories.setSelection(pos);
						break;
					}
					pos++;
				} while(c_category.moveToNext());
			}	

			int bgcolor = CurrentTask.categorybgcolor;
			if (bgcolor == 0) {bgcolor = Color.GRAY;}
			spinnercategories.setBackgroundDrawable(CommonFunctions.GetDrawable(bgcolor));

			/*
			  int textcolor = CurrentTask.categorytextcolor;
			if (textcolor == 0) {textcolor = Color.WHITE;}
			spinnercategories.set(textcolor);*/




			LinearLayout layoutsave = (LinearLayout)this.findViewById(R.id.layoutsave); 		
			layoutsave.setOnClickListener( new Button.OnClickListener() {
				public void onClick(View v) {

					String name = EditTextName.getText().toString();
					if (name.length()==0 && taskid==0) 
					{
						Toast.makeText(getBaseContext(), getString(R.string.from_code59), Toast.LENGTH_LONG).show();
						return;
					}			

					int ThereAreChanges = SaveChanges();  


					Intent intent = new Intent();
					intent.putExtra("Result", ThereAreChanges);
					setResult(RESULT_OK,intent);
					tasksDB.close();
					finish();      
				}});

			LinearLayout layoutcancel = (LinearLayout)this.findViewById(R.id.layoutcancel); 		
			layoutcancel.setOnClickListener( new Button.OnClickListener() {
				public void onClick(View v) {  
					Intent intent = new Intent();
					intent.putExtra("Result", 0);
					setResult(RESULT_CANCELED,intent);	
					tasksDB.close();
					finish();      
				}});

	}

	public int SaveChanges()
	{
		Date CurrentDate = new Date();
		SimpleDateFormat sdf_history = new SimpleDateFormat(getString(R.string.sdf2)); 
		String TextForHistory = ""+sdf_history.format(CurrentDate)+" [FIELD][OLD VALUE][NEW VALUE]";

		Boolean ThereAreChanges = false;

		CurrentTask.name = EditTextName.getText().toString();
		CurrentTask.description = editText_description.getText().toString();
		//CurrentTask.percentofcompletion = new Integer(editText_percent_of_completion.getText().toString());

		
		try {
			CurrentTask.processingtime = Integer
					.parseInt(editText_processingtime.getText().toString());
		} catch (NumberFormatException e) {
			CurrentTask.processingtime = 0;
		}

		try {
			CurrentTask.processingtimefact = Integer
					.parseInt(editText_processingtime_fact.getText().toString());
		} catch (NumberFormatException e) {
			CurrentTask.processingtimefact = 0;
		}
		
		//CurrentTask.processingtime =new Integer(editText_processingtime.getText().toString());
		//CurrentTask.processingtimefact = new Integer(editText_processingtime_fact.getText().toString());
		CurrentExecutor = editText_executor.getText().toString();
		CurrentResponsible = editText_responsible.getText().toString();

		if (!CurrentTask.executor.startsWith("content:"))
		{
			CurrentTask.executor = CurrentExecutor;
		}

		if (!CurrentTask.responsible.startsWith("content:"))
		{
			CurrentTask.responsible = CurrentResponsible;
		}	

		if (CurrentTask.processingtime.intValue() != OldTask.processingtime.intValue())
		{
			TextForHistory+=" [Proc.time]["+OldTask.processingtime.intValue()+"]["+CurrentTask.processingtime.intValue()+"] \n";
			ThereAreChanges = true;
		}

		if (CurrentTask.parent.intValue() != OldTask.parent.intValue())
		{
			TextForHistory+=" [Parent]["+OldTask.parent.intValue()+"]["+CurrentTask.parent.intValue()+"] \n";
			ThereAreChanges = true;
		}

		//if (CurrentTask.name != OldTask.name)
		if (!CurrentTask.name.equals(OldTask.name))
		{
			TextForHistory+=" [Name]["+OldTask.name+"]["+CurrentTask.name+"] \n";
			ThereAreChanges = true;
		}		

		if (CurrentTask.status.intValue() != OldTask.status.intValue()){
			TextForHistory+=" [Status]["+OldTask.statusname+"]["+CurrentTask.statusname+"] \n";		
			ThereAreChanges = true;
		}

		if (CurrentTask.processingtimefact.intValue() != OldTask.processingtimefact.intValue()){
			TextForHistory+=" [Proc.time fact]["+OldTask.processingtimefact.intValue()+"]["+CurrentTask.processingtimefact.intValue()+"] \n";	
			ThereAreChanges = true;
		}

		if (CurrentTask.priority.intValue() != OldTask.priority.intValue()){
			TextForHistory+=" [priority]["+OldTask.priority.intValue()+"]["+CurrentTask.priority.intValue()+"] \n";	
			ThereAreChanges = true;
		}


		//if ((CurrentTask.description.toString() != OldTask.description.toString()) && (CurrentTask.description.length()>0 || OldTask.description.length()>0)){
		if (!CurrentTask.description.equals(OldTask.description))
		{
			TextForHistory+=" [description]["+OldTask.description+"]["+CurrentTask.description+"] \n";		
			ThereAreChanges = true;
		}

		//if ((CurrentTask.responsible != OldTask.responsible) && (CurrentTask.responsible.length()>0 || OldTask.responsible.length()>0)){
		/*if (!CurrentTask.responsible.equals(OldTask.responsible)){
		TextForHistory+=" [responsible]["+OldTask.responsible+"]["+CurrentTask.responsible+"] \n";	
			ThereAreChanges = true;
		}*/

		if (!CurrentExecutor.equals(OldExecutor)){
			//if ((CurrentExecutor != OldExecutor) && (CurrentExecutor.length()>0 || OldExecutor.length()>0)){
			TextForHistory+=" [executor]["+OldExecutor+"]["+CurrentExecutor+"] \n";	
			ThereAreChanges = true;
		}
		
		if (!CurrentTask.image.equals(OldTask.image)){
			TextForHistory+=" [image]";	
			ThereAreChanges = true;
		}

		if (!CurrentResponsible.equals(OldResponsible)){
			TextForHistory+=" [responsible]["+OldResponsible+"]["+CurrentResponsible+"] \n";	
			ThereAreChanges = true;
		}		

		if (!CurrentTask.code.equals(OldTask.code)){
			//if ((CurrentTask.code.toString() != OldTask.code.toString()) && (CurrentTask.code.length()>0 || OldTask.code.length()>0)){
			TextForHistory+=" [code]["+OldTask.code+"]["+CurrentTask.code+"] \n";			
			ThereAreChanges = true;
		}


		if (CurrentTask.percentofcompletion.intValue() != OldTask.percentofcompletion.intValue()){
			TextForHistory+=" [% of completion]["+OldTask.percentofcompletion.intValue()+"]["+CurrentTask.percentofcompletion.intValue()+"] \n";
			ThereAreChanges = true;
		}

		if (CurrentTask.category != OldTask.category){
			TextForHistory+=" [Category]["+OldTask.categoryname+"]["+CurrentTask.categoryname+"] \n";		
			ThereAreChanges = true;
		}

		if (CurrentTask.date.getTime() != OldTask.date.getTime()){
			TextForHistory+=" [Date]["+sdf_ddMM.format(OldTask.date)+"]["+sdf_ddMM.format(CurrentTask.date)+"] \n";	
			ThereAreChanges = true;
		}

		if (CurrentTask.duedate.getTime() != OldTask.duedate.getTime()){
			TextForHistory+=" [Duedate]["+sdf_ddMMhhmm.format(OldTask.duedate)+"]["+sdf_ddMMhhmm.format(CurrentTask.duedate)+"] \n";
			ThereAreChanges = true;
		}

		if (CurrentTask.releasedate.getTime() != OldTask.releasedate.getTime()){
			TextForHistory+=" [Release date]["+sdf_ddMMhhmm.format(OldTask.releasedate)+"]["+sdf_ddMMhhmm.format(CurrentTask.releasedate)+"] \n";	
			ThereAreChanges = true;
		}


		if (CurrentTask.datefrom.getTime() != OldTask.datefrom.getTime()){
			TextForHistory+=" [date from]["+sdf_ddMMhhmm.format(OldTask.datefrom)+"]["+sdf_ddMMhhmm.format(CurrentTask.datefrom)+"] \n";
			ThereAreChanges = true;
		}

		if (CurrentTask.dateto.getTime() != OldTask.dateto.getTime()){
			TextForHistory+=" [date to]["+sdf_ddMMhhmm.format(OldTask.dateto)+"]["+sdf_ddMMhhmm.format(CurrentTask.dateto)+"] \n";	
			ThereAreChanges = true;
		}


		if ((CurrentTask.datefrom.getTime() != OldTask.datefrom.getTime()) || (CurrentTask.dateto.getTime() != OldTask.dateto.getTime()))
		{
			//check date
			if (RoundDate(CurrentTask.datefrom,0).equals(RoundDate(CurrentTask.dateto,0)))
			{
				if ((!CurrentTask.datefrom.equals(new Date(0)) && !RoundDate(CurrentTask.datefrom,0).equals(RoundDate(CurrentTask.date,0))))
				{
					CurrentTask.date = RoundDate(CurrentTask.datefrom,0);
					TextForHistory+=" [Date]["+sdf_ddMM.format(OldTask.date)+"]["+sdf_ddMM.format(CurrentTask.date)+"] \n";	
					ThereAreChanges = true;
					Toast.makeText(getBaseContext(), getString(R.string.from_code60)+" "+ sdf_ddMM.format(CurrentTask.date) +" "+getString(R.string.from_code61), Toast.LENGTH_LONG).show();
				}
			}
		}

		if (CurrentTask.date.getTime() != OldTask.date.getTime()){
			//if ((RoundDate(CurrentTask.datefrom,0) == RoundDate(CurrentTask.dateto,0)) && (RoundDate(OldTask.datefrom,0) == RoundDate(OldTask.dateto,0)))
			//{
			//check date
			if (RoundDate(CurrentTask.datefrom,0).equals(RoundDate(CurrentTask.dateto,0)))
			{
				if ((!CurrentTask.datefrom.equals(new Date(0)) && !RoundDate(CurrentTask.datefrom,0).equals(RoundDate(CurrentTask.date,0))))
				{
					CurrentTask.datefrom = new Date(CurrentTask.date.getTime()+(CurrentTask.datefrom.getTime()-RoundDate(CurrentTask.datefrom,0).getTime()));
					CurrentTask.dateto = new Date(CurrentTask.date.getTime()+(CurrentTask.dateto.getTime()-RoundDate(CurrentTask.dateto,0).getTime()));
					TextForHistory+=" [date from]["+sdf_ddMMhhmm.format(OldTask.datefrom)+"]["+sdf_ddMMhhmm.format(CurrentTask.datefrom)+"] \n";
					TextForHistory+=" [date to]["+sdf_ddMMhhmm.format(OldTask.dateto)+"]["+sdf_ddMMhhmm.format(CurrentTask.dateto)+"] \n";	

					Toast.makeText(getBaseContext(), getString(R.string.from_layout98), Toast.LENGTH_LONG).show();
				}
			}
			//}

		}





		if (CurrentTask.datetofact.getTime() != OldTask.datetofact.getTime()){
			TextForHistory+=" [date to fact]["+sdf_ddMMhhmm.format(OldTask.datetofact)+"]["+sdf_ddMMhhmm.format(CurrentTask.datetofact)+"] \n";	
			ThereAreChanges = true;
		}

		if (CurrentTask.datefromfact.getTime() != OldTask.datefromfact.getTime()){
			TextForHistory+=" [date from fact]["+sdf_ddMMhhmm.format(OldTask.datefromfact)+"]["+sdf_ddMMhhmm.format(CurrentTask.datefromfact)+"] \n";	
			ThereAreChanges = true;
		}

		//------------------------alarm-----------------------
		Spinner spinner_alarm = (Spinner)this.findViewById(R.id.spinner_alarm);
		int spinner_position = spinner_alarm.getSelectedItemPosition();
		if (spinner_position != OldTask.alarm)
		{
			TextForHistory+=" [alarm]["+OldTask.alarm+"]["+spinner_position+"] \n";	
			CurrentTask.alarm = spinner_position;
			ThereAreChanges = true;
		}
		//------------------------end of alarm----------------



		if (ThereAreChanges){

			int rowID=0;
			
			
			if (!tasksDB.isOpen())
			{
				Toast.makeText(this,"Exception. Database was closed.",Toast.LENGTH_SHORT).show();
				taskOrganizerDatabaseHelper dbHelper = new taskOrganizerDatabaseHelper(this,  
						taskOrganizerDatabaseHelper.DATABASE_NAME, 
						null, 
						taskOrganizerDatabaseHelper.DATABASE_VERSION);
				tasksDB = dbHelper.getWritableDatabase();		
			}
			
			
			ContentValues values_task = CurrentTask.ReturnContentValues();
			if (taskid==0)
			{
				taskid = tasksDB.insert(taskOrganizerDatabaseHelper.Tasks_TABLE, "item", values_task);	

				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
				String prefix = settings.getString("prefix", "");	
				if (prefix.length()!=0)
				{
					values_task.clear();
					values_task.put(taskOrganizerDatabaseHelper.KEY_CODE, prefix+"_"+taskid);
					String where = taskOrganizerDatabaseHelper.KEY_ID + "=" + taskid;
					rowID = tasksDB.update(taskOrganizerDatabaseHelper.Tasks_TABLE,values_task,where,null);			
				}			    
			}
			else
			{
				String where = taskOrganizerDatabaseHelper.KEY_ID + "=" + CurrentTask.idtask;
				rowID = tasksDB.update(taskOrganizerDatabaseHelper.Tasks_TABLE,values_task,where,null);				
			}


			sendBroadcast(new Intent(taskorganizerActivity.WIDGET_REFRESHED));

			//------------------------alarm-----------------------
			if (spinner_position != OldTask.alarm || CurrentTask.datefrom.getTime() != OldTask.datefrom.getTime())
			{
				TextForHistory+=SetAlarm(spinner_position,taskid);
			}
			//------------------------end of alarm----------------


			ContentValues values = new ContentValues();
			values.put(taskOrganizerDatabaseHelper.History_task_id, CurrentTask.idtask);
			values.put(taskOrganizerDatabaseHelper.History_NAME, TextForHistory);
			long rowID2 = tasksDB.insert(taskOrganizerDatabaseHelper.History_Table, "item", values);	


			if (CurrentTask.status==2 || CurrentTask.processingtimefact>0){
				CommonFunctions.UpdateConsumedTime(taskForm.this, CurrentTask);
			}


			return 1;
		}
		else
		{return 0;}

	}

	public String SetAlarm(int spinner_position, long taskid2)
	{
		Intent intent = new Intent(getBaseContext(), taskForm.class);
		intent.putExtra("_id", taskid2);
		intent.putExtra("alarm", 1);
		PendingIntent pendingIntent =  PendingIntent.getActivity(getBaseContext(), (int) taskid2, intent, 0);	
		AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);


		if (OldTask.alarm!=0)
		{
			alarmManager.cancel(pendingIntent);
		}

		if (CurrentTask.datefrom.equals(new Date(0)))
		{
			return "[no alarm]";
		}

		if (spinner_position==0)
		{
			return "[no alarm]";
		}


		long currentTime = (new Date()).getTime();
		long taskTime = CurrentTask.datefrom.getTime();

		Calendar cal = Calendar.getInstance();
		cal.setTime(CurrentTask.datefrom);
		//cal.add(Calendar.MINUTE, 1);

		int minutes = 0;

		if (spinner_position==1)
		{
			minutes = 1;
		}
		else if (spinner_position==2)
		{
			minutes = 5;
		}
		else if (spinner_position==3)
		{
			minutes = 10;
		}
		else if (spinner_position==4)
		{
			minutes = 15;
		}
		else if (spinner_position==5)
		{
			minutes = 30;
		}
		else if (spinner_position==6)
		{
			minutes = 60;
		}
		else if (spinner_position==7)
		{
			minutes = 120;
		}
		else if (spinner_position==8)
		{
			minutes = 720;
		}
		else if (spinner_position==9)
		{
			minutes = 1440;
		}		
		else if (spinner_position==10)
		{
			minutes = 2*24*60;
		}
		else if (spinner_position==11)
		{
			minutes = 7*24*60;
		}		

		cal.add(Calendar.MINUTE, -1*minutes);

		//cal.add(arg0, arg1)
		//cal.setTimeInMillis(System.currentTimeMillis());
		//cal.clear();
		//cal.set(2012,2,8,18,16);
		alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);

		return " [alarm set on "+sdf_ddMMhhmm.format(cal.getTime())+" id="+taskid2+"]\n";

	}


	public Cursor query(Long _id) {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String SelectQuery = "SELECT tasks."
				+ taskOrganizerDatabaseHelper.KEY_ID+" as _id,tasks."
				+ taskOrganizerDatabaseHelper.KEY_DATE+",tasks."
				+ taskOrganizerDatabaseHelper.KEY_ALARM+",tasks."
				+ taskOrganizerDatabaseHelper.KEY_IMAGE+",tasks."
				+ taskOrganizerDatabaseHelper.KEY_CODE+",tasks."
				+ taskOrganizerDatabaseHelper.KEY_PARENT+",tasks."
				+ taskOrganizerDatabaseHelper.KEY_NAME+",tasks."
				+ taskOrganizerDatabaseHelper.KEY_RESPONSIBLE+",tasks."
				+ taskOrganizerDatabaseHelper.KEY_EXECUTOR+",tasks."
				+ taskOrganizerDatabaseHelper.KEY_DESCRIPTION+",tasks."
				+ taskOrganizerDatabaseHelper.KEY_PROCESSINGTIME+",tasks."
				+ taskOrganizerDatabaseHelper.KEY_DUEDATE+",tasks."
				+ taskOrganizerDatabaseHelper.KEY_RELEASEDATE+",tasks."
				+ taskOrganizerDatabaseHelper.KEY_STATUS+",tasks."
				+ taskOrganizerDatabaseHelper.KEY_PROCESSINGTIMEFACT+",tasks."
				+ taskOrganizerDatabaseHelper.KEY_PRIORITY+",tasks."
				+ taskOrganizerDatabaseHelper.KEY_DATEFROM+",tasks."
				+ taskOrganizerDatabaseHelper.KEY_DATETO+",tasks."
				+ taskOrganizerDatabaseHelper.KEY_DATEFROMFACT+",tasks."
				+ taskOrganizerDatabaseHelper.KEY_DATETOFACT+",tasks."
				+ taskOrganizerDatabaseHelper.KEY_PERCENTOFCOMPLETION+",tasks."
				+ taskOrganizerDatabaseHelper.KEY_CATEGORY+" as category,tasks."
				+ taskOrganizerDatabaseHelper.KEY_STATUS+" as status,"
				+ "parenttable."+taskOrganizerDatabaseHelper.KEY_NAME+" as parentname,"
				+ taskOrganizerDatabaseHelper.Statuses_Table+"."+ taskOrganizerDatabaseHelper.Status_NAME+" as statusname,"
				+ taskOrganizerDatabaseHelper.Categories_Table+"."+ taskOrganizerDatabaseHelper.Categories_BG_COLOR+" as categorybgcolor,"
				+ taskOrganizerDatabaseHelper.Categories_Table+"."+ taskOrganizerDatabaseHelper.Categories_TEXT_COLOR+" as categorytextcolor,"
				+ taskOrganizerDatabaseHelper.Categories_Table+"."+ taskOrganizerDatabaseHelper.Categories_NAME+" as categoryname"
				+" FROM "+ taskOrganizerDatabaseHelper.Tasks_TABLE
				+" LEFT JOIN "+ taskOrganizerDatabaseHelper.Statuses_Table+" ON "+ taskOrganizerDatabaseHelper.Tasks_TABLE+"."+ taskOrganizerDatabaseHelper.KEY_STATUS+" = "+ taskOrganizerDatabaseHelper.Statuses_Table+"."+ taskOrganizerDatabaseHelper.Status_ID
				+" LEFT JOIN "+ taskOrganizerDatabaseHelper.Categories_Table+" ON "+ taskOrganizerDatabaseHelper.Tasks_TABLE+"."+ taskOrganizerDatabaseHelper.KEY_CATEGORY+" = "+ taskOrganizerDatabaseHelper.Categories_Table+"."+ taskOrganizerDatabaseHelper.Categories_ID
				+" LEFT JOIN "+ taskOrganizerDatabaseHelper.Tasks_TABLE+" as parenttable ON "+ taskOrganizerDatabaseHelper.Tasks_TABLE+"."+ taskOrganizerDatabaseHelper.KEY_PARENT+" = parenttable."+ taskOrganizerDatabaseHelper.KEY_ID;

		SelectQuery +=" where tasks."+taskOrganizerDatabaseHelper.KEY_ID+" = "+_id;	


		Cursor c = tasksDB.rawQuery(SelectQuery, null);
		return c;
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

	public Cursor query_history(long _id) {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String SelectQuery = "SELECT "
				+ taskOrganizerDatabaseHelper.History_ID+" as _id,"
				+ taskOrganizerDatabaseHelper.History_NAME+" as name"
				+" FROM "+ taskOrganizerDatabaseHelper.History_Table
				+" WHERE "+taskOrganizerDatabaseHelper.History_task_id+" = "+_id
				+ " ORDER BY "+taskOrganizerDatabaseHelper.History_ID+" DESC";

		Cursor _c = tasksDB.rawQuery(SelectQuery, null);
		return _c;
	}		



	public void CustomizeExecutorResponsible()
	{

		String executor = CurrentTask.executor;

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
			String contactname = CommonFunctions.GetContactNameFromString(taskForm.this,executor);
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

		CurrentExecutor = editText_executor.getText().toString();



		String responsible = CurrentTask.responsible;
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
			String contactname = CommonFunctions.GetContactNameFromString(taskForm.this,responsible);
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

		CurrentResponsible = editText_responsible.getText().toString();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_NEW_CODE && resultCode == Activity.RESULT_OK)
			if (data.getExtras().getInt("Result") == 1)
			{
				CurrentTask.code = data.getExtras().getString("Name");
				Button btn_code = (Button)findViewById(R.id.buttoncode); 
				btn_code.setText(CurrentTask.code);   			 
			}

		if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK)
			{
				Uri selectedImageUri = data.getData();

				
				CurrentTask.image = selectedImageUri.toString();
				ShowImage();
			}
		
		if (requestCode == REQUEST_DATE_TIME && resultCode == Activity.RESULT_OK)
		{	//if (data.getExtras().getInt("Result") == 1)
			//{

			String button= data.getExtras().getString("button");
			if (button.equals("button_date_from"))
			{
				CurrentTask.datefrom = new Date(data.getExtras().getLong("mseconds"));
			}

			if (button.equals("buttondate"))
			{
				CurrentTask.date = new Date(data.getExtras().getLong("mseconds"));
				textViewDate.setText(sdf_ddMM.format(CurrentTask.date));
			}

			if (button.equals("button_release_date"))
			{
				CurrentTask.releasedate = new Date(data.getExtras().getLong("mseconds"));
			}


			if (button.equals("button_due_date"))
			{
				CurrentTask.duedate = new Date(data.getExtras().getLong("mseconds"));
			}			


			if (button.equals("button_date_to"))
			{
				CurrentTask.dateto = new Date(data.getExtras().getLong("mseconds"));
			}

			if (button.equals("button_date_from_fact"))
			{
				CurrentTask.datefromfact = new Date(data.getExtras().getLong("mseconds"));
			}


			if (button.equals("button_date_to_fact"))
			{
				CurrentTask.datetofact = new Date(data.getExtras().getLong("mseconds"));
			}		  			
			UpdateDatesInViews();
		}


		if(requestCode == REQUEST_PICK_PARENT){
			if(resultCode == RESULT_OK){
				int tempid  = (int) data.getExtras().getInt("id");
				if (CurrentTask.idtask!= null){
					if (tempid == CurrentTask.idtask)
					{
						Toast.makeText(this, getString(R.string.from_code62), Toast.LENGTH_LONG).show();
						return;
					}
				}

				CurrentTask.parent = tempid;
				CurrentTask.parentname = data.getExtras().getString("name");
				Button button_menu_parent = (Button)findViewById(R.id.button_menu_parent); 
				button_menu_parent.setText(CurrentTask.parentname);
			}
		}



		if (requestCode == REQUEST_CONTEXT_MENU)
		{
			if (resultCode == Activity.RESULT_OK) {
				String Regim = data.getExtras().getString("Regim");
				if (Regim.equals("ParentClear"))
				{
					if (data.getExtras().getInt("Result") == 0)
					{   
						Intent i = new Intent(taskForm.this,task_selection.class);
						//i.putExtra("Regim",  "ParentClear");
						startActivityForResult(i, REQUEST_PICK_PARENT);
					}
					else
					{
						Button button_menu_parent = (Button)findViewById(R.id.button_menu_parent); 
						CurrentTask.parentname = "";
						CurrentTask.parent = -1;
						button_menu_parent.setText(CurrentTask.parentname);
					}
				}
				else{
					if (data.getExtras().getInt("Result") == 0)
					{   				

						//contactUri --> content://com.android.contacts/data/1557
						//Intent intent = new Intent(Intent.ACTION_PICK, People.CONTENT_URI);
						//Intent intent = new Intent(Intent.ACTION_PICK, People.CONTENT_URI);
						Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
						//Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_TYPE);
						startActivityForResult(intent, RQS_PICK_CONTACT);	
						if (data.getExtras().getInt("executorresponsible") == 0){RQS_PICK_CONTACT_TYPE = 0;}
						else{RQS_PICK_CONTACT_TYPE = 1;}
					}
					else
					{
						if (data.getExtras().getInt("executorresponsible") == 0){
							editText_executor.setText("");		
							CurrentTask.executor = "";
						}
						else
						{
							editText_responsible.setText("");		
							CurrentTask.responsible = "";  					
						}
						CustomizeExecutorResponsible();
					}
				}
			}	
		}

		if(requestCode == RQS_PICK_CONTACT){
			if(resultCode == RESULT_OK){
				Uri contactData = data.getData();

				if (RQS_PICK_CONTACT_TYPE ==0)
				{
					CurrentTask.executor = contactData.toString();
				}
				else
				{
					CurrentTask.responsible = contactData.toString();
				}
				CustomizeExecutorResponsible();
			}
		}



	} 		


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuItem myMenuItem;

		myMenuItem = menu.add(0, MENU_SMS, Menu.NONE, R.string.menu_sms);
		myMenuItem.setIcon(R.drawable.ic_menu_send);
		myMenuItem = menu.add(0, MENU_EMAIL, Menu.NONE, R.string.menu_email);
		myMenuItem.setIcon(R.drawable.ic_menu_send);


		return true;
	}  

	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);


		CurrentExecutor = editText_executor.getText().toString();
		CurrentResponsible = editText_responsible.getText().toString();

		if (!CurrentTask.executor.startsWith("content:"))
		{
			CurrentTask.executor = CurrentExecutor;
		}

		if (!CurrentTask.responsible.startsWith("content:"))
		{
			CurrentTask.responsible = CurrentResponsible;
		}			

		switch (item.getItemId()) {
		case (MENU_SMS): {		

			if (CurrentResponsible.length()==0 && CurrentExecutor.length()==0)
			{
				Toast.makeText(getBaseContext(), getString(R.string.from_code63), Toast.LENGTH_LONG).show();
				return true;
			}

			SMS mySMS;
			if (CurrentResponsible.length()>0 && CurrentExecutor.length()==0)
			{
				mySMS = new SMS(this,CurrentTask,true,null);//to responsible
			}
			else
			{
				mySMS = new SMS(this,CurrentTask,false,null);//to executor
			}
			if (!mySMS.ErrorByCreating)
			{
				mySMS.SendSMS(this);
				c_history.requery();
			}

			return true; 
		}

		case (MENU_EMAIL): {		

			if (CurrentResponsible.length()==0 && CurrentExecutor.length()==0)
			{
				Toast.makeText(getBaseContext(), getString(R.string.from_code63), Toast.LENGTH_LONG).show();
				return true;
			}

			SMS mySMS;
			if (CurrentResponsible.length()>0 && CurrentExecutor.length()==0)
			{
				String email = CommonFunctions.FindEmailOfContact(getBaseContext(),CurrentTask.responsible);
				if (email.length()!=0) 
				{
					SendEmail(CurrentTask,email,true);
				}
				else
				{
					Toast.makeText(getBaseContext(), getString(R.string.from_code64), Toast.LENGTH_LONG).show();
				}
			}
			else
			{
				String email = CommonFunctions.FindEmailOfContact(getBaseContext(),CurrentTask.executor);
				if (email.length()!=0) 
				{
					SendEmail(CurrentTask,email,false);
				}
				else
				{
					Toast.makeText(getBaseContext(), getString(R.string.from_code64), Toast.LENGTH_LONG).show();
				}
			}

			c_history.requery();



			return true; 
		}	        

		} 
		return false;
	}

	public void SendEmail(Task CurrentTask, String email,boolean status)
	{
		MyMail  mymail = new MyMail();
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		mymail._host = settings.getString("smtp", "smtp.gmail.com");
		mymail._user = settings.getString("user", "");
		mymail._pass = settings.getString("pass", "");	
		mymail._send_to = email;
		mymail._auth = true;
		String from = settings.getString("from", mymail._user+"@gmail.com");


		if (from.startsWith("@"))
		{
			Toast.makeText(this, getString(R.string.from_code75), Toast.LENGTH_LONG).show();
			return;
		}
		mymail._from = settings.getString("from", mymail._user+"@gmail.com");

		mymail.FillSubjectByTask(CurrentTask, status);
		try {
			if (mymail.send())
			{
				Toast.makeText(this, getString(R.string.from_code5)+" "+email+" "+mymail._subject, Toast.LENGTH_LONG).show();
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast.makeText(this, getString(R.string.from_code6), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}	
	}

	
	
	public class MySimpleCursorAdapterCatalogs extends SimpleCursorAdapter {


		public MySimpleCursorAdapterCatalogs(Context context, int layout, Cursor cursor, String[] from, int[] to) {
			super(context,layout, cursor,from,to);
		}

		
		@Override
		public void bindView(View rowView, Context context, Cursor cursor) {
			TextView text_view_history = (TextView) rowView.findViewById(R.id.text_view_history);
			LinearLayout ll_history = (LinearLayout) rowView.findViewById(R.id.ll_history);
			
			String history = cursor.getString(cursor.getColumnIndex(taskOrganizerDatabaseHelper.History_NAME));
			
			int position = cursor.getPosition();
			
			
			if ((position % 2) == 0)
			{
				ll_history.setBackgroundColor(Color.parseColor("#ffffff"));
			}
			else
			{
				ll_history.setBackgroundColor(Color.parseColor("#efefef"));
			}
			
			text_view_history.setText(history);
			
			
			//styledText = Html.fromHtml(getString(R.string.page4));
			
			/*int pos = history.indexOf("[");

			if (pos ==-1)
			{
				text_view_history.setText(history);
			}
			else{
				text_view_history.setText("");
				text_view_history.append(history);
				Spannable sText = (Spannable) text_view_history.getText();
				sText.setSpan(new BackgroundColorSpan(Color.parseColor("#c4c4c4")), 0, pos, 0);
			}*/
			
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			final LayoutInflater inflater = LayoutInflater.from(context);
			final View v = inflater.inflate(R.layout.history_list_item, null);

			return v;
		}
	}		
	
	
}