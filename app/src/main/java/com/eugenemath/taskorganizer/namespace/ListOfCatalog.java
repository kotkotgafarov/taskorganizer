package com.eugenemath.taskorganizer.namespace;




import java.util.Date;

import com.eugenemath.taskorganizer.namespace.R;
import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
import android.util.DisplayMetrics;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Toast;


public class ListOfCatalog extends Activity implements RainbowPickerDialog.OnColorChangedListener {

	public int CurrentTable;
	public String CurrentTableName;
	public boolean OpenedToSelect;
	ListView tasksListView;  
	CursorAdapter ll;
	private Cursor c;
	private String KEY_NAME;
	private String KEY_ID;
	private SQLiteDatabase tasksDB;	
	private myDBHelper dbHelper;
	private int CurrentChoosenId;
	private int CurrentPosition;
	//ContextMenu MyContextMenu;
	private int REQUEST_CONTEXT_MENU = 1;
	private int REQUEST_NEW_NAME = 2;
	
	static final private int MENU_EDIT = Menu.FIRST;
	static final private int MENU_DELETE = Menu.FIRST+1;
	static final private int MENU_RETURN = Menu.FIRST+2;
	
	int[][] array_of_colors;
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuItem myMenuItem;

		/*myMenuItem = menu.add(0, MENU_EDIT, Menu.NONE, R.string.menu_add_catalog);
		myMenuItem.setIcon(android.R.drawable.ic_menu_add);
		myMenuItem = menu.add(0, MENU_DELETE, Menu.NONE, R.string.menu_delete_catalog);
		myMenuItem.setIcon(android.R.drawable.ic_menu_delete);*/
		myMenuItem = menu.add(0, MENU_RETURN, Menu.NONE, R.string.menu_return);
		myMenuItem.setIcon(android.R.drawable.ic_menu_revert);

