package krpsim.utils;

import krpsim.model.Process;
import krpsim.model.Stock;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private static final Pattern STOCK_PATTERN = Pattern.compile("^([^#].*?):(\\d+)$");
    private static final Pattern PROCESS_PATTERN = Pattern.compile(
        "^([^#:]+):\\(([^)]*)\\):\\(([^)]*)\\):(\\d+)$"
    );
    private static final Pattern OPTIMIZE_PATTERN = Pattern.compile("^optimize:\\((.*)\\)$");

    public record Config(
        Map<String, Integer> initialStocks,
        List<Process> processes,
        Set<String> optimizeTargets
    ) {}

    public static Config parse(String filename) throws Exception {
        Map<String, Integer> stocks = new HashMap<>();
        List<Process> processes = new ArrayList<>();
        Set<String> optimizeTargets = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                Matcher stockMatcher = STOCK_PATTERN.matcher(line);
                if (stockMatcher.matches()) {
                    stocks.put(stockMatcher.group(1), Integer.parseInt(stockMatcher.group(2)));
                    continue;
                }

                Matcher processMatcher = PROCESS_PATTERN.matcher(line);
                if (processMatcher.matches()) {
                    String name = processMatcher.group(1);
                    Map<String, Integer> needs = parseMap(processMatcher.group(2));
                    Map<String, Integer> results = parseMap(processMatcher.group(3));
                    int delay = Integer.parseInt(processMatcher.group(4));
                    processes.add(new Process(name, needs, results, delay));
                    continue;
                }

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