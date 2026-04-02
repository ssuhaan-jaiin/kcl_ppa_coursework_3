
/**
 * This class is a short demo showing how to use the DataLoader to load the pollution data 
 * csv files from disk and access the data.
 *
 * @author Michael Kölling
 * @version 1.0
 */
public class FileLoadDemo
{
    /**
     * Create and run the demo.
     */
    public FileLoadDemo()
    {
        showFile();
    }

    /**
     * This method loads one of the pollution data files.
     */
    public void showFile()
    {
        DataLoader loader = new DataLoader();
        
        DataSet dataSet = loader.loadDataFile("UKAirPollutionData/NO2/mapno22023.csv");
        System.out.println(dataSet);
    }
}
