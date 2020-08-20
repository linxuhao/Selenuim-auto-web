package controller;

import java.io.File;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import org.openqa.selenium.WebDriver;

import com.fasterxml.jackson.databind.ObjectMapper;

import actions.AbstractAction;
import customExceptions.LoggedException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.FormuleModel;
import utils.LogUtils;
import utils.WebDriverUtils;

public class FormuleController {

	@FXML
	private Button removeButton;

	@FXML
	private Button addButton;

	@FXML
	private Button loadButton;

	@FXML
	private Button saveButton;

	@FXML
	private Button startButton;

	@FXML
	private Button clearLogButton;

	@FXML
	private TextFlow logConsole;

	@FXML
	private ScrollPane displayPane;

	private VBox fieldZone;

	public Stage primaryStage;

	private FormuleModel model = new FormuleModel();

	public void initialize() {
		fieldZone = new VBox();
		fieldZone.setAlignment(Pos.TOP_CENTER);
		displayPane.setContent(fieldZone);
	}

	public final void activateButtons() {
		addButton.setDisable(false);
		removeButton.setDisable(false);
		loadButton.setDisable(false);
		saveButton.setDisable(false);
		startButton.setDisable(false);
		clearLogButton.setDisable(false);
	}

	public final void disactiveButtons() {
		addButton.setDisable(true);
		removeButton.setDisable(true);
		loadButton.setDisable(true);
		saveButton.setDisable(true);
		startButton.setDisable(true);
		clearLogButton.setDisable(true);
	}

	private void addNewInputField() {
		ActionInput actionInput = new ActionInput();
		fieldZone.getChildren().add(actionInput);
	}

	private void removeTheLastInputField() {
		if (!fieldZone.getChildren().isEmpty()) {
			fieldZone.getChildren().remove(fieldZone.getChildren().size() - 1);
		}
	}

	@FXML
	void add(ActionEvent event) {
		addNewInputField();
	}

	@FXML
	void remove(ActionEvent event) {
		removeTheLastInputField();
	}

	@FXML
	void loadFromFile(ActionEvent event) throws Exception {
		disactiveButtons();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Load Configuration File");
		File file = fileChooser.showOpenDialog(primaryStage);
		try {
			if (null != file) {
				final ObjectMapper mapper = getObjectMapper();
				FormuleModel model = mapper.readValue(file, FormuleModel.class);
				this.model = model;
				updateFromModel();
				addLog(Level.DEBUG, "Configuration file load succed");
			}
		} catch (Exception e) {
			addLog(Level.ERROR, Arrays.toString(e.getStackTrace()));
			throw e;
		} finally {
			activateButtons();
		}

	}

	private ObjectMapper getObjectMapper() {
		final ObjectMapper mapper = new ObjectMapper();
		return mapper;
	}

	private List<ActionInput> loadFromObjectList(final List<AbstractAction> actionList) {
		List<ActionInput> actionInputList = new ArrayList<>();
		for (AbstractAction action : actionList) {
			actionInputList.add(action.toActionInput());
		}
		return actionInputList;
	}

	@FXML
	void saveToFile(ActionEvent event) throws Exception {
		disactiveButtons();
		try {
			//Not saving web driver to json, so no need to set it in actions
			final List<AbstractAction> actionList = getActionList(null);
			if (!actionList.isEmpty()) {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Save Configuration file");
				File file = fileChooser.showSaveDialog(primaryStage);

				if (null != file) {
					final ObjectMapper mapper = getObjectMapper();
					updateToModel();
					addLog(Level.DEBUG, "Saving configuration to file : " + file.getAbsolutePath());
					mapper.writeValue(file, model);
				}
			} else {
				addLog(Level.DEBUG, "No action to save");
			}
		} catch (Exception e) {
			addLog(Level.ERROR, Arrays.toString(e.getStackTrace()));
			throw e;
		} finally {
			activateButtons();
		}
	}

	private void updateFromModel() {
		fieldZone.getChildren().clear();
		fieldZone.getChildren().addAll(loadFromObjectList(this.model.getActionList()));
	}
	
	private void updateToModel() {
		this.model.setActionList(getActionList(null));
	}

	private List<AbstractAction> getActionList(@Nullable final WebDriver webDriver) {
		final List<AbstractAction> actionList = new ArrayList<>();
		for (Node node : fieldZone.getChildrenUnmodifiable()) {
			if (node instanceof ActionInput) {
				final ActionInput actionInput = (ActionInput) node;
				try {
					final AbstractAction action = actionInput.toAction();
					if(null != webDriver) {
						action.setWebDriver(webDriver);
					}
					actionList.add(action);
				} catch (LoggedException e) {
					addLog(e.getLogLevel(), e.getMessage());
					if (Level.ERROR == e.getLogLevel()) {
						throw e;
					}
				}
			}
		}
		return actionList;
	}

	@FXML
	void clearLog(ActionEvent event) {
		LogUtils.clearLog(logConsole);
	}

	public final void addLog(Level level, String message) {
		LogUtils.addLog(logConsole, level, message);
	}
	
	@FXML
	void startTask(ActionEvent event) {
		disactiveButtons();
		try {
			final WebDriver webDriver = WebDriverUtils.getWebDriver();
			webDriver.get("https://www.google.com/");
			WebDriverUtils.getHttpCode(webDriver);
			final List<AbstractAction> actionList = getActionList(webDriver);
			addLog(Level.INFO, "There are total of " + actionList.size() + " actions to execute");
			for (int i = 0; i < actionList.size(); i++) {
				addLog(Level.DEBUG, "Executing action " + (i + 1) + " out of " + actionList.size());
				try {
					actionList.get(i).doAction((level, message) -> addLog(level, message));
				} catch (LoggedException e) {
					// Stops the execution if is error level exception,
					// otherwise stop & skip only the current action
					if (Level.ERROR == e.getLogLevel()) {
						throw e;
					} else {
						addLog(e.getLogLevel(), e.getMessage());
					}
				}
			}
		} catch (Exception e) {
			addLog(Level.ERROR, Arrays.toString(e.getStackTrace()));
			throw e;
		} finally {
			addLog(Level.INFO, "Execution finished");
			activateButtons();
		}
	}

}
