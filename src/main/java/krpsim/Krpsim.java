package krpsim;

import krpsim.model.Event;
import krpsim.model.Process;
import krpsim.utils.Parser;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.PriorityQueue;
import java.util.Comparator;

/**
 * Krpsim is the main class for simulating the Key Resource Process system.
 * 
 * Usage: java Krpsim <configFile> <maxDelay>
 */
public class Krpsim {

    public static void main(String[] args) throws Exception {
        // System.setProperty("file.encoding", "UTF-8");
        System.out.println("🙂 🚀 🔧 🧩");

        if (args.length != 2) {
            System.out.println("\u001B[32mUsage:\u001B[0m krpsim <file> <maxDelay>");
            return;
        }

        String file = args[0];

        int maxDelay = 100;
        try {
            maxDelay = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("\u001B[32mIncorrect maxDelay argument, using the default value: 100\u001B[0m");
        }

        var config = Parser.parse(file);
        var result = simulate(config, maxDelay);

        System.out.println("Nice file! " + config.processes().size() + " processes, " +
            config.initialStocks().size() + " stocks, " + config.optimizeTargets().size() + " to optimize");

        System.out.println("Evaluating .................. done.");
        System.out.println("Main walk");

        // Print the simulation trace
        for (String line : result.trace) {
            System.out.println(line);
        }

        // Display whether simulation finished or reached max delay
        if (result.finished) {
            System.out.println("No more processes can be executed at time " + result.finalTime);
        } else {
            System.out.println("Reached delay " + maxDelay);
        }

        // Display final stock quantities
        System.out.println("Stock:");
        config.initialStocks().forEach((k, v) -> {
            int current = result.finalStocks.getOrDefault(k, 0);
            System.out.println(k + " => " + current);
        });
    }

    /**
     * A record to store the simulation results.
     */
    public record SimResult(
        List<String> trace,             // Trace of executed processes
        Map<String, Integer> finalStocks, // Final stock quantities
        int finalTime,                  // Last simulation time
        boolean finished                // Whether simulation finished all processes
    ) {}

    /**
     * Simulates the system given a configuration and maximum delay.
     * 
     * @param config Parsed configuration
     * @param maxDelay Maximum simulation time
     * @return Simulation results
     */
    public static SimResult simulate(Parser.Config config, int maxDelay) {
        Map<String, Integer> stocks = new HashMap<>(config.initialStocks());
        List<Process> processes = config.processes();
        Set<String> targets = config.optimizeTargets();

        PriorityQueue<Event> active = new PriorityQueue<>(); // Active processes
        List<String> trace = new ArrayList<>();
        int currentTime = 0;

        while (currentTime <= maxDelay) {
            // Complete all processes that are finished at current time
            while (!active.isEmpty() && active.peek().time() <= currentTime) {
                Event e = active.poll();
                Process p = findProcess(processes, e.processName());
                applyResults(stocks, p);
                trace.add(currentTime + ":" + e.processName());
            }

            // Find all runnable processes (enough resources)
            List<Process> candidates = getRunnable(stocks, processes);
            if (candidates.isEmpty()) {
                if (active.isEmpty()) break;
                currentTime = active.peek().time();
                continue;
            }

            // Choose the best process according to greedy strategy
            Process best = chooseBest(candidates, stocks, targets, maxDelay - currentTime);
            if (best == null) {
                if (active.isEmpty()) break;
                currentTime = active.peek().time();
                continue;
            }

            // Consume resources and schedule the process
            consumeResources(stocks, best);
            active.add(new Event(currentTime + best.delay(), best.name()));
            currentTime = active.isEmpty() ? currentTime + 1 : Math.min(currentTime + 1, active.peek().time());
        }

        // Complete remaining active processes
        while (!active.isEmpty()) {
            Event e = active.poll();
            Process p = findProcess(processes, e.processName());
            applyResults(stocks, p);
            trace.add(e.time() + ":" + e.processName());
        }

        int finalTime = trace.isEmpty() ? 0 : Integer.parseInt(trace.get(trace.size() - 1).split(":")[0]);
        boolean finished = active.isEmpty() && getRunnable(stocks, processes).isEmpty();

        return new SimResult(trace, new HashMap<>(stocks), finalTime, finished);
    }

    /**
     * Finds a process by name in the list of processes.
     */
    private static Process findProcess(List<Process> processes, String name) {
        return processes.stream().filter(p -> p.name().equals(name)).findFirst().orElse(null);
    }

    /**
     * Returns a list of processes that can currently run (enough resources).
     */
    private static List<Process> getRunnable(Map<String, Integer> stocks, List<Process> processes) {
        return processes.stream()
            .filter(p -> p.needs().entrySet().stream()
                .allMatch(e -> stocks.getOrDefault(e.getKey(), 0) >= e.getValue()))
            .toList();
    }

    /**
     * Consumes resources required by the process.
     */
    private static void consumeResources(Map<String, Integer> stocks, Process p) {
        p.needs().forEach((k, v) -> stocks.merge(k, -v, Integer::sum));
    }

    /**
     * Applies the results produced by the process to stocks.
     */
    private static void applyResults(Map<String, Integer> stocks, Process p) {
        p.results().forEach((k, v) -> stocks.merge(k, v, Integer::sum));
    }

    /**
     * Greedy strategy to choose the best process:
     * Maximizes contributions to target resources per unit delay.
     */
    private static Process chooseBest(List<Process> candidates, Map<String, Integer> stocks,
                                      Set<String> targets, int remaining) {
        return candidates.stream()
            .max(Comparator.comparingInt(p -> {
                int score = 0;
                for (var e : p.results().entrySet()) {
                    if (targets.contains(e.getKey())) {
                        score += e.getValue() * 1000; // prioritize target resources
                    } else {
                        score += e.getValue();
                    }
                }
                score = score * 1000 / Math.max(1, p.delay());
                return score;
            }))
            .orElse(null);
    }
}
