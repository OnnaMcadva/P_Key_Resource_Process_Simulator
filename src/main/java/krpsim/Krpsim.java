package krpsim;

import krpsim.model.Event;
import krpsim.model.Process;
import krpsim.utils.Parser;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Main simulation class for the Key Resource Process Simulator (krpsim).
 * 
 * This program simulates a process scheduling system with resource constraints,
 * optimizing for either time or specific resource production targets.
 *
 * Usage: java -cp <classpath> krpsim.Krpsim <configFile> <maxDelay>
 */
public class Krpsim {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: krpsim <configFile> <maxDelay>");
            return;
        }

        String file = args[0];
        int maxDelay;
        try { maxDelay = Integer.parseInt(args[1]); }
        catch (NumberFormatException ex) {
            System.out.println("Second argument must be integer maxDelay");
            return;
        }

        var config = Parser.parse(file);
        SimResult result = simulate(config, maxDelay);

        System.err.println("Nice file! " + config.processes().size() + " processes, " +
            config.initialStocks().size() + " stocks, " + config.optimizeTargets().size() + " to optimize");
        System.err.println("Evaluating .................. done.");
        System.err.println("Main walk");

        for (String line : result.trace) {
            System.out.println(line);
        }

        if (result.finished) {
            System.err.println("no more process doable at time " + result.finalTime);
        } else {
            System.err.println("Reached delay " + maxDelay);
        }

        System.err.println("Stock:");
        // Print stocks in alphabetical order like example
        result.finalStocks.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> System.err.println(e.getKey() + " => " + e.getValue()));
    }

    public record SimResult(List<String> trace, Map<String,Integer> finalStocks, int finalTime, boolean finished) {}

    /**
     * Simulates the process scheduling system.
     *
     * Rules:
     * - Trace contains lines "<startTime>:<processName>" representing process start times.
     * - Resources are consumed when a process starts.
     * - Results are applied when the process completes (startTime + delay).
     * - Multiple instances of the same process can start in the same cycle if resources permit.
     * - If optimize contains "time", processes are started as quickly as possible (resource permitting).
     * - If optimize contains resource names, priority is given to processes that produce those resources.
     * 
     * @param config the parsed configuration containing stocks, processes, and optimization targets
     * @param maxDelay the maximum simulation time
     * @return simulation results including trace, final stocks, and completion status
     */
    public static SimResult simulate(Parser.Config config, int maxDelay) {
        Map<String,Integer> stocks = new LinkedHashMap<>(config.initialStocks());
        List<Process> processes = config.processes();
        Set<String> optimize = config.optimizeTargets();

        PriorityQueue<Event> active = new PriorityQueue<>(); // completion events
        List<String> trace = new ArrayList<>();
        int currentTime = 0;
        boolean reachedDelay = false;

        // last completion time (for printing finalTime)
        int lastCompletionTime = -1;

        while (true) {
            // 1) Apply all completions that occur at currentTime:
            while (!active.isEmpty() && active.peek().time() <= currentTime) {
                Event ev = active.poll();
                Process p = findProcess(processes, ev.processName());
                if (p != null) {
                    applyResults(stocks, p);
                }
                lastCompletionTime = Math.max(lastCompletionTime, ev.time());
            }

            // 2) Attempt to start processes at the current time.
            //    We allow multiple starts of the same process in a single cycle.
            boolean startedAny = false;

            // If current time exceeds maxDelay, stop starting new processes.
            if (currentTime > maxDelay) {
                reachedDelay = true;
                break;
            }

            // Form the order of candidate processes.
            List<Process> candidates = getRunnable(stocks, processes);

            if (!candidates.isEmpty()) {
                // If optimize contains specific resources (not "time"), give priority to processes
                // that produce those resources.
                Comparator<Process> priority = Comparator.comparingInt(p -> {
                    int score = 0;
                    for (var e : p.results().entrySet()) {
                        if (optimize.contains(e.getKey())) score -= e.getValue(); // lower score => earlier in sort
                    }
                    return score;
                });

                List<Process> ordered;
                if (optimize.contains("time")) {
                    // If optimizing for time, start processes in definition order (consume resources quickly).
                    ordered = new ArrayList<>(processes);
                } else if (!Collections.disjoint(optimize, extractAllResultKeys(processes))) {
                    // Target resources exist - sort to prioritize processes that produce target resources
                    ordered = candidates.stream()
                            .sorted(priority.thenComparing(p -> processes.indexOf(p)))
                            .collect(Collectors.toList());
                } else {
                    // Default: in file definition order
                    ordered = new ArrayList<>(processes);
                }

                // For each process: start while possible (multiple times) - but only among candidates
                for (Process p : ordered) {
                    // skip if p not runnable at this moment
                    while (isRunnable(stocks, p)) {
                        // Check time bounds again - if we've exceeded maxDelay, don't start new processes.
                        if (currentTime > maxDelay) {
                            reachedDelay = true;
                            break;
                        }
                        // Start process p at current time:
                        consumeResources(stocks, p);
                        trace.add(currentTime + ":" + p.name()); // record the start
                        active.add(new Event(currentTime + p.delay(), p.name()));
                        startedAny = true;
                    }
                    if (reachedDelay) break;
                }

                if (startedAny) {
                    // After starting processes at the same currentTime, we remain at the same currentTime
                    // to process completions on the next iteration. This avoids a busy-loop if no completions exist.
                } else {
                    // Nothing started: if there are active processes, move to next completion;
                    // otherwise, the system is finished.
                    if (active.isEmpty()) {
                        break; // no active processes, nothing can start -> finish
                    } else {
                        // move to the next event
                        currentTime = active.peek().time();
                    }
                }
            } else {
                // No candidates available now
                if (active.isEmpty()) {
                    // Finish simulation
                    break;
                } else {
                    // Jump to the time of the next completion
                    currentTime = active.peek().time();
                }
            }
        }

        // After stopping new process starts, apply all remaining completions (if any)
        while (!active.isEmpty()) {
            Event ev = active.poll();
            Process p = findProcess(processes, ev.processName());
            if (p != null) applyResults(stocks, p);
            lastCompletionTime = Math.max(lastCompletionTime, ev.time());
        }

        int finalTime;
        boolean finished;
        if (!reachedDelay) {
            // Successfully reached the end (no active processes and nothing to run)
            finished = true;
            finalTime = (lastCompletionTime >= 0) ? (lastCompletionTime + 1) : 0;
        } else {
            finished = false;
            finalTime = Math.min(lastCompletionTime >= 0 ? lastCompletionTime + 1 : 0, maxDelay);
        }

        return new SimResult(List.copyOf(trace), new LinkedHashMap<>(stocks), finalTime, finished);
    }

    private static Process findProcess(List<Process> processes, String name) {
        return processes.stream().filter(p -> p.name().equals(name)).findFirst().orElse(null);
    }

    private static List<Process> getRunnable(Map<String,Integer> stocks, List<Process> processes) {
        return processes.stream().filter(p -> isRunnable(stocks, p)).toList();
    }

    private static boolean isRunnable(Map<String,Integer> stocks, Process p) {
        for (var need : p.needs().entrySet()) {
            if (stocks.getOrDefault(need.getKey(), 0) < need.getValue()) return false;
        }
        return true;
    }

    private static void consumeResources(Map<String,Integer> stocks, Process p) {
        p.needs().forEach((k,v) -> stocks.merge(k, -v, Integer::sum));
    }

    private static void applyResults(Map<String,Integer> stocks, Process p) {
        p.results().forEach((k,v) -> stocks.merge(k, v, Integer::sum));
    }

    private static Set<String> extractAllResultKeys(List<Process> processes) {
        Set<String> s = new HashSet<>();
        for (var p : processes) s.addAll(p.results().keySet());
        return s;
    }
}