    import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DataManager handles loading and querying pollution data.
 *
 * It loads all 18 csv files once at startup and stores them in a map.
 * Keys are in the format "POLLUTANT-YEAR" (for example "NO2-2023")
 */
public class DataManager
{
    // Boundaries for the London map (class variables).
    // The minimum x coordinate (Easting) for London.
    private static final int LONDON_MIN_X = 510394;
    // The maximum x coordinate (Easting) for London.
    private static final int LONDON_MAX_X = 553297;
    // The minimum y coordinate (Northing) for London
    private static final int LONDON_MIN_Y = 168504;
    // The maximum y coordinate (northing) for London.
    private static final int LONDON_MAX_Y = 193305;

    // The years where data is available.
    private static final String[] YEARS = {"2018", "2019", "2020", "2021", "2022", "2023"};
    // pollutants tracked in the datasets
    private static final String[] POLLUTANTS = {"NO2", "PM10", "PM2.5"};

    //map storing all loaded datasets
    private final Map<String, DataSet> datasets;

    // Separate helper for challenge API calls.
    // Keeps DataManager cleaner and avoids mixing HTTP code with CSV logic.
    private final LiveDataClient liveDataClient;

    /**
     * Constructor: load all datasets once at startup
     */
    public DataManager()
    {
        datasets = new HashMap<String, DataSet>();

        // Build the API helper once here.
        // This object is reused whenever LiveDataPanel presses refresh.
        liveDataClient = new LiveDataClient();
        DataLoader loader = new DataLoader();

        // Loop through all pollutants and years to load files
        for (String pollutant : POLLUTANTS) {
            for (String year : YEARS) {
                String fileName = buildFileName(pollutant, year);
                DataSet dataSet = loader.loadDataFile(fileName);
                if (dataSet != null) {
                    datasets.put(makeKey(pollutant, year), dataSet);
                }
            }
        }
    }

    /**
     * Returns one specific dataset
        * @param pollutant The pollutant name
        * @param year The year as text.
        * @return The matching dataset, or null if not found
        * 
        * eg: DataSet ds = dm.getDataSet("NO2", "2023");
     */
    public DataSet getDataSet(String pollutant, String year)
    {
        // normalise input to handle small variations in string format
        String normalisedPollutant = normalisePollutant(pollutant);
        if (normalisedPollutant == null || year == null) {
            return null;
        }

        return datasets.get(makeKey(normalisedPollutant, year.trim()));
    }

    /**
     * Returns London-only data points for the requested pollutant and year
     * Invalid or missing data points (value < 0) are filtered out.
        * @param pollutant The pollutant name
        * @param year The year as text
        * @return A list of valid points inside London bounds
        * 
        * eg: List<DataPoint> points = dm.getLondonData("PM10", "2021");
     */
    public List<DataPoint> getLondonData(String pollutant, String year)
    {
        DataSet dataSet = getDataSet(pollutant, year);
        List<DataPoint> londonData = new ArrayList<DataPoint>();

        // return empty list if dataset doesn't exist
        if (dataSet == null) {
            return londonData;
        }

        for (DataPoint point : dataSet.getData()) {
            // only add points within London bounds and with valid values
            if (isInLondonBounds(point) && point.value() >= 0) {
                londonData.add(point);
            }
        }

        return londonData;
    }



    /**
     * Returns all supported years
     * @return The list of available years.
     */
    public List<String> getAvailableYears()
    {
        return Arrays.asList(YEARS);
    }

    /**
     * Returns all supported pollutant names.
     * @return The list of available pollutants.
     */
    public List<String> getAvailablePollutants()
    {
        return Arrays.asList(POLLUTANTS);
    }

    /**
     * Returns London average value for one pollutant and year.
     * @param pollutant The pollutant name.
     * @param year The year as text.
     * @return The average value, or NaN if there are no valid points.
     */
    public double getAverage(String pollutant, String year)
    {
        List<DataPoint> points = getLondonData(pollutant, year);
        // return NaN if no points are available to prevent division by zero
        if (points.isEmpty()) {
            return Double.NaN;
        }
        
        // calculate total
        double total = 0.0;
        for (DataPoint point : points) {
            total += point.value();
        }

        return total / points.size();
    }

    /**
     * Returns the highest-value London datapoint for one pollutant and year.
        * @param pollutant The pollutant name.
        * @param year The year as text.
        * @return The point with the highest value, or null if no points exist.
     */
    public DataPoint getMax(String pollutant, String year)
    {
        List<DataPoint> points = getLondonData(pollutant, year);
        // return null if no points are available
        if (points.isEmpty()) {
            return null;
        }

        // simple linear search
        DataPoint maxPoint = points.get(0);
        for (DataPoint point : points) {
            if (point.value() > maxPoint.value()) {
                maxPoint = point;
            }
        }

        return maxPoint;
    }

    /**
     * Returns a pollutant trend map (year -> London average)
      * @param pollutant The pollutant name
      * @return A year -> average map in year order.
     */
    public Map<String, Double> getTrend(String pollutant)
    {
        // Use LinkedHashMap to preserve the order of years
        Map<String, Double> trend = new LinkedHashMap<String, Double>();

        for (String year : YEARS) {
            trend.put(year, getAverage(pollutant, year));
        }

        return trend;
    }

    /**
     * Returns current live air quality data for London using an external API.
     * Keys returned are: "NO2", "PM10", "PM2.5".
     * If the API call fails, values are returned as NaN.
     */
    public Map<String, Double> getCurrentLiveData()
    {
        // DataManager acts as the single access point for the whole app.
        // UI code (LiveDataPanel) calls DataManager, not LiveDataClient directly.
        // This keeps app structure consistent with the rest of the project.
        return liveDataClient.getCurrentLiveData();
    }

    private boolean isInLondonBounds(DataPoint point)
    {
        // check if point falls within defined min/max coordinate ranges
        return point.x() >= LONDON_MIN_X && point.x() <= LONDON_MAX_X
            && point.y() >= LONDON_MIN_Y && point.y() <= LONDON_MAX_Y;
    }

    /**
     * Build a map key from pollutant and year.
     * Example: "PM10-2021".
     */
    private String makeKey(String pollutant, String year)
    {
        // standardise key format for map lookup
        return pollutant + "-" + year;
    }

    /**
     * Clean up pollutant text so we use one consistent name
     */
    private String normalisePollutant(String pollutant)
    {
        // handle null inputs gracefully
        if (pollutant == null) {
            return null;
        }

        String p = pollutant.trim().toUpperCase();
        if (p.equals("NO2")) {
            return "NO2";
        }
        if (p.equals("PM10")) {
            return "PM10";
        }
        // handle various formats for PM2.5
        if (p.equals("PM2.5") || p.equals("PM25") || p.equals("PM2_5")) {
            return "PM2.5";
        }

        return null;
    }

    private String buildFileName(String pollutant, String year)
    {
        // map specific pollutant strings to exact file paths
        if (pollutant.equals("NO2")) {
            return "UKAirPollutionData/NO2/mapno2" + year + ".csv";
        }
        if (pollutant.equals("PM10")) {
            return "UKAirPollutionData/pm10/mappm10" + year + "g.csv";
        }
        
        // PM2.5 files use the pm2.5 folder and end in g.csv.
        return "UKAirPollutionData/pm2.5/mappm25" + year + "g.csv";
    }
}
