package org.kolatzek.robert.MySaarBahn;

import com.google.android.maps.GeoPoint;

import android.location.Location;

public class Station {
	public String name;
	public double latitude;
	public double logitude;
	public String shortname;
	public double distance;
	public long time = 0;
	public int position = 0;
	public Station(String n, double d, double e, String shortname, int position)
	{
		this.name = n;
		this.latitude = d;
		this.logitude = e;
		this.shortname = shortname;
		this.position = position;
	}
	
	public Location getLocation()
	{
		Location l = new Location("gps");
		l.setLongitude(this.logitude);
		l.setLatitude(this.latitude);
		return l;
	}
	
	public GeoPoint getGeopoint()
	{
		return new GeoPoint((int)(this.latitude*1E6), (int)(this.logitude*1E6));
	}
}
