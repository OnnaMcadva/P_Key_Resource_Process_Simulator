
package krpsim.optimizer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.PriorityQueue;
import java.util.TreeSet;

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

/**
 * Branch and Bound optimization with A* heuristic.
 * 
 * This algorithm performs an exhaustive search with pruning. It explores all possible
 * process schedules but uses bounds to eliminate branches that cannot lead to better
 * solutions. The A* heuristic guides the search toward promising areas.
 * 
 * Algorithm:
 * 1. Maintain priority queue of states ordered by f(state) = g(state) + h(state)
 *    where g = actual cost so far, h = optimistic estimate of remaining value
 * 2. Expand best state by trying all possible process starts
 * 3. Prune states that cannot beat current best
 * 4. Continue until time limit or queue empty
 * 
 * Time complexity: Potentially exponential, but bounded by time limit
 * Quality: Optimal or near-optimal (depending on time limit)
 */
public class BranchAndBoundOptimizer implements OptimizationStrategy {
    
    private static final long DEFAULT_TIME_LIMIT_MS = 5000; // 5 seconds
    private final long timeLimitMs;
    private long startTime;
    
    public BranchAndBoundOptimizer() {
        this(DEFAULT_TIME_LIMIT_MS);
    }
    
    public BranchAndBoundOptimizer(long timeLimitMs) {
        this.timeLimitMs = timeLimitMs;
    }
    
    @Override
    public String getName() {
        return "Branch & Bound A* (limit=" + timeLimitMs + "ms)";
    }
    
    /**
     * Search state with A* scoring.
     */
    private static class SearchState implements Comparable<SearchState> {
        Map<String, Integer> stocks;
        PriorityQueue<Event> activeProcesses;
        List<String> trace;
        int currentTime;
        double actualScore; // g(n): actual score achieved so far
        double estimatedTotal; // f(n) = g(n) + h(n): total estimated score
        
        SearchState(Map<String, Integer> stocks, PriorityQueue<Event> active,
                   List<String> trace, int time, double actual, double estimated) {
            this.stocks = new LinkedHashMap<>(stocks);
            this.activeProcesses = new PriorityQueue<>(active);
            this.trace = new ArrayList<>(trace);
            this.currentTime = time;
            this.actualScore = actual;
            this.estimatedTotal = estimated;
        }
        
        SearchState copy() {
            return new SearchState(stocks, activeProcesses, trace, currentTime, 
                                 actualScore, estimatedTotal);
        }
        
        @Override
        public int compareTo(SearchState other) {
            // Higher estimated total is better
            return Double.compare(other.estimatedTotal, this.estimatedTotal);
        }
    }
    
