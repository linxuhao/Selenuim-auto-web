package actions;

import java.lang.System.Logger.Level;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.openqa.selenium.WebDriver;

import com.fasterxml.jackson.annotation.JsonIgnore;

import constants.ActionType;
import controller.ActionInput;
import customExceptions.LoggedException;
import utils.DateTimeUtils;
import utils.LogUtils;

public abstract class AbstractAction {

	public static final int RETRY_TIMES = 3;
	
	@JsonIgnore
	private WebDriver webDriver;
	private String executionTime;
	private ActionType actionType;
	private String target;

	public AbstractAction() {
		super();
	}

	public AbstractAction(final String executionTime, final ActionType actionType, final String target) {
		super();
		this.webDriver = null;
		this.executionTime = executionTime;
		this.actionType = actionType;
		this.target = target;
	}

	public void doAction(final BiConsumer<Level, String> logConsumer) throws Exception {
		if (null == webDriver) {
			throw new LoggedException(Level.ERROR, "No web driver set");
		}
		try {
			waitUntillExecutionTime(logConsumer);
		} catch (DateTimeParseException e) {
			final String reason = "Unknow execution time, execution will not wait its execution time (it doesn't have one)";
			produceLog(logConsumer, Level.INFO, String.format(LogUtils.INFO_MESSAGE_TEMPLATE, reason));
		} catch (Exception e) {
			final String reason = "Wait until execution time failed, executing now";
			produceLog(logConsumer, Level.INFO, String.format(LogUtils.INFO_MESSAGE_TEMPLATE, reason));
		} finally {
			doSubAction(logConsumer, RETRY_TIMES);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractAction other = (AbstractAction) obj;
		return actionType == other.actionType && Objects.equals(executionTime, other.executionTime)
				&& Objects.equals(target, other.target);
	}

	public final ActionType getActionType() {
		return actionType;
	}

	public final String getExecutionTime() {
		return executionTime;
	}

	@JsonIgnore
	public final LocalTime getExecutionTimeAsLocalTime() {
		return DateTimeUtils.toLocalTime(executionTime);
	}

	public final String getTarget() {
		return target;
	}

	@JsonIgnore
	public WebDriver getWebDriver() {
		return webDriver;
	}

	@Override
	public int hashCode() {
		return Objects.hash(actionType, executionTime, target);
	}

	public void setWebDriver(WebDriver webDriver) {
		this.webDriver = webDriver;
	}

	public final ActionInput toActionInput() {
		ActionInput input = new ActionInput();
		input.setActionType(actionType);
		input.setExecutionTime(executionTime);
		input.setTarget(target);
		input = populateInputNext(input);
		input.refreshComponents();
		return input;
	}

	@Override
	public String toString() {
		return getClass() + " [executionTime=" + executionTime + ", actionType=" + actionType + ", target=" + target
				+ ", " + subToString() + "]";
	}

	/**
	 * Guess the execution date if the time is in the past <br>
	 * If time is at least 1 hours before now, i suppose the user wants to execute the actions for tomorrow <br>
	 * Otherwise, user could be repeatly clicking on start and didnt refresh his execution time, execution date is still today 
	 * @param logConsumer
	 * @param now
	 * @return an guessed local date time, is neither today or tomorrow
	 */
	private LocalDateTime guessExecutionDateTime(final BiConsumer<Level, String> logConsumer, final LocalDateTime now) {
		LocalDateTime executionDateTime = DateTimeUtils.toTodayDateTime(getExecutionTime());
		if(executionDateTime.isBefore(now)) {
			//if I schedule a time in the past
			if (executionDateTime.isBefore(now.minusHours(1))) {
				//if I schedule a time 1 hours before now, 
				//I assume that i want it to be executed tomorrow
				produceLog(logConsumer, Level.DEBUG, "The execution time is " + getExecutionTime()
						+ ", It is at least 1 hours before now, so i assume you want it to be executed tomorrow");
				executionDateTime = executionDateTime.plusDays(1);
			}else {
				produceLog(logConsumer, Level.DEBUG, "The execution time is " + getExecutionTime()
				+ ", It is within 1 hours before now, so i guess you want it to be executed immediatly");
			}
		}
		return executionDateTime;
	}

	private void waitUntillExecutionTime(final BiConsumer<Level, String> logConsumer) throws InterruptedException {
		LocalDateTime now = LocalDateTime.now();
		final LocalDateTime executionDateTime = guessExecutionDateTime(logConsumer, now);
		//refresh now
		now = LocalDateTime.now();
		while (now.isBefore(executionDateTime) && !now.isEqual(executionDateTime)) {
			final long timeBeforeExecution = now.until(executionDateTime, ChronoUnit.MILLIS);
			final long sleepTimeInMilli = timeBeforeExecution;
			produceLog(logConsumer, Level.DEBUG, "The execution time is " + executionDateTime.toString() + ", now is "
					+ now.toString() + ", sleeping for " + sleepTimeInMilli + " milliseconds before next check");
			Thread.sleep(sleepTimeInMilli);
			now = LocalDateTime.now();
		}
	}

	protected abstract void doSubAction(final BiConsumer<Level, String> logConsumer, final int retryTimes) throws Exception;

	protected abstract ActionInput populateInputNext(final ActionInput input);

	protected void produceLog(final BiConsumer<Level, String> logConsumer, final Level level, final String logMessage) {
		if (null != logConsumer) {
			logConsumer.accept(level, logMessage);
		}
	}

	protected abstract String subToString();

}
