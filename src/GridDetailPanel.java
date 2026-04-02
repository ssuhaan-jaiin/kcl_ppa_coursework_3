import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * GridDetailPanel lets users look up detailed pollution data for a
 * specific grid location in London.
 *
 * A grid code can be entered manually via the search box, or the panel
 * can be populated automatically when a point is clicked on the map.
 *
 * It displays the grid code, coordinates, pollutant, year, and value.
 *
 * @author Ssuhaan Jaiin
 */
public class GridDetailPanel extends VBox {

    // Display labels, default to "—"
    private Label gridCodeValue   = new Label("—");
    private Label xValue          = new Label("—");
    private Label yValue          = new Label("—");
    private Label pollutantValue  = new Label("—");
    private Label valueDisplay    = new Label("—");
    private Label colourIndicator = new Label("     ");
    private Label statusLabel     = new Label("");

    private DataManager dataManager;

    // Stored as fields so handleSearch() can read values
    private ComboBox<String> pollutantBox = new ComboBox<>();
    private ComboBox<String> yearBox      = new ComboBox<>();
    private TextField gridTextField       = new TextField();

    public GridDetailPanel(DataManager dataManager) {
        this.dataManager = dataManager;
        this.setStyle("-fx-background-color: #1a1a2e;");
        this.setSpacing(20);
        this.setPadding(new Insets(40));
        this.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        
        // Title
        Label titleLabel = new Label("Grid Details");
        titleLabel.setStyle("-fx-font-size: 42px; -fx-font-weight: bold; -fx-text-fill: #e0e0e0;");

        // Controls
        
        pollutantBox.getItems().addAll(dataManager.getAvailablePollutants());
        pollutantBox.setValue("NO2");

        yearBox.getItems().addAll(dataManager.getAvailableYears());
        yearBox.setValue("2023");

        gridTextField.setPromptText("Enter grid code...");

        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> handleSearch());

        HBox controlsPane = new HBox(15);
        controlsPane.getStyleClass().add("controls-bar");
        controlsPane.getChildren().addAll(
            new Label("Pollutant:"), pollutantBox,
            new Label("Year:"), yearBox,
            gridTextField, searchButton
        );

        
        
        // Main Display

        // Field name labels
        Label gcLabel = new Label("Grid Code:");
        Label xLabel  = new Label("X (Easting):");
        Label yLabel  = new Label("Y (Northing):");
        Label pLabel  = new Label("Pollutant / Year:");
        Label vLabel  = new Label("Pollution Value:");

        gcLabel.getStyleClass().add("field-label");
        xLabel.getStyleClass().add("field-label");
        yLabel.getStyleClass().add("field-label");
        pLabel.getStyleClass().add("field-label");
        vLabel.getStyleClass().add("field-label");

        // Value labels 
        gridCodeValue.getStyleClass().add("value-label");
        xValue.getStyleClass().add("value-label");
        yValue.getStyleClass().add("value-label");
        pollutantValue.getStyleClass().add("value-label");
        valueDisplay.getStyleClass().add("value-label");

        // Arrange into a GridPane
        GridPane dataGrid = new GridPane();
        dataGrid.setHgap(40);
        dataGrid.setVgap(15);
        dataGrid.setPadding(new Insets(20));
        dataGrid.getStyleClass().add("data-grid");
        

        dataGrid.add(gcLabel,        0, 0);
        dataGrid.add(gridCodeValue,  1, 0);
        dataGrid.add(xLabel,         0, 1);
        dataGrid.add(xValue,         1, 1);
        dataGrid.add(yLabel,         0, 2);
        dataGrid.add(yValue,         1, 2);
        dataGrid.add(pLabel,         0, 3);
        dataGrid.add(pollutantValue, 1, 3);
        dataGrid.add(vLabel,         0, 4);
        dataGrid.add(valueDisplay,   1, 4);
        dataGrid.add(colourIndicator, 2, 4);

        
        
        //Status
        statusLabel.getStyleClass().add("status-label");


        //Add all to panel
        this.getChildren().addAll(titleLabel, controlsPane, dataGrid, statusLabel);
    }

    // Reads the search box and dropdowns, then finds and displays the matching point
    private void handleSearch() {
        String inputPollutant = pollutantBox.getValue();
        String inputYear      = yearBox.getValue();
        String inputText      = gridTextField.getText().trim();

        if (inputText.isEmpty()) {
            statusLabel.setText("Please enter a grid code");
            return;
        }
        if (!isNumber(inputText)) {
            statusLabel.setText("Invalid input — numbers only");
            return;
        }

        int searchCode = Integer.parseInt(inputText);

        for (DataPoint point : dataManager.getLondonData(inputPollutant, inputYear)) {
            if (point.gridCode() == searchCode) {
                showDataPoint(point, inputPollutant, inputYear);
                return;
            }
        }

        statusLabel.setText("Grid code not found");
    }

    // Checks that every character in the string is a digit
    private boolean isNumber(String text) {
        for (char c : text.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    // Populates the display with data from a given point — called from the map click or search
    public void showDataPoint(DataPoint point, String pollutant, String year) {

        // Sync dropdowns to match the incoming data
        pollutantBox.setValue(pollutant);
        yearBox.setValue(year);

        gridCodeValue.setText("" + point.gridCode());
        xValue.setText("" + point.x());
        yValue.setText("" + point.y());
        pollutantValue.setText(pollutant + " (" + year + ")");
        valueDisplay.setText(point.value() + " µg/m³");
        statusLabel.setText("Showing data for grid " + point.gridCode());
    }
}
