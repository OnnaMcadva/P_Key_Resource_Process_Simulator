package krpsim;

import krpsim.model.Process;
import krpsim.utils.Parser;

import java.io.BufferedReader;
import java.io.FileReader;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * KrpsimVerif is a program to verify a simulation trace
 * against a given configuration file.
 * 
 * Usage: java KrpsimVerif <configFile> <traceFile>
 */
public class KrpsimVerif {

    public static void main(String[] args) throws Exception {
        // Check if correct number of arguments is provided
        if (args.length != 2) {
            System.out.println("Usage: krpsim_verif <config> <trace>");
            return;
        }

        String configFile = args[0];
        String traceFile = args[1];

        // Parse configuration file using Parser
        var config = Parser.parse(configFile);

        // Read trace file lines into a list
        List<String> trace = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(traceFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) trace.add(line.trim());
            }
        }

        // Verify the trace against the configuration
        verify(config, trace);
    }

    /**
     * Verifies the simulation trace against the configuration.
     * @param config The parsed configuration object
     * @param trace The list of trace lines
     */
    private static void verify(Parser.Config config, List<String> trace) {
        // Initialize current stocks and process map
        Map<String, Integer> stocks = new HashMap<>(config.initialStocks());
        Map<String, Process> processMap = new HashMap<>();
        config.processes().forEach(p -> processMap.put(p.name(), p));

        // Track pending processes and their end times
        Map<String, Integer> pending = new HashMap<>();
        int currentTime = 0;

        for (String line : trace) {
            String[] parts = line.split(":", 2);
            int time = Integer.parseInt(parts[0]);
            String procName = parts[1];

            // Advance time and apply completed processes
            while (currentTime < time) {
                applyCompleted(stocks, pending, currentTime, processMap);
                currentTime++;
            }

            Process p = processMap.get(procName);
            if (p == null) {
                System.out.println("ERROR: Unknown process " + procName + " at cycle " + time);
                printFinal(stocks, time);
                return;
            }

            // Check if enough resources are available to start the process
            for (var need : p.needs().entrySet()) {
                int available = stocks.getOrDefault(need.getKey(), 0);
                if (available < need.getValue()) {
                    System.out.println("ERROR: Not enough " + need.getKey() +
                        " at cycle " + time + " for " + procName);
                    printFinal(stocks, time);
                    return;
                }
            }

            // Consume resources and add process to pending list
            consume(stocks, p);
            pending.put(procName + "@" + time, time + p.delay());
        }

        // Finish all pending processes
        while (!pending.isEmpty()) {
            applyCompleted(stocks, pending, currentTime, processMap);
            currentTime++;
        }

        // Print success message and final stock values
        System.out.println("Trace is correct!");
        printFinal(stocks, currentTime - 1);
    }

    /**
     * Applies results of all processes that have completed at the current time.
     * @param stocks Current stock quantities
     * @param pending Pending processes with their end times
     * @param time Current simulation time
     * @param processMap Map of process name to Process object
     */
    private static void applyCompleted(Map<String, Integer> stocks, Map<String, Integer> pending,
                                       int time, Map<String, Process> processMap) {
        pending.entrySet().removeIf(e -> {
            if (e.getValue() <= time) {
                String procName = e.getKey().split("@")[0];
                Process p = processMap.get(procName);
                if (p != null) applyResults(stocks, p);
                return true;
            }
            return false;
        });
    }

    /**
     * Consumes resources required by a process.
     * @param stocks Current stock quantities
     * @param p Process to consume resources for
     */
    private static void consume(Map<String, Integer> stocks, Process p) {
        p.needs().forEach((k, v) -> stocks.merge(k, -v, Integer::sum));
    }

    /**
     * Applies the results produced by a process to the stock.
     * @param stocks Current stock quantities
     * @param p Process whose results are applied
     */
    private static void applyResults(Map<String, Integer> stocks, Process p) {
        p.results().forEach((k, v) -> stocks.merge(k, v, Integer::sum));
    }

    /**
     * Prints the final stock quantities at the end of simulation.
     * @param stocks Current stock quantities
     * @param time Simulation time
     */
    private static void printFinal(Map<String, Integer> stocks, int time) {
        System.out.println("Final stocks at cycle " + time + ":");
        stocks.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> System.out.println(e.getKey() + " => " + e.getValue()));
    }
}
