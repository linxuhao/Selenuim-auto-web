package controller;

import java.io.IOException;
import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;

import actions.AbstractAction;
import actions.ActionWithContent;
import actions.ActionWithNextCondition;
import constants.ActionType;
import constants.NextConditionType;
import customExceptions.LoggedException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import utils.LogUtils;

public class ActionInput extends VBox {
	
	@FXML
	private ComboBox<ActionType> actionTypeSelect;
	@FXML
	private Label timeLabel;
	@FXML
	private TextField time;
	@FXML
	private Label targetLabel;
	@FXML
	private TextField target;
	@FXML
	private Label contentLabel;
	@FXML
	private TextField content;
	@FXML
	private Label nextConditionTypeSelectLabel;
	@FXML
	private ComboBox<NextConditionType> nextConditionTypeSelect;
	@FXML
	private Label nextConditionLabel;
	@FXML
	private TextField nextCondition;
	@FXML
	private CheckBox retryIfNotNext;

	public ActionInput() {
		final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/action.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
			actionTypeSelect.getItems().addAll(ActionType.values());
			actionTypeSelect.getSelectionModel().selectFirst();
			nextConditionTypeSelect.getItems().addAll(NextConditionType.values());
			nextConditionTypeSelect.getSelectionModel().selectFirst();
			refreshComponents();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	public final void refreshComponents() {
		onActionTypeChanged();
		onNextConditionTypeChanged();
	}

	public final void setActionType(ActionType actionType) {
		this.actionTypeSelect.getSelectionModel().select(actionType);
	}

	public final void setContent(String content) {
		this.content.setText(content);
	}

	public final void setExecutionTime(String executionTime) {
		this.time.setText(executionTime);
	}

	public final void setNextCondition(String nextCondition) {
		this.nextCondition.setText(nextCondition);
	}

	public final void setNextConditionType(NextConditionType nextConditionType) {
		this.nextConditionTypeSelect.getSelectionModel().select(nextConditionType);
	}

	public final void setRetryIfNotNext(boolean retry) {
		this.retryIfNotNext.setSelected(retry);
	}

	public final void setTarget(String target) {
		this.target.setText(target);
	}

	public final AbstractAction toAction() {
		AbstractAction action = null;
		final ActionType actionType = this.actionTypeSelect.getSelectionModel().getSelectedItem();
		final NextConditionType nextConditionType = this.nextConditionTypeSelect.getSelectionModel().getSelectedItem();
		final String targetString = target.getText();
		if (StringUtils.isBlank(targetString)) {
			final String reason = "No target locator Specified";
			throw createLoggedException(Level.WARNING, LogUtils.WARNING_MESSAGE_TEMPALTE, reason);
		}
		switch (actionType) {
		case CLICK:
		case NAVIGATE:
			final String nextConditionString = nextCondition.getText();
			if (NextConditionType.NO_CONDITION != nextConditionType && StringUtils.isBlank(nextConditionString)) {
				final String reason = "No Next Condition specified (Http code or delay in millisecond)";
				throw createLoggedException(Level.WARNING, LogUtils.WARNING_MESSAGE_TEMPALTE, reason);
			}
			action = new ActionWithNextCondition(time.getText(), actionType, targetString, nextConditionType,
					nextConditionString, retryIfNotNext.isSelected());
			break;
		case SELECT:
		case FILL:
			action = new ActionWithContent(time.getText(), actionType, targetString, content.getText());
			break;
		default:
			final String reason = "Unsupported actionType : " + actionType;
			throw createLoggedException(Level.WARNING, LogUtils.WARNING_MESSAGE_TEMPALTE, reason);
		}
		return action;
	}

	@Override
	public String toString() {
		return "ActionInput [actionTypeSelect=" + actionTypeSelect.getSelectionModel().getSelectedItem() + ", time="
				+ time.getText() + ", target=" + target.getText() + ", content=" + content.getText()
				+ ", nextConditionTypeSelect=" + nextConditionTypeSelect.getSelectionModel().getSelectedItem()
				+ ", nextCondition=" + nextCondition.getText() + ", retryIfNotNext=" + retryIfNotNext.isSelected()
				+ "]";
	}

	private void changeComponenetsVisibility(boolean visibility, final Control... components) {
		for (Control component : components) {
			component.setVisible(visibility);
		}
	}

	private LoggedException createLoggedException(final Level level, final String messageTemplate,
			final String reason) {
		return new LoggedException(level, String.format(messageTemplate, reason, toString()));
	}

	private void displayContent() {
		changeComponenetsVisibility(true, contentLabel, content);
	}

	private void displayNextConditionBlock() {
		changeComponenetsVisibility(true, nextConditionTypeSelectLabel, nextConditionTypeSelect, nextConditionLabel, nextCondition, retryIfNotNext);
	}

	private void displayNextConditionContent() {
		changeComponenetsVisibility(true, nextConditionLabel, nextCondition, retryIfNotNext);
	}

	private void hideContent() {
		changeComponenetsVisibility(false, contentLabel, content);
	}

	private void hideNextConditionBlock() {
		changeComponenetsVisibility(false, nextConditionTypeSelectLabel, nextConditionTypeSelect, nextConditionLabel, nextCondition, retryIfNotNext);
	}

	private void hideNextConditionContent() {
		changeComponenetsVisibility(false, nextConditionLabel, nextCondition, retryIfNotNext);
	}

	private void onActionTypeChanged() {
		switch (actionTypeSelect.getSelectionModel().getSelectedItem()) {
		case NAVIGATE:
		case CLICK:
			displayNextConditionBlock();
			hideContent();
			break;
		case SELECT:
		case FILL:
			hideNextConditionBlock();
			displayContent();
			break;
		default:
			break;

		}
	}

	private void onNextConditionTypeChanged() {
		if (nextConditionTypeSelect.isVisible()) {
			switch (nextConditionTypeSelect.getSelectionModel().getSelectedItem()) {
			case HTTP_CODE:
			case DELAY_MILISECONDS:
				displayNextConditionContent();
				break;
			case NO_CONDITION:
				hideNextConditionContent();
			default:
				break;

			}
		} else {
			hideNextConditionContent();
		}
	}

	@FXML
	void onActionTypeChange(ActionEvent event) {
		refreshComponents();
	}

	@FXML
	void onNextConditionTypeChange(ActionEvent event) {
		onNextConditionTypeChanged();
	}

}
