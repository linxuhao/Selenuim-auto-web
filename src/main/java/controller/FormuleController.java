package controller;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.FloatProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import spider.SpiderHandler;

public class FormuleController {

    @FXML
    private Button valideButton;

    @FXML
    private TextField urlInput;
    
    @FXML
    private TextField maxNumber;
    
    @FXML
    private Button removeButton;

    @FXML
    private VBox fieldZone;

    @FXML
    private Button addButton;
    
    @FXML
    private ScrollPane displayPane;
    
    @FXML
    private Label message;
    
    private ProgressBar progressBar;
    public Stage primaryStage;
   
    
    private List<String> fields;
    SpiderHandler spiderHandler;

    public void initialize(){
    	this.maxNumber.setText("-1");
        this.fields = new ArrayList<String>();
        //init the default fields
        addNewInputFieldWithIdWithContent(fields.size(),"prix");
        addNewInputFieldWithIdWithContent(fields.size(),"localisation");
        addNewInputFieldWithIdWithContent(fields.size(),"finition");
        addNewInputFieldWithIdWithContent(fields.size(),"Année");
        addNewInputFieldWithIdWithContent(fields.size(),"Kilométrage");
        addNewInputFieldWithIdWithContent(fields.size(),"Puissance fiscale");
        addNewInputFieldWithIdWithContent(fields.size(),"Puissance din");
        addNewInputFieldWithIdWithContent(fields.size(),"Couleur extérieure");
        addNewInputFieldWithIdWithContent(fields.size(),"Garantie");
    }
    
    @FXML
    void valide(ActionEvent event) {
    	//get the fields we want
    	String url = urlInput.getText().trim();
    	Integer max = Integer.parseInt(maxNumber.getText().trim());
    	System.out.println("URL is : " + url);
    	for(int i = 0; i < fieldZone.getChildren().size(); i++){
    		TextField inputField = (TextField) fieldZone.getChildren().get(i);
    		fields.set(i, inputField.getText().trim());
    		System.out.println("Field " + i + " is : " + fields.get(i));
    	}
    	
    	//create a spider handler and ask it to do the job
    	spiderHandler = new SpiderHandler(url, fields, this, max);
    	
    	createAndBindProgressionBar(spiderHandler.CurrentProgressionProperty(), this.displayPane);
        
    	Thread handlerThread = new Thread(spiderHandler, "Spider Handler");
    	handlerThread.start();
    	disactiveButtons();
    }

	private void createAndBindProgressionBar(FloatProperty currentProgressionProperty, ScrollPane parentPane) {
    	progressBar= new ProgressBar();
		progressBar.progressProperty().bind(currentProgressionProperty);
		
		parentPane.setContent(progressBar);
	}

	public void activateButtons(){
    	addButton.setDisable(false);
    	removeButton.setDisable(false);
    	valideButton.setDisable(false);
    }
    
    public void disactiveButtons(){
    	addButton.setDisable(true);
    	removeButton.setDisable(true);
    	valideButton.setDisable(true);
    }

    @FXML
    void add(ActionEvent event) {
    	addNewInputFieldWithIdWithContent(fields.size(),"");
    }

    private void addNewInputFieldWithIdWithContent(int id, String content) {
    	TextField field = new TextField();
    	field.setId(String.valueOf(id));
    	field.setText(content);
    	fieldZone.getChildren().add(field);
    	
    	String fieldContent = new String();
    	fields.add(fieldContent);
	}

	@FXML
    void remove(ActionEvent event) {
    	removeTheLastInputField();
    }

	private void removeTheLastInputField() {
		fieldZone.getChildren().remove(fields.size() - 1);
		fields.remove(fields.size() - 1);
		
	}
	
	public void displayMessage(String message){
		this.message.setText(message);
	}

}
