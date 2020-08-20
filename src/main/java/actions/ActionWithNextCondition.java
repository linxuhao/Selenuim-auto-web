package actions;

import java.util.Objects;

import controller.ActionInput;

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
	public void doAction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected ActionInput populateInputNext(final ActionInput input) {
		input.setNextConditionType(nextConditionType);
		input.setNextCondition(nextCondition);
		input.setRetryIfNotNext(retryIfNotNext);
		return input;
	}
	
}
