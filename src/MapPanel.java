import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * MapPanel coordinates the toolbar and the two map modes.
 * 
 * @author M.Qasim Imran
 */
public class MapPanel extends BorderPane {
    // Shared data access used for static pollution lookups.
    private final DataManager dataManager;
    //livemap component
    private final LiveMapPanel livePanel;

    // Static map visuals: the London image, aas above it for dots, and a      
    // small tooltip label that follows the mouse.
    private Canvas mapCanvas;
    private ImageView mapImageView;
    private Label tooltip;

    //static container for static map components
    private Pane staticContainer;
    private boolean usingLiveMap = false;

    //stores datapoint coordinates for easier collision detection with mouse   
    private record DrawnPoint(DataPoint dataPoint, double px, double py) {}
    private final List<DrawnPoint> drawnPoints = new ArrayList<>();

    //default pollutants and year
    private String selectedPollutant = "NO2";
    private String selectedYear = "2018";

    // Combo boxes and toggles for updating them later
    private ComboBox<String> pollutantBox;
    private ComboBox<String> yearBox;
    private ToggleButton mapToggle;

    //Helper class to open grid detail panel when dot clicked on map
    private DotClickListener dotClickListener;
    private final List<DataPoint> selectedGridPoints = new ArrayList<>();
    private final Label selectionPrompt = new Label();
    private AreaSelectListener areaSelectListener;

    //coordinates to display pollutant accurately on map
    private static final double MIN_EASTING  = 510394.0;
    private static final double MAX_EASTING  = 553297.0;
    private static final double MIN_NORTHING = 168504.0;
    private static final double MAX_NORTHING = 193305.0;

    //intialise panel content
    public MapPanel(DataManager dataManager) {
        this.dataManager = dataManager;
        this.livePanel = new LiveMapPanel();
        staticContainer = buildStaticContainer();
        setCenter(staticContainer);
        setTop(buildToolbar());
    }

    /**
     * Build the layered static map:
     * - base image
     * - canvas for coloured dots
     * - tooltip label floating above both
     */
    private Pane buildStaticContainer() {
        //load london png
        mapImageView = new ImageView(loadLondonMapImage());
        //maintain aspect ratio to avoid distortion, but allow stretching to fill the space
        mapImageView.setPreserveRatio(false);

        mapCanvas = new Canvas();
        tooltip = createInfoLabel();
        selectionPrompt.setVisible(false);

        //fitting canvas onto png so dots can be drawn
        Pane container = new Pane();
        mapImageView.fitWidthProperty().bind(container.widthProperty());
        mapImageView.fitHeightProperty().bind(container.heightProperty());
        mapCanvas.widthProperty().bind(container.widthProperty());
        mapCanvas.heightProperty().bind(container.heightProperty());
        container.getChildren().addAll(mapImageView, mapCanvas, tooltip, selectionPrompt);

        Runnable redraw = () -> {
            if (mapCanvas.getWidth() <= 0 || mapCanvas.getHeight() <= 0) {
                return;
            }

            drawStaticDots();

            for (DataPoint point : selectedGridPoints) {
                highlightSelectedAreaPoint(point);
            }
            if (selectedGridPoints.size() == 2) {
                drawSelectionArea();
                showSelectionPrompt();
            }
        };
        //redraw dots when canvas resized
        mapCanvas.widthProperty().addListener((o, old, value) -> redraw.run());
        mapCanvas.heightProperty().addListener((o, old, value) -> redraw.run());

        //coordinates grab used for hover and click events
        mapCanvas.setOnMouseMoved(e -> handleHover(e.getX(), e.getY()));
        mapCanvas.setOnMouseExited(e -> tooltip.setVisible(false));
        mapCanvas.setOnMouseClicked(this::handleClick);

        return container;
    }

    private Image loadLondonMapImage() {
        InputStream stream = getClass().getResourceAsStream("/London.png");
        if (stream == null) {
            stream = getClass().getResourceAsStream("London.png");
        }
        if (stream != null) {
            return new Image(stream);
        }
        throw new IllegalStateException("Cannot load London.png from resources or classpath");
    }

