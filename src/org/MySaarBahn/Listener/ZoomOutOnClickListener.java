package org.MySaarBahn.Listener;

import org.MySaarBahn.OSMView;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class ZoomOutOnClickListener implements OnClickListener {
	private OSMView mymap;
	public void setMap(OSMView map)
	{
		mymap = map;
	}
	public void onClick(View v)
	{
		try
		{
			mymap.zoomOut();
		}
		catch (Exception ex)
		{
			Log.d("no mymap", ex.getMessage());
		}
	}

}
