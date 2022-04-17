package com.eugenemath.taskorganizer.namespace;

import android.content.*;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.Color;
import android.util.Log;

//The underlying database

class taskOrganizerDatabaseHelper extends SQLiteOpenHelper {

    Context _context;
	private static SQLiteDatabase tasksDB;
	private static final String TAG = "TaskOrganizerProvider";
	public static final String DATABASE_NAME = "tasks.db";
	public static final int DATABASE_VERSION = 17;
	public static final String Tasks_TABLE = "tasks";
	public static final String Statuses_Table = "statuses";
	public static final String Categories_Table = "categories";
	public static final String Filters_Table = "filters";
	public static final String Consumed_Time_Table = "consumedtime";
	
	// Column Names
	public static final String KEY_ID = "idtask";
	public static final String KEY_DATE = "date";
	public static final String KEY_CODE = "code";
	public static final String KEY_PARENT = "parenttask";

	public static final String KEY_NAME = "name"; 
	public static final String KEY_RESPONSIBLE = "responsible"; 	  
	public static final String KEY_EXECUTOR = "executor";
	public static final String KEY_DESCRIPTION = "description";

	public static final String KEY_PROCESSINGTIME = "processingtime";
	public static final String KEY_DUEDATE = "duedate";
	public static final String KEY_RELEASEDATE = "releasedate";
	public static final String KEY_STATUS = "status";
	public static final String KEY_PROCESSINGTIMEFACT = "processingtimefact";
	public static final String KEY_PRIORITY = "priority";
	public static final String KEY_DATEFROM = "datefrom";
	public static final String KEY_DATETO = "dateto";
	public static final String KEY_DATEFROMFACT = "datefromfact";
	public static final String KEY_DATETOFACT = "datetofact";	  
	public static final String KEY_PERCENTOFCOMPLETION= "percentofcompletion";
	public static final String KEY_CATEGORY= "category";
	public static final String KEY_IDEVENT = "idevent";
	public static final String KEY_IDTOODLEDO = "idtoodledo";
	public static final String KEY_DATEMODIFIED= "datemodified";
	public static final String KEY_ALARM = "alarm";
	public static final String KEY_IMAGE = "image";

	// Column indexes 
	public static final int ID_COLUMN = 0;
	public static final int DATE_COLUMN = 1;
	public static final int CODE_COLUMN = 2;
	public static final int PARENT_COLUMN = 3;  
	public static final int NAME_COLUMN = 4; 
	public static final int RESPONSIBLE_COLUMN = 5; 	  
	public static final int EXECUTOR_COLUMN = 6;
	public static final int DESCRIPTION_COLUMN = 7;	  
	public static final int PROCESSINGTIME_COLUMN = 8;
	public static final int DUEDATE_COLUMN = 9;
	public static final int RELEASEDATE_COLUMN = 10;
	public static final int STATUS_COLUMN = 11;
	public static final int PROCESSINGTIMEFACT_COLUMN = 12;
	public static final int PRIORITY_COLUMN = 13;
	public static final int DATEFROM_COLUMN = 14;
	public static final int DATETO_COLUMN = 15;
	public static final int DATEFROMFACT_COLUMN = 16;
	public static final int DATETOFACT_COLUMN = 17;	  
	public static final int PERCENTOFCOMPLETION_COLUMN= 18;
	public static final int CATEGORY_COLUMN= 19;	  



	// Statuses & Categories
	public static final String Status_ID = "idstatus";
	public static final String Status_NAME = "name";   

	public static final String Categories_ID = "idcategory";
	public static final String Categories_PARENT = "parentcategory";
	public static final String Categories_NAME = "name"; 
	public static final String Categories_BG_COLOR = "bgcolor";
	public static final String Categories_TEXT_COLOR = "textcolor"; 

