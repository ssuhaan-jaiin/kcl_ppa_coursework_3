import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

/**
 * Unit tests for the DataManager class.
 * 
 * Tests verify that DataManager loads, filters, and processes the 
 * DEFRA air pollution data correctly.
 */
public class DataManagerTest
{
    private DataManager dm;

    /**
     * Sets up a new DataManager before each test.
     * Called before every test method.
     */
    @Before
    public void setUp()
    {
        dm = new DataManager();
    }

    /**
     * Test that getDataSet returns the correct dataset with correct year.
     */
    @Test
    public void testGetDataSetReturnsCorrectYear()
    {
        DataSet ds = dm.getDataSet("NO2", "2018");
        assertNotNull("Dataset should not be null", ds);
        assertEquals("Year of dataset should be 2018", "2018", ds.getYear());
    }

    /**
     * Test that getDataSet returns the correct dataset with correct pollutant.
     */
    @Test
    public void testGetDataSetReturnsCorrectPollutant()
    {
        DataSet ds = dm.getDataSet("PM10", "2021");
        assertNotNull("Dataset should not be null", ds);
        assertEquals("Pollutant of dataset should be pm10", "pm10", ds.getPollutant());
    }

    /**
     * Test that getDataSet returns null for invalid year.
     */
    @Test
    public void testGetDataSetInvalidYear()
    {
        DataSet result = dm.getDataSet("NO2", "1999");
        assertNull("Invalid year should return null", result);
    }

    /**
     * Test that getDataSet returns null for invalid pollutant.
     */
    @Test
    public void testGetDataSetInvalidPollutant()
    {
        DataSet result = dm.getDataSet("INVALID", "2023");
        assertNull("Invalid pollutant should return null", result);
    }

    /**
     * Test that getDataSet returns null for null inputs.
     */
    @Test
    public void testGetDataSetNullInputs()
    {
        DataSet result1 = dm.getDataSet(null, "2023");
        DataSet result2 = dm.getDataSet("NO2", null);
        assertNull("Null pollutant should return null", result1);
        assertNull("Null year should return null", result2);
    }

    /**
     * Test that getDataSet normalises different pollutant names to the same dataset.
     * PM2.5 can be written as "pm2.5", "PM25", or "PM2_5".
     */
    @Test
    public void testGetDataSetNormalisation()
    {
        DataSet ds1 = dm.getDataSet("PM2.5", "2023");
        DataSet ds2 = dm.getDataSet("pm25", "2023");
        DataSet ds3 = dm.getDataSet("PM2_5", "2023");
        
        assertNotNull("PM2.5 should be found", ds1);
        assertEquals("pm25 and PM2.5 should return same dataset", ds1, ds2);
        assertEquals("PM2_5 and PM2.5 should return same dataset", ds1, ds3);
    }

    /**
     * Test that getLondonData returns data points within London boundaries.
     */
    @Test
    public void testLondonDataWithinBounds()
    {
        List<DataPoint> points = dm.getLondonData("PM2.5", "2023");
        assertFalse("London data list should not be empty", points.isEmpty());
        
        // Check all points are within London bounds
        for (DataPoint p : points) {
            assertTrue("Easting (x) is too far west", p.x() >= 510394);
            assertTrue("Easting (x) is too far east", p.x() <= 553297);
            assertTrue("Northing (y) is too far south", p.y() >= 168504);
            assertTrue("Northing (y) is too far north", p.y() <= 193305);
        }
    }

    /**
     * Test that getLondonData filters out negative values.
     */
    @Test
    public void testGetLondonDataFiltersNegativeValues()
    {
        List<DataPoint> result = dm.getLondonData("NO2", "2023");
        
        // All points should have non-negative values
        for (DataPoint point : result) {
            assertTrue("All points should have non-negative values", point.value() >= 0);
        }
    }

