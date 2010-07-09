package org.MySaarBahn.Listener;

import java.math.BigDecimal;
import java.util.HashMap;

import org.MySaarBahn.MySaarBahn;
import org.MySaarBahn.R;
import org.MySaarBahn.Station;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class OnSaarbahnStationsItemClick implements OnItemSelectedListener {
	private MySaarBahn parent;
	public void setParent(MySaarBahn p)
	{
		parent = p;
	}
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
		//Log.i("showDestinationStations", "clicked");
		if(parent.autoLocation == false)
		{
			parent.otherStation = (Spinner) parent.findViewById(R.id.otherStation);
    		String s = parent.otherStation.getSelectedItem().toString();
    		HashMap<String, Station> orte = parent.sd.getStations();
    		Station station = orte.get(s);
    		parent.tv.setText(parent.getString(R.string.to_selected_station, station.name, (new BigDecimal(station.distance)).setScale( 2, BigDecimal.ROUND_HALF_UP )));
    		parent.calcRoadmap(station);
    		//Log.d("s", station.name);
		}
		parent.autoLocation = false;
	}

	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
	}

}
