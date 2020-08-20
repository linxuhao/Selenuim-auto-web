package actions;

import java.lang.System.Logger.Level;
import java.util.Objects;
import java.util.function.BiConsumer;

import constants.ActionType;
import controller.ActionInput;
import utils.LogUtils;
import utils.WebDriverUtils;

public class ActionWithContent extends AbstractAction {
	
	private String content;

	public ActionWithContent() {
		super();
	}

	public ActionWithContent(final String executionTime, final String target, final String content) {
		super(executionTime, ActionType.FILL, target);
		this.content = content;
	}

	public final String getContent() {
		return content;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(content);
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
		ActionWithContent other = (ActionWithContent) obj;
		return Objects.equals(content, other.content);
	}

	@Override
	public String toString() {
		return "ActionWithContent [content=" + content + ", getExecutionTime()=" + getExecutionTime() + ", getTarget()="
				+ getTarget() + ", getActionType()=" + getActionType() + "]";
	}
	

	@Override
	public void doSubAction(final BiConsumer<Level, String> logConsumer) {
		if (content.isBlank()) {
			final String reason = "No content to fill specified";
			if(null != logConsumer) {
				logConsumer.accept(Level.INFO, String.format(LogUtils.INFO_MESSAGE_TEMPLATE, reason));
			}
		}
		WebDriverUtils.fill(getWebDriver(), getTarget(), getContent());
	}
	
	@Override
	protected ActionInput populateInputNext(final ActionInput input) {
		input.setContent(content);
		return input;
	}

}
