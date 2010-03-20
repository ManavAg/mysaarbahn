/**
 * A class to check if a date (Date object) is a catholic holiday (or one of special days)
 * @author Umberto
 * @license GPLv3
 */
package org.MySaarBahn;

import java.util.*; 

public class CatholicCalendar 
{
	/**
	 * Return true if given date is  first sunday in advent
	 * @param d Date to check
	 * @return boolean true if so
	 */
   public static boolean isFirstAdvent( Date d )
   {
	   Calendar e = new GregorianCalendar(d.getYear(), 12, 25);
	   e.add(Calendar.DAY_OF_YEAR, -21);
	   for(int i = 0; e.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY; i++)
	   {
		   e.add(Calendar.DAY_OF_YEAR, -1);
	   }
	   Calendar x = new GregorianCalendar(d.getYear(), d.getMonth(), d.getDay());
	   if(e.compareTo(x) == 0)
	   {
		   return true;
	   }
	   return false;
   }
   
   /**
	 * Return true if given date is  first Christmas day
	 * @param d Date to check
	 * @return boolean true if so
	 */
   public static boolean isChristmasDay( Date d )
   {
	   Calendar e = new GregorianCalendar(d.getYear(), 12, 25);
	   Calendar x = new GregorianCalendar(d.getYear(), d.getMonth(), d.getDay());
	   if(e.compareTo(x) == 0)
	   {
		   return true;
	   }
	   return false;
   }
   
   /**
	 * Return true if given date is second christmas day
	 * @param d Date to check
	 * @return boolean true if so
	 */
   public static boolean isStStephansDay( Date d )
   {
	   Calendar e = new GregorianCalendar(d.getYear(), 12, 26);
	   Calendar x = new GregorianCalendar(d.getYear(), d.getMonth(), d.getDay());
	   if(e.compareTo(x) == 0)
	   {
		   return true;
	   }
	   return false;
   }
   
   /**
	 * Return true if given date is second christmas day
	 * @param d Date to check
	 * @return boolean true if so
	 */
  public static boolean isHollyFamilySunday( Date d )
  {
	   Calendar e = new GregorianCalendar(d.getYear(), 12, 26);
	   Calendar x = new GregorianCalendar(d.getYear(), d.getMonth(), d.getDay());
	   if(e.compareTo(x) == -1)
	   {
		   for(int i=0; e.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY; i++)
		   {
			   e.add(Calendar.DAY_OF_YEAR, +1);
		   }
		   if(e.compareTo(x) == 0)
		   {
			   return true;
		   }
	   }
	   return false;
  }
   
   /**
	 * Return true if given date is New Year's Day
	 * @param d Date to check
	 * @return boolean true if so
	 */
   public static boolean isNewYearsDay( Date d )
   {
	   Calendar e = new GregorianCalendar(d.getYear(), 1, 1);
	   Calendar x = new GregorianCalendar(d.getYear(), d.getMonth(), d.getDay());
	   if(e.compareTo(x) == 0)
	   {
		   return true;
	   }
	   return false;
   }
   
   /**
	 * Return true if given date is Epiphany (Three Kings)
	 * @param d Date to check
	 * @return boolean true if so
	 */
   public static boolean isEpiphany( Date d )
   {
	   Calendar e = new GregorianCalendar(d.getYear(), 1, 6);
	   Calendar x = new GregorianCalendar(d.getYear(), d.getMonth(), d.getDay());
	   if(e.compareTo(x) == 0)
	   {
		   return true;
	   }
	   return false;
   }
   
   /**
	 * Return true if given date is Ash Wednesday
	 * @param d Date to check
	 * @return boolean true if so
	 */
   public static boolean isAshWednesday( Date d )
   {
	   Calendar e = easterDate(d.getYear());
	   Calendar x = new GregorianCalendar(d.getYear(), d.getMonth(), d.getDay());
	   x.add(Calendar.DAY_OF_YEAR, +46);
	   if(e.compareTo(x) == 0)
	   {
		   return true;
	   }
	   return false;
   }
   
   /**
	 * Return true if given date is Laetare Sunday
	 * @param d Date to check
	 * @return boolean true if so
	 */
  public static boolean isLaetare( Date d )
  {
	   Calendar e = easterDate(d.getYear());
	   Calendar x = new GregorianCalendar(d.getYear(), d.getMonth(), d.getDay());
	   x.add(Calendar.DAY_OF_YEAR, +21);
	   if(e.compareTo(x) == 0)
	   {
		   return true;
	   }
	   return false;
  }
   
   /**
	 * Return true if given date is Palm Sunday
	 * @param d Date to check
	 * @return boolean true if so
	 */
   public static boolean isPalmSunday( Date d )
   {
	   Calendar e = easterDate(d.getYear());
	   Calendar x = new GregorianCalendar(d.getYear(), d.getMonth(), d.getDay());
	   x.add(Calendar.DAY_OF_YEAR, +7);
	   if(e.compareTo(x) == 0)
	   {
		   return true;
	   }
	   return false;
   }
   
   /**
	 * Return true if given date is Maundy Thursday de: "Gründonnerstag"
	 * @param d Date to check
	 * @return boolean true if so
	 */
   public static boolean isMaundyThursday( Date d )
   {
	   Calendar e = easterDate(d.getYear());
	   Calendar x = new GregorianCalendar(d.getYear(), d.getMonth(), d.getDay());
	   x.add(Calendar.DAY_OF_YEAR, +3);
	   if(e.compareTo(x) == 0)
	   {
		   return true;
	   }
	   return false;
   }

