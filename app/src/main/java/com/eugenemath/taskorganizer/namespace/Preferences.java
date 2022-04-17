package com.eugenemath.taskorganizer.namespace;




import com.eugenemath.taskorganizer.namespace.R;
import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView;
import android.widget.Toast;


public class Preferences extends Activity implements RainbowPickerDialog.OnColorChangedListener{


	private SQLiteDatabase tasksDB;	

	
	private static final String PREFS_NAME = "PrefsTaskOrganizer";
	private Long id_default_filter = (long) -1;
	private String name_of_filter = "";
	private boolean send_sms_automatically = true;	
	private boolean toodoledo_every_60_minutes = false;	
	private boolean  first_send_email = true;
	private int day_off_color;

	
	static final private int MENU_SAVE = Menu.FIRST;
	static final private int MENU_RETURN = Menu.FIRST+1;
	static final int FILTER_CHOOSE_SAVED = 8;

	EditText editText_imap,editText_fonsize,editText_smtp,editText_user,editText_pass,editText_frequency,editText_from,editText_prefix,editText_lastemailid,editText_toodledo_pass,editText_toodledo_email;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		/*MenuItem myMenuItem;

		
		myMenuItem = menu.add(0, MENU_SAVE, Menu.NONE, R.string.menu_save);
		myMenuItem.setIcon(android.R.drawable.ic_menu_save);	
		myMenuItem = menu.add(0, MENU_RETURN, Menu.NONE, R.string.menu_return);
		myMenuItem.setIcon(android.R.drawable.ic_menu_revert);*/

