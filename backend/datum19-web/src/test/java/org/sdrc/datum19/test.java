package org.sdrc.datum19;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.sdrc.datum19.document.TimePeriod;

public class test {
	
	private static SimpleDateFormat simpleDateformater = new SimpleDateFormat("yyyy-MM-dd");
	
	private static DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("MMM");
	
	private static SimpleDateFormat fullDateFormat = new SimpleDateFormat("MMMM");
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy");

	public static void main(String[] args) {
		int randomNum = new Random().nextInt((100 - 50) + 1) + 50;
		System.out.println(randomNum);
		
		List<TimePeriod> liTimePeriods = new ArrayList<>();
		String financialYear = financialYear();
		for (int i = 5; i >= 0; i--) {
			liTimePeriods.add(insertTimeperiod(financialYear, String.valueOf(i), "1"));
		}
		System.out.println(liTimePeriods);
		System.out.println(generate(randomNum, 5));
		
		
	}

	private static Integer randombetween(Integer min, Integer max) {
	    return (int) Math.floor(Math.random()*(max-min+1)+min);
	  }

	private static List<Integer> generate(int max, int thecount) {
		List<Integer> r = new ArrayList<>();
	    int currsum = 0;
	    for(int i=0; i<thecount-1; i++) {
	    	int v = randombetween(1, max-(thecount-i-1)-currsum);
	    	currsum += v;
	        r.add(v);
	    }
	    r.add(max-currsum);
	    return r;
	  }

	public static String financialYear() {
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH);
		int preYear = 0, nextYear = 0;
		if (month > 5) {
			preYear = cal.get(Calendar.YEAR);
			cal.add(Calendar.YEAR, 1);
			nextYear = cal.get(Calendar.YEAR);
		} else {
			cal.add(Calendar.YEAR, -1);
			preYear = cal.get(Calendar.YEAR);
			cal.add(Calendar.YEAR, 1);
			nextYear = cal.get(Calendar.YEAR);
		}
		return preYear + "-" + nextYear;
	}

	private static TimePeriod insertTimeperiod(String financialYear, String month, String periodicity) {
		
		Integer startMonth = Integer.valueOf(month).intValue();
		Integer endMonth = Integer.valueOf(month).intValue();
		TimePeriod timePeriod = null;
		try {
			Calendar startDateCalendar = Calendar.getInstance();
			startDateCalendar.add(Calendar.MONTH, -startMonth);
			startDateCalendar.set(Calendar.DATE, 1);
			Date strDate = startDateCalendar.getTime();
			String startDateStr = simpleDateformater.format(strDate);
			Date startDate = (Date) formatter.parse(startDateStr + " 00:00:00.000");
			Calendar endDateCalendar = Calendar.getInstance();
			endDateCalendar.add(Calendar.MONTH, -endMonth);
			endDateCalendar.set(Calendar.DATE, endDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
			Date eDate = endDateCalendar.getTime();
			String endDateStr = simpleDateformater.format(eDate);
			Date endDate = (Date) formatter.parse(endDateStr + " 00:00:00.000");

			timePeriod = new TimePeriod();
			timePeriod.setStartDate(new java.sql.Date(startDate.getTime()));
			timePeriod.setEndDate(new java.sql.Date(endDate.getTime()));
			timePeriod.setPeriodicity(periodicity);
			timePeriod.setTimePeriod(fullDateFormat.format(endDate) + "," + sdf.format(endDate));
			timePeriod.setYear(Integer.valueOf(sdf.format(endDate)));
			timePeriod.setTimePeriodDuration(dateFormat.format(startDate));
			timePeriod.setShortName(dateFormat.format(startDate)+"-"+timePeriod.getYear());
			timePeriod.setFinancialYear(financialYear);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return timePeriod;
	}

}
