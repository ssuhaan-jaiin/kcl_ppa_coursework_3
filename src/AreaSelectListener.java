
/**
 * Listener that takes in two data points, used to connect to stat panel from map panel when
 * an area has been selected and user confirms to calculate average.
 *
 * @author (Shadid Miah)
 * @version (1.00)
 */
@FunctionalInterface
interface AreaSelectListener {
    void onAreaSelected(DataPoint point1, DataPoint point2);
}