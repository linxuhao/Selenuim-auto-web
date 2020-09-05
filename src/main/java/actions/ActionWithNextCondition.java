package actions;


import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.logging.Level;

import constants.ActionType;
import constants.NextConditionType;
import controller.ActionInput;
import customExceptions.LoggedException;
import utils.WebDriverUtils;

public class ActionWithNextCondition extends AbstractAction {

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

	@Override
	public void doSubAction(final BiConsumer<Level, String> logConsumer, final int retryTimes) throws Exception {
		int httpCode = -1;
		if (retryTimes < RETRY_TIMES) {
			//retry case
			produceLog(logConsumer, Level.FINER, "Refreshing current page " + getTarget());
			httpCode = WebDriverUtils.refresh(getWebDriver());
			retryOnCondition(logConsumer, httpCode, retryTimes);
		} else {
			//No retry case
			switch (getActionType()) {
			case NAVIGATE:
				produceLog(logConsumer, Level.FINER, "Navigating to " + getTarget());
				httpCode = WebDriverUtils.navigate(getWebDriver(), getTarget());
				retryOnCondition(logConsumer, httpCode, retryTimes);
				break;
			case CLICK:
				produceLog(logConsumer, Level.FINER, "Clicking " + getTarget());
				httpCode = WebDriverUtils.click(getWebDriver(), getTarget());
				retryOnCondition(logConsumer, httpCode, retryTimes);
				break;
			default:
				throw new LoggedException(Level.SEVERE,
						"Unsupported action type: " + getActionType() + " for the class: " + getClass());
			}
		}

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

	public final String getNextCondition() {
		return nextCondition;
	}

	public final NextConditionType getNextConditionType() {
		return nextConditionType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(nextCondition, nextConditionType, retryIfNotNext);
		return result;
	}

	public final boolean isRetryIfNotNext() {
		return retryIfNotNext;
	}

	@Override
	public String subToString() {
		return "nextConditionType=" + nextConditionType + ", nextCondition=" + nextCondition + ", retryIfNotNext="
				+ retryIfNotNext;
	}

	private boolean evaluateNextCondition(final BiConsumer<Level, String> logConsumer, final int httpCode)
			throws NumberFormatException, InterruptedException {
		boolean result = false;
		switch (getNextConditionType()) {
		case HTTP_CODE:
			if (Integer.parseInt(nextCondition) == httpCode) {
				result = true;
			}
			break;
		case DELAY_MILISECONDS:
			produceLog(logConsumer, Level.FINER, "Delaying for " + nextCondition + " milliseconds before next action");
			Thread.sleep(Integer.parseInt(nextCondition));
		case NO_CONDITION:
		default:
			result = true;
			break;
		}
		return result;
	}

	private void retryOnCondition(final BiConsumer<Level, String> logConsumer, final int httpCode, final int retryTimes)
			throws InterruptedException, Exception {
		final boolean succed = evaluateNextCondition(logConsumer, httpCode);
		final boolean httpCodeNotFound = WebDriverUtils.HTTP_CODE_URL_NOT_FOUND == httpCode;
		if (httpCodeNotFound) {
			produceLog(logConsumer, Level.WARNING,
					"The http code of " + getWebDriver().getCurrentUrl() + " is not found");
		} else {
			if (!succed) {
				produceLog(logConsumer, Level.WARNING, "The http code of " + getWebDriver().getCurrentUrl() + " is : "
						+ httpCode + "\nWhich is not the next condition : " + nextCondition);
				if (retryIfNotNext && retryTimes > 0) {
					final int retryTimeLeft = retryTimes - 1;
					produceLog(logConsumer, Level.FINER, "Retrying, " + retryTimeLeft + " attemp left");
					doSubAction(logConsumer, retryTimeLeft);
				} else {
					throw new LoggedException(Level.SEVERE, "Action failed");
				}
			}
		}
	}

	@Override
	protected ActionInput populateInputNext(final ActionInput input) {
		input.setNextConditionType(nextConditionType);
		input.setNextCondition(nextCondition);
		input.setRetryIfNotNext(retryIfNotNext);
		return input;
	}

}
