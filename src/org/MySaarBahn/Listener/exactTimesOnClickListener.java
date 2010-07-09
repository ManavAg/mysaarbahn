package org.MySaarBahn.Listener;

import org.MySaarBahn.MySaarBahn;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class exactTimesOnClickListener implements OnClickListener {
	private String myexactURL = "";
	private MySaarBahn parent;
	public void setParent(MySaarBahn p)
	{
		parent = p;
	}
	public void setURL(String exactURL)
	{
		Log.d("exactURL", exactURL);
		myexactURL = exactURL;
	}
	public void onClick(View v)
	{
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(myexactURL));
		try
		{parent.startActivity(i);}
		catch (Exception ex)
		{
			Log.d("startActivity exception", ex.toString());
			Log.d("i", i.toString());
		}
	}

}
