/**
 * extend standart touch navigation tool, to accept long click as zoomIn and a short as a zoomOut
 * @author Umberto
 * Under GPLv3 license
 */
package org.MySaarBahn;


import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

import com.ericsson.mmaps.MapView;
import com.ericsson.mmaps.tools.TouchNavigationTool;

public class sbTouchNavigationTool extends TouchNavigationTool 
{
	private OSMView osmv;
	public sbTouchNavigationTool(Context arg0, MapView arg1, boolean arg2, OSMView arg3) {
		super(arg0, arg1, arg2);
		osmv = arg3;
		Log.d("sbTouchNavigationTool", "created");
	}
	
	/**
	 * @override 
	 */
	public boolean onSingleTapUp(MotionEvent e)
	{
		super.onSingleTapUp(e);
		//osmv.zoomOut();
		return true;
	}
	
	
	/**
	 * @override 
	 */
	public void onLongPress(MotionEvent e)
	{
		super.onLongPress(e);
		//osmv.zoomIn();
	}

}