   /**
	 * Return true if given date is Good Friday de: "Karfreitag"
	 * @param d Date to check
	 * @return boolean true if so
	 */
   public static boolean isGoodFriday( Date d )
   {
	   Calendar e = easterDate(d.getYear());
	   Calendar x = new GregorianCalendar(d.getYear(), d.getMonth(), d.getDay());
	   x.add(Calendar.DAY_OF_YEAR, +2);
	   if(e.compareTo(x) == 0)
	   {
		   return true;
	   }
	   return false;
   }

   /**
	 * Return true if given date is Holy Saturday de: "Karsammstag"
	 * @param d Date to check
	 * @return boolean true if so
	 */
   public static boolean isHolySaturday( Date d )
   {
	   Calendar e = easterDate(d.getYear());
	   Calendar x = new GregorianCalendar(d.getYear(), d.getMonth(), d.getDay());
	   x.add(Calendar.DAY_OF_YEAR, +1);
	   if(e.compareTo(x) == 0)
	   {
		   return true;
	   }
	   return false;
   }
   
	/** 
	 * Return Easter date for a given year
	 * @param year int must be > 1583 
	 * @return Easter date as GregorianCalendar object 
	 */ 
	public static Calendar easterDate( int year ) 
	{
		   int modulo19 = year % 19; 
		   int cent = (int) (year / 100); 
		   int centAdd = year % 100; 
		   int i = (19 * modulo19 +cent -(cent/4) -((cent - ((cent+8)/25) +1) / 3) +15) % 30; 
		   int j = (32 + 2 * (cent % 4) +2 * (centAdd / 4) - i - (centAdd % 4)) % 7; 
		   int k = i + j - 7 * ((modulo19 +11 * i +22 * j) / 451) +114; 
		   int month = k / 31; 
		   int day   = (k % 31) + 1; 	 
		   return new GregorianCalendar( year, month - 1, day );
	}

	/**
	 * Return true if given date is Easter Sunday de: "Ostersonntag"
	 * @param d Date to check
	 * @return boolean true if so
	 */
	public static boolean isEasterSunday( Date d)
	{
		Calendar e = easterDate(d.getYear());
		Calendar x = new GregorianCalendar(d.getYear(), d.getMonth(), d.getDay());
		if(e.compareTo(x) == 0)
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Return true if given date is Easter Monday de: "Ostermontag"
	 * @param d Date to check
	 * @return boolean true if so
	 */
	public static boolean isEasterMonday( Date d )
	{
		Calendar e = easterDate(d.getYear());
		Calendar x = new GregorianCalendar(d.getYear(), d.getMonth(), d.getDay());
		x.add(Calendar.DAY_OF_YEAR, -1);
		if(e.compareTo(x) == 0)
		{
			return true;
		}
		return false;
	}
   
	/**
	 * Return true if given date is Ascension de: "Christi Himmelfahrt"
	 * @param d Date to check
	 * @return boolean true if so
	 */
	public static boolean isAscension( Date d )
	{
		Calendar e = easterDate(d.getYear());
		Calendar x = new GregorianCalendar(d.getYear(), d.getMonth(), d.getDay());
		x.add(Calendar.DAY_OF_YEAR, -39);
		if(e.compareTo(x) == 0)
		{
			return true;
		}
		return false;
	}

	/**
	 * Return true if given date is Pentecost Sunday de: "Pfingstsonntag"
	 * @param d Date to check
	 * @return boolean true if so
	 */
	public static boolean isPentecostSunday( Date d )
	{
		Calendar e = easterDate(d.getYear());
		Calendar x = new GregorianCalendar(d.getYear(), d.getMonth(), d.getDay());
		x.add(Calendar.DAY_OF_YEAR, -49);
		if(e.compareTo(x) == 0)
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Return true if given date is Pentecost Monday de: "Pfingstmontag"
	 * @param d Date to check
	 * @return boolean true if so
	 */
	public static boolean isPentecostMontag( Date d )
	{
		Calendar e = easterDate(d.getYear());
		Calendar x = new GregorianCalendar(d.getYear(), d.getMonth(), d.getDay());
		x.add(Calendar.DAY_OF_YEAR, -50);
		if(e.compareTo(x) == 0)
		{
			return true;
		}
		return false;
	}

	/**
	 * Return true if given date is Corpus Christi de: "Fronleichnahm"
	 * @param d Date to check
	 * @return boolean true if so
	 */
	public static boolean isCorpusChristi( Date d )
	{
		Calendar e = easterDate(d.getYear());
		Calendar x = new GregorianCalendar(d.getYear(), d.getMonth(), d.getDay());
		x.add(Calendar.DAY_OF_YEAR, -60);
		if(e.compareTo(x) == 0)
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Return true if given date is Trinitas de: "Dreifaltigkeitssontag"
	 * @param d Date to check
	 * @return boolean true if so
	 */
	public static boolean isTrinitas( Date d )
	{
		Calendar e = easterDate(d.getYear());
		Calendar x = new GregorianCalendar(d.getYear(), d.getMonth(), d.getDay());
		x.add(Calendar.DAY_OF_YEAR, -66);
		if(e.compareTo(x) == 0)
		{
			return true;
		}
		return false;
	}
   
	/**
	 * Return true if given date is All Saints' Day de: "Allerheiligen"
	 * @param d
	 * @return
	 */
   public static boolean isAllSaintsDay( Date d )
   {
	   Calendar e = new GregorianCalendar(d.getYear(), 11, 1);
	   Calendar x = new GregorianCalendar(d.getYear(), d.getMonth(), d.getDay());
	   if(e.compareTo(x) == 0)
	   {
		   return true;
	   }
	   return false;
   }
}