	// FILTER
	public static final String Filter_ID = "idfilter";
	public static final String Filter_XML = "xml";
	public static final String Filter_NAME = "name"; 	
	
	
	// Consumed time
	public static final String Consumed_Time_ID = "idconsumedtime";
	public static final String Consumed_Time_Category = "category";
	public static final String Consumed_Time_Task = "idtask";
	public static final String Consumed_Time_Comment = "comment";
	public static final String Consumed_Time_Value = "value";
	public static final String Consumed_Time_Date = "date";
	
	
	
	// History
	public static final String History_Table = "history";
	public static final String History_NAME = "name";
	public static final String History_task_id = "idtask";
	public static final String History_ID = "idhistory";
	
	
	private static final String TABLE_TASKS_CREATE =
			"create table " + Tasks_TABLE + " (" 
					+ KEY_ID + " integer primary key autoincrement, "
					+ KEY_DATE + " INTEGER, "
					+ KEY_IDEVENT + " INTEGER, "
					+ KEY_IDTOODLEDO + " INTEGER, "
					+ KEY_CODE + " TEXT, "
					+ KEY_PARENT + " INTEGER, "
					+ KEY_NAME + " TEXT, "
					+ KEY_RESPONSIBLE + " TEXT, "
					+ KEY_EXECUTOR + " TEXT, "
					+ KEY_DESCRIPTION + " TEXT, "
					+ KEY_STATUS + " INTEGER, "
					+ KEY_PROCESSINGTIME + " INTEGER, "
					+ KEY_DUEDATE + " INTEGER, "
					+ KEY_RELEASEDATE + " INTEGER, "
					+ KEY_PROCESSINGTIMEFACT + " INTEGER, "	      
					+ KEY_PRIORITY + " BYTE, "
					+ KEY_DATEFROM + " INTEGER, "
					+ KEY_DATETO + " INTEGER, "
					+ KEY_DATEMODIFIED + " INTEGER, "
					+ KEY_DATEFROMFACT + " INTEGER, "
					+ KEY_DATETOFACT + " INTEGER, "	   
					+ KEY_ALARM + " INTEGER, "
					+ KEY_IMAGE + " TEXT, "
					+ KEY_PERCENTOFCOMPLETION + " BYTE," 
					+  KEY_CATEGORY + " INTEGER);";

	
	// Consumed time
	private static final String TABLE_CONSUMED_TIME_CREATE =
			"create table " + Consumed_Time_Table + " (" 
					+ Consumed_Time_ID + " integer primary key autoincrement, "
					+ Consumed_Time_Category +" integer,"
					+ Consumed_Time_Date +" integer,"
					+ Consumed_Time_Task +" integer,"
					+ Consumed_Time_Value +" integer,"
					+ Consumed_Time_Comment + " TEXT);";	   
	

	private static final String TABLE_STATUSES_CREATE =
			"create table " + Statuses_Table + " (" 
					+ Status_ID + " integer primary key autoincrement, "
					+ Status_NAME + " TEXT);";	    

	private static final String TABLE_CATEGORIES_CREATE =
			"create table " + Categories_Table + " (" 
					+ Categories_ID + " integer primary key autoincrement, "
					+ Categories_NAME + " TEXT,"
					+ Categories_BG_COLOR +" integer,"
					+ Categories_TEXT_COLOR +" integer,"
					+ Categories_PARENT +" integer);";
	
	private static final String TABLE_FILTERS_CREATE =
			"create table " + Filters_Table + " (" 
					+ Filter_ID + " integer primary key autoincrement, "
					+ Filter_NAME + " TEXT,"
					+ Filter_XML +" TEXT);";	
	

	private static final String TABLE_HISTORY_CREATE =
			"create table " + History_Table + " (" 
					+ History_ID + " integer primary key autoincrement, "
					+ History_NAME + " TEXT,"
					+ History_task_id +" integer);";
	
	private static final String TABLE_ADD_COLUMN_IDEVENT =
			"alter table " + Tasks_TABLE + " ADD "+KEY_IDEVENT+" INTEGER";
	
