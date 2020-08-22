package actions;

import java.lang.System.Logger.Level;
import java.util.Objects;
import java.util.function.BiConsumer;

import constants.ActionType;
import constants.NextConditionType;
import controller.ActionInput;
import customExceptions.LoggedException;
import utils.WebDriverUtils;

public class ActionWithNextCondition extends AbstractAction{
	
	private NextConditionType nextConditionType;
	private String nextCondition;
	private boolean retryIfNotNext;

	public ActionWithNextCondition() {
		super();
	}

	public ActionWithNextCondition(final String executionTime, final ActionType actionType, final String target,
			final NextConditionType nextConditionType, final String nextCondition, final boolean retryIfNotNext) {
		super(executionTime, actionType, target);
		this.nextConditionType = nextConditionType;
		this.nextCondition = nextCondition;
		this.retryIfNotNext = retryIfNotNext;
	}
	

	public final NextConditionType getNextConditionType() {
		return nextConditionType;
	}


	public final String getNextCondition() {
		return nextCondition;
	}

	public final boolean isRetryIfNotNext() {
		return retryIfNotNext;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(nextCondition, nextConditionType, retryIfNotNext);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActionWithNextCondition other = (ActionWithNextCondition) obj;
		return Objects.equals(nextCondition, other.nextCondition) && nextConditionType == other.nextConditionType
				&& retryIfNotNext == other.retryIfNotNext;
	}

	@Override
	public String toString() {
		return "ActionWithNextCondition [nextConditionType=" + nextConditionType + ", nextCondition=" + nextCondition
				+ ", retryIfNotNext=" + retryIfNotNext + ", getExecutionTime()=" + getExecutionTime() + ", getTarget()="
				+ getTarget() + ", getActionType()=" + getActionType() + "]";
	}
	
	@Override
	public void doSubAction(final BiConsumer<Level, String> logConsumer) throws Exception {
		final boolean wantHttpCode = NextConditionType.HTTP_CODE == getNextConditionType();
		int httpCode = -1;
		switch (getActionType()) {
		case NAVIGATE:
			produceLog(logConsumer, Level.DEBUG, "Navigating to " + getTarget());
			httpCode = WebDriverUtils.navigate(getWebDriver(), getTarget(), wantHttpCode);
			retryOnCondition(logConsumer, httpCode);
			break;
		case CLICK:
			httpCode = WebDriverUtils.click(getWebDriver(), getTarget(), wantHttpCode);
			break;
		default:
			throw new LoggedException(Level.ERROR, "Unsupported action type: " + getActionType() + " for the class: " + getClass());
		}
	}

	private void retryOnCondition(final BiConsumer<Level, String> logConsumer, int httpCode)
			throws InterruptedException, Exception {
		boolean succed = evaluateNextCondition(httpCode);
		boolean httpCodeNotFound = WebDriverUtils.HTTP_CODE_URL_NOT_FOUND == httpCode;
		if(httpCodeNotFound) {
			produceLog(logConsumer, Level.WARNING, "The http code of " + getWebDriver().getCurrentUrl() + " is not found");
		}else {
			if(retryIfNotNext && !succed) {
				produceLog(logConsumer, Level.DEBUG, "The http code of " + getWebDriver().getCurrentUrl() + " is " + httpCode + "\nWhich is not " + nextCondition + ", retrying");
				doSubAction(logConsumer);
			}
		}
	}

	private boolean evaluateNextCondition(final int httpCode) throws NumberFormatException, InterruptedException {
		boolean result = false;
		switch (getNextConditionType()) {
		case HTTP_CODE:
			if(Integer.parseInt(nextCondition) == httpCode) {
				result = true;
			}
			break;
		case DELAY_MILISECONDS:
			Thread.sleep(Integer.parseInt(nextCondition));
		case NO_CONDITION:
		default:
			result = true;
			break;
		}
		return result;
	}

	@Override
	protected ActionInput populateInputNext(final ActionInput input) {
		input.setNextConditionType(nextConditionType);
		input.setNextCondition(nextCondition);
		input.setRetryIfNotNext(retryIfNotNext);
		return input;
	}
	
}