import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

/**
 * Main entry point for the London Air Pollution app.
 *
 * This class builds the main window and adds the four tabs.
 */
public class App extends Application
{

    /**
     * Create and show the main JavaFX window with the four tabs.
     * @param stage The main 'stage' provided by JavaFX
     */
    @Override
    public void start(Stage stage)
    {
        // create the shared backend used across tabs
        DataManager dm = new DataManager();

        // main layout container for the tabbed interface
        TabPane tabPane = new TabPane();
        
        // keep core assign›ment tabs always visible
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // named variables so tabs/panels can talk to each other.
        //  keep the Map tab as a variable because alert clicks jump back to it.
        MapPanel mapPanel = new MapPanel(dm);
        Tab mapTab = new Tab("Map", mapPanel);
        GridDetailPanel gridPanel = new GridDetailPanel(dm);
        Tab gridTab = new Tab("Grid Detail", gridPanel);
        StatisticsPanel statisticsPanel = new StatisticsPanel(dm);
        Tab statsTab = new Tab("Statistics", statisticsPanel);
        
        // alerts challenge
        AlertPanel alertPanel = new AlertPanel(dm, mapPanel);

        // Live data panel is the API challenge tab
        // It asks DataManager for current NO2/PM10/PM2.5 values
        LiveDataPanel liveDataPanel = new LiveDataPanel(dm);
        
    
     
        //When dot clicked via mappanel class, data transferred t griddetailapnel and current tab view siwtched aswell
        mapPanel.setDotClickListener((point, pollutant, year) -> {
            gridPanel.showDataPoint(point, pollutant, year);
            tabPane.getSelectionModel().select(gridTab);
        });

        // alert click
        alertPanel.setAlertClickListener((point, pollutant, year) -> {
            mapPanel.highlightPoint(point, pollutant, year);
            tabPane.getSelectionModel().select(mapTab);
        });
        
        // Map panel area click.
        mapPanel.setAreaSelectListener((p1, p2) -> {
            statisticsPanel.prefillGridValues(
                String.valueOf(p1.gridCode()),
                String.valueOf(p2.gridCode()));
            tabPane.getSelectionModel().select(statsTab);
        });
        
        // add the four tabs
        tabPane.getTabs().add(new Tab("Welcome", new WelcomePanel()));
        tabPane.getTabs().add(mapTab);
        tabPane.getTabs().add(statsTab);
        tabPane.getTabs().add(new Tab("Alerts", alertPanel));
        // Extra challenge tab
        tabPane.getTabs().add(new Tab("Live Data", liveDataPanel));
        tabPane.getTabs().add(gridTab);

        Scene scene = new Scene(tabPane, 1000, 600);

        java.net.URL styleUrl = getClass().getResource("/style.css");
        if (styleUrl != null) {
            scene.getStylesheets().add(styleUrl.toExternalForm());
        } else {
            scene.getStylesheets().add("style.css");
        }
        
        stage.setScene(scene);
        stage.setTitle("London Air Pollution");
        stage.show();
    }

    /**
     * Launches the JavaFX application.
     */
    public static void main(String[] args)
    {
        launch(args);
    }
}