    /**
     * Build the top toolbar for the Map tab.
     *
     * Static mode uses pollutant/year.
     * Live mode uses region.
     * The toggle decides which centre component is currently displayed.
     */
    private HBox buildToolbar() {
        // Dropdowns for static map mode
        pollutantBox = new ComboBox<>();
        pollutantBox.getItems().addAll(dataManager.getAvailablePollutants());   
        pollutantBox.setValue(selectedPollutant);
        pollutantBox.setOnAction(e -> {
            selectedPollutant = pollutantBox.getValue();
            selectionPrompt.setVisible(false);
            drawDots();
        });

        yearBox = new ComboBox<>();
        yearBox.getItems().addAll(dataManager.getAvailableYears());
        yearBox.setValue(selectedYear);
        yearBox.setOnAction(e -> {
            selectedYear = yearBox.getValue();
            selectionPrompt.setVisible(false);
            drawDots();
        });

        // Region dropdown for live map mode
        ComboBox<LiveMapPanel.MapRegion> regionBox = new ComboBox<>();
        regionBox.getItems().addAll(LiveMapPanel.MapRegion.values());
        regionBox.setValue(LiveMapPanel.MapRegion.LONDON);
        regionBox.setOnAction(e -> livePanel.switchRegion(regionBox.getValue()));
        regionBox.setDisable(true);

        // Toggle button to switch between static and live map modes
        mapToggle = new ToggleButton("Live Map");
        mapToggle.setOnAction(e -> {
            usingLiveMap = mapToggle.isSelected();
            mapToggle.setText(usingLiveMap ? "Static Map" : "Live Map");

            //disable static dropdowns in lvie mode
            pollutantBox.setDisable(usingLiveMap);
            yearBox.setDisable(usingLiveMap);
            regionBox.setDisable(!usingLiveMap);
            selectionPrompt.setVisible(false);

            //switch maps and redraw if needed
            if (usingLiveMap) {
                setCenter(livePanel);
                livePanel.redraw();
            }
            else {
                setCenter(staticContainer);
                drawStaticDots();
            }
        });

        HBox toolbar = new HBox(10,
            styledLabel("Pollutant:"), pollutantBox,
            styledLabel("Year:"), yearBox,
            styledLabel("Region:"), regionBox,
            mapToggle);
        toolbar.setPadding(new Insets(8, 12, 8, 12));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: #2b2b2b;");
        return toolbar;
    }

    //Redraw whichever map mode is currently active.
    private void drawDots() {
        if (usingLiveMap) {
            livePanel.redraw();
        }
        else {
            drawStaticDots();
        }
    }

    /**
     * Draws dots from dataset
     * within london
     * with appropriate colours based on pollution value
     */
    /**
     * Paint all historical static-map dots for the selected pollutant/year.
     */
    private void drawStaticDots() {
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        double width = mapCanvas.getWidth();
        double height = mapCanvas.getHeight();
        gc.clearRect(0, 0, width, height);
        drawnPoints.clear();

        //calculates average pollution values to be compared against later for colour assignemnt
        int yearInt = Integer.parseInt(selectedYear);
        StatsCalculator stats = new StatsCalculator(selectedPollutant, yearInt, yearInt, dataManager);
        stats.setFullMapSelected(true);
        double currentAverage = stats.calculateAverage();

        for (DataPoint point : dataManager.getLondonData(selectedPollutant, selectedYear)) {
            // Convert dataset coordinates into pixels in the current resized map.
            double px = toPixelX(point.x(), width);
            double py = toPixelY(point.y(), height);
            gc.setFill(getPollutionColour(point.value(), currentAverage));
            gc.fillOval(px - 4, py - 4, 8, 8);
            // Store the pixel location so later hover/click checks can use    
            // distance tests instead of recomputing the whole projection.     
            drawnPoints.add(new DrawnPoint(point, px, py));
        }
    }

