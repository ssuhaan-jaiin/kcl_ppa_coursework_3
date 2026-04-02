import java.util.LinkedHashMap;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import java.util.LinkedHashMap;
import java.util.*;

/**
 * Class to create a view of the highest pollution stats using a LinkedHasMap of values.
 *
 * @author (Shadid Miah)
 * @version (1.00)
 */
public class StatViewBuilder
{
    // instance variables - replace the example below with your own
    private LinkedHashMap<String, Integer> selectedValues;
    private DataManager dataManager;
    private GridPane statGrid = new GridPane();
    private Label statTitle = new Label();

    /**
     * Constructor for objects of class highestPollutionView
     */
    public StatViewBuilder(LinkedHashMap<String, Integer> selectedValues, DataManager dataManager)
    {
        // initialise instance variables
        this.dataManager = dataManager;
        this.selectedValues = selectedValues;
        
        statGrid.setHgap(15);
        statGrid.setVgap(20);
    }

    /**
     * Creates a view for highest pollution stats using a gridpane a label, and the given paramaters.
     *
     * @param  comparisonToggle The toggle button that sets comparison mode
     * @param pollutantBox The pollutant combo box user selects from.
     * @param pollutantBox2 The second pollutant combo box user selects from.
     * @return The map entry containing the title label and the stat gridpane.
     */
    public Map.Entry<Label, GridPane> createHighestPollutionView(boolean comparisonToggle, ComboBox<String> pollutantBox, ComboBox<String> pollutantBox2)
    {

        statGrid.getChildren().clear();
        int year1, year2;
        int year3 = 2018;   // Placeholder values
        int year4 = 2018;
        if (selectedValues != null) {
            year1 = selectedValues.get("year1");
            year2 = selectedValues.get("year2");
            if (comparisonToggle) {
                year3 = selectedValues.get("year3");
                year4 = selectedValues.get("year4");
            }
        }
        else {
            return null;
        }
        
        // Calculates highest pollution levels and their location using StatsCalculator object for default selection box.
        StatsCalculator statsCalc = new StatsCalculator (pollutantBox.getValue(), year1, year2, dataManager);
        Map.Entry<Integer, Double> Stat = statsCalc.calcHighestLevels();
        int pollutantLocation = Stat.getKey();
        Double highestLevel = Stat.getValue();
        
        // Calculates highest pollution levels and location for comparison selection box.
        if (comparisonToggle) {
            // Get highest pollution level for selected years.
            StatsCalculator statsCalc2 = new StatsCalculator (pollutantBox2.getValue(), year3, year4, dataManager);
            Map.Entry<Integer, Double> Stat2 = statsCalc2.calcHighestLevels();
            int pollutantLocation2 = Stat2.getKey();
            Double highestLevel2 = Stat2.getValue();
            // sets statTitle to be used in statPane and fills statGrid with results.
            statTitle.setText("Comparison of highest pollution levels for " 
            + pollutantBox.getValue() + " from " + year1 + " to " + year2 + " " +
            "compared to " + pollutantBox2.getValue() + " from " + year3 + " to " + year4 + ".");
            
            statGrid.add(new Label("Compared to"), 0, 2);
            statGrid.add(new Label(pollutantBox2.getValue() + ":"), 0, 3);
            statGrid.add(new Label(String.format("%.4f", highestLevel2)), 1, 3);
            statGrid.add(new Label(String.valueOf(pollutantLocation2)), 2, 3);
        }
        else {
            statTitle.setText("Stats for highest pollution levels from " + year1 + " to " + year2);
        }
        // Fills statGrid with results.
        statGrid.add(new Label("Pollutant"), 0, 0);
        statGrid.add(new Label("Stat"), 1, 0);
        statGrid.add(new Label("Location"), 2, 0);
        statGrid.add(new Label(pollutantBox.getValue() + ":"), 0, 1);
        statGrid.add(new Label(String.format("%.4f", highestLevel)), 1, 1);
        statGrid.add(new Label(String.valueOf(pollutantLocation)), 2, 1);
        
        return Map.entry(statTitle, statGrid);
    }
    
    /**
     * Creates a view for highest pollution stats using a gridpane a label, and the given paramaters.
     *
     * @param  comparisonToggle The toggle button that sets comparison mode
     * @param pollutantBox The pollutant combo box user selects from.
     * @param pollutantBox2 The second pollutant combo box user selects from.
     * @return The map entry containing the title label and the stat gridpane.
     */
    public Map.Entry<Label, GridPane> createAverageLevelsView(boolean comparisonToggle, ComboBox<String> pollutantBox, ComboBox<String> pollutantBox2, 
        boolean fullMap, boolean fullMap2) {
        statGrid.getChildren().clear();
        // Initialise variables
        int year1, year2; // Placeholder values
        int year3 = 2018; 
        int year4 = 2018;
        int gridV1 = -1;
        int gridV2 = -1;
        int gridV3 = -1;
        int gridV4 = -1;
        double average, average2;
        if (selectedValues != null) {
            year1 = selectedValues.get("year1");
            year2 = selectedValues.get("year2");
            gridV1 = selectedValues.get("gridV1");
            gridV2 = selectedValues.get("gridV2");
            if (comparisonToggle) {
                year3 = selectedValues.get("year3");
                year4 = selectedValues.get("year4");
                gridV3 = selectedValues.get("gridV3");
                gridV4 = selectedValues.get("gridV4");
            }
                
        }
        else {
            return null;
        }
        
        // Calculates average for pollutant in default box using StatsCalculator object method.
        StatsCalculator statsCalc = new StatsCalculator (pollutantBox.getValue(), year1, year2, dataManager);
        statsCalc.matchGridValue(gridV1, gridV2);
        statsCalc.setFullMapSelected(fullMap);
        average = statsCalc.calculateAverage();
        if (average == -1) {
            return null;
        }
        
        // Calculates average for pollutant in default box using StatsCalculator object method.
        if (comparisonToggle) {
            // If comparison is toggled, then attempt to calculate stat for values from the comparison box.
            StatsCalculator statsCalc2 = new StatsCalculator (pollutantBox2.getValue(), year3, year4, dataManager);
            statsCalc2.matchGridValue(gridV3, gridV4);
            statsCalc2.setFullMapSelected(fullMap2);
            average2 = statsCalc2.calculateAverage();
            if (average2 == -1) {
                return null;
            }
            // Sets title to be used in statPane and fills statGrid with results.
            statTitle.setText("Comparison of average pollution levels for " 
            + pollutantBox.getValue() + " from " + year1 + " to " + year2 + " " +
            "compared to " + pollutantBox2.getValue() + " from " + year3 + " to " + year4 + ".");
            statGrid.add(new Label("Compared to"), 0, 2);
            statGrid.add(new Label(pollutantBox2.getValue() + ":"), 0, 3);
            statGrid.add(new Label(String.format("%.4f", average2)), 1, 3);
        }
        else {
            statTitle.setText("Stats for average pollution levels from " + year1 + " to " + year2);
        }
        // Fills statGrid with results.
        statGrid.add(new Label("Pollutant"), 0, 0);
        statGrid.add(new Label("Stat"), 1, 0);
        statGrid.add(new Label(pollutantBox.getValue() + ":"), 0, 1);
        statGrid.add(new Label(String.format("%.4f", average)), 1, 1);
        
        return Map.entry(statTitle, statGrid);
    }
}