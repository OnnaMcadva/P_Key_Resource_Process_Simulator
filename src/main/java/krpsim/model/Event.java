package krpsim.model;

public record Event(int time, String processName) 
    implements Comparable<Event> {
    @Override
    public int compareTo(Event o) {
        return Integer.compare(this.time, o.time);
    }
}