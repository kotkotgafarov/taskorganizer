package com.eugenemath.taskorganizer.namespace;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.text.method.SingleLineTransformationMethod;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class taskorganizerActivity extends Activity {
	/** Called when the activity is first created. */
	static final private int MENU_ADD = Menu.FIRST;
	static final private int MENU_FILTER = Menu.FIRST + 1;
	static final private int MENU_FILTER_SAVED = Menu.FIRST + 2;
	static final private int MENU_PREFERENCES = Menu.FIRST + 3;
	static final private int MENU_CATALOGS = Menu.FIRST + 4;
	static final private int MENU_CATEGORIES = Menu.FIRST + 5;
	static final private int MENU_STATUSES = Menu.FIRST + 6;

	static final private int MENU_FROM_CALENDAR = Menu.FIRST + 7;
	static final private int MENU_TO_CALENDAR = Menu.FIRST + 8;
	static final private int MENU_EXPORT = Menu.FIRST + 9;
	static final private int MENU_IMPORT = Menu.FIRST + 10;
	static final private int MENU_GROUP_EDITING = Menu.FIRST + 11;
	static final private int MENU_RECEIVE_EMAILS = Menu.FIRST + 12;

	static final private int MENU_ADDITIONAL = Menu.FIRST + 13;

	static final int Task_DIALOG = 1;
	static final int SHOW_PREFERENCES = 2;
	static final int SHOW_LIST_OF_CATALOGS = 3;
	static final int REQUEST_CONTEXT_MENU = 4;
	static final int DIALOG_DELETE_TASK = 5;
	static final int TASK_FORM_OPENED = 6;
	static final int FILTER_FORM_OPENED = 7;
	static final int FILTER_CHOOSE_SAVED = 8;
	static final int PREFERENCES = 9;
	static final int dialog_filter_description = 10;
	static final int GROUP_EDITING = 11;
	static final int CHOOSE_ADDITIONAL = 12;
	static final int REQUEST_CONTEXT_MENU_SMS_EMAIL = 13;
	static final int REQUEST_DATE_TIME = 14;
	static final int LICENSE = 15;

	Long CurrentChoosenId;
	String CurrentChoosenName;
	ListView tasksListView;
	SimpleDateFormat sdf_ddMM = new SimpleDateFormat("dd MMM yyyy");
	private String EmailSendTo = "";
	private String SMSSendTo = "";
	private Task CurrentTaskToSendStatus;
	private int CurrentPage = 0;
	private int PreviousPage = -1;

	public Filter filter;
	private Cursor c;
	private Cursor c_statuses;
	private static SQLiteDatabase tasksDB;
	public MySimpleCursorAdapter ll;
	public TaskHierarchyLIstViewAdapter ll2;

	private String STATUS_ACTIVE = "Active";
	private String STATUS_COMPLETED = "Completed";

	public Map<String, String> ExecutorMap = new LinkedHashMap<String, String>();

	private GestureDetector myGestureDetector;
	private View.OnTouchListener myGestureListener;

	private static final String PREFS_NAME = "PrefsTaskOrganizer";
	private Long id_default_filter = (long) -1;
	private boolean send_sms_automatically = true;
	private boolean first_send_email = true;
	private boolean toodoledo_every_60_minutes = false;
	private String toodoledo_email = "";
	private String toodoledo_pass = "";
	private String emailimap = "";
	private String emailuser = "";
	private String emailpass = "";
	private int emailperiod = 0;
	
	private int fontsize = 0;
	
	boolean itWasMowing = false;
	boolean isGroupEditing = false;

	List<String> listofchoosenid = new ArrayList<String>();

	
	private Handler mHandler = new Handler();
	private Handler mHandlerEmail = new Handler();
	
	private Runnable mSyncToodledo = new Runnable() {
		   public void run() { 
		       //mHandler.postAtTime(this,start + (((minutes * 60) + seconds + 1) * 1000));
			 
			   CommonFunctions.SyncToodledo(taskorganizerActivity.this,toodoledo_email,toodoledo_pass);
			   //mHandler.postDelayed(this, 3600*1000);
			   //Toast.makeText(taskorganizerActivity.this, getString(R.string.from_code87)+" "+0,Toast.LENGTH_SHORT).show();
			   mHandler.removeCallbacks(mSyncToodledo);
			   mHandler.postDelayed(this, 3600*1000);
		   }
		};
		
	private Runnable mReceiveEmails = new Runnable() {
		public void run() {
			if (CommonFunctions.isNetworkAvailable(taskorganizerActivity.this)) {
				// Toast.makeText(act,
				// act.getString(R.string.from_code85),Toast.LENGTH_SHORT).show();
				MyMail mymail = new MyMail();

				mymail._host = emailimap;
				mymail._user = emailuser;
				mymail._pass = emailpass;
				mymail.act = taskorganizerActivity.this;
				
				
				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
				
				int lastnumber = 0;

				try
				{
					lastnumber = Integer.parseInt(settings.getString("lastemailid", "0"));
				}
				 catch(NumberFormatException e) 
				 { 
					 
				 }
				
				
				mymail.lastnumber = lastnumber;

				try {
					boolean success = mymail.receive();

					/*
					 * if (success){ Toast.makeText(taskorganizerActivity.this,
					 * getString(R.string.from_code51),
					 * Toast.LENGTH_SHORT).show(); } else {
					 * Toast.makeText(taskorganizerActivity.this,
					 * getString(R.string.from_code52),
					 * Toast.LENGTH_SHORT).show(); }
					 */

				} catch (Exception e) {
					// TODO Auto-generated catch block
					Toast.makeText(getBaseContext(),
							getString(R.string.from_code52), Toast.LENGTH_SHORT)
							.show();
					e.printStackTrace();
				}

				//Toast.makeText(taskorganizerActivity.this, "Email received ",	Toast.LENGTH_SHORT).show();
				mHandlerEmail.removeCallbacks(mReceiveEmails);
				mHandlerEmail.postDelayed(mReceiveEmails, emailperiod * 1000);
			}
		}
	};
	
	
	// for calendar
	private GridView calendarView;
	private GridCellAdapter monthadapter;
	private Calendar _calendar;
	private int monthview_currentmonth, monthview_currentyear;
	private Date current_date;
	private final DateFormat dateFormatter = new DateFormat();
	private boolean isRunned = false;
	private int day_off_color;
	// private static final String dateTemplate = "MMMM yyyy";
	public static String WIDGET_REFRESHED = "com.eugenemath.taskorganizer.TASKS_REFRESHED";

	public float scale;
	public int SWIPE_MIN_DISTANCE;
	public int height_of_textview = 0;

	// view pagers
	ViewPager viewpager_day;
	private PagerAdapterDay pagerAdapterDay;
	RelativeLayout rl_viewpager_day_left;
	RelativeLayout rl_viewpager_day_right;
	RelativeLayout rl_viewpager_day_center;
	ScrollView scrollviewdayleft, scrollviewdayright, scrollviewdaycenter;

	ViewPager viewpager_week;
	private PagerAdapterWeek pagerAdapterWeek;
	RelativeLayout rl_viewpager_week_left;
	RelativeLayout rl_viewpager_week_right;
	RelativeLayout rl_viewpager_week_center;
	ScrollView scrollviewweekleft, scrollviewweekright, scrollviewweekcenter;

	ViewPager viewpager_month;
	private PagerAdapterMonth pagerAdapterMonth;
	RelativeLayout rl_viewpager_month_left;
	RelativeLayout rl_viewpager_month_right;
	RelativeLayout rl_viewpager_month_center;
	ScrollView scrollviewmonthleft, scrollviewmonthright,
			scrollviewmonthcenter;
	TextView tv_left, tv_right;

	private void InitiatePagerAdapters() {

		// day
		pagerAdapterDay = new PagerAdapterDay();
		viewpager_day = (ViewPager) findViewById(R.id.viewpager_day);
		viewpager_day.setAdapter(pagerAdapterDay);
		viewpager_day.setCurrentItem(1, false);

		RelativeLayout.LayoutParams params;
		LayoutParams params0;

		scrollviewdayleft = new ScrollView(this);
		params0 = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		scrollviewdayleft.setLayoutParams(params0);

		rl_viewpager_day_left = new RelativeLayout(this);
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		rl_viewpager_day_left.setBackgroundColor(Color.WHITE);
		rl_viewpager_day_left.setLayoutParams(params);
		scrollviewdayleft.addView(rl_viewpager_day_left, params);
		FillDay(0);

		scrollviewdayright = new ScrollView(this);
		scrollviewdayright.setLayoutParams(params0);

		rl_viewpager_day_right = new RelativeLayout(this);
		rl_viewpager_day_right.setBackgroundColor(Color.WHITE);
		rl_viewpager_day_right.setLayoutParams(params);
		scrollviewdayright.addView(rl_viewpager_day_right, params);
		FillDay(2);

		scrollviewdaycenter = new ScrollView(this);
		scrollviewdaycenter.setLayoutParams(params0);

		rl_viewpager_day_center = new RelativeLayout(this);
		rl_viewpager_day_center.setBackgroundColor(Color.WHITE);
		rl_viewpager_day_center.setLayoutParams(params);
		scrollviewdaycenter.addView(rl_viewpager_day_center, params);

		// ---------------------------------week
		pagerAdapterWeek = new PagerAdapterWeek();
		viewpager_week = (ViewPager) findViewById(R.id.viewpager_week);
		viewpager_week.setAdapter(pagerAdapterWeek);
		viewpager_week.setCurrentItem(1, false);

		scrollviewweekleft = new ScrollView(this);
		params0 = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		scrollviewweekleft.setLayoutParams(params0);

		rl_viewpager_week_left = new RelativeLayout(this);
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		rl_viewpager_week_left.setBackgroundColor(Color.WHITE);
		rl_viewpager_week_left.setLayoutParams(params);
		scrollviewweekleft.addView(rl_viewpager_week_left, params);
		FillWeek(0);

		scrollviewweekright = new ScrollView(this);
		scrollviewweekright.setLayoutParams(params0);

		rl_viewpager_week_right = new RelativeLayout(this);
		rl_viewpager_week_right.setBackgroundColor(Color.WHITE);
		rl_viewpager_week_right.setLayoutParams(params);
		scrollviewweekright.addView(rl_viewpager_week_right, params);
		FillWeek(2);

		scrollviewweekcenter = new ScrollView(this);
		scrollviewweekcenter.setLayoutParams(params0);

		rl_viewpager_week_center = new RelativeLayout(this);
		rl_viewpager_week_center.setBackgroundColor(Color.WHITE);
		rl_viewpager_week_center.setLayoutParams(params);
		scrollviewweekcenter.addView(rl_viewpager_week_center, params);

		// ---------------------------------month
		pagerAdapterMonth = new PagerAdapterMonth();
		viewpager_month = (ViewPager) findViewById(R.id.viewpager_month);
		viewpager_month.setAdapter(pagerAdapterMonth);
		viewpager_month.setCurrentItem(1, false);

		scrollviewmonthleft = new ScrollView(this);
		params0 = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		scrollviewmonthleft.setLayoutParams(params0);

		rl_viewpager_month_left = new RelativeLayout(this);
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		rl_viewpager_month_left.setBackgroundColor(Color.WHITE);
		rl_viewpager_month_left.setLayoutParams(params);
		scrollviewmonthleft.addView(rl_viewpager_month_left, params);
		// FillMonth(0);

		tv_left = new TextView(this);
		// tv.setTextAppearance(this, android.R.style.TextAppearance_Large);
		tv_left.setTextColor(Color.parseColor("#2c2c2c"));
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		tv_left.setLayoutParams(params);
		tv_left.setGravity(Gravity.CENTER | Gravity.CENTER);
		tv_left.setTextSize(30);
		rl_viewpager_month_left.addView(tv_left, params);

		scrollviewmonthright = new ScrollView(this);
		scrollviewmonthright.setLayoutParams(params0);

		rl_viewpager_month_right = new RelativeLayout(this);
		rl_viewpager_month_right.setBackgroundColor(Color.WHITE);
		rl_viewpager_month_right.setLayoutParams(params);
		scrollviewmonthright.addView(rl_viewpager_month_right, params);
		// FillMonth(2);

		tv_right = new TextView(this);
		// tv.setTextAppearance(this, android.R.style.TextAppearance_Large);
		tv_right.setTextColor(Color.parseColor("#2c2c2c"));
		// params = new
		// RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
		tv_right.setLayoutParams(params);
		tv_right.setGravity(Gravity.CENTER | Gravity.CENTER);
		tv_right.setTextSize(30);
		rl_viewpager_month_right.addView(tv_right, params);

		scrollviewmonthcenter = new ScrollView(this);
		scrollviewmonthcenter.setLayoutParams(params0);

		rl_viewpager_month_center = new RelativeLayout(this);
		rl_viewpager_month_center.setBackgroundColor(Color.WHITE);
		rl_viewpager_month_center.setLayoutParams(params);
		scrollviewmonthcenter.addView(rl_viewpager_month_center, params);

	}

	private class PagerAdapterDay extends PagerAdapter {

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public Object instantiateItem(View collection, int position) {
			// TextView tv = new TextView(cxt);
			// Toast.makeText(getBaseContext(), "instantiateItem.. "+position,
			// Toast.LENGTH_SHORT).show();

			// final LayoutInflater inflater =
			// LayoutInflater.from(getBaseContext());
			/*
			 * final View ll = inflater.inflate(R.layout.ll, null);
			 * ll.setTag(""+position); TextView tv =
			 * (TextView)ll.findViewById(R.id.textView1);
			 * tv.setTextColor(Color.WHITE); tv.setText("Bonjour PAUG " +
			 * position); ((ViewPager) collection).addView(ll,0);
			 */

			/*
			 * tv.setText("Bonjour PAUG " + position);
			 * tv.setTextColor(Color.WHITE); tv.setTextSize(30);
			 * 
			 * ((ViewPager) collection).addView(tv,0);
			 */

			ScrollView scr = scrollviewdayleft;

			if (position == 0) {
				scr = scrollviewdayleft;
			} else if (position == 1) {
				scr = scrollviewdaycenter;
			} else if (position == 2) {
				scr = scrollviewdayright;
			}
			((ViewPager) collection).addView(scr, 0);

			return scr;
		}

		// private View mCurrentView;

		@Override
		public void setPrimaryItem(ViewGroup container, int position,
				Object object) {
			if (position == 0) {
				ChangeFilter(false);
				viewpager_day.setCurrentItem(1, false);
			} else if (position == 2) {
				ChangeFilter(true);
				viewpager_day.setCurrentItem(1, false);
			}
		}

		@Override
		public void destroyItem(View collection, int position, Object view) {
			// Toast.makeText(getBaseContext(), "destroyItem.. "+position,
			// Toast.LENGTH_SHORT).show();
			((ViewPager) collection).removeView((ScrollView) view);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == ((ScrollView) object);
		}

		@Override
		public void finishUpdate(View arg0) {// Toast.makeText(getBaseContext(),
												// "finishUpdate "+arg0.getTag(),
												// Toast.LENGTH_SHORT).show();
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {// Toast.makeText(getBaseContext(),
											// "startUpdate "+arg0.getTag(),
											// Toast.LENGTH_SHORT).show();
		}

	}

	private class PagerAdapterWeek extends PagerAdapter {

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public Object instantiateItem(View collection, int position) {
			ScrollView scr = scrollviewweekleft;

			if (position == 0) {
				scr = scrollviewweekleft;
			} else if (position == 1) {
				scr = scrollviewweekcenter;
			} else if (position == 2) {
				scr = scrollviewweekright;
			}
			((ViewPager) collection).addView(scr, 0);

			return scr;
		}

		// private View mCurrentView;

		@Override
		public void setPrimaryItem(ViewGroup container, int position,
				Object object) {
			if (position == 0) {
				ChangeFilter(false);
				viewpager_week.setCurrentItem(1, false);
			} else if (position == 2) {
				ChangeFilter(true);
				viewpager_week.setCurrentItem(1, false);
			}
		}

		@Override
		public void destroyItem(View collection, int position, Object view) {
			// Toast.makeText(getBaseContext(), "destroyItem.. "+position,
			// Toast.LENGTH_SHORT).show();
			((ViewPager) collection).removeView((ScrollView) view);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == ((ScrollView) object);
		}

		@Override
		public void finishUpdate(View arg0) {// Toast.makeText(getBaseContext(),
												// "finishUpdate "+arg0.getTag(),
												// Toast.LENGTH_SHORT).show();
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {// Toast.makeText(getBaseContext(),
											// "startUpdate "+arg0.getTag(),
											// Toast.LENGTH_SHORT).show();
		}

	}

	private class PagerAdapterMonth extends PagerAdapter {

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public Object instantiateItem(View collection, int position) {
			ScrollView scr = scrollviewmonthleft;

			if (position == 0) {
				scr = scrollviewmonthleft;
			} else if (position == 1) {
				scr = scrollviewmonthcenter;
			} else if (position == 2) {
				scr = scrollviewmonthright;
			}
			((ViewPager) collection).addView(scr, 0);

			return scr;
		}

		// private View mCurrentView;

		@Override
		public void setPrimaryItem(ViewGroup container, int position,
				Object object) {
			if (position == 0) {
				ChangeFilter(false);
				viewpager_month.setCurrentItem(1, false);
				// FillMonth(0);
				// FillMonth(2);
			} else if (position == 2) {
				ChangeFilter(true);
				viewpager_month.setCurrentItem(1, false);
				// FillMonth(0);
				// FillMonth(2);
			}
		}

		@Override
		public void destroyItem(View collection, int position, Object view) {
			// Toast.makeText(getBaseContext(), "destroyItem.. "+position,
			// Toast.LENGTH_SHORT).show();
			((ViewPager) collection).removeView((ScrollView) view);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == ((ScrollView) object);
		}

		@Override
		public void finishUpdate(View arg0) {// Toast.makeText(getBaseContext(),
												// "finishUpdate "+arg0.getTag(),
												// Toast.LENGTH_SHORT).show();
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {// Toast.makeText(getBaseContext(),
											// "startUpdate "+arg0.getTag(),
											// Toast.LENGTH_SHORT).show();
		}

	}

	public static class AppRater {
		private final static String APP_TITLE = "Rabotan ToDo Calendar";
		// private final static String APP_PNAME = "Rabotan ToDo Calendar";

		private final static int DAYS_UNTIL_PROMPT = 0;
		private final static int LAUNCHES_UNTIL_PROMPT = 5;

		public static void app_launched(Context mContext) {
			SharedPreferences prefs = mContext.getSharedPreferences("apprater",
					0);
			if (prefs.getBoolean("dontshowagain", false)) {
				return;
			}

			SharedPreferences.Editor editor = prefs.edit();

			// Increment launch counter
			long launch_count = prefs.getLong("launch_count", 0) + 1;
			editor.putLong("launch_count", launch_count);

			// Get date of first launch
			Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
			if (date_firstLaunch == 0) {
				date_firstLaunch = System.currentTimeMillis();
				editor.putLong("date_firstlaunch", date_firstLaunch);
			}

			// Wait at least n days before opening
			if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
				if (System.currentTimeMillis() >= date_firstLaunch
						+ (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
					showRateDialog(mContext, editor);
				}
			}

			editor.commit();
		}

		public static void showRateDialog(final Context mContext,
				final SharedPreferences.Editor editor) {
			final Dialog dialog = new Dialog(mContext);
			dialog.setTitle(mContext.getString(R.string.from_code76) + " " + APP_TITLE);

			LinearLayout ll = new LinearLayout(mContext);
			ll.setOrientation(LinearLayout.VERTICAL);
			//ll.setBackgroundColor(Color.)

			TextView tv = new TextView(mContext);
			tv.setText(mContext.getString(R.string.from_code77) + " " + APP_TITLE + ""
					+ mContext.getString(R.string.from_code78));
			tv.setWidth(240);
			tv.setTextColor(Color.WHITE);
			tv.setPadding(4, 0, 4, 10);
			ll.addView(tv);

			Button b1 = new Button(mContext);
			b1.setText(mContext.getString(R.string.from_code76) + " " + APP_TITLE);
			b1.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri
							.parse("market://details?id=" + mContext.getPackageName())));
					if (editor != null) {
						editor.putBoolean("dontshowagain", true);
						editor.commit();
					}					
					dialog.dismiss();
				}
			});
			ll.addView(b1);

			Button b2 = new Button(mContext);
			b2.setText(mContext.getString(R.string.from_code79));
			b2.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			ll.addView(b2);

			Button b3 = new Button(mContext);
			b3.setText(mContext.getString(R.string.from_code80));
			b3.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if (editor != null) {
						editor.putBoolean("dontshowagain", true);
						editor.commit();
					}
					dialog.dismiss();
				}
			});
			ll.addView(b3);

			dialog.setContentView(ll);
			dialog.show();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/*if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0
				&& PreviousPage != -1 && CurrentPage != 0) {
			int PreviousPage0 = CurrentPage;
			CurrentPage = PreviousPage;
			PreviousPage = PreviousPage0;

			ShowOrHidePages();
			return true;
		}*/   //USERS WHANT TO HAVE EXIT. 


		return super.onKeyDown(keyCode, event);
	}

	/*
	 * public void onClick(View v) { String date_month_year = (String)
	 * v.getTag(); if (!date_month_year.contains("-")) { return; } String[]
	 * date_month_year_array = date_month_year.split("-");
	 * //selectedDayMonthYearButton.setText("Selected: " + date_month_year);
	 * Date parsedDate = new
	 * Date(Integer.valueOf(date_month_year_array[2])-1900,
	 * Integer.valueOf(date_month_year_array
	 * [1])-1,Integer.valueOf(date_month_year_array[0]));
	 * 
	 * 
	 * //try //{ //Date parsedDate = dateFormatter.parse(date_month_year);
	 * //Log.d(tag, "Parsed Date: " + parsedDate.toString()); LinearLayout
	 * linearLayout1 = (LinearLayout)findViewById(R.id.linearLayout1);
	 * LinearLayout linearLayout_month =
	 * (LinearLayout)findViewById(R.id.linearLayout_month); CurrentPage = 0;
	 * linearLayout1.setVisibility(View.VISIBLE);
	 * linearLayout_month.setVisibility(View.GONE); filter.date_checked = true;
	 * filter.spinner_date = 0; filter.date_from = parsedDate;
	 * filter.spinner_date_from = 0; loadTasksFromProvider(); }
	 */

	public OnCheckedChangeListener myOnCheckedChangeListener = new OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {

			Integer posInt = (Integer) buttonView.getTag();
			int id = posInt.intValue();

			if (isGroupEditing) {
				if (buttonView.isChecked()) {
					listofchoosenid.add("" + id);
				} else {
					if (listofchoosenid.contains("" + id)) {
						listofchoosenid.remove("" + id);
					}
				}
				return;
			}

			LinearLayout Parent = (LinearLayout) buttonView.getParent()
					.getParent();
			// TextView tvname = (TextView)Parent.getChildAt(1);
			TextView tvname = (TextView) Parent.findViewById(R.id.nameoftask);
			TextView tvDate = (TextView) Parent.findViewById(R.id.date);
			TextView tvduedate = (TextView) Parent.findViewById(R.id.duedate);
			TextView tvExecutor = (TextView) Parent.findViewById(R.id.executor);
			TextView tvCode = (TextView) Parent.findViewById(R.id.code);
			TextView tvResponsible = (TextView) Parent
					.findViewById(R.id.responsible);

			// LinearLayout UpParent = (LinearLayout)Parent.getParent();
			// TextView tvstatus =
			// (TextView)((LinearLayout)UpParent.getChildAt(0)).getChildAt(1);
			TextView tvstatus = (TextView) Parent.findViewById(R.id.status);

			ContentValues _initialValues = new ContentValues();
			String where = taskOrganizerDatabaseHelper.KEY_ID + "=" + id;
			Date CurrentDate = new Date();
			SimpleDateFormat sdf_history = new SimpleDateFormat(
					getString(R.string.sdf2));

			String TextForHistory = "";

			if (buttonView.isChecked()) {
				// Toast.makeText(getBaseContext(), "1id.. "+id,
				// Toast.LENGTH_SHORT).show();
				_initialValues.put(taskOrganizerDatabaseHelper.KEY_STATUS, 2);// COMPLETED
				TextForHistory = "" + sdf_history.format(CurrentDate)
						+ " STATUS = COMPLETED";
				tvname.setPaintFlags(tvname.getPaintFlags()
						| Paint.STRIKE_THRU_TEXT_FLAG);
				tvstatus.setText(STATUS_COMPLETED);
				tvname.setTextColor(Color.parseColor("#cacaca"));
				tvDate.setTextColor(Color.parseColor("#cacaca"));
				tvduedate.setTextColor(Color.parseColor("#cacaca"));
				tvResponsible.setTextColor(Color.parseColor("#cacaca"));
				tvCode.setTextColor(Color.parseColor("#cacaca"));
				tvExecutor.setTextColor(Color.parseColor("#cacaca"));

				for (int i = 0; i < ll2.list.size(); i++) {
					if (ll2.list.get(i).id == id) {
						c.moveToPosition(ll2.list.get(i).position);
						Date date = new Date(
								c.getLong(c
										.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATE)));
						if (date.equals(new Date(0))) {
							_initialValues.put(
									taskOrganizerDatabaseHelper.KEY_DATE,
									RoundDate(new Date(), 0).getTime());
							tvDate.setText(sdf_ddMM.format(RoundDate(
									new Date(), 0)));
						}
						break;
					}
				}

			} else {
				// Toast.makeText(getBaseContext(),"2id.. "+id,
				// Toast.LENGTH_SHORT).show();
				_initialValues.put(taskOrganizerDatabaseHelper.KEY_STATUS, 1);// ACTIVE
				TextForHistory = "" + sdf_history.format(CurrentDate)
						+ " STATUS = ACTIVE";
				tvname.setPaintFlags(tvname.getPaintFlags()
						& ~Paint.STRIKE_THRU_TEXT_FLAG);
				tvstatus.setText(STATUS_ACTIVE);

				tvname.setTextColor(Color.parseColor("#000000"));
				tvDate.setTextColor(Color.parseColor("#000000"));
				tvduedate.setTextColor(Color.parseColor("#000000"));
				tvResponsible.setTextColor(Color.parseColor("#000000"));
				tvCode.setTextColor(Color.parseColor("#000000"));
				tvExecutor.setTextColor(Color.parseColor("#000000"));
			}

			_initialValues.put(taskOrganizerDatabaseHelper.KEY_DATEMODIFIED, (new Date()).getTime());
			long rowID = tasksDB.update(
					taskOrganizerDatabaseHelper.Tasks_TABLE, _initialValues,
					where, null);

			ContentValues values = new ContentValues();
			values.put(taskOrganizerDatabaseHelper.History_task_id, id);
			values.put(taskOrganizerDatabaseHelper.History_NAME, TextForHistory);

			rowID = tasksDB.insert(taskOrganizerDatabaseHelper.History_Table,
					"item", values);

			// Task CurrentTask = new Task(getBaseContext(), Long.valueOf(id));
			Task CurrentTask = new Task(getBaseContext(), (long) id);
			sendBroadcast(new Intent(WIDGET_REFRESHED));

			if (buttonView.isChecked()) {
				CommonFunctions.UpdateConsumedTime(taskorganizerActivity.this,
						CurrentTask);
			}

			if (CurrentTask.responsible.length() > 0) {
				SendSMSorEmailByStatusChanging(Long.valueOf(id));
			}

			// find tripe
			for (int i = 0; i < ll2.list.size(); i++) {
				if (ll2.getItem(i).id == id) {
					if (buttonView.isChecked()) {
						ll2.getItem(i).status_id = 2;
					} else {
						ll2.getItem(i).status_id = 1;
					}
					break;
				}
			}

			// c.requery();
			// ll.notifyDataSetChanged();
		}
	};

	public void getTwoStatuses() {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String SelectQuery = "SELECT " + taskOrganizerDatabaseHelper.Status_ID
				+ " as _id," + taskOrganizerDatabaseHelper.Status_NAME
				+ " as name" + " FROM "
				+ taskOrganizerDatabaseHelper.Statuses_Table;

		Cursor _c = tasksDB.rawQuery(SelectQuery, null);

		if (_c.moveToFirst()) {
			STATUS_ACTIVE = _c.getString(_c.getColumnIndex("name"));
			if (_c.moveToNext()) {
				STATUS_COMPLETED = _c.getString(_c.getColumnIndex("name"));
			}

		}
	}

	public void SendSMSorEmailByStatusChanging(Long _id) {
		CurrentTaskToSendStatus = new Task(getBaseContext(), _id);
		SMS mySMS;
		if (send_sms_automatically) {
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			first_send_email = settings.getBoolean("first_send_email", true);
			if (first_send_email) {
				String email = CommonFunctions.FindEmailOfContact(
						getBaseContext(), CurrentTaskToSendStatus.responsible);
				if (email.length() != 0) {
					SendEmail(CurrentTaskToSendStatus, email);
				} else {
					mySMS = new SMS(getBaseContext(), CurrentTaskToSendStatus,
							true, null);// to responsible
					if (!mySMS.ErrorByCreating) {
						mySMS.SendSMS(getBaseContext());
					}
				}
			} else {
				mySMS = new SMS(getBaseContext(), CurrentTaskToSendStatus,
						true, null);// to responsible
				if (!mySMS.ErrorByCreating) {
					mySMS.SendSMS(getBaseContext());
				} else {
					String email = CommonFunctions.FindEmailOfContact(
							getBaseContext(),
							CurrentTaskToSendStatus.responsible);
					if (email.length() != 0) {
						SendEmail(CurrentTaskToSendStatus, email);
					}
				}
			}
		} else {

			String contactname = CommonFunctions.GetContactNameFromString(
					taskorganizerActivity.this,
					CurrentTaskToSendStatus.responsible);
			// ask user to send email or sms
			String email = CommonFunctions.FindEmailOfContact(getBaseContext(),
					CurrentTaskToSendStatus.responsible);

			Intent i = new Intent(taskorganizerActivity.this,
					flexiblemenu.class);
			i.putExtra("Regim", "ChooseEmailSMS");
			i.putExtra("Phone", getString(R.string.from_code1) + contactname);
			i.putExtra("Email", getString(R.string.from_code2) + contactname);
			EmailSendTo = "";
			SMSSendTo = "";
			boolean ThereIsToChoose = false;

			if (email.length() != 0) {
				i.putExtra("Email", getString(R.string.from_code3)
						+ contactname + "\n email:" + email);
				EmailSendTo = email;
				ThereIsToChoose = true;
			}

			String phoneNumber = CommonFunctions.FindPhoneOfContact(
					getBaseContext(), CurrentTaskToSendStatus.responsible);
			if (phoneNumber.length() != 0) {
				i.putExtra("Phone", getString(R.string.from_code4)
						+ contactname + "\n phone:" + phoneNumber);
				SMSSendTo = phoneNumber;
				ThereIsToChoose = true;
			}

			if (ThereIsToChoose) {
				startActivityForResult(i, REQUEST_CONTEXT_MENU_SMS_EMAIL);
			}

		}
	}

	public void SendEmail(Task CurrentTask, String email) {
		MyMail mymail = new MyMail();
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		mymail._host = settings.getString("smtp", "smtp.gmail.com");
		mymail._user = settings.getString("user", "");
		mymail._pass = settings.getString("pass", "");
		mymail._send_to = email;
		mymail._auth = true;
		mymail._from = settings.getString("from", mymail._user + "@gmail.com");

		mymail.FillSubjectByTask(CurrentTask, true);
		try {
			if (mymail.send()) {
				Toast.makeText(
						this,
						getString(R.string.from_code5) + email + " "
								+ mymail._subject, Toast.LENGTH_LONG).show();
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast.makeText(this, getString(R.string.from_code6),
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}

	public int getPixels(float dip) {
		return Math.round(dip * scale);
	}

	public class MySimpleCursorAdapter extends SimpleCursorAdapter {

		public MySimpleCursorAdapter(Context context, int layout,
				Cursor cursor, String[] from, int[] to) {
			super(context, layout, cursor, from, to);
		}

		@Override
		public void bindView(View rowView, Context context, Cursor cursor) {
			TextView tvDate = (TextView) rowView.findViewById(R.id.date);
			TextView tvduedate = (TextView) rowView.findViewById(R.id.duedate);
			TextView tvname = (TextView) rowView.findViewById(R.id.nameoftask);
			TextView tvStatus = (TextView) rowView.findViewById(R.id.status);
			TextView tvCategory = (TextView) rowView
					.findViewById(R.id.category);
			TextView tvExecutor = (TextView) rowView
					.findViewById(R.id.executor);
			TextView tvResponsible = (TextView) rowView
					.findViewById(R.id.responsible);
			final CheckBox MyCheckBox = (CheckBox) rowView
					.findViewById(R.id.checkBox1);

			if (isGroupEditing) {
				MyCheckBox
						.setButtonDrawable(R.drawable.checkbox_selector_orange);
			} else {
				MyCheckBox.setButtonDrawable(R.drawable.checkbox_selector_flat);
			}

			String name = cursor.getString(cursor
					.getColumnIndex(taskOrganizerDatabaseHelper.KEY_NAME));
			String code = cursor.getString(cursor
					.getColumnIndex(taskOrganizerDatabaseHelper.KEY_CODE));
			final int id = cursor.getInt(cursor.getColumnIndex("_id"));

			if ((!code.equals("" + id)) && code.length() > 0) {
				name += " (" + code + ")";
			}
			tvname.setText(name);

			if (fontsize>0)
			{
				tvname.setTextSize(fontsize);
			}
			
			
			final Date date = new Date(cursor.getLong(cursor
					.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATE)));

			Date StartDate = new Date(0);
			if (!date.equals(StartDate)) {
				tvDate.setText(sdf_ddMM.format(date));
			} else {
				tvDate.setText(getString(R.string.from_code7));
			}

			final String status = cursor.getString(cursor
					.getColumnIndex("statusname"));
			tvStatus.setText(status);

			int status_id = cursor.getInt(cursor
					.getColumnIndex(taskOrganizerDatabaseHelper.KEY_STATUS));

			MyCheckBox.setOnCheckedChangeListener(null);

			if (isGroupEditing) {
				if (listofchoosenid.contains("" + id)) {
					MyCheckBox.setChecked(true);
				} else {
					MyCheckBox.setChecked(false);
				}
			} else {
				if (status_id == 2) {
					tvname.setPaintFlags(tvname.getPaintFlags()
							| Paint.STRIKE_THRU_TEXT_FLAG);
					if (!MyCheckBox.isChecked()) {
						MyCheckBox.setChecked(true);
					}
				} else {
					if (MyCheckBox.isChecked()) {
						MyCheckBox.setChecked(false);
					}
					tvname.setPaintFlags(tvname.getPaintFlags()
							& ~Paint.STRIKE_THRU_TEXT_FLAG);
				}
			}

			final String category = cursor.getString(cursor
					.getColumnIndex("categoryname"));
			tvCategory.setText(category);

			int bgcolor = c.getInt(c.getColumnIndex("categorybgcolor"));
			if (bgcolor != 0) {
				tvCategory.setBackgroundColor(bgcolor);
			}
			int textcolor = c.getInt(c.getColumnIndex("categorytextcolor"));
			if (textcolor != 0) {
				tvCategory.setTextColor(textcolor);
			}

			final Date duedate = new Date(cursor.getLong(cursor
					.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DUEDATE)));
			if (!duedate.equals(StartDate)) {
				tvduedate.setText(sdf_ddMM.format(duedate));
			} else {
				tvduedate.setText("");
			}

			String executor = cursor.getString(cursor
					.getColumnIndex(taskOrganizerDatabaseHelper.KEY_EXECUTOR));

			if (executor.startsWith("content:")) {
				// Uri contactData = Uri.parse(executor);
				String contactname;
				if (!ExecutorMap.containsKey(executor)) {
					// Cursor c_contact = managedQuery(contactData, null, null,
					// null, null);
					// c_contact.moveToFirst();
					// contactname =
					// c_contact.getString(c_contact.getColumnIndexOrThrow(People.NAME));
					contactname = CommonFunctions.GetContactNameFromString(
							taskorganizerActivity.this, executor);
					ExecutorMap.put(executor, contactname);
				} else {
					contactname = ExecutorMap.get(executor);
				}
				executor = contactname;
			}
			if (executor.length() > 0) {
				tvExecutor.setText(executor);
			} else {
				tvExecutor.setText("");
			}

			String responsible = cursor
					.getString(cursor
							.getColumnIndex(taskOrganizerDatabaseHelper.KEY_RESPONSIBLE));

			if (responsible.startsWith("content:")) {
				// Uri contactData = Uri.parse(responsible);
				String contactname;
				if (!ExecutorMap.containsKey(responsible)) {
					// Cursor c_contact = managedQuery(contactData, null, null,
					// null, null);
					// if (c_contact.moveToFirst())
					// {
					contactname = CommonFunctions.GetContactNameFromString(
							taskorganizerActivity.this, responsible);
					// contactname =
					// c_contact.getString(c_contact.getColumnIndexOrThrow(People.NAME));
					ExecutorMap.put(responsible, contactname);
					// }
					// else
					// {
					// contactname =responsible;
					// }
				} else {
					contactname = ExecutorMap.get(responsible);
				}
				responsible = contactname;
			}
			if (responsible.length() > 0) {
				tvResponsible.setText(responsible);
			} else {
				tvResponsible.setText("");
			}

			MyCheckBox.setTag(id);
			MyCheckBox.setOnCheckedChangeListener(myOnCheckedChangeListener);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			final LayoutInflater inflater = LayoutInflater.from(context);
			final View v = inflater.inflate(R.layout.row, null);

			return v;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Toast.makeText(getBaseContext(), "Destroy ",
		// Toast.LENGTH_LONG).show();
		tasksDB.close();
		c.close();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// Checks the orientation of the screen
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			// Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();

			if (CurrentPage == 2) {
				FillDay(1);
			} else if (CurrentPage == 1) {
				FillMonth(1);
				FillMonth(0);
				FillMonth(2);
			} else if (CurrentPage == 3) {
				FillWeek(1);
			}

		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			// Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
			if (CurrentPage == 2) {
				FillDay(1);
			} else if (CurrentPage == 1) {
				FillMonth(1);
				FillMonth(0);
				FillMonth(2);
			} else if (CurrentPage == 3) {
				FillWeek(1);
			}
		}
	}

	/*
	 * @Override public void onCreate(Bundle icicle) { super.onCreate(icicle);
	 * ReadPreferences(); }
	 */

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		ReadPreferences();

		setContentView(R.layout.main);

		tasksListView = (ListView) this.findViewById(R.id.tasksListView);
		// GridView myGridView = (GridView)this.findViewById(R.id.calendar);

		// Toast.makeText(getBaseContext(), "CurrentPage "+ CurrentPage,
		// Toast.LENGTH_LONG).show();

		final LayoutInflater inflater = LayoutInflater.from(this);
		final View rowView = inflater.inflate(R.layout.row, null);

		final EditText myEditText = (EditText) findViewById(R.id.taskname);
		final Button buttonaddnewtask = (Button) findViewById(R.id.buttonaddnewtask);

		final Button button_list = (Button) findViewById(R.id.button_list);
		final Button button_month = (Button) findViewById(R.id.button_month);
		final Button button_day = (Button) findViewById(R.id.button_day);
		final Button button_week = (Button) findViewById(R.id.button_week);
		final Button button_agenda = (Button) findViewById(R.id.button_agenda);

		button_list.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				PreviousPage = CurrentPage;
				CurrentPage = 0;
				ShowOrHidePages();
			}
		});

		button_month.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				PreviousPage = CurrentPage;
				CurrentPage = 1;
				ShowOrHidePages();
			}
		});

		button_day.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				PreviousPage = CurrentPage;
				CurrentPage = 2;
				ShowOrHidePages();
			}
		});

		button_week.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				PreviousPage = CurrentPage;
				CurrentPage = 3;// week
				ShowOrHidePages();
			}
		});

		button_agenda.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				PreviousPage = CurrentPage;
				CurrentPage = 4;// agenda
				ShowOrHidePages();
			}
		});

		buttonaddnewtask.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (myEditText.getText().toString().length() == 0) {
					return;
				}
				Task newTask = new Task(myEditText.getText().toString());

				if (filter.date_checked && filter.spinner_date == 0
						&& filter.spinner_date_from == 0) {
					// newTask.date = filter.date_from;
				}

				addNewTask(newTask);
				myEditText.setText("");
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(myEditText.getWindowToken(), 0);
			}
		});

		taskOrganizerDatabaseHelper dbHelper = new taskOrganizerDatabaseHelper(
				this, taskOrganizerDatabaseHelper.DATABASE_NAME, null,
				taskOrganizerDatabaseHelper.DATABASE_VERSION);
		tasksDB = dbHelper.getWritableDatabase();
		if (!tasksDB.isOpen())
		{
			Toast.makeText(this, this.getString(R.string.from_code89),Toast.LENGTH_SHORT).show();
		}

		// ImageView iv =
		// (ImageView)this.findViewById(R.id.imageview_end_listview);
		// iv.setLayoutParams(new LinearLayout.LayoutParams(1, 800));

		filter = new Filter();
		query_statuses();
		loadTasksFromProvider();

		ShowGroupEditingLayout(false);
		ShowOrHidePages();

		_calendar = Calendar.getInstance(Locale.getDefault());
		monthview_currentmonth = _calendar.get(Calendar.MONTH) + 1;
		monthview_currentyear = _calendar.get(Calendar.YEAR);
		current_date = new Date();
		// FillMonth(1);

		tasksListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent i = new Intent(taskorganizerActivity.this,
						taskForm.class);
				i.putExtra("_id", id);
				startActivityForResult(i, TASK_FORM_OPENED);
			}
		});

		tasksListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int pos, long id) {
				if (!itWasMowing) {
					// c.moveToPosition(pos);
					c.moveToPosition(ll2.list.get(pos).position);

					CurrentChoosenId = c.getLong(c.getColumnIndexOrThrow("_id"));
					CurrentChoosenName = c.getString(c
							.getColumnIndexOrThrow(taskOrganizerDatabaseHelper.KEY_NAME));
					Intent i = new Intent(taskorganizerActivity.this,
							flexiblemenu.class);
					i.putExtra("Regim", "EditDeleteChild");
					startActivityForResult(i, REQUEST_CONTEXT_MENU);
				}
				return true;
			}
		});

		myGestureDetector = new GestureDetector(new MyGestureDetector());
		// myGestureDetector.setIsLongpressEnabled(false);
		myGestureListener = new View.OnTouchListener() {
			// @Override
			public boolean onTouch(View v, MotionEvent event) {
				return myGestureDetector.onTouchEvent(event);
			}
		};

		OnClickListener myDateOnClickListener = new Button.OnClickListener() {
			public void onClick(View v) {
				// showDialog(dialog_filter_description);
				Intent i = new Intent(taskorganizerActivity.this,
						date_time_picker.class);

				Date edate = new Date(0);
				Date currentDate = new Date();

				i.putExtra("choose_time", 0);// 0 or 1
				i.putExtra("regim", CurrentPage);
				i.putExtra("button", "buttondate");
				i.putExtra("begin_of_the_day_or_end", 0);// 0 or 1
				i.putExtra("mseconds", filter.date_from.getTime());
				startActivityForResult(i, REQUEST_DATE_TIME);
			}
		};

		OnClickListener myDateOnClickListener2 = new Button.OnClickListener() {
			public void onClick(View v) {
				// showDialog(dialog_filter_description);
				Intent i = new Intent(taskorganizerActivity.this,
						date_time_picker.class);

				Date edate = new Date(0);
				Date currentDate = new Date();

				Resources res = getResources();
				Drawable button_calendar2 = res
						.getDrawable(R.drawable.button_calendar2);
				v.setBackgroundDrawable(button_calendar2);

				i.putExtra("choose_time", 0);// 0 or 1
				i.putExtra("regim", CurrentPage);
				i.putExtra("button", "buttondate");
				i.putExtra("y", ((Button) v).getHeight());
				i.putExtra("begin_of_the_day_or_end", 0);// 0 or 1
				i.putExtra("mseconds", filter.date_from.getTime());
				startActivityForResult(i, REQUEST_DATE_TIME);
			}
		};

		Button button_calendar = (Button) this
				.findViewById(R.id.button_calendar);
		button_calendar.setOnClickListener(myDateOnClickListener2);

		TextView filter_description = (TextView) findViewById(R.id.filter_description);
		filter_description.setOnClickListener(myDateOnClickListener);

		tasksListView.setOnTouchListener(myGestureListener);
		// myGridView.setOnTouchListener(myGestureListener);

		Button button_back = (Button) this.findViewById(R.id.button_back);
		button_back.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ChangeFilter(false);
			}
		});

		Button button_forward = (Button) this.findViewById(R.id.button_forward);
		button_forward.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ChangeFilter(true);
			}
		});

		TextView textview_back = (TextView) this
				.findViewById(R.id.textview_back);
		textview_back.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ChangeFilter(false);
			}
		});

		TextView textview_forward = (TextView) this
				.findViewById(R.id.textview_forward);
		textview_forward.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ChangeFilter(true);
			}
		});

		Button button_goto = (Button) findViewById(R.id.button_goto);
		button_goto.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				_calendar = Calendar.getInstance(Locale.getDefault());
				monthview_currentmonth = _calendar.get(Calendar.MONTH) + 1;
				monthview_currentyear = _calendar.get(Calendar.YEAR);
				current_date = new Date();

				if (CurrentPage == 4) {
					Date newDate = new Date();
					filter.date_checked = true;
					filter.spinner_date = 0;
					filter.date_from = newDate;
					filter.spinner_date_from = 0;
					current_date = newDate;
					loadTasksFromProvider();
				}
				ShowOrHidePages();
			}
		});

		Button button_help = (Button) this.findViewById(R.id.button_help);
		button_help.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(getBaseContext(), manual.class);
				startActivity(i);
			}
		});

		getTwoStatuses();
		updateFromPreferences();

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		SWIPE_MIN_DISTANCE = Math.max(Math.round(metrics.widthPixels / 3), 120);
		SWIPE_MIN_DISTANCE = Math.min(SWIPE_MIN_DISTANCE, 300);

		scale = getBaseContext().getResources().getDisplayMetrics().density;

		// STATUS_ACTIVE = getString(R.string.status1);
		// STATUS_COMPLETED = getString(R.string.status2);

		sdf_ddMM = new SimpleDateFormat(getString(R.string.sdf1));
		// mGestureDetector = new GestureDetector(this, new
		// LearnGestureListener());

		InitiatePagerAdapters();
		
		AppRater.app_launched(this);

	}

	public void ReadPreferences() {
		final SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		id_default_filter = settings.getLong("id_default_filter", -1);
		day_off_color = settings.getInt("day_off_color",
				Color.parseColor("#1C1C1C"));

		if (id_default_filter != -1) {
			SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

			String SelectQuery = "SELECT "
					+ taskOrganizerDatabaseHelper.Filters_Table + "."
					+ taskOrganizerDatabaseHelper.Filter_ID + " as _id,"
					+ taskOrganizerDatabaseHelper.Filters_Table + "."
					+ taskOrganizerDatabaseHelper.Filter_XML + " as _xml"
					+ " FROM " + taskOrganizerDatabaseHelper.Filters_Table;
			SelectQuery += " where "
					+ taskOrganizerDatabaseHelper.Filters_Table + "."
					+ taskOrganizerDatabaseHelper.Filter_ID + " = "
					+ id_default_filter;

			Cursor c_filter = tasksDB.rawQuery(SelectQuery, null);
			if (c_filter.moveToFirst()) {
				String xml = c_filter
						.getString(c_filter.getColumnIndex("_xml"));
				filter = new Filter(xml);
			}
		}

		send_sms_automatically = settings.getBoolean("send_sms_automatically",
				true);

		toodoledo_every_60_minutes = settings.getBoolean("toodoledo_every_60_minutes",
				true);
		
		
	
		emailimap = settings.getString("imap", "");
		emailuser = settings.getString("user", "");
		emailpass = settings.getString("pass", "");
		
		//emailperiod = settings.getInt("frequency", 0);
		emailperiod = 0;

		try
		{
		   emailperiod = Integer.parseInt(settings.getString("frequency", "0"));
		}
		 catch(NumberFormatException e) 
		 { 
			 
		 }
		
		try
		{
		   fontsize = Integer.parseInt(settings.getString("fontsize", "0"));
		}
		 catch(NumberFormatException e) 
		 { 
			 
		 }
		
		
		mHandlerEmail.removeCallbacks(mReceiveEmails);
		mHandler.removeCallbacks(mSyncToodledo);
		
		if (emailimap.length()>0 && emailuser.length()>0 && emailuser.length()>0 && emailperiod>0)
		{
			mHandlerEmail.postDelayed(mReceiveEmails, emailperiod*1000);
		}
		
		
		if (toodoledo_every_60_minutes)
		{
			toodoledo_email = settings.getString("toodoledo_email", "");
			toodoledo_pass = settings.getString("toodoledo_pass", "");
			//mHandler.postDelayed(mSyncToodledo, 3600*1000);
			mHandler.postDelayed(mSyncToodledo, 30*1000);
		}
		
		
		if (!settings.getBoolean("LicenseAccepted", false)) {
			Intent i = new Intent(taskorganizerActivity.this,
					licenseagreement.class);
			startActivityForResult(i, LICENSE);
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		/*
		 * // Reset longclick item if new touch is starting if
		 * (event.getAction()==MotionEvent.ACTION_DOWN) { longClickedItem = -1;
		 * }
		 */
		itWasMowing = false;

		if (myGestureDetector.onTouchEvent(event)) {
			// tasksListView.cancelLongPress();
			return true;
		} else {
			return false;
		}
		// return myGestureDetector.onTouchEvent(event);
	}

	public class MyGestureDetector extends SimpleOnGestureListener {

		// private static final int SWIPE_MIN_DISTANCE = 120;
		private static final int SWIPE_MAX_OFF_PATH = 250;
		private static final int SWIPE_THRESHOLD_VELOCITY = 50;

		@Override
		public void onLongPress(MotionEvent e) {
			// if (longClickedItem != -1) {
			itWasMowing = false;
			// Toast.makeText(getBaseContext(), "Long click!",
			// Toast.LENGTH_LONG).show();
			// }
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {

			if (CurrentPage == 0 || CurrentPage == 2 || CurrentPage == 3
					|| CurrentPage == 4) {
				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH
						&& (5 * Math.abs(e1.getY() - e2.getY()) > Math.abs(e1
								.getX() - e2.getX()))) {
					// Toast.makeText(getBaseContext(), "Fling"+(e1.getY() -
					// e2.getY()) , Toast.LENGTH_SHORT).show();
					itWasMowing = false;
					return false;
				}
				// right to left swipe
				if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					// Toast.makeText(getBaseContext(), "Fling left",
					// Toast.LENGTH_SHORT).show();
					// Log.d("ICS-Calendar", "Fling left");
					itWasMowing = true;
					ChangeFilter(true);
					return true;
				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					// Toast.makeText(getBaseContext(), "Fling right",
					// Toast.LENGTH_SHORT).show();
					itWasMowing = true;
					ChangeFilter(false);
					return true;
				}
			}

			if (CurrentPage == 1) {

				if (Math.abs(e1.getX() - e2.getX()) > Math.abs(e1.getY()
						- e2.getY())) {

					// right to left swipe
					if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
							&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
						// Toast.makeText(getBaseContext(), "Fling left",
						// Toast.LENGTH_SHORT).show();
						// Log.d("ICS-Calendar", "Fling left");
						itWasMowing = true;
						ChangeFilter(true);
						return true;
					} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
							&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
						// Toast.makeText(getBaseContext(), "Fling right",
						// Toast.LENGTH_SHORT).show();
						itWasMowing = true;
						ChangeFilter(false);
						return true;
					}
				} else {
					/*
					 * if (velocityY<0 && Math.abs(e1.getY() - e2.getY()) >
					 * SWIPE_MIN_DISTANCE && Math.abs(velocityY) >
					 * SWIPE_THRESHOLD_VELOCITY) {
					 * //Toast.makeText(getBaseContext(), "Fling up",
					 * Toast.LENGTH_SHORT).show(); //Log.d("ICS-Calendar",
					 * "Fling left"); itWasMowing = true; ChangeFilter(true);
					 * return true; } else if (velocityY>0 && Math.abs(e1.getY()
					 * - e2.getY()) > SWIPE_MIN_DISTANCE && Math.abs(velocityY)
					 * > SWIPE_THRESHOLD_VELOCITY) {
					 * //Toast.makeText(getBaseContext(), "Fling down",
					 * Toast.LENGTH_SHORT).show(); itWasMowing = true;
					 * ChangeFilter(false); return true; }
					 */
				}

			}

			return false;
		}

	}

	public void ChangeFilter(boolean toLeft) {

		if (CurrentPage == 1) {
			if (toLeft) {
				if (monthview_currentmonth == 12) {
					monthview_currentmonth = 1;
					monthview_currentyear++;
				} else {
					monthview_currentmonth++;
				}
			} else {
				if (monthview_currentmonth == 1) {
					monthview_currentmonth = 12;
					monthview_currentyear--;
				} else {
					monthview_currentmonth--;
				}
			}
			ChangeMonthForMonthView();
			FillMonth(1);
			FillMonth(0);
			FillMonth(2);
			return;
		}

		if (CurrentPage == 3) {
			if (toLeft) {
				current_date = new Date(current_date.getTime() + 7 * 24 * 3600
						* 1000);
			} else {
				current_date = new Date(current_date.getTime() - 7 * 24 * 3600
						* 1000);
			}
			ChangeWeekForWeekView();
			FillWeek(1);
			return;
		}

		if (!filter.date_checked) {
			Toast.makeText(getBaseContext(), getString(R.string.from_code8),
					Toast.LENGTH_SHORT).show();
			return;
		}

		if (filter.spinner_date != 0) {
			Toast.makeText(getBaseContext(), getString(R.string.from_code9),
					Toast.LENGTH_SHORT).show();
			return;
		}

		Date newDate;

		if (filter.spinner_date_from == 0) {
			newDate = filter.date_from;
		} else {
			newDate = getDateFromCondition(filter.spinner_date_from);
		}

		if (toLeft) {
			newDate = new Date(newDate.getTime() + 24 * 3600 * 1000);
		} else {
			newDate = new Date(newDate.getTime() - 24 * 3600 * 1000);
		}

		filter.date_from = newDate;
		filter.spinner_date_from = 0;
		loadTasksFromProvider();

		if (CurrentPage == 2) {
			current_date = newDate;
			FillDay(1);
		} else if (CurrentPage == 4) {
			FillAgenda();
		}

	}

	public void ChangeFilterToParticularDate(Date newDate) {

		if (CurrentPage == 1) {

			monthview_currentyear = newDate.getYear() + 1900;
			monthview_currentmonth = newDate.getMonth() + 1;

			ChangeMonthForMonthView();
			FillMonth(1);
			return;
		}

		if (CurrentPage == 3) {

			int numberoftheday = newDate.getDay();
			numberoftheday = (numberoftheday == 0) ? 7 : numberoftheday;
			long miliseconds = newDate.getTime() - (numberoftheday - 1) * 24
					* 3600 * 1000;
			current_date = new Date(miliseconds);

			ChangeWeekForWeekView();
			FillWeek(1);
			return;
		}

		filter.date_checked = true;
		filter.spinner_date = 0;
		filter.spinner_date_from = 0;

		filter.date_from = newDate;
		filter.spinner_date_from = 0;
		loadTasksFromProvider();

		if (CurrentPage == 2) {
			current_date = newDate;
			FillDay(1);
		} else if (CurrentPage == 4) {
			FillAgenda();
		}

	}

	public void ShowFilterDescription() {
		TextView filter_description = (TextView) findViewById(R.id.filter_description);
		TextView textview_back = (TextView) findViewById(R.id.textview_back);
		TextView textview_forward = (TextView) findViewById(R.id.textview_forward);
		Button button_back = (Button) findViewById(R.id.button_back);
		Button button_forward = (Button) findViewById(R.id.button_forward);

		SimpleDateFormat sdf_yyyyMMdd = new SimpleDateFormat(
				getString(R.string.sdf1));
		// SimpleDateFormat sdf_yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
		String[] date_filters;
		Resources res = getResources();
		date_filters = res.getStringArray(R.array.date_filters);

		String[] compare_types_dates = { "=", "<>", "<", ">", "<=", ">=",
				getString(R.string.from_code10),
				getString(R.string.from_code11) };

		String date_from = "";
		String date_to = "";
		int spinner_position = 0;

		if (filter.spinner_date_from == 0) {
			date_from = sdf_yyyyMMdd.format(filter.date_from);
		} else {
			date_from = date_filters[filter.spinner_date_from];
		}
		if (filter.spinner_date_to == 0) {
			date_to = sdf_yyyyMMdd.format(filter.date_to);
		} else {
			date_to = date_filters[filter.spinner_date_to];
		}
		spinner_position = filter.spinner_date;

		String textforbutton = "";

		if (spinner_position != 0) {
			textforbutton = getString(R.string.from_code15) + " "
					+ compare_types_dates[spinner_position] + " " + date_from;
			button_back.setVisibility(View.INVISIBLE);
			button_forward.setVisibility(View.INVISIBLE);
			textview_back.setVisibility(View.INVISIBLE);
			textview_forward.setVisibility(View.INVISIBLE);

		} else {
			textforbutton = date_from;
			SimpleDateFormat sdf_MMdd = new SimpleDateFormat("dd MMMM");

			textview_back.setText(sdf_MMdd.format(new Date(filter.date_from
					.getTime() - 24 * 3600 * 1000)));
			textview_forward.setText(sdf_MMdd.format(new Date(filter.date_from
					.getTime() + 24 * 3600 * 1000)));

			button_back.setVisibility(View.VISIBLE);
			button_forward.setVisibility(View.VISIBLE);
			textview_back.setVisibility(View.VISIBLE);
			textview_forward.setVisibility(View.VISIBLE);

		}
		if (spinner_position > 5) {
			textforbutton += " - " + date_to;
		}
		filter_description.setText(textforbutton);

	}

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			int _year = year - 1900;

			Date newDate = new Date(_year, monthOfYear, dayOfMonth);

			filter.date_checked = true;
			filter.spinner_date = 0;
			filter.date_from = newDate;
			filter.spinner_date_from = 0;
			loadTasksFromProvider();

		}
	};

	/*
	 * @Override protected void onPrepareDialog (int id, Dialog dialog){
	 * 
	 * 
	 * if (id == dialog_filter_description) { if ((filter.date_from.getTime() ==
	 * (new Date(0)).getTime()) || filter.spinner_date_from!=0){ Date newDate =
	 * new Date();
	 * 
	 * //return new DatePickerDialog(this, mDateSetListener,
	 * 1900+newDate.getYear(), newDate.getMonth(), newDate.getDate()); } else{
	 * //return new DatePickerDialog(this, mDateSetListener,
	 * 1900+filter.date_from.getYear(), filter.date_from.getMonth(),
	 * filter.date_from.getDate()); } } }
	 */

	@Override
	protected Dialog onCreateDialog(int id) {
		// Dialog dialog;

		if (id == DIALOG_DELETE_TASK) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(this.getString(R.string.question_delete_task));
			builder.setCancelable(false);
			builder.setPositiveButton(getString(R.string.from_code12),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							DeleteTask();
						}
					});

			builder.setNegativeButton(getString(R.string.from_code13),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			return builder.create();
		} else if (id == dialog_filter_description) {
			if ((filter.date_from.getTime() == (new Date(0)).getTime())
					|| filter.spinner_date_from != 0) {
				Date newDate = new Date();
				return new DatePickerDialog(this, mDateSetListener,
						1900 + newDate.getYear(), newDate.getMonth(),
						newDate.getDate());
			} else {
				return new DatePickerDialog(this, mDateSetListener,
						1900 + filter.date_from.getYear(),
						filter.date_from.getMonth(), filter.date_from.getDate());
			}
		} else {
			return null;
		}

		// dialog.show();
		// return dialog;
	}

	private void DeleteTask() {

		// Toast.makeText(getApplicationContext(), "Index = "+ CurrentChoosenId,
		// Toast.LENGTH_SHORT).show();
		// int count
		// =tasksDB.delete(taskOrganizerDatabaseHelper.Tasks_TABLE,taskOrganizerDatabaseHelper.KEY_ID+"="+_index,null);

		ContentValues _initialValues = new ContentValues();
		_initialValues.put(taskOrganizerDatabaseHelper.KEY_STATUS, 4);// archive

		String where = taskOrganizerDatabaseHelper.KEY_ID + "="
				+ CurrentChoosenId;
		long rowID = tasksDB.update(taskOrganizerDatabaseHelper.Tasks_TABLE,
				_initialValues, where, null);

		Date CurrentDate = new Date();
		SimpleDateFormat sdf_history = new SimpleDateFormat(
				getString(R.string.sdf2));
		String TextForHistory = "" + sdf_history.format(CurrentDate)
				+ " STATUS = ARCHIVE";
		ContentValues values = new ContentValues();
		values.put(taskOrganizerDatabaseHelper.History_task_id,
				CurrentChoosenId);
		values.put(taskOrganizerDatabaseHelper.History_NAME, TextForHistory);

		rowID = tasksDB.insert(taskOrganizerDatabaseHelper.History_Table,
				"item", values);

		c.requery();
		// ll.notifyDataSetChanged();
		ll2 = new TaskHierarchyLIstViewAdapter(getApplicationContext());
		tasksListView.setAdapter(ll2);
	}

	private void addNewTask(Task _task) {

		ContentValues values = _task.ReturnContentValues();
		long rowID = tasksDB.insert(taskOrganizerDatabaseHelper.Tasks_TABLE,
				"item", values);

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		String prefix = settings.getString("prefix", "");
		if (prefix.length() != 0) {
			values.clear();
			values.put(taskOrganizerDatabaseHelper.KEY_CODE, prefix + "_"
					+ rowID);
			String where = taskOrganizerDatabaseHelper.KEY_ID + "=" + rowID;
			rowID = tasksDB.update(taskOrganizerDatabaseHelper.Tasks_TABLE,
					values, where, null);
		}

		sendBroadcast(new Intent(WIDGET_REFRESHED));

		c.requery();
		// ll.notifyDataSetChanged();

		ll2 = new TaskHierarchyLIstViewAdapter(getApplicationContext());
		tasksListView.setAdapter(ll2);
	}

	public Cursor query(String[] projection, String selection,
			String[] selectionArgs, String sort) {

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
				+ taskOrganizerDatabaseHelper.KEY_PROCESSINGTIMEFACT + ","
				+ taskOrganizerDatabaseHelper.KEY_PRIORITY + ","
				+ taskOrganizerDatabaseHelper.KEY_DATEFROM + ","
				+ taskOrganizerDatabaseHelper.KEY_DATETO + ","
				+ taskOrganizerDatabaseHelper.KEY_DATEFROMFACT + ","
				+ taskOrganizerDatabaseHelper.KEY_DATETOFACT + ","
				+ taskOrganizerDatabaseHelper.KEY_PERCENTOFCOMPLETION + ","
				+ taskOrganizerDatabaseHelper.KEY_CATEGORY + ","
				+ taskOrganizerDatabaseHelper.KEY_STATUS + ","
				+ taskOrganizerDatabaseHelper.Statuses_Table + "."
				+ taskOrganizerDatabaseHelper.Status_NAME + " as statusname,"
				+ taskOrganizerDatabaseHelper.Categories_Table + "."
				+ taskOrganizerDatabaseHelper.Categories_BG_COLOR
				+ " as categorybgcolor,"
				+ taskOrganizerDatabaseHelper.Categories_Table + "."
				+ taskOrganizerDatabaseHelper.Categories_TEXT_COLOR
				+ " as categorytextcolor,"
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

		// SelectQuery
		// +=" where tasks."+taskOrganizerDatabaseHelper.KEY_CATEGORY+" <> 4";
		SelectQuery += ConstructWhereClause();
		// Toast.makeText(getBaseContext(), ConstructWhereClause(),
		// Toast.LENGTH_SHORT).show();

		String orderBy = GetOrder();
		/*
		 * if (TextUtils.isEmpty(sort)) { orderBy = "tasks."+
		 * taskOrganizerDatabaseHelper.KEY_ID; } else { orderBy = sort; }
		 */

		SelectQuery += " ORDER BY " + orderBy;

		if (!tasksDB.isOpen())
		{
			Toast.makeText(this,"Exception. Database was closed.",Toast.LENGTH_SHORT).show();
			taskOrganizerDatabaseHelper dbHelper = new taskOrganizerDatabaseHelper(
					this, taskOrganizerDatabaseHelper.DATABASE_NAME, null,
					taskOrganizerDatabaseHelper.DATABASE_VERSION);
			tasksDB = dbHelper.getWritableDatabase();
		}
		
		Cursor c = tasksDB.rawQuery(SelectQuery, selectionArgs);
		return c;
	}

	public String GetOrder() {
		String orderBy = "";

		if (filter.spinner_sort == 0) {
			orderBy = "tasks." + taskOrganizerDatabaseHelper.KEY_ID;
		} else if (filter.spinner_sort == 1) {
			orderBy = "tasks." + taskOrganizerDatabaseHelper.KEY_ID + " DESC";
		} else if (filter.spinner_sort == 2) {
			orderBy = "tasks." + taskOrganizerDatabaseHelper.KEY_CODE;
		} else if (filter.spinner_sort == 3) {
			orderBy = "tasks." + taskOrganizerDatabaseHelper.KEY_CODE + " DESC";
		} else if (filter.spinner_sort == 4) {
			orderBy = "tasks." + taskOrganizerDatabaseHelper.KEY_NAME;
		} else if (filter.spinner_sort == 5) {
			orderBy = "tasks." + taskOrganizerDatabaseHelper.KEY_NAME + " DESC";
		} else if (filter.spinner_sort == 6) {
			orderBy = "tasks." + taskOrganizerDatabaseHelper.KEY_DATE;
		} else if (filter.spinner_sort == 7) {
			orderBy = "tasks." + taskOrganizerDatabaseHelper.KEY_DATE + " DESC";
		} else if (filter.spinner_sort == 8) {
			orderBy = "tasks." + taskOrganizerDatabaseHelper.KEY_RELEASEDATE;
		} else if (filter.spinner_sort == 9) {
			orderBy = "tasks." + taskOrganizerDatabaseHelper.KEY_RELEASEDATE
					+ " DESC";
		} else if (filter.spinner_sort == 10) {
			orderBy = "tasks." + taskOrganizerDatabaseHelper.KEY_DUEDATE;
		} else if (filter.spinner_sort == 11) {
			orderBy = "tasks." + taskOrganizerDatabaseHelper.KEY_DUEDATE
					+ " DESC";
		} else if (filter.spinner_sort == 12) {
			orderBy = "tasks." + taskOrganizerDatabaseHelper.KEY_STATUS;
		} else if (filter.spinner_sort == 13) {
			orderBy = "tasks." + taskOrganizerDatabaseHelper.KEY_CATEGORY;
		}

		return orderBy;
	}

	public String ConstructWhereClause() {
		String Where = " WHERE ";

		/*
		 * String[] compare_types_strings = {"=", "<>", "contains",
		 * "not contains"}; String[] compare_types_dates = {"=", "<>", "<",
		 * ">","<=",">=","in interval","out of interval"}; String[]
		 * compare_types_list = {"in list", "out of the list"}; String[]
		 * compare_types_executor = {"=", "<>"};
		 */
		int number_of_conditions = 0;

		if (filter.code_checked) {
			if (filter.spinner_code == 0) {
				Where += ((number_of_conditions == 0) ? "" : " AND ")
						+ "tasks." + taskOrganizerDatabaseHelper.KEY_CODE
						+ " = '" + filter.code + "'";
			}
			if (filter.spinner_code == 1) {
				Where += ((number_of_conditions == 0) ? "" : " AND ")
						+ "tasks." + taskOrganizerDatabaseHelper.KEY_CODE
						+ " <> '" + filter.code + "'";
			}
			if (filter.spinner_code == 2) {
				Where += ((number_of_conditions == 0) ? "" : " AND ")
						+ "tasks." + taskOrganizerDatabaseHelper.KEY_CODE
						+ " LIKE '%" + filter.code + "%'";
			}
			if (filter.spinner_code == 3) {
				Where += ((number_of_conditions == 0) ? "" : " AND ") + "(NOT "
						+ "tasks." + taskOrganizerDatabaseHelper.KEY_CODE
						+ " LIKE '%" + filter.code + "%')";
			}
			number_of_conditions++;
		}

		if (filter.name_checked) {
			if (filter.spinner_name == 0) {
				Where += ((number_of_conditions == 0) ? "" : " AND ")
						+ "tasks." + taskOrganizerDatabaseHelper.KEY_NAME
						+ " = '" + filter.name + "'";
			}
			if (filter.spinner_name == 1) {
				Where += ((number_of_conditions == 0) ? "" : " AND ")
						+ "tasks." + taskOrganizerDatabaseHelper.KEY_NAME
						+ " <> '" + filter.name + "'";
			}
			if (filter.spinner_name == 2) {
				Where += ((number_of_conditions == 0) ? "" : " AND ")
						+ "tasks." + taskOrganizerDatabaseHelper.KEY_NAME
						+ " LIKE '%" + filter.name + "%'";
			}
			if (filter.spinner_name == 3) {
				Where += ((number_of_conditions == 0) ? "" : " AND ") + "(NOT "
						+ "tasks." + taskOrganizerDatabaseHelper.KEY_NAME
						+ " LIKE '%" + filter.name + "%')";
			}
			number_of_conditions++;
		}

		if (filter.description_checked) {
			if (filter.spinner_description == 0) {
				Where += ((number_of_conditions == 0) ? "" : " AND ")
						+ "tasks."
						+ taskOrganizerDatabaseHelper.KEY_DESCRIPTION + " = '"
						+ filter.description + "'";
			}
			if (filter.spinner_description == 1) {
				Where += ((number_of_conditions == 0) ? "" : " AND ")
						+ "tasks."
						+ taskOrganizerDatabaseHelper.KEY_DESCRIPTION + " <> '"
						+ filter.description + "'";
			}
			if (filter.spinner_description == 2) {
				Where += ((number_of_conditions == 0) ? "" : " AND ")
						+ "tasks."
						+ taskOrganizerDatabaseHelper.KEY_DESCRIPTION
						+ " LIKE '%" + filter.description + "%'";
			}
			if (filter.spinner_description == 3) {
				Where += ((number_of_conditions == 0) ? "" : " AND ") + "(NOT "
						+ "tasks."
						+ taskOrganizerDatabaseHelper.KEY_DESCRIPTION
						+ " LIKE '%" + filter.description + "%')";
			}
			number_of_conditions++;
		}

		if (filter.executor_checked) {
			if (filter.spinner_executor == 0) {
				Where += ((number_of_conditions == 0) ? "" : " AND ")
						+ "tasks." + taskOrganizerDatabaseHelper.KEY_EXECUTOR
						+ " = '" + filter.executor + "'";
			}
			if (filter.spinner_executor == 1) {
				Where += ((number_of_conditions == 0) ? "" : " AND ")
						+ "tasks." + taskOrganizerDatabaseHelper.KEY_EXECUTOR
						+ " <> '" + filter.executor + "'";
			}
			number_of_conditions++;
		}

		if (filter.responsible_checked) {
			if (filter.spinner_responsible == 0) {
				Where += ((number_of_conditions == 0) ? "" : " AND ")
						+ "tasks."
						+ taskOrganizerDatabaseHelper.KEY_RESPONSIBLE + " = '"
						+ filter.responsible + "'";
			}
			if (filter.spinner_responsible == 1) {
				Where += ((number_of_conditions == 0) ? "" : " AND ")
						+ "tasks."
						+ taskOrganizerDatabaseHelper.KEY_RESPONSIBLE + " <> '"
						+ filter.responsible + "'";
			}
			number_of_conditions++;
		}

		if (filter.status_checked) {
			String Status = "(" + filter.status.replaceAll(";", ",") + "0)";

			if (filter.spinner_status == 0) {
				Where += ((number_of_conditions == 0) ? "" : " AND ")
						+ "tasks." + taskOrganizerDatabaseHelper.KEY_STATUS
						+ " IN " + Status;
			}
			if (filter.spinner_status == 1) {
				Where += ((number_of_conditions == 0) ? "" : " AND ")
						+ "(NOT tasks."
						+ taskOrganizerDatabaseHelper.KEY_STATUS + " IN "
						+ Status + ")";
			}
			number_of_conditions++;
		}

		if (filter.category_checked) {
			String Category = "(" + filter.category.replaceAll(";", ",") + "0)";

			if (filter.spinner_category == 0) {
				Where += ((number_of_conditions == 0) ? "" : " AND ")
						+ "tasks." + taskOrganizerDatabaseHelper.KEY_CATEGORY
						+ " IN " + Category;
			}
			if (filter.spinner_category == 1) {
				Where += ((number_of_conditions == 0) ? "" : " AND ")
						+ "(NOT tasks."
						+ taskOrganizerDatabaseHelper.KEY_CATEGORY + " IN "
						+ Category + ")";
			}
			number_of_conditions++;
		}

		if (filter.date_checked) {
			Date date_from;
			Date date_to;

			if (filter.spinner_date_from == 0) {
				date_from = filter.date_from;
			} else {
				date_from = getDateFromCondition(filter.spinner_date_from);
			}

			if (filter.spinner_date_to == 0) {
				date_to = filter.date_to;
			} else {
				date_to = getDateFromCondition(filter.spinner_date_to);
			}

			date_from = RoundDate(date_from, 0);
			if (filter.spinner_date < 6) {
				date_to = RoundDate(date_from, 1);
			} else {
				date_to = RoundDate(date_to, 1);
			}

			if (filter.show_empty_date_checked) {
				Date date0 = new Date(0);
				if (filter.spinner_date == 0 || filter.spinner_date == 6) {
					Where += ((number_of_conditions == 0) ? "" : " AND ")
							+ "(tasks." + taskOrganizerDatabaseHelper.KEY_DATE
							+ " = " + date0.getTime() + " OR (tasks."
							+ taskOrganizerDatabaseHelper.KEY_DATE + " >= "
							+ date_from.getTime() + " AND tasks."
							+ taskOrganizerDatabaseHelper.KEY_DATE + " <= "
							+ date_to.getTime() + "))";
				}
				if (filter.spinner_date == 1 || filter.spinner_date == 7) {
					Where += ((number_of_conditions == 0) ? "" : " AND ")
							+ "(tasks." + taskOrganizerDatabaseHelper.KEY_DATE
							+ " < " + date_from.getTime() + " OR tasks."
							+ taskOrganizerDatabaseHelper.KEY_DATE + " > "
							+ date_to.getTime() + "))";
				}
				if (filter.spinner_date == 2) {
					Where += ((number_of_conditions == 0) ? "" : " AND ")
							+ "(tasks." + taskOrganizerDatabaseHelper.KEY_DATE
							+ " = " + date0.getTime() + " OR (tasks."
							+ taskOrganizerDatabaseHelper.KEY_DATE + " < "
							+ date_from.getTime() + "))";
				}
				if (filter.spinner_date == 3) {
					Where += ((number_of_conditions == 0) ? "" : " AND ")
							+ "(tasks." + taskOrganizerDatabaseHelper.KEY_DATE
							+ " = " + date0.getTime() + " OR (tasks."
							+ taskOrganizerDatabaseHelper.KEY_DATE + " > "
							+ date_to.getTime() + "))";
				}
				if (filter.spinner_date == 4) {
					Where += ((number_of_conditions == 0) ? "" : " AND ")
							+ "(tasks." + taskOrganizerDatabaseHelper.KEY_DATE
							+ " = " + date0.getTime() + " OR (tasks."
							+ taskOrganizerDatabaseHelper.KEY_DATE + " <= "
							+ date_to.getTime() + "))";
				}
				if (filter.spinner_date == 5) {
					Where += ((number_of_conditions == 0) ? "" : " AND ")
							+ "(tasks." + taskOrganizerDatabaseHelper.KEY_DATE
							+ " = " + date0.getTime() + " OR (tasks."
							+ taskOrganizerDatabaseHelper.KEY_DATE + " >= "
							+ date_from.getTime() + "))";
				}
			} else {
				if (filter.spinner_date == 0 || filter.spinner_date == 6) {
					Where += ((number_of_conditions == 0) ? "" : " AND ")
							+ "(tasks." + taskOrganizerDatabaseHelper.KEY_DATE
							+ " >= " + date_from.getTime() + " AND tasks."
							+ taskOrganizerDatabaseHelper.KEY_DATE + " <= "
							+ date_to.getTime() + ")";
				}
				if (filter.spinner_date == 1 || filter.spinner_date == 7) {
					Where += ((number_of_conditions == 0) ? "" : " AND ")
							+ "(tasks." + taskOrganizerDatabaseHelper.KEY_DATE
							+ " < " + date_from.getTime() + " OR tasks."
							+ taskOrganizerDatabaseHelper.KEY_DATE + " > "
							+ date_to.getTime() + ")";
				}
				if (filter.spinner_date == 2) {
					Where += ((number_of_conditions == 0) ? "" : " AND ")
							+ "(tasks." + taskOrganizerDatabaseHelper.KEY_DATE
							+ " < " + date_from.getTime() + ")";
				}
				if (filter.spinner_date == 3) {
					Where += ((number_of_conditions == 0) ? "" : " AND ")
							+ "(tasks." + taskOrganizerDatabaseHelper.KEY_DATE
							+ " > " + date_to.getTime() + ")";
				}
				if (filter.spinner_date == 4) {
					Where += ((number_of_conditions == 0) ? "" : " AND ")
							+ "(tasks." + taskOrganizerDatabaseHelper.KEY_DATE
							+ " <= " + date_to.getTime() + ")";
				}
				if (filter.spinner_date == 5) {
					Where += ((number_of_conditions == 0) ? "" : " AND ")
							+ "(tasks." + taskOrganizerDatabaseHelper.KEY_DATE
							+ " >= " + date_from.getTime() + ")";
				}
			}
			number_of_conditions++;
		}

		if (filter.duedate_checked) {
			Date date_from;
			Date date_to;

			if (filter.spinner_duedate_from == 0) {
				date_from = filter.duedate_from;
			} else {
				date_from = getDateFromCondition(filter.spinner_duedate_from);
			}

			if (filter.spinner_duedate_to == 0) {
				date_to = filter.duedate_to;
			} else {
				date_to = getDateFromCondition(filter.spinner_duedate_to);
			}

			date_from = RoundDate(date_from, 0);
			if (filter.spinner_duedate < 6) {
				date_to = RoundDate(date_from, 1);
			} else {
				date_to = RoundDate(date_to, 1);
			}

			if (filter.spinner_duedate == 0 || filter.spinner_date == 6) {
				Where += ((number_of_conditions == 0) ? "" : " AND ")
						+ "(tasks." + taskOrganizerDatabaseHelper.KEY_DUEDATE
						+ " >= " + date_from.getTime() + " AND tasks."
						+ taskOrganizerDatabaseHelper.KEY_DATE + " <= "
						+ date_to.getTime() + ")";
			}
			if (filter.spinner_duedate == 1 || filter.spinner_date == 7) {
				Where += ((number_of_conditions == 0) ? "" : " AND ")
						+ "(tasks." + taskOrganizerDatabaseHelper.KEY_DUEDATE
						+ " < " + date_from.getTime() + " OR tasks."
						+ taskOrganizerDatabaseHelper.KEY_DATE + " > "
						+ date_to.getTime() + ")";
			}
			if (filter.spinner_duedate == 2) {
				Where += ((number_of_conditions == 0) ? "" : " AND ")
						+ "(tasks." + taskOrganizerDatabaseHelper.KEY_DUEDATE
						+ " < " + date_from.getTime() + ")";
			}
			if (filter.spinner_duedate == 3) {
				Where += ((number_of_conditions == 0) ? "" : " AND ")
						+ "(tasks." + taskOrganizerDatabaseHelper.KEY_DUEDATE
						+ " > " + date_to.getTime() + ")";
			}
			if (filter.spinner_duedate == 4) {
				Where += ((number_of_conditions == 0) ? "" : " AND ")
						+ "(tasks." + taskOrganizerDatabaseHelper.KEY_DUEDATE
						+ " <= " + date_to.getTime() + ")";
			}
			if (filter.spinner_duedate == 5) {
				Where += ((number_of_conditions == 0) ? "" : " AND ")
						+ "(tasks." + taskOrganizerDatabaseHelper.KEY_DUEDATE
						+ " >= " + date_from.getTime() + ")";
			}
			number_of_conditions++;
		}

		if (filter.releasedate_checked) {
			Date date_from;
			Date date_to;

			if (filter.spinner_releasedate_from == 0) {
				date_from = filter.releasedate_from;
			} else {
				date_from = getDateFromCondition(filter.spinner_releasedate_from);
			}

			if (filter.spinner_releasedate_to == 0) {
				date_to = filter.releasedate_to;
			} else {
				date_to = getDateFromCondition(filter.spinner_releasedate_to);
			}

			date_from = RoundDate(date_from, 0);
			if (filter.spinner_releasedate < 6) {
				date_to = RoundDate(date_from, 1);
			} else {
				date_to = RoundDate(date_to, 1);
			}

			if (filter.spinner_releasedate == 0
					|| filter.spinner_releasedate == 6) {
				Where += ((number_of_conditions == 0) ? "" : " AND ")
						+ "(tasks."
						+ taskOrganizerDatabaseHelper.KEY_RELEASEDATE + " >= "
						+ date_from.getTime() + " AND tasks."
						+ taskOrganizerDatabaseHelper.KEY_DATE + " <= "
						+ date_to.getTime() + ")";
			}
			if (filter.spinner_releasedate == 1
					|| filter.spinner_releasedate == 7) {
				Where += ((number_of_conditions == 0) ? "" : " AND ")
						+ "(tasks."
						+ taskOrganizerDatabaseHelper.KEY_RELEASEDATE + " < "
						+ date_from.getTime() + " OR tasks."
						+ taskOrganizerDatabaseHelper.KEY_DATE + " > "
						+ date_to.getTime() + ")";
			}
			if (filter.spinner_releasedate == 2) {
				Where += ((number_of_conditions == 0) ? "" : " AND ")
						+ "(tasks."
						+ taskOrganizerDatabaseHelper.KEY_RELEASEDATE + " < "
						+ date_from.getTime() + ")";
			}
			if (filter.spinner_releasedate == 3) {
				Where += ((number_of_conditions == 0) ? "" : " AND ")
						+ "(tasks."
						+ taskOrganizerDatabaseHelper.KEY_RELEASEDATE + " > "
						+ date_to.getTime() + ")";
			}
			if (filter.spinner_releasedate == 4) {
				Where += ((number_of_conditions == 0) ? "" : " AND ")
						+ "(tasks."
						+ taskOrganizerDatabaseHelper.KEY_RELEASEDATE + " <= "
						+ date_to.getTime() + ")";
			}
			if (filter.spinner_releasedate == 5) {
				Where += ((number_of_conditions == 0) ? "" : " AND ")
						+ "(tasks."
						+ taskOrganizerDatabaseHelper.KEY_RELEASEDATE + " >= "
						+ date_from.getTime() + ")";
			}
			number_of_conditions++;
		}

		if (number_of_conditions > 0) {
			return Where;
		} else {
			return "";
		}
	}

	public Date RoundDate(Date date0, int UpOrDown) {
		if (UpOrDown == 1) {
			return new Date(date0.getYear(), date0.getMonth(), date0.getDate(),
					23, 59, 59);
		} else {
			return new Date(date0.getYear(), date0.getMonth(), date0.getDate(),
					0, 0, 0);
		}

	}

	public Date getDateFromCondition(int position) {
		Date date0 = new Date();
		// Calendar cal = Calendar.getInstance();
		// cal.setTime(date0);
		// int currentDOW = cal.get(Calendar.DAY_OF_WEEK);
		// cal.add(Calendar.DAY_OF_YEAR, (currentDOW * -1)+1);

		if (position == 2)// begin of the week
		{
			int numberoftheday = date0.getDay();
			numberoftheday = (numberoftheday == 0) ? 7 : numberoftheday;
			long miliseconds = date0.getTime() - (numberoftheday - 1) * 24
					* 3600 * 1000;
			date0 = new Date(miliseconds);
		} else if (position == 3)// end of the week
		{
			int numberoftheday = date0.getDay();
			numberoftheday = (numberoftheday == 0) ? 7 : numberoftheday;
			long miliseconds = date0.getTime() + (7 - numberoftheday) * 24
					* 3600 * 1000;
			date0 = new Date(miliseconds);
		} else if (position == 4)// beginning of the month
		{
			date0 = new Date(date0.getYear(), date0.getMonth(), 1, 0, 0, 0);

		} else if (position == 5)// end of the month
		{
			date0 = new Date(date0.getYear(), date0.getMonth(), 1, 0, 0, 0);

			if (date0.getMonth() == 12) {
				date0 = new Date(date0.getYear(), 12, 31, 0, 0, 0);
			} else {
				Date date1 = new Date(date0.getYear(), date0.getMonth() + 1, 1,
						0, 0, 0);
				date1 = new Date(date1.getTime() - 1);
				date0 = new Date(date0.getYear(), date0.getMonth(),
						date1.getDate(), 23, 59, 59);
			}
		} else if (position == 6)// beginning of the year
		{
			date0 = new Date(date0.getYear(), 1, 1, 0, 0, 0);
		} else if (position == 7)// beginning of the year
		{
			date0 = new Date(date0.getYear(), 12, 31, 23, 59, 59);
		}

		return date0;
	}

	public void query_statuses() {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String SelectQuery = "SELECT " + taskOrganizerDatabaseHelper.Status_ID
				+ " as _id," + taskOrganizerDatabaseHelper.Status_NAME
				+ " as name" + " FROM "
				+ taskOrganizerDatabaseHelper.Statuses_Table;

		c_statuses = tasksDB.rawQuery(SelectQuery, null);

		if (c_statuses.moveToFirst()) {
			STATUS_ACTIVE = c_statuses.getString(c_statuses
					.getColumnIndex(taskOrganizerDatabaseHelper.Status_NAME));
			if (c_statuses.moveToNext()) {
				STATUS_COMPLETED = c_statuses
						.getString(c_statuses
								.getColumnIndex(taskOrganizerDatabaseHelper.Status_NAME));
			}
		}

	}

	private void loadTasksFromProvider() {

		// ContentResolver cr = getContentResolver();
		// Return all the saved tasks
		c = query(null, null, null, null);

		// ll = new MySimpleCursorAdapter(this, R.layout.row, c, new
		// String[]{taskOrganizerDatabaseHelper.KEY_NAME,
		// taskOrganizerDatabaseHelper.KEY_DATE}, new
		// int[]{R.id.nameoftask,R.id.date});

		ListView simple_tasksListView = (ListView) this
				.findViewById(R.id.simple_tasksListView);
		ll2 = new TaskHierarchyLIstViewAdapter(getApplicationContext());

		// tasksListView.setAdapter(ll);

		tasksListView.setAdapter(ll2);

		// startManagingCursor(c);
		stopManagingCursor(c);
		ShowFilterDescription();
		/*
		 * if (c.moveToFirst()) { do {
		 * 
		 * Task q = new Task(c);
		 * 
		 * addTaskToArray(q); } while(c.moveToNext()); }
		 */
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuItem myMenuItem;

		myMenuItem = menu.add(0, MENU_ADD, Menu.NONE, R.string.menu_add);
		myMenuItem.setIcon(R.drawable.ic_add_task);
		myMenuItem = menu.add(0, MENU_FILTER, Menu.NONE, R.string.menu_filter);
		myMenuItem.setIcon(R.drawable.ic_filter);
		myMenuItem = menu.add(0, MENU_FILTER_SAVED, Menu.NONE,
				R.string.menu_filter_saved);
		myMenuItem.setIcon(R.drawable.ic_saved_filters);

		myMenuItem = menu.add(0, MENU_PREFERENCES, Menu.NONE,
				R.string.menu_preferences);
		myMenuItem.setIcon(R.drawable.ic_preferences);

		SubMenu sub = menu.addSubMenu(0, MENU_CATALOGS, Menu.NONE,
				R.string.menu_catalogs);
		sub.setHeaderIcon(R.drawable.ic_catalogs);
		sub.setIcon(R.drawable.ic_catalogs);

		myMenuItem = sub.add(0, MENU_CATEGORIES, Menu.NONE,
				R.string.menu_categories);
		myMenuItem = sub.add(0, MENU_STATUSES, Menu.NONE,
				R.string.menu_statuses);
		// myMenuItem.setIcon(android.R.drawable.ic);

		myMenuItem = menu
				.add(0, MENU_ADDITIONAL, Menu.NONE, R.string.menu_more);
		myMenuItem.setIcon(R.drawable.ic_more);

		/*
		 * myMenuItem = menu.add(0, MENU_FROM_CALENDAR, Menu.NONE,
		 * R.string.menu_from_calendar);
		 * myMenuItem.setIcon(R.drawable.ic_menu_my_calendar);
		 * 
		 * myMenuItem = menu.add(0, MENU_TO_CALENDAR, Menu.NONE,
		 * R.string.menu_to_calendar);
		 * myMenuItem.setIcon(R.drawable.ic_menu_my_calendar);
		 * 
		 * myMenuItem = menu.add(0, MENU_EXPORT, Menu.NONE,
		 * R.string.menu_export); myMenuItem = menu.add(0, MENU_IMPORT,
		 * Menu.NONE, R.string.menu_import);
		 * 
		 * 
		 * myMenuItem = menu.add(0, MENU_GROUP_EDITING, Menu.NONE,
		 * R.string.menu_group_editing); myMenuItem = menu.add(0,
		 * MENU_RECEIVE_EMAILS, Menu.NONE, R.string.menu_receive_emails);
		 */

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case (MENU_ADD): {
			Intent i = new Intent(taskorganizerActivity.this, taskForm.class);
			i.putExtra("_id", 0);

			if (filter.date_checked && filter.spinner_date == 0
					&& filter.spinner_date_from == 0) {
				i.putExtra("date", filter.date_from.getTime());
			}

			startActivityForResult(i, TASK_FORM_OPENED);

			return true;
		}

		case (MENU_PREFERENCES): {
			Intent i = new Intent(this, Preferences.class);
			startActivityForResult(i, PREFERENCES);
			return true;
		}
		case (MENU_FILTER): {
			Intent i = new Intent(this, filter_form.class);
			filter.ToXML();
			i.putExtra("filter", filter.xmlstring);
			i.putExtra("id", -1);
			i.putExtra("name_of_filter", "");
			startActivityForResult(i, FILTER_FORM_OPENED);
			return true;
		}
		case (MENU_FILTER_SAVED): {
			Intent i = new Intent(this, ListOfCatalog.class);
			i.putExtra("Table", 3);
			startActivityForResult(i, FILTER_CHOOSE_SAVED);
			return true;
		}
		case (MENU_CATEGORIES): {
			Intent i = new Intent(this, ListOfCatalog.class);
			i.putExtra("Table", 1);
			startActivity(i);
			return true;
		}
		case (MENU_STATUSES): {
			Intent i = new Intent(this, ListOfCatalog.class);
			i.putExtra("Table", 2);
			startActivity(i);
			return true;
		}
		case (MENU_ADDITIONAL): {
			Intent i = new Intent(this, additionalfunctions.class);
			startActivityForResult(i, CHOOSE_ADDITIONAL);
			return true;
		}

		/*
		 * case (MENU_FROM_CALENDAR): { CommonFunctions.ReadCalendars(this);
		 * return true; } case (MENU_TO_CALENDAR): {
		 * CommonFunctions.WriteTasksToCalendar(this); return true; } case
		 * (MENU_EXPORT): { CommonFunctions.ExportToXML(this); return true; }
		 * case (MENU_IMPORT): { CommonFunctions.ImportFromXML(this); return
		 * true; } case (MENU_RECEIVE_EMAILS): { SharedPreferences settings =
		 * getSharedPreferences(PREFS_NAME, 0);
		 * 
		 * MyMail mymail = new MyMail();
		 * 
		 * mymail._host = settings.getString("imap", "imap.gmail.com");
		 * mymail._user =settings.getString("user", ""); mymail._pass =
		 * settings.getString("pass", ""); String lastemailid =
		 * settings.getString("lastemailid", "0"); try { mymail.lastnumber =
		 * Integer.valueOf(lastemailid); } finally {
		 * 
		 * } mymail.act = getBaseContext(); mymail.istest = false;
		 * 
		 * try { boolean success = mymail.receive();
		 * 
		 * if (success){ Toast.makeText(getBaseContext(),
		 * "New emails are checked", Toast.LENGTH_SHORT).show();
		 * 
		 * if (mymail.newlastnumber> mymail.lastnumber) {
		 * SharedPreferences.Editor editor = settings.edit();
		 * editor.putString("lastemailid", ""+mymail.newlastnumber);
		 * editor.commit(); }
		 * 
		 * } else { Toast.makeText(getBaseContext(), "Connection failed",
		 * Toast.LENGTH_SHORT).show(); }
		 * 
		 * } catch (Exception e) { // TODO Auto-generated catch block
		 * Toast.makeText(getBaseContext(), "Connection failed",
		 * Toast.LENGTH_SHORT).show(); e.printStackTrace(); } return true; }
		 * case (MENU_GROUP_EDITING): { isGroupEditing = true;
		 * listofchoosenid.clear(); ShowGroupEditingLayout(true); return true; }
		 */

		}
		return false;
	}

	public void ShowGroupEditingLayout(boolean notify) {
		LinearLayout linearLayout_group = (LinearLayout) findViewById(R.id.linearLayout_group);
		if (isGroupEditing) {
			linearLayout_group.setVisibility(View.VISIBLE);

			LinearLayout layoutedit = (LinearLayout) this
					.findViewById(R.id.layoutedit);
			LinearLayout layoutcancel = (LinearLayout) this
					.findViewById(R.id.layoutcancel);
			layoutcancel.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					isGroupEditing = false;
					ShowGroupEditingLayout(true);
				}
			});

			layoutedit.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {

					Iterator it = listofchoosenid.iterator();

					String choosenid = "";

					while (it.hasNext()) {
						choosenid += ((choosenid.length() == 0) ? "" : ",")
								+ (String) it.next();
					}

					if (choosenid.length() == 0) {
						Toast.makeText(getBaseContext(),
								getString(R.string.from_code14),
								Toast.LENGTH_SHORT).show();
						return;
					}

					Intent i = new Intent(taskorganizerActivity.this,
							groupediting.class);
					i.putExtra("choosenid", choosenid);
					startActivityForResult(i, GROUP_EDITING);

					isGroupEditing = false;
					listofchoosenid.clear();
					// ShowGroupEditingLayout(true);
				}
			});

			// ll.notifyDataSetChanged();//to change the button of checkbox
		} else {
			linearLayout_group.setVisibility(View.GONE);
		}
		if (notify) {
			// ll.notifyDataSetChanged();//to change the button of checkbox
			ll2 = new TaskHierarchyLIstViewAdapter(getApplicationContext());
			tasksListView.setAdapter(ll2);
		}
	}

	private void updateFromPreferences() {
		Context context = getApplicationContext();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		/*
		 * minimumMagnitude =
		 * Integer.parseInt(prefs.getString(Preferences.PREF_MIN_MAG, "0"));
		 * updateFreq =
		 * Integer.parseInt(prefs.getString(Preferences.PREF_UPDATE_FREQ, "0"));
		 * autoUpdate = prefs.getBoolean(Preferences.PREF_AUTO_UPDATE, false);
		 */
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == SHOW_LIST_OF_CATALOGS)
			if (resultCode == Activity.RESULT_OK)
				if (data.getExtras().getString("Result") == "Statuses") {

				} else {
				}

		if (requestCode == LICENSE && resultCode != Activity.RESULT_OK) {
			finish();
		}

		if (requestCode == PREFERENCES && resultCode == Activity.RESULT_OK) {
			ReadPreferences();
		}

		if (requestCode == REQUEST_DATE_TIME) {
			Resources res = getResources();
			Drawable button_calendar2 = res
					.getDrawable(R.drawable.button_calendar_selector);
			Button button_calendar = (Button) this
					.findViewById(R.id.button_calendar);
			button_calendar.setBackgroundDrawable(button_calendar2);
		}

		if (requestCode == REQUEST_DATE_TIME
				&& resultCode == Activity.RESULT_OK) {
			// String button= data.getExtras().getString("button");
			Date datefrom = new Date(data.getExtras().getLong("mseconds"));
			ChangeFilterToParticularDate(datefrom);
		}

		if (requestCode == CHOOSE_ADDITIONAL
				&& resultCode == Activity.RESULT_OK) {
			if (data.getExtras().getInt("Result") == 5) {
				isGroupEditing = true;
				listofchoosenid.clear();
				ShowGroupEditingLayout(true);
			}
		}

		if (requestCode == REQUEST_CONTEXT_MENU) {
			if (resultCode == Activity.RESULT_OK)
				if (data.getExtras().getInt("Result") == 0) {
					Intent i = new Intent(taskorganizerActivity.this,
							taskForm.class);
					i.putExtra("_id", CurrentChoosenId);
					startActivityForResult(i, TASK_FORM_OPENED);
				} else if (data.getExtras().getInt("Result") == 1) {
					showDialog(DIALOG_DELETE_TASK);
				} else if (data.getExtras().getInt("Result") == 2) {
					Intent i = new Intent(taskorganizerActivity.this,
							taskForm.class);
					i.putExtra("parentname", CurrentChoosenName);
					i.putExtra("_id", (long) -1);
					if (filter.date_checked && filter.spinner_date == 0
							&& filter.spinner_date_from == 0) {
						i.putExtra("date", filter.date_from.getTime());
					}
					i.putExtra("parentid", CurrentChoosenId);
					startActivityForResult(i, TASK_FORM_OPENED);
				}
		}

		if (requestCode == REQUEST_CONTEXT_MENU_SMS_EMAIL
				&& resultCode == Activity.RESULT_OK) {

			if (data.getExtras().getInt("Result") == 0
					&& SMSSendTo.length() > 0)// PHONE
			{
				SMS mySMS = new SMS(getBaseContext(), CurrentTaskToSendStatus,
						true, null);
				if (!mySMS.ErrorByCreating) {
					mySMS.SendSMS(getBaseContext());
				}
			}

			if (data.getExtras().getInt("Result") == 1
					&& EmailSendTo.length() > 0)// EMAIL
			{
				SendEmail(CurrentTaskToSendStatus, EmailSendTo);
			}
		}

		if (requestCode == TASK_FORM_OPENED && resultCode == Activity.RESULT_OK) {
			if (data.getExtras().getInt("Result") == 1) {
				c.requery();
				// ll.notifyDataSetChanged();
				ll2 = new TaskHierarchyLIstViewAdapter(getApplicationContext());
				tasksListView.setAdapter(ll2);
				// loadTasksFromProvider();

				if (CurrentPage == 2) {
					FillDay(1);
				} else if (CurrentPage == 1) {
					FillMonth(1);
				} else if (CurrentPage == 3) {
					FillWeek(1);
				} else if (CurrentPage == 4) {
					FillAgenda();
				}
				// monthadapter.printMonth();
				// monthadapter.notifyDataSetChanged();
			}
		}

		if (requestCode == GROUP_EDITING && resultCode == Activity.RESULT_OK) {
			c.requery();

			// ll.notifyDataSetChanged();

			ll2 = new TaskHierarchyLIstViewAdapter(getApplicationContext());
			tasksListView.setAdapter(ll2);

			if (CurrentPage == 2) {
				FillDay(1);
			} else if (CurrentPage == 1) {
				FillMonth(1);
			} else if (CurrentPage == 3) {
				FillWeek(1);
			} else if (CurrentPage == 4) {
				FillAgenda();
			}
			// monthadapter.printMonth();
			// monthadapter.notifyDataSetChanged();
		}

		if (requestCode == GROUP_EDITING) {
			ShowGroupEditingLayout(true);
		}

		if (requestCode == FILTER_FORM_OPENED
				&& resultCode == Activity.RESULT_OK) {
			String xml = data.getExtras().getString("filter");
			filter = new Filter(xml);
			loadTasksFromProvider();

			if (CurrentPage == 2) {
				FillDay(1);
			} else if (CurrentPage == 1) {
				FillMonth(1);
			} else if (CurrentPage == 3) {
				FillWeek(1);
			} else if (CurrentPage == 4) {
				FillAgenda();
			}

		}
		if (requestCode == FILTER_CHOOSE_SAVED
				&& resultCode == Activity.RESULT_OK) {
			String xml = data.getExtras().getString("filter");
			filter = new Filter(xml);
			loadTasksFromProvider();

			if (CurrentPage == 2) {
				FillDay(1);
			} else if (CurrentPage == 1) {
				FillMonth(1);
			} else if (CurrentPage == 3) {
				FillWeek(1);
			} else if (CurrentPage == 4) {
				FillAgenda();
			}
		}
	}

	public void ShowOrHidePages() {
		// linearLayout3.setVisibility(View.VISIBLE);
		LinearLayout linearLayout1 = (LinearLayout) this
				.findViewById(R.id.linearLayout1);
		LinearLayout linearLayout2 = (LinearLayout) this
				.findViewById(R.id.linearLayout2);
		LinearLayout linearLayout_month = (LinearLayout) findViewById(R.id.linearLayout_month);
		LinearLayout linearLayout_day = (LinearLayout) findViewById(R.id.linearLayout_day);
		LinearLayout linearLayout_week = (LinearLayout) findViewById(R.id.linearLayout_week);
		LinearLayout linearLayout_agenda = (LinearLayout) findViewById(R.id.linearLayout_agenda);

		final Button button_list = (Button) findViewById(R.id.button_list);
		final Button button_month = (Button) findViewById(R.id.button_month);
		final Button button_day = (Button) findViewById(R.id.button_day);
		final Button button_week = (Button) findViewById(R.id.button_week);
		final Button button_agenda = (Button) findViewById(R.id.button_agenda);

		TextView textview_head = (TextView) findViewById(R.id.textview_head);

		Resources res = getResources();

		Drawable dr_not_pressed = res
				.getDrawable(R.drawable.background_bottom_not_pressed);
		Drawable dr_pressed = res
				.getDrawable(R.drawable.background_bottom_pressed);

		button_list.setBackgroundDrawable(dr_not_pressed);
		button_month.setBackgroundDrawable(dr_not_pressed);
		button_day.setBackgroundDrawable(dr_not_pressed);
		button_week.setBackgroundDrawable(dr_not_pressed);
		button_agenda.setBackgroundDrawable(dr_not_pressed);

		if (CurrentPage == 0) {
			linearLayout1.setVisibility(View.VISIBLE);
			linearLayout2.setVisibility(View.VISIBLE);
			linearLayout_month.setVisibility(View.GONE);
			linearLayout_day.setVisibility(View.GONE);
			linearLayout_week.setVisibility(View.GONE);
			linearLayout_agenda.setVisibility(View.GONE);

			button_list.setBackgroundDrawable(dr_pressed);

			Date newDate = new Date();
			filter.date_checked = true;
			filter.spinner_date = 0;
			filter.date_from = newDate;
			filter.spinner_date_from = 0;
			loadTasksFromProvider();

			textview_head.setText(getString(R.string.from_layout90));

		} else if (CurrentPage == 1) {
			
			//Debug.startMethodTracing("scores");
			linearLayout1.setVisibility(View.GONE);
			linearLayout2.setVisibility(View.GONE);
			linearLayout_month.setVisibility(View.VISIBLE);
			linearLayout_day.setVisibility(View.GONE);
			linearLayout_week.setVisibility(View.GONE);
			linearLayout_agenda.setVisibility(View.GONE);

			button_month.setBackgroundDrawable(dr_pressed);

			TextView filter_description = (TextView) findViewById(R.id.filter_description);
			// height_of_textview = filter_description.getMeasuredHeight();
			height_of_textview = (int) ((float) filter_description.getHeight() * 0.8f);

			textview_head.setText(getString(R.string.from_layout91));

			ChangeMonthForMonthView();
			FillMonth(0);
			FillMonth(2);
			FillMonth(1);
			//Debug.stopMethodTracing();
		} else if (CurrentPage == 2) {

			linearLayout1.setVisibility(View.GONE);
			linearLayout2.setVisibility(View.GONE);
			linearLayout_month.setVisibility(View.GONE);
			linearLayout_day.setVisibility(View.VISIBLE);
			linearLayout_week.setVisibility(View.GONE);
			linearLayout_agenda.setVisibility(View.GONE);

			button_day.setBackgroundDrawable(dr_pressed);

			Date newDate = new Date();
			filter.date_checked = true;
			filter.spinner_date = 0;
			filter.date_from = newDate;
			filter.spinner_date_from = 0;

			current_date = newDate;
			loadTasksFromProvider();
			FillDay(1);
			textview_head.setText(getString(R.string.from_layout93));
		}

		else if (CurrentPage == 3) {

			linearLayout1.setVisibility(View.GONE);
			linearLayout2.setVisibility(View.GONE);
			linearLayout_month.setVisibility(View.GONE);
			linearLayout_day.setVisibility(View.GONE);
			linearLayout_week.setVisibility(View.VISIBLE);
			linearLayout_agenda.setVisibility(View.GONE);

			button_week.setBackgroundDrawable(dr_pressed);

			textview_head.setText(getString(R.string.from_layout92));
			current_date = new Date();
			ChangeWeekForWeekView();
			FillWeek(1);
		} else if (CurrentPage == 4) {

			linearLayout1.setVisibility(View.GONE);
			linearLayout2.setVisibility(View.GONE);
			linearLayout_month.setVisibility(View.GONE);
			linearLayout_day.setVisibility(View.GONE);
			linearLayout_week.setVisibility(View.GONE);
			linearLayout_agenda.setVisibility(View.VISIBLE);

			button_agenda.setBackgroundDrawable(dr_pressed);
			textview_head.setText(getString(R.string.from_layout94));
			FillAgenda();
		}

	}

	public void ChangeMonthForMonthView() {
		filter.date_checked = true;
		filter.spinner_date = 6;

		Date datefrom = new Date(monthview_currentyear - 1900,
				monthview_currentmonth - 1, 1);
		Date dateto;

		if (monthview_currentmonth == 12) {
			// dateto = new Date(monthview_currentyear-1900,12,31,0,0,0);
			dateto = new Date(monthview_currentyear - 1900, 11, 31, 0, 0, 0);
		} else {
			Date date1 = new Date(monthview_currentyear - 1900,
					monthview_currentmonth, 1, 0, 0, 0);
			date1 = new Date(date1.getTime() - 1);
			dateto = new Date(monthview_currentyear - 1900,
					monthview_currentmonth - 1, date1.getDate(), 23, 59, 59);
		}

		filter.date_from = datefrom;
		filter.date_to = dateto;
		filter.spinner_date_from = 0;
		filter.spinner_date_to = 0;

		loadTasksFromProvider();

		// change the head
		TextView filter_description = (TextView) findViewById(R.id.filter_description);
		int version = Integer.valueOf(android.os.Build.VERSION.SDK);
		SimpleDateFormat sdf_yyyyMMdd;
		if (version < 9) {
			sdf_yyyyMMdd = new SimpleDateFormat("MMMM yyyy");
		} else {
			sdf_yyyyMMdd = new SimpleDateFormat("LLLL yyyy");
		}
		String textforbutton = getString(R.string.from_code15)
				+ sdf_yyyyMMdd.format(filter.date_from);
		filter_description.setText(textforbutton);

		TextView textview_back = (TextView) findViewById(R.id.textview_back);
		TextView textview_forward = (TextView) findViewById(R.id.textview_forward);
		Button button_back = (Button) findViewById(R.id.button_back);
		Button button_forward = (Button) findViewById(R.id.button_forward);

		button_back.setVisibility(View.VISIBLE);
		button_forward.setVisibility(View.VISIBLE);
		textview_back.setVisibility(View.VISIBLE);
		textview_forward.setVisibility(View.VISIBLE);

		SimpleDateFormat sdf = new SimpleDateFormat("MMMM");
		if (version < 9) {
			sdf = new SimpleDateFormat("MMMM");
		} else {
			sdf = new SimpleDateFormat("LLLL");
		}

		textview_back = (TextView) this.findViewById(R.id.textview_back);
		textview_back.setText(sdf.format(new Date(
				filter.date_from.getTime() - 24 * 3600 * 1000)));

		textview_forward = (TextView) this.findViewById(R.id.textview_forward);
		// textview_forward.setText(sdf.format(new Date((new
		// Date(monthview_currentyear-1900,monthview_currentmonth-1,28).getTime()+4*24*3600*1000))));
		textview_forward.setText(sdf.format(new Date(filter.date_to.getTime()
				+ 1 * 24 * 3600 * 1000)));

	}

	public void ChangeWeekForWeekView() {
		filter.date_checked = true;
		filter.spinner_date = 6;

		int numberoftheday = current_date.getDay();
		numberoftheday = (numberoftheday == 0) ? 7 : numberoftheday;
		long miliseconds = current_date.getTime() - (numberoftheday - 1) * 24
				* 3600 * 1000;
		Date datefrom = RoundDate(new Date(miliseconds), 0);

		numberoftheday = (numberoftheday == 0) ? 7 : numberoftheday;
		miliseconds = current_date.getTime() + (7 - numberoftheday) * 24 * 3600
				* 1000;
		Date dateto = RoundDate(new Date(miliseconds), 1);

		filter.date_from = datefrom;
		filter.date_to = dateto;
		filter.spinner_date_from = 0;
		filter.spinner_date_to = 0;

		loadTasksFromProvider();

		String textforbutton = "";
		// change the head
		TextView filter_description = (TextView) findViewById(R.id.filter_description);

		if (datefrom.getYear() == dateto.getYear()) {
			if (datefrom.getMonth() == dateto.getMonth()) {
				SimpleDateFormat sdf_dd = new SimpleDateFormat("dd");
				SimpleDateFormat sdf_MMMyyyy = new SimpleDateFormat("MMM yyyy");
				textforbutton = " " + sdf_dd.format(datefrom) + " - "
						+ sdf_dd.format(dateto) + " "
						+ sdf_MMMyyyy.format(dateto);
			} else {
				SimpleDateFormat sdf_yyyyMMdd = new SimpleDateFormat("dd MMM");
				SimpleDateFormat sdf_yyyy = new SimpleDateFormat("yyyy");
				textforbutton = " " + sdf_yyyyMMdd.format(datefrom) + " - "
						+ sdf_yyyyMMdd.format(dateto) + " "
						+ sdf_yyyy.format(dateto);
			}
		} else {
			SimpleDateFormat sdf_yyyyMMdd = new SimpleDateFormat(
					getString(R.string.sdf1));
			textforbutton = " " + sdf_yyyyMMdd.format(datefrom) + " - "
					+ sdf_yyyyMMdd.format(dateto);
		}

		filter_description.setText(textforbutton);

		TextView textview_back = (TextView) findViewById(R.id.textview_back);
		TextView textview_forward = (TextView) findViewById(R.id.textview_forward);
		Button button_back = (Button) findViewById(R.id.button_back);
		Button button_forward = (Button) findViewById(R.id.button_forward);

		button_back.setVisibility(View.VISIBLE);
		button_forward.setVisibility(View.VISIBLE);
		textview_back.setVisibility(View.VISIBLE);
		textview_back.setText(getResources().getString(R.string.from_layout96));

		textview_forward.setVisibility(View.VISIBLE);
		textview_forward.setText(getResources().getString(
				R.string.from_layout97));
	}

	private final class MyTouchListener implements OnTouchListener {
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
				// ClipData data = ClipData.newPlainText("", "");

				// ClipData.Item item = new ClipData.Item((CharSequence)
				// view.getTag());
				// ClipDescription NOTE_STREAM_TYPES = new ClipDescription(
				// (CharSequence) v.getTag(),new String[] {
				// ClipDescription.MIMETYPE_TEXT_PLAIN });
				// /ClipData data = new ClipData(NOTE_STREAM_TYPES, item);
				// ClipData data = new ClipData(null, null, item);

				/*
				 * DragShadowBuilder shadowBuilder = new
				 * View.DragShadowBuilder(view); view.startDrag(data,
				 * shadowBuilder, view, 0); view.setVisibility(View.INVISIBLE);
				 */
				return true;
			} else {
				return false;
			}
		}
	}

	// ---------------------------------------- DRAG AND DROP
	// -------------------------------------

	private static class MyDragShadowBuilder extends View.DragShadowBuilder {

		private static Drawable shadow;

		public MyDragShadowBuilder(View v) {
			super(v);
			shadow = new ColorDrawable(Color.LTGRAY);
		}
		@Override
		public void onProvideShadowMetrics(Point size, Point touch) {
			int width, height;

			width = getView().getWidth() / 2;
			height = getView().getHeight() / 2;

			shadow.setBounds(0, 0, width, height);
			size.set(width, height);
			touch.set(width / 2, height / 2);
		}

		@Override
		public void onDrawShadow(Canvas canvas) {
			shadow.draw(canvas);
		}
	}

	public void UpdateTheTimeOfTask(long _id, Date datefrom) {
		Date dateto = datefrom;

		if (c.moveToFirst()) {
			do {
				// Date date =new
				// Date(c.getLong(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATE)));
				long date0 = c
						.getLong(c
								.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATEFROM));
				long date1 = c
						.getLong(c
								.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATETO));
				dateto = new Date(datefrom.getTime() + (date1 - date0));

				if (date1 == date0) {
					dateto = new Date(datefrom.getTime() + 3600 * 1000);
				}

				int id = c.getInt(c.getColumnIndex("_id"));

				if (((int) _id) == id) {
					break;
				}
			} while (c.moveToNext());
		}

		ContentValues _initialValues = new ContentValues();
		String where = taskOrganizerDatabaseHelper.KEY_ID + "=" + _id;
		Date CurrentDate = new Date();
		SimpleDateFormat sdf_history = new SimpleDateFormat(
				getString(R.string.sdf2));

		String TextForHistory = "";
		_initialValues.put(taskOrganizerDatabaseHelper.KEY_DATEFROM,
				datefrom.getTime());
		_initialValues.put(taskOrganizerDatabaseHelper.KEY_DATETO,
				dateto.getTime());
		_initialValues.put(taskOrganizerDatabaseHelper.KEY_DATE,
				RoundDate(dateto, 0).getTime());

		TextForHistory = "" + sdf_history.format(CurrentDate) + " DATE FROM = "
				+ sdf_history.format(datefrom) + " DATE TO = "
				+ sdf_history.format(dateto) + " DATE = "
				+ sdf_history.format(RoundDate(dateto, 0));

		long rowID = tasksDB.update(taskOrganizerDatabaseHelper.Tasks_TABLE,
				_initialValues, where, null);

		ContentValues values = new ContentValues();
		values.put(taskOrganizerDatabaseHelper.History_task_id, _id);
		values.put(taskOrganizerDatabaseHelper.History_NAME, TextForHistory);

		rowID = tasksDB.insert(taskOrganizerDatabaseHelper.History_Table,
				"item", values);

		loadTasksFromProvider();

		if (CurrentPage == 3) {
			ChangeWeekForWeekView();
			FillWeek(1);
		} else {
			FillDay(1);
		}
	}

	private class MyDragListener implements OnDragListener {
		// Drawable enterShape =
		// getResources().getDrawable(R.drawable.shape_droptarget);
		// Drawable normalShape = getResources().getDrawable(R.drawable.shape);

		public boolean onDrag(View v, DragEvent event) {
			int action = event.getAction();
			switch (event.getAction()) {
			case DragEvent.ACTION_DRAG_STARTED:
				// Do nothing
				break;
			case DragEvent.ACTION_DRAG_ENTERED:
				// v.setBackgroundDrawable(enterShape);
				// Toast.makeText(getBaseContext(), "ACTION_DRAG_ENTERED",
				// Toast.LENGTH_SHORT).show();
				v.setBackgroundColor(Color.parseColor("#c1c1c1"));
				break;
			case DragEvent.ACTION_DRAG_EXITED:
				// v.setBackgroundDrawable(normalShape);
				// Toast.makeText(getBaseContext(), "ACTION_DRAG_EXITED",
				// Toast.LENGTH_SHORT).show();
				v.setBackgroundColor(Color.WHITE);
				break;
			case DragEvent.ACTION_DROP:
				// Dropped, reassign View to ViewGroup
				/*
				 * View view = (View) event.getLocalState(); ViewGroup owner =
				 * (ViewGroup) view.getParent(); owner.removeView(view);
				 * LinearLayout container = (LinearLayout) v;
				 * container.addView(view); view.setVisibility(View.VISIBLE);
				 * break;
				 */
				View view_drag = (View) event.getLocalState();

				String tag = (String) v.getTag();
				Date datefrom;
				int hour;
				if (tag.contains("-")) {
					String[] data = tag.split("-");
					int day = Integer.valueOf(data[0]);
					hour = Integer.valueOf(data[1]);
					datefrom = new Date(RoundDate(filter.date_from, 0)
							.getTime() + (day * 24 + hour) * 3600 * 1000);
				} else {
					hour = Integer.valueOf((String) v.getTag());
					datefrom = new Date(RoundDate(current_date, 0).getTime()
							+ hour * 3600 * 1000);
				}

				// i.putExtra("_id", Long.valueOf((String) v.getTag()));
				// int hour = Integer.valueOf((String) v.getTag());
				// Date datefrom = new
				// Date(RoundDate(current_date,0).getTime()+hour*3600*1000);
				UpdateTheTimeOfTask(Long.valueOf((String) view_drag.getTag()),
						datefrom);

				// Toast.makeText(getBaseContext(), "ACTION_DROP",
				// Toast.LENGTH_SHORT).show();
			case DragEvent.ACTION_DRAG_ENDED:
				// v.setBackgroundDrawable(normalShape);
			default:
				break;
			}
			return true;
		}
	}

	View.OnLongClickListener DragOnLongClickListener = new View.OnLongClickListener() {

		// Defines the one method for the interface, which is called when the
		// View is long-clicked
		public boolean onLongClick(View v) {

			/*
			 * ClipData.Item item = new ClipData.Item((CharSequence)
			 * v.getTag()); ClipData dragData = new
			 * ClipData(v.getTag(),ClipData.MIMETYPE_TEXT_PLAIN,item);
			 * 
			 * MyDragShadowBuilder myShadow = new
			 * MyDragShadowBuilder(imageView); v.startDrag(dragData, // the data
			 * to be dragged myShadow, // the drag shadow builder null, // no
			 * need to use local data 0 // flags (not currently used, set to 0)
			 * );
			 */

			// Toast.makeText(getBaseContext(), "Start drag",
			// Toast.LENGTH_SHORT).show();
			ClipData data = ClipData.newPlainText("", "");
			DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
			v.startDrag(data, shadowBuilder, v, 0);
			v.setVisibility(View.INVISIBLE);

			return true;

		}
	};

	private boolean DragIsAllowed() {
		int version = Integer.valueOf(android.os.Build.VERSION.SDK);
		if (version < 11) {
			return false;
		}
		return true;
	}

	// ///////////////////////////////////////////////////////////////////////////////////////
	// MONTH

	public void FillMonth(int position) {
		/*
		 * calendarView = (GridView) this.findViewById(R.id.calendar);
		 * monthadapter = new GridCellAdapter(getApplicationContext(),
		 * R.id.calendar_day_gridcell, monthview_currentmonth,
		 * monthview_currentyear); calendarView.setAdapter(monthadapter);
		 */
		printButtonsInMonth(position);
	}

	private int getNumberOfDaysOfMonth(int month, int year) {

		Date date0 = new Date();

		if (month == 12) {
			date0 = new Date(year - 1900, 11, 31, 0, 0, 0);// 11 = december
		} else {
			date0 = new Date(date0.getYear() - 1900, month, 1, 0, 0, 0);
			date0 = new Date(date0.getTime() - 1);
		}

		return date0.getDate();
	}

	private void printButtonsInMonth(int position) {
		// The number of days to leave blank at
		// the start of this month.
		int trailingSpaces = 0;
		int leadSpaces = 0;
		int daysInPrevMonth = 0;
		int prevMonth = 0;
		int prevYear = 0;
		int nextMonth = 0;
		int nextYear = 0;

		// RelativeLayout rl = (RelativeLayout)
		// this.findViewById(R.id.relativelayout_for_month2);
		int month = monthview_currentmonth;
		int year = monthview_currentyear;

		RelativeLayout rl = rl_viewpager_month_left;
		if (position == 0) {
			rl = rl_viewpager_month_left;

		} else if (position == 1) {
			rl = rl_viewpager_month_center;

		} else if (position == 2) {
			rl = rl_viewpager_month_right;
		}

		if (position == 0) {
			if (month == 1) {
				month = 12;
				year--;
			} else {
				month--;
			}
		}
		if (position == 2) {
			if (month == 12) {
				month = 1;
				year++;
			} else {
				month++;
			}
		}

		if (position != 1) {
			Date date0 = new Date(year - 1900, month - 1, 1);
			int version = Integer.valueOf(android.os.Build.VERSION.SDK);
			SimpleDateFormat sdf_yyyyMMdd;
			if (version < 9) {
				sdf_yyyyMMdd = new SimpleDateFormat("MMMM");
			} else {
				sdf_yyyyMMdd = new SimpleDateFormat("LLLL");
			}
			SimpleDateFormat sdf_yyyy = new SimpleDateFormat("yyyy");
			if (position == 0) {
				tv_left.setText(sdf_yyyyMMdd.format(date0) + "\n"
						+ sdf_yyyy.format(date0));
			} else {
				tv_right.setText(sdf_yyyyMMdd.format(date0) + "\n"
						+ sdf_yyyy.format(date0));
			}
			return;

		}
		
		//Debug.startMethodTracing();

		// rl.setOnTouchListener(myGestureListener);
		rl.removeAllViews();

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		LinearLayout linearLayout_top_button = (LinearLayout) this
				.findViewById(R.id.linearLayout_top_button);
		LinearLayout linearLayout_top_period = (LinearLayout) this
				.findViewById(R.id.linearLayout_top_period);
		LinearLayout linearLayout_buttons = (LinearLayout) this
				.findViewById(R.id.linearLayout_buttons);
		TextView filter_description = (TextView) this
				.findViewById(R.id.filter_description);

		int daysInMonth = getNumberOfDaysOfMonth(month, year);

		if (month == 12) {
			prevMonth = month-1;
			//nextMonth = 0;
			nextMonth = 1;
			prevYear = year;
			nextYear = year + 1;
			daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth, prevYear);
		} else if (month == 1) {
			prevMonth = 12;
			prevYear = year - 1;
			nextYear = year;
			daysInPrevMonth = getNumberOfDaysOfMonth(12, prevYear);
			//nextMonth = 1;
			nextMonth = 2;
		} else {
			prevMonth = month - 1;
			nextMonth = month + 1;
			nextYear = year;
			prevYear = year;
			daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth, prevYear);
		}

		// Compute how much to leave before before the first day of the
		// month.
		// getDay() returns 0 for Sunday.

		// Gregorian Calendar : MINUS 1, set to FIRST OF MONTH
		GregorianCalendar cal = new GregorianCalendar(year, month - 1, 1);
		Date date0 = new Date(year - 1900, month - 1, 1);
		int currentWeekDay = date0.getDay();
		int currentDayOfMonth = _calendar.get(Calendar.DATE);
		// Toast.makeText(getBaseContext(),
		// ""+date0.toString()+" "+currentWeekDay, Toast.LENGTH_SHORT).show();
		// int currentWeekDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
		int currentmonth = _calendar.get(Calendar.MONTH) + 1;
		int currentyear = _calendar.get(Calendar.YEAR);

		if (currentWeekDay == 0) {
			currentWeekDay = 7;
		}
		trailingSpaces = currentWeekDay;

		// if (cal.isLeapYear(cal.get(Calendar.YEAR)) && month == 1)
		// {
		// ++daysInMonth;
		// }

		int width = (int) (metrics.widthPixels / 7);

		int totalweeks = 7 - trailingSpaces;

		if (daysInMonth - totalweeks > 28) {
			totalweeks = 6;
		} else {
			totalweeks = 5;
		}

		int totalheight = (metrics.heightPixels
				- linearLayout_top_button.getHeight()
				- (int) (linearLayout_top_period.getHeight() * 0.7f)
				- filter_description.getHeight() - linearLayout_buttons
				.getHeight());
		int height = (int) (totalheight / totalweeks);

		int row = 1;
		int column = 1;

		Resources res = getResources();

		Drawable button1_month = res.getDrawable(R.drawable.button1_month);
		Drawable button2_month = res.getDrawable(R.drawable.button2_month);
		Drawable button3_month = res.getDrawable(R.drawable.button3_month);
		Drawable button4_month = res.getDrawable(R.drawable.button4_month);
		RelativeLayout.LayoutParams params;

		// Trailing Month days
		for (int i = 1; i < trailingSpaces; i++) {
			// list.add(String.valueOf((daysInPrevMonth - trailingSpaces + 1) +
			// i) + "-GREY" + "-" +prevMonth+"-"+ prevYear);
			ImageView btn = new ImageView(this);

			btn.setBackgroundDrawable(button3_month);
			if (column == 6) {
				btn.setBackgroundDrawable(button4_month);
			}
			btn.setTag(String.valueOf((daysInPrevMonth - trailingSpaces + 1)
					+ i)
					+ "-GREY" + "-" + prevMonth + "-" + prevYear);

			params = new RelativeLayout.LayoutParams(width, height);
			params.leftMargin = width * (column - 1);
			params.topMargin = height * (row - 1);
			btn.setLayoutParams(params);
			rl.addView(btn, params);

			TextView tv = new TextView(this);
			// tv.setTextAppearance(this, android.R.style.TextAppearance_Large);
			tv.setTextColor(Color.parseColor("#b2b2b2"));
			tv.setTag(String
					.valueOf((daysInPrevMonth - trailingSpaces + 1) + i)
					+ "-GREY" + "-" + prevMonth + "-" + prevYear);
			tv.setText("" + (daysInPrevMonth - trailingSpaces + 1 + i));

			params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			params.leftMargin = width * (column - 1) + 5;
			params.topMargin = height * (row - 1) + 5;
			tv.setLayoutParams(params);
			rl.addView(tv, params);

			if (column == 7) {
				column = 1;
				row++;
				totalheight -= height;
			} else {
				column++;
			}
			btn.setOnClickListener(myOnClickListenerMonth);
			// btn.setOnTouchListener(myGestureListener);
		}

		// Current Month Days
		for (int i = 1; i <= daysInMonth; i++) {

			ImageView btn = new ImageView(this);
			btn.setBackgroundDrawable(button1_month);
			if (column > 5) {
				btn.setBackgroundDrawable(button2_month);
			}
			btn.setTag("" + String.valueOf(i) + "-BLUE" + "-" + month + "-"
					+ year);
			if (row == totalweeks) {
				params = new RelativeLayout.LayoutParams(width, totalheight);
			} else {
				params = new RelativeLayout.LayoutParams(width, height);
			}
			params.leftMargin = width * (column - 1);
			params.topMargin = height * (row - 1);
			btn.setLayoutParams(params);
			rl.addView(btn, params);

			TextView tv = new TextView(this);
			// tv.setTextAppearance(this, android.R.style.TextAppearance_Large);
			tv.setTextColor(Color.parseColor("#5f5f5f"));
			tv.setTag("" + String.valueOf(i) + "-BLUE" + "-" + month + "-"
					+ year);
			tv.setText("" + i);
			params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			params.leftMargin = width * (column - 1) + 5;
			params.topMargin = height * (row - 1) + 5;
			tv.setLayoutParams(params);
			rl.addView(tv, params);

			if (position == 1) {
				AddInformationAboutTasks(i, month, year, height,
						params.topMargin, params.leftMargin, width);
			}

			if (column == 7) {
				column = 1;
				row++;
				totalheight -= height;
			} else {
				column++;
			}

			btn.setOnClickListener(myOnClickListenerMonth);
			// btn.setOnTouchListener(myGestureListener);

			if (i == currentDayOfMonth && currentmonth == month
					&& currentyear == year) {

				Shape rrs = new RoundRectShape(new float[] { 10f, 10f, 10f,
						10f, 10f, 10f, 10f, 10f }, null, null);

				// PaintDrawable p = new
				// PaintDrawable(Color.parseColor("#31b5e5"));
				PaintDrawable p = new PaintDrawable(Color.BLACK);
				p.setShape(rrs);

				tv.setBackgroundDrawable((Drawable) p);
				tv.setTextColor(Color.WHITE);
				tv.setTypeface(null, Typeface.BOLD);
				// list.add(String.valueOf(i) + "-BLUE" + "-" +month+"-"+ year);
			} else {
				// list.add(String.valueOf(i) + "-WHITE" +"-" +month+ "-" +
				// year);
			}

		}

		int column0 = column;

		// Leading Month days
		for (int i = 0; i < (7 - column0 + 1); i++) {
			ImageView btn = new ImageView(this);
			btn.setBackgroundDrawable(button3_month);
			if (column > 5) {
				btn.setBackgroundDrawable(button4_month);
			}

			btn.setTag("" + String.valueOf(i + 1) + "-GREY" + "-" + nextMonth
					+ "-" + nextYear);

			if (row == totalweeks) {
				params = new RelativeLayout.LayoutParams(width, totalheight);
			} else {
				params = new RelativeLayout.LayoutParams(width, height);
			}
			params.leftMargin = width * (column - 1);
			params.topMargin = height * (row - 1);
			btn.setLayoutParams(params);
			rl.addView(btn, params);

			TextView tv = new TextView(this);
			// tv.setTextAppearance(this, android.R.style.TextAppearance_Large);
			tv.setTextColor(Color.parseColor("#b2b2b2"));
			tv.setTag("" + String.valueOf(i + 1) + "-GREY" + "-" + nextMonth
					+ "-" + nextYear);
			tv.setText("" + (i + 1));

			params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			params.leftMargin = width * (column - 1) + 5;
			params.topMargin = height * (row - 1) + 5;
			tv.setLayoutParams(params);
			rl.addView(tv, params);

			if (column == 7) {
				column = 1;
				row++;
				totalheight -= height;
			} else {
				column++;
			}
			btn.setOnClickListener(myOnClickListenerMonth);
			// btn.setOnTouchListener(myGestureListener);

		}
		//Debug.stopMethodTracing();
	}

	public void AddInformationAboutTasks(int theday, int themonth, int theyear,
			int height, int top, int left, int width) {

		// RelativeLayout rl = (RelativeLayout)
		// this.findViewById(R.id.relativelayout_for_month2);
		RelativeLayout rl = rl_viewpager_month_center;
		// LinearLayout linearLayout_temp =
		// (LinearLayout)row.findViewById(R.id.linearLayout_temp);
		Date datetocompare = new Date(theyear - 1900, themonth - 1, theday);
		TextView filter_description = (TextView) this
				.findViewById(R.id.filter_description);
		height_of_textview = (int) (filter_description.getHeight() * 0.8f);

		Resources res = getResources();

		Drawable dots_month = res.getDrawable(R.drawable.dots_month);

		int totalshown = 0, total = 0;
		// gridcell = (Button) row.findViewById(R.id.calendar_day_gridcell);
		int maxtoshown = (int) (height - height_of_textview - 11)
				/ height_of_textview;
		// if (maxtoshown==0) {maxtoshown =1;}

		TextView lasttv = new TextView(getBaseContext());

		if (c.moveToFirst()) {
			do {
				// Task q = new Task(c);
				Date date = new Date(c.getLong(c
						.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATE)));
				// if (date.getDate() theday && date.getMonth()==themonth &&
				// date.getYear() ==theyear)
				date = RoundDate(date,0);
				if (datetocompare.compareTo(date) == 0) {
					total++;
					if (maxtoshown <= totalshown) {
						continue;
					}

					TextView tv = new TextView(getBaseContext());
					// tv.setBackgroundColor(Color.DKGRAY);
					// tv.setTextColor(Color.WHITE);
					String name = c
							.getString(c
									.getColumnIndex(taskOrganizerDatabaseHelper.KEY_NAME));
					// if (name.length()>6) name = name.substring(0, 6);

					tv.setText(name);

					RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
							width, height_of_textview);

					params.leftMargin = left - 4;
					params.topMargin = top + (totalshown + 1)
							* height_of_textview;
					tv.setLayoutParams(params);

					rl.addView(tv);

					tv.setInputType(0);
					tv.setTag("" + theday + "-" + "BLUE-" + themonth + "-"
							+ theyear);
					tv.setOnClickListener(myOnClickListenerMonth);
					tv.setOnTouchListener(myGestureListener);

					int bgcolor = c.getInt(c.getColumnIndex("categorybgcolor"));
					if (bgcolor == 0) {
						bgcolor = Color.DKGRAY;
					}
					tv.setBackgroundDrawable(CommonFunctions
							.GetDrawableNoCorner(bgcolor));

					int textcolor = c.getInt(c
							.getColumnIndex("categorytextcolor"));
					if (textcolor == 0) {
						textcolor = Color.WHITE;
					}
					tv.setTextColor(textcolor);

					final Date duedate = new Date(
							c.getLong(c
									.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DUEDATE)));
					int status_id = c
							.getInt(c
									.getColumnIndex(taskOrganizerDatabaseHelper.KEY_STATUS));
					if (status_id == 2) {
						tv.setPaintFlags(tv.getPaintFlags()
								| Paint.STRIKE_THRU_TEXT_FLAG);
					} else {
						if (duedate.before(new Date())
								&& duedate.after(new Date(0))) {
							tv.setTextColor(Color.BLACK);
						}
						tv.setPaintFlags(tv.getPaintFlags()
								& ~Paint.STRIKE_THRU_TEXT_FLAG);
					}

					tv.setTransformationMethod(SingleLineTransformationMethod
							.getInstance());
					// tv.setLayoutParams(new
					// LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
					// LayoutParams.FILL_PARENT));
					// tv.setWidth(row.getWidth()-3);
					totalshown++;
					lasttv = tv;
				}
			} while (c.moveToNext());
		}

		if (totalshown < total) {
			ImageView btn = new ImageView(this);

			btn.setBackgroundDrawable(dots_month);

			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.leftMargin = left - 4
					+ (int) ((width - dots_month.getIntrinsicWidth()) / 2);
			params.topMargin = top + 2 + (totalshown + 1) * height_of_textview;
			btn.setLayoutParams(params);
			rl.addView(btn, params);

		}

	}

	OnClickListener myOnClickListenerMonth = new OnClickListener() {
		public void onClick(View view) {
			String date_month_year = (String) view.getTag();
			String[] date_month_year_array = date_month_year.split("-");
			// selectedDayMonthYearButton.setText("Selected: " +
			// date_month_year);
			Date parsedDate = new Date(
					Integer.valueOf(date_month_year_array[3]) - 1900,
					Integer.valueOf(date_month_year_array[2]) - 1,
					Integer.valueOf(date_month_year_array[0]));

			// try
			// {
			// Date parsedDate = dateFormatter.parse(date_month_year);
			// Log.d(tag, "Parsed Date: " + parsedDate.toString());

			// SimpleDateFormat sdf_ddMM0 = new SimpleDateFormat("dd MMM yyyy");
			current_date = parsedDate;
			CurrentPage = 2;
			LinearLayout linearLayout1 = (LinearLayout) findViewById(R.id.linearLayout1);
			LinearLayout linearLayout_month = (LinearLayout) findViewById(R.id.linearLayout_month);
			LinearLayout linearLayout_day = (LinearLayout) findViewById(R.id.linearLayout_day);
			LinearLayout linearLayout_week = (LinearLayout) findViewById(R.id.linearLayout_week);
			LinearLayout linearLayout_agenda = (LinearLayout) findViewById(R.id.linearLayout_agenda);

			//linearLayout_agenda.setVisibility(View.GONE);
			//linearLayout1.setVisibility(View.GONE);
			linearLayout_month.setVisibility(View.GONE);
			linearLayout_day.setVisibility(View.VISIBLE);
			//linearLayout_week.setVisibility(View.GONE);

			Date newDate = current_date;
			filter.date_checked = true;
			filter.spinner_date = 0;
			filter.date_from = newDate;
			filter.spinner_date_from = 0;
			loadTasksFromProvider();
			FillDay(1);

			/*Resources res = getResources();

			Drawable dr_not_pressed = res
					.getDrawable(R.drawable.background_bottom_not_pressed);
			Drawable dr_pressed = res
					.getDrawable(R.drawable.background_bottom_pressed);

			final Button button_month = (Button) findViewById(R.id.button_month);
			final Button button_day = (Button) findViewById(R.id.button_day);
			button_month.setBackgroundDrawable(dr_not_pressed);
			button_day.setBackgroundDrawable(dr_pressed);*/
			
			//to avoid white lines in bottom panel
			
			final Button button_list = (Button) findViewById(R.id.button_list);
			final Button button_month = (Button) findViewById(R.id.button_month);
			final Button button_day = (Button) findViewById(R.id.button_day);
			final Button button_week = (Button) findViewById(R.id.button_week);
			final Button button_agenda = (Button) findViewById(R.id.button_agenda);

			TextView textview_head = (TextView) findViewById(R.id.textview_head);

			Resources res = getResources();

			Drawable dr_not_pressed = res
					.getDrawable(R.drawable.background_bottom_not_pressed);
			Drawable dr_pressed = res
					.getDrawable(R.drawable.background_bottom_pressed);

			button_list.setBackgroundDrawable(dr_not_pressed);
			button_month.setBackgroundDrawable(dr_not_pressed);
			button_day.setBackgroundDrawable(dr_pressed);
			button_week.setBackgroundDrawable(dr_not_pressed);
			button_agenda.setBackgroundDrawable(dr_not_pressed);

			
			
			
			/*
			 * LinearLayout linearLayout1 =
			 * (LinearLayout)findViewById(R.id.linearLayout1); LinearLayout
			 * linearLayout_month =
			 * (LinearLayout)findViewById(R.id.linearLayout_month); CurrentPage
			 * = 0; linearLayout1.setVisibility(View.VISIBLE);
			 * linearLayout_month.setVisibility(View.GONE); filter.date_checked
			 * = true; filter.spinner_date = 0; filter.date_from = parsedDate;
			 * filter.spinner_date_from = 0; loadTasksFromProvider();
			 */

			// }
			// catch (ParseException e)
			// {
			// e.printStackTrace();
			// }
		}
	};

	public class GridCellAdapter extends BaseAdapter // implements
														// OnClickListener
	{
		private static final String tag = "GridCellAdapter";
		private final Context _context;

		private final List<String> list;
		// private final String[] months = {"January", "February", "March",
		// "April", "May", "June", "July", "August", "September", "October",
		// "November", "December"};
		// private final String[] months = {"1", "2", "3", "4", "5", "6", "7",
		// "8", "9", "10", "11", "12"};
		private final int month, year;
		private int daysInMonth, prevMonthDays;
		private int currentDayOfMonth;
		// private int currentWeekDay;
		private TextView gridcell;

		// private final SimpleDateFormat dateFormatter = new
		// SimpleDateFormat("dd-MMM-yyyy");

		// Days in Current Month
		public GridCellAdapter(Context context, int textViewResourceId,
				int month, int year) {
			super();
			this._context = context;
			this.list = new ArrayList<String>();
			this.month = monthview_currentmonth;
			this.year = monthview_currentyear;

			Calendar calendar = Calendar.getInstance();
			printMonth();

		}

		private int getNumberOfDaysOfMonth(int month, int year) {

			Date date0 = new Date();

			if (month == 12) {
				date0 = new Date(year - 1900, 11, 31, 0, 0, 0);// 11 = december
			} else {
				date0 = new Date(date0.getYear() - 1900, month, 1, 0, 0, 0);
				date0 = new Date(date0.getTime() - 1);
			}

			return date0.getDate();
		}

		public String getItem(int position) {
			return list.get(position);
		}

		public int getCount() {
			return list.size();
		}

		private void printMonth() {
			// The number of days to leave blank at
			// the start of this month.
			int trailingSpaces = 0;
			int leadSpaces = 0;
			int daysInPrevMonth = 0;
			int prevMonth = 0;
			int prevYear = 0;
			int nextMonth = 0;
			int nextYear = 0;

			daysInMonth = getNumberOfDaysOfMonth(month, year);

			if (month == 12) {
				prevMonth = month;
				nextMonth = 0;
				prevYear = year;
				nextYear = year + 1;
				daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth, prevYear);
			} else if (month == 1) {
				prevMonth = 12;
				prevYear = year - 1;
				nextYear = year;
				daysInPrevMonth = getNumberOfDaysOfMonth(12, prevYear);
				nextMonth = 1;
			} else {
				prevMonth = month - 1;
				nextMonth = month + 1;
				nextYear = year;
				prevYear = year;
				daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth, prevYear);
			}

			// Compute how much to leave before before the first day of the
			// month.
			// getDay() returns 0 for Sunday.

			// Gregorian Calendar : MINUS 1, set to FIRST OF MONTH
			GregorianCalendar cal = new GregorianCalendar(year, month - 1, 1);
			Date date0 = new Date(year - 1900, month - 1, 1);
			currentDayOfMonth = _calendar.get(Calendar.DATE);
			int currentWeekDay = date0.getDay();
			// Toast.makeText(getBaseContext(),
			// ""+date0.toString()+" "+currentWeekDay,
			// Toast.LENGTH_SHORT).show();
			// int currentWeekDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
			int currentmonth = _calendar.get(Calendar.MONTH) + 1;
			int currentyear = _calendar.get(Calendar.YEAR);

			if (currentWeekDay == 0) {
				currentWeekDay = 7;
			}
			trailingSpaces = currentWeekDay;

			// if (cal.isLeapYear(cal.get(Calendar.YEAR)) && month == 1)
			// {
			// ++daysInMonth;
			// }

			// Trailing Month days
			for (int i = 1; i < trailingSpaces; i++) {
				// Log.d(tag, "PREV MONTH:= " + prevMonth + " => " +
				// getMonthAsString(prevMonth) + " " +
				// String.valueOf((daysInPrevMonth - trailingSpaces +
				// DAY_OFFSET) + i));
				list.add(String.valueOf((daysInPrevMonth - trailingSpaces + 1)
						+ i)
						+ "-GREY" + "-" + prevMonth + "-" + prevYear);
			}

			// Current Month Days
			for (int i = 1; i <= daysInMonth; i++) {
				// Log.d(currentMonthName, String.valueOf(i) + " " +
				// getMonthAsString(currentMonth) + " " + year);
				if (i == currentDayOfMonth && currentmonth == month
						&& currentyear == year) {
					list.add(String.valueOf(i) + "-BLUE" + "-" + month + "-"
							+ year);
				} else {
					list.add(String.valueOf(i) + "-WHITE" + "-" + month + "-"
							+ year);
				}
			}

			// Leading Month days
			for (int i = 0; i < list.size() % 7; i++) {
				// Log.d(tag, "NEXT MONTH:= " + getMonthAsString(nextMonth));
				list.add(String.valueOf(i + 1) + "-GREY" + "-" + nextMonth
						+ "-" + nextYear);
			}
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			if (row == null) {
				LayoutInflater inflater = (LayoutInflater) _context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				row = inflater.inflate(R.layout.calendar_day_gridcell, parent,
						false);
			}

			// Get a reference to the Day gridcell
			gridcell = (TextView) row.findViewById(R.id.calendar_day_gridcell);
			gridcell.setOnClickListener(myOnClickListenerMonth);
			gridcell.setOnTouchListener(myGestureListener);
			row.setOnClickListener(myOnClickListenerMonth);
			row.setOnTouchListener(myGestureListener);

			// ACCOUNT FOR SPACING

			// Log.d(tag, "Current Day: " + getCurrentDayOfMonth());
			String[] day_color = list.get(position).split("-");
			Button button_temp = (Button) row.findViewById(R.id.button_temp);
			RelativeLayout calendar_day_gridcell_relativeLayout = (RelativeLayout) row
					.findViewById(R.id.calendar_day_gridcell_relativeLayout);

			LinearLayout linearLayout_buttons = (LinearLayout) findViewById(R.id.linearLayout_buttons);
			TextView filter_description = (TextView) findViewById(R.id.filter_description);

			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			int height = metrics.heightPixels
					- linearLayout_buttons.getHeight() - 2
					* filter_description.getHeight() - 4;
			// int wwidth = metrics.widthPixels;

			button_temp.setHeight((int) (height / 6.1));

			String theday = day_color[0];
			String themonth = day_color[2];
			String theyear = day_color[3];

			// Set the Day GridCell
			gridcell.setText(theday);
			gridcell.setTag(theday + "-" + themonth + "-" + theyear);
			row.setTag(theday + "-" + themonth + "-" + theyear);

			if (day_color[1].equals("GREY")) {
				// gridcell.setTextColor(Color.LTGRAY);
				gridcell.setBackgroundColor(Color.DKGRAY);
				calendar_day_gridcell_relativeLayout
						.setBackgroundColor(Color.DKGRAY);
				button_temp.setBackgroundColor(Color.DKGRAY);
			}
			if (day_color[1].equals("WHITE")) {
				gridcell.setTextColor(Color.WHITE);
				calendar_day_gridcell_relativeLayout
						.setBackgroundColor(Color.BLACK);
				gridcell.setBackgroundColor(Color.BLACK);
			}
			if (day_color[1].equals("BLUE")) {
				// gridcell.setTextColor(getResources().getColor(R.color.static_text_color));
				gridcell.setTextColor(Color.BLACK);
				calendar_day_gridcell_relativeLayout
						.setBackgroundColor(Color.LTGRAY);
				gridcell.setBackgroundColor(Color.LTGRAY);
				button_temp.setBackgroundColor(Color.LTGRAY);
			}

			Date datetocompare = new Date(Integer.valueOf(theyear) - 1900,
					Integer.valueOf(themonth) - 1, Integer.valueOf(theday));

			if ((datetocompare.getDay() == 0 || datetocompare.getDay() == 6)
					&& day_color[1].equals("WHITE")) {
				gridcell.setBackgroundColor(day_off_color);
				row.setBackgroundColor(day_off_color);
				button_temp.setBackgroundColor(day_off_color);
			}

			// int widthMeasureSpec = MeasureSpec.makeMeasureSpec(100,
			// MeasureSpec.EXACTLY);
			// int heightMeasureSpec = MeasureSpec.makeMeasureSpec(25,
			// MeasureSpec.EXACTLY);
			// gridcell.measure(widthMeasureSpec, heightMeasureSpec);
			// int height_of_text_view = gridcell.getMeasuredHeight();

			AddInformationAboutTasks(Integer.valueOf(theday),
					Integer.valueOf(themonth), Integer.valueOf(theyear), row,
					(int) (height / (5)));

			return row;
		}

		public void AddInformationAboutTasks(int theday, int themonth,
				int theyear, View row, int height) {

			LinearLayout linearLayout_temp = (LinearLayout) row
					.findViewById(R.id.linearLayout_temp);
			Date datetocompare = new Date(theyear - 1900, themonth - 1, theday);

			int totalshown = 0, total = 0;
			// gridcell = (Button) row.findViewById(R.id.calendar_day_gridcell);
			int maxtoshown = (int) (height - height_of_textview)
					/ height_of_textview;
			if (maxtoshown == 0) {
				maxtoshown = 1;
			}

			TextView lasttv = new TextView(getBaseContext());

			if (c.moveToFirst()) {
				do {
					// Task q = new Task(c);
					Date date = new Date(
							c.getLong(c
									.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATE)));
					// if (date.getDate() theday && date.getMonth()==themonth &&
					// date.getYear() ==theyear)
					
					date = RoundDate(date,0);
					
					if (datetocompare.compareTo(date) == 0) {
						total++;
						if (maxtoshown <= totalshown) {
							continue;
						}

						TextView tv = new TextView(getBaseContext());
						// tv.setBackgroundColor(Color.DKGRAY);
						// tv.setTextColor(Color.WHITE);
						String name = c
								.getString(c
										.getColumnIndex(taskOrganizerDatabaseHelper.KEY_NAME));
						// if (name.length()>6) name = name.substring(0, 6);

						tv.setText(name);
						linearLayout_temp.addView(tv);
						tv.setLayoutParams(new LinearLayout.LayoutParams(
								LayoutParams.FILL_PARENT,
								LayoutParams.WRAP_CONTENT));
						tv.setInputType(0);
						tv.setTag("" + theday + "-" + themonth + "-" + theyear);
						tv.setOnClickListener(myOnClickListenerMonth);
						tv.setOnTouchListener(myGestureListener);

						int bgcolor = c.getInt(c
								.getColumnIndex("categorybgcolor"));
						if (bgcolor == 0) {
							bgcolor = Color.DKGRAY;
						}
						tv.setBackgroundColor(bgcolor);

						int textcolor = c.getInt(c
								.getColumnIndex("categorytextcolor"));
						if (textcolor == 0) {
							textcolor = Color.WHITE;
						}
						tv.setTextColor(textcolor);

						final Date duedate = new Date(
								c.getLong(c
										.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DUEDATE)));
						int status_id = c
								.getInt(c
										.getColumnIndex(taskOrganizerDatabaseHelper.KEY_STATUS));
						if (status_id == 2) {
							tv.setPaintFlags(tv.getPaintFlags()
									| Paint.STRIKE_THRU_TEXT_FLAG);
						} else {
							if (duedate.before(new Date())
									&& duedate.after(new Date(0))) {
								tv.setTextColor(Color.BLACK);
							}
							tv.setPaintFlags(tv.getPaintFlags()
									& ~Paint.STRIKE_THRU_TEXT_FLAG);
						}

						tv.setTransformationMethod(SingleLineTransformationMethod
								.getInstance());
						// tv.setLayoutParams(new
						// LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
						// LayoutParams.FILL_PARENT));
						// tv.setWidth(row.getWidth()-3);
						totalshown++;
						lasttv = tv;
					}
				} while (c.moveToNext());
			}

			if (totalshown < total) {
				lasttv.setText("...");

			}

		}

		OnClickListener myOnClickListenerMonth = new OnClickListener() {
			public void onClick(View view) {
				String date_month_year = (String) view.getTag();
				String[] date_month_year_array = date_month_year.split("-");
				// selectedDayMonthYearButton.setText("Selected: " +
				// date_month_year);
				Date parsedDate = new Date(
						Integer.valueOf(date_month_year_array[2]) - 1900,
						Integer.valueOf(date_month_year_array[1]) - 1,
						Integer.valueOf(date_month_year_array[0]));

				// try
				// {
				// Date parsedDate = dateFormatter.parse(date_month_year);
				// Log.d(tag, "Parsed Date: " + parsedDate.toString());

				// SimpleDateFormat sdf_ddMM0 = new
				// SimpleDateFormat("dd MMM yyyy");
				current_date = parsedDate;
				CurrentPage = 2;
				LinearLayout linearLayout1 = (LinearLayout) findViewById(R.id.linearLayout1);
				LinearLayout linearLayout_month = (LinearLayout) findViewById(R.id.linearLayout_month);
				LinearLayout linearLayout_day = (LinearLayout) findViewById(R.id.linearLayout_day);
				LinearLayout linearLayout_week = (LinearLayout) findViewById(R.id.linearLayout_week);
				LinearLayout linearLayout_agenda = (LinearLayout) findViewById(R.id.linearLayout_agenda);

				linearLayout_agenda.setVisibility(View.GONE);
				linearLayout1.setVisibility(View.GONE);
				linearLayout_month.setVisibility(View.GONE);
				linearLayout_day.setVisibility(View.VISIBLE);
				linearLayout_week.setVisibility(View.GONE);

				Date newDate = current_date;
				filter.date_checked = true;
				filter.spinner_date = 0;
				filter.date_from = newDate;
				filter.spinner_date_from = 0;
				loadTasksFromProvider();
				FillDay(1);

				/*
				 * LinearLayout linearLayout1 =
				 * (LinearLayout)findViewById(R.id.linearLayout1); LinearLayout
				 * linearLayout_month =
				 * (LinearLayout)findViewById(R.id.linearLayout_month);
				 * CurrentPage = 0; linearLayout1.setVisibility(View.VISIBLE);
				 * linearLayout_month.setVisibility(View.GONE);
				 * filter.date_checked = true; filter.spinner_date = 0;
				 * filter.date_from = parsedDate; filter.spinner_date_from = 0;
				 * loadTasksFromProvider();
				 */

				// }
				// catch (ParseException e)
				// {
				// e.printStackTrace();
				// }
			}
		};

	}
	

	// ///////////////////////////////////////////////////////////////////////////////////////
	// DAY

	public void DrawTasks(RelativeLayout rl) {
		final List<Long> templist;

		templist = new ArrayList<Long>();

		if (c.getCount() == 0) {
			return;
		}

		Long[][] ArrayOfTasks = new Long[c.getCount() + 1][4];

		// Long StartDateTime = (new
		// Date(current_date.getYear()+1900,current_date.getMonth()+1,current_date.getDate(),0,0,0)).getTime();
		// Long EndDateTime = (new
		// Date(current_date.getYear()+1900,current_date.getMonth()+1,current_date.getDate(),23,59,59)).getTime();

		int number_of_points = 0;
		int number_of_tasks = 0;
		// firstly, receive all time points
		int position_in_cursor = 0;

		Date StartDate = new Date(current_date.getYear(),
				current_date.getMonth(), current_date.getDate(), 0, 0, 0);
		Date EndDate = new Date(current_date.getYear(),
				current_date.getMonth(), current_date.getDate(), 23, 59, 59);
		long StartDateTime = StartDate.getTime();
		long EndDateTime = EndDate.getTime();

		if (c.moveToFirst()) {
			do {
				// Date date =new
				// Date(c.getLong(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATE)));
				Date datefrom = new Date(
						c.getLong(c
								.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATEFROM)));
				Date dateto = new Date(
						c.getLong(c
								.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATETO)));
				position_in_cursor++;
				ArrayOfTasks[position_in_cursor][0] = (long) -1;
				if ((datefrom.after(EndDate) || datefrom.before(StartDate))
						|| (dateto.after(EndDate) || dateto.before(StartDate))) {
					continue;// all day task
				}

				long starttime, endtime;

				if (datefrom.getTime() > EndDateTime
						|| datefrom.getTime() < StartDateTime) {
					starttime = StartDateTime;
				} else {
					starttime = datefrom.getTime() - StartDateTime;
				}

				if (dateto.getTime() > EndDateTime
						|| dateto.getTime() < StartDateTime) {
					endtime = EndDateTime;
				} else {
					endtime = dateto.getTime() - StartDateTime;
				}

				starttime = (Long) (starttime / 60000);// minutes
				endtime = (Long) (endtime / 60000);
				endtime--;

				if (starttime >= endtime) {
					endtime = starttime + 10;
				}

				ArrayOfTasks[position_in_cursor][0] = (long) position_in_cursor;
				ArrayOfTasks[position_in_cursor][1] = starttime;
				ArrayOfTasks[position_in_cursor][2] = endtime;

				if (!templist.contains(starttime)) {
					templist.add(starttime);
					number_of_points++;
				}

				if (!templist.contains(endtime)) {
					templist.add(endtime);
					number_of_points++;
				}

				number_of_tasks++;
			} while (c.moveToNext());
		}

		if (number_of_tasks == 0) {
			return;
		}

		// Long MinPoint = (long) (24*60);
		// int position = 0;

		Collections.sort(templist);

		// now sort all time points
		/*
		 * for (int i=0;i<number_of_points;i++) { MinPoint = (long) (24*60); for
		 * (int j=0;i<number_of_points;i++) { if (templist.get(j)==-1)
		 * {continue;}
		 * 
		 * if (templist.get(j)<MinPoint) } }
		 */

		Long[][] arrayOfWindows = new Long[templist.size()][number_of_tasks];

		int maximal_column_number = 0;

		for (int i = 1; i < c.getCount() + 1; i++) {
			if (ArrayOfTasks[i][0] == (long) -1) {
				continue;
			}

			int startposition = 0, endposition = 0;
			for (int j = 0; j < templist.size(); j++) {
				if (ArrayOfTasks[i][1].equals(templist.get(j))) {
					startposition = j;
				}
				if (ArrayOfTasks[i][2].equals(templist.get(j))) {
					endposition = j;
					break;
				}
			}

			// consider all possible columns for this task
			int column_for_task = 0;
			for (int j = 0; j < number_of_tasks; j++) {
				boolean column_founded = true;
				for (int k = startposition; k < endposition; k++) {
					if (arrayOfWindows[k][j] != null) {
						column_founded = false;
						break;
					}
				}
				if (column_founded) {
					column_for_task = j;
					break;
				}
			}

			// reserve this column
			if (column_for_task > maximal_column_number) {
				maximal_column_number = column_for_task;
			}

			ArrayOfTasks[i][3] = (long) column_for_task;
			for (int k = startposition; k <= endposition; k++) {
				arrayOfWindows[k][column_for_task] = ArrayOfTasks[i][0];
			}
		}

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int width = metrics.widthPixels - 35;

		OnClickListener myOnClickListenerTemp = new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(taskorganizerActivity.this,
						taskForm.class);
				i.putExtra("_id", Long.valueOf((String) v.getTag()));
				startActivityForResult(i, TASK_FORM_OPENED);
			}
		};

		for (int i = 1; i < c.getCount() + 1; i++) {
			if (ArrayOfTasks[i][0] == (long) -1) {
				continue;
			}

			// once again -- start and end positions
			int startposition = 0, endposition = 0;
			for (int j = 0; j < templist.size(); j++) {
				if (ArrayOfTasks[i][1].equals(templist.get(j))) {
					startposition = j;
				}
				if (ArrayOfTasks[i][2].equals(templist.get(j))) {
					endposition = j;
					break;
				}
			}

			int task_from_right = 0;
			int column_for_task = Integer
					.valueOf(ArrayOfTasks[i][3].toString());
			for (int k = startposition; k < endposition; k++) {
				int task_from_right0 = 0;
				for (int j = column_for_task + 1; j <= maximal_column_number; j++) {
					if (arrayOfWindows[k][j] != null) {
						task_from_right0++;
					}
				}
				if (task_from_right0 > task_from_right) {
					task_from_right = task_from_right0;
				}
			}

			int width_of_column = (int) (width / (column_for_task
					+ task_from_right + 1)) - 2;
			int height_of_task = (int) (ArrayOfTasks[i][2] - ArrayOfTasks[i][1]);
			height_of_task = (int) (height_of_task * getPixels(30f) / 60) - 2;

			TextView tv = new TextView(this);
			// tv.setBackgroundColor(Color.YELLOW);
			tv.setTextColor(Color.BLACK);
			RelativeLayout.LayoutParams params;
			params = new RelativeLayout.LayoutParams(width_of_column,
					height_of_task);
			c.moveToPosition((int) (ArrayOfTasks[i][0] - 1));
			tv.setText(c.getString(c
					.getColumnIndex(taskOrganizerDatabaseHelper.KEY_NAME)));

			int bgcolor = c.getInt(c.getColumnIndex("categorybgcolor"));
			if (bgcolor == 0) {
				bgcolor = Color.GRAY;
			}
			tv.setBackgroundDrawable(CommonFunctions
					.GetDrawableNoCorner(bgcolor));

			int textcolor = c.getInt(c.getColumnIndex("categorytextcolor"));
			if (textcolor == 0) {
				textcolor = Color.WHITE;
			}
			tv.setTextColor(textcolor);

			tv.setTag("" + c.getInt(c.getColumnIndex("_id")));

			tv.setOnClickListener(myOnClickListenerTemp);
			if (DragIsAllowed()) {
				tv.setOnLongClickListener(DragOnLongClickListener);
			}

			final Date duedate = new Date(c.getLong(c
					.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DUEDATE)));
			int status_id = c.getInt(c
					.getColumnIndex(taskOrganizerDatabaseHelper.KEY_STATUS));
			if (status_id == 2) {
				tv.setPaintFlags(tv.getPaintFlags()
						| Paint.STRIKE_THRU_TEXT_FLAG);
			} else {
				if (duedate.before(new Date()) && duedate.after(new Date(0))) {
					tv.setTextColor(Color.BLACK);
				}
				tv.setPaintFlags(tv.getPaintFlags()
						& ~Paint.STRIKE_THRU_TEXT_FLAG);
			}

			params.leftMargin = getPixels(22f) + width_of_column
					* column_for_task + 1 * column_for_task;
			params.topMargin = (int) ((ArrayOfTasks[i][1] - 0) * getPixels(30f) / 60);
			rl.addView(tv, params);

		}
	}

	// ///////////////////////////////////////////////////////////////////////////////////////
	// WEEK

	public void SetTextAllDayTasks(LinearLayout linearLayout_all_day_task,
			int numberoftheday) {
		TextView task_all_day_week = (TextView) linearLayout_all_day_task
				.findViewById(R.id.task_all_day_1_week);
		if (numberoftheday == 1) {
			task_all_day_week = (TextView) linearLayout_all_day_task
					.findViewById(R.id.task_all_day_1_week);
		} else if (numberoftheday == 2) {
			task_all_day_week = (TextView) linearLayout_all_day_task
					.findViewById(R.id.task_all_day_2_week);
		} else if (numberoftheday == 3) {
			task_all_day_week = (TextView) linearLayout_all_day_task
					.findViewById(R.id.task_all_day_3_week);
		} else if (numberoftheday == 4) {
			task_all_day_week = (TextView) linearLayout_all_day_task
					.findViewById(R.id.task_all_day_4_week);
		} else if (numberoftheday == 5) {
			task_all_day_week = (TextView) linearLayout_all_day_task
					.findViewById(R.id.task_all_day_5_week);
		} else if (numberoftheday == 6) {
			task_all_day_week = (TextView) linearLayout_all_day_task
					.findViewById(R.id.task_all_day_6_week);
		} else if (numberoftheday == 7) {
			task_all_day_week = (TextView) linearLayout_all_day_task
					.findViewById(R.id.task_all_day_7_week);
		}

		String text = (String) task_all_day_week.getText();
		int number_of_all_day_tasks = (text.length() == 0) ? 1 : (Integer
				.valueOf(text) + 1);
		task_all_day_week.setText("" + number_of_all_day_tasks);
		task_all_day_week.setVisibility(View.VISIBLE);
	}
	

	public void PrepareButtonsWeek(LinearLayout linearLayout_all_day_task) {
		Date beginning_of_the_week = filter.date_from;
		Date end_of_the_week = filter.date_to;

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int width = metrics.widthPixels;
		width = (int) ((width - getPixels(20f)) / 7);

		ImageView iv = (ImageView) this.findViewById(R.id.divideline_week01);
		TableRow.LayoutParams paramsl = new TableRow.LayoutParams(width, 1);
		iv.setLayoutParams(paramsl);

		iv = (ImageView) this.findViewById(R.id.divideline_week02);
		paramsl = new TableRow.LayoutParams(width, 1);
		iv.setLayoutParams(paramsl);

		iv = (ImageView) this.findViewById(R.id.divideline_week03);
		paramsl = new TableRow.LayoutParams(width, 1);
		iv.setLayoutParams(paramsl);

		iv = (ImageView) this.findViewById(R.id.divideline_week04);
		paramsl = new TableRow.LayoutParams(width, 1);
		iv.setLayoutParams(paramsl);

		iv = (ImageView) this.findViewById(R.id.divideline_week05);
		paramsl = new TableRow.LayoutParams(width, 1);
		iv.setLayoutParams(paramsl);

		iv = (ImageView) this.findViewById(R.id.divideline_week06);
		paramsl = new TableRow.LayoutParams(width, 1);
		iv.setLayoutParams(paramsl);

		iv = (ImageView) this.findViewById(R.id.divideline_week07);
		paramsl = new TableRow.LayoutParams(width, 1);
		iv.setLayoutParams(paramsl);

		OnClickListener myOnClickListenerTemp = new OnClickListener() {
			public void onClick(View v) {
				try {
					SimpleDateFormat sdf_ddMM0 = new SimpleDateFormat(
							getString(R.string.sdf1));
					current_date = sdf_ddMM0.parse((String) v.getTag());
					CurrentPage = 2;
					LinearLayout linearLayout1 = (LinearLayout) findViewById(R.id.linearLayout1);
					LinearLayout linearLayout_month = (LinearLayout) findViewById(R.id.linearLayout_month);
					LinearLayout linearLayout_day = (LinearLayout) findViewById(R.id.linearLayout_day);
					LinearLayout linearLayout_week = (LinearLayout) findViewById(R.id.linearLayout_week);
					LinearLayout linearLayout_agenda = (LinearLayout) findViewById(R.id.linearLayout_agenda);

					linearLayout1.setVisibility(View.GONE);
					linearLayout_month.setVisibility(View.GONE);
					linearLayout_day.setVisibility(View.VISIBLE);
					linearLayout_week.setVisibility(View.GONE);
					linearLayout_agenda.setVisibility(View.GONE);

					Date newDate = current_date;
					filter.date_checked = true;
					filter.spinner_date = 0;
					filter.date_from = newDate;
					filter.spinner_date_from = 0;
					loadTasksFromProvider();
					FillDay(1);

					Resources res = getResources();

					Drawable dr_not_pressed = res
							.getDrawable(R.drawable.background_bottom_not_pressed);
					Drawable dr_pressed = res
							.getDrawable(R.drawable.background_bottom_pressed);

					final Button button_week = (Button) findViewById(R.id.button_week);
					final Button button_day = (Button) findViewById(R.id.button_day);
					button_week.setBackgroundDrawable(dr_not_pressed);
					button_day.setBackgroundDrawable(dr_pressed);

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};

		LinearLayout linearLayout_day_of_week = (LinearLayout) findViewById(R.id.linearLayout_day_of_week2);

		SimpleDateFormat sdf_ddMM0 = new SimpleDateFormat(
				getString(R.string.sdf1));

		TextView task_all_day_week = (TextView) linearLayout_all_day_task
				.findViewById(R.id.task_all_day_1_week);
		task_all_day_week.setText("");
		task_all_day_week.setVisibility(View.INVISIBLE);

		// Date date0 = new Date(beginning_of_the_week);
		task_all_day_week.setTag(sdf_ddMM0.format(beginning_of_the_week));
		task_all_day_week.setOnClickListener(myOnClickListenerTemp);
		task_all_day_week.setWidth(getPixels(20f));

		TextView dayofweek_week = (TextView) linearLayout_day_of_week
				.findViewById(R.id.dayofweek_1_week);
		dayofweek_week.setTag(sdf_ddMM0.format(beginning_of_the_week));
		dayofweek_week.setOnClickListener(myOnClickListenerTemp);

		task_all_day_week = (TextView) linearLayout_all_day_task
				.findViewById(R.id.task_all_day_2_week);
		task_all_day_week.setText("");
		task_all_day_week.setVisibility(View.INVISIBLE);

		Date date0 = new Date(
				beginning_of_the_week.getTime() + 24 * 3600 * 1000);
		task_all_day_week.setTag(sdf_ddMM0.format(date0));
		task_all_day_week.setOnClickListener(myOnClickListenerTemp);
		task_all_day_week.setWidth(getPixels(20f));

		dayofweek_week = (TextView) linearLayout_day_of_week
				.findViewById(R.id.dayofweek_2_week);
		dayofweek_week.setTag(sdf_ddMM0.format(date0));
		dayofweek_week.setOnClickListener(myOnClickListenerTemp);

		task_all_day_week = (TextView) linearLayout_all_day_task
				.findViewById(R.id.task_all_day_3_week);
		task_all_day_week.setText("");
		task_all_day_week.setVisibility(View.INVISIBLE);

		date0 = new Date(beginning_of_the_week.getTime() + 2 * 24 * 3600 * 1000);
		task_all_day_week.setTag(sdf_ddMM0.format(date0));
		task_all_day_week.setOnClickListener(myOnClickListenerTemp);
		task_all_day_week.setWidth(getPixels(20f));

		dayofweek_week = (TextView) linearLayout_day_of_week
				.findViewById(R.id.dayofweek_3_week);
		dayofweek_week.setTag(sdf_ddMM0.format(date0));
		dayofweek_week.setOnClickListener(myOnClickListenerTemp);

		task_all_day_week = (TextView) linearLayout_all_day_task
				.findViewById(R.id.task_all_day_4_week);
		task_all_day_week.setText("");
		task_all_day_week.setVisibility(View.INVISIBLE);

		date0 = new Date(beginning_of_the_week.getTime() + 3 * 24 * 3600 * 1000);
		task_all_day_week.setTag(sdf_ddMM0.format(date0));
		task_all_day_week.setOnClickListener(myOnClickListenerTemp);
		task_all_day_week.setWidth(getPixels(20f));

		dayofweek_week = (TextView) linearLayout_day_of_week
				.findViewById(R.id.dayofweek_4_week);
		dayofweek_week.setTag(sdf_ddMM0.format(date0));
		dayofweek_week.setOnClickListener(myOnClickListenerTemp);

		task_all_day_week = (TextView) linearLayout_all_day_task
				.findViewById(R.id.task_all_day_5_week);
		task_all_day_week.setText("");
		task_all_day_week.setVisibility(View.INVISIBLE);

		date0 = new Date(beginning_of_the_week.getTime() + 4 * 24 * 3600 * 1000);
		task_all_day_week.setTag(sdf_ddMM0.format(date0));
		task_all_day_week.setOnClickListener(myOnClickListenerTemp);
		task_all_day_week.setWidth(getPixels(20f));

		dayofweek_week = (TextView) linearLayout_day_of_week
				.findViewById(R.id.dayofweek_5_week);
		dayofweek_week.setTag(sdf_ddMM0.format(date0));
		dayofweek_week.setOnClickListener(myOnClickListenerTemp);

		task_all_day_week = (TextView) linearLayout_all_day_task
				.findViewById(R.id.task_all_day_6_week);
		task_all_day_week.setText("");
		task_all_day_week.setVisibility(View.INVISIBLE);

		date0 = new Date(beginning_of_the_week.getTime() + 5 * 24 * 3600 * 1000);
		task_all_day_week.setTag(sdf_ddMM0.format(date0));
		task_all_day_week.setOnClickListener(myOnClickListenerTemp);
		task_all_day_week.setWidth(getPixels(20f));

		dayofweek_week = (TextView) linearLayout_day_of_week
				.findViewById(R.id.dayofweek_6_week);
		dayofweek_week.setTag(sdf_ddMM0.format(date0));
		dayofweek_week.setOnClickListener(myOnClickListenerTemp);

		task_all_day_week = (TextView) linearLayout_all_day_task
				.findViewById(R.id.task_all_day_7_week);
		task_all_day_week.setText("");
		task_all_day_week.setVisibility(View.INVISIBLE);

		date0 = new Date(beginning_of_the_week.getTime() + 6 * 24 * 3600 * 1000);
		task_all_day_week.setTag(sdf_ddMM0.format(date0));
		task_all_day_week.setOnClickListener(myOnClickListenerTemp);
		task_all_day_week.setWidth(getPixels(20f));

		dayofweek_week = (TextView) linearLayout_day_of_week
				.findViewById(R.id.dayofweek_7_week);
		dayofweek_week.setTag(sdf_ddMM0.format(date0));
		dayofweek_week.setOnClickListener(myOnClickListenerTemp);
		linearLayout_all_day_task.setVisibility(View.GONE);

	}
	

	// ///////////////////////////////////////////////////////////////////////////////////////
	// WEEK

	public void FillWeek(int position) {

		RelativeLayout rl = rl_viewpager_week_left;
		ScrollView ScrollView_week = scrollviewweekleft;
		if (position == 0) {
			rl = rl_viewpager_week_left;
			ScrollView_week = scrollviewweekleft;
		} else if (position == 1) {
			rl = rl_viewpager_week_center;
			ScrollView_week = scrollviewweekcenter;
		} else if (position == 2) {
			rl = rl_viewpager_week_right;
			ScrollView_week = scrollviewweekright;
		}

		// RelativeLayout rl = (RelativeLayout)
		// findViewById(R.id.relativeLayout_week);
		LinearLayout linearLayout_buttons = (LinearLayout) this
				.findViewById(R.id.linearLayout_buttons);

		// ScrollView ScrollView_week = (ScrollView)
		// findViewById(R.id.ScrollView_week);
		// ScrollView_week.setOnTouchListener(myGestureListener);
		rl.removeAllViews();
		LinearLayout linearLayout_all_day_task = (LinearLayout) findViewById(R.id.linearLayout_all_day_task);
		PrepareButtonsWeek(linearLayout_all_day_task);

		RelativeLayout.LayoutParams params;

		ImageView iv;
		iv = new ImageView(this);
		iv.setBackgroundColor(Color.WHITE);
		params = new RelativeLayout.LayoutParams(1, getPixels(30f) * 24
				+ linearLayout_buttons.getHeight());
		iv.setLayoutParams(params);
		rl.addView(iv, params);
		// to expand the day

		if (position == 1) {
			ScrollView_week.post(new Runnable() {
				public void run() {
					if (!isRunned) {
						// ScrollView ScrollView_week = (ScrollView)
						// findViewById(R.id.ScrollView_week);
						scrollviewweekcenter.fullScroll(ScrollView.FOCUS_DOWN);
					}
					isRunned = true;
				}
			});
		}

		TextView tv;

		int day_off_color = Color.parseColor("#eaeaea");

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int width = metrics.widthPixels;

		int widthofcolumn = (int) ((width - getPixels(22f)) / 7);

		for (int i = 0; i < 25; i++) {

			if (i > 0 && i < 25) {
				for (int j = 0; j < 7; j++) {
					iv = new ImageView(this);
					// if (i<=8 || i>18)
					if (j > 4) {
						iv.setBackgroundColor(day_off_color);
					} else {
						iv.setBackgroundColor(Color.WHITE);
					}
					params = new RelativeLayout.LayoutParams(widthofcolumn - 2,
							getPixels(30f) - 2);
					params.leftMargin = getPixels(22f) + j * widthofcolumn + 1;
					params.topMargin = getPixels(30f) * (i - 1) + 1;
					iv.setTag("" + j + "-" + (i - 1));
					// iv.setOnClickListener(myOnClickListenerNewTask);
					iv.setOnClickListener(myOnClickListenerNewTask);
					// iv.setOnTouchListener(myGestureListener);
					if (DragIsAllowed()) {
						iv.setOnDragListener(new MyDragListener());
					}
					rl.addView(iv, params);
				}
			}

			iv = new ImageView(this);
			/*
			 * if (i<8 || i>18) { iv.setBackgroundColor(Color.BLACK); } else{
			 * iv.setBackgroundColor(Color.parseColor("#1C1C1C")); }
			 */
			iv.setBackgroundColor(Color.parseColor("#f1f1f1"));
			params = new RelativeLayout.LayoutParams(width, 2);
			params.leftMargin = 0;
			params.topMargin = getPixels(30f) * (i);
			rl.addView(iv, params);

			if (i < 24) {
				tv = new TextView(this);
				if (i <= 8 || i > 18) {
					tv.setTextColor(Color.parseColor("#c1c1c1"));
				} else {
					tv.setTextColor(Color.parseColor("#828282"));
				}
				tv.setTypeface(null, Typeface.BOLD);
				params = new RelativeLayout.LayoutParams(40, getPixels(30f));
				tv.setText("" + i);
				params.leftMargin = 5;
				if (i > 9) {
					// tv.setText(""+i);
				} else {
					params.leftMargin = 5 + getPixels(7f);
				}
				// params.leftMargin = 5;
				params.topMargin = getPixels(30f) * (i) + getPixels(5f);
				rl.addView(tv, params);
			}
		}

		for (int i = 0; i < 7; i++) {

			iv = new ImageView(this);
			iv.setBackgroundColor(Color.parseColor("#bbbbbb"));
			params = new RelativeLayout.LayoutParams(2, getPixels(30f) * 24);
			params.leftMargin = getPixels(22f) + i * widthofcolumn;
			params.topMargin = 0;
			rl.addView(iv, params);
		}

		iv = (ImageView) this.findViewById(R.id.divideline_week);
		LinearLayout.LayoutParams paramsl = new LinearLayout.LayoutParams(
				width, 1);
		iv.setLayoutParams(paramsl);

		iv = (ImageView) this.findViewById(R.id.divideline_week0);
		paramsl = new LinearLayout.LayoutParams(getPixels(22f), 1);
		iv.setLayoutParams(paramsl);

		iv = (ImageView) this.findViewById(R.id.divideline_week1);
		paramsl = new LinearLayout.LayoutParams(5, 1);
		iv.setLayoutParams(paramsl);

		iv = (ImageView) this.findViewById(R.id.divideline_week2);
		paramsl = new LinearLayout.LayoutParams(getPixels(22f), 1);
		iv.setLayoutParams(paramsl);

		iv = (ImageView) this.findViewById(R.id.divideline_week3);
		paramsl = new LinearLayout.LayoutParams(5, 1);
		iv.setLayoutParams(paramsl);

		if (position != 1) {
			return;
		}

		// all day tasks and other
		if (c.getCount() > 0) {

		OnClickListener myOnClickListenerTemp = new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(taskorganizerActivity.this,
						taskForm.class);
				i.putExtra("_id", Long.valueOf((String) v.getTag()));
				startActivityForResult(i, TASK_FORM_OPENED);
			}
		};

		Date beginning_of_the_week = filter.date_from;
		Date end_of_the_week = filter.date_to;

		if (c.moveToFirst()) {
			do {
				// Date date =new
				// Date(c.getLong(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATE)));
				Date datefrom = new Date(
						c.getLong(c
								.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATEFROM)));
				Date dateto = new Date(
						c.getLong(c
								.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATETO)));

				Date StartDate = new Date(datefrom.getYear(),
						datefrom.getMonth(), datefrom.getDate(), 0, 0, 0);
				Date EndDate = new Date(datefrom.getYear(),
						datefrom.getMonth(), datefrom.getDate(), 23, 59, 59);
				long StartDateTime = StartDate.getTime();
				long EndDateTime = EndDate.getTime();

				Date date = new Date(c.getLong(c
						.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATE)));
				if (date.equals(new Date(0))) {
					continue;
				}

				if (!((datefrom.before(EndDate) && datefrom.after(StartDate))
						&& (dateto.before(EndDate) && dateto.after(StartDate)) && (datefrom
						.before(end_of_the_week) && datefrom
						.after(beginning_of_the_week)))) {
					int numberoftheday = date.getDay();
					numberoftheday = (numberoftheday == 0) ? 7 : numberoftheday;
					linearLayout_all_day_task.setVisibility(View.VISIBLE);
					SetTextAllDayTasks(linearLayout_all_day_task,
							numberoftheday);
					continue;
				}

				int numberoftheday = datefrom.getDay();
				numberoftheday = (numberoftheday == 0) ? 7 : numberoftheday;

				long starttime, endtime;

				if (datefrom.getTime() > EndDateTime
						|| datefrom.getTime() < StartDateTime) {
					starttime = StartDateTime;
				} else {
					starttime = datefrom.getTime() - StartDateTime;
				}

				if (dateto.getTime() > EndDateTime
						|| dateto.getTime() < StartDateTime) {
					endtime = EndDateTime;
				} else {
					endtime = dateto.getTime() - StartDateTime;
				}

				starttime = (Long) (starttime / 60000);// minutes
				endtime = (Long) (endtime / 60000);

				if (starttime >= endtime) {
					endtime = starttime + 10;
				}

				int width_of_column = widthofcolumn;
				int height_of_task = (int) ((endtime - starttime)
						* getPixels(30f) / 60) - 2;

				tv = new TextView(this);
				params = new RelativeLayout.LayoutParams(width_of_column,
						height_of_task);

				tv.setText(c.getString(c
						.getColumnIndex(taskOrganizerDatabaseHelper.KEY_NAME)));
				// tv.setBackgroundColor(Color.LTGRAY);
				tv.setTag("" + c.getInt(c.getColumnIndex("_id")));
				if (DragIsAllowed()) {
					tv.setOnLongClickListener(DragOnLongClickListener);
				}

				tv.setOnClickListener(myOnClickListenerTemp);

				int bgcolor = c.getInt(c.getColumnIndex("categorybgcolor"));
				if (bgcolor == 0) {
					bgcolor = Color.GRAY;
				}
				tv.setBackgroundDrawable(CommonFunctions
						.GetDrawableNoCorner(bgcolor));

				int textcolor = c.getInt(c.getColumnIndex("categorytextcolor"));
				if (textcolor == 0) {
					textcolor = Color.WHITE;
				}
				tv.setTextColor(textcolor);

				final Date duedate = new Date(
						c.getLong(c
								.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DUEDATE)));
				int status_id = c
						.getInt(c
								.getColumnIndex(taskOrganizerDatabaseHelper.KEY_STATUS));
				if (status_id == 2) {
					tv.setPaintFlags(tv.getPaintFlags()
							| Paint.STRIKE_THRU_TEXT_FLAG);
				} else {
					if (duedate.before(new Date())
							&& duedate.after(new Date(0))) {
						tv.setTextColor(Color.BLACK);
					}
					tv.setPaintFlags(tv.getPaintFlags()
							& ~Paint.STRIKE_THRU_TEXT_FLAG);
				}

				params.leftMargin = getPixels(22f) + width_of_column
						* (numberoftheday - 1);
				params.topMargin = (int) (starttime * getPixels(30f) / 60);
				rl.addView(tv, params);

			} while (c.moveToNext());
		}
	}
		// draw line -- current time
		Date today = new Date();

		for (int i = 0; i < 7; i++) {
			Date tempdate = new Date(filter.date_from.getTime() + i * 24 * 3600
					* 1000);

			if (today.getYear() == tempdate.getYear()
					&& today.getDate() == tempdate.getDate()
					&& today.getMonth() == tempdate.getMonth()) {
				iv = new ImageView(this);
				iv.setBackgroundColor(Color.parseColor("#db7e7e"));
				//params = new RelativeLayout.LayoutParams((int) ((width - getPixels(22f)) / 7), 2);
						
				params = new RelativeLayout.LayoutParams(widthofcolumn,2);
						
				//params.leftMargin = getPixels(22f) + i* ((int) ((width - 35) / 7));
				params.leftMargin = getPixels(22f) + i*widthofcolumn;
				
				
				int minutes = today.getHours() * 60 + today.getMinutes();
				params.topMargin = (int) (minutes * getPixels(30f) / 60);
				rl.addView(iv, params);

				iv = new ImageView(this);
				iv.setBackgroundColor(Color.parseColor("#db7e7e"));
				params = new RelativeLayout.LayoutParams(getPixels(22f), 2);
				params.leftMargin = 0;
				minutes = today.getHours() * 60 + today.getMinutes();
				params.topMargin = (int) (minutes * getPixels(30f) / 60);
				rl.addView(iv, params);

			}
		}

	}

	public void FillAgenda() {
		LinearLayout rl = (LinearLayout) findViewById(R.id.linearLayout_agenda_in_scroll);
		rl.removeAllViews();
		ScrollView ScrollView_agenda = (ScrollView) findViewById(R.id.ScrollView_agenda);
		// ScrollView_agenda.setOnTouchListener(myGestureListener);
		// rl.setOnTouchListener(myGestureListener);

		LinearLayout rl0 = (LinearLayout) findViewById(R.id.linearLayout_agenda);
		// rl0.setOnTouchListener(myGestureListener);

		LinearLayout.LayoutParams params;

		final List<Date> templist;

		templist = new ArrayList<Date>();

		final List<SimpleTask> listoftasks = new ArrayList<SimpleTask>();
		SimpleDateFormat sdf_EEEEddMM = new SimpleDateFormat("EEEE dd MMM yyyy");

		if (c.moveToFirst()) {
			do {
				Date date = new Date(c.getLong(c
						.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATE)));
				date = RoundDate(date, 0);

				SimpleTask st = new SimpleTask(c);
				listoftasks.add(st);

				if (!templist.contains(date)) {
					templist.add(date);
				}
			} while (c.moveToNext());
		}

		Collections.sort(templist);

		OnClickListener myOnClickListenerTemp = new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(taskorganizerActivity.this,
						taskForm.class);
				i.putExtra("_id", Long.valueOf((String) v.getTag()));
				startActivityForResult(i, TASK_FORM_OPENED);
			}
		};

		OnClickListener myOnClickListenerTempDay = new OnClickListener() {
			public void onClick(View v) {
				try {
					SimpleDateFormat sdf_ddMM0 = new SimpleDateFormat(
							getString(R.string.sdf1));
					current_date = sdf_ddMM0.parse((String) v.getTag());
					CurrentPage = 2;
					LinearLayout linearLayout1 = (LinearLayout) findViewById(R.id.linearLayout1);
					LinearLayout linearLayout_month = (LinearLayout) findViewById(R.id.linearLayout_month);
					LinearLayout linearLayout_day = (LinearLayout) findViewById(R.id.linearLayout_day);
					LinearLayout linearLayout_week = (LinearLayout) findViewById(R.id.linearLayout_week);
					LinearLayout linearLayout_agenda = (LinearLayout) findViewById(R.id.linearLayout_agenda);

					linearLayout_agenda.setVisibility(View.GONE);
					linearLayout1.setVisibility(View.GONE);
					linearLayout_month.setVisibility(View.GONE);
					linearLayout_day.setVisibility(View.VISIBLE);
					linearLayout_week.setVisibility(View.GONE);

					Date newDate = current_date;
					filter.date_checked = true;
					filter.spinner_date = 0;
					filter.date_from = newDate;
					filter.spinner_date_from = 0;
					loadTasksFromProvider();
					FillDay(1);

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};

		SimpleDateFormat sdf_ddMM0 = new SimpleDateFormat(
				getString(R.string.sdf1));

		SimpleDateFormat sdf_hhmm = new SimpleDateFormat(
				getString(R.string.sdf3));
		// not optimal algorithm
		for (int i = 0; i < templist.size(); i++) {
			Date date = templist.get(i);
			TextView tv = new TextView(this);
			// tv.setBackgroundColor(Color.LTGRAY);
			tv.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.background_panel_period));
			tv.setTextColor(Color.BLACK);
			params = new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			tv.setText(sdf_EEEEddMM.format(date));
			tv.setGravity(Gravity.CENTER | Gravity.CENTER);
			// tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);

			if (date.equals(new Date(0)) || date.before(new Date(0))) {
				tv.setText(getString(R.string.from_code72));
			}

			tv.setTag(sdf_ddMM0.format(date));
			tv.setOnClickListener(myOnClickListenerTempDay);
			tv.setOnTouchListener(myGestureListener);
			rl.addView(tv, params);

			for (int j = 0; j < listoftasks.size(); j++) {
				Date datetocompare = RoundDate(listoftasks.get(j).date, 0);

				if (datetocompare.equals(date)) {
					/*
					 * tv = new TextView(this);
					 * tv.setBackgroundColor(Color.BLACK);
					 * tv.setTextColor(Color.WHITE); params = new
					 * LinearLayout.LayoutParams
					 * (ViewGroup.LayoutParams.FILL_PARENT,
					 * ViewGroup.LayoutParams.WRAP_CONTENT);
					 */
					LinearLayout rowView = null;
					LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					rowView = (LinearLayout) inflater.inflate(
							R.layout.row_agenda, rl, false);
					tv = (TextView) rowView
							.findViewById(R.id.nameoftask_agenda);
					TextView tv_agenda_time = (TextView) rowView
							.findViewById(R.id.tv_agenda_time);

					tv.setText(listoftasks.get(j).name);
					tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);

					if (listoftasks.get(j).status == 2) {
						tv.setPaintFlags(tv.getPaintFlags()
								| Paint.STRIKE_THRU_TEXT_FLAG);
					} else {
						if (listoftasks.get(j).duedate.before(new Date())
								&& listoftasks.get(j).duedate
										.after(new Date(0))) {
							tv.setTextColor(Color.BLACK);
						}
						tv.setPaintFlags(tv.getPaintFlags()
								& ~Paint.STRIKE_THRU_TEXT_FLAG);
					}
					tv.setTag("" + listoftasks.get(j).idtask);
					tv.setOnClickListener(myOnClickListenerTemp);
					rowView.setTag("" + listoftasks.get(j).idtask);
					rowView.setOnClickListener(myOnClickListenerTemp);

					// tv.setOnTouchListener(myGestureListener);
					// rowView.setOnTouchListener(myGestureListener);

					if (listoftasks.get(j).datefrom.getTime() != (new Date(0))
							.getTime()) {
						tv_agenda_time.setVisibility(View.VISIBLE);
						tv_agenda_time.setText(""
								+ sdf_hhmm.format(listoftasks.get(j).datefrom)
								+ " - "
								+ sdf_hhmm.format(listoftasks.get(j).dateto));
					}

					rl.addView(rowView);
				}
			}
		}

	}

	OnClickListener myOnClickListenerNewTask = new OnClickListener() {
		public void onClick(View view) {

			String tag = (String) view.getTag();
			Date datefrom;
			int hour;
			if (tag.contains("-")) {
				String[] data = tag.split("-");
				int day = Integer.valueOf(data[0]);
				hour = Integer.valueOf(data[1]);
				datefrom = new Date(RoundDate(filter.date_from, 0).getTime()
						+ (day * 24 + hour) * 3600 * 1000);
			} else {
				hour = Integer.valueOf((String) view.getTag());
				datefrom = new Date(RoundDate(current_date, 0).getTime() + hour
						* 3600 * 1000);
			}

			Intent i = new Intent(taskorganizerActivity.this, taskForm.class);
			i.putExtra("_id", 0);
			i.putExtra("datefrom", datefrom.getTime());

			startActivityForResult(i, TASK_FORM_OPENED);

		}
	};

	public void FillDay(int position) {

		RelativeLayout rl = rl_viewpager_day_left;
		ScrollView ScrollView_day = scrollviewdayleft;
		if (position == 0) {
			rl = rl_viewpager_day_left;
			ScrollView_day = scrollviewdayleft;
		} else if (position == 1) {
			rl = rl_viewpager_day_center;
			ScrollView_day = scrollviewdaycenter;
		} else if (position == 2) {
			rl = rl_viewpager_day_right;
			ScrollView_day = scrollviewdayright;
		}

		// RelativeLayout rl = (RelativeLayout)
		// findViewById(R.id.relativeLayout_day);
		// ScrollView ScrollView_day = (ScrollView)
		// findViewById(R.id.ScrollView_day);
		// ScrollView_day.setOnTouchListener(myGestureListener);
		rl.removeAllViews();

		LinearLayout linearLayout_buttons = (LinearLayout) this
				.findViewById(R.id.linearLayout_buttons);
		RelativeLayout.LayoutParams params;

		ImageView iv;
		iv = new ImageView(this);
		iv.setBackgroundColor(Color.BLACK);
		params = new RelativeLayout.LayoutParams(1, getPixels(30f) * 24
				+ linearLayout_buttons.getHeight());
		iv.setLayoutParams(params);
		rl.addView(iv, params);
		// to expand the day

		if (position == 1) {
			ScrollView_day.post(new Runnable() {
				public void run() {

					if (!isRunned) {
						// ScrollView ScrollView_day = (ScrollView)
						// findViewById(R.id.ScrollView_day);
						// ScrollView_day.fullScroll(ScrollView.FOCUS_DOWN);
						scrollviewdaycenter.fullScroll(ScrollView.FOCUS_DOWN);
					}
					isRunned = true;
				}
			});
		}

		TextView tv;

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int width = metrics.widthPixels;

		/*
		 * iv = new ImageView(this); iv.setBackgroundColor(day_off_color);
		 * params = new RelativeLayout.LayoutParams(width,
		 * getPixels(30f)*8+getPixels(20f)); params.leftMargin = 0;
		 * params.topMargin = 0; rl.addView(iv, params);
		 * 
		 * 
		 * iv = new ImageView(this); iv.setBackgroundColor(day_off_color);
		 * params = new RelativeLayout.LayoutParams(width, getPixels(30f)*7);
		 * params.leftMargin = 0; params.topMargin =
		 * getPixels(20f)+getPixels(30f)*18; rl.addView(iv, params);
		 */
		int day_off_color = Color.parseColor("#eaeaea");

		int hours = 25;

		Date today = new Date();
		if (today.getYear() == current_date.getYear()
				&& today.getDate() == current_date.getDate()
				&& today.getMonth() == current_date.getMonth()) {
			hours = today.getHours();
		}

		for (int i = 0; i < 25; i++) {

			if (i > 0 && i < 25) {
				iv = new ImageView(this);
				params = new RelativeLayout.LayoutParams(
						width - getPixels(20f), getPixels(30f) - 2);
				params.leftMargin = getPixels(22f);
				params.topMargin = getPixels(30f) * (i - 1) + 1;
				iv.setTag("" + (i - 1));
				iv.setOnClickListener(myOnClickListenerNewTask);
				// iv.setOnTouchListener(myGestureListener);
				if (DragIsAllowed()) {
					iv.setOnDragListener(new MyDragListener());
				}
				rl.addView(iv, params);
			}

			iv = new ImageView(this);
			/*
			 * if (i<8 || i>18) { iv.setBackgroundColor(Color.BLACK); } else{
			 * iv.setBackgroundColor(Color.parseColor("#1C1C1C")); }
			 */
			iv.setBackgroundColor(Color.parseColor("#f1f1f1"));
			params = new RelativeLayout.LayoutParams(width, 2);
			params.leftMargin = 0;
			params.topMargin = getPixels(30f) * (i);
			rl.addView(iv, params);

			if (i < 24) {
				tv = new TextView(this);

				if (hours == 25) {
					if (i <= 8 || i > 18) {
						tv.setTextColor(Color.parseColor("#c1c1c1"));
					} else {
						tv.setTextColor(Color.parseColor("#828282"));
					}
				} else {
					if (i <= hours) {
						tv.setTextColor(Color.parseColor("#c1c1c1"));
					} else {
						tv.setTextColor(Color.parseColor("#828282"));
					}
				}
				tv.setTypeface(null, Typeface.BOLD);
				params = new RelativeLayout.LayoutParams(40, getPixels(30f));
				tv.setText("" + i);
				params.leftMargin = 5;
				if (i > 9) {
					// tv.setText(""+i);
				} else {
					params.leftMargin = 5 + getPixels(7f);
				}
				// params.leftMargin = 5;
				params.topMargin = getPixels(30f) * (i) + getPixels(5f);
				rl.addView(tv, params);
			}
		}

		iv = new ImageView(this);
		iv.setBackgroundColor(Color.parseColor("#bbbbbb"));
		params = new RelativeLayout.LayoutParams(2, getPixels(30f) * 24);
		params.leftMargin = getPixels(22f);
		params.topMargin = 0;
		rl.addView(iv, params);

		iv = (ImageView) this.findViewById(R.id.divideline_day);
		LinearLayout.LayoutParams paramsl = new LinearLayout.LayoutParams(
				width, 1);
		iv.setLayoutParams(paramsl);

		if (position != 1) {
			return;
		}

		DrawTasks(rl);

		WindowManager winMan = (WindowManager) getBaseContext()
				.getSystemService(Context.WINDOW_SERVICE);

		ListView simple_tasksListView = (ListView) this
				.findViewById(R.id.simple_tasksListView);
		SimpleLIstViewAdapter alldaylistadapter = new SimpleLIstViewAdapter(
				getApplicationContext());
		simple_tasksListView.setAdapter(alldaylistadapter);

		int maxrows = 5;

		if (winMan != null) {
			int orientation = winMan.getDefaultDisplay().getOrientation();
			if (orientation == 1) {
				maxrows = 2;
			}
		}

		if (alldaylistadapter.getCount() > maxrows) {
			// View item = alldaylistadapter.getView(0, null,
			// simple_tasksListView);
			// item.measure(0, 0);
			// ViewGroup.LayoutParams params0 = new
			// ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, (int) (5.5 *
			// item.getMeasuredHeight()));
			LinearLayout.LayoutParams params0 = new LinearLayout.LayoutParams(
					width, getPixels(30f) * (maxrows + 1));
			simple_tasksListView.setLayoutParams(params0);
		} else {
			LinearLayout.LayoutParams params0 = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			simple_tasksListView.setLayoutParams(params0);
		}

		TextView text_view_all_day = (TextView) this
				.findViewById(R.id.text_view_all_day);
		if (alldaylistadapter.getCount() == 0) {
			text_view_all_day.setVisibility(View.GONE);
		} else {
			text_view_all_day.setVisibility(View.VISIBLE);
		}

		simple_tasksListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent i = new Intent(taskorganizerActivity.this,
						taskForm.class);
				i.putExtra("_id", id);
				startActivityForResult(i, TASK_FORM_OPENED);
			}
		});

		// draw a red line -- current time
		if (today.getYear() == current_date.getYear()
				&& today.getDate() == current_date.getDate()
				&& today.getMonth() == current_date.getMonth()) {
			iv = new ImageView(this);
			iv.setBackgroundColor(Color.RED);
			params = new RelativeLayout.LayoutParams(width, 1);
			params.leftMargin = 0;
			int minutes = today.getHours() * 60 + today.getMinutes();
			params.topMargin = (int) (minutes * getPixels(30f) / 60);
			rl.addView(iv, params);

			iv = new ImageView(this);
			iv.setBackgroundColor(Color.parseColor("#bbbbbb"));
			params = new RelativeLayout.LayoutParams(2, minutes
					* getPixels(30f) / 60);
			params.leftMargin = getPixels(22f);
			params.topMargin = 0;
			rl.addView(iv, params);

		}

	}

	private class SimpleTask {
		public Integer idtask;
		public Date date;
		public String name;
		public Date duedate, datefrom, dateto;
		public int status;
		public int bgcolor, textcolor;

		public SimpleTask(Cursor c) {
			idtask = c.getInt(c.getColumnIndex("_id"));
			name = c.getString(c
					.getColumnIndex(taskOrganizerDatabaseHelper.KEY_NAME));
			date = new Date(c.getLong(c
					.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATE)));
			duedate = new Date(c.getLong(c
					.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DUEDATE)));
			status = c.getInt(c
					.getColumnIndex(taskOrganizerDatabaseHelper.KEY_STATUS));

			datefrom = new Date(c.getLong(c
					.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATEFROM)));
			dateto = new Date(c.getLong(c
					.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATETO)));

			bgcolor = c.getInt(c.getColumnIndex("categorybgcolor"));
			textcolor = c.getInt(c.getColumnIndex("categorytextcolor"));

		}

		@Override
		public String toString() {
			return name;
		}
	}

	public class SimpleLIstViewAdapter extends BaseAdapter // implements
															// OnClickListener
	{

		private static final String tag = "SimpleLIstViewAdapter";
		private final Context _context;

		private final List<SimpleTask> list;

		OnClickListener myOnClickListenerTemp = new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(taskorganizerActivity.this,
						taskForm.class);
				i.putExtra("_id", Long.valueOf((String) v.getTag()));
				startActivityForResult(i, TASK_FORM_OPENED);
			}
		};

		public SimpleLIstViewAdapter(Context context) {
			// Calendar calendar = Calendar.getInstance();
			this.list = new ArrayList<SimpleTask>();
			this._context = context;

			Date StartDate = new Date(current_date.getYear(),
					current_date.getMonth(), current_date.getDate(), 0, 0, 0);
			Date EndDate = new Date(current_date.getYear(),
					current_date.getMonth(), current_date.getDate(), 23, 59, 59);

			if (c.moveToFirst()) {
				do {
					// Date date =new
					// Date(c.getLong(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATE)));
					Date datefrom = new Date(
							c.getLong(c
									.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATEFROM)));
					Date dateto = new Date(
							c.getLong(c
									.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATETO)));
					int ms = (int) c
							.getLong(c
									.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATE));
					// if ((datefrom.getTime()>EndDate.getTime()
					// ||datefrom.getTime()<StartDate.getTime()) &&
					// (dateto.getTime()>EndDate.getTime()
					// ||dateto.getTime()<StartDate.getTime()))
					if ((datefrom.after(EndDate) || datefrom.before(StartDate))
							|| (dateto.after(EndDate) || dateto
									.before(StartDate))) {
						if (ms != 0) {
							SimpleTask st = new SimpleTask(c);
							list.add(st);
						}
					}
				} while (c.moveToNext());
			}
		}

		public long getItemId(int position) {
			return Long.valueOf(list.get(position).idtask);
		}

		public SimpleTask getItem(int position) {
			return list.get(position);
		}

		public int getCount() {
			return list.size();
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			LinearLayout rowLayout = null;
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) _context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowLayout = (LinearLayout) inflater.inflate(
						R.layout.simple_task_list_row, parent, false);
				// rowLayout = (LinearLayout)
				// inflater.inflate(android.R.layout.simple_list_item_1, parent,
				// false);
			} else {
				rowLayout = (LinearLayout) convertView;
			}

			TextView tv1 = (TextView) rowLayout
					.findViewById(R.id.textview_alldaytask);
			tv1.setText(list.get(position).name);

			tv1.setTag("" + list.get(position).idtask);
			if (DragIsAllowed()) {
				tv1.setOnLongClickListener(DragOnLongClickListener);
			}
			tv1.setOnClickListener(myOnClickListenerTemp);

			if (list.get(position).status == 2) {
				tv1.setPaintFlags(tv1.getPaintFlags()
						| Paint.STRIKE_THRU_TEXT_FLAG);
			} else {
				if (list.get(position).duedate.before(new Date())
						&& list.get(position).duedate.after(new Date(0))) {
					// tv1.setTextColor(Color.BLACK);
				}
				tv1.setPaintFlags(tv1.getPaintFlags()
						& ~Paint.STRIKE_THRU_TEXT_FLAG);
			}

			int bgcolor = list.get(position).bgcolor;
			// if (bgcolor != 0) {rowLayout.setBackgroundColor(bgcolor);}

			if (bgcolor == 0) {
				bgcolor = Color.GRAY;
			}
			tv1.setBackgroundDrawable(CommonFunctions
					.GetDrawableNoCorner(bgcolor));

			// int textcolor = list.get(position).textcolor;
			// if (textcolor != 0) {tv1.setTextColor(textcolor);}

			tv1.setPadding(getPixels(20f), 0, 0, 0);
			tv1.setTextColor(Color.WHITE);
			// tv1.setTextSize(getPixels(10f));

			// tv1.setText("hh");

			return rowLayout;
		}

	}

	// ///////////////////////////////////////////////////////////////////////////////////////
	// NEW PROCEDURES FOR HIERARCHY

	private class Triple {
		public int id, parent, position, level, status_id;

		public Triple(int _id, int _parent, int _position, int _status_id) {
			id = _id;
			parent = _parent;
			position = _position;
			level = 0;
			status_id = _status_id;
		}

		@Override
		public String toString() {
			return "" + id;
		}
	}

	public class TaskHierarchyLIstViewAdapter extends BaseAdapter // implements
																	// OnClickListener
	{

		private static final String tag = "TaskHierarchyLIstViewAdapter";
		private final Context _context;

		private final List<Triple> list;
		private final List<Triple> listtemp;

		public void FillChildren(int parent, int level) {
			for (int i = 0; i < listtemp.size(); i++) {
				if (listtemp.get(i).parent == parent) {
					list.add(listtemp.get(i));
					listtemp.get(i).level = level;
					FillChildren(listtemp.get(i).id, level + 1);
				}
			}
		}

		public TaskHierarchyLIstViewAdapter(Context context) {
			// Calendar calendar = Calendar.getInstance();
			this.list = new ArrayList<Triple>();
			this.listtemp = new ArrayList<Triple>();
			this._context = context;

			int position = 0;

			if (c.moveToFirst()) {
				do {
					int idtask = c.getInt(c.getColumnIndex("_id"));
					int parent = c
							.getInt(c
									.getColumnIndex(taskOrganizerDatabaseHelper.KEY_PARENT));
					int status_id = c
							.getInt(c
									.getColumnIndex(taskOrganizerDatabaseHelper.KEY_STATUS));
					Triple st = new Triple(idtask, parent, position, status_id);
					listtemp.add(st);
					position++;
				} while (c.moveToNext());
			}

			for (int i = 0; i < listtemp.size(); i++) {
				if (listtemp.get(i).parent == -1) {
					list.add(listtemp.get(i));
					FillChildren(listtemp.get(i).id, 1);
				}
			}

			for (int i = 0; i < listtemp.size(); i++) {
				if (!list.contains(listtemp.get(i))) {
					listtemp.get(i).parent = -1;
					list.add(listtemp.get(i));
				}
			}

		}

		public long getItemId(int position) {
			return Long.valueOf(list.get(position).id);
		}

		public Triple getItem(int position) {
			return list.get(position);
		}

		public int getCount() {
			return list.size();
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			LinearLayout rowView = null;
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) _context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = (LinearLayout) inflater.inflate(
						R.layout.row_hierarchy, parent, false);
				// rowView = (LinearLayout) inflater.inflate(R.layout.row,
				// parent, false);
			} else {
				rowView = (LinearLayout) convertView;
			}

			TextView tvDate = (TextView) rowView.findViewById(R.id.date);
			TextView tvduedate = (TextView) rowView.findViewById(R.id.duedate);
			TextView tvname = (TextView) rowView.findViewById(R.id.nameoftask);
			if (fontsize>0)
			{
				tvname.setTextSize(fontsize);
			}
			TextView tvStatus = (TextView) rowView.findViewById(R.id.status);
			TextView tvCategory = (TextView) rowView
					.findViewById(R.id.category);
			TextView tvExecutor = (TextView) rowView
					.findViewById(R.id.executor);
			TextView tvCode = (TextView) rowView.findViewById(R.id.code);
			TextView tvResponsible = (TextView) rowView
					.findViewById(R.id.responsible);
			final CheckBox MyCheckBox = (CheckBox) rowView
					.findViewById(R.id.checkBox1);

			TextView title_duedate = (TextView) rowView
					.findViewById(R.id.title_duedate);
			LinearLayout linearLayout_executor = (LinearLayout) rowView
					.findViewById(R.id.linearLayout_executor);
			LinearLayout linearLayout_responsible = (LinearLayout) rowView
					.findViewById(R.id.linearLayout_responsible);

			if (isGroupEditing) {
				MyCheckBox
						.setButtonDrawable(R.drawable.checkbox_selector_flat3);
			} else {
				MyCheckBox.setButtonDrawable(R.drawable.checkbox_selector_flat);
			}

			/*
			 * Button button_hierarchy = (Button)
			 * rowView.findViewById(R.id.button_hierarchy); int width = 1; if
			 * (list.get(position).level>0) {width =
			 * 20*list.get(position).level;} button_hierarchy.setWidth(width);
			 */

			// hierarchy
			ImageView image_hierarchy = (ImageView) rowView
					.findViewById(R.id.image_hierarchy);
			LinearLayout.LayoutParams params;
			image_hierarchy.setBackgroundColor(Color.WHITE);
			int width = 1;
			if (list.get(position).level > 0) {
				width = 40 * list.get(position).level;
			}
			params = new LinearLayout.LayoutParams(width,
					ViewGroup.LayoutParams.FILL_PARENT);
			image_hierarchy.setLayoutParams(params);
			// hierarchy

			c.moveToPosition(list.get(position).position);

			String name = c.getString(c
					.getColumnIndex(taskOrganizerDatabaseHelper.KEY_NAME));
			String code = c.getString(c
					.getColumnIndex(taskOrganizerDatabaseHelper.KEY_CODE));
			final int id = c.getInt(c.getColumnIndex("_id"));

			if ((!code.equals("" + id)) && code.length() > 0) {
				// name += " ("+code+")";
				tvCode.setText(code);
				tvCode.setVisibility(View.VISIBLE);
			} else {
				tvCode.setVisibility(View.INVISIBLE);
			}
			tvname.setText(name);

			final Date date = new Date(c.getLong(c
					.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DATE)));

			Date StartDate = new Date(0);
			if (!date.equals(StartDate)) {
				tvDate.setText(sdf_ddMM.format(date));
			} else {
				tvDate.setText(getString(R.string.from_code7));
			}

			final String status = c.getString(c.getColumnIndex("statusname"));
			tvStatus.setText(status);

			// int status_id =
			// c.getInt(c.getColumnIndex(taskOrganizerDatabaseHelper.KEY_STATUS));
			int status_id = list.get(position).status_id;

			MyCheckBox.setOnCheckedChangeListener(null);

			if (isGroupEditing) {
				if (listofchoosenid.contains("" + id)) {
					MyCheckBox.setChecked(true);
				} else {
					MyCheckBox.setChecked(false);
				}
			} else {
				if (status_id == 2) {
					tvname.setPaintFlags(tvname.getPaintFlags()
							| Paint.STRIKE_THRU_TEXT_FLAG);
					if (!MyCheckBox.isChecked()) {
						MyCheckBox.setChecked(true);
					}
					tvname.setTextColor(Color.parseColor("#cacaca"));
					tvDate.setTextColor(Color.parseColor("#cacaca"));
					tvduedate.setTextColor(Color.parseColor("#cacaca"));
					tvCode.setTextColor(Color.parseColor("#cacaca"));
					tvResponsible.setTextColor(Color.parseColor("#cacaca"));
					tvExecutor.setTextColor(Color.parseColor("#cacaca"));

				} else {
					if (MyCheckBox.isChecked()) {
						MyCheckBox.setChecked(false);
					}
					tvname.setPaintFlags(tvname.getPaintFlags()
							& ~Paint.STRIKE_THRU_TEXT_FLAG);

					tvname.setTextColor(Color.parseColor("#000000"));
					tvDate.setTextColor(Color.parseColor("#000000"));
					tvduedate.setTextColor(Color.parseColor("#000000"));
					tvCode.setTextColor(Color.parseColor("#000000"));
					tvResponsible.setTextColor(Color.parseColor("#000000"));
					tvExecutor.setTextColor(Color.parseColor("#000000"));
				}
			}

			final String category = c.getString(c
					.getColumnIndex("categoryname"));
			tvCategory.setText(category);

			int bgcolor = c.getInt(c.getColumnIndex("categorybgcolor"));
			tvCategory.setPadding(5, 0, 5, 0);
			if (bgcolor != 0) {
				// tvCategory.setPadding(5, 0, 5, 0);
				// tvCategory.setBackgroundColor(bgcolor);
				tvCategory.setBackgroundDrawable(CommonFunctions
						.GetDrawable(bgcolor));
			} else {
				// tvCategory.setPadding(5, 0, 5, 0);
				// tvCategory.setBackgroundColor(bgcolor);
				tvCategory.setBackgroundDrawable(CommonFunctions
						.GetDrawable(Color.DKGRAY));

			}
			int textcolor = c.getInt(c.getColumnIndex("categorytextcolor"));
			if (textcolor != 0) {
				tvCategory.setTextColor(textcolor);
			} else {
				tvCategory.setTextColor(Color.WHITE);
			}

			final Date duedate = new Date(c.getLong(c
					.getColumnIndex(taskOrganizerDatabaseHelper.KEY_DUEDATE)));

			if (!duedate.equals(StartDate)) {
				tvduedate.setText(sdf_ddMM.format(duedate));
				tvduedate.setVisibility(View.VISIBLE);
				title_duedate.setVisibility(View.VISIBLE);
			} else {
				tvduedate.setVisibility(View.INVISIBLE);
				title_duedate.setVisibility(View.INVISIBLE);
				tvduedate.setText("");
			}

			String executor = c.getString(c
					.getColumnIndex(taskOrganizerDatabaseHelper.KEY_EXECUTOR));

			if (executor.startsWith("content:")) {
				// Uri contactData = Uri.parse(executor);
				String contactname;
				if (!ExecutorMap.containsKey(executor)) {
					// Cursor c_contact = managedQuery(contactData, null, null,
					// null, null);
					// c_contact.moveToFirst();
					// contactname =
					// c_contact.getString(c_contact.getColumnIndexOrThrow(People.NAME));
					contactname = CommonFunctions.GetContactNameFromString(
							taskorganizerActivity.this, executor);
					ExecutorMap.put(executor, contactname);
				} else {
					contactname = ExecutorMap.get(executor);
				}
				executor = contactname;
				// linearLayout_executor.setVisibility(View.VISIBLE);
			}
			if (executor.length() > 0) {
				tvExecutor.setText(executor);
				linearLayout_executor.setVisibility(View.VISIBLE);
			} else {
				tvExecutor.setText("");
				linearLayout_executor.setVisibility(View.GONE);
			}

			String responsible = c
					.getString(c
							.getColumnIndex(taskOrganizerDatabaseHelper.KEY_RESPONSIBLE));

			if (responsible.startsWith("content:")) {
				// Uri contactData = Uri.parse(responsible);
				String contactname;
				if (!ExecutorMap.containsKey(responsible)) {
					// Cursor c_contact = managedQuery(contactData, null, null,
					// null, null);
					// if (c_contact.moveToFirst())
					// {
					contactname = CommonFunctions.GetContactNameFromString(
							taskorganizerActivity.this, responsible);
					// contactname =
					// c_contact.getString(c_contact.getColumnIndexOrThrow(People.NAME));
					ExecutorMap.put(responsible, contactname);
					// }
					// else
					// {
					// contactname =responsible;
					// }
				} else {
					contactname = ExecutorMap.get(responsible);
				}
				responsible = contactname;
				// linearLayout_responsible.setVisibility(View.VISIBLE);
			}
			if (responsible.length() > 0) {
				tvResponsible.setText(responsible);
				linearLayout_responsible.setVisibility(View.VISIBLE);
			} else {
				tvResponsible.setText("");
				linearLayout_responsible.setVisibility(View.GONE);
			}

			MyCheckBox.setTag(id);
			MyCheckBox.setOnCheckedChangeListener(myOnCheckedChangeListener);

			return rowView;
		}

	}

}