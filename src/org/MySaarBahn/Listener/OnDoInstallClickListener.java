package org.MySaarBahn.Listener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.MySaarBahn.Installer;
import org.MySaarBahn.MySaarBahn;
import org.MySaarBahn.R;
import org.MySaarBahn.Station;
import org.MySaarBahn.StationsData;
import org.MySaarBahn.Toaster;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class OnDoInstallClickListener implements OnClickListener, Runnable {

	private Thread myThread = null;
	private Handler myHandler = null;
	private MySaarBahn parent;
	
	public OnDoInstallClickListener()
	{
        if(myHandler == null)
        {
        	myHandler = new Handler();
        }
        if(myThread == null)
        {
        	myThread = new Thread(this);
        	myThread.start();
        }
	}
	
	public void setParent(MySaarBahn p)
	{
		parent = p;
	}
	
	public void onClick(View v)
	{
		parent.installDialog.setTitle(parent.getString(R.string.installing));
		TextView itv = (TextView) parent.installDialog.findViewById(R.id.installTextView);
		itv.setText(R.string.installHint);
		parent.checkVersion(parent.data_version);
		Button dataInstallButton = (Button) parent.installDialog.findViewById(R.id.installButton);
		dataInstallButton.setVisibility(View.GONE);
		ProgressBar progressBar = (ProgressBar) parent.installDialog.findViewById(R.id.installProgress);
		progressBar.setVisibility(View.VISIBLE);
		
		myHandler.post(new Toaster(parent.installDialog.getContext(), parent.installDialog.getContext().getString(R.string.installing_roadmap), Toast.LENGTH_SHORT));
		Installer inst = new Installer(parent);
    	
		
		//myHandler.post(new Toaster(parent.installDialog.getContext(), parent.installDialog.getContext().getString(R.string.all_roadmap_data_installed), Toast.LENGTH_LONG));
    	
		myHandler.post(inst);
	}
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
