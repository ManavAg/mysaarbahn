package org.MySaarBahn.Listener;

import org.MySaarBahn.MySaarBahn;
import org.MySaarBahn.Station;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class OnDestinationChangeListener implements OnClickListener {
	private MySaarBahn parent;
	public void setParent(MySaarBahn p)
	{
		parent = p;
	}
	public void onClick(View v)
	{
		//Log.i("destinationChangeListener", "clicked: "+((TextView) v).getText().toString());
		Station s = parent.sd.getStationByName(((TextView) v).getText().toString());
		parent.selectedDestination = s.shortname;
    	if(parent.dataInstalled == false)
    	{
    		return; //Do nothing if data are not installed
    	}
		parent.calcRoadmap(parent.selectedStation);
	}

}
