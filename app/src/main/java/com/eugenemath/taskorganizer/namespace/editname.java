package com.eugenemath.taskorganizer.namespace;

 

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class editname extends Activity {


	
	
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		// Inflate your view
		setContentView(R.layout.editname);
		final EditText myEditText = (EditText)findViewById(R.id.editnametextfield);
		TextView myTextView = (TextView)findViewById(R.id.textView1);

		
		Intent i = getIntent();
		String CurrentName = i.getExtras().getString("Name");
		String Title = i.getExtras().getString("Title");
		myTextView.setText(Title);
		
		myEditText.setText(CurrentName);
		
		Button myButtonSave = (Button)findViewById(R.id.button1);
		Button myButtonCancel = (Button)findViewById(R.id.button2);
	    

		myButtonSave.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("Name", myEditText.getText().toString());
				intent.putExtra("Result", 1);
				setResult(RESULT_OK,intent);
				finish();					
				}});

		myButtonCancel.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("Result", 0);
				intent.putExtra("Name", "");
				setResult(RESULT_OK,intent);
				finish();					
				}});		


	}
	

	
}