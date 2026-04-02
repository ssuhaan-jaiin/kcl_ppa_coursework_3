import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;
import java.util.ArrayList;

/**
 * AlertPanel displays a pollution alert system for London air quality data.
 * Users can set custom thresholds for NO2, PM10, and PM2.5 using sliders,
 * then scan all available data to find locations that exceed those limits.
 *
 * Results are shown in a scrollable list, and clicking an entry highlights
 * the location on the map via an AlertClickListener.
 *
 * @author Ssuhaan Jaiin
 */
public class AlertPanel extends VBox {

    // Data source
    private DataManager dataManager;

    // Threshold sliders for each pollutant
    private Slider no2Slider  = new Slider(0, 100, 40);
    private Slider pm10Slider = new Slider(0, 80, 25);
    private Slider pm25Slider = new Slider(0, 60, 15);

    // Results list and status label
    private ListView<String> alertList = new ListView<>();
    private Label summaryLabel = new Label("No scan run yet");

    private record AlertMatch(DataPoint point, String pollutant, String year) {}
    // Stores the DataPoints matching the list entries, used for map highlighting
    private List<AlertMatch> alertPoints = new ArrayList<>();
    private AlertClickListener alertClickListener;

    public AlertPanel(DataManager dataManager, MapPanel mapPanel) {
        this.dataManager = dataManager;
        this.setSpacing(20);
        this.setPadding(new Insets(40));
        this.setStyle("-fx-background-color: #1a1a2e;");
        this.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        // Title
        Label titleLabel = new Label("Pollution Alerts");
        titleLabel.setStyle("-fx-font-size: 42px; -fx-font-weight: bold; -fx-text-fill: #e0e0e0;");

        
        // Sliders

        // Show tick labels
        no2Slider.setShowTickLabels(true);
        pm10Slider.setShowTickLabels(true);
        pm25Slider.setShowTickLabels(true);

        // Set value labels 
        Label no2Val  = new Label("40.0");
        Label pm10Val = new Label("25.0");
        Label pm25Val = new Label("15.0");

        // Read values from sliders and dksplay them
        no2Slider.valueProperty().addListener((obs, old, val) -> no2Val.setText(String.format("%.1f", val.doubleValue())));
        pm10Slider.valueProperty().addListener((obs, old, val) -> pm10Val.setText(String.format("%.1f", val.doubleValue())));
        pm25Slider.valueProperty().addListener((obs, old, val) -> pm25Val.setText(String.format("%.1f", val.doubleValue())));

        // Create a slider box for all particle types
        HBox no2Row  = new HBox(10, new Label("NO2 threshold:"),   no2Slider,  no2Val);
        HBox pm10Row = new HBox(10, new Label("PM10 threshold:"),  pm10Slider, pm10Val);
        HBox pm25Row = new HBox(10, new Label("PM2.5 threshold:"), pm25Slider, pm25Val);

        VBox slidersBox = new VBox(15, no2Row, pm10Row, pm25Row);
        slidersBox.setPadding(new Insets(15));
        slidersBox.setStyle("-fx-background-color: #16213e;");

        
        
        // Scan Bar

        Button scanButton = new Button("Scan for Alerts");
        scanButton.setOnAction(e -> handleScan());

        summaryLabel.getStyleClass().add("status-label");

        HBox scanBar = new HBox(20, scanButton, summaryLabel);
        scanBar.setPadding(new Insets(10, 0, 10, 0));

        // Results List

        alertList.setStyle("-fx-background-color: #16213e; -fx-control-inner-background: #16213e;");

        // Clicking a result highlights the matching point on the map
        alertList.setOnMouseClicked(e -> {
            int selectedIndex = alertList.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                AlertMatch selected = alertPoints.get(selectedIndex);
                if (alertClickListener != null) {
                    alertClickListener.onAlertClicked(selected.point(), selected.pollutant(), selected.year());
                }
            }
        });

        // Adding all elements to the panel

        this.getChildren().addAll(titleLabel, slidersBox, scanBar, alertList);
    }

    // Runs when the Scan button is pressed and checks every data point against thresholds
    private void handleScan() {
        double no2Threshold  = no2Slider.getValue();
        double pm10Threshold = pm10Slider.getValue();
        double pm25Threshold = pm25Slider.getValue();

        List<String> results = new ArrayList<>();
        alertPoints.clear();

        for (String pollutant : dataManager.getAvailablePollutants()) {

            // Match the pollutant to its corresponding threshold
            double threshold;
            if (pollutant.equals("NO2")) {
                threshold = no2Threshold;
            } else if (pollutant.equals("PM10")) {
                threshold = pm10Threshold;
            } else {
                threshold = pm25Threshold;
            }

            for (String year : dataManager.getAvailableYears()) {
                for (DataPoint point : dataManager.getLondonData(pollutant, year)) {
                    if (point.value() > threshold) {

                        // Build the result string shown in the list
                        double excess = point.value() - threshold;
                        String entry = "Grid: " + point.gridCode()
                            + "  |  " + pollutant
                            + "  |  " + year
                            + "  |  " + point.value() + " µg/m³"
                            + "  |  +" + String.format("%.2f", excess)
                            + " above threshold";

                        results.add(entry);
                        alertPoints.add(new AlertMatch(point, pollutant, year));
                    }
                }
            }
        }

        alertList.getItems().clear();
        alertList.getItems().addAll(results);
        summaryLabel.setText("Found " + results.size() + " alerts");
    }

    // Allows other classes to register a listener for when an alert is clicked
    public void setAlertClickListener(AlertClickListener listener) {
        this.alertClickListener = listener;
    }
}