		return true;
	}
 
	public void SavePreferences()
	{
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		
		CheckBox checkBox_sendsmsautomatically = (CheckBox)findViewById(R.id.checkBox_sendsmsautomatically); 
		editor.putBoolean("send_sms_automatically", checkBox_sendsmsautomatically.isChecked());
		CheckBox checkBox_firstsendemail = (CheckBox)findViewById(R.id.checkBox_firstsendemail); 
		editor.putBoolean("first_send_email", checkBox_firstsendemail.isChecked());
		editor.putLong("id_default_filter", id_default_filter);
			
		
		editor.putString("imap", editText_imap.getText().toString());
		editor.putString("smtp", editText_smtp.getText().toString());
		editor.putString("user", editText_user.getText().toString());
		editor.putString("pass", editText_pass.getText().toString());
		editor.putString("from", editText_from.getText().toString());
		
		editor.putString("prefix", editText_prefix.getText().toString());
		
		
		editor.putString("fontsize", editText_fonsize.getText().toString());
		editor.putString("lastemailid", editText_lastemailid.getText().toString());

		
		editor.putString("frequency", editText_frequency.getText().toString());
		//editor.putInt("day_off_color", day_off_color);
		
		CheckBox checkBox_toodoledo_every_60_minutes = (CheckBox)findViewById(R.id.checkBox_toodoledo_every_60_minutes); 
		editor.putBoolean("toodoledo_every_60_minutes", checkBox_toodoledo_every_60_minutes.isChecked());
		
		editor.putString("toodoledo_email", editText_toodledo_email.getText().toString());
		editor.putString("toodoledo_pass", editText_toodledo_pass.getText().toString());	
		
		// Commit the edits!
		editor.commit();		

		Intent intent = new Intent();
		setResult(RESULT_OK,intent);	
	}
	
	
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case (MENU_SAVE): {

			SavePreferences();
			Intent intent = new Intent();
			setResult(RESULT_OK,intent);	
			finish();				
			
			return true; 
		}
		case (MENU_RETURN): {
			finish();
		}	  
		} 
		return false;
	}


	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		
		setContentView(R.layout.preferences);
		
		taskOrganizerDatabaseHelper dbHelper = new taskOrganizerDatabaseHelper(this,  
				taskOrganizerDatabaseHelper.DATABASE_NAME, 
				null, 
				taskOrganizerDatabaseHelper.DATABASE_VERSION);
		tasksDB = dbHelper.getWritableDatabase();		
		

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

		Button button_choose_filter = (Button)findViewById(R.id.button_choose_filter);
		TextView textView_default_filter = (TextView)findViewById(R.id.textView_default_filter);  

		editText_imap = (EditText)findViewById(R.id.editText_imap);
		editText_fonsize = (EditText)findViewById(R.id.editText_fonsize);
		editText_smtp = (EditText)findViewById(R.id.editText_smtp);
		editText_user = (EditText)findViewById(R.id.editText_user);
		editText_pass = (EditText)findViewById(R.id.editText_pass);
		editText_from = (EditText)findViewById(R.id.editText_from);
		editText_frequency = (EditText)findViewById(R.id.editText_frequency);
		
		editText_prefix = (EditText)findViewById(R.id.editText_prefix);
		editText_lastemailid = (EditText)findViewById(R.id.editText_lastemailid);		
		
		editText_prefix.setText(settings.getString("prefix", ""));
		editText_lastemailid.setText(settings.getString("lastemailid", "0"));
		
		
		editText_imap.setText(settings.getString("imap", "imap.gmail.com"));
		editText_smtp.setText(settings.getString("smtp", "smtp.gmail.com"));
		editText_user.setText(settings.getString("user", ""));
		editText_fonsize.setText(settings.getString("fontsize", "0"));
		
		
		editText_pass.setText(settings.getString("pass", ""));
		editText_from.setText(settings.getString("from", settings.getString("user", "")+"@gmail.com"));
		editText_frequency.setText(""+settings.getString("frequency", "600"));
		
		editText_toodledo_email = (EditText)findViewById(R.id.editText_toodledo_email);
		editText_toodledo_pass = (EditText)findViewById(R.id.editText_toodledo_pass);
		editText_toodledo_email.setText(settings.getString("toodoledo_email", ""));
		editText_toodledo_pass.setText(settings.getString("toodoledo_pass", ""));
	
		id_default_filter = settings.getLong("id_default_filter", -1);

		if (id_default_filter!=-1)
		{
			name_of_filter = FilterName(id_default_filter);
		}
		
		
		// day_off_color = settings.getInt("day_off_color", Color.parseColor("#1C1C1C"));
		//TextView tv_pick_color = (TextView)this.findViewById(R.id.textView_color_day_off_pick);	
		//tv_pick_color.setBackgroundColor(day_off_color);
		
		/*tv_pick_color.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
	        	//new ColorPickerDialog(Preferences.this, Preferences.this, "day_off_color", day_off_color , day_off_color).show();	 
	        	new RainbowPickerDialog(Preferences.this,Preferences.this,"").show();
	        }
	    });*/ 
		
		ImageView iv1 = (ImageView)this.findViewById(R.id.iv1);
		LinearLayout.LayoutParams params;

		
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int width = metrics.widthPixels;	
		iv1.setBackgroundColor(Color.parseColor("#a9a9a9"));
		params =  new LinearLayout.LayoutParams(width, 1);
		iv1.setLayoutParams(params);
		
		textView_default_filter.setText("Default filter = "+name_of_filter);
		
		send_sms_automatically = settings.getBoolean("send_sms_automatically", true);
		
		CheckBox checkBox_sendsmsautomatically = (CheckBox)findViewById(R.id.checkBox_sendsmsautomatically); 
		checkBox_sendsmsautomatically.setChecked(send_sms_automatically);
		
		
		
		toodoledo_every_60_minutes = settings.getBoolean("toodoledo_every_60_minutes", false);
		
		CheckBox checkBox_toodoledo_every_60_minutes = (CheckBox)findViewById(R.id.checkBox_toodoledo_every_60_minutes); 
		checkBox_toodoledo_every_60_minutes.setChecked(toodoledo_every_60_minutes);
		
		
		
		CheckBox checkBox_firstsendemail = (CheckBox)findViewById(R.id.checkBox_firstsendemail); 
		first_send_email = settings.getBoolean("first_send_email", true);
		checkBox_firstsendemail.setChecked(first_send_email);
		
		
		button_choose_filter.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
				Intent i = new Intent(getBaseContext(), ListOfCatalog.class);
				i.putExtra("Table", 3);
				startActivityForResult(i,FILTER_CHOOSE_SAVED);	        	
	        }
	    });
		
		Button button_clear_filter = (Button)findViewById(R.id.button_clear_filter);
		button_clear_filter.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
				name_of_filter = "";
				id_default_filter = (long)-1;
				TextView textView_default_filter = (TextView)findViewById(R.id.textView_default_filter); 
				textView_default_filter.setText(getString(R.string.from_code50)+name_of_filter);       	
	        }
	    });	
		
		
		Button button_check_connection = (Button)findViewById(R.id.button_check_connection);
		button_check_connection.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
				MyMail mymail = new MyMail();

				mymail._host = editText_imap.getText().toString();
				mymail._user = editText_user.getText().toString();
				mymail._pass = editText_pass.getText().toString();
				mymail.act =  getBaseContext();
				mymail.istest = true;
				
				try {
					boolean success = mymail.receive();
					
					if (success){
						Toast.makeText(getBaseContext(), getString(R.string.from_code51), Toast.LENGTH_SHORT).show();
					}
					else
					{
						Toast.makeText(getBaseContext(), getString(R.string.from_code52), Toast.LENGTH_SHORT).show();
					}
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Toast.makeText(getBaseContext(), getString(R.string.from_code52), Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
	        }
	    });
		
		Button button_sync_toodledo = (Button)findViewById(R.id.button_sync_toodledo);
		button_sync_toodledo.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {

	        	CommonFunctions.SyncToodledo(Preferences.this,  editText_toodledo_email.getText().toString(),editText_toodledo_pass.getText().toString());
				/*
				
				try {
					boolean success = mymail.receive();
					
					if (success){
						Toast.makeText(getBaseContext(), getString(R.string.from_code51), Toast.LENGTH_SHORT).show();
					}
					else
					{
						Toast.makeText(getBaseContext(), getString(R.string.from_code52), Toast.LENGTH_SHORT).show();
					}
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Toast.makeText(getBaseContext(), getString(R.string.from_code52), Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}*/
	        }
	    });
		
		
		LinearLayout layoutsave = (LinearLayout)this.findViewById(R.id.layoutsave); 
		LinearLayout layoutcancel = (LinearLayout)this.findViewById(R.id.layoutcancel); 
		
		
		layoutsave.setOnClickListener( new Button.OnClickListener() {
			public void onClick(View v) {

				SavePreferences();
				Intent intent = new Intent();
				setResult(RESULT_OK,intent);	
				finish();	
			}});

		
		layoutcancel.setOnClickListener( new Button.OnClickListener() {
			public void onClick(View v) {  
				finish();	
    
			}});
		
	}
	
	public String FilterName(long _id)
	{
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String SelectQuery = "SELECT "
				+ taskOrganizerDatabaseHelper.Filter_ID+" as _id,"
				+ taskOrganizerDatabaseHelper.Filter_NAME+" as _name"
		        +" FROM "+ taskOrganizerDatabaseHelper.Filters_Table;
		SelectQuery +=" where "+taskOrganizerDatabaseHelper.Filters_Table+"."+taskOrganizerDatabaseHelper.Filter_ID+" = "+_id;	
		
		
		Cursor c = tasksDB.rawQuery(SelectQuery, null);
		if (c.moveToFirst()){
			return c.getString(c.getColumnIndex("_name"));
		}
		else { return (""+_id);}
	}
	
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == FILTER_CHOOSE_SAVED && resultCode == Activity.RESULT_OK)	
		{
			name_of_filter = data.getExtras().getString("name_of_filter");
			id_default_filter = data.getExtras().getLong("idfilter");
			TextView textView_default_filter = (TextView)findViewById(R.id.textView_default_filter); 
			textView_default_filter.setText("Default filter = "+name_of_filter);
		}		
		else if (requestCode == FILTER_CHOOSE_SAVED)
		{
			//name_of_filter = "";
			//id_default_filter = (long)-1;
			//TextView textView_default_filter = (TextView)findViewById(R.id.textView_default_filter); 
			//textView_default_filter.setText("Default filter = "+name_of_filter);
		}
    		
    }

	
    public void colorChanged(String key, int color) {
    	/*day_off_color = color;
		TextView tv_pick_color = (TextView)this.findViewById(R.id.textView_color_day_off_pick);	
		tv_pick_color.setBackgroundColor(day_off_color);*/
		
		
	} 		
	
	
	
}