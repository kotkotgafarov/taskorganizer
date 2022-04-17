package com.eugenemath.taskorganizer.namespace;

import com.eugenemath.taskorganizer.namespace.R;

import android.app.Application;
import android.content.*;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class TaskOrganizerProvider extends ContentProvider {
	
	public static final Uri CONTENT_URI = Uri.parse("content://com.eugenemath.provider.taskorganizer/tasks");
	
	  @Override
	  public boolean onCreate() {
	    Context context = getContext();

	    taskOrganizerDatabaseHelper dbHelper = new taskOrganizerDatabaseHelper(context,  
	    		taskOrganizerDatabaseHelper.DATABASE_NAME, 
	                                                                   null, 
	                                                                   taskOrganizerDatabaseHelper.DATABASE_VERSION);
	    tasksDB = dbHelper.getWritableDatabase();
	    return (tasksDB == null) ? false : true;
	  } 
	   

	  @Override
	  public Cursor query(Uri uri, 
	                      String[] projection, 
	                      String selection, 
	                      String[] selectionArgs, 
	                      String sort) {
	        
	    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

	    String SelectQuery = "SELECT tasks."+ taskOrganizerDatabaseHelper.KEY_ID+","+ taskOrganizerDatabaseHelper.KEY_DATE+","+ taskOrganizerDatabaseHelper.KEY_CODE+","+ taskOrganizerDatabaseHelper.KEY_PARENT+",tasks."+ taskOrganizerDatabaseHelper.KEY_NAME+","+ taskOrganizerDatabaseHelper.KEY_RESPONSIBLE+","+ taskOrganizerDatabaseHelper.KEY_EXECUTOR+","
	    		+ taskOrganizerDatabaseHelper.KEY_DESCRIPTION+","+ taskOrganizerDatabaseHelper.KEY_PROCESSINGTIME+","+ taskOrganizerDatabaseHelper.KEY_DUEDATE+","+ taskOrganizerDatabaseHelper.KEY_RELEASEDATE+","+ taskOrganizerDatabaseHelper.KEY_STATUS+","+ taskOrganizerDatabaseHelper.KEY_PROCESSINGTIMEFACT+","+ taskOrganizerDatabaseHelper.KEY_PRIORITY+","+ taskOrganizerDatabaseHelper.KEY_DATEFROM+","+ taskOrganizerDatabaseHelper.KEY_DATETO+","
	    		+ taskOrganizerDatabaseHelper.KEY_DATEFROMFACT+","+ taskOrganizerDatabaseHelper.KEY_DATETOFACT+","+ taskOrganizerDatabaseHelper.KEY_PERCENTOFCOMPLETION+","+ taskOrganizerDatabaseHelper.KEY_CATEGORY+","+ taskOrganizerDatabaseHelper.KEY_ID+","+ taskOrganizerDatabaseHelper.Statuses_Table+"."+ taskOrganizerDatabaseHelper.Status_NAME+","+ taskOrganizerDatabaseHelper.Categories_Table+"."+ taskOrganizerDatabaseHelper.Categories_NAME
	    		+" FROM "+ taskOrganizerDatabaseHelper.Tasks_TABLE+" LEFT JOIN "+ taskOrganizerDatabaseHelper.Statuses_Table+" ON "+ taskOrganizerDatabaseHelper.Tasks_TABLE+"."+ taskOrganizerDatabaseHelper.KEY_STATUS+" = "+ taskOrganizerDatabaseHelper.Statuses_Table+"."+ taskOrganizerDatabaseHelper.Status_ID
	    		+" LEFT JOIN "+ taskOrganizerDatabaseHelper.Categories_Table+" ON "+ taskOrganizerDatabaseHelper.Tasks_TABLE+"."+ taskOrganizerDatabaseHelper.KEY_STATUS+" = "+ taskOrganizerDatabaseHelper.Categories_Table+"."+ taskOrganizerDatabaseHelper.Categories_ID;
	    
	    String orderBy;
	    if (TextUtils.isEmpty(sort)) {
	      orderBy = "tasks."+ taskOrganizerDatabaseHelper.KEY_ID;
	    } else {
	      orderBy = sort;
	    }
	    
	    SelectQuery +=" ORDER BY "+sort;

	    switch (uriMatcher.match(uri)) {
	      case Tasks_ID: 
	    	  SelectQuery +=" WHERE "+ taskOrganizerDatabaseHelper.KEY_ID + "=" + uri.getPathSegments().get(1);
	    	  //qb.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1));
	                     break;
	      default      : break;
	    }	    
	    
	    Cursor c = tasksDB.rawQuery(SelectQuery, selectionArgs);
	    

	    // Register the contexts ContentResolver to be notified if
	    // the cursor result set changes. 
	    c.setNotificationUri(getContext().getContentResolver(), uri);
	    
	    // Return a cursor to the query result.
	    return c;
	  }

	  @Override
	  public Uri insert(Uri _uri, ContentValues _initialValues) {
	    // Insert the new row, will return the row number if 
	    // successful.
	    long rowID = tasksDB.insert( taskOrganizerDatabaseHelper.Tasks_TABLE, "task", _initialValues);
	          
	    // Return a URI to the newly inserted row on success.
	    if (rowID > 0) {
	      Uri uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
	      //getContext().getContentResolver().notifyChange(uri, null);
	      return uri;
	    }

	    throw new SQLException("Failed to insert row into " + _uri);
	    
	  }

	  public static void insertNew(ContentValues _initialValues) {
		    // Insert the new row, will return the row number if 
		    // successful.
		    long rowID = tasksDB.insert( taskOrganizerDatabaseHelper.Tasks_TABLE, "task", _initialValues);
		  }	  
	  
	  @Override
	  public int delete(Uri uri, String where, String[] whereArgs) {
	    int count;
	    
	    switch (uriMatcher.match(uri)) {
	      case Tasks:
	        count = tasksDB.delete( taskOrganizerDatabaseHelper.Tasks_TABLE, where, whereArgs);
	        break;

	      case Tasks_ID:
	        String segment = uri.getPathSegments().get(1);
	        count = tasksDB.delete( taskOrganizerDatabaseHelper.Tasks_TABLE,  taskOrganizerDatabaseHelper.KEY_ID + "="
	                                    + segment
	                                    + (!TextUtils.isEmpty(where) ? " AND (" 
	                                    + where + ')' : ""), whereArgs);
	        break;

	      default: throw new IllegalArgumentException("Unsupported URI: " + uri);
	    }

	    getContext().getContentResolver().notifyChange(uri, null);
	    return count;
	  }

	  @Override
	  public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
	    int count;
	    switch (uriMatcher.match(uri)) {
	      case Tasks: count = tasksDB.update( taskOrganizerDatabaseHelper.Tasks_TABLE, values, 
	                                               where, whereArgs);
	                   break;

	      case Tasks_ID: String segment = uri.getPathSegments().get(1);
	                     count = tasksDB.update( taskOrganizerDatabaseHelper.Tasks_TABLE, values,  taskOrganizerDatabaseHelper.KEY_ID 
	                             + "=" + segment 
	                             + (!TextUtils.isEmpty(where) ? " AND (" 
	                             + where + ')' : ""), whereArgs);
	                     break;

	      default: throw new IllegalArgumentException("Unknown URI " + uri);
	    }

	    getContext().getContentResolver().notifyChange(uri, null);
	    return count;
	  }
	  
	  @Override
	  public String getType(Uri uri) {
	    switch (uriMatcher.match(uri)) {
	      case Tasks: return "vnd.android.cursor.dir/vnd.eugenemath.taskorganizer";
	      case Tasks_ID: return "vnd.android.cursor.item/vnd.eugenemath.taskorganizer";
	      default: throw new IllegalArgumentException("Unsupported URI: " + uri);
	    }
	  }
	  
	  // Create the constants used to differentiate between the different URI 
	  // requests.
	  private static final int Tasks = 1;
	  private static final int Tasks_ID = 2;

	  private static final UriMatcher uriMatcher;

	  // Allocate the UriMatcher object, where a URI ending in 'tasks' will
	  // correspond to a request for all earthquakes, and 'earthquakes' with a 
	  // trailing '/[rowID]' will represent a single earthquake row.
	  static {
	   uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	   uriMatcher.addURI("com.eugenemath.provider.TaskOrganizer", "tasks", Tasks);
	   uriMatcher.addURI("com.eugenemath.provider.TaskOrganizer", "tasks/#", Tasks_ID);
	  }
	  
	  //The underlying database
	  private static SQLiteDatabase tasksDB;


	  
	}