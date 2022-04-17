package  com.eugenemath.taskorganizer.namespace;
 
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsManager;
import android.telephony.gsm.SmsMessage;
import android.util.Log;
import android.widget.Toast;
 
public class SmsReceiver extends BroadcastReceiver
{
	
	private static SQLiteDatabase tasksDB;

	
    @Override
    public void onReceive(Context context, Intent intent) 
    {
        //---get the SMS message passed in---
        Bundle bundle = intent.getExtras();    
        
        switch (getResultCode())//IntentFilter(SENT)
        {     
            case Activity.RESULT_OK:
            	break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
            	break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
            	break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
            	break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
            	break;
        }
           
        switch (getResultCode())//IntentFilter(DELIVERED)
        {
            case Activity.RESULT_OK:
            	break;
            case Activity.RESULT_CANCELED:
            	break;                        
        }       
        
        //return;
        
        SmsMessage[] msgs = null;
        String str = "";            
        if (bundle != null)
        {
            //---retrieve the SMS message received---
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];  
            
            String PhoneNumber = "";
            String message = "";
            
            for (int i=0; i<msgs.length; i++){
                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);                
                str += "SMS from " + msgs[i].getOriginatingAddress();   
                PhoneNumber = msgs[i].getOriginatingAddress();
                str += " :";
                str += msgs[i].getMessageBody().toString();
                message +=msgs[i].getMessageBody().toString();
                str += "\n";        
            }
            //---display the new SMS message---
            Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
            
