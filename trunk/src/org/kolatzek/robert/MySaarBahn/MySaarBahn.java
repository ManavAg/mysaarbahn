package org.kolatzek.robert.MySaarBahn;


import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TabActivity;
import android.os.Bundle;
import android.os.Handler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.*;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Date;
import com.google.android.maps.*;

import org.kolatzek.robert.MySaarBahn.*;

public class MySaarBahn extends TabActivity  implements LocationListener, Runnable {
	TextView tv = null;
	LocationManager lm = null;
	SQLiteDatabase db = null;
	StationsData sd = null;
	static int WEEKDAY = 0;
	static int FRIDAY = 1;
	static int SATURDAY = 2;
	static int SUNDAY = 3;
	TabHost mTabHost;
	TableLayout roadmapWidget;
	Spinner otherStation;
	String selectedDestination = null;
	Station selectedStation= null;
	Intent mapView = null;
	TabSpec positionTab;
	TabSpec roadmapTab;
	TabSpec mapTab;
	Boolean dataInstalled = false;
	Station startStation = null;
	String data_date = "";
	String data_sheets_url = "";
	String data_version = "";
	int check_date = 0;
	int mapFlag;
	static int update_interval = 7*24*60*60;
	Dialog installDialog;
	String firstStation;
	String lastStation;
	boolean autoLocation = true;
	private Thread myThread = null;
	private Handler myHandler = null;

    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        if(myHandler == null)
        {
        	myHandler = new Handler();
        }
        if(myThread == null)
        {
        	myThread = new Thread(this);
        	myThread.start();
        }
        
        setContentView(R.layout.main);

        db = openOrCreateDatabase("mysaarbahnDB", MODE_PRIVATE, null);
        
    	if(		db.rawQuery("SELECT name FROM sqlite_master WHERE name='geopositions'", null).getCount() > 0 
    			&&
    			db.rawQuery("SELECT name FROM sqlite_master WHERE name='Roadmap'", null).getCount() > 0
    		)
    	{
    		dataInstalled = true;
    	}

    	int now =  (int) ((new Date()).getTime() * 0.001);
        try
        {
        	SharedPreferences prefs = getSharedPreferences("mySaarBahn.prefs", MODE_PRIVATE);
        	data_version = prefs.getString("data_version", "0");
        	check_date = prefs.getInt("last_update", 0);
        	if ( (now < (check_date+update_interval)))
        	{
        		dataInstalled = true;
        	}
        	else if( checkVersion(data_version) == true )
        	{
        		check_date = now;
        		dataInstalled = true;
        	}
        	else
        	{
        		dataInstalled = false;
        	}
        }
        catch (Exception e)
        {
        	Log.i("get preferences", "first start, no prefs");
        }
    	
