import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LiveDataClient fetches current London pollution values from an external API
 * This is used for the optional challenge feature
 *
 * 
 * I split this into its own class so DataManager stays on csv data
 * If the API endpoint changes later I only need to update this file
 */
public class LiveDataClient
{
    // Primary endpoint: this gives "current" values directly
    private static final String PRIMARY_API_URL =
        "https://air-quality-api.open-meteo.com/v1/air-quality"
        + "?latitude=51.5072&longitude=-0.1276"
        + "&current=nitrogen_dioxide,pm10,pm2_5";

    // Backup endpoint: this gives hourly arrays
    // If primary fails, we use the newest value from each array
    private static final String FALLBACK_API_URL =
        "https://air-quality-api.open-meteo.com/v1/air-quality"
        + "?latitude=51.5072&longitude=-0.1276"
        + "&hourly=nitrogen_dioxide,pm10,pm2_5&forecast_days=1";

    /**
    * returns live values for NO2, PM10 and PM2.5
     * if API call fails, values are NaN
     *
     * 
     */
    public Map<String, Double> getCurrentLiveData()
    {
        // Start with safe fallback values
        // If both API calls fail, panel still works and shows unavailable
        Map<String, Double> liveData = new HashMap<String, Double>();
        liveData.put("NO2", Double.NaN);
        liveData.put("PM10", Double.NaN);
        liveData.put("PM2.5", Double.NaN);

        HttpClient client = HttpClient.newHttpClient();

        // Try primary endpoint first
        boolean primaryOk = tryLoadFromPrimary(client, liveData);
        if (primaryOk) {
            return liveData;
        }

        // If primary fails, try backup endpoint
        tryLoadFromFallback(client, liveData);
        return liveData;
    }

    /**
        * Calls primary endpoint and fills map if response is valid
     */
    private boolean tryLoadFromPrimary(HttpClient client, Map<String, Double> liveData)
    {
        try {
            // Build GET request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PRIMARY_API_URL))
                .GET()
                .build();

            // Send request and read body as JSON text
            HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString());

            // Non-200 means this attempt failed
            if (response.statusCode() != 200) {
                return false;
            }

            // Primary response has direct fields
            String json = response.body();
            liveData.put("NO2", extractNumber(json, "nitrogen_dioxide"));
            liveData.put("PM10", extractNumber(json, "pm10"));
            liveData.put("PM2.5", extractNumber(json, "pm2_5"));

            return hasAtLeastOneValue(liveData);
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
        * Calls fallback endpoint and fills map if response is valid
     */
    private boolean tryLoadFromFallback(HttpClient client, Map<String, Double> liveData)
    {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(FALLBACK_API_URL))
                .GET()
                .build();

            HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return false;
            }

            // Fallback response has arrays inside "hourly"
            String json = response.body();
            liveData.put("NO2", extractLastArrayNumber(json, "nitrogen_dioxide"));
            liveData.put("PM10", extractLastArrayNumber(json, "pm10"));
            liveData.put("PM2.5", extractLastArrayNumber(json, "pm2_5"));

            return hasAtLeastOneValue(liveData);
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
        * True if at least one pollutant value is usable
     */
    private boolean hasAtLeastOneValue(Map<String, Double> liveData)
    {
        return !Double.isNaN(liveData.get("NO2"))
            || !Double.isNaN(liveData.get("PM10"))
            || !Double.isNaN(liveData.get("PM2.5"));
    }

    /**
        * Extract one number from json when the field is a single value
     * Example: "pm10": 12.34
     */
    private double extractNumber(String json, String fieldName)
    {
        String patternText = "\"" + fieldName + "\"\\s*:\\s*(-?[0-9]+(?:\\.[0-9]+)?)";
        Pattern pattern = Pattern.compile(patternText);
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }

        return Double.NaN;
    }

    /**
        * Extract newest number from json when the field is an array
     * Example: "pm10": [10.1, 11.2, 9.8]
     */
    private double extractLastArrayNumber(String json, String fieldName)
    {
        String patternText = "\"" + fieldName + "\"\\s*:\\s*\\[([^\\]]+)\\]";
        Pattern pattern = Pattern.compile(patternText);
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            String[] parts = matcher.group(1).split(",");
            if (parts.length == 0) {
                return Double.NaN;
            }

            String last = parts[parts.length - 1].trim();
            if (last.equals("null")) {
                return Double.NaN;
            }

            try {
                return Double.parseDouble(last);
            }
            catch (NumberFormatException e) {
                return Double.NaN;
            }
        }

        return Double.NaN;
    }
}