    /**
     * Visually emphasise a static-map point after the user selects it from the Alerts tab.
     */
    public void highlightPoint(DataPoint point, String pollutant, String year) {
        if (usingLiveMap) {
            // toggle back to static map by firing if it's currently selected
            if (mapToggle.isSelected()) {
                mapToggle.fire();
            }
        }

        // Make sure comboboxes match the Alert
        if (!pollutant.equals(pollutantBox.getValue())) {
            pollutantBox.setValue(pollutant);
        }
        if (!year.equals(yearBox.getValue())) {
            yearBox.setValue(year);
        }

        // Start from a clean redraw so we only have one highlighted point.    
        drawStaticDots();

        int yearInt = Integer.parseInt(selectedYear);
        StatsCalculator stats = new StatsCalculator(selectedPollutant, yearInt, yearInt, dataManager);
        stats.setFullMapSelected(true);
        double currentAverage = stats.calculateAverage();

        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        double px = toPixelX(point.x(), mapCanvas.getWidth());
        double py = toPixelY(point.y(), mapCanvas.getHeight());

        // White ring for contrast, then redraw the coloured point larger on top.
        gc.setFill(Color.WHITE);
        gc.fillOval(px - 10, py - 10, 20, 20);

        // Larger coloured point on top
        gc.setFill(getPollutionColour(point.value(), currentAverage));
        gc.fillOval(px - 8, py - 8, 16, 16);
    }

    //Show a tooltip when the mouse is close enough to a static-map dot.       

    /**
     * Show a tooltip when the mouse is close enough to a static-map dot.
     */
    private void handleHover(double x, double y) {
        DrawnPoint hit = findNearestDot(x, y);
        if (hit == null) {
            tooltip.setVisible(false);
            return;
        }

        //tooltip display text format and coordinates
        DataPoint point = hit.dataPoint();
        showTooltipText("Grid: " + point.gridCode()
            + "  |  X: " + point.x() + "  |  Y: " + point.y()
            + "  |  " + selectedPollutant + ": "
            + String.format("%.2f", point.value()) + " ug/m3");
        tooltip.setLayoutX(Math.min(x + 12, Math.max(0, mapCanvas.getWidth() - 140)));
        tooltip.setLayoutY(Math.max(0, y - 28));
    }

    //hit detection calls dotclicklistner
    /**
     * Forward static-dot clicks to the installed listeners.
     */
    private void handleClick(MouseEvent event) {
        DrawnPoint hit = findNearestDot(event.getX(), event.getY());
        if (hit == null) {
            return;
        }

        if (event.getButton() == MouseButton.PRIMARY) {
            if (dotClickListener != null) {
                dotClickListener.onDotClicked(hit.dataPoint(), selectedPollutant, selectedYear);
            }
        }
        else if (event.getButton() == MouseButton.SECONDARY) {
            if (selectedGridPoints.size() >= 2) {
                selectedGridPoints.clear();
                selectionPrompt.setVisible(false);
            }

            selectedGridPoints.add(hit.dataPoint());
            drawStaticDots();
            for (DataPoint point : selectedGridPoints) {
                highlightSelectedAreaPoint(point);
            }
            if (selectedGridPoints.size() == 2) {
                drawSelectionArea();
                showSelectionPrompt();
            }
        }
    }

    private void highlightSelectedAreaPoint(DataPoint point) {
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        double px = toPixelX(point.x(), mapCanvas.getWidth());
        double py = toPixelY(point.y(), mapCanvas.getHeight());
        gc.setFill(Color.WHITE);
        gc.fillOval(px - 8, py - 8, 16, 16);
        gc.setFill(Color.rgb(51, 142, 212));
        gc.fillOval(px - 6, py - 6, 12, 12);
    }

