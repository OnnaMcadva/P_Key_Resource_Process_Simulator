package krpsim.model;

/**
 * Stock class represents a resource in the simulation.
 * 
 * A stock has:
 * - a name: the identifier of the resource
 * - quantity: the amount of this resource currently available
 * 
 * This class is implemented as a Java record, which is an immutable data carrier.
 * Once a Stock object is created, its fields cannot be modified.
 * Records automatically provide getters (name(), quantity()), 
 * equals(), hashCode(), and toString() methods.
 */
public record Stock(String name, int quantity) {}
