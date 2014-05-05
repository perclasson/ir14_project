package awarn.irproject;

import javafx.beans.property.*;

public class Summary {
	
	private StringProperty summary;
	public void setSummary(String value) { summaryProperty().set(value); }
	public String getSummary() { return summaryProperty().get(); }
	public StringProperty summaryProperty() {
	if (summary == null) summary = new SimpleStringProperty(this, "summary");
		return summary;
	}

	private StringProperty link;
	public void setLink(String value) { linkProperty().set(value); }
	public String getLink() { return linkProperty().get(); }
	public StringProperty linkProperty() {
	if (link == null) link = new SimpleStringProperty(this, "link");
		return link;
	}
	
}