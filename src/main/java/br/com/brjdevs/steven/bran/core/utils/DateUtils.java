package br.com.brjdevs.steven.bran.core.utils;

public class DateUtils {
	
	public static String format(long time) {
		final long years = time / 31104000000L,
				months = time / 2592000000L % 12,
				days = time / 86400000L % 30,
				hours = time / 3600000L % 24,
				minutes = time / 60000L % 60,
				seconds = time / 1000L % 60;
		return (years == 0 ? "" : decimal(years) + ":")
				+ (months == 0 ? "" : decimal(months) + ":")
				+ (days == 0 ? "" : decimal(days) + ":")
				+ (hours == 0 ? "" : decimal(hours) + ":")
				+ (minutes == 0 ? "00" : decimal(minutes)) + ":"
				+ (seconds == 0 ? "00" : decimal(seconds));
	}
	
	public static String formatTimeBetween(long first, long second) {
		long time = first - second;
		if (time < 0) time *= -1;
		final long years = time / 31104000000L,
				months = time / 2592000000L % 12,
				days = time / 86400000L % 30,
				hours = time / 3600000L % 24,
				minutes = time / 60000L % 60,
				seconds = time / 1000L % 60;
		return (years == 0 ? "" : decimal(years) + ":")
				+ (months == 0 ? "" : decimal(months) + ":")
				+ (days == 0 ? "" : decimal(days) + ":")
				+ (hours == 0 ? "" : decimal(hours) + ":")
				+ (minutes == 0 ? "00" : decimal(minutes)) + ":"
				+ (seconds == 0 ? "00" : decimal(seconds));
	}
	public static String decimal(long num) {
		if (num > 9) return String.valueOf(num);
		return "0" + num;
	}
}
