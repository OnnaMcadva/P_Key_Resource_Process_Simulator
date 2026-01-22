package krpsim.utils;

import krpsim.model.Process;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser class is responsible for reading and parsing configuration files
 * for the simulation.
 * 
 * The configuration file can contain:
 * 1. Initial stocks of resources
 * 2. Processes (their requirements, results, and delay)
 * 3. Targets to optimize
 *
 * The class uses regular expressions to detect the different lines in the file.
 */
public class Parser {

    // Pattern to detect stock lines, e.g., "iron:5"
    // Must NOT contain parentheses to avoid matching process lines
    private static final Pattern STOCK_PATTERN = Pattern.compile("^([^#:()]+):(\\d+)$");

    // Pattern to detect process lines, e.g., "Proc1:(iron:2;wood:3):(plank:5):10"
    private static final Pattern PROCESS_PATTERN = Pattern.compile(
        "^([^#:]+):\\(([^)]*)\\):\\(([^)]*)\\):(\\d+)$"
    );

    // Pattern to detect optimization targets, e.g., "optimize:(plank;gear)"
    private static final Pattern OPTIMIZE_PATTERN = Pattern.compile("^optimize:\\((.*)\\)$");

    /**
     * Config record stores all parsed information from the file.
     *
     * @param initialStocks map of stock name -> quantity
     * @param processes list of all processes
     * @param optimizeTargets set of stock names to optimize
     */
    public record Config(
        Map<String, Integer> initialStocks,
        List<Process> processes,
        Set<String> optimizeTargets
    ) {}

    /**
     * Parses the given configuration file and returns a Config object.
     *
     * @param filename path to the configuration file
     * @return parsed configuration
     * @throws Exception if file reading fails
     */
    public static Config parse(String filename) throws Exception {
        Map<String, Integer> stocks = new HashMap<>();
        List<Process> processes = new ArrayList<>();
        Set<String> optimizeTargets = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                // Check for stock lines
                Matcher stockMatcher = STOCK_PATTERN.matcher(line);
                if (stockMatcher.matches()) {
                    stocks.put(stockMatcher.group(1), Integer.parseInt(stockMatcher.group(2)));
                    continue;
                }

                // Check for process lines
                Matcher processMatcher = PROCESS_PATTERN.matcher(line);
                if (processMatcher.matches()) {
                    String name = processMatcher.group(1);
                    Map<String, Integer> needs = parseMap(processMatcher.group(2));
                    Map<String, Integer> results = parseMap(processMatcher.group(3));
                    int delay = Integer.parseInt(processMatcher.group(4));
                    processes.add(new Process(name, needs, results, delay));
                    continue;
                }

                // Check for optimization targets
                Matcher optMatcher = OPTIMIZE_PATTERN.matcher(line);
                if (optMatcher.matches()) {
                    String[] targets = optMatcher.group(1).split(";");
                    for (String t : targets) {
                        optimizeTargets.add(t.trim());
                    }
                }
            }
        }

        return new Config(stocks, processes, optimizeTargets);
    }

    /**
     * Parses a semicolon-separated string like "iron:2;wood:3" into a map.
     *
     * @param input string to parse
     * @return map of resource name -> quantity
     */
    private static Map<String, Integer> parseMap(String input) {
        Map<String, Integer> map = new HashMap<>();
        if (input.isBlank()) return map;
        for (String part : input.split(";")) {
            String[] kv = part.split(":");
            if (kv.length == 2) {
                map.put(kv[0].trim(), Integer.parseInt(kv[1].trim()));
            }
        }
        return map;
    }
}
