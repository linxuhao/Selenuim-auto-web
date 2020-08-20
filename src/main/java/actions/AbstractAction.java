package actions;

import java.lang.System.Logger.Level;
import java.time.LocalTime;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.openqa.selenium.WebDriver;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import constants.ActionType;
import controller.ActionInput;
import customExceptions.LoggedException;
import utils.TimeUtils;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonSubTypes({
  @JsonSubTypes.Type(value = ActionWithContent.class, name = "ActionWithContent"),
  @JsonSubTypes.Type(value = ActionWithNextCondition.class, name = "ActionWithNextCondition")
})
public abstract class AbstractAction {

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
	
	@JsonIgnore
	public WebDriver getWebDriver() {
		return webDriver;
	}

	public void setWebDriver(WebDriver webDriver) {
		this.webDriver = webDriver;
	}

	@JsonIgnore
	public final LocalTime getExecutionTimeAsLocalTime() {
		return TimeUtils.toLocalTime(executionTime);
	}
	
	public final String getExecutionTime() {
		return executionTime;
	}

	public final String getTarget() {
		return target;
	}

	public final ActionType getActionType() {
		return actionType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(actionType, executionTime, target);
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

	@Override
	public String toString() {
		return "AbstractAction [executionTime=" + executionTime + ", actionType=" + actionType + ", target=" + target
				+ "]";
	}

	public final ActionInput toActionInput() {
		final ActionInput input = new ActionInput();
		input.setActionType(actionType);
		input.setExecutionTime(executionTime);
		input.setTarget(target);
		return populateInputNext(input);
	}

	protected abstract ActionInput populateInputNext(final ActionInput input);

	public void doAction(final BiConsumer<Level, String> logConsumer) {
		if(null == webDriver) {
			throw new LoggedException(Level.ERROR, "No web driver set");
		}
		doSubAction(logConsumer);
	}
	
	protected abstract void doSubAction(final BiConsumer<Level, String> logConsumer);


}
