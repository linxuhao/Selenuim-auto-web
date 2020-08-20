package controller;

import java.io.File;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import actions.AbstractAction;
import actions.ActionList;
import customExceptions.LoggedException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

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
	void startTask(ActionEvent event) {
		disactiveButtons();
		final List<AbstractAction> actionList = getActionList();
		addLog(Level.INFO, "There are total of " + actionList.size() + " actions to execute");
		for (int i = 0; i < actionList.size(); i++) {
			addLog(Level.DEBUG, "Executing action " + (i + 1) + " out of " + actionList.size());
			actionList.get(i).doAction();
		}
		addLog(Level.INFO, "Execution finished");
		activateButtons();
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
				List<AbstractAction> actionList = mapper.readValue(file, ActionList.class).getActionList();
				fieldZone.getChildren().clear();
				fieldZone.getChildren().addAll(loadFromObjectList(actionList));
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
		final List<AbstractAction> actionList = getActionList();
		if (!actionList.isEmpty()) {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save Configuration file");
			File file = fileChooser.showSaveDialog(primaryStage);

			try {
				if (null != file) {
					final ObjectMapper mapper = getObjectMapper();
					final ActionList actionListToSerialize = new ActionList(actionList);

					addLog(Level.DEBUG, "Saving configuration to file : " + file.getAbsolutePath());
					mapper.writeValue(file, actionListToSerialize);
				}
			} catch (Exception e) {
				addLog(Level.ERROR, Arrays.toString(e.getStackTrace()));
				throw e;
			} finally {
				activateButtons();
			}

		} else {
			addLog(Level.DEBUG, "No action to save");
			activateButtons();
		}
	}

	private List<AbstractAction> getActionList() {
		final List<AbstractAction> actionList = new ArrayList<>();
		for (Node node : fieldZone.getChildrenUnmodifiable()) {
			if (node instanceof ActionInput) {
				final ActionInput actionInput = (ActionInput) node;
				try {
					actionList.add(actionInput.toAction());
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
		logConsole.getChildren().clear();
	}

	public final void addLog(Level level, String message) {
		if (null != logConsole) {
			String style = "-fx-fill: ";
			switch (level) {
			case ERROR:
				style += "red; -fx-font-weight: bolder;";
				break;
			case WARNING:
				style += "darkorange;-fx-font-weight: bold;";
				break;
			case INFO:
				style += "black;";
				break;
			case DEBUG:
				style += "blueviolet;";
				break;
			default:
				style += "navy;";
				break;
			}
			final Text logText = new Text();
			logText.setStyle(style);
			logText.setText(">> " + level + ", " + message + "\n\n");
			logConsole.getChildren().add(logText);
		}
	}

}
