package krpsim.model;

import java.util.Map;

public record Process(
    String name,
    Map<String, Integer> needs,
    Map<String, Integer> results,
    int delay
) {}