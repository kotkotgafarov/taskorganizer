package com.eugenemath.taskorganizer.namespace;

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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Contacts.People;
import android.text.Editable;
import android.util.Log;

public class Filter {

	public String xmlstring;
	public String name_of_filter;
	
	public String status = "";	 	 
	public Integer priority = 0;	 	 	 	 	 
	public String category = "";	 
	public Date releasedate;
	
	public Date date_to;
	public Date date_from;
	public int spinner_date_to = 0;
	public int spinner_date_from = 0;
	
	
	public Date releasedate_from;
	public Date releasedate_to;	
	public int spinner_releasedate_from = 0;
	public int spinner_releasedate_to = 0;
	
	
	public Date duedate_from;
	public Date duedate_to;
	public int spinner_duedate_from = 0;
	public int spinner_duedate_to = 0;	
	
	public Boolean show_empty_date_checked = true;
	
	public String name = "";
	public String code = "";
	public String executor = "";  
	public String responsible = "";  
	public String description = "";  
	public String  categoryname = "";
	public String statusname = "";
	public String executorname = ""; 
	public String responsiblename = ""; 
	
	
	public Boolean status_checked = false;	 	  	 	 	 
	public Boolean category_checked= false;	 
	public Boolean date_checked= false;
	public Boolean releasedate_checked= false;
	public Boolean duedate_checked= false;
	public Boolean name_checked= false;
	public Boolean code_checked= false;
	public Boolean executor_checked= false;  
	public Boolean responsible_checked= false;  
	public Boolean description_checked= false;  

	public int spinner_status = 0;	 	  	 	 	 
	public int spinner_category= 0;	 
	public int spinner_date= 0;
	public int spinner_releasedate= 0;
	public int spinner_duedate= 0;
	public int spinner_name= 0;
	public int spinner_code= 0;
	public int spinner_executor= 0;  
	public int spinner_responsible= 0;  
	public int spinner_description= 0;	
	
	public int spinner_sort = 0;	
	
	private SimpleDateFormat sdf_yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");

	public Filter(Cursor c)	
	{

	}

	public Filter(String xml)
	{
		name  = "";
		date_from = new Date(0);
		date_to= new Date(0);
		releasedate_from= new Date(0);
		releasedate_to= new Date(0);
		duedate_from= new Date(0);
		duedate_to= new Date(0);		
		
		xmlstring = xml;
		FromXML();
	}
	
	public Filter()	
	{
		name  = "";
		date_from = new Date(0);
		date_to= new Date(0);
		releasedate_from= new Date(0);
		releasedate_to= new Date(0);
		duedate_from= new Date(0);
		duedate_to= new Date(0);
		
		status_checked = true;
		spinner_status = 1;
		status = "4;";
		
		
		date_checked = true;
		spinner_date = 0;
		spinner_date_from = 1;
	}
	
	@Override
	public String toString() {

		return "Filter : " + name_of_filter ;
	}
	
	public void FromXML() 
	{
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+xmlstring;
		Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        
        try {
        	 
            DocumentBuilder db = dbf.newDocumentBuilder();
 
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xml));
            doc = db.parse(is); 
            //doc = db.newDocument(); 
            
            
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
        
        //Element rootElement = doc.getDocumentElement();
        //String g = rootElement.getNodeName();
        NodeList nl = doc.getElementsByTagName("field");

        String name_of_element = "";
        String spinner_position = "";

        String checked = "";
        
