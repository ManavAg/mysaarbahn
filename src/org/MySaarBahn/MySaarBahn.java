/**
 * Main file/activity of mySaarBahn
 * @author Umberto
 * Under GPLv3 license
 */
package org.MySaarBahn;

import android.app.Dialog;
import android.app.TabActivity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TabHost.TabSpec;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Date;

import org.MySaarBahn.Listener.FitInOnClickListener;
import org.MySaarBahn.Listener.OnDestinationChangeListener;
import org.MySaarBahn.Listener.OnDoInstallClickListener;
import org.MySaarBahn.Listener.OnSaarbahnStationsItemClick;
import org.MySaarBahn.Listener.ZoomInOnClickListener;
import org.MySaarBahn.Listener.ZoomOutOnClickListener;
import org.MySaarBahn.Listener.exactTimesOnClickListener;

public class MySaarBahn extends TabActivity  implements LocationListener, Runnable {
	public LocationManager lm = null;
	public SQLiteDatabase db = null;
	public StationsData sd = null;
	/** Date-time constants **/
	static int WEEKDAY = 0;
	static int FRIDAY = 1;
	static int SATURDAY = 2;
	static int SUNDAY = 3;
	/** layout **/
	public TextView tv = null;
	private TabHost mTabHost = null;
	private TableLayout roadmapWidget = null;
	public Spinner otherStation = null;
	public String selectedDestination = null;
	public Station selectedStation= null;
	private TabSpec positionTab = null;
	private TabSpec roadmapTab = null;
	private TabSpec mapTab = null;
	/** data versions **/
	public Boolean dataInstalled = false;
	public String data_date = "";
	public String data_sheets_url = "";
	public String data_version = "";
	public int check_date = 0;
	static int update_interval = 7*24*60*60;
	public Dialog installDialog;
	/** calculating position **/
	private String firstStation;
	private String lastStation;
	public boolean autoLocation = true;
	/** Threads **/
	private Thread myThread = null;
	private Handler myHandler = null;
	/** OpenStreetMap object **/
	protected OSMView map = null;
	/** URL to roadmap on Saarbahn server **/
	private String exactURL;
	protected Location actualLocation = null;
	/** Listener **/
	private ZoomInOnClickListener zoomInListener = new ZoomInOnClickListener();
	private ZoomOutOnClickListener zoomOutListener = new ZoomOutOnClickListener();
	private FitInOnClickListener fitInListener = new FitInOnClickListener();
	private exactTimesOnClickListener exactTimesInBrowser = new exactTimesOnClickListener();
	private OnDestinationChangeListener destinationChangeListener = new OnDestinationChangeListener();
	private OnSaarbahnStationsItemClick onSaarbahnStationsItemClick = new OnSaarbahnStationsItemClick();
	private OnDoInstallClickListener doInstallListener = new OnDoInstallClickListener();

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
        exactTimesInBrowser.setParent(this);
        destinationChangeListener.setParent(this);
        onSaarbahnStationsItemClick.setParent(this);
        doInstallListener.setParent(this);
        
        Log.d("tabhost", "get TabHost");
        mTabHost = getTabHost();
    	
        positionTab = mTabHost.newTabSpec("position").setIndicator(getString(R.string.Position)).setContent(R.id.position);

        mTabHost.addTab(positionTab);
        roadmapTab = mTabHost.newTabSpec("roadmapScrollView").setIndicator(getString(R.string.Roadmap)).setContent(R.id.roadmap);
        mTabHost.addTab(roadmapTab);
        Log.d("make mapTab", "start");
        mapTab = mTabHost.newTabSpec("OSMview").setIndicator(getString(R.string.Map)).setContent(R.id.OSMviewParent);
        Log.d("make map", "start");
        mTabHost.addTab(mapTab);
        
        map = new OSMView(this.getBaseContext(), (LinearLayout) findViewById(R.id.OSMview));
        Log.d("map", "add map");
        
        Button zoomInButton = (Button) findViewById(R.id.zInButton);
        zoomInButton.setOnClickListener(zoomInListener);
        zoomInListener.setMap(map);

        Button zoomOutButton = (Button) findViewById(R.id.zOutButton);
        zoomOutButton.setOnClickListener(zoomOutListener);
        zoomOutListener.setMap(map);

        Button zoomCenterButton = (Button) findViewById(R.id.zCenterButton);
        zoomCenterButton.setOnClickListener(fitInListener);
        fitInListener.setMap(map);
        fitInListener.setMyName(getString(R.string.me));
        fitInListener.setActualLocation(actualLocation);
        
        tv = (TextView) findViewById(R.id.actualPosTextView);
        sd = new StationsData();
		roadmapWidget = (TableLayout) findViewById(R.id.roadmap);     

