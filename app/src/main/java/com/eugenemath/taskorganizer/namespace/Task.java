package com.eugenemath.taskorganizer.namespace;

import java.util.Date;
import java.text.SimpleDateFormat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.location.Location;

public class Task {

	public Integer idtask;
	public Long idevent;
	public Integer parent;
	public Integer processingtime;
	public Integer status;
	public Integer processingtimefact;	 	 	 
	public Integer priority;	 	 	 
	public Integer percentofcompletion;	 	 	 
	public Integer category;	
	public Integer alarm;
	public Date date;
	public Date duedate;
	public Date releasedate;
	public Date datefrom;
	public Date dateto;
	public Date datefromfact; 
	public Date datetofact;
	public String name;
	public String code;
	public String executor;  
	public String responsible;
	public String description;  
	public String  categoryname;
	public String statusname;
	public String parentname = "";
	public String image = "";
	public Integer categorybgcolor = 0;
	public Integer categorytextcolor = 0;
	
	
	private static SQLiteDatabase tasksDB;

	/* public Date getDate() { return date; }
  public String getDetails() { return details; }
  public Location getLocation() { return location; }
  public double getMagnitude() { return magnitude; }
  public String getLink() { return link; }*/



	public Task(Integer _id,
			Integer _parent,
			Integer _processingtime,
			Integer _status,
			Integer _processingtimefact,	 	 	 
			Integer _priority,	 	 	 
			Integer _percentofcompletion,	 	 	 
			Integer _category,	 
			Date _date,
			Date _duedate,
			Date _releasedate,
			Date _datefrom,
			Date _dateto,
			Date _datefromfact, 
			Date _datetofact,
			String _name,
			String _code,
			String _executor,  
			String _responsible,
			String _description  ) {

		idtask = _id;
		parent =  _parent;
		processingtime  = _processingtime;
		status  =_status;
		processingtimefact = _processingtimefact; 	 	 
		priority  =_priority;	 	 	 
		percentofcompletion  =_percentofcompletion;	 	 	 
		category  =_category; 
		date  =_date;
		duedate  =_duedate;
		releasedate = _releasedate;
		datefrom  =_datefrom;
		dateto  =_dateto;
		datefromfact  =_datefromfact; 
		datetofact  =_datetofact;
		name  =_name;
		code  =_code;
		executor  =_executor;  
		responsible  =_responsible;
		description  =_description;
		alarm = 0;
		image = "";
	}

	public Task(Cursor c)	
	{
		FillByCursor(c);
	}

	public Task(String TaskName)	
	{
		name  = TaskName;
		
		idtask = null;
		idevent = null;
		
		parent =  -1;
		processingtime  = 0;
		status  = 1;
		processingtimefact = 0; 	 	 
		priority  =0;	 	 	 
		percentofcompletion  =0;	 	 	 
		category  =1; 
		alarm = 0;

		date  =new Date(0);
		duedate  =new Date(0);//new Date(70,1,1);
		releasedate = new Date(0);
		datefrom  =new Date(0);
		dateto  =new Date(0);
		datefromfact  =new Date(0); 
		datetofact  =new Date(0);

		code  ="";
		executor  =""; 
		responsible  ="";
		description  =""; 
		
		image = "";
	}
	
