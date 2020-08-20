package utils;

import java.lang.System.Logger.Level;
import java.time.LocalTime;

import customExceptions.LoggedException;

public class TimeUtils {
	
	public static final String TIME_DELIMITER = ":";
	
	public static final LocalTime toLocalTime(final String time) {
		try {
			final String[] timeSequence = time.split(TIME_DELIMITER);
			return LocalTime.of(Integer.parseInt(timeSequence[0]), Integer.parseInt(timeSequence[1]),
					Integer.parseInt(timeSequence[2]));
		} catch (Exception e) {
			final String reason = "unknow execution time, execution will not wait its execution time (it doesn't have one)";
			throw new LoggedException(Level.INFO, String.format(LogUtils.INFO_MESSAGE_TEMPLATE, reason));
		}

	}

}
