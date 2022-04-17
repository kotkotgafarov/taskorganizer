package com.eugenemath.taskorganizer.namespace;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date; 
import java.util.GregorianCalendar;
import java.util.Properties; 
import javax.activation.CommandMap; 
import javax.activation.DataHandler; 
import javax.activation.DataSource; 
import javax.activation.FileDataSource; 
import javax.activation.MailcapCommandMap; 
import javax.mail.Address;
import javax.mail.BodyPart; 
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart; 
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.PasswordAuthentication; 
import javax.mail.Session; 
import javax.mail.Store;
import javax.mail.Transport; 
import javax.mail.internet.InternetAddress; 
import javax.mail.internet.MimeBodyPart; 
import javax.mail.internet.MimeMessage; 
import javax.mail.internet.MimeMultipart; 
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.MessageNumberTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.CheckBox;
import android.widget.Toast;


public class MyMail extends javax.mail.Authenticator { 
	public String _user; 
	public String _pass; 

	public String[] _to; 
	public String _send_to; 
	public String _from; 

	public String _port; 
	public String _sport; 

	public String _host; 

	public String _subject; 
	public String _body; 

	public long lastnumber = 0;
	public long newlastnumber = 0;
	
	public boolean _auth; 
	
	public Context act = null;
	public boolean istest = false;

	private static final String PREFS_NAME = "PrefsTaskOrganizer";
	
	private boolean _debuggable; 

	private Multipart _multipart; 


	public MyMail() { 
		_host = "smtp.gmail.com"; // default smtp server 
		_port = "465"; // default smtp port 
		_sport = "465"; // default socketfactory port 

		_user = ""; // username 
		_pass = ""; // password 
		_from = ""; // email sent from 
		_subject = ""; // email subject 
		_body = ""; // email body 

		_debuggable = false; // debug mode on or off - default off 
		_auth = true; // smtp authentication - default on 

		_multipart = new MimeMultipart(); 

		// There is something wrong with MailCap, javamail can not find a handler for the multipart/mixed part, so this bit needs to be added. 
		MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap(); 
		mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html"); 
		mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml"); 
		mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain"); 
		mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed"); 
		mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822"); 
		CommandMap.setDefaultCommandMap(mc); 
	} 

	public MyMail(String user, String pass) { 
		this(); 

		_user = user; 
		_pass = pass; 
	} 

