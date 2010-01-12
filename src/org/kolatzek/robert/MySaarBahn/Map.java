package org.kolatzek.robert.MySaarBahn;

import java.util.HashMap;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.MapController;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ZoomControls;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.kolatzek.robert.MySaarBahn.*;

public class Map extends MapActivity {

	public Drawable icon;
	public String station_short;
	public MapView mv;
	public MyLocationOverlay loc_overl;
	public WayOverlay way;
	List<Overlay> mapOverlays;
	
     public void onCreate(Bundle savedInstanceState) 
     {
    	 super.onCreate(savedInstanceState);
    	 setContentView(R.layout.mapview);
    	 
    	 mv = new MapView(this, "0FVFyEwkP5TWtThz8CHPphO3OSuHNJiFIdEXynQ");

    	 mapOverlays = mv.getOverlays();
    	 mapOverlays.clear();
    	 
    	 loc_overl = new MyLocationOverlay(this, mv); 
    	 loc_overl.enableMyLocation();
    	 loc_overl.enableCompass();
    	 mapOverlays.add(loc_overl);

         icon = this.getResources().getDrawable(R.drawable.train);
         icon.setBounds(0,0,icon.getIntrinsicWidth(), icon.getIntrinsicWidth());

    	 mv.getController().setZoom(16);
    	 View zv = mv.getZoomControls();
    	 zv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    	 mv.addView(zv);
         mv.setBuiltInZoomControls(true);
    	 mv.displayZoomControls(true);
    	 mv.setClickable(true);
    	 mv.setEnabled(true);
    	 mv.setSatellite(false);
         
         /*if(getIntent().getExtras() != null)
         {
        	 Log.i( "extras", ": "+getIntent().getExtras().getString("org.kolatzek.robert.MySaarBahn.station_name"));
        	 String station = getIntent().getExtras().getString("org.kolatzek.robert.MySaarBahn.station_name");
        	 setStation(station);
        	 if(getIntent().getExtras().getBoolean("org.kolatzek.robert.MySaarBahn.station_new") == true && station != null)
        	 {
            	 mv.invalidate();
        	 }
        	 else if(getIntent().getExtras().getBoolean("org.kolatzek.robert.MySaarBahn.station_new") == true && station == null)
        	 {
        		 Log.e("booleanExtra", "no station");
        	 }
         }
         else
         {
        	 Location p = loc_overl.getLastFix();
        	 if(p != null)
        	 {
        		 mv.getController().animateTo(new GeoPoint((int) (p.getLongitude()*1e6), (int)(p.getLatitude()*1e6)));
        	 }
         }*/
         
         setContentView(mv);
     }
     
     private void setStation(String station)
     {
    	 try
    	 {
        	 way = new WayOverlay(mv, icon);
	    	 Station s = new StationsData().getStationByName(station);
	         GeoPoint point = s.getGeopoint();
	         way.addData(point, s.name, s.distance+"");
	         mv.getController().animateTo( point );
	         mapOverlays.add(way);
    	 }
    	 catch (Exception ex)
    	 {
    		 
    	 }
     }
     
     @Override
     protected boolean isRouteDisplayed() {
          return false;
     }
     
     /*@Override
     public void onResume()
     {
    	 super.onResume();
    	 Log.e("onResume", "true");
     }
    
     @Override
     public void onDestroy() 
     {
    	 super.onDestroy();
    	 Log.e("onDestroy", "true");
     }
     
     @Override
     public void onContentChanged() 
     {
    	 super.onContentChanged();
    	 Log.e("onContentChanged", "true");
     }
     
     @Override
     public void onNewIntent(Intent newIntent)
     {
    	 super.onNewIntent(newIntent);
    	 Log.e("onNewIntent", "true");
     }*/
     
     @Override
     public void onWindowFocusChanged(boolean wert)
     {
    	 super.onWindowFocusChanged(wert);
    	 String echo = wert == true ? "true": "false";
    	 //Log.e("onWindowFocusChanged", echo);
    	 if(wert == false)
    	 {
    		 if(getIntent().getExtras() != null)
             {
            	 //Log.i( "extras", ": "+getIntent().getExtras().getString("org.kolatzek.robert.MySaarBahn.station_name"));
            	 String station = getIntent().getExtras().getString("org.kolatzek.robert.MySaarBahn.station_name");
            	 setStation(station);
            	 if(getIntent().getExtras().getBoolean("org.kolatzek.robert.MySaarBahn.station_new") == true && station != null)
            	 {
                	 mv.invalidate();
                	 Log.e("invalidate()", "true");
            	 }
            	 else if(getIntent().getExtras().getBoolean("org.kolatzek.robert.MySaarBahn.station_new") == true && station == null)
            	 {
            		 Log.e("booleanExtra", "no station");
            	 }
             }
             else
             {
            	 Location p = loc_overl.getLastFix();
            	 if(p != null)
            	 {
            		 mv.getController().animateTo(new GeoPoint((int) (p.getLongitude()*1e6), (int)(p.getLatitude()*1e6)));
            	 }
             }
    	 }
     }
}