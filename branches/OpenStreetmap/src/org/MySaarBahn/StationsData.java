/**
 * Saarbahn stations data (missing any after Brebach)
 * @author Umberto
 * Under GPLv3 license
 */
package org.MySaarBahn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.MySaarBahn.Station;


public class StationsData {
	HashMap<String, Station> gps;
	HashMap<String, String> SaarbahnLatin1;
	public StationsData()
	{
		gps = new HashMap<String, Station>();
		gps.put("Walpershofen/Etzenhofen", new Station("Walpershofen/Etzenhofen", 49.317809, 6.914497, "wlphf", 1));
		gps.put("Riegelsberg Gisorsstraße", new Station("Riegelsberg Gisorsstraße", 49.314361, 6.932811, "gisstr", 2));
		gps.put("Riegelsberg Güchenbach", new Station("Riegelsberg Güchenbach", 49.310591, 6.939109, "gbach", 3));
		gps.put("Riegelsberghalle", new Station("Riegelsberghalle", 49.308341, 6.940073, "rgbhalle", 4));
		gps.put("Riegelsberg Post", new Station("Riegelsberg Post", 49.307047, 6.944958, "rgbpost", 5));
		gps.put("Riegelsberg Rathaus", new Station("Riegelsberg Rathaus", 49.301182, 6.944999, "rgbrhaus", 6));
		gps.put("Riegelsberg Wolfskaulstraße", new Station("Riegelsberg Wolfskaulstraße", 49.297092, 6.94838, "wlkstr", 7));
		gps.put("Riegelsberg Süd", new Station("Riegelsberg Süd", 49.294298,6.953292, "rgbsued", 8));
		gps.put("Heinrichshaus", new Station("Heinrichshaus", 49.27475, 6.96096, "heinrichsh", 9));
		gps.put("Siedlerheim", new Station("Siedlerheim", 49.25438, 6.960992, "siedlerh", 10));
		gps.put("Rastpfuhl", new Station("Rastpfuhl", 49.250396, 6.963481, "rastpf", 11));
		gps.put("Pariser Platz", new Station("Pariser Platz", 49.246967, 6.967515, "parieserpl", 12));
		gps.put("Cottbuser Platz", new Station("Cottbuser Platz", 49.243065, 6.972209, "cottbpl", 13));
		gps.put("Ludwigstraße", new Station("Ludwigstraße", 49.241214, 6.978378, "ludwigsstr", 14));
		gps.put("Trierer Straße", new Station("Trierer Straße", 49.240269, 6.984394, "trierstr", 15));		
		gps.put("Saarbrücken Hbf", new Station("Saarbrücken Hbf", 49.24024, 6.99022, "sbhbf", 16));
		gps.put("Kaiserstraße", new Station("Kaiserstraße", 49.23783, 6.994005, "kaiserstr", 17));
		gps.put("Johanneskirche", new Station("Johanneskirche", 49.235789, 6.99654, "johkirch", 18));
		gps.put("Landwehrplatz", new Station("Landwehrplatz", 49.233565, 7.00049, "landwpl", 19));
		gps.put("Uhlandstraße", new Station("Uhlandstraße", 49.23128, 7.006574, "uhlstr", 20));
		gps.put("Helwigstraße", new Station("Helwigstraße", 49.229212, 7.011635, "helwigstr", 21));
		gps.put("Kieselhumes", new Station("Kieselhumes", 49.228131, 7.017421, "kieselh", 22));
		gps.put("Römerkastell", new Station("Römerkastell", 49.226964, 7.021449, "roemerk", 23));
		gps.put("Brebach", new Station("Brebach", 49.216662, 7.028801, "brebach", 24));
		
		SaarbahnLatin1 = new HashMap<String, String>();
		//Auersmacher, Bübingen, G%C3%BCdingen+Bahnhof, Hanweiler+Bahnhof, Sarreguemines, Kleinblittersdorf+Bahnhof
		SaarbahnLatin1.put("Walpershofen/Etzenhofen", "Walpershofen%2FEtzenhofen");
		SaarbahnLatin1.put("Riegelsberg Gisorsstraße", "Gisorsstra%C3%9Fe");
		SaarbahnLatin1.put("Riegelsberg Güchenbach", "G%C3%BCchenbach");
		SaarbahnLatin1.put("Riegelsberghalle", "Riegelsberghalle");
		SaarbahnLatin1.put("Riegelsberg Post", "Riegelsberg+Post");
		SaarbahnLatin1.put("Riegelsberg Rathaus", "Riegelsberg+Rathaus");
		SaarbahnLatin1.put("Riegelsberg Wolfskaulstraße", "Wolfskaulstra%C3%9Fe");
		SaarbahnLatin1.put("Riegelsberg Süd", "Riegelsberg+S%C3%BCd");
		SaarbahnLatin1.put("Heinrichshaus", "Heinrichshaus");
		SaarbahnLatin1.put("Siedlerheim", "Siedlerheim");
		SaarbahnLatin1.put("Rastpfuhl", "Rastpfuhl");
		SaarbahnLatin1.put("Pariser Platz", "Pariser+Platz");
		SaarbahnLatin1.put("Cottbuser Platz", "Cottbuser+Platz");
		SaarbahnLatin1.put("Ludwigstraße", "Ludwigstra%C3%9Fe");
		SaarbahnLatin1.put("Trierer Straße", "Trierer+Stra%C3%9Fe");		
		SaarbahnLatin1.put("Saarbrücken Hbf", "Hauptbahnhof");
		SaarbahnLatin1.put("Kaiserstraße", "Kaiserstra%C3%9Fe");
		SaarbahnLatin1.put("Johanneskirche", "Johanneskirche");
		SaarbahnLatin1.put("Landwehrplatz", "Landwehrplatz");
		SaarbahnLatin1.put("Uhlandstraße", "Uhlandstra%C3%9Fe");
		SaarbahnLatin1.put("Helwigstraße", "Hellwigstra%C3%9Fe");
		SaarbahnLatin1.put("Kieselhumes", "Kieselhumes");
		SaarbahnLatin1.put("Römerkastell", "R%C3%B6merkastell");
		SaarbahnLatin1.put("Brebach", "Brebach+Bahnhof");
	}
	