	private static final String TABLE_ADD_COLUMN_IDTOODLEDO =
			"alter table " + Tasks_TABLE + " ADD "+KEY_IDTOODLEDO+" INTEGER";
	
	private static final String TABLE_ADD_COLUMN_IDTOKEY_DATEMODIFIED =
			"alter table " + Tasks_TABLE + " ADD "+KEY_DATEMODIFIED+" INTEGER";

	private static final String TABLE_ADD_COLUMN_COLORS1 =
			"alter table " + Categories_Table + " ADD "+Categories_TEXT_COLOR+" INTEGER";
	
	private static final String TABLE_ADD_COLUMN_COLORS2 =
			"alter table " + Categories_Table + " ADD "+Categories_BG_COLOR+" INTEGER";
	
	private static final String TABLE_ADD_COLUMN_ALARM =
			"alter table " + Tasks_TABLE + " ADD "+KEY_ALARM+" INTEGER";	
	
	private static final String TABLE_ADD_COLUMN_IMAGE =
			"alter table " + Tasks_TABLE + " ADD "+KEY_IMAGE+" TEXT";	
	
	
	
	public taskOrganizerDatabaseHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		_context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		CreateTables(db);
	}

	public void CreateTables(SQLiteDatabase db){
		db.execSQL(TABLE_TASKS_CREATE);        
		db.execSQL(TABLE_STATUSES_CREATE);
		db.execSQL(TABLE_CATEGORIES_CREATE);
		db.execSQL(TABLE_HISTORY_CREATE);
		db.execSQL(TABLE_FILTERS_CREATE);
		db.execSQL(TABLE_CONSUMED_TIME_CREATE);
		
		ContentValues values = new ContentValues();  
		values.put(Status_NAME, _context.getString(R.string.status1));
		db.insert(Statuses_Table, null, values);


		values.clear();
		values.put(Status_NAME, _context.getString(R.string.status2));
		db.insert(Statuses_Table, null, values);	


		values.clear();
		values.put(Status_NAME, _context.getString(R.string.status3));
		db.insert(Statuses_Table, null, values);	      

		values.clear();
		values.put(Status_NAME, _context.getString(R.string.status4));
		db.insert(Statuses_Table, null, values);	

		values.clear();
		values.put(Status_NAME, _context.getString(R.string.status5));
		db.insert(Statuses_Table, null, values);	

		values.clear();
		values.put(Categories_NAME, _context.getString(R.string.category1));
		values.put(Categories_BG_COLOR, Color.BLUE);
		db.insert(Categories_Table, null, values);	

		values.clear();
		values.put(Categories_NAME, _context.getString(R.string.category2));
		values.put(Categories_BG_COLOR, Color.RED);
		db.insert(Categories_Table, null, values);      

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		if (oldVersion < 14)
		{
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");			
			db.execSQL("DROP TABLE IF EXISTS " + Tasks_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + Statuses_Table);
			db.execSQL("DROP TABLE IF EXISTS " + Categories_Table);
			db.execSQL("DROP TABLE IF EXISTS " + History_Table);
			db.execSQL("DROP TABLE IF EXISTS " + Filters_Table);
			db.execSQL("DROP TABLE IF EXISTS " + Consumed_Time_Table);
			onCreate(db);			
		}
		
		if (oldVersion ==14)
		{
			db.execSQL("DROP TABLE IF EXISTS " + Consumed_Time_Table);
			db.execSQL(TABLE_CONSUMED_TIME_CREATE);
		}
		
		if (oldVersion ==15)
		{
			db.execSQL(TABLE_ADD_COLUMN_IDTOKEY_DATEMODIFIED);
			db.execSQL(TABLE_ADD_COLUMN_IDTOODLEDO);
		}
		
		if (oldVersion ==16)
		{
			db.execSQL(TABLE_ADD_COLUMN_IMAGE);

		}			
	}
}
