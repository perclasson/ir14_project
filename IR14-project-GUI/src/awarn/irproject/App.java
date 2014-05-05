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
import javafx.scene.text.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Callback;
import jdk.internal.org.objectweb.asm.tree.analysis.Interpreter;

public class App extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {

		primaryStage.setTitle("Summary search");

		final GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(25, 25, 25, 25));

		Text scenetitle = new Text("Start typing:");
		scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
		grid.add(scenetitle, 0, 0, 2, 1);

		Text webtitle = new Text("Chosen document:");
		webtitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
		grid.add(webtitle, 0, 5, 2, 1);

		final WebView wv = new WebView();
		grid.add(wv, 0, 6, 10, 2);

		final TextField searchTextField = new TextField();
		grid.add(searchTextField, 0, 1, 7, 1);

		final TableView<Summary> table = new TableView<Summary>();
		grid.add(table, 0, 2, 10, 2);

		/**
		 * Create the observable list to which we will add the summaries.
		 */
		List<Summary> summaries = new ArrayList<Summary>();

		final ObservableList<Summary> observableSummaries = FXCollections
				.observableList(summaries);
		observableSummaries.addListener(new ListChangeListener<Summary>() {
			@Override
			public void onChanged(
					ListChangeListener.Change<? extends Summary> change) {

			}
		});

		table.setItems(observableSummaries);

		TableColumn<Summary, String> summaryCol = new TableColumn<>("Summary");
		summaryCol.setMinWidth(600);
		summaryCol.setCellValueFactory(new PropertyValueFactory("summary"));

		TableColumn<Summary, Button> linkCol = new TableColumn<>("Link");
		linkCol.setMinWidth(100);
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
							param.getTableView().getSelectionModel()
									.select(getIndex());
							final Summary summary = table.getSelectionModel()
									.getSelectedItem();
							linkBtn.setText(summary.getLink());

							linkBtn.setOnAction(new EventHandler<ActionEvent>() {

								@Override
								public void handle(ActionEvent event) {
									if (summary != null) {
										// Knapp i rad i tabell
										System.out.println(summary.getLink());

										WebEngine we = wv.getEngine();
										we.load("https://en.wikipedia.org");
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
				System.out.println("Starting search for: "
						+ searchTextField.getText());
				String searchQuery = searchTextField.getText();
				String url = QueryTransformer.interpret(searchQuery.split(" "));
				List<String> summaries = new JsonParser().search(url);

				for (String summary : summaries) {
					Summary newSummary = new Summary();
					newSummary.setSummary(summary);
					newSummary.setLink("Link to Bla bla");
					observableSummaries.add(newSummary);
				}
			}

		});
		grid.add(searchBtn, 7, 1, 2, 1);

		Scene scene = new Scene(grid, 1000, 600);
		primaryStage.setScene(scene);

		primaryStage.show();
	}
}