    private void drawSelectionArea() {
        DataPoint p1 = selectedGridPoints.get(0);
        DataPoint p2 = selectedGridPoints.get(1);

        double px1 = toPixelX(Math.min(p1.x(), p2.x()), mapCanvas.getWidth());
        double py1 = toPixelY(Math.max(p1.y(), p2.y()), mapCanvas.getHeight());
        double px2 = toPixelX(Math.max(p1.x(), p2.x()), mapCanvas.getWidth());
        double py2 = toPixelY(Math.min(p1.y(), p2.y()), mapCanvas.getHeight());

        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        gc.setFill(Color.rgb(88, 177, 245, 0.25));
        gc.fillRect(px1, py1, px2 - px1, py2 - py1);
        gc.setStroke(Color.rgb(39, 155, 245));
        gc.setLineWidth(1.5);
        gc.strokeRect(px1, py1, px2 - px1, py2 - py1);
    }

    private void showSelectionPrompt() {
        DataPoint p1 = selectedGridPoints.get(0);
        DataPoint p2 = selectedGridPoints.get(1);

        selectionPrompt.setText("Area: " + p1.gridCode() + " -> " + p2.gridCode()
            + "   |   Right-click here to reset   |   Click here to calculate average");
        selectionPrompt.setStyle(
            "-fx-background-color: rgba(0,100,200,0.85);"
            + "-fx-text-fill: white; -fx-padding: 6 12 6 12;"
            + "-fx-background-radius: 4; -fx-font-size: 12px; -fx-cursor: hand;");
        selectionPrompt.setLayoutX(10);
        selectionPrompt.setLayoutY(mapCanvas.getHeight() - 40);
        selectionPrompt.setVisible(true);
        selectionPrompt.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                selectedGridPoints.clear();
                selectionPrompt.setVisible(false);
                drawStaticDots();
            }
            else {
                fireAreaSelection(p1, p2);
            }
        });
    }

    private void fireAreaSelection(DataPoint p1, DataPoint p2) {
        if (areaSelectListener != null) {
            areaSelectListener.onAreaSelected(p1, p2);
        }
    }

    public void setAreaSelectListener(AreaSelectListener listener) {
        this.areaSelectListener = listener;
    }

    private void showTooltipText(String text) {
        tooltip.setText(text);
        tooltip.setVisible(true);
    }

    //tooltip format
    private Label createInfoLabel() {
        Label label = new Label();
        label.getStyleClass().add("map-tooltip");
        label.setWrapText(true);
        label.setMaxWidth(260);
        label.setVisible(false);
        label.setMouseTransparent(true);
        return label;
    }

    /**
     * collision detection between mouse and closest dot
     * calcualtes distance via pythagoras
     */
    private DrawnPoint findNearestDot(double mouseX, double mouseY) {
        final double hitRadiusSq = 36.0;
        for (DrawnPoint drawnPoint : drawnPoints) {
            double dx = drawnPoint.px() - mouseX;
            double dy = drawnPoint.py() - mouseY;
            if (dx * dx + dy * dy <= hitRadiusSq) {
                return drawnPoint;
            }
        }
        return null;
    }

    //Convert dataset easting to an x pixel on the resized image.
    private double toPixelX(int easting, double imageWidth) {
        return (easting - MIN_EASTING) / (MAX_EASTING - MIN_EASTING) * imageWidth;
    }

    //Convert dataset northing to a y pixel on the resized image.
    private double toPixelY(int northing, double imageHeight) {
        return (MAX_NORTHING - northing) / (MAX_NORTHING - MIN_NORTHING) * imageHeight;
    }

    //colour assignment based on relative scaling to mean
    private Color getPollutionColour(double value, double average) {
        if (average <= 0) {
            return Color.GRAY;
        }
        if (value < average * 0.8) {
            return Color.GREEN;
        }
        if (value < average * 1.2) {
            return Color.YELLOW;
        }
        if (value < average * 1.5) {
            return Color.ORANGE;
        }
        return Color.RED;
    }

    //label fomratting
    private Label styledLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("map-toolbar-label");
        return label;
    }

    /**
     * App calls this to connect map-dot clicks to the Grid Detail panel.      
     */
    public void setDotClickListener(DotClickListener listener) {
        this.dotClickListener = listener;
    }
}
