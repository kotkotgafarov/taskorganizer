package com.eugenemath.taskorganizer.namespace;

 

import com.eugenemath.taskorganizer.namespace.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class licenseagreement extends Activity {


	
	
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		
		
		// Inflate your view
		setContentView(R.layout.licenseagreement);
		
		String PREFS_NAME = "PrefsTaskOrganizer";
		final SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		
		
	    if(!settings.getBoolean("LicenseAccepted", false))
		{

			final SharedPreferences.Editor prefsEditor = settings.edit();
 
			//this.licenseView = this.findViewById(R.layout.licenseagreement);
			final View  licenseView = View.inflate(this, R.layout.licenseagreement, null);
			this.setContentView(licenseView);
			final CheckBox checkboxAcceptLicense = (CheckBox)this.findViewById(R.id.checkLicenseAgreement);
			//TextView textviewLicense = (TextView)this.findViewById(R.id.textviewLicenseAgreement);
			Button buttonOK= (Button)this.findViewById(R.id.buttonOK);
			Button buttonCancel = (android.widget.Button)this.findViewById(R.id.buttonCancel);
			buttonCancel.setOnClickListener(
					new View.OnClickListener()
					{
						public void onClick(View view)
						{
							Intent intent = new Intent();
							intent.putExtra("Result", 1);
							setResult(RESULT_CANCELED,intent);
							finish();
						}
					}
					);
			buttonOK.setOnClickListener(
					new View.OnClickListener()
					{
						public void onClick(View view)
						{
							if(checkboxAcceptLicense.isChecked())
							{
								prefsEditor.putBoolean("LicenseAccepted", true);
								prefsEditor.commit();
								//licenseView.setVisibility(View.INVISIBLE);
								Intent intent = new Intent();
								intent.putExtra("Result", 1);
								setResult(RESULT_OK,intent);
								finish();	
							}

						}
					});
		}
		else
		{
			Intent intent = new Intent();
			intent.putExtra("Result", 1);
			setResult(RESULT_OK,intent);
			finish();	
		}
	}
	

	
}