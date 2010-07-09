package org.MySaarBahn.Listener;

import org.MySaarBahn.OSMView;

import android.location.Location;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class FitInOnClickListener implements OnClickListener{
	private OSMView mymap;
	private Location myactualLocation;
	private String myname = "me";
	public void setMap(OSMView map)
	{
		mymap = map;
	}
	public void setActualLocation(Location actualLocation)
	{
		myactualLocation = actualLocation;
	}
	public void setMyName(String name)
	{
		myname = name;
	}
	public void onClick(View v)
	{
		try
		{
			mymap.removeMe();
			mymap.setMe(myactualLocation.getLatitude(), myactualLocation.getLongitude(), myname);
			mymap.fitOn(myactualLocation.getLatitude(), myactualLocation.getLongitude());
			mymap.refresh();
		}
		catch (Exception ex)
		{
			Log.d("no mymap", ex.getMessage());
		}
	}
}