	public void FillByCursor(Cursor c)
	{

		idtask = 	c.getInt(c.getColumnIndex("_id"));
		
		if (c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_IDEVENT)!=-1)
		{
		    idevent = 	c.getLong(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_IDEVENT));
		}
		
		parent =  c.getInt(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_PARENT));
		processingtime  = c.getInt(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_PROCESSINGTIME));
		status  = c.getInt(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_STATUS));
		processingtimefact = c.getInt(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_PROCESSINGTIMEFACT)); 	 	 
		priority  =c.getInt(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_PRIORITY));	 	 	 
		percentofcompletion  =c.getInt(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_PERCENTOFCOMPLETION));	 	 	 
		category  =c.getInt(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_CATEGORY)); 
		//alarm  =c.getInt(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_ALARM)); 
		
		if (c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_ALARM)!=-1)
		{
			alarm  =c.getInt(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_ALARM)); 
		}
		
		
		if (c.getColumnIndex("categorybgcolor")!=-1)
		{
			categorybgcolor  =c.getInt(c.getColumnIndex("categorybgcolor")); 
		}
		
		if (c.getColumnIndex("categorytextcolor")!=-1)
		{
			categorytextcolor  =c.getInt(c.getColumnIndex("categorytextcolor")); 
		}
		
		
		date  =new Date(c.getLong(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATE)));
		duedate  =new Date(c.getLong(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DUEDATE)));
		releasedate = new Date(c.getLong(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_RELEASEDATE)));
		datefrom  =new Date(c.getLong(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATEFROM)));
		dateto  =new Date(c.getLong(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATETO)));
		datefromfact  =new Date(c.getLong(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATEFROMFACT))); 
		datetofact  =new Date(c.getLong(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATETOFACT)));

		name  =c.getString(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_NAME));
		code  =c.getString(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_CODE));
		executor  =c.getString(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_EXECUTOR)); 
		responsible  =c.getString(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_RESPONSIBLE));
		description  =c.getString(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DESCRIPTION));  

		
		if (c.getColumnIndex("image")!=-1)
		{
			image  =c.getString(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_IMAGE));
		}		
		
		if (image == null)
		{
			image = "";
		}
		
		if (c.getColumnIndex("parentname")!=-1)
		{
			parentname =c.getString(c.getColumnIndex("parentname")); 
		}
		

		if (c.getColumnIndex("categoryname")!=-1)
		{
			categoryname  =c.getString(c.getColumnIndex("categoryname"));
		}

		if (c.getColumnIndex("statusname")!=-1)
		{
			statusname  =c.getString(c.getColumnIndex("statusname"));
		}	

		
	}
	
	
	public Task(Context context, Long _id)
	{
		taskOrganizerDatabaseHelper dbHelper = new taskOrganizerDatabaseHelper(context,  
				taskOrganizerDatabaseHelper.DATABASE_NAME, 
				null, 
				taskOrganizerDatabaseHelper.DATABASE_VERSION);
		tasksDB = dbHelper.getWritableDatabase();	
		
		Cursor c = query(_id);
		c.moveToFirst();
		FillByCursor(c);	
		
	}
	
	public Cursor query(Long _id) {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String SelectQuery = "SELECT tasks."
				+ taskOrganizerDatabaseHelper.KEY_ID+" as _id,tasks."
				+ taskOrganizerDatabaseHelper.KEY_IDEVENT+",tasks."
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

				//?? tasks."
				//				+ "parenttable."+taskOrganizerDatabaseHelper.KEY_NAME+" as parentname,
				+ taskOrganizerDatabaseHelper.Statuses_Table+"."+ taskOrganizerDatabaseHelper.Status_NAME+" as statusname,"
				+ taskOrganizerDatabaseHelper.Categories_Table+"."+ taskOrganizerDatabaseHelper.Categories_NAME+" as categoryname"
				+" FROM "+ taskOrganizerDatabaseHelper.Tasks_TABLE
				+" LEFT JOIN "+ taskOrganizerDatabaseHelper.Statuses_Table+" ON "+ taskOrganizerDatabaseHelper.Tasks_TABLE+"."+ taskOrganizerDatabaseHelper.KEY_STATUS+" = "+ taskOrganizerDatabaseHelper.Statuses_Table+"."+ taskOrganizerDatabaseHelper.Status_ID
				+" LEFT JOIN "+ taskOrganizerDatabaseHelper.Categories_Table+" ON "+ taskOrganizerDatabaseHelper.Tasks_TABLE+"."+ taskOrganizerDatabaseHelper.KEY_CATEGORY+" = "+ taskOrganizerDatabaseHelper.Categories_Table+"."+ taskOrganizerDatabaseHelper.Categories_ID
	        	+" LEFT JOIN "+ taskOrganizerDatabaseHelper.Tasks_TABLE+" as parenttable ON "+ taskOrganizerDatabaseHelper.Tasks_TABLE+"."+ taskOrganizerDatabaseHelper.KEY_PARENT+" = parenttable."+ taskOrganizerDatabaseHelper.KEY_ID;



		SelectQuery +=" where tasks."+taskOrganizerDatabaseHelper.KEY_ID+" = "+_id;	
		
		
		Cursor c = tasksDB.rawQuery(SelectQuery, null);
		return c;
	}			
	
	
	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH.mm");
		String dateString = sdf.format(date);
		return dateString + ": " + name + " " + description;
	}
	
	public ContentValues ReturnContentValues()
	{
		Task _task = this;
		ContentValues values = new ContentValues(); 
		
		//if (_task.idtask != null){ values.put(taskOrganizerDatabaseHelper.KEY_ID, _task.idtask); }

		values.put(taskOrganizerDatabaseHelper.KEY_DATE, _task.date.getTime());
		values.put(taskOrganizerDatabaseHelper.KEY_CODE, _task.code);
		values.put(taskOrganizerDatabaseHelper.KEY_PARENT, _task.parent);
		values.put(taskOrganizerDatabaseHelper.KEY_NAME, _task.name); 
		values.put(taskOrganizerDatabaseHelper.KEY_RESPONSIBLE, _task.responsible); 	  
		values.put(taskOrganizerDatabaseHelper.KEY_EXECUTOR, _task.executor);
		values.put(taskOrganizerDatabaseHelper.KEY_DESCRIPTION, _task.description); 
		values.put(taskOrganizerDatabaseHelper.KEY_PROCESSINGTIME, _task.processingtime);
		values.put(taskOrganizerDatabaseHelper.KEY_DUEDATE, _task.duedate.getTime());
		values.put(taskOrganizerDatabaseHelper.KEY_RELEASEDATE, _task.releasedate.getTime());
		values.put(taskOrganizerDatabaseHelper.KEY_STATUS, _task.status);
		values.put(taskOrganizerDatabaseHelper.KEY_PROCESSINGTIMEFACT, _task.processingtimefact);
		values.put(taskOrganizerDatabaseHelper.KEY_PRIORITY, _task.priority);
		values.put(taskOrganizerDatabaseHelper.KEY_DATEFROM, _task.datefrom.getTime());
		values.put(taskOrganizerDatabaseHelper.KEY_DATETO, _task.dateto.getTime());
		values.put(taskOrganizerDatabaseHelper.KEY_DATEFROMFACT, _task.datefromfact.getTime());
		values.put(taskOrganizerDatabaseHelper.KEY_DATETOFACT, _task.datetofact.getTime());	  
		values.put(taskOrganizerDatabaseHelper.KEY_PERCENTOFCOMPLETION, _task.percentofcompletion);
		values.put(taskOrganizerDatabaseHelper.KEY_CATEGORY, _task.category);		  		
		values.put(taskOrganizerDatabaseHelper.KEY_ALARM, _task.alarm); 
		values.put(taskOrganizerDatabaseHelper.KEY_IMAGE, _task.image); 
		values.put(taskOrganizerDatabaseHelper.KEY_DATEMODIFIED, (new Date()).getTime()); 
		
		return values;
	}
	
}