package org.MySaarBahn;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

public class Installer implements Runnable {
		private MySaarBahn parent;
		private String StationsCols = "";
		private ProgressBar progressBar;
		private Handler mHandler = new Handler();
		
		public Installer(MySaarBahn p)
		{
			parent = p;
			progressBar = ((ProgressBar) parent.installDialog.findViewById(R.id.installProgress));
		}
	    /**
	     * Install geopositions and data from StationData or online-csv
	     * @return boolean true 
	     */
		public void run()
		{
	         new Thread(new Runnable() 
	         {
	             public void run() 
	             {
	                if(createTable())
	                {
	                     // Update the progress bar
	                     mHandler.post(new Runnable() {
	                         public void run() {
	                         	progressBar.setProgress(1);
	                         }
	                     });
	                    if(installData())
	         			{
	         				mHandler.post(new Runnable() {
	                             public void run() {
	                             	progressBar.setProgress(5);

                        			if(parent.installDialog != null && parent.installDialog.isShowing())
                        	    	{
                        				parent.installDialog.hide();
                        	    	}
	                        		try
	                        		{
	                        			SharedPreferences prefs = parent.getSharedPreferences("mySaarBahn.prefs", Activity.MODE_PRIVATE);
	                        			SharedPreferences.Editor editor = prefs.edit();
	                        			editor.putString("data_version", parent.data_version);
	                        			editor.putInt("last_update", (int) ((new Date()).getTime() * 0.001));
	                        			editor.commit();
	                        		}
	                        		catch (Exception e)
	                        		{
	                        			Log.e("mySaarBahnSettings", "not saved");
	                        		}
	                        		parent.myinit();
	                             }
	                         });
	         			}
	                 }
	             }
	         }).start();
		}
		
		public boolean createTable()
		{
			parent.db.execSQL("DROP TABLE IF EXISTS geopositions");
			parent.db.execSQL("CREATE TABLE IF NOT EXISTS geopositions (name, latitude, longitude, shortname, position INTEGER, PRIMARY KEY(position DESC))");
			parent.sd = new StationsData();
			
	        List<Station> orte = parent.sd.getBySort();
	        Iterator<Station> i = orte.iterator();
	        while(i.hasNext())
	        {
	        	Station actual = (Station) i.next();
	        	String[] o = {actual.name, actual.latitude+"", actual.logitude+"", actual.shortname, actual.position+""};
	        	StationsCols = StationsCols + ", " + actual.shortname+" INTEGER";
	        	parent.db.execSQL("INSERT OR IGNORE INTO geopositions VALUES (?, ?, ?, ?, ?)", o);
	        }
	        return true;
		}
		
		public boolean installData()
		{	        
	        // Install data
	        parent.db.execSQL("DROP TABLE IF EXISTS Roadmap");
	        parent.db.execSQL("CREATE TABLE IF NOT EXISTS Roadmap (type"+StationsCols+")");
	        boolean installation = true;
	        try
	        {
		        URL file = new URL(parent.data_sheets_url);
		    	BufferedReader in = new BufferedReader(new InputStreamReader(file.openStream()));
		    	String inputLine;
		    	while ((inputLine = in.readLine()) != null && installation != false)
		    	{
		    		String[] line = inputLine.split(",");
		    		if(installRoadmap(Integer.parseInt(line[0]), line[1]))
		    		{
		    			//Log.i("install data", line[0]+": "+line[1]+" installed");
		    			mHandler.post(new Runnable() {
		                    public void run() {
		                    	progressBar.setProgress(progressBar.getProgress()+1);
		                    }
		                });
		    		}
		    		else
		    		{
		    			installation = false;
		    		}
		    	}
		        in.close();
	        }
	        catch (Exception ex) 
	        { 
	        	Log.e("ErrorGettingDataFeed", parent.installDialog.getContext().getString(R.string.can_not_open_data, parent.data_sheets_url));
	        	return false;
	        }
	        
	        if(installation == true)
	        {
	        	parent.dataInstalled = true;
	        }
	        return installation;
		}
		
		/**
		 * Get and install roadmap data per day/type 
		 * @param int type - type of day
		 * @param String url - url of csv with data
		 * @return boolean true if successfull
		 */
		public boolean installRoadmap(int type, String url)
		{
			mHandler.post(new Toaster(parent.getBaseContext(), parent.getBaseContext().getString(R.string.downloading_data), Toast.LENGTH_SHORT));
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
		    		parent.db.execSQL("INSERT OR IGNORE INTO Roadmap VALUES ("+type+stops+")");
		    	}
		        in.close();
		        return true;
	        }
	        catch (Exception ex) 
	        { Log.e("installRoadmapError", parent.installDialog.getContext().getString(R.string.can_not_open_data, url));}
	        return false;
		}

}