	public void FillSubjectByTask(Task task,boolean status)
	{
		_subject = "@task:";
		String code = task.code;
		if (code.length() == 0)
		{
			code = ""+task.idtask;
		}
		_subject+="[code="+code+"]";
		
		String name = task.name;
		if (name.length()>20)
		{
			name = name.substring(0, 19)+"...";
		}
		_subject+="[name="+name+"]";
		
		
		if (status)//message about new status
		{
			_subject+="[status="+task.status+"("+task.statusname+")]";
		}
		else 
		{
			if (task.duedate.getTime() > (new Date(0)).getTime())
			{
				SimpleDateFormat sdf_yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
				_subject+="[duedate="+sdf_yyyyMMdd.format(task.duedate)+"]"; 
			}
		}		
	}
	
	
	public boolean send() throws Exception { 
		Properties props = _setProperties(); 

		if(!_user.equals("") && !_pass.equals("") && !_send_to.equals("") && !_from.equals("") && !_subject.equals("")) { 
			Session session = Session.getInstance(props, this); 

		
			MimeMessage msg = new MimeMessage(session); 

			msg.setFrom(new InternetAddress(_from)); 

			//InternetAddress[] addressTo = new InternetAddress[_to.length]; 
			//for (int i = 0; i < _to.length; i++) { 
			//	addressTo[i] = new InternetAddress(_to[i]); 
			//
        	msg.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(_send_to)); 

			msg.setSubject(_subject); 
			msg.setSentDate(new Date()); 

			// setup message body 
			BodyPart messageBodyPart = new MimeBodyPart(); 
			messageBodyPart.setText(_body); 
			_multipart.addBodyPart(messageBodyPart); 

			// Put parts in message 
			msg.setContent(_multipart); 

			// send email 
			Transport.send(msg); 

			return true; 
		} else { 
			return false; 
		} 
	} 
	
	public boolean receive() throws Exception { 
		Properties props = _setProperties(); 
		props.setProperty("mail.store.protocol", "imaps");
		
		if(!_user.equals("") && !_pass.equals("") && _host.length() > 0) { 

			 
			try
			{
				/*  Create the session and get the store for read the mail. */
				Session session = Session.getInstance(props, this); 

				Store store = session.getStore("imaps");
				//Store store = session.getStore("pop3");// for mail ru
				try
				{
					store.connect(_host,_user,_pass);
				}
				catch (MessagingException e)
				{
					store = session.getStore("pop3");
					store.connect(_host,_user,_pass);
				}

				/*String name = "Inbox";
				Folder d1 =  store.getDefaultFolder();
				Folder[] f = store.getDefaultFolder().list();
				for(Folder fd:f)
				{
					name = fd.getName();
				};*/

				
				/*  Mention the folder name which you want to read. */
				Folder inbox = store.getFolder("Inbox");
				//System.out.println("No of Unread Messages : " + inbox.getUnreadMessageCount());

				/*Open the inbox using store.*/
				inbox.open(Folder.READ_ONLY);

				/*  Get the messages which is unread in the Inbox*/
				GregorianCalendar date = new GregorianCalendar();
				date.add(date.DATE, -7);
				

				
				SearchTerm newerThen = new ReceivedDateTerm(ComparisonTerm.GT,date.getTime());

				Message[] messages = inbox.search(newerThen);	

				
				if (messages.length==0)
				{
					Flags recent = new Flags(Flags.Flag.RECENT);
					FlagTerm recentFlagTerm = new FlagTerm(recent, true);
					messages = inbox.search(recentFlagTerm);
				}	
				
				/*if (messages.length==0)// attempt for mail.ru after it messages length = 0
				{
					
					Flags seen = new Flags(Flags.Flag.SEEN);
					FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
					Flags recent = new Flags(Flags.Flag.RECENT);
					FlagTerm recentFlagTerm = new FlagTerm(recent, true);
					SearchTerm searchTerm = new AndTerm(unseenFlagTerm, recentFlagTerm);
					
					messages = inbox.search(searchTerm);
					
					//messages = inbox.search(new FlagTerm(new Flags(Flag.SEEN), false));
				}*/
						
				    
				/* Use a suitable FetchProfile    */
				FetchProfile fp = new FetchProfile();
				fp.add(FetchProfile.Item.ENVELOPE);
				fp.add(FetchProfile.Item.CONTENT_INFO);
				inbox.fetch(messages, fp);

				try
				{
					//printAllMessages(messages);
					for (int i = 0; i < messages.length; i++)
					{
						// System.out.println("MESSAGE #" + (i + 1) + ":");
						checkEnvelope(messages[i]);
					}					
					inbox.close(true);
					store.close();
					if (!istest && newlastnumber>0)
					{
						SharedPreferences settings = act.getSharedPreferences(PREFS_NAME, 0);
						SharedPreferences.Editor editor = settings.edit();
						editor.putString("lastemailid", ""+newlastnumber);
						editor.commit();	
					}
				}
				catch (Exception ex)
				{
					//System.out.println("Exception arise at the time of read mail");
					ex.printStackTrace();
					return false; 
				}
			}
			catch (NoSuchProviderException e)
			{
				e.printStackTrace();
				return false; 
				//System.exit(1);
			}
			catch (MessagingException e)
			{
				e.printStackTrace();
				//System.exit(2);
				return false; 
			}
					 

			return true; 
		} else { 
			return false; 
		} 	
	}
	
	/*  Print the envelope(FromAddress,ReceivedDate,Subject)  */
	public void checkEnvelope(Message message) throws Exception
	{
		String subject = message.getSubject();
		
		if (subject == null)
		{
			return;
		}
		
		Date receivedDate = message.getReceivedDate();
		long number = message.getMessageNumber();
		
		if (number<=lastnumber)
		{
			return;
		}		
		
		newlastnumber = (number>newlastnumber)?number:newlastnumber;
		
		if (!subject.startsWith("@task:"))
		{
			return;
		}
		
		if (act != null && istest)
		{
			Toast.makeText(act, subject, Toast.LENGTH_SHORT).show();
		}
		
		
		
		
		
		Address[] a;
		//String from = "";
		
		
		// FROM
		if ((a = message.getFrom()) != null)
		{
			for (int j = 0; j < a.length; j++)
			{
				//System.out.println("FROM: " + a[j].toString());
				_from = a[j].toString();
				break;
			}
		}
		
		
		int pos = _from.lastIndexOf("<");
		int pos2 = _from.lastIndexOf(">");
		if (pos!=-1 && pos2 != -1)
		{
			_from = _from.substring(pos+1, pos2);
		}
		
		
		if (!istest)
		{
			CommonFunctions.PerformTaskMessage(act,_from,subject,true);
			
			

		}
		
		
		
		// TO
		/*if ((a = message.getRecipients(Message.RecipientType.TO)) != null)
		{
			for (int j = 0; j < a.length; j++)
			{
				//System.out.println("TO: " + a[j].toString());
			}
		}*/
		
		//String content = message.getContent().toString();
		//System.out.println("Subject : " + subject);
		///System.out.println("Received Date : " + receivedDate.toString());
		//System.out.println("Content : " + content);
		
		//String body = getContent(message);
	}

	public String getContent(Message msg)
	{
		String body = "";
		try
		{
			String contentType = msg.getContentType();
			//System.out.println("Content Type : " + contentType);
			Multipart mp = (Multipart) msg.getContent();
			int count = mp.getCount();
			for (int i = 0; i < count; i++)
			{
				body +=dumpPart(mp.getBodyPart(i));
			}
		}
		catch (Exception ex)
		{
			//System.out.println("Exception arise at get Content");
			ex.printStackTrace();
		}
		return body;
	}

	public String dumpPart(Part p) throws Exception
	{
		String body = "";
		// Dump input stream ..
		InputStream is = p.getInputStream();
		// If "is" is not already buffered, wrap a BufferedInputStream
		// around it.
		if (!(is instanceof BufferedInputStream))
		{
			is = new BufferedInputStream(is);
		}
		int c;
		//System.out.println("Message : ");
		while ((c = is.read()) != -1)
		{
			body+=c;
			
		}
		return body;
	}	
	
	public void addAttachment(String filename) throws Exception { 
		BodyPart messageBodyPart = new MimeBodyPart(); 
		DataSource source = new FileDataSource(filename); 
		messageBodyPart.setDataHandler(new DataHandler(source)); 
		messageBodyPart.setFileName(filename); 

		_multipart.addBodyPart(messageBodyPart); 
	} 

	@Override 
	public PasswordAuthentication getPasswordAuthentication() { 
		return new PasswordAuthentication(_user, _pass); 
	} 

	private Properties _setProperties() { 
		Properties props = new Properties(); 

		props.put("mail.smtp.host", _host); 

		if(_debuggable) { 
			props.put("mail.debug", "true"); 
		} 

		if(_auth) { 
			props.put("mail.smtp.auth", "true"); 
		} 

		props.put("mail.smtp.port", _port); 
		props.put("mail.smtp.socketFactory.port", _sport); 
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); 
		props.put("mail.smtp.socketFactory.fallback", "false"); 

		return props; 
	} 

	// the getters and setters 
	public String getBody() { 
		return _body; 
	} 

	public void setBody(String _body) { 
		this._body = _body; 
	} 
	

	// more of the getters and setters
} 