    	if(dataInstalled == false)
    	{
    		data_date = "";
			data_sheets_url = "";
			data_version = "";
    		Toast.makeText(this, getString(R.string.No_data_found_Insteallation), Toast.LENGTH_SHORT);
    		installDialog = new Dialog(this);
    		installDialog.setContentView(R.layout.install_dialog);
    		installDialog.getWindow().setTitle(getString(R.string.installTitle));
    		if(check_date != 0 && now > check_date+update_interval)
    		{
    			((TextView) installDialog.findViewById(R.id.installTextView)).setText(R.string.updateText);
    		}
            installDialog.show();
    		Button dataInstallButton = (Button) installDialog.findViewById(R.id.installButton);
            dataInstallButton.setOnClickListener(doInstallListener);
    	}
    	else
    	{
    		init();
    	}
    }
    
	
	public void onStop()
	{
		super.onStop();
		try{installDialog.dismiss();}catch(Exception e){}
		try
		{
			SharedPreferences prefs = getSharedPreferences("mySaarBahn.prefs", MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("data_version", data_version);
			editor.putInt("last_update", check_date);
			editor.commit();
		}
		catch (Exception e)
		{
			Log.e("mySaarBahnSettings", "not saved");
		}
	}
    
    public void init()
    {
        mTabHost = getTabHost();        
        positionTab = mTabHost.newTabSpec("position").setIndicator(getString(R.string.Position)).setContent(R.id.position);
        mTabHost.addTab(positionTab);
        roadmapTab = mTabHost.newTabSpec("roadmapScrollView").setIndicator(getString(R.string.Roadmap)).setContent(R.id.roadmap);
        mTabHost.addTab(roadmapTab);
        mapView = new Intent(this, Map.class);
        mapFlag=Intent.FLAG_ACTIVITY_CLEAR_TOP;
        mapTab = mTabHost.newTabSpec("map").setIndicator(getString(R.string.Map)).setContent(mapView.addFlags(mapFlag)); //.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        mTabHost.addTab(mapTab);
        mTabHost.setOnTabChangedListener(TabChangeListener);

        tv = (TextView) findViewById(R.id.actualPosTextView);
        sd = new StationsData();
		roadmapWidget = (TableLayout) findViewById(R.id.roadmap);
		
        otherStation = (Spinner) findViewById(R.id.otherStation);
        Cursor stations = db.query(true, "geopositions", new String[]{"name"}, null, null, null, null, "name ASC", null);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        while(stations.moveToNext())
        {
        	spinnerArrayAdapter.add(stations.getString(stations.getColumnIndex("name")));
        }
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        otherStation.setAdapter((SpinnerAdapter)spinnerArrayAdapter);
        otherStation.setOnItemSelectedListener(showSaarbahnStationsItemClick);
        
        Criteria crit = new Criteria();
		crit.setAccuracy(Criteria.ACCURACY_COARSE);
		crit.setAltitudeRequired(false);
		crit.setBearingRequired(false);
		crit.setCostAllowed(false);
		crit.setPowerRequirement(Criteria.POWER_HIGH);
		crit.setSpeedRequired(false);
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		List<String> LBS_list = lm.getProviders(crit, false);
		String bestLM = "";
		for(int i=0; i < LBS_list.size(); i++)
		{
			bestLM = bestLM  + " " + (String)LBS_list.get(i);
		}
		bestLM = getString(R.string.Best_location_services, bestLM);
		//Toast t = Toast.makeText(this, "", 0);
		String provider = "";
		if(LBS_list.size() == 0)
		{
			tv.setText(R.string.No_location_service_active);
			//t = Toast.makeText(this, R.string.No_location_service_active, Toast.LENGTH_LONG);
			myHandler.post(new Toaster(this, R.string.No_location_service_active, Toast.LENGTH_LONG));
		}
		else
		{
			String bestProvider = lm.getBestProvider(crit, false);
			if( lm.isProviderEnabled(bestProvider) ) //When best provider active
			{
				provider = bestProvider;
				lm.requestLocationUpdates(provider, 10000, 25, this);
				//t = Toast.makeText(this, bestLM +" "+ getString(R.string.I_will_use_PROVIDER, provider), Toast.LENGTH_LONG);
				//Log.i("prov", provider);
				//myHandler.post(new Toaster(this, bestLM +" "+ getString(R.string.I_will_use_PROVIDER, provider), Toast.LENGTH_LONG));
			}
			else if( lm.isProviderEnabled((String)LBS_list.get(0)) && bestProvider !=  (String)LBS_list.get(0)) //When bestprovider != the first, but first is enabled
			{
				provider = (String) LBS_list.get(0);
				lm.requestLocationUpdates(provider, 10000, 25, this);
				//t = Toast.makeText(this, bestLM +" "+ getString(R.string.I_will_use_PROVIDER, provider), Toast.LENGTH_LONG);
				//Log.i("prov", provider);
				//myHandler.post(new Toaster(this, bestLM +" "+ getString(R.string.I_will_use_PROVIDER, provider), Toast.LENGTH_LONG));
			}

			else if( LBS_list.size() > 1 && lm.isProviderEnabled((String)LBS_list.get(1)) && bestProvider !=  (String)LBS_list.get(1))//When bestprovider != the second, but second is enabled
			{
				provider = (String) LBS_list.get(1);
				lm.requestLocationUpdates(provider, 10000, 25, this);
				//t = Toast.makeText(this, bestLM +" "+ getString(R.string.I_will_use_PROVIDER, provider), Toast.LENGTH_LONG);
				//Log.i("prov", provider);
				//myHandler.post(new Toaster(this, bestLM +" "+ getString(R.string.I_will_use_PROVIDER, provider), Toast.LENGTH_LONG));
			}
			else
			{
				//t = Toast.makeText(this, bestLM +" "+ getString(R.string.Please_activate_PROVIDER, bestProvider), 3000);
				myHandler.post(new Toaster(this, bestLM +" "+ getString(R.string.Please_activate_PROVIDER, bestProvider), 3000));
			}
		}
		//t.show();
		try
		{
			Location last = lm.getLastKnownLocation(provider);
			if(last == null)
			{
				tv.setText(R.string.NoOldPositionFound);
			}
			else
			{
				//Toast.makeText(this, R.string.I_will_use_last_known_position, Toast.LENGTH_SHORT).show();
				myHandler.post(new Toaster(this, R.string.I_will_use_last_known_position, Toast.LENGTH_LONG));
				if(dataInstalled)
				{
					autoLocation = true;
					calcNewLocation(last);
					autoLocation = true;
				}
				else
				{
					//warn -> no data
				}
			}
		}
		catch (SecurityException e)
		{
			Toast t2 = Toast.makeText(this, e.toString() + provider, 2000);
			t2.show();
		}
		catch (IllegalArgumentException e)
		{
			Toast t2 = Toast.makeText(this, e.toString() + provider, 2000);
			t2.show();
		}
        
    }
    
	public void onLocationChanged(Location location) 
	{
		autoLocation = true;
    	if(dataInstalled == false)
    	{
    		return; //Do nothing if data are not installed
    	}
		calcNewLocation(location);
	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		lm.removeUpdates(this);
		Toast.makeText(this, R.string.No_location_service_active_no_help, 5000).show();
	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		lm.requestLocationUpdates(provider, 10000, 50, this);
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
	}

	OnTabChangeListener TabChangeListener = new OnTabChangeListener()
	{
		public void onTabChanged(String arg0) {
			int selectedTab = mTabHost.getCurrentTab();
			//Log.w("TAB", selectedTab+"");
			if (selectedTab != 2)
			{
				//mapTab.setContent( new TabContentFactory(){public View createTabContent(String tag){return mTabHost;}});
				if(startStation != null)
				{
					mapView.removeExtra("org.kolatzek.robert.MySaarBahn.station_name");
					mapView.removeExtra("org.kolatzek.robert.MySaarBahn.station_new");
				}
			}
			else
			{
				//Log.e("putExtra", startStation.name);
				if(startStation != null)
				{
					mapView.putExtra("org.kolatzek.robert.MySaarBahn.station_name", startStation.name);
				}
			}
		}
	};
	
    OnClickListener doInstallListener = new OnClickListener()
    {
    	public void onClick(View v)
    	{
    		installDialog.setTitle(getString(R.string.installing));
    		TextView itv = (TextView) installDialog.findViewById(R.id.installTextView);
			itv.setText(R.string.installHint);
    		checkVersion(data_version);
    		Button dataInstallButton = (Button) installDialog.findViewById(R.id.installButton);
    		dataInstallButton.setVisibility(View.GONE);
    		myHandler.post(new Toaster(installDialog.getContext(), installDialog.getContext().getString(R.string.installing_roadmap), Toast.LENGTH_SHORT));
	    	Runnable inst = new Runnable()
    		{
    		    /**
    		     * Install geopositions and data from StationData or online-csv
    		     * @return boolean true 
    		     */
    			public void run()
    			{
    		        db.execSQL("DROP TABLE IF EXISTS geopositions");
    		        db.execSQL("CREATE TABLE IF NOT EXISTS geopositions (name, latitude, longitude, shortname, position INTEGER, PRIMARY KEY(position DESC))");
    		        sd = new StationsData();
    		        List<Station> orte = sd.getBySort();
    		        String StationsCols = "";
    		        Iterator i = orte.iterator();
    		        while(i.hasNext())
    		        {
    		        	Station actual = (Station) i.next();
    		        	String[] o = {actual.name, actual.latitude+"", actual.logitude+"", actual.shortname, actual.position+""};
    		        	StationsCols = StationsCols + ", " + actual.shortname+" INTEGER";
    		        	db.execSQL("INSERT OR IGNORE INTO geopositions VALUES (?, ?, ?, ?, ?)", o);
    		        }
    		        
    		        // Install data
    		        db.execSQL("DROP TABLE IF EXISTS Roadmap");
    		        db.execSQL("CREATE TABLE IF NOT EXISTS Roadmap (type"+StationsCols+")");
    		        boolean installation = true;
    		        try
    		        {
    			        URL file = new URL(data_sheets_url);
    			    	BufferedReader in = new BufferedReader(new InputStreamReader(file.openStream()));
    			    	String inputLine;
    			    	while ((inputLine = in.readLine()) != null && installation != false)
    			    	{
    			    		String[] line = inputLine.split(",");
    			    		if(installRoadmap(Integer.parseInt(line[0]), line[1]))
    			    		{
    			    			Log.i("install data", line[0]+": "+line[1]+" installed");
    			    		}
    			    		else
    			    		{
    			    			installation = false;
    			    		}
    			    	}
    			        in.close();
    		        }
    		        catch (Exception ex) { Log.e("ErrorGettingDataFeed", installDialog.getContext().getString(R.string.can_not_open_data, data_sheets_url));}
    		        
    		        if(installation == true)
    		        {
    		        	dataInstalled = true;

    		    		if(dataInstalled == true)
    		    		{
    		        		myHandler.post(new Toaster(installDialog.getContext(), installDialog.getContext().getString(R.string.all_roadmap_data_installed), Toast.LENGTH_LONG));
    		            	if(installDialog != null && installDialog.isShowing())
    		            	{
    		            		installDialog.hide();
    		            		init();
    		            	}
    		    		}
    		        }
    			}
    			
    			/**
    			 * Get and install roadmap data per day/type 
    			 * @param int type - type of day
    			 * @param String url - url of csv with data
    			 * @return boolean true if successfull
    			 */
    			public boolean installRoadmap(int type, String url)
    			{
    				//myHandler.post(new Toaster(d.getContext(), d.getContext().getString(R.string.downloading_data), Toast.LENGTH_SHORT));
    				boolean installation = false;
    		        try
    		        {
    			        URL file = new URL(url);
    			    	BufferedReader in = new BufferedReader(new InputStreamReader(file.openStream()));
    			    	String inputLine;
    			    	while ((inputLine = in.readLine()) != null)
    			    	{
    			    		String[] line = inputLine.split(",");
    			    		String stops = "";
    			    		for(int i=0; i<line.length; i++)
    			    		{
    			    			stops = stops + ", " + line[i];
    			    		}
    			    		db.execSQL("INSERT OR IGNORE INTO Roadmap VALUES ("+type+stops+")");
    			    	}
    			        in.close();
    			        installation = true;
    			        return installation;
    		        }
    		        catch (Exception ex) 
    		        { Log.e("installRoadmapError", installDialog.getContext().getString(R.string.can_not_open_data, url));}
    		        return false;
    			}
    		};
    		myHandler.post(inst);
    	}
    };
    
    OnClickListener destinationChangeListener = new OnClickListener()
    {
    	public void onClick(View v)
    	{
    		//Log.i("destinationChangeListener", "clicked: "+((TextView) v).getText().toString());
    		Station s = sd.getStationByName(((TextView) v).getText().toString());
    		selectedDestination = s.shortname;
    		/*Log.w("dest", "Select shortname FROM geopositions WHERE name="+((TextView) v).getText().toString());
    		Cursor sel = db.rawQuery("SELECT shortname FROM geopositions WHERE name = '"+((TextView) v).getText().toString()+"'", null);
    		if(sel.getCount() > 0)
    		{
    			sel.moveToFirst();
        		Log.w("selectedDestination", sel.getString(sel.getColumnIndex("shortname")));
    			selectedDestination = sel.getString(sel.getColumnIndex("shortname"));
    		}*/
        	if(dataInstalled == false)
        	{
        		return; //Do nothing if data are not installed
        	}
    		calcRoadmap(selectedStation);
    	}
    };
    
    OnItemSelectedListener showSaarbahnStationsItemClick = new OnItemSelectedListener()
    {
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
    		//Log.i("showDestinationStations", "clicked");
			if(autoLocation == false)
			{
	    		otherStation = (Spinner) findViewById(R.id.otherStation);
	    		String s = otherStation.getSelectedItem().toString();
	    		HashMap<String, Station> orte = sd.getStations();
	    		Station station = orte.get(s);
	    		tv.setText(getString(R.string.to_selected_station, station.name, (new BigDecimal(station.distance)).setScale( 2, BigDecimal.ROUND_HALF_UP )));
	    		calcRoadmap(station);
	    		mapView.putExtra("org.kolatzek.robert.MySaarBahn.station_name", station.name);
		    	mapView.putExtra("org.kolatzek.robert.MySaarBahn.station_new", true);
	    		//Log.d("s", station.name);
	    		calcRoadmap(station);
			}
			autoLocation = false;
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
		}
    };

    /**
     * Calculate data for new location
     * @param location
     */
	public void calcNewLocation(Location location)
	{
        HashMap<String, Station> orte = sd.getStations();
        Date d = new Date();
        for(Iterator<String> iterator = orte.keySet().iterator(); iterator.hasNext();)
        {
        	String key = iterator.next();
        	orte.get(key).distance = Distance.calculateDistance(location, orte.get(key).getLocation(), Distance.KILOMETERS);
        	orte.get(key).time = d.getTime();
        }
        List<Station> ls = sd.getByDistances();
        Station next = ls.get(0);
        
        tv.setText(getString(R.string.to_next_station, (new BigDecimal(next.distance)).setScale( 2, BigDecimal.ROUND_HALF_UP ), next.name));
        
        calcRoadmap(next);
        startStation = next;
        if(startStation != null)
        {
	        mapView.putExtra("org.kolatzek.robert.MySaarBahn.station_name", startStation.name);
	        mapView.putExtra("org.kolatzek.robert.MySaarBahn.station_new", true);
        }
	}
    
    private void calcRoadmap(Station station)
    {
    	selectedStation = station;
    	//Log.d("station", station.name);
    	//new Toaster(this, getString(R.string.calculating_roadmap), Toast.LENGTH_SHORT);
    	
    	Date d = new Date();
    	Calendar calendar = new GregorianCalendar();
    	Cursor stations = db.query(false, "geopositions", new String[]{"*"}, null, null, null, null, "position ASC", null);
    	String prevStation = "";
    	String nextStation = "";
    	while(stations.moveToNext())
    	{
    		if(station.shortname.equalsIgnoreCase(stations.getString(stations.getColumnIndex("shortname"))))
    		{
    			stations.moveToNext();
    			if( !stations.isAfterLast() )
    			{
    				nextStation = stations.getString(stations.getColumnIndex("shortname"));
    			}
    			break;
    		}
    		else
    		{
    			prevStation = stations.getString(stations.getColumnIndex("shortname"));
    		}
    	}
    	stations.moveToFirst();
    	firstStation = stations.getString(stations.getColumnIndex("name"));
    	stations.moveToLast();
    	lastStation = stations.getString(stations.getColumnIndex("name"));
    	
    	
    	roadmapWidget.removeAllViewsInLayout();
    	tableHeader(lastStation, firstStation);
    	
    	//Log.e("nextStation", nextStation+" ");
    	//Log.e("prevStation", prevStation+" ");
    	calendar.setTime(d);

    	int Weekday = getWeekday(calendar);
    	if(d.getHours() >= 0 && d.getHours() < 3)
    	{
        	calendar.add(Calendar.DAY_OF_YEAR, -1);
        	Weekday = getWeekday(calendar);
    	}
    	String WeekdayCondition = "";
    	if(Weekday > 2)
    	{
    		WeekdayCondition = "="+Weekday;
    	}
    	else if(Weekday == FRIDAY)
    	{
    		WeekdayCondition = " IN ("+FRIDAY+", "+WEEKDAY+")";
    	}
    	else if(Weekday == WEEKDAY)
    	{
    		WeekdayCondition = "="+Weekday;
    	}
    	//Log.i("date" , (d.getHours()*100)+d.getMinutes()+"");
    	
    	String destination = "";
    	if(selectedDestination != null)
    	{
    		destination = " AND ("+selectedDestination+" >= 0) ";
    	}
    	
    	String lt_gt = "";
    	String gt_lt = "";
    	if(nextStation != null &&  !nextStation.equalsIgnoreCase(""))
    	{
			if(d.getHours() >= 1)
			{
				lt_gt = station.shortname+" < "+nextStation;
	    		gt_lt = station.shortname+" > "+nextStation;
			}
			else
			{
				lt_gt = station.shortname+"-2360 < "+nextStation;
	    		gt_lt = station.shortname+" > "+nextStation+"-2360";
			}
    	}
		if(prevStation != null &&  !prevStation.equalsIgnoreCase(""))
		{
			if(d.getHours() >= 1)
	    	{
	    		lt_gt = station.shortname+" > "+prevStation;
				gt_lt = station.shortname+" < "+prevStation;
	    	}
	    	else
	    	{
	    		lt_gt = station.shortname+" > "+prevStation+"-2360";
				gt_lt = station.shortname+"-2360 < "+prevStation;
	    	}
		}
    	String fwSQL = "SELECT * FROM Roadmap WHERE ("+station.shortname+" >= "+((d.getHours()*100)+d.getMinutes())+")"+
						" AND ("+lt_gt+") AND type "+WeekdayCondition+ destination+" ORDER BY "+station.shortname+" ASC";
    	Cursor roadmapForw = db.rawQuery(fwSQL, null);
    	
    	String backSQL = "SELECT * FROM Roadmap WHERE ("+station.shortname+" >= "+((d.getHours()*100)+d.getMinutes())+")"+
							" AND ("+gt_lt+") AND type "+WeekdayCondition+ destination+" ORDER BY "+station.shortname+" ASC";
    	Cursor roadmapBack = db.rawQuery(backSQL, null);
		
    	//Log.i("fw sql", fwSQL);
    	//Log.i("back sql", backSQL);
		stations.moveToFirst();
		
		/*while(roadmapForw.moveToNext())
		{
			String Plan = ""; 
			for( int i = 0; i<roadmapForw.getColumnCount(); i++)
			{
				Plan = Plan+" "+roadmapForw.getColumnName(i)+"="+roadmapForw.getString(i);
			}
			Log.e("Plan", Plan);
		}*/
		
		int startTimeFw = -1;
		int startTimeBck = -1;
		do
		{
			//Log.i("mystation",stations.getString(stations.getColumnIndex("shortname")));
			int i = stations.getPosition()+1;
			int fwTime = 0; int bckTime = 0;
			if(roadmapForw.getCount() > 0)
			{
				roadmapForw.moveToFirst() ;
				//Log.e("frorw", roadmapForw.getString(i));
				fwTime = roadmapForw.getInt(i);
			}
			else
			{
				fwTime = -1;
			}
			if(roadmapBack.getCount() > 0)
			{
				roadmapBack.moveToFirst();
				//Log.e("back", roadmapBack.getString(i));
				bckTime = roadmapBack.getInt(i);
			}
			else
			{
				bckTime = -1;
			}
			roadmapWidget.addView(newStation(stations.getString(stations.getColumnIndex("name")), fwTime , bckTime));
			if(selectedStation.shortname.equalsIgnoreCase(stations.getString(stations.getColumnIndex("shortname"))))
			{
				startTimeFw = fwTime;
				startTimeBck = bckTime;
			}
		} while(stations.moveToNext());
		
    	String fwtime = (startTimeFw-(startTimeFw%100))/100+":"+String.format("%02d", (startTimeFw%100));
    	if(startTimeFw < 0) {fwtime = getString(R.string.no_connection);}
    	String bktime = (startTimeBck-(startTimeBck%100))/100+":"+String.format("%02d", (startTimeBck%100));
    	if(startTimeBck < 0) {bktime = getString(R.string.no_connection);}
    	((TextView) findViewById(R.id.nextConnections)).setText(
				getString(R.string.next_conections_from, selectedStation.name
				));
    	if(selectedDestination != null)
    	{
    		stations.moveToFirst();
    		do
			{
    			if(stations.getString(stations.getColumnIndex("shortname")).equalsIgnoreCase(selectedDestination))
    			{
    	    		((TextView) findViewById(R.id.conn_forward)).setText(
    	    				getString(R.string.selected_station_time, stations.getString(stations.getColumnIndex("name")), bktime
    	    				));
    	    		((TextView) findViewById(R.id.conn_backward)).setText(
    	    				getString(R.string.from_selected_station_time, stations.getString(stations.getColumnIndex("name")), fwtime
    	    				));
    	    		break;
    			}
			}
    		while(stations.moveToNext());

    		
    	}
    	else
    	{
    		((TextView) findViewById(R.id.conn_forward)).setText(
    				getString(R.string.destination_time, firstStation,bktime
    				));
    		((TextView) findViewById(R.id.conn_backward)).setText(
    				getString(R.string.destination_time, lastStation, fwtime
    				));
    	}
		
		stations.close(); roadmapBack.close(); roadmapForw.close();
		tableHeader(lastStation, firstStation);
		selectedDestination = null;
		autoLocation = false;
    }
    
    private void tableHeader(String start, String stop)
    {
		TableRow tr = new TableRow(this);
    	tr.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    	LinearLayout ll = new LinearLayout(this);
    	{
	    	TextView n = new TextView(this); 
	    	n.setText(R.string.direction); n.setGravity(Gravity.LEFT); n.setPadding(5, 5, 15, 5); n.setTextColor(Color.GREEN);
	    	ll.addView(n);
    	}
	    tr.addView(ll, 0);
    	TextView n2 = new TextView(this); 
    	n2.setText(start.substring(0,5)+".");
    	n2.setGravity(Gravity.RIGHT);
    	n2.setTextColor(Color.GREEN);
    	n2.setPadding(0, 5, 10, 5);
    	tr.addView(n2, 1);
    	TextView n3 = new TextView(this); 
    	n3.setText(stop.substring(0,5)+"."); 
    	n3.setGravity(Gravity.RIGHT); 
    	n3.setTextColor(Color.GREEN);
    	n3.setPadding(0, 5, 5, 5);
    	tr.addView(n3, 2);
    	tr.setVisibility(TableRow.VISIBLE);
    	roadmapWidget.addView(tr);  
    }
    
    private TableRow newStation(String n, int forward, int backward)
    {
    	//Log.i("Station", n+", "+forward+", "+backward);
    	TextView ch = new TextView(this);
    	ch.setText(n);
		ch.setOnClickListener(destinationChangeListener);
    	ch.setGravity(Gravity.LEFT);
    	ch.setPadding(5, 5, 15, 5);
    	if(n.equalsIgnoreCase(selectedStation.name))
    	{
    		ch.setHint(R.string.start_station);
    		ch.setTextColor(Color.YELLOW);
    	}
    	TextView ch2 = new TextView(this);
    	if(forward > -1)
    	{
    		ch2.setText( (forward-(forward%100))/100+":"+String.format("%02d", (forward%100)) );
    	}
    	else
    	{
    		ch2.setText(R.string.no_connection);
    	}
    	ch2.setGravity(Gravity.RIGHT);
    	ch2.setPadding(0, 5, 10, 5);
    	if(n.equalsIgnoreCase(selectedStation.name))
    	{
    		ch2.setTextColor(Color.YELLOW);
    	}
    	TextView ch3 = new TextView(this);
    	if(backward > -1)
    	{
    		ch3.setText( (backward-(backward%100))/100+":"+String.format("%02d", (backward%100)) );
    	}
    	else
    	{
    		ch3.setText(R.string.no_connection);
    	}
    	ch3.setGravity(Gravity.RIGHT);
    	ch3.setPadding(0, 5, 5, 5);
    	if(n.equalsIgnoreCase(selectedStation.name))
    	{
    		ch3.setTextColor(Color.YELLOW);
    	}
    	TableRow tr = new TableRow(this);
    	tr.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT)); 
    	tr.addView(ch, 0);
    	tr.addView(ch2, 1);
    	tr.addView(ch3, 2);
    	tr.setVisibility(TableRow.VISIBLE);
    	return tr;
    }
	
	public int getWeekday(Calendar calendar)
	{
    	int Weekday = -1;
    	if(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
    	{
    		Log.e("day", "Sonntag");
    		Weekday = SUNDAY;
    	}
    	else if(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
    	{
    		Log.e("day", "Samstag");
    		Weekday = SATURDAY;
    	}
    	else if(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
    	{
    		Weekday = FRIDAY;
    		Log.e("day", "Freitag");
    	}
    	else
    	{
    		Weekday = WEEKDAY;
    		Log.e("day", "Wochentag");
    	}
    	return Weekday;
	}
	
	public boolean checkVersion( String myversion )
	{
    	try
        {
	        URL file = new URL("http://spreadsheets.google.com/pub?key=tP4mNamPMLcs8K9PH5dJGWw&single=true&gid=0&output=csv");
	    	BufferedReader in = new BufferedReader(new InputStreamReader(file.openStream()));
	    	String inputLine;
	    	boolean uptodate = false;
	    	while ((inputLine = in.readLine()) != null)
	    	{
	    		String[] line = inputLine.split(",");
	    		if(line[0].equalsIgnoreCase("version") && line[1].equalsIgnoreCase(myversion))
	    		{
	    			return true;
	    		}
	    		else if(line[0].equalsIgnoreCase("version") && !line[1].equalsIgnoreCase(myversion))
	    		{
	    			uptodate = false;
	    		}
	    		else if(line[0].equalsIgnoreCase("date"))
	    		{
	    			data_date = line[1];
	    		}
	    		else if(line[0].equalsIgnoreCase("sheetsURL"))
	    		{
	    			data_sheets_url = line[1];
	    		}
	    		Log.w("line", line[0]+"-"+line[1]);
	    	}
	        in.close();
            check_date = (int) ((new Date()).getTime() * 0.001);
	        return uptodate;
        }
        catch (Exception ex) 
        { Log.e("installRoadmapError", this.getString(R.string.can_not_open_data, "http://spreadsheets.google.com/pub?key=tP4mNamPMLcs8K9PH5dJGWw&single=true&gid=0&output=csv"));}
        return false;
	}

	public void run() {
	}
}