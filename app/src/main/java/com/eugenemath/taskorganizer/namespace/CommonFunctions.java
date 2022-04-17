package com.eugenemath.taskorganizer.namespace;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.R.string;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.Contacts.People;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.net.Uri;
import android.os.Environment;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;


public class CommonFunctions {

	private static HttpURLConnection httpcon;

	public CommonFunctions() {

	}

	// ---------------export-import. calendar---------------------------

	public static final void ReadCalendars(Activity act) {
		String[] projection = new String[] { "_id", "name" };
		Uri calendars;

		int version = Integer.valueOf(android.os.Build.VERSION.SDK);
		if (version < 14) {
			calendars = Uri.parse(getCalendarUriBase(act) + "calendars");
		}
		else
		{
			calendars = Calendars.CONTENT_URI;
		}
		
		
		Cursor c_calendars = act.managedQuery(calendars, projection, null,
				null, null);

		if (c_calendars == null) {
			Toast.makeText(act, act.getString(R.string.from_code21),
					Toast.LENGTH_SHORT).show();
			return;
		}

		if (c_calendars.moveToFirst()) {
			String calId;
			int nameColumn = c_calendars.getColumnIndex("name");
			int idColumn = c_calendars.getColumnIndex("_id");
			do {
				// calName = c_calendars.getString(nameColumn);
				calId = c_calendars.getString(idColumn);

				ReadEventsFromCalendar(act, calId);
				// break;
			} while (c_calendars.moveToNext());
		}
	}

	public static void UpdateConsumedTime(Activity act, Task task) {
		taskOrganizerDatabaseHelper dbHelper = new taskOrganizerDatabaseHelper(
				act, taskOrganizerDatabaseHelper.DATABASE_NAME, null,
				taskOrganizerDatabaseHelper.DATABASE_VERSION);
		SQLiteDatabase tasksDB = dbHelper.getWritableDatabase();

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String SelectQuery = "SELECT "
				+ taskOrganizerDatabaseHelper.Consumed_Time_Table + "."
				+ taskOrganizerDatabaseHelper.Consumed_Time_ID + "" + " FROM "
				+ taskOrganizerDatabaseHelper.Consumed_Time_Table + " WHERE "
				+ taskOrganizerDatabaseHelper.Consumed_Time_Task + " = "
				+ task.idtask;

		Cursor c = tasksDB.rawQuery(SelectQuery, null);
		// boolean exists = false;

		if (c.moveToFirst()) {
			// exists = true;
			ContentValues _initialValues = new ContentValues();
			// _initialValues.put(taskOrganizerDatabaseHelper.Consumed_Time_Task,
			// task.idtask);
			_initialValues.put(
					taskOrganizerDatabaseHelper.Consumed_Time_Category,
					task.category);
			_initialValues.put(taskOrganizerDatabaseHelper.Consumed_Time_Value,
					task.processingtimefact);
			String where = taskOrganizerDatabaseHelper.Consumed_Time_Task + "="
					+ task.idtask;
			long rowID = tasksDB.update(
					taskOrganizerDatabaseHelper.Consumed_Time_Table,
					_initialValues, where, null);
		} else {
			ContentValues _initialValues = new ContentValues();
			_initialValues.put(taskOrganizerDatabaseHelper.Consumed_Time_Task,
					task.idtask);
			_initialValues.put(taskOrganizerDatabaseHelper.Consumed_Time_Date,
					(new Date()).getTime());
			_initialValues.put(
					taskOrganizerDatabaseHelper.Consumed_Time_Category,
					task.category);
			_initialValues.put(taskOrganizerDatabaseHelper.Consumed_Time_Value,
					task.processingtimefact);
			long rowID = tasksDB.insert(
					taskOrganizerDatabaseHelper.Consumed_Time_Table, "item",
					_initialValues);
		}
		tasksDB.close();
	}

	public static void ReadEventsFromCalendar(Activity act, String calendar_id) {

		taskOrganizerDatabaseHelper dbHelper = new taskOrganizerDatabaseHelper(
				act, taskOrganizerDatabaseHelper.DATABASE_NAME, null,
				taskOrganizerDatabaseHelper.DATABASE_VERSION);
		SQLiteDatabase tasksDB = dbHelper.getWritableDatabase();

		Cursor c = queryTaskWithIdEvent(act, tasksDB);

		Map<Long, Long> IdEventMap = new LinkedHashMap<Long, Long>();

		if (c.moveToFirst()) {
			Long idevent = (long) 0;
			Long id = (long) 0;
			do {
				idevent = c.getLong(c.getColumnIndex("_idevent"));
				if (idevent > 0) {
					id = c.getLong(c.getColumnIndex("_id"));
					IdEventMap.put(idevent, id);
				}
			} while (c.moveToNext());
		}

		// String[] projection = new String[] { "_id", "name" };
		//Uri events = Uri.parse(getCalendarUriBase(act) + "events");

		Uri events;
		int version = Integer.valueOf(android.os.Build.VERSION.SDK);
		if (version < 14) {
			try {
				events = Uri.parse(getCalendarUriBase(act) + "events");
			} catch (NoClassDefFoundError e) {
				Toast.makeText(act, act.getString(R.string.from_code88),Toast.LENGTH_SHORT).show();
                return;
			}
		} else {
			events = Events.CONTENT_URI;
		}
		
		

		Cursor c_events = act.managedQuery(events, null, "calendar_id="
				+ calendar_id, null, null);

		if (c_events == null) {
			Toast.makeText(act, act.getString(R.string.from_code21),Toast.LENGTH_SHORT).show();
			return;
		}

		//if(true) return;
		
		Long idevent;
		Task newTask;

		if (c_events.moveToFirst()) {

			do {
				idevent = c_events.getLong(c_events.getColumnIndex("_id"));
				if (IdEventMap.containsKey(idevent)) {
					continue;
				}
				// add task
				newTask = new Task(c_events.getString(c_events
						.getColumnIndex("title")));
				newTask.idevent = idevent;
				newTask.date = new Date(c_events.getLong(c_events
						.getColumnIndex("dtstart")));
				newTask.datefrom = new Date(c_events.getLong(c_events
						.getColumnIndex("dtstart")));
				newTask.dateto = new Date(c_events.getLong(c_events
						.getColumnIndex("dtend")));

				if (c_events.getInt(c_events.getColumnIndex("eventStatus")) == 2) {
					newTask.status = 2;
				}

				newTask.description = c_events.getString(c_events
						.getColumnIndex("title"));

				ContentValues values = newTask.ReturnContentValues();
				values.put(taskOrganizerDatabaseHelper.KEY_IDEVENT,
						newTask.idevent);
				long rowID = tasksDB
						.insert(taskOrganizerDatabaseHelper.Tasks_TABLE,
								"item", values);

				values = new ContentValues();
				values.put(taskOrganizerDatabaseHelper.History_task_id, rowID);
				values.put(taskOrganizerDatabaseHelper.History_NAME,
						"Downloaded from calendar. idevent = "
								+ newTask.idevent);
				long rowID2 = tasksDB.insert(
						taskOrganizerDatabaseHelper.History_Table, "item",
						values);
				// break;
			} while (c_events.moveToNext());
		}

		c.close();
		c_events.close();
	}

