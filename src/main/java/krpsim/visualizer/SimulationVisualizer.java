package krpsim.visualizer;

import krpsim.optimizer.OptimizationStrategy;
import krpsim.utils.Parser;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Main window for simulation visualization.
 * 
 * Displays:
 * - Gantt chart showing process execution timeline
 * - Resource graphs showing stock changes over time
 * - Summary statistics
 */
public class SimulationVisualizer extends JFrame {
    
    private final OptimizationStrategy.OptimizationResult result;
    private final Parser.Config config;
    private final int maxDelay;
    
    public SimulationVisualizer(OptimizationStrategy.OptimizationResult result, 
                               Parser.Config config, int maxDelay) {
        this.result = result;
        this.config = config;
        this.maxDelay = maxDelay;
        
        initUI();
    }
    
    private void initUI() {
        setTitle("KRPSim - Process Schedule Visualization");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Add Gantt chart tab
        GanttChartPanel ganttPanel = new GanttChartPanel(result, config);
        tabbedPane.addTab("Process Timeline (Gantt)", ganttPanel);
        
        // Add resource graph tab
        ResourceGraphPanel resourcePanel = new ResourceGraphPanel(result, config, maxDelay);
        tabbedPane.addTab("Resource Evolution", resourcePanel);
        
        // Add summary panel
        JPanel summaryPanel = createSummaryPanel();
        tabbedPane.addTab("Summary", summaryPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Set size and center on screen
        setSize(1400, 850);
        setLocationRelativeTo(null);
    }
    
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════\n");
        sb.append("           SIMULATION SUMMARY\n");
        sb.append("═══════════════════════════════════════════════════════\n\n");
        
        sb.append("Configuration:\n");
        sb.append("  • Processes: ").append(config.processes().size()).append("\n");
        
        Set<String> allStocks = new HashSet<>(config.initialStocks().keySet());
        for (var p : config.processes()) {
            allStocks.addAll(p.needs().keySet());
            allStocks.addAll(p.results().keySet());
        }
        sb.append("  • Resources: ").append(allStocks.size()).append("\n");
        sb.append("  • Optimization targets: ").append(config.optimizeTargets()).append("\n\n");
        
        sb.append("Results:\n");
        sb.append("  • Total process executions: ").append(result.trace().size()).append("\n");
        sb.append("  • Final time: ").append(result.finalTime()).append("\n");
        sb.append("  • Status: ").append(result.finished() ? "Completed" : "Reached time limit").append("\n");
        sb.append("  • Optimization score: ").append(String.format("%.2f", result.score())).append("\n\n");
        
        sb.append("Initial stocks:\n");
        config.initialStocks().forEach((k, v) -> 
            sb.append("  ").append(k).append(": ").append(v).append("\n"));
        
        sb.append("\nFinal stocks:\n");
        result.finalStocks().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> sb.append("  ").append(e.getKey()).append(": ").append(e.getValue()).append("\n"));
        
        sb.append("\nProcess definitions:\n");
        for (var p : config.processes()) {
            sb.append("  • ").append(p.name()).append("\n");
            sb.append("    Needs: ").append(p.needs()).append("\n");
            sb.append("    Produces: ").append(p.results()).append("\n");
            sb.append("    Duration: ").append(p.delay()).append(" cycles\n\n");
        }
        
        textArea.setText(sb.toString());
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    public static void show(OptimizationStrategy.OptimizationResult result, 
                           Parser.Config config, int maxDelay) {
        SwingUtilities.invokeLater(() -> {
            SimulationVisualizer visualizer = new SimulationVisualizer(result, config, maxDelay);
            visualizer.setVisible(true);
        });
    }
}