        db = openOrCreateDatabase("mysaarbahnDB", MODE_PRIVATE, null);
        
    	int now =  (int) ((new Date()).getTime() * 0.001);
        try
        {
        	SharedPreferences prefs = getSharedPreferences("mySaarBahn.prefs", MODE_PRIVATE);
        	data_version = prefs.getString("data_version", "0");
        	check_date = prefs.getInt("last_update", 0);
        	if (now < (check_date+update_interval) )
        	{
        		dataInstalled = true;
        	}
        	else if( checkVersion(data_version) == true)
        	{
        		check_date = now;
        		dataInstalled = true;
        		Log.i("actual", "true");
        	}
        	else
        	{
        		dataInstalled = false;
        		Log.i("actual", "false");
        	}
        }
        catch (Exception e)
        {
        	Log.i("get preferences", "first start, no prefs");
        }
        
        if(!(db.rawQuery("SELECT name FROM sqlite_master WHERE name='geopositions'", null).getCount() > 0 
    		||
    		db.rawQuery("SELECT name FROM sqlite_master WHERE name='Roadmap'", null).getCount() > 0
    		))
    	{
    		dataInstalled = false;
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
    		myinit();
    	}
    }
    
	/**
	 * On stop store all data about update / actuality
	 */
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
    
	/** 
	 * Initialize Layout, set data, calculate - only if data installed 
	 */
    public void myinit()
    {
		
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
        otherStation.setOnItemSelectedListener(onSaarbahnStationsItemClick);
        
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
		String provider = "";
		if(LBS_list.size() == 0)
		{
			tv.setText(R.string.No_location_service_active);
			myHandler.post(new Toaster(this, R.string.No_location_service_active, Toast.LENGTH_LONG));
		}
		else
		{
			String bestProvider = lm.getBestProvider(crit, false);
			if( lm.isProviderEnabled(bestProvider) ) //When best provider active
			{
				provider = bestProvider;
				lm.requestLocationUpdates(provider, 5000, 15, this);
			}
			else if( lm.isProviderEnabled((String)LBS_list.get(0)) && bestProvider !=  (String)LBS_list.get(0)) //When bestprovider != the first, but first is enabled
			{
				provider = (String) LBS_list.get(0);
				lm.requestLocationUpdates(provider, 5000, 15, this);
			}

			else if( LBS_list.size() > 1 && lm.isProviderEnabled((String)LBS_list.get(1)) && bestProvider !=  (String)LBS_list.get(1))//When bestprovider != the second, but second is enabled
			{
				provider = (String) LBS_list.get(1);
				lm.requestLocationUpdates(provider, 5000, 15, this);
			}
			else
			{
				myHandler.post(new Toaster(this, bestLM +" "+ getString(R.string.Please_activate_PROVIDER, bestProvider), 3000));
			}
		}
		
		try
		{
			Location last = lm.getLastKnownLocation(provider);
			if(last == null)
			{
				tv.setText(R.string.NoOldPositionFound);
			}
			else
			{
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
			myHandler.post(new Toaster(this, e.toString() + provider, Toast.LENGTH_LONG));
		}
		catch (IllegalArgumentException e)
		{
			myHandler.post(new Toaster(this, e.toString() + provider, Toast.LENGTH_LONG));
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
		lm.removeUpdates(this);
		Toast.makeText(this, R.string.No_location_service_active_no_help, 5000).show();
	}

	public void onProviderEnabled(String provider) {
		lm.requestLocationUpdates(provider, 10000, 25, this);
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

    /**
     * Calculate data for new location
     * @param location
     */
	public void calcNewLocation(Location location)
	{
		actualLocation = location;
        fitInListener.setActualLocation(actualLocation);
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
	}
    
	/**
	 * calculate roadmap
	 * @param station seleted station
	 */
    public void calcRoadmap(Station station)
    {
    	Toast.makeText(this, getString(R.string.calculating_roadmap), Toast.LENGTH_SHORT);
    	if(selectedStation != station)
    	{
    		map.removeStation();
        	map.setStation(station);
    	}
    	if(actualLocation != null)
    	{
	        map.removeMe();
	        map.setMe(actualLocation.getLatitude(), actualLocation.getLongitude(), getString(R.string.me));
	        map.fitOn(actualLocation.getLatitude(), actualLocation.getLongitude());
	        map.refresh();
    	}
    	else
    	{
        	map.fitOn(station.latitude, station.logitude);
    	}
    	selectedStation = station;
    	//Log.d("station", station.name);
    	
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
        	d = new Date(calendar.get(Calendar.YEAR) - 1900, calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
    	}
    	if(
    			CatholicCalendar.isAllSaintsDay(d) || CatholicCalendar.isAscension(d) || CatholicCalendar.isChristmasDay(d) || 
    			CatholicCalendar.isCorpusChristi(d) || CatholicCalendar.isEasterMonday(d) || 
    			CatholicCalendar.isGoodFriday(d) || CatholicCalendar.isNewYearsDay(d) || CatholicCalendar.isPentecostMontag(d) ||
    			CatholicCalendar.isStStephansDay(d))
    	{
    		Weekday = SUNDAY;
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
		
		stations.moveToFirst();
		
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
    	/*http://lupe.tec-saar.de/smartinfo/service/jsp/?olifServerId=68&autorefresh=0&default_autorefresh=60&routeId=68%2F1&stopId=Hellwigstra%C3%9Fe&optDir=-1&optTime=now&time=&nRows=8*/
    	// %2F = /
		exactURL = "http://lupe.tec-saar.de/smartinfo/service/jsp/mobile.jsp?olifServerId=68&autorefresh=0&default_autorefresh=60&routeId=68%2F1&stopId="+
		sd.getSaarbahnLatin1(selectedStation.name)+"&optDir=-1&optTime=now&time=&nRows=8";
		if(exactURL != null)
    	{
    		((TextView) findViewById(R.id.exactTimes)).setVisibility(View.VISIBLE);
    		((TextView) findViewById(R.id.exactTimes)).setTextColor(Color.rgb(0, 140, 37));
    		((TextView) findViewById(R.id.exactTimes)).setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
    		((TextView) findViewById(R.id.exactTimes)).setText(R.string.here_are_exact_times);
    		((TextView) findViewById(R.id.exactTimes)).setOnClickListener(exactTimesInBrowser);
    		exactTimesInBrowser.setURL(exactURL);
    	}
    	else
    	{
    		((TextView) findViewById(R.id.exactTimes)).setVisibility(View.INVISIBLE);
    	}
    	
		stations.close(); roadmapBack.close(); roadmapForw.close();
		tableHeader(lastStation, firstStation);
		selectedDestination = null;
		autoLocation = false;
    }
    
    /**
     * Create table header
     * @param start - starting station
     * @param stop - ending station
     */
    private void tableHeader(String start, String stop)
    {
		TableRow tr = new TableRow(this);
    	tr.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    	LinearLayout ll = new LinearLayout(this);
    	{
	    	TextView n = new TextView(this); 
	    	n.setText(R.string.direction); 
	    	n.setGravity(Gravity.LEFT); 
	    	n.setPadding(5, 5, 15, 5); 
	    	n.setTextColor(Color.GREEN);
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
    
    /**
     * create a table row for a roadmap
     * @param name station name
     * @param forward arriving time: start to stop (from Riegelsberg)
     * @param backward arriving time: stop to start (the other way - to Riegelsberg)
     * @return
     */
    private TableRow newStation(String name, int forward, int backward)
    {
    	TextView ch = new TextView(this);
    	ch.setText(name);
		ch.setOnClickListener(destinationChangeListener);
    	ch.setGravity(Gravity.LEFT);
    	ch.setPadding(5, 5, 15, 5);
    	if(name.equalsIgnoreCase(selectedStation.name))
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
    	if(name.equalsIgnoreCase(selectedStation.name))
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
    	if(name.equalsIgnoreCase(selectedStation.name))
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
	
    /**
     * get day of week - importand for roadmap: weekday, friday, saturday or sunday?
     * @param calendar (today)
     * @return
     */
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
	
	/**
	 * check version of data (an update needed?)
	 * @param myversion string with actual version
	 * @return
	 */
	public boolean checkVersion( String myversion )
	{
    	try
        {
	        URL file = new URL("http://spreadsheets.google.com/pub?key=tP4mNamPMLcs8K9PH5dJGWw&single=true&gid=0&output=csv");
	    	BufferedReader in = new BufferedReader(new InputStreamReader(file.openStream()));
	    	String inputLine;
            check_date = (int) ((new Date()).getTime() * 0.001);
	    	while ((inputLine = in.readLine()) != null)
	    	{
	    		String[] line = inputLine.split(",");
	    		if(line[0].equalsIgnoreCase("version") && line[1].equalsIgnoreCase(myversion))
	    		{
	    			return true;
	    		}
	    		else if(line[0].equalsIgnoreCase("date"))
	    		{
	    			data_date = line[1];
	    		}
	    		else if(line[0].equalsIgnoreCase("sheetsURL"))
	    		{
	    			data_sheets_url = line[1];
	    		}
	    		Log.i("line", line[0]+"-"+line[1]);
	    	}
	        in.close();
	        return false;
        }
        catch (Exception ex) 
        { Log.e("installRoadmapError", this.getString(R.string.can_not_open_data, "http://spreadsheets.google.com/pub?key=tP4mNamPMLcs8K9PH5dJGWw&single=true&gid=0&output=csv"));}
        return false;
	}

	public void run() {
	}

}