	public static final void WriteTasksToCalendar(Activity act) {

		String[] projection = new String[] { "_id", "name" };

		Uri calendars;  
		int version = Integer.valueOf(android.os.Build.VERSION.SDK);
		if (version < 14) {
			calendars = Uri.parse(getCalendarUriBase(act) + "calendars");
		}
		else
		{
			calendars = Calendars.CONTENT_URI;
		}
		
		
		// Cursor c_calendars = act.managedQuery(calendars, projection,
		// "selected=1", null, null);
		Cursor c_calendars = act.managedQuery(calendars, projection, null,
				null, null);
		String calId = "";

		if (c_calendars == null) {
			Toast.makeText(act, act.getString(R.string.from_code21),
					Toast.LENGTH_SHORT).show();
			return;
		}

		if (c_calendars.moveToFirst()) {
			calId = c_calendars.getString(c_calendars.getColumnIndex("_id"));
		} else {
			Toast.makeText(act, act.getString(R.string.from_code21),
					Toast.LENGTH_SHORT).show();
			return;
		}

		taskOrganizerDatabaseHelper dbHelper = new taskOrganizerDatabaseHelper(
				act, taskOrganizerDatabaseHelper.DATABASE_NAME, null,
				taskOrganizerDatabaseHelper.DATABASE_VERSION);
		SQLiteDatabase tasksDB = dbHelper.getWritableDatabase();

		Cursor c = queryTask(act, tasksDB, true);

		//Uri eventsUri = Uri.parse(getCalendarUriBase(act) + "events");
		
		Uri events;
		//int version = Integer.valueOf(android.os.Build.VERSION.SDK);
		if (version < 14) {
			try {
				events = Uri.parse(getCalendarUriBase(act) + "events");
			} catch (NoClassDefFoundError e) {
				Toast.makeText(act, act.getString(R.string.from_code88),Toast.LENGTH_SHORT).show(); 
                return;
			}
		} else {
			events = Events.CONTENT_URI;
		}
		
		
		ContentValues event;

		if (c.moveToFirst()) {

			do {

				if (c.getLong(c
						.getColumnIndex(taskOrganizerDatabaseHelper.KEY_IDEVENT)) == 0) {
					event = new ContentValues();
					event.put(Events.CALENDAR_ID, calId);
					event.put(
							Events.TITLE,
							c.getString(c
									.getColumnIndex(taskOrganizerDatabaseHelper.KEY_NAME)));
					event.put(
							Events.DESCRIPTION,
							c.getString(c
									.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DESCRIPTION)));
					long startTime = c
							.getLong(c
									.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATE));

					if (startTime == 0) {
						continue;
					}
					startTime += 24 * 3600 * 1000;// else it will be -1 day

					long endTime = c
							.getLong(c
									.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATETO));
					if (endTime < startTime) {
						endTime = startTime;
					}
					event.put(Events.DTSTART, startTime);
					event.put(Events.DTEND, endTime);
					event.put(Events.ALL_DAY, 1); // 0 for false, 1 for true
					event.put(Events.EVENT_TIMEZONE, TimeZone.getDefault()
							.getID());

					/*
					 * if
					 * (c.getInt(c.getColumnIndex(taskOrganizerDatabaseHelper.
					 * KEY_STATUS))!=2) { event.put("eventStatus",
					 * 1);//tentative (0), confirmed (1) or canceled (2): } else
					 * { event.put("eventStatus", 2);//tentative (0), confirmed
					 * (1) or canceled (2): }
					 * 
					 * event.put("visibility", 0); event.put("transparency", 0);
					 * event.put("hasAlarm", 0); // 0 for false, 1 for true
					 */
					
					//if (true) break;

					Uri urlevent = act.getContentResolver().insert(events, event);
					// Uri urlevent = act.getContentResolver().insert(eventsUri,
					// event);
					/*
					 * String _temp = urlevent.toString();
					 * 
					 * Long idevent = (long)0;
					 * 
					 * int index = _temp.lastIndexOf("/"); try { idevent =
					 * Long.valueOf(_temp.substring(index+1)); }
					 * catch(NumberFormatException e) { continue; }
					 */
					long idevent = Long
							.parseLong(urlevent.getLastPathSegment());

					Long id = c.getLong(c.getColumnIndex("_id"));

					String where = taskOrganizerDatabaseHelper.KEY_ID + "="
							+ id;

					// break;

					ContentValues values = new ContentValues();
					values.put(taskOrganizerDatabaseHelper.KEY_IDEVENT, idevent);
					long rowID = tasksDB.update(
							taskOrganizerDatabaseHelper.Tasks_TABLE, values,
							where, null);

					values = new ContentValues();
					values.put(taskOrganizerDatabaseHelper.History_task_id, id);
					values.put(taskOrganizerDatabaseHelper.History_NAME,
							act.getString(R.string.from_code22)
									+ ". idevent = " + idevent);
					long rowID2 = tasksDB.insert(
							taskOrganizerDatabaseHelper.History_Table, "item",
							values);
					// break;
				}
			} while (c.moveToNext());
		}

	}

	public static Cursor queryTaskWithIdEvent(Activity act,
			SQLiteDatabase tasksDB) {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String SelectQuery = "SELECT tasks."
				+ taskOrganizerDatabaseHelper.KEY_ID + " as _id,"
				+ taskOrganizerDatabaseHelper.KEY_IDEVENT + " as _idevent"
				+ " FROM " + taskOrganizerDatabaseHelper.Tasks_TABLE;

		// SelectQuery
		// +=" WHERE tasks."+taskOrganizerDatabaseHelper.KEY_IDEVENT+" <> NULL AND tasks."+taskOrganizerDatabaseHelper.KEY_IDEVENT+">0";

		Cursor c = tasksDB.rawQuery(SelectQuery, null);
		return c;
	}

	public static Cursor queryTask(Activity act, SQLiteDatabase tasksDB,
			boolean notInCalendar) {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String SelectQuery = "SELECT tasks."
				+ taskOrganizerDatabaseHelper.KEY_ID + " as _id,"
				+ taskOrganizerDatabaseHelper.KEY_IDEVENT + ","
				+ taskOrganizerDatabaseHelper.KEY_DATE + ","
				+ taskOrganizerDatabaseHelper.KEY_CODE + ","
				+ taskOrganizerDatabaseHelper.KEY_PARENT + ",tasks."
				+ taskOrganizerDatabaseHelper.KEY_NAME + ","
				+ taskOrganizerDatabaseHelper.KEY_RESPONSIBLE + ","
				+ taskOrganizerDatabaseHelper.KEY_EXECUTOR + ","
				+ taskOrganizerDatabaseHelper.KEY_DESCRIPTION + ","
				+ taskOrganizerDatabaseHelper.KEY_PROCESSINGTIME + ","
				+ taskOrganizerDatabaseHelper.KEY_DUEDATE + ","
				+ taskOrganizerDatabaseHelper.KEY_RELEASEDATE + ","
				+ taskOrganizerDatabaseHelper.KEY_PROCESSINGTIMEFACT + ","
				+ taskOrganizerDatabaseHelper.KEY_PRIORITY + ","
				+ taskOrganizerDatabaseHelper.KEY_DATEFROM + ","
				+ taskOrganizerDatabaseHelper.KEY_DATETO + ","
				+ taskOrganizerDatabaseHelper.KEY_DATEFROMFACT + ","
				+ taskOrganizerDatabaseHelper.KEY_DATETOFACT + ","
				+ taskOrganizerDatabaseHelper.KEY_PERCENTOFCOMPLETION + ","
				+ taskOrganizerDatabaseHelper.KEY_CATEGORY + ","
				+ taskOrganizerDatabaseHelper.KEY_STATUS + " FROM "
				+ taskOrganizerDatabaseHelper.Tasks_TABLE;

		if (notInCalendar) {
			// SelectQuery
			// +=" where tasks."+taskOrganizerDatabaseHelper.KEY_STATUS+" <> 4 AND tasks."+taskOrganizerDatabaseHelper.KEY_IDEVENT+" = NULL";
		}
		SelectQuery += " where tasks." + taskOrganizerDatabaseHelper.KEY_STATUS
				+ " <> 4";

		Cursor c = tasksDB.rawQuery(SelectQuery, null);
		return c;
	}

	public static Cursor queryConsumedTime(Activity act, SQLiteDatabase tasksDB) {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String SelectQuery = "SELECT "
				+ taskOrganizerDatabaseHelper.Consumed_Time_Table + "."
				+ taskOrganizerDatabaseHelper.Consumed_Time_ID + " as _id,"
				+ taskOrganizerDatabaseHelper.Consumed_Time_Table + "."
				+ taskOrganizerDatabaseHelper.Consumed_Time_Category + ","
				+ taskOrganizerDatabaseHelper.Consumed_Time_Table + "."
				+ taskOrganizerDatabaseHelper.Consumed_Time_Comment + ","
				+ taskOrganizerDatabaseHelper.Consumed_Time_Table + "."
				+ taskOrganizerDatabaseHelper.Consumed_Time_Date + ","
				+ taskOrganizerDatabaseHelper.Consumed_Time_Table + "."
				+ taskOrganizerDatabaseHelper.Consumed_Time_Task + ","
				+ taskOrganizerDatabaseHelper.Consumed_Time_Table + "."
				+ taskOrganizerDatabaseHelper.Consumed_Time_Value + " FROM "
				+ taskOrganizerDatabaseHelper.Consumed_Time_Table;

		Cursor c = tasksDB.rawQuery(SelectQuery, null);
		return c;
	}

	private static String getCalendarUriBase(Activity act) {

		//Uri uri = Calendars.CONTENT_URI;   
		//Events.
		String calendarUriBase = null;
		Uri calendars = Uri.parse("content://calendar/calendars");
		Cursor managedCursor = null;
		try {
			managedCursor = act.managedQuery(calendars, null, null, null, null);
		} catch (Exception e) {
		}
		if (managedCursor != null) {
			calendarUriBase = "content://calendar/";
		} else {
			calendars = Uri.parse("content://com.android.calendar/calendars");
			try {
				managedCursor = act.managedQuery(calendars, null, null, null,
						null);
			} catch (Exception e) {
			}
			if (managedCursor != null) {
				calendarUriBase = "content://com.android.calendar/";
			}
		}
		return calendarUriBase;
	}

	// ---------------end calendar---------------------------

	// ---------------EXPORT IMPORT---------------------------

	public static final void ExportToXML(Activity act) {
		taskOrganizerDatabaseHelper dbHelper = new taskOrganizerDatabaseHelper(
				act, taskOrganizerDatabaseHelper.DATABASE_NAME, null,
				taskOrganizerDatabaseHelper.DATABASE_VERSION);
		SQLiteDatabase tasksDB = dbHelper.getWritableDatabase();

		Document doc = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {

			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.newDocument();
		} catch (ParserConfigurationException e) {
			Log.e("Error: ", e.getMessage());
			return;
		}

		Element RootElement = doc.createElement("Tasks");
		doc.appendChild(RootElement);

		Cursor c = queryTask(act, tasksDB, false);

		Task _task;
		if (c.moveToFirst()) {
			do {
				_task = new Task(c);
				Element child = TaskToXml(_task, doc);
				RootElement.appendChild(child);

			} while (c.moveToNext());
		}
		c.close();

		Cursor c_ct = queryConsumedTime(act, tasksDB);
		if (c_ct.moveToFirst()) {
			do {
				Element child = CTToXml(c_ct, doc);
				RootElement.appendChild(child);
			} while (c_ct.moveToNext());
		}
		c_ct.close();

		Cursor c_status = query_statuses(tasksDB);

		if (c_status.moveToFirst()) {
			do {
				Element child = doc.createElement("status");
				child.setAttribute(
						"name",
						c_status.getString(c_status
								.getColumnIndex(taskOrganizerDatabaseHelper.Status_NAME)));
				child.setAttribute("id",
						"" + c_status.getInt(c_status.getColumnIndex("_id")));
				RootElement.appendChild(child);
			} while (c_status.moveToNext());
		}
		c_status.close();

		Cursor c_categories = query_categories(tasksDB);

		if (c_categories.moveToFirst()) {
			do {
				Element child = doc.createElement("category");
				child.setAttribute(
						"name",
						c_categories.getString(c_categories
								.getColumnIndex(taskOrganizerDatabaseHelper.Status_NAME)));
				child.setAttribute(
						"id",
						""
								+ c_categories.getInt(c_categories
										.getColumnIndex("_id")));
				RootElement.appendChild(child);
			} while (c_categories.moveToNext());
		}
		c_categories.close();

		String FILENAME = "task_organizer_export.xml";
		String xmlstring = getStringFromNode(RootElement);
		xmlstring = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + xmlstring;

		boolean filefound = false;
		FileOutputStream fos = null;
		try {
			File sdCard = Environment.getExternalStorageDirectory();
			File dir = new File(sdCard.getAbsolutePath());
			dir.mkdirs();
			fos = new FileOutputStream(new File(dir, FILENAME));
			// fos = act.openFileOutput(""+dir+FILENAME,
			// Context.MODE_WORLD_READABLE);
			filefound = true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Toast.makeText(
					act,
					act.getString(R.string.from_code23) + " " + FILENAME + " "
							+ act.getString(R.string.from_code24),
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

		try {
			if (!filefound) {
				fos = act.openFileOutput(FILENAME, Context.MODE_WORLD_READABLE);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Toast.makeText(
					act,
					act.getString(R.string.from_code23) + " " + FILENAME + " "
							+ act.getString(R.string.from_code25),
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			return;
		}

		try {
			fos.write(xmlstring.getBytes());
			Toast.makeText(
					act,
					act.getString(R.string.from_code23)
							+ " "
							+ FILENAME
							+ " "
							+ act.getString(R.string.from_code26)
							+ ((filefound) ? "" : (" " + act
									.getString(R.string.from_code27))),
					Toast.LENGTH_SHORT).show();
			fos.close();
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

	public static final void ImportFromXML(Activity act) {

		String FILENAME = "task_organizer_export.xml";
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File(sdCard.getAbsolutePath());
		dir.mkdirs();

		Document doc = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		boolean filefound = false;

		FileInputStream fstream = null;

		try {

			fstream = new FileInputStream(new File(dir, FILENAME));
			filefound = true;
		} catch (Exception FileNotFoundException) {
			Toast.makeText(
					act,
					act.getString(R.string.from_code28) + " " + FILENAME + " "
							+ act.getString(R.string.from_code29),
					Toast.LENGTH_SHORT).show();
		}

		try {

			if (!filefound) {
				fstream = act.openFileInput(FILENAME);
				// InputSource is = new InputSource(fstream);
			}
		} catch (Exception FileNotFoundException) {
			Toast.makeText(
					act,
					act.getString(R.string.from_code28) + " " + FILENAME + " "
							+ act.getString(R.string.from_code30),
					Toast.LENGTH_SHORT).show();
			return;
			// Log.v(logCat, "File not found exception!:" + FILENAME);
		}

		InputSource is = new InputSource(fstream);

		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(is);
		} catch (ParserConfigurationException e) {
			Log.e("Error: ", e.getMessage());
			return;
		} catch (SAXException e) {
			Log.e("Error: ", e.getMessage());
			return;
		} catch (IOException e) {
			Log.e("Error: ", e.getMessage());
			return;
		}

		NodeList nl = doc.getElementsByTagName("task");

		Task _task;

		taskOrganizerDatabaseHelper dbHelper = new taskOrganizerDatabaseHelper(
				act, taskOrganizerDatabaseHelper.DATABASE_NAME, null,
				taskOrganizerDatabaseHelper.DATABASE_VERSION);
		SQLiteDatabase tasksDB = dbHelper.getWritableDatabase();

		for (int i = 0; i < nl.getLength(); i++) {
			Element element = (Element) nl.item(i);
			_task = new Task(element.getAttribute("name"));
			FillTaskByXML(_task, element);
			SaveTask(_task, tasksDB);
		}

		nl = doc.getElementsByTagName("status");
		for (int i = 0; i < nl.getLength(); i++) {
			Element element = (Element) nl.item(i);
			int _id = Integer.valueOf(element.getAttribute("id"));
			String _name = element.getAttribute("name");
			SaveStatus(_id, _name, tasksDB);
		}

		nl = doc.getElementsByTagName("category");
		for (int i = 0; i < nl.getLength(); i++) {
			Element element = (Element) nl.item(i);
			int _id = Integer.valueOf(element.getAttribute("id"));
			String _name = element.getAttribute("name");
			SaveCategory(_id, _name, tasksDB);
		}

		nl = doc.getElementsByTagName("consumed_time");
		for (int i = 0; i < nl.getLength(); i++) {
			Element element = (Element) nl.item(i);
			SaveConsumedTime(element, tasksDB);
		}

		Toast.makeText(
				act,
				act.getString(R.string.from_code31) + " " + FILENAME + " "
						+ act.getString(R.string.from_code32),
				Toast.LENGTH_SHORT).show();
	}

	public static void FillTaskByXML(Task _task, Element element) {
		SimpleDateFormat sdf_yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");

		_task.idtask = Integer.valueOf(element.getAttribute("id"));
		_task.idevent = Long.valueOf(element.getAttribute("idevent"));

		_task.parent = Integer.valueOf(element.getAttribute("parent"));
		_task.processingtime = Integer.valueOf(element
				.getAttribute("processingtime"));
		_task.status = Integer.valueOf(element.getAttribute("status"));
		_task.processingtimefact = Integer.valueOf(element
				.getAttribute("processingtimefact"));
		_task.priority = Integer.valueOf(element.getAttribute("priority"));
		_task.percentofcompletion = Integer.valueOf(element
				.getAttribute("percentofcompletion"));
		_task.category = Integer.valueOf(element.getAttribute("category"));

		_task.code = element.getAttribute("code");
		_task.executor = element.getAttribute("executor");
		_task.responsible = element.getAttribute("responsible");
		_task.description = element.getAttribute("description");

		try {
			if (!element.getAttribute("date").equals("1970-01-01")) {
				_task.date = sdf_yyyyMMdd.parse(element.getAttribute("date"));
			}
			if (!element.getAttribute("duedate").equals("1970-01-01")) {
				_task.duedate = sdf_yyyyMMdd.parse(element
						.getAttribute("duedate"));
			}
			if (!element.getAttribute("releasedate").equals("1970-01-01")) {
				_task.releasedate = sdf_yyyyMMdd.parse(element
						.getAttribute("releasedate"));
			}
			if (!element.getAttribute("datefrom").equals("1970-01-01")) {
				_task.datefrom = sdf_yyyyMMdd.parse(element
						.getAttribute("datefrom"));
			}
			if (!element.getAttribute("dateto").equals("1970-01-01")) {
				_task.dateto = sdf_yyyyMMdd.parse(element
						.getAttribute("dateto"));
			}
			if (!element.getAttribute("datefromfact").equals("1970-01-01")) {
				_task.datefromfact = sdf_yyyyMMdd.parse(element
						.getAttribute("datefromfact"));
			}
			if (!element.getAttribute("datetofact").equals("1970-01-01")) {
				_task.datetofact = sdf_yyyyMMdd.parse(element
						.getAttribute("datetofact"));
			}

		} catch (ParseException e) {
			Log.e("Error: ", e.getMessage());
			return;
		}

	}

	public static void SaveTask(Task _task, SQLiteDatabase tasksDB) {
		SimpleDateFormat sdf_yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String SelectQuery = "SELECT tasks."
				+ taskOrganizerDatabaseHelper.KEY_ID + " as _id" + " FROM "
				+ taskOrganizerDatabaseHelper.Tasks_TABLE + " where tasks."
				+ taskOrganizerDatabaseHelper.KEY_ID + " = " + _task.idtask;
		Cursor c = tasksDB.rawQuery(SelectQuery, null);

		ContentValues values = _task.ReturnContentValues();

		if (c.getCount() == 0) {

			values.put(taskOrganizerDatabaseHelper.KEY_ID, _task.idtask);
			long rowID = tasksDB.insert(
					taskOrganizerDatabaseHelper.Tasks_TABLE, "item", values);

			values = new ContentValues();
			values.put(taskOrganizerDatabaseHelper.History_task_id, rowID);
			values.put(taskOrganizerDatabaseHelper.History_NAME, "Imported "
					+ sdf_yyyyMMdd.format(new Date()));

			rowID = tasksDB.insert(taskOrganizerDatabaseHelper.History_Table,
					"item", values);
		} else {
			String where = taskOrganizerDatabaseHelper.KEY_ID + "="
					+ _task.idtask;
			long rowID = tasksDB.update(
					taskOrganizerDatabaseHelper.Tasks_TABLE, values, where,
					null);

			values = new ContentValues();
			values.put(taskOrganizerDatabaseHelper.History_task_id,
					_task.idtask);
			values.put(taskOrganizerDatabaseHelper.History_NAME,
					"Updated via import " + sdf_yyyyMMdd.format(new Date()));

			rowID = tasksDB.insert(taskOrganizerDatabaseHelper.History_Table,
					"item", values);

		}
	}

	public static void SaveStatus(int _id, String _name, SQLiteDatabase tasksDB) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String SelectQuery = "SELECT " + taskOrganizerDatabaseHelper.Status_ID
				+ " as _id," + taskOrganizerDatabaseHelper.Status_NAME
				+ " as _name" + " FROM "
				+ taskOrganizerDatabaseHelper.Statuses_Table + " where "
				+ taskOrganizerDatabaseHelper.Status_ID + " = " + _id;
		Cursor c = tasksDB.rawQuery(SelectQuery, null);

		ContentValues values;

		if (c.getCount() == 0) {
			values = new ContentValues();
			values.put(taskOrganizerDatabaseHelper.Status_ID, _id);
			values.put(taskOrganizerDatabaseHelper.Status_NAME, _name);
			long rowID = tasksDB.insert(
					taskOrganizerDatabaseHelper.Statuses_Table, "item", values);
		} else {
			c.moveToFirst();
			if (!c.getString(c.getColumnIndex("_name")).equals(_name)) {
				// here we should check the priority
				values = new ContentValues();
				values.put(taskOrganizerDatabaseHelper.Status_NAME, _name);
				String where = taskOrganizerDatabaseHelper.Status_ID + "="
						+ _id;
				long rowID = tasksDB.update(
						taskOrganizerDatabaseHelper.Statuses_Table, values,
						where, null);
			}

		}
	}

	public static void SaveCategory(int _id, String _name,
			SQLiteDatabase tasksDB) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String SelectQuery = "SELECT "
				+ taskOrganizerDatabaseHelper.Categories_ID + " as _id,"
				+ taskOrganizerDatabaseHelper.Categories_NAME + " as _name"
				+ " FROM " + taskOrganizerDatabaseHelper.Categories_Table
				+ " where " + taskOrganizerDatabaseHelper.Categories_ID + " = "
				+ _id;
		Cursor c = tasksDB.rawQuery(SelectQuery, null);

		ContentValues values;

		if (c.getCount() == 0) {
			values = new ContentValues();
			values.put(taskOrganizerDatabaseHelper.Categories_ID, _id);
			values.put(taskOrganizerDatabaseHelper.Categories_NAME, _name);
			long rowID = tasksDB.insert(
					taskOrganizerDatabaseHelper.Categories_Table, "item",
					values);
		} else {
			c.moveToFirst();
			if (!c.getString(c.getColumnIndex("_name")).equals(_name)) {
				// here we should check the priority
				values = new ContentValues();
				values.put(taskOrganizerDatabaseHelper.Categories_NAME, _name);
				String where = taskOrganizerDatabaseHelper.Categories_ID + "="
						+ _id;
				long rowID = tasksDB.update(
						taskOrganizerDatabaseHelper.Categories_Table, values,
						where, null);
			}

		}
	}

	public static void SaveConsumedTime(Element element, SQLiteDatabase tasksDB) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		SimpleDateFormat sdf_yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		try {
			date = sdf_yyyyMMdd.parse(element.getAttribute("date"));
		} catch (ParseException e) {
			Log.e("Error: ", e.getMessage());
			return;
		}
		String comment = element.getAttribute("comment");
		int value = Integer.valueOf(element.getAttribute("value"));
		int idtask = Integer.valueOf(element.getAttribute("idtask"));
		int category = Integer.valueOf(element.getAttribute("category"));
		int _id = Integer.valueOf(element.getAttribute("id"));
		// String _name = element.getAttribute("name");

		String SelectQuery = "SELECT "
				+ taskOrganizerDatabaseHelper.Consumed_Time_ID + " as _id"
				+ " FROM " + taskOrganizerDatabaseHelper.Consumed_Time_Table
				+ " where " + taskOrganizerDatabaseHelper.Consumed_Time_ID
				+ " = " + _id;
		Cursor c = tasksDB.rawQuery(SelectQuery, null);

		ContentValues values = new ContentValues();
		values.put(taskOrganizerDatabaseHelper.Consumed_Time_Date,
				date.getTime());
		values.put(taskOrganizerDatabaseHelper.Consumed_Time_Value, value);
		values.put(taskOrganizerDatabaseHelper.Consumed_Time_Comment, comment);
		values.put(taskOrganizerDatabaseHelper.Consumed_Time_Category, category);
		values.put(taskOrganizerDatabaseHelper.Consumed_Time_Task, idtask);

		if (c.getCount() > 0) {
			String where = taskOrganizerDatabaseHelper.Consumed_Time_ID + "="
					+ _id;
			long rowID = tasksDB.update(
					taskOrganizerDatabaseHelper.Consumed_Time_Table, values,
					where, null);
			// long rowID =
			// tasksDB.insert(taskOrganizerDatabaseHelper.Tasks_TABLE, "item",
			// values);
		} else {
			long rowID = tasksDB.insert(
					taskOrganizerDatabaseHelper.Consumed_Time_Table, "item",
					values);
		}
	}

	public static Element CTToXml(Cursor c, Document doc) {

		SimpleDateFormat sdf_yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
		Element child = doc.createElement("consumed_time");
		Date date = new Date(
				c.getLong(c
						.getColumnIndex(taskOrganizerDatabaseHelper.Consumed_Time_Date)));

		child.setAttribute("id", "" + c.getInt(c.getColumnIndex("_id")));
		child.setAttribute(
				"idtask",
				""
						+ c.getInt(c
								.getColumnIndex(taskOrganizerDatabaseHelper.Consumed_Time_Task)));
		child.setAttribute(
				"value",
				""
						+ c.getInt(c
								.getColumnIndex(taskOrganizerDatabaseHelper.Consumed_Time_Value)));
		child.setAttribute(
				"category",
				""
						+ c.getInt(c
								.getColumnIndex(taskOrganizerDatabaseHelper.Consumed_Time_Category)));
		child.setAttribute("date", sdf_yyyyMMdd.format(date));
		child.setAttribute(
				"comment",
				""
						+ c.getString(c
								.getColumnIndex(taskOrganizerDatabaseHelper.Consumed_Time_Comment)));

		return child;
	}

	public static Element TaskToXml(Task _task, Document doc) {

		SimpleDateFormat sdf_yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");

		Element child = doc.createElement("task");
		child.setAttribute("name", _task.name);
		// child.setAttribute("name", "third2");

		child.setAttribute("code", _task.code);
		child.setAttribute("parent", "" + _task.parent);
		child.setAttribute("responsible", _task.responsible);
		child.setAttribute("executor", _task.executor);
		child.setAttribute("description", _task.description);
		child.setAttribute("processingtime", "" + _task.processingtime);
		child.setAttribute("status", "" + _task.status);

		child.setAttribute("processingtimefact", "" + _task.processingtimefact);
		child.setAttribute("priority", "" + _task.priority);
		child.setAttribute("percentofcompletion", ""
				+ _task.percentofcompletion);
		child.setAttribute("category", "" + _task.category);
		child.setAttribute("idevent", "" + _task.idevent);
		child.setAttribute("id", "" + _task.idtask);

		child.setAttribute("date", sdf_yyyyMMdd.format(_task.date));
		child.setAttribute("duedate", sdf_yyyyMMdd.format(_task.duedate));
		child.setAttribute("releasedate",
				sdf_yyyyMMdd.format(_task.releasedate));
		child.setAttribute("datefrom", sdf_yyyyMMdd.format(_task.datefrom));
		child.setAttribute("dateto", sdf_yyyyMMdd.format(_task.dateto));
		child.setAttribute("datefromfact",
				sdf_yyyyMMdd.format(_task.datefromfact));
		child.setAttribute("datetofact", sdf_yyyyMMdd.format(_task.datetofact));

		return child;
	}

	public static String getStringFromNode(Element root) {

		StringBuilder result = new StringBuilder();

		if (root.getNodeType() == 3)
			result.append(root.getNodeValue());
		else {
			if (root.getNodeType() != 9) {
				StringBuffer attrs = new StringBuffer();
				for (int k = 0; k < root.getAttributes().getLength(); ++k) {
					attrs.append(" ")
							.append(root.getAttributes().item(k).getNodeName())
							.append("=\"")
							.append(root.getAttributes().item(k).getNodeValue())
							.append("\" ");
				}
				result.append("<").append(root.getNodeName()).append(" ")
						.append(attrs).append(">");
			} else {
				result.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			}

			NodeList nodes = root.getChildNodes();
			for (int i = 0, j = nodes.getLength(); i < j; i++) {
				Element node = (Element) nodes.item(i);
				result.append(getStringFromNode(node));
			}

			if (root.getNodeType() != 9) {
				result.append("</").append(root.getNodeName()).append(">");
			}
		}
		return result.toString();
	}

	public static Cursor query_statuses(SQLiteDatabase tasksDB) {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String SelectQuery = "SELECT " + taskOrganizerDatabaseHelper.Status_ID
				+ " as _id," + taskOrganizerDatabaseHelper.Status_NAME
				+ " as name" + " FROM "
				+ taskOrganizerDatabaseHelper.Statuses_Table;

		Cursor _c = tasksDB.rawQuery(SelectQuery, null);
		return _c;
	}

	public static Cursor query_categories(SQLiteDatabase tasksDB) {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String SelectQuery = "SELECT "
				+ taskOrganizerDatabaseHelper.Categories_ID + " as _id,"
				+ taskOrganizerDatabaseHelper.Categories_NAME + " as name"
				+ " FROM " + taskOrganizerDatabaseHelper.Categories_Table;

		Cursor _c = tasksDB.rawQuery(SelectQuery, null);
		return _c;
	}

	// ---------------SMS EMAIL---------------------------

	public static void PerformTaskMessage(Context context, String PhoneNumber,
			String message, boolean isemail) {
		String _temp = message;
		int status = -1;
		String name = "";
		Date duedate = new Date(0);
		SimpleDateFormat sdf_yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");

		int index = _temp.indexOf("[code=", 0);
		if (index == -1) {
			return;
		}
		int index2 = _temp.indexOf("]", index);
		String code = _temp.substring(index + 6, index2);

		index = _temp.indexOf("[name=", 0);
		if (index == -1) {
			return;
		}
		index2 = _temp.indexOf("]", index);
		name = _temp.substring(index + 6, index2);

		index = _temp.indexOf("[duedate=", 0);
		if (index != -1) {
			index2 = _temp.indexOf("]", index);
			String duedatestring = _temp.substring(index + 9, index2);
			try {
				duedate = sdf_yyyyMMdd.parse(duedatestring);
			} catch (ParseException e) {
				Log.e("Error: ", e.getMessage());
				return;
			}
		}

		index = _temp.indexOf("[status=", 0);
		if (index != -1) {
			index2 = _temp.indexOf("(", index);
			if (index2 != -1) {
				try {
					status = Integer
							.valueOf(_temp.substring(index + 8, index2));
				} catch (NumberFormatException e) {
					status = 1;
				}

			}
		}

		taskOrganizerDatabaseHelper dbHelper = new taskOrganizerDatabaseHelper(
				context, taskOrganizerDatabaseHelper.DATABASE_NAME, null,
				taskOrganizerDatabaseHelper.DATABASE_VERSION);
		SQLiteDatabase tasksDB = dbHelper.getWritableDatabase();

		boolean taskExists = true;

		int taskid = 0;

		Cursor c = query(code, true, tasksDB);
		if (c.getCount() == 0) {
			try {
				int x = Integer.valueOf(code);
				c = query("" + x, false, tasksDB);
				if (c.getCount() == 0) {
					taskExists = false;
				}
			} catch (NumberFormatException e) {
				taskExists = false;
			}
		}

		if (taskExists) {
			c.moveToFirst();
			taskid = c.getInt(c.getColumnIndex("_id"));
			c.close();
		}

		String CustomContent = "";

		if (!taskExists) {
			// new task
			Task newTask = new Task(name);
			if (status != -1) {
				newTask.status = status;
			}

			newTask.responsible = PhoneNumber;
			newTask.code = code;
			newTask.duedate = duedate;

			if (!isemail) {
				Uri uri = Uri.withAppendedPath(
						ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
						Uri.encode(PhoneNumber));
				Long id_contact;

				ContentResolver contentResolver = context.getContentResolver();
				Cursor contactLookup = contentResolver.query(uri, new String[] {
						BaseColumns._ID,
						ContactsContract.PhoneLookup.DISPLAY_NAME }, null,
						null, null);

				try {
					if (contactLookup != null && contactLookup.getCount() > 0) {
						if (contactLookup.moveToFirst()) {
							id_contact = contactLookup.getLong(contactLookup
									.getColumnIndex(ContactsContract.Data._ID));
							newTask.responsible = "content://contacts/people/"
									+ id_contact;
						}
						// String contactId =
						// contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
					}
				} finally {
					if (contactLookup != null) {
						contactLookup.close();
					}
				}
			} else// look for email
			{
				Uri uri = Uri.withAppendedPath(Email.CONTENT_LOOKUP_URI,
						Uri.encode(PhoneNumber));
				String id_contact, id_contact0;

				ContentResolver contentResolver = context.getContentResolver();
				// Cursor contactLookup = contentResolver.query(uri, new
				// String[] {Email.CONTACT_ID,
				// Email.DISPLAY_NAME }, null, null, null);
				Cursor contactLookup = contentResolver.query(uri, null, null,
						null, null);

				try {
					if (contactLookup != null && contactLookup.getCount() > 0) {
						if (contactLookup.moveToFirst()) {
							/*
							 * String toTest = ""; for (int
							 * i=0;i<contactLookup.getColumnCount()-1;i++) {
							 * toTest += " ("+i+")"+contactLookup.getString(i);
							 * } toTest = toTest;
							 */
							// id_contact =
							// contactLookup.getLong(contactLookup.getColumnIndex(Email.CONTACT_ID));
							// contactLookup.getColumnIndex("lookup")
							// String[] a = contactLookup.getColumnNames();
							id_contact = contactLookup.getString(contactLookup
									.getColumnIndex("contact_id"));// 3
							id_contact0 = contactLookup.getString(contactLookup
									.getColumnIndex("lookup"));// 4 or 5
							newTask.responsible = "content://com.android.contacts/contacts/lookup/"
									+ id_contact0 + "/" + id_contact;
							// newTask.responsible =
							// "content://contacts/people/"+id_contact;
						}

						// String contactId =
						// contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
					}
				} finally {
					if (contactLookup != null) {
						contactLookup.close();
					}
				}
			}

			ContentValues values = newTask.ReturnContentValues();
			values.put(taskOrganizerDatabaseHelper.KEY_DATEMODIFIED, (new Date()).getTime());
			long rowID = tasksDB.insert(
					taskOrganizerDatabaseHelper.Tasks_TABLE, "item", values);

			taskid = Integer.valueOf("" + rowID);

			values = new ContentValues();
			values.put(taskOrganizerDatabaseHelper.History_task_id, rowID);
			values.put(taskOrganizerDatabaseHelper.History_NAME,
					"Incoming message from " + PhoneNumber + " " + message);
			long rowID2 = tasksDB.insert(
					taskOrganizerDatabaseHelper.History_Table, "item", values);

			CustomContent = context.getString(R.string.from_code33) + name;
		} else if (status != -1 && taskExists) {
			String where = taskOrganizerDatabaseHelper.KEY_ID + "=" + taskid;
			ContentValues values = new ContentValues();
			values.put(taskOrganizerDatabaseHelper.KEY_STATUS, status);
			
			values.put(taskOrganizerDatabaseHelper.KEY_DATEMODIFIED, (new Date()).getTime());
			long rowID = tasksDB.update(
					taskOrganizerDatabaseHelper.Tasks_TABLE, values, where,
					null);

			values = new ContentValues();
			values.put(taskOrganizerDatabaseHelper.History_task_id, taskid);
			values.put(taskOrganizerDatabaseHelper.History_NAME,
					"Incoming message from " + PhoneNumber + " " + message);
			long rowID2 = tasksDB.insert(
					taskOrganizerDatabaseHelper.History_Table, "item", values);

			CustomContent = context.getString(R.string.from_code34) + name
					+ context.getString(R.string.from_code35);
		} else if (status == -1 && taskExists) {
			Toast.makeText(context, context.getString(R.string.from_code36),
					Toast.LENGTH_SHORT).show();
			return;
		}

		// String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification;
				//= new Notification(R.drawable.ic_menu_send,
				//CustomContent, System.currentTimeMillis());

		Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.ic_menu_send);
		Intent intent = new Intent(context, taskForm.class);
		intent.putExtra("_id", Long.valueOf(taskid));
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				intent, 0);

		Notification.Builder builder = new Notification.Builder(context);

		builder.setAutoCancel(false);
		builder.setTicker("Task Organizer event");
		builder.setContentTitle("Task Organizer event");
		builder.setContentText(CustomContent);
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setLargeIcon(icon);
		builder.setContentIntent(pendingIntent);
		builder.setOngoing(true);  //API level 16
		builder.setNumber(100);
		builder.build();

		// notification.flags = notification.flags |
		// Notification.FLAG_ONGOING_EVENT;
		// notification.contentView = new RemoteViews(this.getPackageName(),
		// R.layout.my_status_window_layout);

		// notification.contentIntent = pendingIntent;

		notification = builder.getNotification();
		mNotificationManager.notify(taskid, notification);
	}

	public static Cursor query(String code, boolean isCode,
			SQLiteDatabase tasksDB) {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String SelectQuery = "SELECT tasks."
				+ taskOrganizerDatabaseHelper.KEY_ID + " as _id,"
				+ taskOrganizerDatabaseHelper.KEY_DATE + ","
				+ taskOrganizerDatabaseHelper.KEY_CODE + ","
				+ taskOrganizerDatabaseHelper.KEY_PARENT + ",tasks."
				+ taskOrganizerDatabaseHelper.KEY_NAME + ","
				+ taskOrganizerDatabaseHelper.KEY_RESPONSIBLE + ","
				+ taskOrganizerDatabaseHelper.KEY_EXECUTOR + ","
				+ taskOrganizerDatabaseHelper.KEY_DESCRIPTION + ","
				+ taskOrganizerDatabaseHelper.KEY_PROCESSINGTIME + ","
				+ taskOrganizerDatabaseHelper.KEY_DUEDATE + ","
				+ taskOrganizerDatabaseHelper.KEY_RELEASEDATE + ","
				+ taskOrganizerDatabaseHelper.KEY_STATUS + ","
				+ taskOrganizerDatabaseHelper.KEY_PROCESSINGTIMEFACT + ","
				+ taskOrganizerDatabaseHelper.KEY_PRIORITY + ","
				+ taskOrganizerDatabaseHelper.KEY_DATEFROM + ","
				+ taskOrganizerDatabaseHelper.KEY_DATETO + ","
				+ taskOrganizerDatabaseHelper.KEY_DATEFROMFACT + ","
				+ taskOrganizerDatabaseHelper.KEY_DATETOFACT + ","
				+ taskOrganizerDatabaseHelper.KEY_PERCENTOFCOMPLETION + ","
				+ taskOrganizerDatabaseHelper.KEY_CATEGORY + " as category,"
				+ taskOrganizerDatabaseHelper.KEY_STATUS + " as status,"
				+ taskOrganizerDatabaseHelper.Statuses_Table + "."
				+ taskOrganizerDatabaseHelper.Status_NAME + " as statusname,"
				+ taskOrganizerDatabaseHelper.Categories_Table + "."
				+ taskOrganizerDatabaseHelper.Categories_NAME
				+ " as categoryname" + " FROM "
				+ taskOrganizerDatabaseHelper.Tasks_TABLE + " LEFT JOIN "
				+ taskOrganizerDatabaseHelper.Statuses_Table + " ON "
				+ taskOrganizerDatabaseHelper.Tasks_TABLE + "."
				+ taskOrganizerDatabaseHelper.KEY_STATUS + " = "
				+ taskOrganizerDatabaseHelper.Statuses_Table + "."
				+ taskOrganizerDatabaseHelper.Status_ID + " LEFT JOIN "
				+ taskOrganizerDatabaseHelper.Categories_Table + " ON "
				+ taskOrganizerDatabaseHelper.Tasks_TABLE + "."
				+ taskOrganizerDatabaseHelper.KEY_CATEGORY + " = "
				+ taskOrganizerDatabaseHelper.Categories_Table + "."
				+ taskOrganizerDatabaseHelper.Categories_ID;

		if (isCode) {
			SelectQuery += " where tasks."
					+ taskOrganizerDatabaseHelper.KEY_CODE + " = '" + code
					+ "'";
		} else {
			SelectQuery += " where tasks." + taskOrganizerDatabaseHelper.KEY_ID
					+ " = " + code;
		}

		Cursor c = tasksDB.rawQuery(SelectQuery, null);
		return c;
	}

	public static String GetContactNameFromString(Activity act, String contact) {
		String contactname = "";

		if (contact.startsWith("content:")) {
			Uri contactData = Uri.parse(contact);

			// Uri lookupUri =
			// Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI,
			// contact);

			/*
			 * String[] projection = new String[] {
			 * ContactsContract.Contacts.DISPLAY_NAME,
			 * ContactsContract.CommonDataKinds.Phone
			 * ContactsContract.CommonDataKinds.Email };
			 */

			contactname = act.getString(R.string.from_code37);
			// Cursor c_contact =
			// act.getContentResolver().query(lookupUri,null,null,null,null);
			Cursor c_contact;
			try {
				c_contact = act.managedQuery(contactData, null, null, null,
						null);
				act.stopManagingCursor(c_contact);
			} catch (Exception ex) {
				// System.out.println("Exception arise at the time of read mail");
				ex.printStackTrace();
				return "Contact is not found";
			}

			try {
				if (c_contact.moveToFirst()) {
					contactname = c_contact
							.getString(c_contact
									.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
				} else {
					return contact;
				}
			} finally {
				// contactname = "contact is not found";
				c_contact.close();
			}

			/*
			 * Cursor c_contact = act.managedQuery(contactData, null, null,
			 * null, null); if (c_contact.moveToFirst()) { contactname =
			 * c_contact
			 * .getString(c_contact.getColumnIndexOrThrow(People.NAME)); } else
			 * { String IDforMessage = contact; int pos =
			 * contact.lastIndexOf("/"); if (pos!=-1) { contactname
			 * ="contact with ID = "+contact.substring(pos+1)+" is not found"; }
			 * 
			 * contactname ="contact is not found"; }
			 */
			return contactname;
		} else {
			return contact;
		}

	}

	public static String FindPhoneOfContact(Context context, String tofind) {
		String phoneNumber = "";
		if (tofind.startsWith("content:")) {
			Uri contactData = Uri.parse(tofind);

			ContentResolver cr = context.getContentResolver();
			Cursor c_contact = cr.query(contactData, null, null, null, null);
			c_contact.moveToFirst();
			// phoneNumber =
			// c_contact.getString(c_contact.getColumnIndexOrThrow(People.NUMBER_KEY));

			phoneNumber = "";
			String contactId = c_contact.getString(c_contact
					.getColumnIndex(ContactsContract.Contacts._ID));
			// Get all phone numbers.
			//
			Cursor phones = cr.query(Phone.CONTENT_URI, null, Phone.TYPE
					+ " = " + Phone.TYPE_MOBILE + " AND " + Phone.CONTACT_ID
					+ " = " + contactId, null, null);
			if (phones.getCount() == 0) {
				phones = cr.query(Phone.CONTENT_URI, null, Phone.CONTACT_ID
						+ " = " + contactId, null, null);
			}

			/*
			 * while (phones.moveToNext()) { String number =
			 * phones.getString(phones.getColumnIndex(Phone.NUMBER)); int type =
			 * phones.getInt(phones.getColumnIndex(Phone.TYPE)); phoneNumber =
			 * number; switch (type) { case Phone.TYPE_HOME: if Phone. break;
			 * case Phone.TYPE_MOBILE: phoneNumber = number; break; case
			 * Phone.TYPE_WORK: phoneNumber = number; break; } if
			 * (phoneNumber.length()>0) { break; }
			 */

			// }
			if (phones.moveToFirst()) {
				phoneNumber = phones.getString(phones
						.getColumnIndex(Phone.NUMBER));
			}
			phones.close();
			c_contact.close();

			if (phoneNumber.length() == 0) {
				// ErrorByCreating = true;
				// Toast.makeText(context,
				// "Phone for the contact"+tofind+" is not defined",
				// Toast.LENGTH_LONG).show();
				return "";
			}
		} else {
			phoneNumber = tofind;
		}
		return phoneNumber;
	}

	public static String FindEmailOfContact(Context context, String contact) {
		String email = "";
		if (contact.startsWith("content:")) {
			Uri contactData = Uri.parse(contact);

			ContentResolver cr = context.getContentResolver();
			Cursor c_contact = cr.query(contactData, null, null, null, null);
			if (!c_contact.moveToFirst()) {
				return "";
			}

			email = "";
			String contactId = c_contact.getString(c_contact
					.getColumnIndex(ContactsContract.Contacts._ID));
			// Get all emails.
			//
			Cursor emails = cr.query(Email.CONTENT_URI, null, Email.CONTACT_ID
					+ " = " + contactId, null, null);
			if (emails.moveToFirst()) {
				email = emails.getString(emails.getColumnIndex(Email.ADDRESS));
			}
			emails.close();
			c_contact.close();

			if (email.length() == 0) {
				// Toast.makeText(context,
				// "Phone for the contact"+tofind+" is not defined",
				// Toast.LENGTH_LONG).show();
			}
		} else {
			int pos = contact.indexOf("@");
			if (pos != -1) {
				email = contact;
			}

		}

		return email;

	}

	// ---------------DRAWING---------------------------

	public static Drawable GetDrawable(final int _color) {
		// Button theButton = (Button)findViewById(R.id.thebutton);
		ShapeDrawable.ShaderFactory sf = new ShapeDrawable.ShaderFactory() {
			@Override
			public Shader resize(int width, int height) {

				float[] hsv = new float[3];
				// int color = getColor();
				// _color
				Color.colorToHSV(_color, hsv);
				hsv[2] *= 0.8f; // value component
				final int _color2 = Color.HSVToColor(hsv);
				// Color.alpha(Color.RED),
				LinearGradient lg = new LinearGradient(0, 0, 0, 20, new int[] {
						_color, _color2 }, // substitute the correct colors for
											// these
						new float[] { 0, 1 }, Shader.TileMode.CLAMP);
				return lg;
			}
		};

		Shape rrs = new RoundRectShape(new float[] { 10f, 10f, 10f, 10f, 10f,
				10f, 10f, 10f }, null, null);

		PaintDrawable p = new PaintDrawable();
		p.setShape(rrs);
		p.setShaderFactory(sf);
		return (Drawable) p;
		// theButton.setBackgroundDrawable((Drawable)p);
	}

	public static Drawable GetDrawableNoCorner(final int _color) {
		// Button theButton = (Button)findViewById(R.id.thebutton);
		ShapeDrawable.ShaderFactory sf = new ShapeDrawable.ShaderFactory() {
			@Override
			public Shader resize(int width, int height) {

				float[] hsv = new float[3];
				// int color = getColor();
				// _color
				Color.colorToHSV(_color, hsv);
				hsv[2] *= 0.8f; // value component
				final int _color2 = Color.HSVToColor(hsv);
				// Color.alpha(Color.RED),
				LinearGradient lg = new LinearGradient(0, 0, 0, 20, new int[] {
						_color, _color2 }, // substitute the correct colors for
											// these
						new float[] { 0, 1 }, Shader.TileMode.CLAMP);
				return lg;
			}
		};

		Shape rrs = new RectShape();

		PaintDrawable p = new PaintDrawable();
		p.setShape(rrs);
		p.setShaderFactory(sf);
		return (Drawable) p;
		// theButton.setBackgroundDrawable((Drawable)p);
	}

	// ---------------Toodledo---------------------------
	public static String userid;
	public static String sig = "api50e67bcfaeb7a";
	//public static String key = "9cd2848987f26153e3a11a854a2f7478";//debugging
	public static String appid = "rabotan";
	public static JSONArray jsonArray;
	public static String PREFS_NAME_TOODOLEDO = "Prefs_TOODOLEDO";
	public static long maxmodified = 0;
	
	
	public static long TimeLastConnection = (long)0;
	public static long TimeLastSync = (long)0;
	public static String SessionIDToodledo = "";
	
	public static List<String> listofmodifiedid = new ArrayList<String>();
	public static int numberofchanges = 0;
	
	

	public static boolean isNetworkAvailable(Activity act) {
		ConnectivityManager cm = (ConnectivityManager) act
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		// if no network is available networkInfo will be null
		// otherwise check if we are connected
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}

	public static String md5(String s) 
	{
	    MessageDigest digest;
	    try 
	    {
	        digest = MessageDigest.getInstance("MD5");
	        digest.update(s.getBytes(),0,s.length());
	        String hash = new BigInteger(1, digest.digest()).toString(16);
	        return hash;
	    } 
	    catch (NoSuchAlgorithmException e) 
	    {
	        e.printStackTrace();
	    }
	    return "";
	}
	
	
	public static Cursor queryTaskToodledo(Activity act, SQLiteDatabase tasksDB) {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String SelectQuery = "SELECT tasks."
				+ taskOrganizerDatabaseHelper.KEY_ID + " as _id,"
				+ taskOrganizerDatabaseHelper.KEY_IDTOODLEDO + ","
				+ taskOrganizerDatabaseHelper.KEY_DATEMODIFIED + ","
				+ taskOrganizerDatabaseHelper.KEY_DATE + ","
				+ taskOrganizerDatabaseHelper.KEY_CODE + ","
				+ taskOrganizerDatabaseHelper.KEY_PARENT + ",tasks."
				+ taskOrganizerDatabaseHelper.KEY_NAME + ","
				+ taskOrganizerDatabaseHelper.KEY_RESPONSIBLE + ","
				+ taskOrganizerDatabaseHelper.KEY_EXECUTOR + ","
				+ taskOrganizerDatabaseHelper.KEY_DESCRIPTION + ","
				+ taskOrganizerDatabaseHelper.KEY_PROCESSINGTIME + ","
				+ taskOrganizerDatabaseHelper.KEY_DUEDATE + ","
				+ taskOrganizerDatabaseHelper.KEY_RELEASEDATE + ","
				+ taskOrganizerDatabaseHelper.KEY_PROCESSINGTIMEFACT + ","
				+ taskOrganizerDatabaseHelper.KEY_PRIORITY + ","
				+ taskOrganizerDatabaseHelper.KEY_DATEFROM + ","
				+ taskOrganizerDatabaseHelper.KEY_DATETO + ","
				+ taskOrganizerDatabaseHelper.KEY_DATEFROMFACT + ","
				+ taskOrganizerDatabaseHelper.KEY_DATETOFACT + ","
				+ taskOrganizerDatabaseHelper.KEY_PERCENTOFCOMPLETION + ","
				+ taskOrganizerDatabaseHelper.KEY_CATEGORY + ","
				+ taskOrganizerDatabaseHelper.KEY_STATUS + " FROM "
				+ taskOrganizerDatabaseHelper.Tasks_TABLE;

		SelectQuery += " where tasks."
				+ taskOrganizerDatabaseHelper.KEY_DATEMODIFIED + ">0 OR tasks."
				+ taskOrganizerDatabaseHelper.KEY_IDTOODLEDO + "=0 OR tasks."
				+ taskOrganizerDatabaseHelper.KEY_IDTOODLEDO + " IS NULL";

		Cursor c = tasksDB.rawQuery(SelectQuery, null);
		return c;
	}

	public static boolean RequestJSON(URL url) {
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		

		try {
			HttpGet httpGet = new HttpGet(url.toURI());

			try {
				HttpResponse response = client.execute(httpGet);
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				if (statusCode == 200) {
					HttpEntity entity = response.getEntity();
					InputStream content = entity.getContent();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(content));
					String line;
					while ((line = reader.readLine()) != null) {
						builder.append(line);
					}
				} else {
					// Log.e(ParseJSON.class.toString(),
					// "Failed to download file");
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			String str = builder.toString();
			if (!str.startsWith("["))
			{
				str = "["+str+"]";
			}
			jsonArray = new JSONArray(str);

			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;

	}

	public static JSONObject FillObject(Cursor c,boolean ref)
	{
		JSONObject object = new JSONObject();
		try {
			
			if (!ref) {
				object.put("id", ""+c.getInt(c
						.getColumnIndex(taskOrganizerDatabaseHelper.KEY_IDTOODLEDO)));
			}
			
			object.put(
					"title",""+
					c.getString(c
							.getColumnIndex(taskOrganizerDatabaseHelper.KEY_NAME)));
			if (ref) {
				object.put("ref",""+ c.getInt(c
						.getColumnIndex("_id")));
			}
			/*object.put(
					"folder",""+
					c.getInt(c
							.getColumnIndex(taskOrganizerDatabaseHelper.KEY_CATEGORY)));*/
			
			TimeZone tz = TimeZone.getDefault();
			int differenceGMT = tz.getRawOffset()/1000;
			
			if (c.getLong(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DUEDATE))>0)
			{
				
				object.put(
						"duedate",""+
						(c.getLong(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DUEDATE))/1000+differenceGMT));
				object.put(
						"duetime",""+
						(c.getLong(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DUEDATE))/1000+differenceGMT));

			}
			
			if (c.getLong(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATEFROM))>0)
			{
			object.put(
					"startdate",""+
					(c.getLong(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATEFROM))/1000+differenceGMT));
			object.put(
					"starttime",""+
					(c.getLong(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATEFROM))/1000+differenceGMT));
			}
			
			if (c.getLong(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATEFROM))==0 && c.getLong(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATE))>0)
			{
				object.put(
						"startdate",""+
						(c.getLong(c
								.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATE))/1000+differenceGMT));
				object.put(
						"starttime",""+
						(c.getLong(c
								.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATE))/1000+differenceGMT));
			}
			
			
			int status = c
					.getInt(c
							.getColumnIndex(taskOrganizerDatabaseHelper.KEY_STATUS));
			if (status == 1) {
				object.put("status", ""+2);
			}
			if (status == 2) {
				//object.put("status", ""+0);
				object.put(
						"completed",""+
						(c.getLong(c
								.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATE))/1000+differenceGMT));
			}
			else
			{
				object.put("completed","0");
			}
			
			if (status == 3) {
				object.put("status", ""+9);
			}
			if (status == 5) {
				object.put("status", ""+7);
			}

			if (c.getInt(c
					.getColumnIndex(taskOrganizerDatabaseHelper.KEY_PRIORITY)) > 0) {
				object.put("star", ""+1);
				object.put("priority",""+ 1);
			}
			//object.put("added",""+ (new Date()).getTime()/1000);
			object.put(
					"note",""+
					c.getString(c
							.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DESCRIPTION)));
			object.put(
					"length",""+
					c.getInt(c
							.getColumnIndex(taskOrganizerDatabaseHelper.KEY_PROCESSINGTIME)));
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	
		return object;
	}
	
	public static void ApplyRequestUpTo50Tasks(SQLiteDatabase tasksDB,JSONArray jsonArray0,Activity act) throws MalformedURLException
	{
		JSONObject object;
		String str = jsonArray0.toString();
		//str = str.replace(":", "%3A");
		//str = str.replace(";", "%2C");
		try {
			str = URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		
		URL url = new URL("http://api.toodledo.com/2/tasks/add.php?key="+ SessionIDToodledo+ ";tasks=" +str+ ";fields=status");
		if (!RequestJSON(url)) {
			Toast.makeText(act,
					act.getString(R.string.from_code84),
					Toast.LENGTH_SHORT).show();
			return;
		}

		for (int i = 0; i < jsonArray.length(); i++) {
			try {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				int id = Integer.parseInt(jsonObject.getString("ref"));
				int tooledoid = Integer.parseInt(jsonObject.getString("id"));
				
				listofmodifiedid.add(""+tooledoid);
				
				ContentValues values = new ContentValues();
				values.put(taskOrganizerDatabaseHelper.KEY_IDTOODLEDO, tooledoid); 
				values.put(taskOrganizerDatabaseHelper.KEY_DATEMODIFIED, 0); 
				long rowID = tasksDB.update(taskOrganizerDatabaseHelper.Tasks_TABLE,values,taskOrganizerDatabaseHelper.KEY_ID + "=" + id,null);
				
				
				values = new ContentValues();
				  values.put(taskOrganizerDatabaseHelper.History_task_id, id); values.put(taskOrganizerDatabaseHelper.History_NAME,
				  act.getString(R.string.from_code22)+". tooledoid = "+tooledoid);
				  long rowID2 = tasksDB.insert(taskOrganizerDatabaseHelper.History_Table, "item", values);
				
				  numberofchanges++;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Toast.makeText(act,
						act.getString(R.string.from_code84)+" pos. 2",
						Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}
	}
	
	public static void ApplyRequestUpTo50TasksEdit(SQLiteDatabase tasksDB,JSONArray jsonArray0,Activity act) throws MalformedURLException
	{
		JSONObject object;
		
		String str = jsonArray0.toString();
		try {
			str = URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			Toast.makeText(act,
					act.getString(R.string.from_code84)+" pos. 4",
					Toast.LENGTH_SHORT).show();
			e1.printStackTrace();
		} 
		
		URL url = new URL("http://api.toodledo.com/2/tasks/edit.php?key="+ SessionIDToodledo+ ";tasks=" + str+ ";fields=folder");
		if (!RequestJSON(url)) {
			Toast.makeText(act,
					act.getString(R.string.from_code84)+" pos.5",
					Toast.LENGTH_SHORT).show();
			return;
		}
		
		ContentValues values = new ContentValues();
		values.put(taskOrganizerDatabaseHelper.KEY_DATEMODIFIED, 0); 
		long rowID = tasksDB.update(taskOrganizerDatabaseHelper.Tasks_TABLE,values,taskOrganizerDatabaseHelper.KEY_DATEMODIFIED + ">0",null);
		
	}
		
	public static void ApplyRequestDeleteTasks(SQLiteDatabase tasksDB,String idToDelete,Activity act) throws MalformedURLException
	{
		JSONObject object;
		
		
		String str = "["+idToDelete+"]";
		try {
			str = URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			Toast.makeText(act,
					act.getString(R.string.from_code84)+" pos. 7",
					Toast.LENGTH_SHORT).show();
			e1.printStackTrace();
		} 
		
		
		URL url = new URL("http://api.toodledo.com/2/tasks/delete.php?key="+ SessionIDToodledo+ ";tasks=" + str);
		if (!RequestJSON(url)) {
			Toast.makeText(act,
					act.getString(R.string.from_code84)+" pos.6",
					Toast.LENGTH_SHORT).show();
			return;
		}
		numberofchanges+=jsonArray.length();

		/*for (int i = 0; i < jsonArray.length(); i++) {
			try {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				int id = Integer.parseInt(jsonObject.getString("ref"));
				int tooledoid = Integer.parseInt(jsonObject.getString("id"));
				ContentValues values = new ContentValues();
				values.put(taskOrganizerDatabaseHelper.KEY_IDTOODLEDO, tooledoid); 
				values.put(taskOrganizerDatabaseHelper.KEY_DATEMODIFIED, 0); 
				long rowID = tasksDB.update(taskOrganizerDatabaseHelper.Tasks_TABLE,values,taskOrganizerDatabaseHelper.KEY_ID + "=" + id,null);
				
				
				values = new ContentValues();
				  values.put(taskOrganizerDatabaseHelper.History_task_id, id); values.put(taskOrganizerDatabaseHelper.History_NAME,
				  act.getString(R.string.from_code22)+". tooledoid = "+tooledoid);
				  long rowID2 = tasksDB.insert(taskOrganizerDatabaseHelper.History_Table, "item", values);
				
				  numberofchanges++;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	*/
	}
	
	public static boolean ExportTasks(Activity act)
			throws MalformedURLException {
		taskOrganizerDatabaseHelper dbHelper = new taskOrganizerDatabaseHelper(
				act, taskOrganizerDatabaseHelper.DATABASE_NAME, null,
				taskOrganizerDatabaseHelper.DATABASE_VERSION);
		SQLiteDatabase tasksDB = dbHelper.getWritableDatabase();
		Cursor c = queryTaskToodledo(act, tasksDB);

		JSONArray jsonArray = new JSONArray();
		JSONObject object;
		int counter = 0;
		
		listofmodifiedid.clear();
		//add new tasks
		if (c.moveToFirst()) {
			do {

				if (c.getLong(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_IDTOODLEDO)) == 0)// add
				{
					if (c.getLong(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_STATUS)) == 4)
						continue;
					object = FillObject(c,true);
					jsonArray.put(object);
					counter ++;
					
					//break;//debugging
					
					if (counter==10)
					{
						ApplyRequestUpTo50Tasks(tasksDB,jsonArray,act);
						jsonArray = new JSONArray();
						counter = 0;
					}
				}
			} while (c.moveToNext());
			
			if (counter>0)
			{
				ApplyRequestUpTo50Tasks(tasksDB,jsonArray,act);
			}
		}
		
		//return true;
		
		jsonArray = new JSONArray();
		//delete tasks
		if (c.moveToFirst()) {
			String idToDelete = "";
			do {
				if (c.getLong(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_IDTOODLEDO)) != 0)
				{
					if (c.getLong(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_STATUS)) != 4)
						continue;
					String id = ""+c.getInt(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_IDTOODLEDO));

					listofmodifiedid.add(""+id);
					
					if (idToDelete.length()>0)
					{
						idToDelete+=",\""+id+"\"";
					}
					else
					{
						idToDelete="\""+id+"\"";
					}				
				}
			} while (c.moveToNext());
			
			if (idToDelete.length()>0)
			{
				ApplyRequestDeleteTasks(tasksDB,idToDelete,act);
				ContentValues values = new ContentValues();
				values.put(taskOrganizerDatabaseHelper.KEY_IDTOODLEDO, 0); 
				values.put(taskOrganizerDatabaseHelper.KEY_DATEMODIFIED, 0); 
				long rowID = tasksDB.update(taskOrganizerDatabaseHelper.Tasks_TABLE,values,taskOrganizerDatabaseHelper.KEY_STATUS + "=4",null);
			}
		}
		counter = 0;
		
		jsonArray = new JSONArray();
		//edit tasks
		if (c.moveToFirst()) {
			do {

				if (c.getLong(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_IDTOODLEDO)) != 0)// add
				{
					if (c.getLong(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_STATUS)) == 4)
						continue;
					object = FillObject(c,false);
					jsonArray.put(object);
					counter ++;
					
					listofmodifiedid.add(""+c.getInt(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_IDTOODLEDO)));
					 numberofchanges++;
					if (counter==10)
					{
						ApplyRequestUpTo50TasksEdit(tasksDB,jsonArray,act);
						jsonArray = new JSONArray();
						counter = 0;
					}
				}
			} while (c.moveToNext());
			
			if (counter>0)
			{
				ApplyRequestUpTo50TasksEdit(tasksDB,jsonArray,act);
			}
		}
		
		
		
		return true;
	}

	public static void ToodoledoDeleteTasks(Activity act)
	{
		taskOrganizerDatabaseHelper dbHelper = new taskOrganizerDatabaseHelper(
				act, taskOrganizerDatabaseHelper.DATABASE_NAME, null,
				taskOrganizerDatabaseHelper.DATABASE_VERSION);
		SQLiteDatabase tasksDB = dbHelper.getWritableDatabase();
		
		String listid = "";
		for (int i = 0; i < jsonArray.length(); i++) {
			try {

				JSONObject jsonObject = jsonArray.getJSONObject(i);
				if (jsonObject.has("num"))
				{
					continue;
				}
				
				long modified = jsonObject.getLong("stamp");
				maxmodified = Math.max(modified, maxmodified);
				numberofchanges++;
				
				int id = Integer.parseInt(jsonObject.getString("id"));
				
				if (listid.length()>0)
				{
					listid+=","+id;
				}
				else
				{
					listid=""+id;
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (listid.length()>0)
		{
			ContentValues values = new ContentValues();
			values.put(taskOrganizerDatabaseHelper.KEY_STATUS, 4); 
			long rowID = tasksDB.update(taskOrganizerDatabaseHelper.Tasks_TABLE,values,taskOrganizerDatabaseHelper.KEY_IDTOODLEDO + " IN (" +listid+")",null);
		}
		
	}
	
	public static Bitmap loadBitmap(String path, int orientation, final int targetWidth, final int targetHeight) {
	    Bitmap bitmap = null;
	    try {
	        // First decode with inJustDecodeBounds=true to check dimensions
	        final BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inJustDecodeBounds = true;
	        BitmapFactory.decodeFile(path, options);

	        // Adjust extents
	        int sourceWidth, sourceHeight;
	        if (orientation == 90 || orientation == 270) {
	            sourceWidth = options.outHeight;
	            sourceHeight = options.outWidth;
	        } else {
	            sourceWidth = options.outWidth;
	            sourceHeight = options.outHeight;
	        }

	        // Calculate the maximum required scaling ratio if required and load the bitmap
	        if (sourceWidth > targetWidth || sourceHeight > targetHeight) {
	            float widthRatio = (float)sourceWidth / (float)targetWidth;
	            float heightRatio = (float)sourceHeight / (float)targetHeight;
	            float maxRatio = Math.max(widthRatio, heightRatio);
	            options.inJustDecodeBounds = false;
	            options.inSampleSize = (int)maxRatio;
	            bitmap = BitmapFactory.decodeFile(path, options);
	        } else {
	            bitmap = BitmapFactory.decodeFile(path);
	        }

	        // Rotate the bitmap if required
	        if (orientation > 0) {
	            Matrix matrix = new Matrix();
	            matrix.postRotate(orientation);
	            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
	        }

	        // Re-scale the bitmap if necessary
	        sourceWidth = bitmap.getWidth();
	        sourceHeight = bitmap.getHeight();
	        if (sourceWidth != targetWidth || sourceHeight != targetHeight) {
	            float widthRatio = (float)sourceWidth / (float)targetWidth;
	            float heightRatio = (float)sourceHeight / (float)targetHeight;
	            float maxRatio = Math.max(widthRatio, heightRatio);
	            sourceWidth = (int)((float)sourceWidth / maxRatio);
	            sourceHeight = (int)((float)sourceHeight / maxRatio);
	            bitmap = Bitmap.createScaledBitmap(bitmap, sourceWidth, sourceHeight, true);
	        }
	    } catch (Exception e) {
	    }
	    return bitmap;
	}
	
	
	
	public static String getPath(Activity act, Uri uri) {
		String selectedImagePath;
		// 1:MEDIA GALLERY --- query from MediaStore.Images.Media.DATA
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = act.managedQuery(uri, projection, null, null, null);
		if (cursor != null) {
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			selectedImagePath = cursor.getString(column_index);
		} else {
			selectedImagePath = null;
		}

		if (selectedImagePath == null) {
			// 2:OI FILE Manager --- call method: uri.getPath()
			selectedImagePath = uri.getPath();
		}
		return selectedImagePath;
	}
	
	public static Cursor queryTaskWithIdToodledo(Activity act,
			SQLiteDatabase tasksDB) {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String SelectQuery = "SELECT tasks."
				+ taskOrganizerDatabaseHelper.KEY_ID + " as _id,"
				+ taskOrganizerDatabaseHelper.KEY_IDTOODLEDO
				+ " FROM " + taskOrganizerDatabaseHelper.Tasks_TABLE;

			SelectQuery += " where tasks."
					+ taskOrganizerDatabaseHelper.KEY_IDTOODLEDO + ">0 AND tasks."
					+ taskOrganizerDatabaseHelper.KEY_IDTOODLEDO + " IS NOT NULL";

		Cursor c = tasksDB.rawQuery(SelectQuery, null);
		return c;
	}
	
	public static void ToodoledoEditTasks(Activity act) {
		taskOrganizerDatabaseHelper dbHelper = new taskOrganizerDatabaseHelper(
				act, taskOrganizerDatabaseHelper.DATABASE_NAME, null,
				taskOrganizerDatabaseHelper.DATABASE_VERSION);
		SQLiteDatabase tasksDB = dbHelper.getWritableDatabase();

		
		Cursor c = queryTaskWithIdToodledo(act, tasksDB);

		Map<Long, Long> IdEventMap = new LinkedHashMap<Long, Long>();

		if (c.moveToFirst()) {
			Long idtoodledo = (long) 0;
			Long id = (long) 0;
			do {
				idtoodledo = c.getLong(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_IDTOODLEDO));
				if (idtoodledo > 0) {
					id = c.getLong(c.getColumnIndex("_id"));
					IdEventMap.put(idtoodledo, id);
				}
			} while (c.moveToNext());
		}
		
		
		for (int i = 0; i < jsonArray.length(); i++) {
			try {

				JSONObject jsonObject = jsonArray.getJSONObject(i);
				if (jsonObject.has("total")) {
					continue;
				}

				
				String idstring = jsonObject.getString("id");

				if (listofmodifiedid.contains(idstring)) {
					continue;
				}
				 numberofchanges++;

				long tooledoid = Long.parseLong(idstring);
				
			
				TimeZone tz = TimeZone.getDefault();
				int differenceGMT = tz.getRawOffset();
				
				ContentValues values = new ContentValues();
				values.put(taskOrganizerDatabaseHelper.KEY_IDTOODLEDO, tooledoid); 
				values.put(taskOrganizerDatabaseHelper.KEY_DATEMODIFIED, 0); 
				
				values.put(taskOrganizerDatabaseHelper.KEY_NAME, jsonObject.getString("title")); 
				values.put(taskOrganizerDatabaseHelper.KEY_DESCRIPTION, jsonObject.getString("note")); 
				if (jsonObject.getLong("duetime")>0)
				{
					values.put(taskOrganizerDatabaseHelper.KEY_DUEDATE, (jsonObject.getLong("duetime"))*1000-differenceGMT); 
				}
				else
				{
					values.put(taskOrganizerDatabaseHelper.KEY_DUEDATE, (jsonObject.getLong("duedate"))*1000); 
				}
				//values.put(taskOrganizerDatabaseHelper.KEY_DUEDATE, (jsonObject.getLong("duedate")+jsonObject.getLong("duetime"))*1000); 
				//values.put(taskOrganizerDatabaseHelper.KEY_DATEFROM, (jsonObject.getLong("startdate")+jsonObject.getLong("starttime"))*1000); 
				//values.put(taskOrganizerDatabaseHelper.KEY_DATE, (jsonObject.getLong("startdate")+jsonObject.getLong("starttime"))*1000);
				values.put(taskOrganizerDatabaseHelper.KEY_DATE, (jsonObject.getLong("startdate"))*1000);
				
				
				long modified = jsonObject.getLong("modified");
				maxmodified = Math.max(modified, maxmodified);
				
				
				int status = jsonObject.getInt("status");
				long completed = jsonObject.getLong("completed");
				
				values.put(taskOrganizerDatabaseHelper.KEY_STATUS, 0);
				
				
				if (completed>0)
				{
					values.put(taskOrganizerDatabaseHelper.KEY_STATUS, 2); 
					if (jsonObject.getLong("startdate") == 0)
					{
						values.put(taskOrganizerDatabaseHelper.KEY_DATE, (new Date()).getTime());
					}
				}
				else
				{
					values.put(taskOrganizerDatabaseHelper.KEY_STATUS, 1); 					
					if (status==2)
					{
						values.put(taskOrganizerDatabaseHelper.KEY_STATUS, 1); 
					}	
					
					if (status==9)
					{
						values.put(taskOrganizerDatabaseHelper.KEY_STATUS, 3); 
					}	
					
					if (status==7)
					{
						values.put(taskOrganizerDatabaseHelper.KEY_STATUS, 5); 
					}	
				}

								
				int star = jsonObject.getInt("star");
				int priority = jsonObject.getInt("priority");
				
				values.put(taskOrganizerDatabaseHelper.KEY_PRIORITY, 0);
				if (star==1)
				{
					values.put(taskOrganizerDatabaseHelper.KEY_PRIORITY, 1); 
				}
				
				if (priority>1)
				{
					values.put(taskOrganizerDatabaseHelper.KEY_PRIORITY, priority); 
				}
				values.put(taskOrganizerDatabaseHelper.KEY_PROCESSINGTIME, jsonObject.getInt("length")); 


				long rowID = 0;
				
				if (IdEventMap.containsKey(tooledoid)) {				
					rowID = tasksDB.update(taskOrganizerDatabaseHelper.Tasks_TABLE,values,taskOrganizerDatabaseHelper.KEY_ID + "=" + IdEventMap.get(tooledoid),null);
				} else {
					
				

					values.put(taskOrganizerDatabaseHelper.KEY_PARENT, -1);
					values.put(taskOrganizerDatabaseHelper.KEY_PROCESSINGTIMEFACT, 0);
					values.put(taskOrganizerDatabaseHelper.KEY_ALARM, 0);
					values.put(taskOrganizerDatabaseHelper.KEY_CATEGORY, 1);
					values.put(taskOrganizerDatabaseHelper.KEY_PERCENTOFCOMPLETION, 0);
					
					
					values.put(taskOrganizerDatabaseHelper.KEY_RELEASEDATE, 0);
					values.put(taskOrganizerDatabaseHelper.KEY_DATETO, 0);
					values.put(taskOrganizerDatabaseHelper.KEY_DATETOFACT, 0);
					values.put(taskOrganizerDatabaseHelper.KEY_DATEFROMFACT, 0);
					values.put(taskOrganizerDatabaseHelper.KEY_CODE, "");
					values.put(taskOrganizerDatabaseHelper.KEY_EXECUTOR, "");
					values.put(taskOrganizerDatabaseHelper.KEY_RESPONSIBLE, "");


					rowID = tasksDB.insert(taskOrganizerDatabaseHelper.Tasks_TABLE, "item", values);
				}
				
				values = new ContentValues();
				values.put(taskOrganizerDatabaseHelper.History_task_id, rowID);
				values.put(taskOrganizerDatabaseHelper.History_NAME, "Updated from toodledo. ID toodledo = "+ tooledoid);
				long rowID2 = tasksDB.insert(taskOrganizerDatabaseHelper.History_Table, "item",values);
				
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static boolean ImportTasks(Activity act) throws MalformedURLException
	{
		
		//first, delete tasks
		
		URL url = new URL("http://api.toodledo.com/2/tasks/deleted.php?key="+ SessionIDToodledo+";after="+TimeLastSync);
		
		if (!RequestJSON(url)) {
			Toast.makeText(act,
					act.getString(R.string.from_code86),
					Toast.LENGTH_SHORT).show();
			return false;
		}
		
		if (jsonArray.length()>1)
		{
	    	ToodoledoDeleteTasks(act);
		}
		
		//next update or insert tasks
		url = new URL("http://api.toodledo.com/2/tasks/get.php?key="+ SessionIDToodledo+";modafter="+TimeLastSync+";fields=startdate,duedate,folder,starttime,duetime,status,star,priority,length,note");
		
		if (!RequestJSON(url)) {
			Toast.makeText(act,
					act.getString(R.string.from_code86),
					Toast.LENGTH_SHORT).show();
			return false;
		}
		
		if (jsonArray.length()>1)
		{
		    ToodoledoEditTasks(act);
		}
		
		return true;
	}
	
	public static boolean ConnectToodledo(Activity act, String email, String pass) {
		SharedPreferences settings = act.getSharedPreferences(PREFS_NAME_TOODOLEDO, 0);
		TimeLastConnection = settings.getLong("TimeLastConnection", 0);
		TimeLastSync = settings.getLong("TimeLastSync", 0);
		
		//TimeZone tz = TimeZone.getDefault();
		//TimeLastSync-= tz.getRawOffset()/1000;
		//TimeLastSync = 0;//debugging
		
		maxmodified = TimeLastSync;
		SessionIDToodledo = settings.getString("SessionIDToodledo", "");
		
		
		if (((new Date()).getTime() - TimeLastConnection) < 4 * 3600 * 1000) {
			return true;
		}

		try {
			//email = "emiliano.heyns+rabotan@iris-advies.com";
			//pass = "justatest";
			//email = URLEncoder.encode(email, "UTF-8");
			//pass = URLEncoder.encode(pass, "UTF-8");
			
			//URL url = new URL("http://api.toodledo.com/2/account/lookup.php?appid=" + appid + ";sig=" + md5(email+sig) + ";email=" + email + ";pass=" + pass);
			URL url = new URL("http://api.toodledo.com/2/account/lookup.php?appid=" + appid + ";sig=" + md5(email+sig) + ";email=" + URLEncoder.encode(email, "UTF-8") + ";pass=" + URLEncoder.encode(pass, "UTF-8"));
			if (!RequestJSON(url)) {
				Toast.makeText(act, act.getString(R.string.from_code82),
						Toast.LENGTH_SHORT).show();
				return false;
			}

			JSONObject jsonObject = jsonArray.getJSONObject(0);
			if (jsonObject.has("errorCode")) {
				Toast.makeText(act, act.getString(R.string.from_code82),
						Toast.LENGTH_SHORT).show();
				return false;
			}
			String UserId = jsonObject.getString("userid");
			url = new URL("http://api.toodledo.com/2/account/token.php?userid="+ UserId + ";appid=" + appid+ ";vers=1;device=android;os=1;sig=" + md5(UserId+sig));

			if (!RequestJSON(url)) {
				Toast.makeText(act, act.getString(R.string.from_code83),
						Toast.LENGTH_SHORT).show();
				return false;
			}
			if (jsonObject.has("errorCode")) {
				Toast.makeText(act, act.getString(R.string.from_code82),
						Toast.LENGTH_SHORT).show();
				return false;
			}
			jsonObject = jsonArray.getJSONObject(0);

			String key = md5( md5(pass)+sig+ jsonObject.getString("token"));
			TimeLastConnection = (new Date()).getTime();
			SessionIDToodledo = key;
			
			SharedPreferences.Editor editor = settings.edit();
			editor.putLong("TimeLastConnection", TimeLastConnection);
			editor.putString("SessionIDToodledo",SessionIDToodledo);
			editor.putLong("TimeLastSync", TimeLastSync);
			
			
			editor.commit();	
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public static boolean SyncToodledo(Activity act, String email, String pass) {
		if (!isNetworkAvailable(act))
		{
			Toast.makeText(act, act.getString(R.string.from_code85),Toast.LENGTH_SHORT).show();
			return false;
		}
			
		numberofchanges=0;
		if (!ConnectToodledo(act, email, pass)) { return false;}
		
		try {
			if (ExportTasks(act)) {

			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			Toast.makeText(act,
					act.getString(R.string.from_code84)+" pos. 3",
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		
		try {
			if (ImportTasks(act)) {

			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			Toast.makeText(act,
					act.getString(R.string.from_code86)+" pos. 8",
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

		
		if (numberofchanges>0)
		{
		    Toast.makeText(act, act.getString(R.string.from_code87)+" "+numberofchanges,Toast.LENGTH_SHORT).show();
		}
		
		SharedPreferences settings = act.getSharedPreferences(PREFS_NAME_TOODOLEDO, 0);
		TimeLastConnection = settings.getLong("TimeLastConnection", 0);
		//TimeLastSync = settings.getLong("TimeLastSync", 0);
		SessionIDToodledo = settings.getString("SessionIDToodledo", "");
		
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong("TimeLastConnection", TimeLastConnection);
		editor.putString("SessionIDToodledo",SessionIDToodledo);
		editor.putLong("TimeLastSync", maxmodified);
		editor.commit();
		
		
		
		
		
		return true;
	}
}