        // looping through all item nodes <item>
        for (int i = 0; i < nl.getLength(); i++) {
        	Element element = (Element) nl.item(i);
        	name_of_element = element.getAttribute("name"); 
        	spinner_position = element.getAttribute("spinner_position");
        	if (name_of_element.equals("name"))
        	{
        		name = element.getAttribute("value"); 
        		spinner_name = Integer.valueOf(spinner_position);
        		
        		checked = element.getAttribute("checked"); 
        		if (checked.equals("1")) {name_checked = true;}
        		
        	}
        	
        	
        	if (name_of_element.equals("spinner_sort"))
        	{
        		spinner_sort = Integer.valueOf(spinner_position);
        	}        
        	
        	
        	
        	if (name_of_element.equals("code"))
        	{
        		code = element.getAttribute("value"); 
        		spinner_code = Integer.valueOf(spinner_position);
        	
        		checked = element.getAttribute("checked"); 
        		if (checked.equals("1")) {code_checked = true;}      		
        	}        	
        	
        	if (name_of_element.equals("status"))
        	{
        		status = element.getAttribute("value"); 
        		spinner_status = Integer.valueOf(spinner_position);
        	
        		checked = element.getAttribute("checked"); 
        		if (checked.equals("1")) {status_checked = true;}
        	}     
        	
        	
        	if (name_of_element.equals("description"))
        	{
        		description = element.getAttribute("value"); 
        		spinner_description = Integer.valueOf(spinner_position);
        		
        		checked = element.getAttribute("checked"); 
        		if (checked.equals("1")) {description_checked = true;}
        	}        
        	
        	
        	if (name_of_element.equals("show_empty_date_checked"))
        	{
        		checked = element.getAttribute("checked"); 
        		if (checked.equals("1")) {show_empty_date_checked = true;}
        		else{show_empty_date_checked = false;}
        	}             	
 
            
        	
        	if (name_of_element.equals("category"))
        	{
        		category = element.getAttribute("value"); 
        		spinner_category = Integer.valueOf(spinner_position);
        		
        		checked = element.getAttribute("checked"); 
        		if (checked.equals("1")) {category_checked = true;}
        	}             	
        	
        	if (name_of_element.equals("executor"))
        	{
        		executor = element.getAttribute("value"); 
        		spinner_executor = Integer.valueOf(spinner_position);
        		
        		checked = element.getAttribute("checked"); 
        		if (checked.equals("1")) {executor_checked = true;}
        	}           	

        	if (name_of_element.equals("responsible"))
        	{
        		responsible = element.getAttribute("value"); 
        		spinner_responsible = Integer.valueOf(spinner_position);
        		
        		checked = element.getAttribute("checked"); 
        		if (checked.equals("1")) {responsible_checked = true;}
        	}            	
        	
        	if (name_of_element.equals("date"))
        	{
        		//date_from = new Date(Long.valueOf(element.getAttribute("value_date_from")));
        		//date_to = new Date(Long.valueOf(element.getAttribute("value_date_to")));
        		try {
        			String value_date_from = element.getAttribute("value_date_from");
        			String value_date_to = element.getAttribute("value_date_to");
        			if (value_date_from.length()==1)
        			{
        				date_from = new Date(70,1,1);
        				spinner_date_from = Integer.valueOf(value_date_from);
        			}
        			else
        			{
        				date_from = sdf_yyyyMMdd.parse(value_date_from);
        				spinner_date_from = 0;
        			}
        			
        			if (value_date_to.length()==1)
        			{
        				date_to = new Date(70,1,1);
        				spinner_date_to = Integer.valueOf(value_date_to);
        			}
        			else
        			{
        				date_to = sdf_yyyyMMdd.parse(value_date_to);
        				spinner_date_to = 0;
        			}       			
        		}
        		catch (ParseException e) {
                    Log.e("Error: ", e.getMessage());
                    return;
                }
        		
        		spinner_date = Integer.valueOf(spinner_position);
        		
        		checked = element.getAttribute("checked"); 
        		if (checked.equals("1")) {date_checked = true;}
        	}           	
        	
        	
        	if (name_of_element.equals("duedate"))
        	{
        		try {
        			String value_duedate_from = element.getAttribute("value_duedate_from");
        			String value_duedate_to = element.getAttribute("value_duedate_to");
        			if (value_duedate_from.length()==1)
        			{
        				duedate_from = new Date(70,1,1);
        				spinner_duedate_from = Integer.valueOf(value_duedate_from);
        			}
        			else
        			{
        				duedate_from = sdf_yyyyMMdd.parse(value_duedate_from);
        				spinner_duedate_from = 0;
        			}
        			
        			if (value_duedate_to.length()==1)
        			{
        				duedate_to = new Date(70,1,1);
        				spinner_duedate_to = Integer.valueOf(value_duedate_to);
        			}
        			else
        			{
        				duedate_to = sdf_yyyyMMdd.parse(value_duedate_to);
        				spinner_duedate_to = 0;
        			}              			       			
        		}
        		catch (ParseException e) {
                    Log.e("Error: ", e.getMessage());
                    return;
                }
        		spinner_duedate = Integer.valueOf(spinner_position);
        		
        		checked = element.getAttribute("checked"); 
        		if (checked.equals("1")) {duedate_checked = true;}
        	}         	
        	
        	if (name_of_element.equals("releasedate"))
        	{
        		try {       			
            		
            			String value_releasedate_from = element.getAttribute("value_releasedate_from");
            			String value_releasedate_to = element.getAttribute("value_releasedate_to");
            			if (value_releasedate_from.length()==1)
            			{
            				releasedate_from = new Date(70,1,1);
            				spinner_releasedate_from = Integer.valueOf(value_releasedate_from);
            			}
            			else
            			{
            				releasedate_from = sdf_yyyyMMdd.parse(value_releasedate_from);
            				spinner_releasedate_from = 0;
            			}
            			
            			if (value_releasedate_to.length()==1)
            			{
            				releasedate_to = new Date(70,1,1);
            				spinner_releasedate_to = Integer.valueOf(value_releasedate_to);
            			}
            			else
            			{
            				releasedate_to = sdf_yyyyMMdd.parse(value_releasedate_to);
            				spinner_releasedate_to = 0;
            			}              			       			
            		    			
        			
        		}
        		catch (ParseException e) {
                    Log.e("Error: ", e.getMessage());
                    return;
                }
        		spinner_releasedate = Integer.valueOf(spinner_position);
        		
        		checked = element.getAttribute("checked"); 
        		if (checked.equals("1")) {releasedate_checked = true;}
        	}        	
        }       
        
	}
	
	public void ToXML() 	
	{
		Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
   
        try {
 
            DocumentBuilder db = dbf.newDocumentBuilder();
 
            //InputSource is = new InputSource();
            //is.setCharacterStream(new StringReader(xml));
            //doc = db.parse(is); 
            doc = db.newDocument(); 
            
            
            } catch (ParserConfigurationException e) {
                Log.e("Error: ", e.getMessage());
                return;
            } /*catch (SAXException e) {
                Log.e("Error: ", e.getMessage());
                return;
            } catch (IOException e) {
                Log.e("Error: ", e.getMessage());
                return;
            }*/
	
        Element RootElement = doc.createElement("filter");
        doc.appendChild(RootElement);
            
        Element child;
        
        child = doc.createElement("field");
        child.setAttribute("checked", (name_checked)?"1":"0");     	
        child.setAttribute("name", "name"); 
        child.setAttribute("spinner_position", ""+spinner_name);
        child.setAttribute("value", name); 
        RootElement.appendChild(child);


        child = doc.createElement("field");
        child.setAttribute("name", "spinner_sort"); 
        child.setAttribute("spinner_position", ""+spinner_sort);
        RootElement.appendChild(child);
        
        

        child = doc.createElement("field");
        child.setAttribute("name", "code"); 
        child.setAttribute("checked", (code_checked)?"1":"0");
        child.setAttribute("spinner_position", ""+spinner_code);
        child.setAttribute("value", code); 
        RootElement.appendChild(child);



        child = doc.createElement("field");
        child.setAttribute("name", "description"); 
        child.setAttribute("checked", (description_checked)?"1":"0");
        child.setAttribute("spinner_position", ""+spinner_description);
        child.setAttribute("value", description); 
        RootElement.appendChild(child);




        child = doc.createElement("field");
        child.setAttribute("name", "executor"); 
        child.setAttribute("checked", (executor_checked)?"1":"0");
        child.setAttribute("spinner_position", ""+spinner_executor);
        child.setAttribute("value", executor); 
        RootElement.appendChild(child);


        child = doc.createElement("field");
        child.setAttribute("name", "responsible"); 
        child.setAttribute("checked", (responsible_checked)?"1":"0");
        child.setAttribute("spinner_position", ""+spinner_responsible);
        child.setAttribute("value", responsible); 
        RootElement.appendChild(child);


        child = doc.createElement("field");
        child.setAttribute("name", "status"); 
        child.setAttribute("checked", (status_checked)?"1":"0");
        child.setAttribute("spinner_position", ""+spinner_status);
        child.setAttribute("value", status); 
        RootElement.appendChild(child);




        child = doc.createElement("field");
        child.setAttribute("name", "category"); 
        child.setAttribute("checked", (category_checked)?"1":"0");
        child.setAttribute("spinner_position", ""+spinner_category);
        child.setAttribute("value", category); 
        RootElement.appendChild(child);



        child = doc.createElement("field");
        child.setAttribute("name", "show_empty_date_checked"); 
        child.setAttribute("checked", (show_empty_date_checked)?"1":"0");
        RootElement.appendChild(child);       
        


        child = doc.createElement("field");
        child.setAttribute("name", "date"); 
        child.setAttribute("checked", (date_checked)?"1":"0");
        child.setAttribute("spinner_position", ""+spinner_date);
        //child.setAttribute("value_date_from", ""+date_from.getTime()); 
        //child.setAttribute("value_date_to", ""+date_to.getTime()); 
        
        if (spinner_date_from>0 && spinner_date_from<8){ child.setAttribute("value_date_from", ""+spinner_date_from); }
        else{child.setAttribute("value_date_from", sdf_yyyyMMdd.format(date_from)); }

        if (spinner_date_from>0 && spinner_date_from<8){ child.setAttribute("value_date_to", ""+spinner_date_to); }
        else{child.setAttribute("value_date_to", sdf_yyyyMMdd.format(date_to));}       

        RootElement.appendChild(child);



        child = doc.createElement("field");
        child.setAttribute("name", "duedate"); 
        child.setAttribute("checked", (duedate_checked)?"1":"0");
        child.setAttribute("spinner_position", ""+spinner_duedate);;
        
        if (spinner_duedate_from>0 && spinner_duedate_from<8){ child.setAttribute("value_duedate_from", ""+spinner_duedate_from); }
        else{child.setAttribute("value_duedate_from", sdf_yyyyMMdd.format(duedate_from)); }

        if (spinner_duedate_to>0 && spinner_duedate_to<8){ child.setAttribute("value_duedate_to", ""+spinner_duedate_to); }
        else{child.setAttribute("value_duedate_to", sdf_yyyyMMdd.format(duedate_to));}            
        
        RootElement.appendChild(child);




        child = doc.createElement("field");
        child.setAttribute("name", "duedate"); 
        child.setAttribute("checked", (releasedate_checked)?"1":"0");
        child.setAttribute("spinner_position", ""+spinner_releasedate);

        if (spinner_releasedate_from>0 && spinner_releasedate_from<8){ child.setAttribute("value_releasedate_from", ""+spinner_releasedate_from); }
        else{child.setAttribute("value_releasedate_from", sdf_yyyyMMdd.format(releasedate_from)); }

        if (spinner_releasedate_to>0 && spinner_releasedate_to<8){ child.setAttribute("value_releasedate_to", ""+spinner_releasedate_to); }
        else{child.setAttribute("value_releasedate_to", sdf_yyyyMMdd.format(releasedate_to));}            
               
        
        RootElement.appendChild(child);


        
        //xmlstring = doc.getXmlEncoding();
        //xmlstring = doc.getTextContent();
        xmlstring = getStringFromNode(RootElement);
	}
	
	public void CustomizeExecutorResponsible(Context context)
	{
		
		if (executor.startsWith("content:"))
		{

			//Uri contactData = Uri.parse(executor);
			//ContentResolver cr = context.getContentResolver();
			//Cursor c_contact =  cr.query(contactData, null, null, null, null);
			//c_contact.moveToFirst();	
			//String contactname =  c_contact.getString(c_contact.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));	
			String contactname = CommonFunctions.GetContactNameFromString((Activity) context,executor);
			executorname = contactname;
		}		
		else
		{
			executorname = executor;
		}
		
		if (responsible.startsWith("content:"))
		{
			//Uri contactData = Uri.parse(responsible);
			//ContentResolver cr = context.getContentResolver();
			//Cursor c_contact =  cr.query(contactData, null, null, null, null);
			//c_contact.moveToFirst();	
			//String contactname =  c_contact.getString(c_contact.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			String contactname = CommonFunctions.GetContactNameFromString((Activity) context,responsible);
			responsiblename = contactname;
		
		}		
		else
		{
			responsiblename = responsible;
		}	

	}
	
	public static String getStringFromNode(Element root){

        StringBuilder result = new StringBuilder();

        if (root.getNodeType() == 3)
            result.append(root.getNodeValue());
        else {
            if (root.getNodeType() != 9) {
                StringBuffer attrs = new StringBuffer();
                for (int k = 0; k < root.getAttributes().getLength(); ++k) {
                    attrs.append(" ").append(
                            root.getAttributes().item(k).getNodeName()).append(
                            "=\"").append(
                            root.getAttributes().item(k).getNodeValue())
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
	
	
}