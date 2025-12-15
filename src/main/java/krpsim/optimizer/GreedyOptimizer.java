package krpsim.optimizer;

import krpsim.model.Event;
import krpsim.model.Process;
import krpsim.utils.Parser;

import java.util.*;
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
        
        PriorityQueue<Event> active = new PriorityQueue<>();
        List<String> trace = new ArrayList<>();
        int currentTime = 0;
        boolean reachedDelay = false;
        int lastCompletionTime = -1;
        
        while (true) {
            // Apply all completions at currentTime
            while (!active.isEmpty() && active.peek().time() <= currentTime) {
                Event ev = active.poll();
                Process p = findProcess(processes, ev.processName());
                if (p != null) {
                    applyResults(stocks, p);
                }
                lastCompletionTime = Math.max(lastCompletionTime, ev.time());
            }
            
            // Try to start processes at current time
            boolean startedAny = false;
            
            if (currentTime > maxDelay) {
                reachedDelay = true;
                break;
            }
            
            List<Process> candidates = getRunnable(stocks, processes);
            
            if (!candidates.isEmpty()) {
                Comparator<Process> priority = Comparator.comparingInt(p -> {
                    int score = 0;
                    for (var e : p.results().entrySet()) {
                        if (optimize.contains(e.getKey())) score -= e.getValue();
                    }
                    return score;
                });
                
                List<Process> ordered;
                if (optimize.contains("time")) {
                    ordered = new ArrayList<>(processes);
                } else if (!Collections.disjoint(optimize, extractAllResultKeys(processes))) {
                    ordered = candidates.stream()
                        .sorted(priority.thenComparing(p -> processes.indexOf(p)))
                        .collect(Collectors.toList());
                } else {
                    ordered = new ArrayList<>(processes);
                }
                
                for (Process p : ordered) {
                    while (isRunnable(stocks, p)) {
                        if (currentTime > maxDelay) {
                            reachedDelay = true;
                            break;
                        }
                        consumeResources(stocks, p);
                        trace.add(currentTime + ":" + p.name());
                        active.add(new Event(currentTime + p.delay(), p.name()));
                        startedAny = true;
                    }
                    if (reachedDelay) break;
                }
                
                if (!startedAny) {
                    if (active.isEmpty()) break;
                    else currentTime = active.peek().time();
                }
            } else {
                if (active.isEmpty()) break;
                else currentTime = active.peek().time();
            }
        }
        
        // Complete remaining active processes
        while (!active.isEmpty()) {
            Event ev = active.poll();
            Process p = findProcess(processes, ev.processName());
            if (p != null) applyResults(stocks, p);
            lastCompletionTime = Math.max(lastCompletionTime, ev.time());
        }
        
        int finalTime = lastCompletionTime >= 0 ? lastCompletionTime : currentTime;
        boolean finished = active.isEmpty() && getRunnable(stocks, processes).isEmpty();
        double score = calculateScore(stocks, optimize, finalTime);
        
        return new OptimizationResult(List.copyOf(trace), new LinkedHashMap<>(stocks), finalTime, finished, score);
    }
    
    private Process findProcess(List<Process> processes, String name) {
        return processes.stream().filter(p -> p.name().equals(name)).findFirst().orElse(null);
    }
    
    private List<Process> getRunnable(Map<String, Integer> stocks, List<Process> processes) {
        return processes.stream().filter(p -> isRunnable(stocks, p)).toList();
    }
    
    private boolean isRunnable(Map<String, Integer> stocks, Process p) {
        for (var need : p.needs().entrySet()) {
            if (stocks.getOrDefault(need.getKey(), 0) < need.getValue()) return false;
        }
        return true;
    }
    
    private void consumeResources(Map<String, Integer> stocks, Process p) {
        p.needs().forEach((k, v) -> stocks.merge(k, -v, Integer::sum));
    }
    
    private void applyResults(Map<String, Integer> stocks, Process p) {
        p.results().forEach((k, v) -> stocks.merge(k, v, Integer::sum));
    }
    
    private Set<String> extractAllResultKeys(List<Process> processes) {
        Set<String> s = new HashSet<>();
        for (var p : processes) s.addAll(p.results().keySet());
        return s;
    }
    
    private double calculateScore(Map<String, Integer> stocks, Set<String> optimize, int finalTime) {
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
}
