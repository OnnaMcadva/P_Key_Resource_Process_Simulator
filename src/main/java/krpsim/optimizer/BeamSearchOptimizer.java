package krpsim.optimizer;

import krpsim.model.Event;
import krpsim.model.Process;
import krpsim.utils.Parser;

import java.util.*;

/**
 * Beam Search optimization strategy.
 * 
 * This algorithm explores multiple solution paths simultaneously, keeping only
 * the most promising candidates (beam width) at each step. It provides a good
 * balance between solution quality and computational cost.
 * 
 * Algorithm:
 * 1. Start with initial state
 * 2. Generate all possible next states (process starts)
 * 3. Evaluate each state with heuristic function
 * 4. Keep only top N states (beam width)
 * 5. Repeat until time limit or no more moves
 * 
 * Time complexity: O(T * P * B) where T is time, P is processes, B is beam width
 * Quality: Very good - explores alternative paths that greedy might miss
 */
public class BeamSearchOptimizer implements OptimizationStrategy {
    
    private static final int DEFAULT_BEAM_WIDTH = 8;
    private final int beamWidth;
    
    public BeamSearchOptimizer() {
        this(DEFAULT_BEAM_WIDTH);
    }
    
    public BeamSearchOptimizer(int beamWidth) {
        this.beamWidth = beamWidth;
    }
    
    @Override
    public String getName() {
        return "Beam Search (width=" + beamWidth + ")";
    }
    
    /**
     * Represents a simulation state during beam search.
     */
    private static class SearchState implements Comparable<SearchState> {
        Map<String, Integer> stocks;
        PriorityQueue<Event> activeProcesses;
        List<String> trace;
        int currentTime;
        double heuristicScore;
        
        SearchState(Map<String, Integer> stocks, PriorityQueue<Event> active, 
                   List<String> trace, int time, double score) {
            this.stocks = new LinkedHashMap<>(stocks);
            this.activeProcesses = new PriorityQueue<>(active);
            this.trace = new ArrayList<>(trace);
            this.currentTime = time;
            this.heuristicScore = score;
        }
        
        SearchState copy() {
            return new SearchState(stocks, activeProcesses, trace, currentTime, heuristicScore);
        }
        
        @Override
        public int compareTo(SearchState other) {
            return Double.compare(other.heuristicScore, this.heuristicScore); // descending
        }
    }
    
    @Override
    public OptimizationResult optimize(Parser.Config config, int maxDelay) {
        List<Process> processes = config.processes();
        Set<String> optimize = config.optimizeTargets();
        Map<String, Integer> baseline = new HashMap<>();
        // Baseline = 0 for optimize targets: we reward gains above zero, spending allowed
        for (String k : optimize) baseline.put(k, 0);
        
        // Initialize beam with starting state
        PriorityQueue<SearchState> beam = new PriorityQueue<>();
        SearchState initialState = new SearchState(
            new LinkedHashMap<>(config.initialStocks()),
            new PriorityQueue<>(),
            new ArrayList<>(),
            0,
            0.0
        );
        beam.add(initialState);
        
        SearchState bestFinalState = null;
        double bestFinalScore = Double.NEGATIVE_INFINITY;
        
        // Main beam search loop
        while (!beam.isEmpty()) {
            PriorityQueue<SearchState> nextBeam = new PriorityQueue<>();
            
            for (SearchState state : beam) {
                // Apply completions
                while (!state.activeProcesses.isEmpty() && 
                       state.activeProcesses.peek().time() <= state.currentTime) {
                    Event ev = state.activeProcesses.poll();
                    Process p = findProcess(processes, ev.processName());
                    if (p != null) {
                        applyResults(state.stocks, p);
                    }
                }
                
                // Check if we've exceeded time limit
                if (state.currentTime > maxDelay) {
                    double finalScore = calculateScore(state.stocks, optimize, state.currentTime, baseline);
                    if (finalScore > bestFinalScore) {
                        bestFinalScore = finalScore;
                        bestFinalState = state;
                    }
                    continue;
                }
                
                // Generate successor states by trying to start each runnable process
                List<Process> candidates = getRunnable(state.stocks, processes);
                
                if (candidates.isEmpty()) {
                    // No more processes can start
                    if (state.activeProcesses.isEmpty()) {
                        // Finished state
                        double finalScore = calculateScore(state.stocks, optimize, state.currentTime, baseline);
                        if (finalScore > bestFinalScore) {
                            bestFinalScore = finalScore;
                            bestFinalState = state;
                        }
                    } else {
                        // Advance to next completion
                        SearchState advanced = state.copy();
                        advanced.currentTime = advanced.activeProcesses.peek().time();
                        advanced.heuristicScore = calculateHeuristic(advanced.stocks, advanced.currentTime, 
                                                                     optimize, maxDelay, processes, baseline);
                        nextBeam.add(advanced);
                    }
                } else {
                    // Try starting each candidate process ONCE per tick
                    for (Process p : candidates) {
                        SearchState newState = state.copy();
                        consumeResources(newState.stocks, p);
                        newState.trace.add(newState.currentTime + ":" + p.name());
                        newState.activeProcesses.add(new Event(newState.currentTime + p.delay(), p.name()));
                        newState.heuristicScore = calculateHeuristic(newState.stocks, newState.currentTime, 
                                                                     optimize, maxDelay, processes, baseline);
                        nextBeam.add(newState);
                    }
                    
                    // Also consider "wait" action - advance time without starting anything
                    if (!state.activeProcesses.isEmpty()) {
                        SearchState waitState = state.copy();
                        waitState.currentTime = Math.min(waitState.currentTime + 1, 
                                                        waitState.activeProcesses.peek().time());
                        waitState.heuristicScore = calculateHeuristic(waitState.stocks, waitState.currentTime, 
                                                                      optimize, maxDelay, processes, baseline);
                        nextBeam.add(waitState);
                    }
                }
            }
            
            // Keep only top beamWidth states
            beam.clear();
            int count = 0;
            while (!nextBeam.isEmpty() && count < beamWidth) {
                beam.add(nextBeam.poll());
                count++;
            }
            
            // Safety check: if beam is empty, we're done
            if (beam.isEmpty()) break;
        }
        
        // Complete remaining processes in best state
        if (bestFinalState != null) {
            while (!bestFinalState.activeProcesses.isEmpty()) {
                Event ev = bestFinalState.activeProcesses.poll();
                Process p = findProcess(processes, ev.processName());
                if (p != null) {
                    applyResults(bestFinalState.stocks, p);
                }
            }
            
            int finalTime = bestFinalState.currentTime;
            boolean finished = bestFinalState.activeProcesses.isEmpty() && 
                             getRunnable(bestFinalState.stocks, processes).isEmpty();
            double score = calculateScore(bestFinalState.stocks, optimize, finalTime, baseline);
            OptimizationResult beamResult = new OptimizationResult(
                List.copyOf(bestFinalState.trace),
                new LinkedHashMap<>(bestFinalState.stocks),
                finalTime,
                finished,
                score
            );
            // Return Beam result directly (no Greedy fallback - Beam should be better)
            return beamResult;
        }
        
        // Fallback to greedy if beam search found nothing useful
        OptimizationStrategy fallback = new GreedyOptimizer();
        return fallback.optimize(config, maxDelay);
    }
    
