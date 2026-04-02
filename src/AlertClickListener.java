@FunctionalInterface
interface AlertClickListener {
    void onAlertClicked(DataPoint point, String pollutant, String year);
}