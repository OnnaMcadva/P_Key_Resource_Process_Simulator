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
                    double finalScore = calculateScore(state.stocks, optimize, state.currentTime);
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
                        double finalScore = calculateScore(state.stocks, optimize, state.currentTime);
                        if (finalScore > bestFinalScore) {
                            bestFinalScore = finalScore;
                            bestFinalState = state;
                        }
                    } else {
                        // Advance to next completion
                        SearchState advanced = state.copy();
                        advanced.currentTime = advanced.activeProcesses.peek().time();
                        advanced.heuristicScore = calculateHeuristic(advanced.stocks, advanced.currentTime, 
                                                                     optimize, maxDelay);
                        nextBeam.add(advanced);
                    }
                } else {
                    // Try starting each candidate process
                    for (Process p : candidates) {
                        SearchState newState = state.copy();
                        consumeResources(newState.stocks, p);
                        newState.trace.add(newState.currentTime + ":" + p.name());
                        newState.activeProcesses.add(new Event(newState.currentTime + p.delay(), p.name()));
                        newState.heuristicScore = calculateHeuristic(newState.stocks, newState.currentTime, 
                                                                     optimize, maxDelay);
                        nextBeam.add(newState);
                    }
                    
                    // Also consider "wait" action - advance time without starting anything
                    if (!state.activeProcesses.isEmpty()) {
                        SearchState waitState = state.copy();
                        waitState.currentTime = Math.min(waitState.currentTime + 1, 
                                                        waitState.activeProcesses.peek().time());
                        waitState.heuristicScore = calculateHeuristic(waitState.stocks, waitState.currentTime, 
                                                                      optimize, maxDelay);
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
            double score = calculateScore(bestFinalState.stocks, optimize, finalTime);
            
            return new OptimizationResult(
                List.copyOf(bestFinalState.trace),
                new LinkedHashMap<>(bestFinalState.stocks),
                finalTime,
                finished,
                score
            );
        }
        
        // Fallback to empty result
        return new OptimizationResult(List.of(), new LinkedHashMap<>(), 0, true, 0.0);
    }
    
    /**
     * Heuristic function to estimate the value of a state.
     * Considers current resources, remaining time, and potential for growth.
     */
    private double calculateHeuristic(Map<String, Integer> stocks, int currentTime, 
                                     Set<String> optimize, int maxDelay) {
        double score = 0;
        
        // Value of target resources
        for (var e : stocks.entrySet()) {
            if (optimize.contains(e.getKey())) {
                score += e.getValue() * 1000.0;
            } else {
                score += e.getValue() * 10.0; // intermediate resources have some value
            }
        }
        
        // Time penalty
        if (optimize.contains("time")) {
            score -= currentTime * 10.0;
        }
        
        // Bonus for having more time remaining (potential for more production)
        int remainingTime = maxDelay - currentTime;
        score += remainingTime * 0.1;
        
        return score;
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
