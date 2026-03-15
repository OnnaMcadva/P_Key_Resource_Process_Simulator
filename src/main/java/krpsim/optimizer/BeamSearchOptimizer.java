package krpsim.optimizer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import krpsim.model.Event;
import krpsim.model.Process;
import krpsim.utils.Parser;

/**
 * Beam-search based optimization strategy.
 *
 * <p>The optimizer explores multiple promising simulation states in parallel,
 * keeping only the top {@code beamWidth} states (by heuristic score) at each
 * expansion step.
 */
public class BeamSearchOptimizer implements OptimizationStrategy {

    private static final int DEFAULT_BEAM_WIDTH = 8;
    private final int beamWidth;

    /**
     * Creates a beam-search optimizer with the default beam width.
     */
    public BeamSearchOptimizer() {
        this(DEFAULT_BEAM_WIDTH);
    }

    /**
     * Creates a beam-search optimizer.
     *
     * @param beamWidth maximum number of states kept at each search layer
     */
    public BeamSearchOptimizer(int beamWidth) {
        this.beamWidth = beamWidth;
    }

    /**
     * @return human-readable strategy name including configured beam width
     */
    @Override
    public String getName() {
        return "Beam Search (width=" + beamWidth + ")";
    }

    /**
     * Search node used by the beam-search frontier.
     */
    private static class SearchState implements Comparable<SearchState> {

        Map<String, Integer> stocks;
        PriorityQueue<Event> activeProcesses;
        List<String> trace;
        int currentTime;
        double heuristicScore;

        /**
         * Creates a deep-copy-ready search state snapshot.
         */
        SearchState(Map<String, Integer> stocks,
                    PriorityQueue<Event> active,
                    List<String> trace,
                    int time,
                    double score) {

            this.stocks = new LinkedHashMap<>(stocks);
            this.activeProcesses = new PriorityQueue<>(active);
            this.trace = new ArrayList<>(trace);
            this.currentTime = time;
            this.heuristicScore = score;
        }

        /**
         * @return independent copy of the current state for branching
         */
        SearchState copy() {
            return new SearchState(stocks, activeProcesses, trace, currentTime, heuristicScore);
        }

        /**
         * Higher heuristic score means higher priority in the queue.
         */
        @Override
        public int compareTo(SearchState other) {
            return Double.compare(other.heuristicScore, this.heuristicScore);
        }
    }

    /**
     * Runs beam search over possible process schedules and returns the best found plan.
     *
     * @param config parsed simulation configuration
     * @param maxDelay upper bound on simulated time for optimization
     * @return optimization result containing trace, final stocks and score
     */
    @Override
    public OptimizationResult optimize(Parser.Config config, int maxDelay) {

        List<Process> processes = config.processes();
        Set<String> optimize = config.optimizeTargets();

        Map<String, Integer> baseline = new HashMap<>();
        for (String k : optimize) baseline.put(k, 0);

        Set<Process> relevantProcesses = buildRelevantProcessGraph(processes, optimize);

        PriorityQueue<SearchState> beam = new PriorityQueue<>();

        SearchState initialState = new SearchState(
                new LinkedHashMap<>(config.initialStocks()),
                new PriorityQueue<>(),
                new ArrayList<>(),
                0,
                0
        );

        beam.add(initialState);

        SearchState bestFinalState = null;
        double bestFinalScore = Double.NEGATIVE_INFINITY;

        // Expand frontier layer by layer, always keeping only top-N states.
        while (!beam.isEmpty()) {

            PriorityQueue<SearchState> nextBeam = new PriorityQueue<>();

            for (SearchState state : beam) {

                while (!state.activeProcesses.isEmpty()
                        && state.activeProcesses.peek().time() <= state.currentTime) {

                    Event ev = state.activeProcesses.poll();
                    Process p = findProcess(processes, ev.processName());

                    if (p != null) applyResults(state.stocks, p);
                }

                if (state.currentTime > maxDelay) {

                    double finalScore =
                            calculateScore(state.stocks, optimize, state.currentTime, baseline);

                    if (finalScore > bestFinalScore) {
                        bestFinalScore = finalScore;
                        bestFinalState = state;
                    }

                    continue;
                }

                List<Process> candidates = getRunnable(state.stocks, processes);

                // Restrict candidates to the backward-reachable subgraph for target resources.
                if (!optimize.contains("time") || optimize.size() > 1) {
                    candidates = candidates.stream()
                            .filter(relevantProcesses::contains)
                            .toList();
                }

                boolean expanded = false;

                for (Process p : candidates) {

                    if (!isRunnable(state.stocks, p)) continue;

                    SearchState child = state.copy();

                    consumeResources(child.stocks, p);

                    child.trace.add(child.currentTime + ":" + p.name());

                    child.activeProcesses.add(
                            new Event(child.currentTime + p.delay(), p.name())
                    );

                    if (!child.activeProcesses.isEmpty()) {
                        child.currentTime = child.activeProcesses.peek().time();
                    }

                    child.heuristicScore =
                            calculateHeuristic(child.stocks,
                                    child.currentTime,
                                    optimize,
                                    maxDelay,
                                    processes,
                                    baseline);

                    nextBeam.add(child);

                    expanded = true;
                }

                if (!expanded && !state.activeProcesses.isEmpty()) {

                    SearchState waitState = state.copy();

                    waitState.currentTime = state.activeProcesses.peek().time();

                    waitState.heuristicScore =
                            calculateHeuristic(waitState.stocks,
                                    waitState.currentTime,
                                    optimize,
                                    maxDelay,
                                    processes,
                                    baseline);

                    nextBeam.add(waitState);
                }

                // Dead-end state with no active/runnable process: evaluate as terminal.
                if (!expanded && state.activeProcesses.isEmpty()) {

                    double finalScore =
                            calculateScore(state.stocks,
                                    optimize,
                                    state.currentTime,
                                    baseline);

                    if (finalScore > bestFinalScore) {
                        bestFinalScore = finalScore;
                        bestFinalState = state;
                    }
                }
            }

            beam.clear();

            int count = 0;

            // Keep only the best states according to the heuristic.
            while (!nextBeam.isEmpty() && count < beamWidth) {

                beam.add(nextBeam.poll());

                count++;
            }

            if (beam.isEmpty()) break;
        }

        if (bestFinalState != null) {

            while (!bestFinalState.activeProcesses.isEmpty()) {

                Event ev = bestFinalState.activeProcesses.poll();

                Process p = findProcess(processes, ev.processName());

                if (p != null) applyResults(bestFinalState.stocks, p);
            }

            int finalTime = bestFinalState.currentTime;

            boolean finished =
                    bestFinalState.activeProcesses.isEmpty()
                            && getRunnable(bestFinalState.stocks, processes).isEmpty();

            double score =
                    calculateScore(bestFinalState.stocks,
                            optimize,
                            finalTime,
                            baseline);

            return new OptimizationResult(
                    List.copyOf(bestFinalState.trace),
                    new LinkedHashMap<>(bestFinalState.stocks),
                    finalTime,
                    finished,
                    score
            );
        }

        OptimizationStrategy fallback = new GreedyOptimizer();
        return fallback.optimize(config, maxDelay);
    }

