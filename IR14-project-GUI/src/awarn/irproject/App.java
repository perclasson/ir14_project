package awarn.irproject;

import java.util.ArrayList;
import java.util.List;

import javafx.application.*;
import javafx.collections.*;
import javafx.event.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Callback;

public class App extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
    	
        primaryStage.setTitle("Summary search");
        
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        
        Text scenetitle = new Text("Start typing:");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);

        TextField userTextField = new TextField();
        grid.add(userTextField, 0, 1);
        
        TableView<Summary> table = new TableView<Summary>();
        grid.add(table, 0, 2, 4, 2);

        
        List<Summary> summaries = new ArrayList<Summary>();
 
	    ObservableList<Summary> observableSummaries = FXCollections.observableList(summaries);
	    observableSummaries.addListener(new ListChangeListener<Summary>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Summary> change) {
                                
            }
        });
	    
	    table.setItems(observableSummaries);

	    TableColumn<Summary,String> summaryCol = new TableColumn<>("Summary");
	    summaryCol.setCellValueFactory(new PropertyValueFactory("summary"));
	    TableColumn<Summary,Button> linkCol = new TableColumn<>("Link");
	    
	    Callback<TableColumn<Summary, Button>, TableCell<Summary, Button>> linkColumnCellFactory = //
                new Callback<TableColumn<Summary, Button>, TableCell<Summary, Button>>() {

            @Override
            public TableCell call(final TableColumn param) {
                final TableCell cell = new TableCell() {

                    @Override
                    public void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            final Button linkBtn = new Button("print name");
                            param.getTableView().getSelectionModel().select(getIndex());
                            Summary summary = table.getSelectionModel().getSelectedItem();
                            linkBtn.setText(summary.getLink());
                            
                            linkBtn.setOnAction(new EventHandler<ActionEvent>() {
                            	
                                @Override
                                public void handle(ActionEvent event) {
                                	if (summary != null) {
                                        System.out.println(summary.getLink());
                                    }
                                }
                            });
                            setGraphic(linkBtn);
                            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                        }
                    }
                };
                return cell;
            }
        };
        linkCol.setCellFactory(linkColumnCellFactory);
	    
	    table.getColumns().setAll(summaryCol, linkCol);
        
        Button searchBtn = new Button();
        searchBtn.setText("Search");
        searchBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	/*
            	 * TODO: Get results of search here.
            	 */
            	System.out.println("Starting search...");
            	
            	//
            	Summary s1 = new Summary();
            	s1.setSummary("Bla bla\nbla");
            	s1.setLink("Link to Bla bla");
            	observableSummaries.add(s1);
            	
            	System.out.println("Done.");
            }
            
        });
        grid.add(searchBtn, 2, 1);

        Scene scene = new Scene(grid, 500, 350);
        primaryStage.setScene(scene);
        
        primaryStage.show();
    }
}