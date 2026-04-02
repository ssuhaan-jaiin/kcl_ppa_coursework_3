import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Helper class for statisticsPanel.
 * Calculate average pollution of a pollutant over a given period and area.
 * Maps given grid values to dataPoints by going through all data point locations in London map
 * and comparing their grid codes to inputted grid values.
 * Calculate Highest pollution levels of a pollutant over a given period.
 *
 * @author (Shadid Miah)
 * @version (1.00)
 */
public class StatsCalculator
{
    // instance variables - replace the example below with your own
    private int year1;
    private int year2;
    private int minX;
    private int maxX;
    private int minY;
    private int maxY;
    private DataPoint point1 = null;
    private DataPoint point2 = null;
    
    private String pollutant;
    private boolean fullMapSelected;
    private DataManager dataManager;

    /**
     * Constructor for objects of class StatsCalculator
     */
    public StatsCalculator(String pollutant, int year1, int year2, DataManager dataManager)
    {
        // initialise instance variables
        this.dataManager = dataManager;
        this.pollutant = pollutant;
        //Rearrange year1 and year2 so that year1 is the smaller year
        if (year1 > year2) {
            int temp = year1;
            this.year1 = year2;
            this.year2 = temp;
        }
        else {
            this.year1 = year1;
            this.year2 = year2;
        }
    }    
    
    /**
     * Match each grid value to a data point with the same grid code.
     * 
     * @param gridValue1 A grid value
     * @param gridValue2 A grid value
     */
    public void matchGridValue(int gridValue1, int gridValue2) {
        // Goes through each point in a given year for a given pollutant.
        // Grid values remains the same for each year, so the year passed in doesn't matter.
        for (DataPoint point : dataManager.getLondonData(pollutant, Integer.toString(year1))) {
            // Check if point matches gridValues passed in.
            // If they match, store the point in a variable.
            if (point.gridCode() == gridValue1) {
                point1 = point;
            }
            if (point.gridCode() == gridValue2) {
                point2 = point;
            }
            if (point1 != null && point2 != null) {
                break;
            }
        }
        
        setMinMaxXY();
        
    }
    
    /**
     * sets min max x and y values based on field points.
     */
    private void setMinMaxXY()
    {
        // Checks which point is larger, so that min max X Y can be assigned the correct values.
        if (point1 != null && point2 != null){
            if (point1.x() < point2.x()) {
                minX = point1.x();
                maxX = point2.x();
            }
            else {
                minX = point2.x();
                maxX = point1.x();
            }
            
            if (point1.y() < point2.y()) {
                minY = point1.y();
                maxY = point2.y();
            }
            else {
                minY = point2.y();
                maxY = point1.y();
            }
            
        }
    }
    
    /**
     * Calculate average pollution over an area and period determined by the given values.
     * Area is determined through the min max X Y values.
     */
    public double calculateAverage() {
        double total = 0;
        double points = 0;
        
        // Checks if grid values have successfully matched to a data point.
        // If they haven't, then calculation cannot proceed.
        if (fullMapSelected == false && (point1 == null || point2 == null)) {
            return -1;
        }
        
        //Loop from year1 to  year2        
        for (int i = year1; i <= year2; i++) {
            List<DataPoint> dataPoints = dataManager.getLondonData(pollutant, Integer.toString(i));
            if (fullMapSelected) {
                // Uses a stream to convert all data point values to double values, and then sums them together. 
                total += dataPoints.stream().mapToDouble(DataPoint::value).sum();
                points += dataPoints.size();
            }
            else {
                for (DataPoint dataPoint : dataPoints) {
                if (insideArea(dataPoint)) {
                    // Check if data point is inside area enclosed by the given grid values. 
                    total += dataPoint.value();
                    points++;
                    } 
                }
            }
            
        }
        return total/points;
    }
    
    /**
     * Calculate highest pollution level for pollutant in the selected area.
     */
    public Map.Entry<Integer, Double> calcHighestLevels() {
        HashMap<Integer, Double> stat = new HashMap();        
        double highestPollutionLevel = -1;
        int location = -1;
        
        //Loop from year1 to  year2        
        for (int i = year1; i <= year2; i++) {
            String year = String.valueOf(i);
            
            if (dataManager.getMax(pollutant, year).value() > highestPollutionLevel) {
                highestPollutionLevel = dataManager.getMax(pollutant, year).value();
                location = dataManager.getMax(pollutant, year).gridCode();
            }         
        }
        
        stat.put(location, new Double(highestPollutionLevel));
        return stat.entrySet().iterator().next();
        
    }
    
    /**
     * Checks if data point is inside selected area on grid
     * @param point The data point being checked.
     * @return Returns true if point is inside area.
     */
    private boolean insideArea(DataPoint point) {
        // checks if point is within the bounds of the area defined by the min max X Y values.
        return point.x() >= minX && point.x()<= maxX
            && point.y() >= minY && point.y()<= maxY;
    }
    
    /**
     * Sets fullMapSelected to true or false
     * @param fullMap The boolean determining whether we use the full map or not
     */
    public void setFullMapSelected(boolean fullMap) {
        fullMapSelected = fullMap;
    }
}