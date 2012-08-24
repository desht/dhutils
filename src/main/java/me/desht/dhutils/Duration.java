package me.desht.dhutils;

import java.util.concurrent.TimeUnit;

public class Duration {
	private final long days, hours, minutes, seconds, milliseconds;

	/**
	 * Create a new Duration object from the given parameters
	 * 
	 * @param d	Days
	 * @param h Hours
	 * @param m Minutes
	 * @param s Seconds
	 */
	public Duration(long d, long h, long m, long s, long ms) {
		this(ms + (s + m * 60 + h * 3600 + d * 86400) * 1000);
	}

	/**
	 * Create a new Duration object
	 * 
	 * @param duration	Duration in milliseconds
	 */
	public Duration(long duration) {
		if (duration < 0) {
			throw new IllegalArgumentException("duration must be positive");
		}
		days  = TimeUnit.MILLISECONDS.toDays(duration);
		hours = TimeUnit.MILLISECONDS.toHours(duration) - TimeUnit.DAYS.toHours(days);
		minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
				- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration));
		seconds = TimeUnit.MILLISECONDS.toSeconds(duration)
				- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration));
		milliseconds = duration % 1000;
	}

	/**
	 * Create a new Duration object
	 * 
	 * @param duration	Duration specification 
	 */
	public Duration(String duration) {
		duration = duration.replaceAll("(\\d)(\\D)", "$1 $2");

		String[] fields = duration.toLowerCase().split("\\s+");
		long total = 0;

		if (fields.length > 1) {
			if (fields.length % 2 == 1) {
				throw new IllegalArgumentException("Odd number of parameters in duration specification");
			}

			for (int i = 0; i < fields.length; i += 2) {
				total += Long.parseLong(fields[i]) * getMult(fields[i + 1]);
			}
		} else if (fields.length == 1) {
			// just a single number - default is the value in seconds
			total = Long.parseLong(fields[0]) * 1000;
		} else {
			throw new IllegalArgumentException("Empty duration specification");
		}

		Duration d = new Duration(total);
		days = d.getDays();
		hours = d.getHours();
		minutes = d.getMinutes();
		seconds = d.getSeconds();
		milliseconds = d.getMilliseconds();
	}

	private int getMult(String str) {
		if (str.startsWith("ms") || str.startsWith("mil")) {
			return 1;
		} else if (str.startsWith("s")) {
			return 1000;
		} else if (str.startsWith("m")) {
			return 60000;
		} else if (str.startsWith("h")) {
			return 3600000;
		} else if (str.startsWith("d")) {
			return 86400000;
		} else {
			throw new IllegalArgumentException("Unknown duration specifier " + str);
		}
	}

	public long getDays() {
		return days;
	}

	public long getHours() {
		return hours;
	}

	public long getMinutes() {
		return minutes;
	}

	public long getSeconds() {
		return seconds;
	}

	public long getMilliseconds() {
		return milliseconds;
	}

	public long getTotalDuration() {
		return milliseconds + (seconds + minutes * 60 + hours * 3600 + days * 86400) * 1000;
	}

	public String shortDescription() {
		if (days == 0 && milliseconds == 0) {
			return String.format("%02d:%02d:%02d", hours, minutes, seconds);
		} else if (days > 0 && milliseconds == 0) {
			return String.format("%dd%02d:%02d:%02d", days, hours, minutes, seconds);
		} else if (days == 0 && milliseconds > 0) {
			return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds);
		} else {  // if (days > 0 && milliseconds > 0)
			return String.format("%dd%02d:%02d:%02d.%03d", days, hours, minutes, seconds, milliseconds);
		} 
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (days > 0) {
			sb.append(days + " d ");
		}
		if (hours > 0) {
			sb.append(hours + " hr ");	
		}
		if (minutes > 0) {
			sb.append(minutes + " min ");	
		}
		if (seconds > 0) {
			sb.append(seconds + " sec");		
		}
		if (milliseconds > 0) {
			sb.append(milliseconds + " ms");
		}
		return sb.toString().trim();
	}
}
