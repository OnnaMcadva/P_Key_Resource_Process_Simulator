package krpsim.optimizer;

import krpsim.utils.Parser;
import java.util.List;
import java.util.Map;

/**
 * Strategy interface for different optimization algorithms.
 * 
 * Each strategy attempts to find an optimal or near-optimal process schedule
 * that maximizes the optimization targets within the given time constraints.
 */
public interface OptimizationStrategy {
    
    /**
     * Result of an optimization run.
     * 
     * @param trace list of process executions in "time:processName" format
     * @param finalStocks final resource quantities after simulation
     * @param finalTime last event time in the simulation
     * @param finished whether all possible processes were completed
     * @param score optimization score (higher is better)
     */
    record OptimizationResult(
        List<String> trace,
        Map<String, Integer> finalStocks,
        int finalTime,
        boolean finished,
        double score
    ) {}
    
    /**
     * Execute the optimization strategy.
     * 
     * @param config parsed configuration containing stocks, processes, and optimization targets
     * @param maxDelay maximum simulation time allowed
     * @return optimization result with trace, final stocks, and score
     */
    OptimizationResult optimize(Parser.Config config, int maxDelay);
    
    /**
     * Get the name of this optimization strategy.
     * 
     * @return strategy name for display purposes
     */
    String getName();
}
