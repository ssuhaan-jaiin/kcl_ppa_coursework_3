import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.*;
import java.util.*;
import javafx.util.Duration;
import javafx.stage.Popup;
import javafx.scene.layout.GridPane;
import java.util.LinkedHashMap;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;

/**
 * StatisticsPanel: Displays relevant statistics for selected periods and areas.
 * 3 type of statistics available: Average pollution levels, Highest pollution levels, Trends over time.
 * 
 * @Author: Shadid Miah
 * @Version: 1.00
 */
public class StatisticsPanel extends ScrollPane {
    // Field objects
    private DataManager dataManager;
    
    // Fields for panes
    private VBox statisticsPanelVBox = new VBox();
    private BorderPane selectionPane = new BorderPane();    //Border pane containing selection bar
    private HBox selectionYearBar = new HBox(13);
    private HBox selectionAreaBar = new HBox(13);
    private HBox pollutantBar = new HBox(13);
    private HBox selectionYearBar2 = new HBox(13);      // Selection bars for when comparison mode is on
    private HBox selectionAreaBar2 = new HBox(13);
    private HBox pollutantBar2 = new HBox(13);
    private VBox selectionBox2 = new VBox(13);
    private BorderPane statPane = new BorderPane();     // Border pane containing stat panel
    
    // Tracker fields.
    private String selectedStat = "average";
    
    // UI controls
    private ToggleButton comparisonToggle;
    private ComboBox<String> yearBox1 = new ComboBox();     // Controls for default selection box
    private ComboBox<String> yearBox2 = new ComboBox();
    private CheckBox selectedFullMap = new CheckBox("Full map area");   // Checkbox to enable full map area selection
    private ComboBox<String> pollutantBox = new ComboBox();
    private TextField gridValue1 = new TextField();
    private TextField gridValue2 = new TextField();
    private ComboBox<String> yearBox3 = new ComboBox();     // Controls for comparison selection box
    private ComboBox<String> yearBox4 = new ComboBox();
    private CheckBox selectedFullMap2 = new CheckBox("Full map area");
    private ComboBox<String> pollutantBox2 = new ComboBox();
    private TextField gridValue3 = new TextField();
    private TextField gridValue4 = new TextField();
    private Label statusLabel = new Label();
    // Declared as field so that it can be used to switch to averages tab when area is selected on the map.
    private ToggleButton averageLevels; 
    private Button execute;
    
    
    public StatisticsPanel(DataManager dataManager) { 
        this.dataManager = dataManager;
        this.setContent(statisticsPanelVBox);
        this.setFitToHeight(true);
        this.setFitToWidth(true);
        
        //Title of panel
        Label titleLabel = new Label("Statistics");
        titleLabel.setStyle("-fx-font-size: 42px; -fx-font-weight: bold; -fx-text-fill: #e0e0e0;");
        statisticsPanelVBox.getChildren().add(titleLabel);
        
        statisticsPanelVBox.setStyle("-fx-background-color: #1a1a2e;");
        statisticsPanelVBox.setSpacing(20);
        statisticsPanelVBox.setPadding(new Insets(40));
        java.net.URL styleUrl = getClass().getResource("style.css");
        if (styleUrl != null) {
            statisticsPanelVBox.getStylesheets().add(styleUrl.toExternalForm());
        }
        
        //Builds toggle bar used to select statistic to be executed.
        buildToggleBar();
        selectedFullMap.setSelected(true);
        selectedFullMap2.setSelected(true);
        
        //Build selection bar; where the user interacts and selectes their values.
        buildSelectionBar();
        gridValue1.setDisable(true);
        gridValue2.setDisable(true);
        gridValue3.setDisable(true);
        gridValue4.setDisable(true);
        
        // Styling
        statPane.setId("stat-border-pane");
        statPane.setBottom(statusLabel);
        statPane.setPadding(new Insets(10, 0, 5, 0));
        statPane.setMargin(statusLabel, new Insets(10, 0, 0, 0));
        
        statisticsPanelVBox.getChildren().add(selectionPane);
        statisticsPanelVBox.getChildren().add(statPane);
        }
        
