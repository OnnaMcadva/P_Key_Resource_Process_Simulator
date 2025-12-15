package krpsim;

import krpsim.model.Process;
import krpsim.utils.Parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * Trace verification program for krpsim.
 * 
 * Expects trace to contain lines in the format "time:processName" representing process start times.
 * Validates that at each start time, sufficient resources are available to begin the process.
 *
 * Usage: java krpsim.KrpsimVerif <configFile> <traceFile>
 */
public class KrpsimVerif {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: krpsim_verif <config> <trace>");
            return;
        }
        var config = Parser.parse(args[0]);
        List<String> trace = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(args[1]))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank()) trace.add(line.trim());
            }
        }
        verify(config, trace);
    }

    private static void verify(Parser.Config config, List<String> trace) {
        Map<String,Integer> stocks = new LinkedHashMap<>(config.initialStocks());
        Map<String, Process> procMap = new HashMap<>();
        for (Process p : config.processes()) procMap.put(p.name(), p);

        // pending: key -> completion time
        Map<String,Integer> pending = new HashMap<>();
        int currentTime = 0;
        int lastCompletionTime = -1;

        for (String line : trace) {
            String[] parts = line.split(":", 2);
            if (parts.length != 2) {
                System.out.println("ERROR: Bad trace line: " + line);
                printFinal(stocks, currentTime);
                return;
            }
            int time;
            try { time = Integer.parseInt(parts[0]); }
            catch (NumberFormatException ex) {
                System.out.println("ERROR: Bad time in trace: " + parts[0]);
                printFinal(stocks, currentTime);
                return;
            }
            String procName = parts[1];

            // Apply completions up to time (inclusive of those finishing <= time)
            while (!pending.isEmpty() && pending.values().stream().min(Integer::compareTo).orElse(Integer.MAX_VALUE) <= time) {
                int tmin = pending.values().stream().min(Integer::compareTo).orElse(Integer.MAX_VALUE);
                // advance to tmin
                applyCompleted(stocks, pending, tmin, procMap);
                lastCompletionTime = Math.max(lastCompletionTime, tmin);
                currentTime = tmin;
            }
            // advance currentTime to trace time
            currentTime = Math.max(currentTime, time);

            Process p = procMap.get(procName);
            if (p == null) {
                System.out.println("ERROR: Unknown process " + procName + " at cycle " + time);
                printFinal(stocks, currentTime);
                return;
            }

            // Check if sufficient resources are available to start the process
            for (var need : p.needs().entrySet()) {
                int avail = stocks.getOrDefault(need.getKey(), 0);
                if (avail < need.getValue()) {
                    System.out.println("ERROR: Not enough " + need.getKey() + " at cycle " + time + " for " + procName);
                    printFinal(stocks, currentTime);
                    return;
                }
            }

            // Consume resources and schedule for completion
            p.needs().forEach((k,v) -> stocks.merge(k, -v, Integer::sum));
            pending.put(procName + "@" + time, time + p.delay());
        }

        // After all trace lines, finish remaining pending processes
        while (!pending.isEmpty()) {
            int tmin = pending.values().stream().min(Integer::compareTo).orElse(Integer.MAX_VALUE);
            applyCompleted(stocks, pending, tmin, procMap);
            lastCompletionTime = Math.max(lastCompletionTime, tmin);
            currentTime = tmin;
        }

        System.out.println("Trace is correct!");
        printFinal(stocks, (lastCompletionTime >= 0) ? (lastCompletionTime + 1) : currentTime);
    }

    private static void applyCompleted(Map<String,Integer> stocks, Map<String,Integer> pending, int time, Map<String,Process> procMap) {
        // Find all processes completing at <= time and apply their results
        List<String> toRemove = new ArrayList<>();
        for (var e : pending.entrySet()) {
            if (e.getValue() <= time) {
                String procKey = e.getKey();
                String procName = procKey.split("@")[0];
                Process p = procMap.get(procName);
                if (p != null) {
                    p.results().forEach((k,v) -> stocks.merge(k, v, Integer::sum));
                }
                toRemove.add(procKey);
            }
        }
        toRemove.forEach(pending::remove);
    }

    private static void printFinal(Map<String,Integer> stocks, int time) {
        System.out.println("Final stocks at cycle " + time + ":");
        stocks.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> System.out.println(e.getKey() + " => " + e.getValue()));
    }
}