    @Override
    public OptimizationResult optimize(Parser.Config config, int maxDelay) {
        startTime = System.currentTimeMillis();
        
        List<Process> processes = config.processes();
        Set<String> optimize = config.optimizeTargets();
        Map<String, Integer> baseline = new HashMap<>();
        // Baseline = 0 for optimize targets so spending (money) is not penalized; only gains matter
        for (String k : optimize) baseline.put(k, 0);
        
        // Priority queue for A* search
        PriorityQueue<SearchState> openSet = new PriorityQueue<>();
        
        SearchState initialState = new SearchState(
            new LinkedHashMap<>(config.initialStocks()),
            new PriorityQueue<>(),
            new ArrayList<>(),
            0,
            0.0,
            estimateUpperBound(new LinkedHashMap<>(config.initialStocks()), 0, 
                             processes, optimize, maxDelay, baseline)
        );
        openSet.add(initialState);
        
        SearchState bestSolution = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        int statesExplored = 0;
        
        while (!openSet.isEmpty()) {
            // Check time limit
            if (System.currentTimeMillis() - startTime > timeLimitMs) {
                break;
            }
            
            SearchState current = openSet.poll();
            statesExplored++;
            
            // Prune if this state cannot beat best known solution
            if (current.estimatedTotal < bestScore) {
                continue;
            }
            
            // Apply completions
            int lastCompletionTime = current.currentTime;
            while (!current.activeProcesses.isEmpty() && 
                   current.activeProcesses.peek().time() <= current.currentTime) {
                Event ev = current.activeProcesses.poll();
                Process p = findProcess(processes, ev.processName());
                if (p != null) {
                    applyResults(current.stocks, p);
                }
                lastCompletionTime = Math.max(lastCompletionTime, ev.time());
            }
            
            // Update actual score
            current.actualScore = calculateActualScore(current.stocks, optimize, current.currentTime, baseline);
            
            // Check if exceeded time limit
            if (current.currentTime > maxDelay) {
                if (current.actualScore > bestScore) {
                    bestScore = current.actualScore;
                    bestSolution = current;
                }
                continue;
            }
            
            // Get runnable processes
            List<Process> candidates = getRunnable(current.stocks, processes);
            
            if (candidates.isEmpty()) {
                // Terminal state
                if (current.activeProcesses.isEmpty()) {
                    double finalScore = calculateActualScore(current.stocks, optimize, lastCompletionTime, baseline);
                    if (finalScore > bestScore) {
                        bestScore = finalScore;
                        bestSolution = current.copy();
                        bestSolution.currentTime = lastCompletionTime;
                    }
                } else {
                    // Advance to next completion
                    SearchState advanced = current.copy();
                    advanced.currentTime = advanced.activeProcesses.peek().time();
                    advanced.estimatedTotal = advanced.actualScore + 
                        estimateRemainingValue(advanced.stocks, advanced.currentTime, 
                                              processes, optimize, maxDelay, baseline);
                    if (advanced.estimatedTotal >= bestScore) {
                        openSet.add(advanced);
                    }
                }
            } else {
                // Expand state by trying each candidate
                for (Process p : candidates) {
                    SearchState newState = current.copy();
                    consumeResources(newState.stocks, p);
                    newState.trace.add(newState.currentTime + ":" + p.name());
                    newState.activeProcesses.add(new Event(newState.currentTime + p.delay(), p.name()));
                    
                    newState.actualScore = calculateActualScore(newState.stocks, optimize, newState.currentTime, baseline);
                    newState.estimatedTotal = newState.actualScore + 
                        estimateRemainingValue(newState.stocks, newState.currentTime, 
                                              processes, optimize, maxDelay, baseline);
                    
                    // Only add if this could potentially beat best
                    if (newState.estimatedTotal >= bestScore) {
                        openSet.add(newState);
                    }
                }
            }
        }
        
        // Complete remaining processes in best solution
        if (bestSolution != null) {
            int finalTime = bestSolution.currentTime;
            while (!bestSolution.activeProcesses.isEmpty()) {
                Event ev = bestSolution.activeProcesses.poll();
                Process p = findProcess(processes, ev.processName());
                if (p != null) {
                    applyResults(bestSolution.stocks, p);
                }
                finalTime = Math.max(finalTime, ev.time());
            }
            
            boolean finished = bestSolution.activeProcesses.isEmpty() && 
                             getRunnable(bestSolution.stocks, processes).isEmpty();
            double finalScore = calculateActualScore(bestSolution.stocks, optimize, finalTime, baseline);
            
            return new OptimizationResult(
                List.copyOf(bestSolution.trace),
                new LinkedHashMap<>(bestSolution.stocks),
                finalTime,
                finished,
                finalScore
            );
        }
        
        // Fallback to greedy only if absolutely nothing was found
        if (bestSolution == null) {
            OptimizationStrategy fallback = new GreedyOptimizer();
            return fallback.optimize(config, maxDelay);
        }
        
        // Should not reach here since we always have initial state
        return new OptimizationResult(List.of(), new LinkedHashMap<>(), 0, true, 0.0);
    }
    
    /**
     * Optimistic upper bound on total value achievable from this state.
     * Assumes infinite resources and parallelism.
     */
    private double estimateUpperBound(Map<String, Integer> stocks, int currentTime,
                                     List<Process> processes, Set<String> optimize, int maxDelay,
                                     Map<String, Integer> baseline) {
        double currentValue = calculateActualScore(stocks, optimize, currentTime, baseline);
        double potentialValue = estimateRemainingValue(stocks, currentTime, processes, optimize, maxDelay, baseline);
        return currentValue + potentialValue;
    }
    
    /**
     * Optimistic estimate of value that can still be gained.
     */
    private double estimateRemainingValue(Map<String, Integer> stocks, int currentTime,
                                         List<Process> processes, Set<String> optimize, int maxDelay,
                                         Map<String, Integer> baseline) {
        int remainingTime = maxDelay - currentTime;
        if (remainingTime <= 0) return 0;
        
        // Find the best value per time unit from all processes
        double bestRatePerTime = 0;
        for (Process p : processes) {
            if (p.delay() == 0) continue;
            double value = 0;
            for (var e : p.results().entrySet()) {
                if (optimize.contains(e.getKey())) {
                    int base = baseline.getOrDefault(e.getKey(), 0);
                    value += Math.max(0, e.getValue() - base) * 1000.0;
                }
            }
            double rate = value / p.delay();
            bestRatePerTime = Math.max(bestRatePerTime, rate);
        }
        
        // Optimistic assumption: we can achieve best rate continuously
        return bestRatePerTime * remainingTime;
    }
    
    private double calculateActualScore(Map<String, Integer> stocks, Set<String> optimize, int currentTime,
                                       Map<String, Integer> baseline) {
        double score = 0;
        for (var e : stocks.entrySet()) {
            if (optimize.contains(e.getKey())) {
                int base = baseline.getOrDefault(e.getKey(), 0);
                score += (e.getValue() - base) * 1000.0;
            }
        }
        if (optimize.contains("time")) {
            score -= currentTime * 10.0;
        }
        return score;
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
}