    private void buildToggleBar() {
        //Creates a HBox for the toggle bar
        HBox toggleBar = new HBox(13);
        
        ToggleGroup statToggles = new ToggleGroup();
        
        // Depending on the stat currently toggled,
        // Some parts of the selection bar will be made invisible, as there is no use for them.
        averageLevels = new ToggleButton("Average pollution levels");
        averageLevels.setToggleGroup(statToggles);
        averageLevels.setOnAction ( e -> {
            selectedStat = "average";
            setYearBarVisibility(true);
            setAreaBarVisibility(true);
            
        });
        
        ToggleButton highestLevels = new ToggleButton("Highest pollution levels");
        highestLevels.setToggleGroup(statToggles);
        highestLevels.setOnAction ( e ->{
            selectedStat = "highestLevels";
            setYearBarVisibility(true);
            setAreaBarVisibility(false);
        });
        
        ToggleButton trendsOverTime = new ToggleButton("Trends over time");
        trendsOverTime.setToggleGroup(statToggles);
        trendsOverTime.setOnAction ( e -> {
            selectedStat = "trends";
            setYearBarVisibility(false);
            setAreaBarVisibility(false);
        });
        
        comparisonToggle = new ToggleButton("Compare");
        comparisonToggle.setSelected(false);
        setComparisonBarsVisibility(comparisonToggle.isSelected());
        comparisonToggle.setOnAction ( e -> {
             setComparisonBarsVisibility(comparisonToggle.isSelected());
        });
        
        statToggles.selectToggle(averageLevels);
        statToggles.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                statToggles.selectToggle(oldToggle);
            }
        });

        
        toggleBar.getStyleClass().add("toggle-bar");
        
        toggleBar.getChildren().addAll(averageLevels, highestLevels, trendsOverTime);
        statisticsPanelVBox.getChildren().addAll(toggleBar, comparisonToggle);
        
    }
    
    /**
     * Helper method to set visibility of all selectionYearBars 
     * @param visibility True if visible, false if invisible
     */
    private void setYearBarVisibility(boolean visibility) {
        selectionYearBar.setVisible(visibility);
        selectionYearBar.setManaged(visibility);
        if (comparisonToggle.isSelected()){
            selectionYearBar2.setVisible(visibility);
            selectionYearBar2.setManaged(visibility);
        }
        
    }
    
    /**
     * Helper method to set visibility of all selectionAreaBars 
     * @param visibility True if visible, false if invisible
     */
    private void setAreaBarVisibility(boolean visibility) {
        selectionAreaBar.setVisible(visibility);
        selectionAreaBar.setManaged(visibility);
        if (comparisonToggle.isSelected()) {
            selectionAreaBar2.setVisible(visibility);
            selectionAreaBar2.setManaged(visibility);
        }
    }
    
    /**
     * Helper method to set visibility of all bars related to comparison.
     * @param visibility True if visible, false if invisible.
     */
    private void setComparisonBarsVisibility(boolean visibility) {
        selectionBox2.setVisible(visibility);
        selectionBox2.setManaged(visibility);
        // This switch case is so that the comparison selection box syncs with the toggle, 
        // even if the toggle switch happens while slectionBox2 is unmanaged and invisible.
        if (visibility == true) {
            switch (selectedStat) {
            case "average": 
                setYearBarVisibility(true);
                setAreaBarVisibility(true);
                break;
            case "trends":
                setYearBarVisibility(false);
                setAreaBarVisibility(false);
                break;
            case "highestLevels":
                setYearBarVisibility(true);
                setAreaBarVisibility(false);
                break;
            
            }
        }
        
    }
    
    /**
     * Builds a selection bar using a vbox with some hboxes inside it.
     * Hboxes contain some comboBoxes for dropdown menus.
     * If execute is pressed, the execute method is called.
     */   
    private void buildSelectionBar () {
        //Builds a selection bar using a vbox with hboxes inside it.
        //HBoxes contain some comboBoxes for dropdown menus.
        
        VBox selectionBox = new VBox(13);
        HBox center = new HBox(50, selectionBox, selectionBox2);
        
        execute = new Button("Execute");
        execute.setOnAction (e -> {
            execute();
        });
        
        // Controls for selectionBox, this is the default selection box present at all times.
        pollutantBox = buildPollutantBox();
        pollutantBar = buildPollutantBar(pollutantBox);
        
        yearBox1 = buildYearBox();
        yearBox2 = buildYearBox();
        selectionYearBar = buildYearBar(yearBox1, yearBox2);
        
        gridValue1 = buildAreaField();
        gridValue2 = buildAreaField();
        
        selectionAreaBar = buildAreaInputBar(gridValue1, gridValue2);  
        selectionAreaBar.getChildren().add(selectedFullMap);
        
        selectedFullMap.setOnAction( e -> {
            if (selectedFullMap.isSelected()) {
            //If Full map area selected grid values are unnecessary
            gridValue1.setDisable(true);
            gridValue2.setDisable(true);
            }
            else {
                gridValue1.setDisable(false);
                gridValue2.setDisable(false);
            }
        });
        
        selectionBox.getChildren().addAll(selectionYearBar, selectionAreaBar, pollutantBar, execute);
        
        // Controls for selectionBox2, this is the selectionBox that appeasr when compare is toggled on.
        pollutantBox2 = buildPollutantBox();
        pollutantBar2 = buildPollutantBar(pollutantBox2);

        yearBox3 = buildYearBox();
        yearBox4 = buildYearBox();
        selectionYearBar2 = buildYearBar(yearBox3, yearBox4);
        
        gridValue3 = buildAreaField();
        gridValue4 = buildAreaField();
        
        selectionAreaBar2 = buildAreaInputBar(gridValue3, gridValue4);
        selectionAreaBar2.getChildren().add(selectedFullMap2);
        
        selectedFullMap2.setOnAction( e -> {
            if (selectedFullMap2.isSelected()) {
            //If Full map area selected grid values are unnecessary
            gridValue3.setDisable(true);
            gridValue4.setDisable(true);
            }
            else {
                gridValue3.setDisable(false);
                gridValue4.setDisable(false);
            }
        });
        
        selectionBox2.getChildren().addAll(selectionYearBar2, selectionAreaBar2, pollutantBar2);

        // Assign to style classes.
        selectionYearBar.getStyleClass().add("selection-bar");
        selectionAreaBar.getStyleClass().add("selection-bar");
        pollutantBar.getStyleClass().add("selection-bar");
        selectionBox.getStyleClass().add("selection-box");
        selectionYearBar2.getStyleClass().add("selection-bar");
        selectionAreaBar2.getStyleClass().add("selection-bar");
        pollutantBar2.getStyleClass().add("selection-bar");
        selectionBox2.getStyleClass().add("selection-box");
        
        selectionPane.setCenter(center);
        
    }
    
    /**
     * Build an area input bar for inputting grid values.
     * @return The area input bar as a HBox.
     */
    private HBox buildAreaInputBar(TextField field1, TextField field2) {
        HBox bar = new HBox(13);
        bar.getChildren().addAll(
            new Label("Area: "),
            field1,
            new Label(" to "),
            field2);
        return bar;
    }
    
    /**
     * Build an area box to input a grid value.
     * @return The area box as a textField.
     */
    private TextField buildAreaField() {
        TextField field = new TextField();
        field.setText("");
        field.setPromptText("Enter diagonal grid value"); 
        return field;
        
    }
    
    /**
     * Build a year box.
     * @return The year box as a combo box.
     */
    private ComboBox buildYearBox() {
        ComboBox box = new ComboBox();
        box.getItems().addAll(dataManager.getAvailableYears());
        box.setValue("2018");
        return box;
        
    }
    
    /**
     * Build a year bar.
     * @return The year bar as a HBox.
     * @param The year box.
     * @param The second year box.
     */
    private HBox buildYearBar(ComboBox yearBox1, ComboBox yearBox2) {
        HBox bar = new HBox(13);
        bar.getChildren().addAll(
            new Label("Year:"),
            yearBox1,
            new Label(" to "),
            yearBox2);
        
        return bar;
        
    }
    
    /**
     * Build a pollutant box.
     * @return The comboBox built.
     */
    private ComboBox buildPollutantBox() {
        ComboBox box = new ComboBox();
        box.getItems().addAll(dataManager.getAvailablePollutants());
        box.setValue("NO2");
            
        return box;
    }
    
    /**
     * Build a pollutant bar.
     * @param The combo box that is going to be in the pollutant bar.
     * @return The bar as a HBox.
     */
    private HBox buildPollutantBar(ComboBox pollutantBox) {
        HBox bar = new HBox(13);
        bar.getChildren().addAll(
        new Label("Pollutant:"),
        pollutantBox);
        
        return bar;
    }
    
    /**
     * Method to reroute to an appropriate method depending on the options picked by the user.
     */
    private void execute() {
        // display stat according to seletecteStat.
        switch (selectedStat) {
            case "average": 
                displayAverageLevels();
                break;
            case "trends":
                createTrendGraphView();
                break;
            case "highestLevels":
                displayHighestPollution();
                break;
            
        }
    }
    
    /**
     * Displays statistics for average pollution levels over selected period and grid area, for the selected pollutant
     * If full map selected then grid values are ignored.
     */
    private void displayAverageLevels() {
        statusLabel.setText("");
        
        LinkedHashMap<String, Integer> selectedValues = validateInput();
        if (selectedValues == null) {
            return;
        }
        // Builds a stat view made of a grid pane and a title label to display results for average pollution levels.
        StatViewBuilder averagePollutionView = new StatViewBuilder(selectedValues, dataManager);
        Map.Entry<Label, GridPane> gridUnit = 
            averagePollutionView.createAverageLevelsView(comparisonToggle.isSelected(), pollutantBox, pollutantBox2, 
                selectedFullMap.isSelected(), selectedFullMap2.isSelected());
        if (gridUnit == null) {
            statusLabel.setText("Failed to match grid value to data point. Invalid grid values");
            return;
        }
        GridPane statGrid = gridUnit.getValue();
        Label statTitle = gridUnit.getKey();
        
        statPane.setMargin(statTitle, new Insets(0, 0, 10, 0));
        statPane.setTop(statTitle);
        statPane.setCenter(statGrid);
        statusLabel.setText("Calculation completed");
    }
    
    
    /**
     * Displays statistics for highest pollution levels over selected period, for selected pollutant.
     */
    private void displayHighestPollution() {
        statusLabel.setText("");
        LinkedHashMap<String, Integer> selectedValues = validateInput();
        if (selectedValues == null) {
            return;
        }
        // Builds a stat view made of a grid pane and a title label to display results for highest pollution levels.
        StatViewBuilder highestPollutionView = new StatViewBuilder(selectedValues, dataManager);
        Map.Entry<Label, GridPane> gridUnit = 
            highestPollutionView.createHighestPollutionView(comparisonToggle.isSelected(), pollutantBox, pollutantBox2);
        if (gridUnit == null) {
            statusLabel.setText("Error in retrieving values");
            return;
        }
        GridPane statGrid = gridUnit.getValue();
        Label statTitle = gridUnit.getKey();
        
        statPane.setMargin(statTitle, new Insets(0, 0, 10, 0));
        statPane.setTop(statTitle);
        statPane.setCenter(statGrid);
        statusLabel.setText("Calculation Complete");
    
    }
    
    /**
     * Creates a trend graph using the trend map obtained from the dataManager object.
     * Uses lineChart for the graph.
     */
    private void createTrendGraphView() {
        HBox graphContainer = new HBox(10);
        LineChart<String, Number> trendChart = createTrendGraph(pollutantBox.getValue());
        graphContainer.getChildren().add(trendChart);
        if (comparisonToggle.isSelected()) {
            LineChart<String, Number> trendChart2 = createTrendGraph(pollutantBox2.getValue());
            graphContainer.getChildren().add(trendChart2);
        }
        Label statTitle = new Label("Trend graph");
        statPane.setTop(statTitle);
        statPane.setCenter(graphContainer);
        
    }
    
    /**
     * Creates a trend graph with a year range and pollutant selected by the user
     */
    private LineChart<String, Number> createTrendGraph(String pollutant) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> trendChart = new LineChart<String, Number>(xAxis,yAxis);
        xAxis.setLabel("Year");     // Naming the axes
        yAxis.setLabel("Average Pollution Level");        
        trendChart.setTitle("Pollution trend"); // Set trendChart title  
        
        //defining series
        XYChart.Series<String, Number> series = new XYChart.Series();  // This series only accepts String, Numer pairs.
        series.setName(pollutant);
        
        
        Map<String, Double> trendValues = dataManager.getTrend(pollutant);    // Use data manager to get trend values
        
        //populating the series with data
        for (Map.Entry<String, Double> yearTrend : trendValues.entrySet()) {
            series.getData().add(new XYChart.Data<String, Number>
            (String.valueOf(yearTrend.getKey()), yearTrend.getValue())); 
        }
        
        trendChart.getData().add(series);
        return trendChart;
        }
    
    /**
     * Method for when areaSelectListener is triggered. Fills grid values fields with grid codes from the data
     * point selected in map panel.
     * @param gridCode1 The grid code of the first data point selected.
     * @param gridCode2 The grid code of the second data point selected.
     */ 
    public void prefillGridValues(String gridCode1, String gridCode2) {
        // Sets selected stat to average and sets toggle to average levels.
        selectedStat = "average";
        averageLevels.setSelected(true);
        setYearBarVisibility(true);
        setAreaBarVisibility(true);
        selectedFullMap.setSelected(false);
        gridValue1.setDisable(false);
        gridValue2.setDisable(false);
        // Fills in grid codes with selected area values.
        gridValue1.setText(gridCode1);
        gridValue2.setText(gridCode2);
        statusLabel.setText("Area pre-filled from map. Press Execute to calculate.");
    }
        
    /**
     * Validates input from selection bar options.
     * @return The hash map created using the values selected by the user.
     */
    private LinkedHashMap<String, Integer> validateInput() {
        // Checks if a grid values has been entered if full map hasn't been selected..
        if (selectedStat.equals("average")) {
            if(selectedFullMap.isSelected() == false 
            && (gridValue1.getText().trim().isEmpty() || gridValue2.getText().trim().isEmpty())) {
            // Performs checks to make sure grid values have been inputted
            statusLabel.setText("Please enter both grid values");
            return null;
            }
            if ((comparisonToggle.isSelected() && selectedFullMap2.isSelected() == false)
                && (gridValue3.getText().trim().isEmpty() || gridValue4.getText().trim().isEmpty())){
                // Performs checks to make sure grid values have been inputted
                statusLabel.setText("Please enter both grid values");
                return null;
            }
        } 
        
        Integer year1, year2;
        Integer year3 = null;
        Integer year4 = null;

        Integer gridV1 = -1;
        Integer gridV2 = -1;
        Integer gridV3 = -1;
        Integer gridV4 = -1;
        try {
            //Attempt to convert selected year dropdown box values to int
            year1 = Integer.parseInt(yearBox1.getValue());
            year2 = Integer.parseInt(yearBox2.getValue());
            if (comparisonToggle.isSelected()) {
                year3 = Integer.parseInt(yearBox3.getValue());
                year4 = Integer.parseInt(yearBox4.getValue());
                }
            }
            catch (NumberFormatException e) {
                statusLabel.setText("Invalid years");
                return null;
            }
        
        // Try catch block for default selection box values.
        if (!selectedFullMap.isSelected() && selectedStat.equals("average")) {
            try {
            //Attempt to convert selected textfield values to int
            gridV1 = Integer.parseInt(gridValue1.getText().trim());
            gridV2 = Integer.parseInt(gridValue2.getText().trim());
            if (String.valueOf(gridV1).length() != 6 || String.valueOf(gridV2).length() != 6) {
                statusLabel.setText("Invalid length. Must be 6 digits");    // Grid values are always 6 digits.
                return null;
            }
            }
            catch (NumberFormatException e) {
                statusLabel.setText("Failed to match grid value to data point. Invalid grid values");
                return null;
            }
        }
        
        // Try catch for comparison box values
        if (!selectedFullMap2.isSelected() && selectedStat.equals("average")) {
            try {
            //Attempt to convert selected textfield values to int 
            gridV3 = Integer.parseInt(gridValue3.getText().trim());
            gridV4 = Integer.parseInt(gridValue4.getText().trim());
            if (comparisonToggle.isSelected() &&
                (String.valueOf(gridV3).length() != 6 || String.valueOf(gridV4).length() != 6)) {
                statusLabel.setText("Invalid length. Must be 6 digits");    // Grid values are always 6 digits.
                return null;
            }
            }
            catch (NumberFormatException e) {
                statusLabel.setText("Failed to match grid value to data point. Invalid grid values");
                return null;
            }
        }
        
        //Creates a linkedHashMap to be used to retrieve selected values.
        LinkedHashMap<String, Integer> selectedValues = new LinkedHashMap();
        selectedValues.put("year1", year1.intValue());
        selectedValues.put("year2", year2.intValue());
        selectedValues.put("gridV1", gridV1.intValue());
        selectedValues.put("gridV2", gridV2.intValue());
        
        if (comparisonToggle.isSelected()) {
            selectedValues.put("gridV3", gridV3.intValue());
            selectedValues.put("gridV4", gridV4.intValue());
            selectedValues.put("year3", year3.intValue());
            selectedValues.put("year4", year4.intValue());
        }
        statPane.setBottom(statusLabel);
        return selectedValues;
    }
}
