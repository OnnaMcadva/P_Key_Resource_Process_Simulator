package krpsim.optimizer;

import krpsim.model.Event;
import krpsim.model.Process;
import krpsim.utils.Parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Greedy optimization strategy.
 * 
 * This is a fast heuristic approach that makes locally optimal choices at each step.
 * At each time point, it starts all processes that:
 * 1. Have sufficient resources available
 * 2. Produce the highest value according to optimization targets
 * 
 * Time complexity: O(T * P^2) where T is time and P is number of processes
 * Quality: Good for most cases, but may miss global optimum
 */
public class GreedyOptimizer implements OptimizationStrategy {
    
    @Override
    public String getName() {
        return "Greedy (Fast)";
    }
    
    @Override
    public OptimizationResult optimize(Parser.Config config, int maxDelay) {
        Map<String, Integer> stocks = new LinkedHashMap<>(config.initialStocks());
        List<Process> processes = config.processes();
        Set<String> optimize = config.optimizeTargets();
        
        // Build dependency graph: which processes are relevant for optimization targets
        Set<Process> relevantProcesses = buildRelevantProcessGraph(processes, optimize);
        
        // Active events are ordered by completion time; the earliest completion is processed first.
        PriorityQueue<Event> active = new PriorityQueue<>();
        // Trace keeps the start time and process name for reporting.
        List<String> trace = new ArrayList<>();
        int currentTime = 0;
        boolean reachedDelay = false;
        int lastCompletionTime = -1;
        
        while (true) {
            // Apply all completions at currentTime and update stocks.
            while (!active.isEmpty() && active.peek().time() <= currentTime) {
                Event ev = active.poll();
                Process p = findProcess(processes, ev.processName());
                if (p != null) {
                    applyResults(stocks, p);
                }
                lastCompletionTime = Math.max(lastCompletionTime, ev.time());
            }
            
            // Try to start processes at current time.
            boolean startedAny = false;
            
            if (currentTime > maxDelay) {
                reachedDelay = true;
                break;
            }
            
            // Collect all processes that are runnable with current stocks.
            List<Process> candidates = getRunnable(stocks, processes);
            
            // Filter only relevant processes that lead to optimization targets.
            if (!optimize.contains("time") || optimize.size() > 1) {
                // If we have specific resource targets (not just "time"), filter candidates
                candidates = candidates.stream()
                    .filter(relevantProcesses::contains)
                    .toList();
            }
            
            if (!candidates.isEmpty()) {
                // Prefer processes that produce more of the optimization targets.
                Comparator<Process> priority = Comparator.comparingInt(p -> {
                    int score = 0;
                    for (var e : p.results().entrySet()) {
                        if (optimize.contains(e.getKey())) score -= e.getValue();
                    }
                    return score;
                });
                
                List<Process> ordered;
                // If time is a target, do not re-order by resources; keep input order.
                if (optimize.contains("time")) {
                    ordered = new ArrayList<>(candidates);
                } else if (!Collections.disjoint(optimize, extractAllResultKeys(processes))) {
                    // Stable tie-breaker keeps deterministic output when scores match.
                    ordered = candidates.stream()
                        .sorted(priority.thenComparing(p -> processes.indexOf(p)))
                        .collect(Collectors.toList());
                } else {
                    // No overlap with optimization resources; keep runnable order.
                    ordered = new ArrayList<>(candidates);
                }

                // Start each runnable process once per tick to avoid
                // a runaway loop of identical starts in the same time unit.
                for (Process p : ordered) {
                    if (isRunnable(stocks, p)) {
                        if (currentTime > maxDelay) {
                            reachedDelay = true;
                            break;
                        }
                        // Consume inputs immediately and schedule a completion event.
                        consumeResources(stocks, p);
                        trace.add(currentTime + ":" + p.name());
                        active.add(new Event(currentTime + p.delay(), p.name()));
                        startedAny = true;
                    }
                }
                if (reachedDelay) break;

                if (startedAny) {
                    // Move to the nearest completion to apply results as soon as possible.
                    if (!active.isEmpty()) {
                        currentTime = active.peek().time();
                    } else {
                        break;
                    }
                } else {
                    // No process could start now; jump to next completion if any.
                    if (active.isEmpty()) break;
                    else currentTime = active.peek().time();
                }
            } else {
                // No candidates; only wait for the next completion or end.
                if (active.isEmpty()) break;
                else currentTime = active.peek().time();
            }
        }
        
        // Complete remaining active processes after loop termination.
        while (!active.isEmpty()) {
            Event ev = active.poll();
            Process p = findProcess(processes, ev.processName());
            if (p != null) applyResults(stocks, p);
            lastCompletionTime = Math.max(lastCompletionTime, ev.time());
        }
        
        // Resolve final time and compute score based on the requested objectives.
        int finalTime = lastCompletionTime >= 0 ? lastCompletionTime : currentTime;
        boolean finished = active.isEmpty() && getRunnable(stocks, processes).isEmpty();
        double score = calculateScore(stocks, optimize, finalTime);
        
        return new OptimizationResult(List.copyOf(trace), new LinkedHashMap<>(stocks), finalTime, finished, score);
    }
    
