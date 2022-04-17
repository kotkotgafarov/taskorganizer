package com.eugenemath.taskorganizer.namespace;

import java.util.Date;

import android.text.TextUtils;
import android.widget.RemoteViews;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Color;

public class ToDoCalendarWidget extends AppWidgetProvider {


	public Filter filter;
	private Cursor c;
	private static SQLiteDatabase tasksDB;
	private static final String PREFS_NAME = "PrefsTaskOrganizer";
	private Long id_default_filter = (long)-1;


	public void ReadPreferences(Context context)
	{
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
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
		}
	}

	@Override
	public void onUpdate(Context context,
			AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		UpdateTasks(context,
				appWidgetManager,
				appWidgetIds);
	}
	
	

	public void UpdateTasks(Context context,
			AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {


		taskOrganizerDatabaseHelper dbHelper = new taskOrganizerDatabaseHelper(context,  
				taskOrganizerDatabaseHelper.DATABASE_NAME, 
				null, 
				taskOrganizerDatabaseHelper.DATABASE_VERSION);
		tasksDB = dbHelper.getWritableDatabase();		


		filter = new Filter();
		ReadPreferences(context);
		c = query(null, null, null, null);

		/*Cursor lastEarthquake;
		ContentResolver cr = context.getContentResolver();
		lastEarthquake = cr.query(EarthquakeProvider.CONTENT_URI,
				null, null, null, null);
		String magnitude = "--";
		String details = "-- None --";
		if (lastEarthquake != null) {
			try {
				if (lastEarthquake.moveToFirst()) {
					magnitude =
							lastEarthquake.getString(EarthquakeProvider.MAGNITUDE_COLUMN);
					details =
							lastEarthquake.getString(EarthquakeProvider.DETAILS_COLUMN);
				}
			}
			finally {
				lastEarthquake.close();
			}
		}*/

		int completed = 0;

		if (c.moveToFirst()) {
			do { 

				int	status  = c.getInt(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_STATUS));
				if (status==2) {completed++;}
			
			} while(c.moveToNext());
		}


		String magnitude = ""+c.getCount();
		String magnitude2 = ""+(c.getCount()-completed);

		/*Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		ComponentName cn = new ComponentName("com.eugenemath.taskorganizer.namespace", "com.eugenemath.taskorganizer.namespace.taskorganizerActivity"); 
		//intent.setClassName("com.eugenemath.taskorganizer.namespace", "com.eugenemath.taskorganizer.namespace.taskorganizerActivity"); 
		intent.setComponent(cn);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent myPI = PendingIntent.getService(context, 0, intent, 0); */
		
		Intent configIntent = new Intent(context, taskorganizerActivity.class);
		PendingIntent myPI = PendingIntent.getActivity(context, 0, configIntent, 0);
		//configIntent.setAction(ACTION_WIDGET_CONFIGURE);
		
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.todocalendar_widget);
			views.setTextViewText(R.id.widget_number_of_tasks, magnitude);
			views.setTextViewText(R.id.widget_number_of_tasks2, magnitude2);
			views.setOnClickPendingIntent(R.id.widget_number_of_tasks, myPI); 
			views.setOnClickPendingIntent(R.id.widget_number_of_tasks2, myPI); 
			views.setOnClickPendingIntent(R.id.widget_layout, myPI);
			
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}




	}
	
	
	
	

	public void UpdateTasks(Context context) {
		ComponentName thisWidget = new ComponentName(context,
				ToDoCalendarWidget.class);
		AppWidgetManager appWidgetManager =
				AppWidgetManager.getInstance(context);
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		UpdateTasks(context, appWidgetManager, appWidgetIds);
	}

	

	@Override
	public void onReceive(Context context, Intent intent){
		super.onReceive(context, intent);
		if (intent.getAction().equals(taskorganizerActivity.WIDGET_REFRESHED))
		{
			UpdateTasks(context);
		}
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
				+ taskOrganizerDatabaseHelper.KEY_NAME+","
				+ taskOrganizerDatabaseHelper.KEY_CATEGORY+","
				+ taskOrganizerDatabaseHelper.KEY_STATUS+""
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

}