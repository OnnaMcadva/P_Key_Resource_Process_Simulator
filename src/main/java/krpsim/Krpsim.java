package krpsim;

import krpsim.optimizer.OptimizationStrategy;
import krpsim.optimizer.GreedyOptimizer;
import krpsim.optimizer.BeamSearchOptimizer;
import krpsim.optimizer.BranchAndBoundOptimizer;
import krpsim.utils.Parser;
import krpsim.visualizer.SimulationVisualizer;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Main simulation class for the Key Resource Process Simulator (krpsim).
 * 
 * This program simulates a process scheduling system with resource constraints,
 * optimizing for either time or specific resource production targets.
 *
 * Usage: java -cp <classpath> krpsim.Krpsim <configFile> <maxDelay> [--optimize-level N]
 *   where N = 0 (Greedy - default), 1 (Beam Search), 2 (Branch & Bound A*)
 */
public class Krpsim {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: krpsim <configFile> <maxDelay> [--optimize-level N] [--visualize]");
            System.out.println("  Optimization levels:");
            System.out.println("    0 = Greedy (fast, default)");
            System.out.println("    1 = Beam Search (good quality)");
            System.out.println("    2 = Branch & Bound A* (best quality, slower)");
            System.out.println("  --visualize: Show GUI with Gantt chart and resource graphs");
            return;
        }

        String file = args[0];
        int maxDelay;
        try { maxDelay = Integer.parseInt(args[1]); }
        catch (NumberFormatException ex) {
            System.out.println("Second argument must be integer maxDelay");
            return;
        }

        // Parse optimization level
        int optimizeLevel = 0; // default: greedy
        boolean visualize = false;
        
        for (int i = 2; i < args.length; i++) {
            if (args[i].equals("--optimize-level") && i + 1 < args.length) {
                try {
                    optimizeLevel = Integer.parseInt(args[i + 1]);
                    if (optimizeLevel < 0 || optimizeLevel > 2) {
                        System.err.println("Warning: optimize-level must be 0, 1, or 2. Using default (0).");
                        optimizeLevel = 0;
                    }
                } catch (NumberFormatException ex) {
                    System.err.println("Warning: Invalid optimize-level. Using default (0).");
                }
                i++; // skip next arg
            } else if (args[i].equals("--visualize")) {
                visualize = true;
            }
        }

        // Select optimization strategy
        // Strategy selection: 0=Greedy (mandatory), 1=Beam Search, 2=Branch & Bound
        OptimizationStrategy strategy = switch (optimizeLevel) {
            case 1 -> new BeamSearchOptimizer(16); // increased beam width for better exploration
            case 2 -> new BranchAndBoundOptimizer(5000); // 5s for speed; no fallback
            default -> new GreedyOptimizer();
        };

        var config = Parser.parse(file);
        var result = strategy.optimize(config, maxDelay);

        // Count all unique stocks (initial + those produced/consumed by processes)
        Set<String> allStocks = new HashSet<>(config.initialStocks().keySet());
        for (var p : config.processes()) {
            allStocks.addAll(p.needs().keySet());
            allStocks.addAll(p.results().keySet());
        }

        // Count all optimize targets exactly as provided (including time)
        int optimizeCount = config.optimizeTargets().size();
        System.err.println("Nice file! " + config.processes().size() + " processes, " +
            allStocks.size() + " stocks, " + optimizeCount + " to optimize");
        System.err.println("Evaluating .................. done.");
        System.err.println("Main walk");
        // Limit trace output to avoid flooding: show first 10 lines, then summarize the rest
        final int MAX_TRACE_LINES = 10; 
        List<String> trace = result.trace();
        int linesToShow = Math.min(trace.size(), MAX_TRACE_LINES);
        for (int i = 0; i < linesToShow; i++) {
            System.out.println(trace.get(i));
        }
        if (trace.size() > MAX_TRACE_LINES) {
            System.err.println("... truncated " + (trace.size() - MAX_TRACE_LINES) + " more entries ...");
        }

        if (result.finished()) {
            System.err.println("no more process doable at time " + result.finalTime());
        } else {
            System.err.println("Reached delay " + maxDelay);
        }

        
        // Show visualization if requested
        if (visualize) {
            SimulationVisualizer.show(result, config, maxDelay);
        }
        System.err.println("Stock :");
        // Print stocks in alphabetical order like example
        result.finalStocks().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> System.err.println(e.getKey() + "=> " + e.getValue()));
    }
}