import javafx.scene.layout.Pane;
import java.net.*;
import java.net.http.*;
import java.util.*;
import java.util.regex.*;
import com.sothawo.mapjfx.*;
import com.sothawo.mapjfx.event.*;

/**
 * LiveMapPanel contains the interactive map,
 * live API loading,
 * hover text for live sample points.
 * 
 * @author M.Qasim Imran    
 */
public class LiveMapPanel extends Pane {
    // live air-quality
    private static final String LIVE_API_BASE_URL =
        "https://air-quality-api.open-meteo.com/v1/air-quality";

    // Radius 2 means we generate a 5x5 grid around the centre point.
    private static final int SAMPLE_RADIUS = 2;

    private static final int DEFAULT_ZOOM = 12;

    /**
     * Each region defines:
     * - a centre point for the live map
     * - offsets used to build the surrounding points
     */
    public enum MapRegion {
        LONDON    (51.5074,  -0.1278, 0.0490, 0.0930),
        MANCHESTER(53.4808,  -2.2426, 0.0330, 0.0525),
        BIRMINGHAM(52.4862,  -1.8904, 0.0350, 0.0560);

        final double lat;
        final double lon;
        final double latOffset;
        final double lonOffset;

        MapRegion(double lat, double lon,
                  double latOffset, double lonOffset) {
            this.lat = lat;
            this.lon = lon;
            this.latOffset = latOffset;
            this.lonOffset = lonOffset;
        }
    }

    // LiveSample is the planned query locati4on before API data is fetched.
    private record LiveSample(String label, double latitude, double longitude) {}

    // LivePoint is the display-ready object after the API values have been loaded.
    private record LivePoint(String locationName, double latitude, double longitude,
                             double no2, double pm10, double pm25) {}

    
    private final HttpClient liveHttpClient = HttpClient.newHttpClient();

    // The embedded interactive OpenStreetMap view.
    private final MapView liveMapView;

    // We track the current markers and the data behind them so hover handlers
    // can look up the correct summary text.
    private final List<Marker> currentMarkers = new ArrayList<>();
    private final Map<Marker, LivePoint> liveMarkerData = new HashMap<>();

    // Labels are created during draw method and only added to the map while a marker is hovered.
    private final Map<Marker, MapLabel> liveMarkerLabels = new HashMap<>();

    // Region shown right now.
    private MapRegion currentRegion = MapRegion.LONDON;

    /**
     * Build the live interactive map once and wire up hover behaviour.
     */
    public LiveMapPanel() {
        liveMapView = new MapView();
        URL customCssUrl = getClass().getResource("/style.css");        
        if (customCssUrl != null) {
            liveMapView.setCustomMapviewCssURL(customCssUrl);
        }
        liveMapView.initialize(Configuration.builder().showZoomControls(true).build());
        liveMapView.setCenter(new Coordinate(currentRegion.lat, currentRegion.lon));
        liveMapView.setZoom(DEFAULT_ZOOM);

        // The WebView-backed map needs to finish initialising before markers can safely be added.
        liveMapView.initializedProperty().addListener((obs, old, val) -> {
            if (val) {
                redraw();
            }
        });

        // When the mouse enters a marker, add its label into the map.
        liveMapView.addEventHandler(MarkerEvent.MARKER_ENTERED, e -> showMarkerLabel(e.getMarker()));

        // When the mouse leaves a marker, remove that label again.
        liveMapView.addEventHandler(MarkerEvent.MARKER_EXITED, e -> hideMarkerLabel(e.getMarker()));

        liveMapView.prefWidthProperty().bind(widthProperty());
        liveMapView.prefHeightProperty().bind(heightProperty());
        getChildren().add(liveMapView);

        // If the pointer leaves the whole live-map area, clear any visible label.
        setOnMouseExited(e -> hideAllMarkerLabels());
    }

    /**
     * Rebuild all live markers for the current region.
     *
     *   called when:
     * - the map first finishes initialising
     * - the user switches region
     * - the map tab toggles into live mode
     */
    public void redraw() {
        if (!liveMapView.initializedProperty().get()) {
            return;
        }

        // Remove the previous marker set from previous regions
        currentMarkers.forEach(liveMapView::removeMarker);
        hideAllMarkerLabels();
        liveMarkerLabels.values().forEach(liveMapView::removeLabel);
        currentMarkers.clear();
        liveMarkerData.clear();
        liveMarkerLabels.clear();

        // Build the full live sample grid, fetch current values for each sample,
        // then create one marker and one hidden label per point.
        for (LiveSample sample : getLiveSamples(currentRegion)) {
            Map<String, Double> liveData = fetchCurrentLiveData(sample.latitude(), sample.longitude());

            LivePoint livePoint = new LivePoint(
                sample.label(),
                sample.latitude(),
                sample.longitude(),
                liveData.getOrDefault("NO2", Double.NaN),
                liveData.getOrDefault("PM10", Double.NaN),
                liveData.getOrDefault("PM2.5", Double.NaN));

            Marker marker = new Marker(getLiveMarkerIcon())
                .setPosition(new Coordinate(livePoint.latitude(), livePoint.longitude()))
                .setVisible(true);
            MapLabel label = new MapLabel(buildLiveSummary(livePoint), 0, -35)
                .setCssClass("live-hover-label")
                .setVisible(false)
                .setPosition(new Coordinate(livePoint.latitude(), livePoint.longitude()));

            liveMapView.addMarker(marker);
            liveMapView.addLabel(label);
            currentMarkers.add(marker);
            liveMarkerData.put(marker, livePoint);
            liveMarkerLabels.put(marker, label);
        }
    }

