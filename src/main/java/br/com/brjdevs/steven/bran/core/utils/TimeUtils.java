package br.com.brjdevs.steven.bran.core.utils;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtils {
	
	private static final Pattern pattern = Pattern.compile("\\d+?[a-zA-Z]");
	
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
	
	public static String neat(long duration) {
		final long
				years = duration / 31104000000L,
				months = duration / 2592000000L % 12,
				days = duration / 86400000L % 30,
				hours = duration / 3600000L % 24,
				minutes = duration / 60000L % 60,
				seconds = duration / 1000L % 60;
        String uptime = (years == 0 ? "" : years + " years, ") + (months == 0 ? "" : months + " months, ")
                + (days == 0 ? "" : days + " days, ") + (hours == 0 ? "" : hours + " hours, ")
                + (minutes == 0 ? "" : minutes + " minutes, ") + (seconds == 0 ? "" : seconds + " seconds, ");
        
        uptime = StringUtils.replaceLast(uptime, ", ", "");
		return StringUtils.replaceLast(uptime, ",", " and");
		
	}
	public static Iterable<String> iterate(Matcher matcher) {
		return new Iterable<String>() {
			
			@Override
			public Iterator<String> iterator() {
				return new Iterator<String>() {
					
					@Override
					public boolean hasNext() {
						return matcher.find();
					}
					
					@Override
					public String next() {
						return matcher.group();
					}
				};
			}
			
			@Override
			public void forEach(Consumer<? super String> action) {
				while (matcher.find()) {
					action.accept(matcher.group());
				}
			}
		};
	}
	
	public static long getTime(String s, TimeUnit u) {
		s = s.toLowerCase();
		long[] time = {0};
		iterate(pattern.matcher(s)).forEach(string -> {
			String l = string.substring(0, string.length() - 1);
			TimeUnit unit;
			switch (string.charAt(string.length() - 1)) {
				case 's':
					unit = TimeUnit.SECONDS;
					break;
				case 'm':
					unit = TimeUnit.MINUTES;
					break;
				case 'h':
					unit = TimeUnit.HOURS;
					break;
				case 'd':
					unit = TimeUnit.DAYS;
					break;
				default:
                    throw new UnsupportedOperationException("Unknown time unit \"" + string.charAt(string.length() - 1) + "\".");
            }
			time[0] += unit.toMillis(Long.parseLong(l));
		});
		return u.convert(time[0], TimeUnit.MILLISECONDS);
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
