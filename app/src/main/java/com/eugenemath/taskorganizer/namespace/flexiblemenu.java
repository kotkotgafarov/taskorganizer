package com.eugenemath.taskorganizer.namespace;

 

import java.util.ArrayList;

import com.eugenemath.taskorganizer.namespace.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class flexiblemenu extends Activity {

	public MySimpleArrayAdapter aa;
	public String Regim = "EditDelete";
	public int executorresponsible = 0;
	
	
	public class MySimpleArrayAdapter extends ArrayAdapter<String> {
		private final Context context;
		private final String[] values;

		public MySimpleArrayAdapter(Context context, String[] values) {
			super(context, R.layout.rowmenu, values);
			this.context = context;
			this.values = values;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.rowmenu, parent, false);
			TextView textView = (TextView) rowView.findViewById(R.id.textViewflexiblemenu);
			ImageView imageView = (ImageView) rowView.findViewById(R.id.imageViewfelxiblemenu);
			textView.setText(values[position]);
		
			String s = values[position];
			if (Regim.equals("EditDelete") || Regim.equals("EditDeleteChild"))
			{
				if (s.startsWith(getString(R.string.from_code44))) {imageView.setImageResource(android.R.drawable.ic_menu_edit);} 
				else if (s.startsWith(getString(R.string.from_code45))) {imageView.setImageResource(android.R.drawable.ic_menu_delete);}
				else if (s.startsWith(getString(R.string.from_code46))) {imageView.setImageResource(android.R.drawable.ic_menu_add);}
			}	
			else if (Regim.equals("ChooseEmailSMS"))
			{
				imageView.setImageResource(R.drawable.ic_menu_send);
			}
			else if (Regim.equals("ConsumedTime"))
			{
				//imageView.setImageResource(R.drawable.ic_menu_send);
			}
			else
			{
				if (s.startsWith(getString(R.string.from_code48))) {imageView.setImageResource(R.drawable.ic_menu_close_clear_cancel);} 
				else {imageView.setImageResource(R.drawable.ic_menu_cc);}
			}				

			return rowView;
		}
	}	
	
	
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		// Inflate your view
		setContentView(R.layout.flexiblemenu);
		ListView myListView = (ListView)findViewById(R.id.myListView);
	

	    
		Intent i = getIntent();
		Regim = i.getExtras().getString("Regim");	    		

		final String[] Items;
		
		if (Regim.equals("EditDelete"))
		{
			Items = new String[] {getString(R.string.from_code44),getString(R.string.from_code45)};
		}
		else if (Regim.equals("EditDeleteChild"))
		{
			Items = new String[] {getString(R.string.from_code44),getString(R.string.from_code45),getString(R.string.from_code46)};
		}
		else if (Regim.equals("ConsumedTime"))
		{
			Items = new String[] {getString(R.string.from_code65),getString(R.string.from_code66)};
		}
		else if (Regim.equals("ChooseEmailSMS"))
		{
			String Phone = i.getExtras().getString("Phone");
			String Email = i.getExtras().getString("Email");
			Items = new String[] {Phone,Email};
			executorresponsible = 0;
		}	
		else if (Regim.equals("ParentClear"))
		{
			Items = new String[] {getString(R.string.from_code47),getString(R.string.from_code48)};
			executorresponsible = -1;
		}		
		else
		{
			Items = new String[] {getString(R.string.from_code49),getString(R.string.from_code48)};
			executorresponsible = i.getExtras().getInt("executorresponsible");
		}

		aa = new MySimpleArrayAdapter(this, Items);		

	    myListView.setAdapter(aa);		
	    myListView.setOnItemClickListener(new OnItemClickListener(){
	    	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent();
				intent.putExtra("Result", position);
				intent.putExtra("Regim", Regim);
				intent.putExtra("executorresponsible", executorresponsible);
				setResult(RESULT_OK,intent);
				finish();	    		
	    	}
	    });
	    

	}
	

	
}