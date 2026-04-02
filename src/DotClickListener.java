//interace that allows connect to gridpanel for when dots are clicked. The mappanel doesn't know what will happen when a dot is clicked, it just notifies the listener (if set) with the relevant data.

@FunctionalInterface
interface DotClickListener {
    void onDotClicked(DataPoint point, String pollutant, String year);
}