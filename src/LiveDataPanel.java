import java.util.Map;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * LiveDataPanel shows current London pollution values from an external API.
 * This panel only handles display and refresh button behaviour.
 */
public class LiveDataPanel extends VBox
{
    private final DataManager dataManager;

    private final Label statusLabel;
    private final Label no2ValueLabel;
    private final Label pm10ValueLabel;
    private final Label pm25ValueLabel;

    public LiveDataPanel(DataManager dataManager)
    {
        this.dataManager = dataManager;

        setStyle("-fx-background-color: #1a1a2e;");
        getStyleClass().add("live-panel");
        setSpacing(12);
        setPadding(new Insets(20));

        Label title = new Label("Live London Air Quality");
        title.getStyleClass().add("live-title");

        Label info = new Label(
            "Current values from Open-Meteo API.\n"
            + "If internet is unavailable, values will show as unavailable");
        info.getStyleClass().add("live-info");

        no2ValueLabel = new Label("NO2: loading...");
        pm10ValueLabel = new Label("PM10: loading...");
        pm25ValueLabel = new Label("PM2.5: loading...");
        no2ValueLabel.getStyleClass().add("live-value");
        pm10ValueLabel.getStyleClass().add("live-value");
        pm25ValueLabel.getStyleClass().add("live-value");

        statusLabel = new Label("");
        statusLabel.getStyleClass().add("live-status");

        Button refreshButton = new Button("Refresh Live Data");
        refreshButton.setOnAction(e -> refreshLiveData());

        getChildren().addAll(
            title,
            info,
            no2ValueLabel,
            pm10ValueLabel,
            pm25ValueLabel,
            refreshButton,
            statusLabel);

        refreshLiveData();
    }

    private void refreshLiveData()
    {
        Map<String, Double> liveData = dataManager.getCurrentLiveData();

        double no2 = liveData.get("NO2");
        double pm10 = liveData.get("PM10");
        double pm25 = liveData.get("PM2.5");

        no2ValueLabel.setText("NO2: " + formatValue(no2));
        pm10ValueLabel.setText("PM10: " + formatValue(pm10));
        pm25ValueLabel.setText("PM2.5: " + formatValue(pm25));

        if (Double.isNaN(no2) && Double.isNaN(pm10) && Double.isNaN(pm25)) {
            statusLabel.setText("Status: Could not load live data (offline or API unavailable)");
        }
        else {
            statusLabel.setText("Status: Live data loaded");
        }
    }

    private String formatValue(double value)
    {
        if (Double.isNaN(value)) {
            return "unavailable";
        }
        return String.format("%.2f ug/m3", value);
    }
}
