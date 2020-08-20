package actions;

import java.lang.System.Logger.Level;
import java.time.LocalTime;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import controller.ActionInput;
import customExceptions.LoggedException;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonSubTypes({
  @JsonSubTypes.Type(value = ActionWithContent.class, name = "ActionWithContent"),
  @JsonSubTypes.Type(value = ActionWithNextCondition.class, name = "ActionWithNextCondition")
})
public abstract class AbstractAction {

	private String executionTime;
	private ActionType actionType;
	private String target;

	public AbstractAction() {
		super();
	}

	public AbstractAction(final String executionTime, final ActionType actionType, final String target) {
		super();
		this.executionTime = executionTime;
		this.actionType = actionType;
		this.target = target;
	}

	@JsonIgnore
	public final LocalTime getExecutionTimeAsLocalTime() {
		return toLocalTime(executionTime);
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

	public abstract void doAction();
	

	LocalTime toLocalTime(String time) {
		try {
			final String[] timeSequence = time.split(ActionInput.TIME_DELIMITER);
			return LocalTime.of(Integer.parseInt(timeSequence[0]), Integer.parseInt(timeSequence[1]),
					Integer.parseInt(timeSequence[2]));
		} catch (Exception e) {
			final String reason = "unknow execution time, execution will not wait its execution time (it doesn't have one)";
			throw new LoggedException(Level.INFO, String.format(ActionInput.INFO_MESSAGE_TEMPLATE, reason, toString()));
		}

	}
	

}
