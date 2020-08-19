package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
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
    private ScrollPane displayPane;
    
    private VBox fieldZone;

    public Stage primaryStage;
    

    public void initialize(){
    	fieldZone = new VBox();
    	fieldZone.setAlignment(Pos.TOP_CENTER);
    	displayPane.setContent(fieldZone);
    }

	public void activateButtons(){
    	addButton.setDisable(false);
    	removeButton.setDisable(false);
    	loadButton.setDisable(false);
    	saveButton.setDisable(false);
    	startButton.setDisable(false);
    }
    
    public void disactiveButtons(){
    	addButton.setDisable(true);
    	removeButton.setDisable(true);
    	loadButton.setDisable(true);
    	saveButton.setDisable(true);
    	startButton.setDisable(true);
    }

    private void addNewInputField() {
    	ActionInput actionInput = new ActionInput();
    	fieldZone.getChildren().add(actionInput);
	}


	private void removeTheLastInputField() {
		if(!fieldZone.getChildren().isEmpty()) {
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
    }
	
	@FXML
    void loadFromFile(ActionEvent event) {
    }
	
	@FXML
    void saveToFile(ActionEvent event) {
    }

}
