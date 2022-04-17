package com.eugenemath.taskorganizer.namespace;

import java.util.ArrayList;
import java.util.Date;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.provider.Contacts.People;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.SmsManager;
import android.text.Editable;
import android.util.Log;
import android.app.Activity;
import android.net.Uri;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;


public class SMS {
	
	public String phoneNumber = "";
	public String message = "@task:";
	public Task CurrentTask;
	public boolean ErrorByCreating = false;
	
	
	public SMS(Context context, Task task, boolean status, String phoneNumber0)
	{
		
		CurrentTask = task;
		
		if (phoneNumber0 == null)
		{
			String tofind = "";
			
			if (status)
			{
				tofind = task.responsible;
			}	
			else{
			    tofind = task.executor;
			}
			phoneNumber = CommonFunctions.FindPhoneOfContact(context, tofind);
			if (phoneNumber.length() == 0)
			{
				ErrorByCreating = true;
				Toast.makeText(context, context.getString(R.string.from_code53)+tofind+context.getString(R.string.from_code54), Toast.LENGTH_LONG).show();	
			}
		
			
			/*if (tofind.startsWith("content:"))
			{
				Uri contactData = Uri.parse(tofind);

				ContentResolver cr = context.getContentResolver();
				Cursor c_contact =  cr.query(contactData, null, null, null, null);
				c_contact.moveToFirst();	
				//phoneNumber = c_contact.getString(c_contact.getColumnIndexOrThrow(People.NUMBER_KEY));
	
				phoneNumber = "";
				String contactId =  c_contact.getString(c_contact.getColumnIndex(ContactsContract.Contacts._ID));
			     //  Get all phone numbers.
		        //
		        Cursor phones = cr.query(Phone.CONTENT_URI, null, Phone.TYPE+" = "+Phone.TYPE_MOBILE+" AND "+Phone.CONTACT_ID + " = " + contactId, null, null);
		        if (phones.getCount() == 0)
		        {
		           phones = cr.query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + " = " + contactId, null, null);
		        }

		           
		        //}
		        if (phones.moveToFirst())
		        {
		        	phoneNumber = phones.getString(phones.getColumnIndex(Phone.NUMBER));
		        }
		        phones.close();						
				c_contact.close();
				
				if (phoneNumber.length()==0)
				{
					ErrorByCreating = true;
					Toast.makeText(context, "Phone for the contact"+tofind+" is not defined", Toast.LENGTH_LONG).show();
				}				
			}
			else
			{
				phoneNumber = tofind;
			}*/
			
			
		}
		
		

		message = "@task:";
		String code = task.code;
		if (code.length() == 0)
		{
			code = ""+task.idtask;
		}
		message+="[code="+code+"]";
		
		String name = task.name;
		if (name.length()>20)
		{
			name = name.substring(0, 19)+"...";
		}
		message+="[name="+name+"]";
		
		
		if (status)
		{
			message+="[status="+task.status+"("+task.statusname+")]";
		}
		else 
		{
			if (task.duedate.getTime() > (new Date(0)).getTime())
			{
				SimpleDateFormat sdf_yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
				message+="[duedate="+sdf_yyyyMMdd.format(task.duedate)+"]"; 
			}
		}
			
		
	}
	
	public void SendSMS(Context context)	
	{
		SmsManager sm = SmsManager.getDefault();
		//sm.sendTextMessage(phoneNumber, null, message, null, null);
		
		ArrayList<String> parts = sm.divideMessage(message);
		try
		{
			sm.sendMultipartTextMessage(phoneNumber, null, parts, null, null);		
		}
		catch (NullPointerException e) {
			// TODO Auto-generated catch block
			Toast.makeText(context, context.getString(R.string.from_code81), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
		
		ContentValues values = new ContentValues();
		values.put(taskOrganizerDatabaseHelper.History_task_id, CurrentTask.idtask);
		values.put(taskOrganizerDatabaseHelper.History_NAME, "SMS to "+phoneNumber+" "+message);
		
		taskOrganizerDatabaseHelper dbHelper = new taskOrganizerDatabaseHelper(context,  
				taskOrganizerDatabaseHelper.DATABASE_NAME, 
				null, 
				taskOrganizerDatabaseHelper.DATABASE_VERSION);
		SQLiteDatabase tasksDB = dbHelper.getWritableDatabase();			
		
		long rowID2 = tasksDB.insert(taskOrganizerDatabaseHelper.History_Table, "item", values);			
		
		
		Toast.makeText(context, context.getString(R.string.from_code55)+phoneNumber+" "+message, Toast.LENGTH_LONG).show();
		
		
		
		/*Intent sendIntent = new Intent(Intent.ACTION_VIEW);
		sendIntent.putExtra("sms_body", "Content of the SMS goes here...");
		sendIntent.setType("vnd.android-dir/mms-sms");
		//sendIntent.putExtra("address", "123456789");
		startActivity(sendIntent);	
		<uses-permission android:name="android.permission.READ_SMS"/>
<uses-permission android:name="android.permission.WRITE_SMS"/>
		 */	
		/*ContentValues values = new ContentValues();
		values.put("address", "123456789");
		values.put("body", "foo bar");
		getContentResolver().insert(Uri.parse("content://sms/sent"), values);*/		

		return;
	}
	
	
	@Override
	public String toString() {

		return "SMS : "+phoneNumber+" "+message;
	}	
}