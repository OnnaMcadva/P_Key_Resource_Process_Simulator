package krpsim.visualizer;

import krpsim.model.Process;
import krpsim.optimizer.OptimizationStrategy;
import krpsim.utils.Parser;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel displaying a Gantt chart of process execution.
 * 
 * Shows when each process starts and completes, with color coding
 * for different processes.
 */
public class GanttChartPanel extends JPanel {
    
    private final OptimizationStrategy.OptimizationResult result;
    private final Parser.Config config;
    private final Map<String, Color> processColors;
    
    private static class ProcessExecution {
        String processName;
        int startTime;
        int endTime;
        
        ProcessExecution(String name, int start, int end) {
            this.processName = name;
            this.startTime = start;
            this.endTime = end;
        }
    }
    
    public GanttChartPanel(OptimizationStrategy.OptimizationResult result, Parser.Config config) {
        this.result = result;
        this.config = config;
        this.processColors = generateColors();
        setBackground(Color.WHITE);
    }
    
    private Map<String, Color> generateColors() {
        Map<String, Color> colors = new HashMap<>();
        Color[] palette = {
            new Color(52, 152, 219),   // Blue
            new Color(46, 204, 113),   // Green
            new Color(155, 89, 182),   // Purple
            new Color(241, 196, 15),   // Yellow
            new Color(231, 76, 60),    // Red
            new Color(26, 188, 156),   // Turquoise
            new Color(230, 126, 34),   // Orange
            new Color(149, 165, 166),  // Gray
        };
        
        int idx = 0;
        for (var p : config.processes()) {
            colors.put(p.name(), palette[idx % palette.length]);
            idx++;
        }
        
        return colors;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (result.trace().isEmpty()) {
            g2.drawString("No processes executed", getWidth() / 2 - 60, getHeight() / 2);
            return;
        }
        
        // Parse trace to get execution intervals
        List<ProcessExecution> executions = parseExecutions();
        
        if (executions.isEmpty()) return;
        
        // Calculate layout
        int margin = 50;
        int labelWidth = 200;
        int chartWidth = getWidth() - margin * 2 - labelWidth;
        int rowHeight = 40;
        
        int maxTime = executions.stream().mapToInt(e -> e.endTime).max().orElse(100);
        double timeScale = (double) chartWidth / maxTime;
        
        // Draw title
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.setColor(Color.BLACK);
        g2.drawString("Process Execution Timeline (Gantt Chart)", margin, 30);
        
        // Draw time axis
        int axisY = margin + 40;
        g2.setColor(Color.GRAY);
        g2.drawLine(margin + labelWidth, axisY, margin + labelWidth + chartWidth, axisY);
        
        // Draw time labels
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        for (int t = 0; t <= maxTime; t += Math.max(1, maxTime / 10)) {
            int x = margin + labelWidth + (int) (t * timeScale);
            g2.drawLine(x, axisY - 5, x, axisY + 5);
            g2.drawString(String.valueOf(t), x - 10, axisY - 10);
        }
        
        // Group executions by process
        Map<String, java.util.List<ProcessExecution>> byProcess = new HashMap<>();
        for (var exec : executions) {
            byProcess.computeIfAbsent(exec.processName, k -> new ArrayList<>()).add(exec);
        }
        
        // Draw process rows
        int row = 0;
        for (var p : config.processes()) {
            int y = axisY + 20 + row * rowHeight;
            
            // Draw process name
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            g2.drawString(p.name(), margin, y + 20);
            
            // Draw executions for this process
            List<ProcessExecution> procExecs = byProcess.getOrDefault(p.name(), List.of());
            Color color = processColors.get(p.name());
            
            for (var exec : procExecs) {
                int x1 = margin + labelWidth + (int) (exec.startTime * timeScale);
                int x2 = margin + labelWidth + (int) (exec.endTime * timeScale);
                int width = Math.max(3, x2 - x1);
                
                // Draw bar
                g2.setColor(color);
                g2.fillRect(x1, y, width, 30);
                
                // Draw border
                g2.setColor(color.darker());
                g2.drawRect(x1, y, width, 30);
                
                // Draw time labels on bar if wide enough
                if (width > 40) {
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Arial", Font.BOLD, 9));
                    String label = exec.startTime + "-" + exec.endTime;
                    g2.drawString(label, x1 + 5, y + 20);
                }
            }
            
            row++;
        }
        
        // Draw legend
        int legendY = axisY + 20 + row * rowHeight + 40;
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.drawString("Total executions: " + executions.size(), margin, legendY);
        g2.drawString("Time range: 0 - " + maxTime, margin + 250, legendY);
    }
    
    private List<ProcessExecution> parseExecutions() {
        List<ProcessExecution> executions = new ArrayList<>();
        Map<String, Process> processMap = new HashMap<>();
        for (var p : config.processes()) {
            processMap.put(p.name(), p);
        }
        
        for (String traceLine : result.trace()) {
            String[] parts = traceLine.split(":");
            if (parts.length != 2) continue;
            
            int startTime = Integer.parseInt(parts[0]);
            String processName = parts[1];
            
            Process p = processMap.get(processName);
            if (p != null) {
                int endTime = startTime + p.delay();
                executions.add(new ProcessExecution(processName, startTime, endTime));
            }
        }
        
        return executions;
    }
    
    @Override
    public Dimension getPreferredSize() {
        int height = Math.max(600, config.processes().size() * 50 + 200);
        return new Dimension(1200, height);
    }
}
