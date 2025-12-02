package krpsim.model;

import java.util.Map;

/**
 * Process class represents a single process in the simulation.
 * 
 * A process has:
 * - a name: identifier of the process
 * - needs: a map of resources required to start the process
 *          (key = resource name, value = amount required)
 * - results: a map of resources produced when the process finishes
 *            (key = resource name, value = amount produced)
 * - delay: the number of time units the process takes to complete
 * 
 * This class is implemented as a Java record, which is an immutable data carrier.
 * Once a Process object is created, its fields cannot be modified.
 * Records automatically provide getters (name(), needs(), results(), delay()), 
 * equals(), hashCode(), and toString() methods.
 */
public record Process(
    String name,
    Map<String, Integer> needs,
    Map<String, Integer> results,
    int delay
) {}