    /**
     * Test that getLondonData returns empty list for invalid year.
     */
    @Test
    public void testGetLondonDataInvalidYear()
    {
        List<DataPoint> result = dm.getLondonData("NO2", "1999");
        assertNotNull("Should return a list, not null", result);
        assertTrue("Should return empty list for invalid year", result.isEmpty());
    }

    /**
     * Test that getAverage returns a positive average for valid data.
     */
    @Test
    public void testAverageIsPositive()
    {
        double avg = dm.getAverage("NO2", "2019");
        assertTrue("Average pollution level should be greater than 0", avg > 0);
    }

    /**
     * Test that getAverage returns NaN for datasets with no valid points.
     */
    @Test
    public void testGetAverageNoPoints()
    {
        double result = dm.getAverage("NO2", "1999");
        assertTrue("Should return NaN for invalid year", Double.isNaN(result));
    }

    /**
     * Test that getMax returns the highest value data point.
     */
    @Test
    public void testMaxIsHighestValue()
    {
        DataPoint maxPoint = dm.getMax("NO2", "2020");
        assertNotNull("Max point should not be null", maxPoint);
        
        // Check that returned max is actually the highest value
        List<DataPoint> allPoints = dm.getLondonData("NO2", "2020");
        for (DataPoint p : allPoints) {
            assertTrue("Found a point with higher value than the max", p.value() <= maxPoint.value());
        }
    }

    /**
     * Test that getMax returns null when there are no valid points.
     */
    @Test
    public void testGetMaxNoPoints()
    {
        DataPoint result = dm.getMax("NO2", "1999");
        assertNull("Should return null for invalid year", result);
    }

    /**
     * Test that getTrend returns a map with all 6 years.
     */
    @Test
    public void testTrendHasSixYears()
    {
        Map<String, Double> trend = dm.getTrend("PM10");
        assertNotNull("Trend map should not be null", trend);
        assertEquals("Trend should contain exactly 6 year entries", 6, trend.size());
        
        // Check that specific years are in the map
        assertTrue("Trend should contain year 2018", trend.containsKey("2018"));
        assertTrue("Trend should contain year 2023", trend.containsKey("2023"));
    }

    /**
     * Test that getTrend preserves year order (2018 to 2023).
     */
    @Test
    public void testGetTrendOrder()
    {
        Map<String, Double> trend = dm.getTrend("NO2");
        List<String> years = dm.getAvailableYears();
        
        // Check that years are in the correct order
        int index = 0;
        for (String year : trend.keySet()) {
            assertEquals("Years should be in order", years.get(index), year);
            index++;
        }
    }

    /**
     * Test that getAvailableYears returns all 6 supported years.
     */
    @Test
    public void testGetAvailableYears()
    {
        List<String> years = dm.getAvailableYears();
        assertEquals("Should return exactly 6 years", 6, years.size());
        
        // Verify exact contents
        String[] expectedYears = {"2018", "2019", "2020", "2021", "2022", "2023"};
        for (String expected : expectedYears) {
            assertTrue("Years list should contain " + expected, years.contains(expected));
        }
    }

    /**
     * Test that getAvailablePollutants returns all 3 supported pollutants.
     */
    @Test
    public void testGetAvailablePollutants()
    {
        List<String> pollutants = dm.getAvailablePollutants();
        assertEquals("Should return exactly 3 pollutants", 3, pollutants.size());
        
        assertTrue("Pollutants list should contain NO2", pollutants.contains("NO2"));
        assertTrue("Pollutants list should contain PM10", pollutants.contains("PM10"));
        assertTrue("Pollutants list should contain PM2.5", pollutants.contains("PM2.5"));
    }

    /**
     * Test that all pollutant-year combinations can be loaded.
     */
    @Test
    public void testAllCombinations()
    {
        List<String> pollutants = dm.getAvailablePollutants();
        List<String> years = dm.getAvailableYears();
        
        // Every combination should be queryable
        for (String pollutant : pollutants) {
            for (String year : years) {
                DataSet ds = dm.getDataSet(pollutant, year);
                assertNotNull("Should be able to get " + pollutant + " " + year, ds);
            }
        }
    }
}