    /**
     * Heuristic function to estimate the value of a state.
     * Considers current resources, remaining time, and potential for growth.
     */
    private double calculateHeuristic(Map<String, Integer> stocks, int currentTime, 
                                     Set<String> optimize, int maxDelay, List<Process> processes,
                                     Map<String, Integer> baseline) {
        double score = 0;

        // Heavy weight on target resources: maximize what we're trying to optimize
        for (var e : stocks.entrySet()) {
            if (optimize.contains(e.getKey()) && !"money".equals(e.getKey())) {
                int base = baseline.getOrDefault(e.getKey(), 0);
                score += (e.getValue() - base) * 5000.0; // much stronger weight
            } else if (!optimize.contains(e.getKey())) {
                score += e.getValue() * 5.0; // small value for intermediates
            }
        }

        // Boost packaged/semi-final goods that lead to target (happy_customer, money, research, etc)
        int packagedCoffee = stocks.getOrDefault("packaged_coffee", 0);
        int boxPastries = stocks.getOrDefault("box_pastries", 0);
        int coffee = stocks.getOrDefault("coffee", 0);
        int croissant = stocks.getOrDefault("croissant", 0);
        int muffin = stocks.getOrDefault("muffin", 0);
        score += packagedCoffee * 2000.0;
        score += boxPastries * 2000.0;
        score += coffee * 500.0;
        score += croissant * 400.0;
        score += muffin * 400.0;

        // Penalize raw material hoarding (beans, flour, etc)
        for (var e : stocks.entrySet()) {
            String name = e.getKey();
            if (name.contains("bean") || name.contains("flour") || name.contains("butter") || 
                name.contains("egg") || name.contains("milk") || name.contains("cup")) {
                int excess = Math.max(0, e.getValue() - 100); // reduce threshold
                score -= excess * 10.0; // STRONG penalty for hoarding
            }
        }

        // Optimistic potential using weighted processes toward targets
        int remainingTime = Math.max(0, maxDelay - currentTime);
        double bestRate = 0;
        for (Process p : processes) {
            if (p.delay() == 0) continue;
            double v = 0;
            // MASSIVE boost for processes creating target resources (sell, happy_customer, money)
            for (var r : p.results().entrySet()) {
                if (optimize.contains(r.getKey()) && !"money".equals(r.getKey())) {
                    v += r.getValue() * 10000.0; // VERY strong for targets like happy_customer
                } else if ("money".equals(r.getKey())) {
                    v += r.getValue() * 5000.0; // also strong for money
                }
            }
            // Boost processes that lead to sales (e.g., package_coffee, package_pastries)
            for (var r : p.results().entrySet()) {
                if (r.getKey().contains("packaged") || r.getKey().contains("box")) {
                    v += r.getValue() * 5000.0; // push toward packaging
                }
            }
            if (v > 0) bestRate = Math.max(bestRate, v / p.delay());
        }
        score += bestRate * remainingTime * 2.0; // double the optimistic gain


        // Time penalty if optimizing time
        if (optimize.contains("time")) {
            score -= currentTime * 10.0;
        }
        return score;
    }
    
    private double calculateScore(Map<String, Integer> stocks, Set<String> optimize, int finalTime,
                                  Map<String, Integer> baseline) {
        double score = 0;
        for (var e : stocks.entrySet()) {
            if (optimize.contains(e.getKey())) {
                int base = baseline.getOrDefault(e.getKey(), 0);
                score += (e.getValue() - base) * 1000.0;
            }
        }
        if (optimize.contains("time")) {
            score -= finalTime * 10.0;
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
