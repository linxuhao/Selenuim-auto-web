package utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;


public class DateTimeUtils {

	public static final String TIME_DELIMITER = ":";

	public static final LocalTime toLocalTime(final String time) throws DateTimeParseException {
		return LocalTime.parse(time);
	}

	public static final LocalDateTime toTodayDateTime(final LocalTime localTime) {
		return LocalDateTime.of(LocalDate.now(), localTime);
	}

	public static final LocalDateTime toTodayDateTime(final String localTime) throws DateTimeParseException {
		return toTodayDateTime(toLocalTime(localTime));
	}
	
	public static final LocalDateTime toTomorrowDateTime(final LocalTime localTime) {
		return toTodayDateTime(localTime).plusDays(1);
	}

	public static final LocalDateTime toTomorrowDateTime(final String localTime) throws DateTimeParseException {
		return toTodayDateTime(localTime).plusDays(1);
	}

}