            if (message.startsWith("@task:"))
            {
            	CommonFunctions.PerformTaskMessage(context,PhoneNumber,message,false);
            }
            
        }                         
    }
    
    public void OLDDELETEPerformTaskMessage(Context context, String PhoneNumber,String message)
    {
    	String _temp = message;
    	int status = -1;
    	String name = "";
    	Date duedate = new Date(0);
    	SimpleDateFormat sdf_yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
    	//substring (int start, int end)
    	//substring (int start)
    	//indexOf (int c)
    	
    	int index = _temp.indexOf("[code=", 0);
    	if (index==-1)
    	{
    		return;
    	}
    	int index2 = _temp.indexOf("]", index);
    	String code = _temp.substring(index+6,index2);
    	
    	index = _temp.indexOf("[name=", 0);
    	if (index==-1)
    	{
    		return;
    	}
    	index2 = _temp.indexOf("]", index);
    	name = _temp.substring(index+6,index2); 
    	
    	index = _temp.indexOf("[duedate=", 0);
    	if (index !=-1)
    	{
        	index2 = _temp.indexOf("]", index);
        	String duedatestring = _temp.substring(index+9,index2);
        	try {
        		duedate = sdf_yyyyMMdd.parse(duedatestring);
        	}
        	catch (ParseException e) {
        		Log.e("Error: ", e.getMessage());
        		return;
        	}        	
    	}
 	
    	index = _temp.indexOf("[status=", 0);
    	if (index!=-1)
    	{
        	index2 = _temp.indexOf("(", index);
        	if (index2!=-1)
        	{
        		try {
        			status = Integer.valueOf(_temp.substring(index+8,index2)); 	    
        		} catch(NumberFormatException e) {
        			status = 1; 
        		}       	    
        	    
        	}
    	}

    	
		taskOrganizerDatabaseHelper dbHelper = new taskOrganizerDatabaseHelper(context,  
				taskOrganizerDatabaseHelper.DATABASE_NAME, 
				null, 
				taskOrganizerDatabaseHelper.DATABASE_VERSION);
		tasksDB = dbHelper.getWritableDatabase();	 	
    	
    	boolean taskExists = true;
    	
    	int taskid = 0;
    	
    	Cursor c = query(code,true);
    	if (c.getCount()==0)
    	{
    		try {
    			int x = Integer.valueOf(code);
    			c = query(""+x,false);
    			if (c.getCount()==0)
    			{
    				taskExists = false; 
    			}		    
    		} catch(NumberFormatException e) {
    			taskExists = false; 
    		}
    	}


    	if (taskExists)
    	{
    		c.moveToFirst();
    		taskid = c.getInt(c.getColumnIndex("_id"));
    		c.close();
    	}

    	
    	String CustomContent = "";
    	
    	
    	if (!taskExists)
    	{
    		//new task
    		Task newTask = new Task(name);	
    		if (status!=-1){
    			newTask.status = status;
    		}
    		
    		newTask.responsible = PhoneNumber;
    		newTask.code = code;
    		newTask.duedate = duedate;
    		
    	    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(PhoneNumber));
    	    Long id_contact;

    	    ContentResolver contentResolver = context.getContentResolver();
    	    Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID,
    	            ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

    	    try {
    	        if (contactLookup != null && contactLookup.getCount() > 0) {
    	            contactLookup.moveToNext();
    	            id_contact = contactLookup.getLong(contactLookup.getColumnIndex(ContactsContract.Data._ID));
    	            newTask.responsible = "content://contacts/people/"+id_contact;
    	            //String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
    	        }
    	    } finally {
    	        if (contactLookup != null) {
    	            contactLookup.close();
    	        }
    	    }  		
    		
    		
    		
    		ContentValues values = newTask.ReturnContentValues();    
    		long rowID = tasksDB.insert(taskOrganizerDatabaseHelper.Tasks_TABLE, "item", values);	
    		
    		taskid = Integer.valueOf(""+rowID);
    		
    		values = new ContentValues();
    		values.put(taskOrganizerDatabaseHelper.History_task_id, rowID);
    		values.put(taskOrganizerDatabaseHelper.History_NAME, "Incoming SMS from "+PhoneNumber+" "+message);		 		
    		long rowID2 = tasksDB.insert(taskOrganizerDatabaseHelper.History_Table, "item", values);			
   		
    		CustomContent = context.getString(R.string.from_code56)+name;
    	}
    	else if(status!=-1 && taskExists)
    	{
    		String where = taskOrganizerDatabaseHelper.KEY_ID + "=" + taskid;
    		ContentValues values = new ContentValues(); 
    		values.put(taskOrganizerDatabaseHelper.KEY_STATUS, status);
    		long rowID = tasksDB.update(taskOrganizerDatabaseHelper.Tasks_TABLE,values,where,null);	
    		
    		values = new ContentValues();
    		values.put(taskOrganizerDatabaseHelper.History_task_id, taskid);
    		values.put(taskOrganizerDatabaseHelper.History_NAME, "Incoming SMS from "+PhoneNumber+" "+message);		 		
    		long rowID2 = tasksDB.insert(taskOrganizerDatabaseHelper.History_Table, "item", values);	  		
    		
    		CustomContent = context.getString(R.string.from_code34)+name+context.getString(R.string.from_code35);
    	}
    	else if(status==-1 && taskExists)
    	{
    		Toast.makeText(context, context.getString(R.string.from_code57), Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	
    	//String ns = Context.NOTIFICATION_SERVICE;
    	NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);    	
    	Notification notification;
    	//= new Notification(R.drawable.ic_menu_send, CustomContent, System.currentTimeMillis());

    	//notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
    	//notification.contentView = new RemoteViews(this.getPackageName(), R.layout.my_status_window_layout);
    	Intent intent = new Intent(context, taskForm.class);
    	intent.putExtra("_id", Long.valueOf(taskid));
    	PendingIntent pendingIntent =  PendingIntent.getActivity(context, 0, intent, 0);
    	//notification.contentIntent = pendingIntent;   	

		//= new Notification(R.drawable.ic_menu_send,
		//CustomContent, System.currentTimeMillis());

		Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.ic_menu_send);


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
    
	public Cursor query(String code,boolean isCode) {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String SelectQuery = "SELECT tasks."
				+ taskOrganizerDatabaseHelper.KEY_ID+" as _id,"
				+ taskOrganizerDatabaseHelper.KEY_DATE+","
				+ taskOrganizerDatabaseHelper.KEY_CODE+","
				+ taskOrganizerDatabaseHelper.KEY_PARENT+",tasks."
				+ taskOrganizerDatabaseHelper.KEY_NAME+","
				+ taskOrganizerDatabaseHelper.KEY_RESPONSIBLE+","
				+ taskOrganizerDatabaseHelper.KEY_EXECUTOR+","
				+ taskOrganizerDatabaseHelper.KEY_DESCRIPTION+","
				+ taskOrganizerDatabaseHelper.KEY_PROCESSINGTIME+","
				+ taskOrganizerDatabaseHelper.KEY_DUEDATE+","
				+ taskOrganizerDatabaseHelper.KEY_RELEASEDATE+","
				+ taskOrganizerDatabaseHelper.KEY_STATUS+","
				+ taskOrganizerDatabaseHelper.KEY_PROCESSINGTIMEFACT+","
				+ taskOrganizerDatabaseHelper.KEY_PRIORITY+","
				+ taskOrganizerDatabaseHelper.KEY_DATEFROM+","
				+ taskOrganizerDatabaseHelper.KEY_DATETO+","
				+ taskOrganizerDatabaseHelper.KEY_DATEFROMFACT+","
				+ taskOrganizerDatabaseHelper.KEY_DATETOFACT+","
				+ taskOrganizerDatabaseHelper.KEY_PERCENTOFCOMPLETION+","
				+ taskOrganizerDatabaseHelper.KEY_CATEGORY+" as category,"
				+ taskOrganizerDatabaseHelper.KEY_STATUS+" as status,"
				+ taskOrganizerDatabaseHelper.Statuses_Table+"."+ taskOrganizerDatabaseHelper.Status_NAME+" as statusname,"
				+ taskOrganizerDatabaseHelper.Categories_Table+"."+ taskOrganizerDatabaseHelper.Categories_NAME+" as categoryname"
				+" FROM "+ taskOrganizerDatabaseHelper.Tasks_TABLE
				+" LEFT JOIN "+ taskOrganizerDatabaseHelper.Statuses_Table+" ON "+ taskOrganizerDatabaseHelper.Tasks_TABLE+"."+ taskOrganizerDatabaseHelper.KEY_STATUS+" = "+ taskOrganizerDatabaseHelper.Statuses_Table+"."+ taskOrganizerDatabaseHelper.Status_ID
				+" LEFT JOIN "+ taskOrganizerDatabaseHelper.Categories_Table+" ON "+ taskOrganizerDatabaseHelper.Tasks_TABLE+"."+ taskOrganizerDatabaseHelper.KEY_CATEGORY+" = "+ taskOrganizerDatabaseHelper.Categories_Table+"."+ taskOrganizerDatabaseHelper.Categories_ID;


		if (isCode)
		{
			SelectQuery +=" where tasks."+taskOrganizerDatabaseHelper.KEY_CODE+" = '"+code+"'";	
		}
		else
		{
			SelectQuery +=" where tasks."+taskOrganizerDatabaseHelper.KEY_ID+" = "+code;	
		}


		Cursor c = tasksDB.rawQuery(SelectQuery, null);
		return c;
	}   
    
}