    //Change the current region, recenter the map, and rebuild the live samples.
    public void switchRegion(MapRegion region) {
        currentRegion = region;
        liveMapView.setCenter(new Coordinate(region.lat, region.lon));
        liveMapView.setZoom(DEFAULT_ZOOM);
        redraw();
    }

    //Build the text shown when hovering a live sample point.
    private String buildLiveSummary(LivePoint point) {
        return point.locationName()
            + "\nNO2: " + formatLiveValue(point.no2())
            + "\nPM10: " + formatLiveValue(point.pm10())
            + "\nPM2.5: " + formatLiveValue(point.pm25());
    }

    // Format live values consistently and handle unavailable API data.
    private String formatLiveValue(double value) {
        return Double.isNaN(value) ? "unavailable" : String.format("%.2f ug/m3", value);
    }

    //Convert enum names such as LONDON into user-facing labels such as London.
    private String getRegionLabel(MapRegion region) {
        String lowerCase = region.name().toLowerCase();
        return Character.toUpperCase(lowerCase.charAt(0)) + lowerCase.substring(1);
    }

    /**
     * Build a square grid of sample points around the region centre.
     *
     * With SAMPLE_RADIUS = 2, this produces:
     * - latStep values: -2, -1, 0, 1, 2
     * - lonStep values: -2, -1, 0, 1, 2
     * - total points: 25
     *
     * Each point is generated arithmetically from the centre plus offsets,
     * rather than storing a hard-coded absolute coordinate list.
     */
    private List<LiveSample> getLiveSamples(MapRegion region) {
        List<LiveSample> samples = new ArrayList<>();
        for (int latStep = -SAMPLE_RADIUS; latStep <= SAMPLE_RADIUS; latStep++) {
            for (int lonStep = -SAMPLE_RADIUS; lonStep <= SAMPLE_RADIUS; lonStep++) {
                double latitude = region.lat + latStep * region.latOffset;
                double longitude = region.lon + lonStep * region.lonOffset;
                samples.add(new LiveSample(
                    buildCoordinateDescriptor(region, latitude, longitude),
                    latitude,
                    longitude));
            }
        }

        return samples;
    }


    // Descriptive label for each generated sample point.
     
    private String buildCoordinateDescriptor(MapRegion region, double latitude, double longitude) {
        return getRegionLabel(region)
            + " (" + String.format("%.4f", latitude)
            + ", " + String.format("%.4f", longitude) + ")";
    }

    //display label
    private void showMarkerLabel(Marker marker) {
        hideAllMarkerLabels();
        MapLabel label = liveMarkerLabels.get(marker);
        if (label != null) label.setVisible(true);
    }

    //hide label after mouse leaves
    private void hideMarkerLabel(Marker marker) {
        MapLabel label = liveMarkerLabels.get(marker);
        if (label != null) label.setVisible(false);
    }

    // Clear all visible marker labels from the map.
    private void hideAllMarkerLabels() {
        liveMarkerLabels.values().forEach(label -> label.setVisible(false));
    }

    /**
     * Fetch the current air quality values.
     * The result map always contains all three keys
     * values may stay NaN if the API could not provide them or the request fails.
     */
    private Map<String, Double> fetchCurrentLiveData(double latitude, double longitude) {
        Map<String, Double> liveData = new HashMap<>();
        liveData.put("NO2", Double.NaN);
        liveData.put("PM10", Double.NaN);
        liveData.put("PM2.5", Double.NaN);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(buildApiUrl(latitude, longitude)))
                .GET()
                .build();

            HttpResponse<String> response = liveHttpClient.send(
                request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String json = response.body();
                liveData.put("NO2", extractNumber(json, "nitrogen_dioxide"));
                liveData.put("PM10", extractNumber(json, "pm10"));
                liveData.put("PM2.5", extractNumber(json, "pm2_5"));
            }
        }
        catch (Exception ignored) {
            // Leave values as NaN if the API fails
        }
        
        return liveData;
    }

    /**
     * URL for the current-values 
     */
    private String buildApiUrl(double latitude, double longitude) {
        return LIVE_API_BASE_URL
            + "?latitude=" + latitude
            + "&longitude=" + longitude
            + "&current=nitrogen_dioxide,pm10,pm2_5";
    }

    //helps extract specific data more efficiently
    private double extractNumber(String json, String fieldName) {
        String patternText = "\"" + fieldName + "\"\\s*:\\s*(-?[0-9]+(?:\\.[0-9]+)?)";
        Matcher matcher = Pattern.compile(patternText).matcher(json);
        
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }

        return Double.NaN;
    }

    //loads blue marker icon for the live data
    private URL getLiveMarkerIcon() {
        URL iconUrl = getClass().getResource("/live-marker-blue.svg");
        if (iconUrl == null) {
            throw new IllegalStateException("Cannot load live marker icon: /live-marker-blue.svg");
        }
        return iconUrl;
    }
}