	public HashMap<String, Station> getStations()
	{
		return gps;
	}
	
	public Station getStationByName(String name)
	{
		return gps.get(name);
	}
	
	public List<Station> getByDistances()
	{
		List<Station> stations = new ArrayList<Station>();
		Object[] keys = (Object[]) gps.keySet().toArray();
		for( int i=0; i<gps.size(); i++)
		{
			stations.add(gps.get((String) keys[i]));
		}
		Collections.sort(stations, new Comparator<Station>() 
			{
				public int compare(Station o1, Station o2) 
			    {
			        double d1 = (Double) ( (Station) o1 ).distance;
			        double d2 = (Double) ( (Station) o2 ).distance;
			        return Double.compare(d1, d2);
			    }
			});
		/*Iterator <Station>  it = stations.iterator( ) ;
		while(it.hasNext())
		{
			Station s = it.next();
			//Log.i("nx station", s.name+" = "+s.distance);
		}*/
		
		return stations;
	}
	
	public List<Station> getBySort()
	{
		List<Station> stations = new ArrayList<Station>();
		Object[] keys = (Object[]) gps.keySet().toArray();
		for( int i=0; i<gps.size(); i++)
		{
			stations.add(gps.get((String) keys[i]));
		}
		Collections.sort(stations, new Comparator<Station>() 
			{
				public int compare(Station o1, Station o2) 
			    {
			        double d1 = (int) ( (Station) o1 ).position;
			        double d2 = (int) ( (Station) o2 ).position;
			        return Double.compare(d1, d2);
			    }
			});
		/*Iterator <Station>  it = stations.iterator( ) ;
		while(it.hasNext())
		{
			Station s = it.next();
		}*/
		
		return stations;
	}
	
	public String getSaarbahnLatin1( String myname )
	{
		return SaarbahnLatin1.get(myname);
	}
}

