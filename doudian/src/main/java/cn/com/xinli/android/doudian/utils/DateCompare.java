package cn.com.xinli.android.doudian.utils;

import java.util.Calendar;

import android.util.Log;

public class DateCompare {
	
	private String[] dateString;
	public DateCompare(String date){
		dateString = date.toString().split("\\.");
	}

	public boolean isExpired() {
		Calendar calendarDate = Calendar.getInstance();
		Calendar expiredOn = Calendar.getInstance();

		expiredOn.clear();
		expiredOn.set(Integer.parseInt(dateString[0]), Integer.parseInt(dateString[1])-1, Integer.parseInt(dateString[2]));

		int result = calendarDate.getTime().compareTo(expiredOn.getTime());

		if (result > 0)
			return true;

		return false;
	}
}
