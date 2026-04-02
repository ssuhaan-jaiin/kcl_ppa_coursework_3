import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * WelcomePanel is the first screen users see when they open the application.
 * It gives a brief overview of the tool, lists the group members,
 * and provides information buttons for each panel that explain how to use them.
 * It also includes a colour legend for the pollution levels shown on the map.
 *
 * @author Ssuhaan Jaiin
 */
public class WelcomePanel extends BorderPane {

    public WelcomePanel() {

        // Main Pane Setup
        this.setPadding(new Insets(40));
        this.getStyleClass().add("border-pane");

        // Top Portion - Title
        Label titleLabel = new Label("AirWatch London");
        titleLabel.setStyle("-fx-font-size: 42px; -fx-font-weight: bold; -fx-text-fill: #e0e0e0;");

        
        // Center Portion - Description

        // Short description of what the app does
        Label descriptionLabel = new Label("""
                A data visualisation tool built using DEFRA
                (UK Department for Environment, Food and Rural Affairs)
                air pollution data for London.

                Covers 3 pollutants — Nitrogen Dioxide (NO2),
                Particulate Matter (PM10 and PM2.5)
                across 6 years from 2018 to 2023.

                Select a tab from the left to learn how to navigate the app.
                """);

        // Group member credits
        Label groupHeading = new Label("Group Members");
        groupHeading.getStyleClass().add("heading-label");

        Label groupText = new Label("""
                M.Qasim Imran   —  Map Panel
                Shadid Miah     —  Statistics Panel
                Ssuhaan Jaiin   —  Welcome, Grid Detail, Alerts
                Mark Juresh     —  Data Backend, App & Testing
                """);

        VBox centerContent = new VBox(20);
        centerContent.setPadding(new Insets(20));
        centerContent.getChildren().addAll(descriptionLabel, groupHeading, groupText);

        
        
        // Left Portion - Pop Up Buttons

        // Each button opens a popup explaining how that panel works

        Label tabContentHeading = new Label("Panel Information");
        tabContentHeading.getStyleClass().add("heading-label");

        
        // Map Button
        Button mapButton = new Button("Map Panel");
        Alert mapAlert = new Alert(Alert.AlertType.INFORMATION);
        mapAlert.setTitle("About Map Panel");
        mapAlert.setHeaderText("The Map Panel");
        mapAlert.setContentText("""
                The Map Panel displays air pollution data across London.
            
                Static Mode:
                  - Select pollutant and year to view historical data
                  - Hover over points for details, click to explore further
                  - Right click two points to analyse an area
            
                Live Mode:
                  - Toggle to view real-time pollution data
                  - Hover over markers to see current readings
            
                This panel supports both data exploration and live monitoring.
                """);
        mapButton.setOnAction(e -> mapAlert.showAndWait());

        
        // Statistics Button
        Button statsButton = new Button("Statistics Panel");
        Alert statsAlert = new Alert(Alert.AlertType.INFORMATION);
        statsAlert.setTitle("About Statistics Panel");
        statsAlert.setHeaderText("The Statistics Panel");
        statsAlert.setContentText("""
                The Statistics Panel provides tools to analyse London air pollution data.
            
                Available analyses:
                  - Average levels
                  - Highest levels
                  - Trends over time (graph)
            
                Choose a pollutant, year range, and area (or full map),
                then press Execute to generate results.
            
                You can also enable comparison mode to compare two datasets
                or use areas selected directly from the Map panel.
                """);
        statsButton.setOnAction(e -> statsAlert.showAndWait());

        
        // Grid Button
        Button gridButton = new Button("Grid Detail Panel");
        Alert gridAlert = new Alert(Alert.AlertType.INFORMATION);
        gridAlert.setTitle("About Grid Detail Panel");
        gridAlert.setHeaderText("The Grid Detail Panel");
        gridAlert.setContentText("""
                The Grid Details Panel lets you view pollution data for a specific location in London.
            
                Enter a grid code or click a point on the map to load its data.
            
                It displays:
                  - Grid code
                  - Coordinates (Easting and Northing)
                  - Selected pollutant and year
                  - Pollution value
            
                Use the dropdowns to change the pollutant or year,
                then press Search to find a specific grid location.
                """);
        gridButton.setOnAction(e -> gridAlert.showAndWait());

        
        
        // Alerts Button
        Button alertsButton = new Button("Alerts Panel");
        Alert alertsAlert = new Alert(Alert.AlertType.INFORMATION);
        alertsAlert.setTitle("About Alerts Panel");
        alertsAlert.setHeaderText("The Alerts Panel");
        alertsAlert.setContentText("""
                The Alerts tab scans all London data for pollution hotspots.

                Use the three sliders to set your own threshold for each pollutant:
                  - NO2 threshold (µg/m³)
                  - PM10 threshold (µg/m³)
                  - PM2.5 threshold (µg/m³)

                Press Scan to find every location that exceeds your thresholds.
                Results appear in a list showing:
                  - Grid code and which pollutant exceeded the threshold
                  - The actual value and how far above the threshold it is

                Click any result to highlight that location on the Map tab.
                """);
        alertsButton.setOnAction(e -> alertsAlert.showAndWait());
        
        
        
        
        // Live Button
        Button liveButton = new Button("Live Data Panel");
        Alert liveAlert = new Alert(Alert.AlertType.INFORMATION);
        liveAlert.setTitle("About Live Data Panel");
        liveAlert.setHeaderText("The Live Data Panel");
        liveAlert.setContentText("""
            The Live Data Panel displays real time air pollution levels in London.
        
            Data is retrieved from an external API and includes:
              - NO2 (µg/m³)
              - PM10 (µg/m³)
              - PM2.5 (µg/m³)
        
            Press the refresh button to update the latest readings.
        
            If values show as unavailable, the API may be offline
            or there may be no internet connection.
            """);
        liveButton.setOnAction(e -> liveAlert.showAndWait());
        
        
        
        
        

        VBox tabContent = new VBox(20);
        tabContent.getChildren().addAll(tabContentHeading, mapButton, statsButton, alertsButton, gridButton, liveButton);
        tabContent.getStyleClass().add("side-panel");

        
        
        
    
        // Right Portion - Colour Legend

        Label legendHeading = new Label("Pollution Level Colour Guide");
        legendHeading.getStyleClass().add("heading-label");

        Label legendText = new Label("""
                🟢  Green   —  Low          (good air quality)
                🟡  Yellow  —  Moderate     (acceptable levels)
                🟠  Orange  —  High         (unhealthy for sensitive groups)
                🔴  Red     —  Very High    (unhealthy for all)
                """);

        VBox rightContent = new VBox(20);
        rightContent.getChildren().addAll(legendHeading, legendText);
        rightContent.getStyleClass().add("side-panel");
        
        

        // Bottom Portion - Credits

        Label sourceText = new Label("Data source: uk-air.defra.gov.uk");

        
        
        // Assign sections to Border Pane

        this.setTop(titleLabel);
        this.setLeft(tabContent);
        this.setCenter(centerContent);
        this.setRight(rightContent);
        this.setBottom(sourceText);
    }
}