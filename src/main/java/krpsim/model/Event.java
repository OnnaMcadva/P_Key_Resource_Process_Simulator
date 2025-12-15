package krpsim.model;

/**
 * Event class represents a single event in the simulation.
 * Each event has a timestamp (time) and the name of the associated process (processName).
 * 
 * This class implements Comparable<Event> so that events can be sorted by time.
 * Useful for PriorityQueue to always get the earliest event first.
 */
public record Event(int time, String processName) 
    implements Comparable<Event> {

    /**
     * Compare this event with another event based on time.
     * 
     * @param o the other Event to compare with
     * @return negative if this event occurs before the other,
     *         zero if they occur at the same time,
     *         positive if this event occurs after the other
     */
    @Override
    public int compareTo(Event other) {
        int c = Integer.compare(this.time, other.time);
        if (c != 0) return c;
        return this.processName.compareTo(other.processName);
    }
    // public int compareTo(Event o) {
    //     // Integer.compare handles the comparison safely and returns -1, 0, or 1
    //     return Integer.compare(this.time, o.time);
    // }
}