		return true;
	}
 

	private void DeleteItem(int _index) {
 
		    //Toast.makeText(getApplicationContext(), "Index = "+_index, Toast.LENGTH_SHORT).show();
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
		    ll.notifyDataSetChanged();			
		  }
	
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		//int index = tasksListView.getSelectedItemPosition();
		int index = CurrentChoosenId;
		switch (item.getItemId()) {
		case (MENU_EDIT): {
			return true; 
		}
		case (MENU_DELETE): {
			DeleteItem(index);
			return true;
			/*Intent i = new Intent(this, Preferences.class);
	          startActivityForResult(i, SHOW_PREFERENCES);
	          return true;*/
		}
		case (MENU_RETURN): {
			finish();
		}	  
		} 
		return false;
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

	public Cursor queryCatalogs(String[] fields, 
			String selection, 
			String[] selectionArgs, 
			String sort) {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(taskOrganizerDatabaseHelper.Categories_Table);

		Cursor c = qb.query(tasksDB, 
				fields, 
				selection, selectionArgs, 
				null, null, 
				null);

		array_of_colors = new int[c.getCount()+100][3];
		
		return c;
		
	}
	
	public Cursor queryFilters(String[] fields, 
			String selection, 
			String[] selectionArgs, 
			String sort) {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(taskOrganizerDatabaseHelper.Filters_Table);

		Cursor c = qb.query(tasksDB, 
				fields, 
				selection, selectionArgs, 
				null, null, 
				null);

		return c;
	}	

	public Cursor queryStatuses(String[] fields, 
			String selection, 
			String[] selectionArgs, 
			String sort) {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(taskOrganizerDatabaseHelper.Statuses_Table);

		Cursor c = qb.query(tasksDB, 
				fields, 
				selection, selectionArgs, 
				null, null, 
				null);

		return c;
	}	  

	public void UpdateItem(String NewName)
	{
	    ContentValues _initialValues = new ContentValues();    

	    _initialValues.put(KEY_NAME, NewName);		
	    String where = KEY_ID + "=" + CurrentChoosenId;
	    long rowID = tasksDB.update(CurrentTableName,_initialValues,where,null);
	    c.requery();
	    ll.notifyDataSetChanged();       	
	}
	
	public void AddNewItem()
	{
		final EditText myEditText = (EditText)findViewById(R.id.itemname);
		
	    ContentValues _initialValues = new ContentValues();    

	    _initialValues.put(KEY_NAME, myEditText.getText().toString());
	    if (CurrentTable==3)
	    {
	    	_initialValues.put(taskOrganizerDatabaseHelper.Filter_XML,"<filter ></filter>");
	    }
	    
	    long rowID = tasksDB.insert(CurrentTableName, "item", _initialValues);
        
	    //Toast.makeText(getApplicationContext(), "added", Toast.LENGTH_SHORT).show();
	    // Return a URI to the newly inserted row on success.

	    c.requery();
	    ll.notifyDataSetChanged();
	    myEditText.setText("");
	    
	    /*if (rowID > 0) {
		      Uri uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
		      return uri;
		    }	    
	    throw new SQLException("Failed to insert row into " + _uri);*/
	}

	
	public class MySimpleCursorAdapterCatalogs extends SimpleCursorAdapter {


		public MySimpleCursorAdapterCatalogs(Context context, int layout, Cursor cursor, String[] from, int[] to) {
			super(context,layout, cursor,from,to);
		}


		public OnClickListener MyOnClickListenerColor = new OnClickListener() {
	        public void onClick(View v) {
	        	
	        	String tag = (String) v.getTag();
	        	
	        	int numberofcolor = 2;
	        	
	        	
	        	if (tag.startsWith("BG"))
	        	{
	        		numberofcolor = 1;
	        		tag = tag.replace("BG", "");
	        	}
	        		
	        	
	        	int position = Integer.parseInt(tag);
	        	 
	        	//new ColorPickerDialog(ListOfCatalog.this, ListOfCatalog.this, (String) v.getTag(),array_of_colors[position][numberofcolor], 0).show();	
	        	new RainbowPickerDialog(ListOfCatalog.this,ListOfCatalog.this,(String) v.getTag()).show();
	        }
	    };
		
		@Override
		public void bindView(View rowView, Context context, Cursor cursor) {
			ImageButton button_bg_color = (ImageButton) rowView.findViewById(R.id.button_bg_color);
			//Button button_text_color = (Button) rowView.findViewById(R.id.button_text_color);
			TextView tvname = (TextView) rowView.findViewById(R.id.text_view_catalogs_name);
		
			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			int width = metrics.widthPixels;
			
			
			//tvname.setWidth(width-160);
			//button_bg_color.setWidth(80);
			//button_text_color.setWidth(80);
			  
			String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
			int id = cursor.getInt(cursor.getColumnIndex("_id"));
			int bgcolor = cursor.getInt(cursor.getColumnIndex(taskOrganizerDatabaseHelper.Categories_BG_COLOR));	
			//int text_color = cursor.getInt(cursor.getColumnIndex(taskOrganizerDatabaseHelper.Categories_TEXT_COLOR));
			
		    /*if (bg_color==0)
			{
				bg_color = Color.BLACK;
			}
			
			if (text_color==0)
			{
				text_color = Color.WHITE;
			}
			
			tvname.setTextColor(text_color);
			rowView.setBackgroundColor(bg_color);*/
			
			if (bgcolor == 0) {bgcolor = Color.GRAY;}
			button_bg_color.setBackgroundDrawable(CommonFunctions.GetDrawable(bgcolor));
			
			
			int position = cursor.getPosition();
			
			//button_text_color.setTag(""+position);
			button_bg_color.setTag("BG"+position);
			
			array_of_colors[position][0] = id;
			array_of_colors[position][1] = bgcolor;
			//array_of_colors[position][2] = text_color;
			
			button_bg_color.setOnClickListener(MyOnClickListenerColor);
			//button_text_color.setOnClickListener(MyOnClickListenerColor);
			
			
			tvname.setText(name);
			button_bg_color.setFocusable(false);
			
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			final LayoutInflater inflater = LayoutInflater.from(context);
			final View v = inflater.inflate(R.layout.catalogs_list_item, null);

			return v;
		}
	}		
	
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		
		
		dbHelper = new myDBHelper(this,  
				taskOrganizerDatabaseHelper.DATABASE_NAME, 
				null, 
				taskOrganizerDatabaseHelper.DATABASE_VERSION);
		tasksDB = dbHelper.getWritableDatabase();

		Intent i = getIntent();
		CurrentTable = i.getExtras().getInt("Table");

		// Inflate your view
		setContentView(R.layout.listofcatalog);

		final EditText myEditText = (EditText)findViewById(R.id.itemname);
		final Button buttonaddnewtask = (Button)findViewById(R.id.buttonaddnewtask);    


		buttonaddnewtask.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
	        	AddNewItem();
	        }
	    });


		TextView tvhead =  (TextView)this.findViewById(R.id.textview_head);

		
		tasksListView = (ListView)this.findViewById(R.id.catalogListView);

		//tasksListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
		
		tasksListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView _av, View _v, int _index, long arg3) {
				//selectedTask = tasks.get(_index);
				//showDialog(Task_DIALOG);
			}	});

		//registerForContextMenu(tasksListView);

		if (CurrentTable == 1){
			tvhead.setText(getString(R.string.menu_categories));
			KEY_NAME = taskOrganizerDatabaseHelper.Categories_NAME;
			KEY_ID = taskOrganizerDatabaseHelper.Categories_ID;
			CurrentTableName = taskOrganizerDatabaseHelper.Categories_Table;
			c = queryCatalogs(new String[]{KEY_NAME,KEY_ID+" as _id",taskOrganizerDatabaseHelper.Categories_BG_COLOR,taskOrganizerDatabaseHelper.Categories_TEXT_COLOR}, null, null, null);
		}
		else if (CurrentTable == 2){
			tvhead.setText(getString(R.string.menu_statuses));
			KEY_NAME = taskOrganizerDatabaseHelper.Status_NAME;
			KEY_ID = taskOrganizerDatabaseHelper.Status_ID;
			CurrentTableName = taskOrganizerDatabaseHelper.Statuses_Table;
			c = queryStatuses(new String[]{KEY_NAME,KEY_ID+" as _id"}, null, null, null);
		}
		else if (CurrentTable == 3)
		{
			tvhead.setText(getString(R.string.menu_filters));
			KEY_NAME = taskOrganizerDatabaseHelper.Filter_NAME;
			KEY_ID = taskOrganizerDatabaseHelper.Filter_ID;
			CurrentTableName = taskOrganizerDatabaseHelper.Filters_Table;
			c = queryFilters(new String[]{KEY_NAME,"XML",KEY_ID+" as _id"}, null, null, null);
		}		
		
		startManagingCursor(c);
		
		if (CurrentTable == 1){
			ll = new MySimpleCursorAdapterCatalogs(this, R.layout.catalogs_list_item, c, new String[]{KEY_NAME}, new int[]{R.id.text_view_catalogs_name});
		}
		else
		{
			ll = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, c, new String[]{KEY_NAME}, new int[]{android.R.id.text1});
		}
 
		tasksListView.setAdapter(ll);
		
		
		
		if (CurrentTable == 3)
		{
			tasksListView.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int pos,
						long id) {
					c.moveToPosition(pos);
					Intent intent = new Intent();
					String XML = c.getString(c.getColumnIndexOrThrow(taskOrganizerDatabaseHelper.Filter_XML));
					String name_of_filter = c.getString(c.getColumnIndexOrThrow(taskOrganizerDatabaseHelper.Filter_NAME));
					intent.putExtra("filter", XML);
					intent.putExtra("name_of_filter", name_of_filter);
					intent.putExtra("idfilter", id);
					setResult(RESULT_OK,intent);	
					tasksDB.close();
					finish();  
				}
			});
			
			tasksListView.setOnItemLongClickListener(new OnItemLongClickListener() {	
				public boolean onItemLongClick(AdapterView<?> parent, View view,
						int pos, long id) {
					c.moveToPosition(pos);
					CurrentPosition = pos;
					CurrentChoosenId = c.getInt(c.getColumnIndexOrThrow("_id"));
					Intent i = new Intent(ListOfCatalog.this,flexiblemenu.class);
					i.putExtra("Regim",  "EditDelete");
					startActivityForResult(i, REQUEST_CONTEXT_MENU);					
					return true;
				}
			});	    
			
		}
		else{
			tasksListView.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> parent, View view, int pos,
						long id) {

					c.moveToPosition(pos);
					CurrentChoosenId = c.getInt(c.getColumnIndexOrThrow("_id"));
					CurrentPosition = pos;
					Intent i = new Intent(ListOfCatalog.this,flexiblemenu.class);
					i.putExtra("Regim",  "EditDelete");
					startActivityForResult(i, REQUEST_CONTEXT_MENU);
				}
			});
		}

		Button button_help = (Button)this.findViewById(R.id.button_help);
		button_help.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(getBaseContext(), manual.class);
				startActivity(i);
			}
		});		



		
		
	}
	
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);

    	if (requestCode == REQUEST_CONTEXT_MENU)
    	{
    		if (resultCode == Activity.RESULT_OK) 
    			if (data.getExtras().getInt("Result") == 0)
    			{
    			
    				if (CurrentTable==3){
    					Intent i = new Intent(ListOfCatalog.this,filter_form.class);
    					Intent intent = new Intent();
    					c.moveToPosition(CurrentPosition);
    					String XML = c.getString(c.getColumnIndexOrThrow(taskOrganizerDatabaseHelper.Filter_XML)); 					
    					i.putExtra("filter",  XML);
    					i.putExtra("id",  c.getLong(c.getColumnIndexOrThrow("_id")));
    					i.putExtra("name_of_filter",  c.getString(c.getColumnIndexOrThrow("name")));
    					startActivityForResult(i, REQUEST_NEW_NAME); 
    				}
    				else{
    					Intent i = new Intent(ListOfCatalog.this,editname.class);
    					c.moveToPosition(CurrentPosition);
    					i.putExtra("Name",  c.getString(c.getColumnIndexOrThrow("name")));
    					i.putExtra("Title",  getString(R.string.editname));
    					startActivityForResult(i, REQUEST_NEW_NAME);     					
    				}
				
     			}
    			else
    			{
    				//Toast.makeText(getApplicationContext(), "ID = "+CurrentChoosenId+" delete.", Toast.LENGTH_SHORT).show();
    				DeleteItem(CurrentChoosenId);
    			}
    	}

    	if (requestCode == REQUEST_NEW_NAME && resultCode == Activity.RESULT_OK)
    		if (data.getExtras().getInt("Result") == 1)
    		{
    			if (CurrentTable!=3){
    				UpdateItem(data.getExtras().getString("Name"));
    			}
    			else
    			{
    				c.requery();
    				ll.notifyDataSetChanged();
    			}
    			//Toast.makeText(getApplicationContext(), "ID = "+CurrentChoosenId+" edit "+NewName, Toast.LENGTH_SHORT).show();
    		}

    		
    }


    public void colorChanged(String key, int color) {
    	//day_off_color = color;
    	//TextView tv_pick_color = (TextView)this.findViewById(R.id.textView_color_day_off_pick);	
    	//tv_pick_color.setBackgroundColor(day_off_color);

    	String tag = key;


    	ContentValues _initialValues = new ContentValues();  

    	if (tag.startsWith("BG"))
    	{
    		_initialValues.put(taskOrganizerDatabaseHelper.Categories_BG_COLOR, color);
    		tag = tag.replace("BG", "");
    	}
    	else
    	{
    		_initialValues.put(taskOrganizerDatabaseHelper.Categories_TEXT_COLOR, color);
    	}


    	int position = Integer.parseInt(tag);

    	String where = KEY_ID + "=" + array_of_colors[position][0];
    	long rowID = tasksDB.update(CurrentTableName,_initialValues,where,null);
    	c.requery();
    	ll.notifyDataSetChanged(); 

    } 		
	
	
	
}