/**
 * Toast as Runnable
 * @author Umberto
 * Under GPLv3 license
 */
package org.kolatzek.robert.MySaarBahn;

import android.content.Context;
import android.widget.Toast;

public class Toaster implements Runnable 
{
	Context context;
	String text = null;
	int duration;
	int resource = 0;
	public Toaster(Context mycontext, String mytext, int myduration) {
		context = mycontext;
		text = mytext;
		duration = myduration;
	}
	
	public Toaster(Context mycontext, int myresource, int myduration) {
		context = mycontext;
		resource = myresource;
		duration = myduration;
	}

	public void run() {
		
		if(text != null)
		{
			Toast.makeText(context, text, duration).show();
		}
		else
		{
			Toast.makeText(context, resource, duration).show();
		}
	}
}