    private Process findProcess(List<Process> processes, String name) {
        // Linear search keeps behavior simple and preserves list order semantics.
        return processes.stream().filter(p -> p.name().equals(name)).findFirst().orElse(null);
    }
    
    private List<Process> getRunnable(Map<String, Integer> stocks, List<Process> processes) {
        // Runnable means all required inputs are available in current stocks.
        return processes.stream().filter(p -> isRunnable(stocks, p)).toList();
    }
    
    private boolean isRunnable(Map<String, Integer> stocks, Process p) {
        // Verify every required resource has enough quantity.
        for (var need : p.needs().entrySet()) {
            if (stocks.getOrDefault(need.getKey(), 0) < need.getValue()) return false;
        }
        return true;
    }
    
    private void consumeResources(Map<String, Integer> stocks, Process p) {
        // Remove required resources at process start.
        p.needs().forEach((k, v) -> stocks.merge(k, -v, Integer::sum));
    }
    
    private void applyResults(Map<String, Integer> stocks, Process p) {
        // Add produced resources when the process finishes.
        p.results().forEach((k, v) -> stocks.merge(k, v, Integer::sum));
    }
    
    private Set<String> extractAllResultKeys(List<Process> processes) {
        Set<String> s = new HashSet<>();
        // Collect all result resource names to detect overlap with optimization targets.
        for (var p : processes) s.addAll(p.results().keySet());
        return s;
    }
    
    private double calculateScore(Map<String, Integer> stocks, Set<String> optimize, int finalTime) {
        // Higher resource totals increase score; shorter time reduces penalty.
        double score = 0;
        for (var e : stocks.entrySet()) {
            if (optimize.contains(e.getKey())) {
                score += e.getValue() * 1000.0;
            }
        }
        if (optimize.contains("time")) {
            score -= finalTime * 10.0;
        }
        return score;
    }
    
    /**
     * Builds a set of processes that are relevant for achieving optimization targets.
     * Uses backward dependency analysis: starts from target resources and includes
     * all processes that directly or indirectly produce them.
     * 
     * @param processes all available processes
     * @param optimize optimization targets (resources or "time")
     * @return set of processes that contribute to optimization targets
     */
    private Set<Process> buildRelevantProcessGraph(List<Process> processes, Set<String> optimize) {
        Set<Process> relevant = new HashSet<>();
        Set<String> neededResources = new HashSet<>();
        
        // Special case: if "time" is the only target, ALL processes are relevant
        if (optimize.contains("time") && optimize.size() == 1) {
            return new HashSet<>(processes);
        }
        
        // Start with non-"time" optimization targets
        for (String target : optimize) {
            if (!target.equals("time")) {
                neededResources.add(target);
            }
        }
        
        // Iteratively add processes that produce needed resources
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Process p : processes) {
                if (relevant.contains(p)) continue;
                
                // Check if this process produces any needed resource
                boolean producesNeeded = p.results().keySet().stream()
                    .anyMatch(neededResources::contains);
                
                if (producesNeeded) {
                    relevant.add(p);
                    // Add this process's needs to the needed resources
                    neededResources.addAll(p.needs().keySet());
                    changed = true;
                }
            }
        }
        
        return relevant;
    }
}
