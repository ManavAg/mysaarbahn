package org.kolatzek.robert.MySaarBahn;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class WayOverlay extends ItemizedOverlay<OverlayItem>{

	private ArrayList<OverlayItem> overlayItems;
	private MapView mv;
	private Drawable icon;
	
	public WayOverlay(MapView map, Drawable defaultMarker)
	{
		super(defaultMarker);
		overlayItems = new ArrayList<OverlayItem>();
		mv = map;
		icon = defaultMarker;
	}

	public void addData(GeoPoint point, String name, String text)
	{
		OverlayItem oi = new OverlayItem(point, name, text);
		oi.setMarker(icon);
		overlayItems.add(oi);
		super.populate();
	}

	@Override
	protected OverlayItem createItem(int i) 
	{
		return overlayItems.get(i);
	}

	@Override
	public int size() 
	{
		return overlayItems.size();
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) 
	{
		super.draw(canvas, mapView, false);
		boundCenter(icon);
	}
}
