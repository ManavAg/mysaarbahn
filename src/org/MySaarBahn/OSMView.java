/**
 * OpenStreetMap based map (as view).
 * This is based on Ericssons mobile maps https://labs.ericsson.com/apis/mobile-maps/
 * which is free only for non-commercial usage!
 * @author Umberto
 * Under GPLv3 license
 */
package org.MySaarBahn;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.LinearLayout;

import com.ericsson.mmaps.CustomLayer;
import com.ericsson.mmaps.CustomSymbol;
import com.ericsson.mmaps.GeoMap;
import com.ericsson.mmaps.MapComponent;
import com.ericsson.mmaps.MapFactory;
import com.ericsson.mmaps.MapStyle;
import com.ericsson.mmaps.MapView;
import com.ericsson.mmaps.tools.KeyNavigationTool;
import com.ericsson.mmaps.tools.ScaleBarTool;

import  org.MySaarBahn.*;
/**
 * @author neo
 *
 */
public class OSMView extends GeoMap 
{
	private static String KEY = "BXKOm4sZ5c8II8854LEpaezYWpOho3f3FlRPb2nF";
	private MapView mapView_ = null;
	Drawable trainIco;
	Drawable meIco;
	CustomLayer l;
	Station actualStation = null;
	String trainIconURL = "http://mysaarbahn.googlecode.com/files/train.png";
	String meIconURL = "http://mysaarbahn.googlecode.com/files/me.png";
	CustomSymbol train = null;
	CustomSymbol me = null;
	private double factor = 0.0004;
	private double actual_factor = 0.0004;

	public OSMView(Context context, LinearLayout osmview) 
	{
		MapFactory factory = MapFactory.getInstance();
		 
		MapStyle style = new MapStyle();
		style.set(MapStyle.MAP_SOURCE, MapStyle.OPEN_STREET_MAP);

		try 
		{
			mapView_ = factory.createMapView(context, KEY, style);
			sbTouchNavigationTool touchController = new sbTouchNavigationTool(context, mapView_, true, this);
			touchController.activate();
			mapView_.getMapComponent().addTool(new ScaleBarTool(mapView_.getMapComponent()));
			osmview.addView(mapView_);
			mapView_.getMap().setGeoHeight(1);
			
			// Add a tool for panning and zooming 
			MapComponent mapComp = mapView_.getMapComponent();
			KeyNavigationTool keynav = new KeyNavigationTool(mapComp, true);
			keynav.activate();
			mapComp.addTool(keynav);
			ScaleBarTool scalenav = new ScaleBarTool(mapComp);
			mapComp.addTool(scalenav);
			
			l = new CustomLayer();
			if(l.isVisible())
			{
				Log.d("isVisible", "visible");
			}
			mapView_.getMap().addLayer(l);

			if(l.isActive())
			{
				//Log.d("l", "aktiv");
			}
			//mapView_.getMapComponent().addTool(new CoordinateTool(mapView_.getMapComponent()));
			this.setMaxScale(100000);
			this.setMinScale(1);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	public void setStation(Station s)
	{
		train = new CustomSymbol(mapView_.getMap().internalValue(s.logitude), mapView_.getMap().internalValue(s.latitude), trainIconURL);
		//Log.d("xInternal", mapView_.getMap().internalValue(s.logitude)+"");
		//Log.d("yInternal", mapView_.getMap().internalValue(s.latitude)+"");
		train.setEnableShadow(true);
		train.setText(s.name);
		train.setTextColor(Color.RED);
		mapView_.getMap().setGeoHeight(factor);
		mapView_.getMap().setGeoCenterX(s.logitude);
		mapView_.getMap().setGeoCenterY(s.latitude);
		l.add(train);
		actualStation = s;
	}
	
	public void removeStation()
	{
		l.remove(train);
	}
	
	public void setMe(double latitude, double logitude, String name)
	{
		me = new CustomSymbol(mapView_.getMap().internalValue(logitude), mapView_.getMap().internalValue(latitude), meIconURL);
		//Log.d("xInternal", mapView_.getMap().internalValue(actualStation.logitude)+"");
		//Log.d("yInternal", mapView_.getMap().internalValue(actualStation.latitude)+"");
		me.setEnableShadow(true);
		me.setText(name);
		me.setTextColor(Color.RED);
		l.add(me);
		mapView_.getMap().setGeoCenterX(actualStation.logitude);
		mapView_.getMap().setGeoCenterY(actualStation.latitude);
		mapView_.getMap().setGeoHeight(factor);
	}
	
	public void removeMe()
	{
		l.remove(me);
	}

	public void refresh() 
	{
		mapView_.destroyDrawingCache();
		mapView_.refresh();
	}
	
	public void fitOn(double latitude, double logitude)
	{
		mapView_.getMap().setGeoHeight(factor);
		double[] a = new double[2];
		double[] b = new double[2];
		mapView_.getMap().getGeoBounds(a,b);
		//Log.d("a", "0: "+a[0]+" 1 "+a[1]);
		//Log.d("b", "0: "+b[0]+" 1 "+b[1]);
		//Log.d("lat", actualStation.latitude+"");
		//Log.d("log", actualStation.logitude+"");
		actual_factor = factor;
		while(!(
				(logitude < a[0] && latitude < a[1] && logitude > b[0] && latitude > b[1])
				||
				(logitude > a[0] && latitude > a[1] && logitude < b[0] && latitude < b[1]))
				)
		{
			actual_factor = actual_factor*2;
			mapView_.getMap().setGeoHeight(actual_factor);
			mapView_.getMap().getGeoBounds(a,b);
			//Log.d("a", "0: "+a[0]+" 1 "+a[1]);
			//Log.d("b", "0: "+b[0]+" 1 "+b[1]);
			//Log.d("lat", actualStation.latitude+"");
			//Log.d("log", actualStation.logitude+"");
		}
	}
	
	public void zoomIn()
	{
		mapView_.getMap().setGeoHeight((actual_factor/2.0));
		actual_factor = actual_factor/2;
		this.refresh();
	}
	
	public void zoomOut()
	{
		mapView_.getMap().setGeoHeight((actual_factor*2.0));
		actual_factor = actual_factor*2;
		this.refresh();
	}
}