    /**
     * Estimates future potential of a state for beam ordering.
     */
    private double calculateHeuristic(
            Map<String, Integer> stocks,
            int currentTime,
            Set<String> optimize,
            int maxDelay,
            List<Process> processes,
            Map<String, Integer> baseline) {

        double score = 0;

        for (var e : stocks.entrySet()) {

            if (optimize.contains(e.getKey())) {

                int base = baseline.getOrDefault(e.getKey(), 0);

                score += (e.getValue() - base) * 5000.0;

            } else {

                score += e.getValue() * 2.0;
            }
        }

        int remainingTime = Math.max(0, maxDelay - currentTime);

        double bestRate = 0;

        for (Process p : processes) {

            if (p.delay() <= 0) continue;

            double value = 0;

            for (var r : p.results().entrySet()) {

                if (optimize.contains(r.getKey())) {

                    value += r.getValue() * 1000.0;

                } else {

                    value += r.getValue() * 10.0;
                }
            }

            bestRate = Math.max(bestRate, value / p.delay());
        }

        score += bestRate * remainingTime;

        if (optimize.contains("time")) {

            score -= currentTime * 10.0;
        }

        return score;
    }

    /**
     * Computes final objective score for a terminal state.
     */
    private double calculateScore(Map<String, Integer> stocks,
                                  Set<String> optimize,
                                  int finalTime,
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

    /**
     * Finds process by name.
     */
    private Process findProcess(List<Process> processes, String name) {

        return processes.stream()
                .filter(p -> p.name().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * @return all processes runnable with current stock levels
     */
    private List<Process> getRunnable(Map<String, Integer> stocks,
                                      List<Process> processes) {

        return processes.stream()
                .filter(p -> isRunnable(stocks, p))
                .toList();
    }

    /**
     * Checks if all required inputs for a process are currently available.
     */
    private boolean isRunnable(Map<String, Integer> stocks, Process p) {

        for (var need : p.needs().entrySet()) {

            if (stocks.getOrDefault(need.getKey(), 0) < need.getValue()) {

                return false;
            }
        }

        return true;
    }

    /**
     * Subtracts process inputs from stocks when the process starts.
     */
    private void consumeResources(Map<String, Integer> stocks, Process p) {

        p.needs().forEach((k, v) -> stocks.merge(k, -v, Integer::sum));
    }

    /**
     * Adds process outputs to stocks when the process completes.
     */
    private void applyResults(Map<String, Integer> stocks, Process p) {

        p.results().forEach((k, v) -> stocks.merge(k, v, Integer::sum));
    }

    /**
     * Builds a backward-reachable process set that can contribute to optimize targets.
     */
    private Set<Process> buildRelevantProcessGraph(List<Process> processes,
                                                   Set<String> optimize) {

        Set<Process> relevant = new HashSet<>();

        Set<String> neededResources = new HashSet<>();

        if (optimize.contains("time") && optimize.size() == 1) {

            return new HashSet<>(processes);
        }

        for (String target : optimize) {

            if (!target.equals("time")) {

                neededResources.add(target);
            }
        }

        boolean changed = true;

        while (changed) {

            changed = false;

            for (Process p : processes) {

                if (relevant.contains(p)) continue;

                boolean producesNeeded =
                        p.results().keySet().stream()
                                .anyMatch(neededResources::contains);

                if (producesNeeded) {

                    relevant.add(p);

                    neededResources.addAll(p.needs().keySet());

                    changed = true;
                }
            }
        }

        return relevant;
    }
}
