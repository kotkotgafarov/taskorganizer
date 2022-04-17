package com.eugenemath.taskorganizer.namespace;

import java.util.ArrayList;
import java.util.List;

import com.eugenemath.taskorganizer.namespace.ColorPickerDialog.OnColorChangedListener;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;

public class RainbowPickerDialog extends Dialog {

	
	private OnColorChangedListener mListener;
	private Activity a;
	private String key;
	private RainbowPickerAdapter rpa;
	
    public interface OnColorChangedListener {
        void colorChanged(String key, int color);
    }
	
	
	public class RainbowPickerAdapter extends BaseAdapter {

		private Context context;
		public List<Integer> colorList = new ArrayList<Integer>();
		int colorGridColumnWidth;

		public RainbowPickerAdapter(Context context) {

			this.context = context;

			// defines the width of each color square
			//DisplayMetrics metrics = new DisplayMetrics();
			//a.getWindowManager().getDefaultDisplay().getMetrics(metrics);
			
			colorGridColumnWidth = 75;
			//colorGridColumnWidth = (int)((float)(metrics.widthPixels-30)/6);
			

			int colorCount = 96;
			int step = 256 / (colorCount / 6);
			int red = 0, green = 0, blue = 0;

			// FF 00 00 --> FF FF 00
			for (red = 255, green = 0, blue = 0; green <= 255; green += step)
				colorList.add(Color.rgb(red, green, blue));

			// FF FF 00 --> 00 FF 00
			for (red = 255, green = 255, blue = 0; red >= 0; red -= step)
				colorList.add(Color.rgb(red, green, blue));

			// 00 FF 00 --> 00 FF FF
			for (red = 0, green = 255, blue = 0; blue <= 255; blue += step)
				colorList.add(Color.rgb(red, green, blue));

			// 00 FF FF -- > 00 00 FF
			for (red = 0, green = 255, blue = 255; green >= 0; green -= step)
				colorList.add(Color.rgb(red, green, blue));

			// 00 00 FF --> FF 00 FF
			for (red = 0, green = 0, blue = 255; red <= 255; red += step)
				colorList.add(Color.rgb(red, green, blue));

			// FF 00 FF -- > FF 00 00
			for (red = 255, green = 0, blue = 255; blue >= 0; blue -= 256 / step)
				colorList.add(Color.rgb(red, green, blue));

			// add gray colors
			for (int i = 255; i >= 0; i -= 11) {
				colorList.add(Color.rgb(i, i, i));
			}
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView;

			// can we reuse a view?
			if (convertView == null) {
				imageView = new ImageView(context);
				// set the width of each color square
				imageView.setLayoutParams(new GridView.LayoutParams(colorGridColumnWidth, colorGridColumnWidth));

			} else {
				imageView = (ImageView) convertView;
			}

			imageView.setBackgroundColor(colorList.get(position));
			imageView.setId(position);

			return imageView;
		}

		public int getCount() {
			return colorList.size();
		}

		public Object getItem(int position) {
			return colorList.get(position);
		}

		public long getItemId(int position) {
			return 0;
		}
	}	
	
	
	
	public RainbowPickerDialog(Context context, OnColorChangedListener listener,String key) {
		super(context);
		this.setTitle(getContext().getString(R.string.from_layout45));
		this.a = (Activity) context;
		this.key = key;
		mListener = listener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.color_picker);

		GridView gridViewColors = (GridView) findViewById(R.id.gridViewColors);
		rpa = new RainbowPickerAdapter(getContext());
		gridViewColors.setAdapter(rpa);
		
		//this.getWindow().setBackgroundDrawableResource(Color.WHITE);
		//this.getWindow().setTitleColor(Color.WHITE);
		//this.getWindow().setBackgroundDrawable(Color.WHITE);

		// close the dialog on item click
		gridViewColors.setOnItemClickListener(new OnItemClickListener() {
			//@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mListener.colorChanged(key, rpa.colorList.get(position));
				RainbowPickerDialog.this.dismiss();
			}
		});
	}
}
