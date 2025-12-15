package krpsim.visualizer;

import krpsim.model.Event;
import krpsim.model.Process;
import krpsim.optimizer.OptimizationStrategy;
import krpsim.utils.Parser;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Panel displaying resource evolution over time.
 * 
 * Shows line graphs for each resource, tracking how quantities
 * change throughout the simulation.
 */
public class ResourceGraphPanel extends JPanel {
    
    private final OptimizationStrategy.OptimizationResult result;
    private final Parser.Config config;
    private final int maxDelay;
    private final Map<String, Color> resourceColors;
    
    private static class ResourceSnapshot {
        int time;
        Map<String, Integer> stocks;
        
        ResourceSnapshot(int time, Map<String, Integer> stocks) {
            this.time = time;
            this.stocks = new LinkedHashMap<>(stocks);
        }
    }
    
    public ResourceGraphPanel(OptimizationStrategy.OptimizationResult result, 
                             Parser.Config config, int maxDelay) {
        this.result = result;
        this.config = config;
        this.maxDelay = maxDelay;
        this.resourceColors = generateColors();
        setBackground(Color.WHITE);
    }
    
    private Map<String, Color> generateColors() {
        Map<String, Color> colors = new HashMap<>();
        Color[] palette = {
            new Color(231, 76, 60),    // Red
            new Color(52, 152, 219),   // Blue
            new Color(46, 204, 113),   // Green
            new Color(155, 89, 182),   // Purple
            new Color(241, 196, 15),   // Yellow
            new Color(26, 188, 156),   // Turquoise
            new Color(230, 126, 34),   // Orange
        };
        
        Set<String> allResources = new TreeSet<>(config.initialStocks().keySet());
        for (var p : config.processes()) {
            allResources.addAll(p.needs().keySet());
            allResources.addAll(p.results().keySet());
        }
        
        int idx = 0;
        for (String resource : allResources) {
            colors.put(resource, palette[idx % palette.length]);
            idx++;
        }
        
        return colors;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Simulate to get resource evolution
        List<ResourceSnapshot> history = simulateResourceHistory();
        
        if (history.isEmpty()) {
            g2.drawString("No resource data", getWidth() / 2 - 60, getHeight() / 2);
            return;
        }
        
        // Calculate layout
        int margin = 60;
        int graphWidth = getWidth() - margin * 2;
        int legendSpace = Math.max(100, (resourceColors.size() / 4 + 1) * 30);
        int graphHeight = getHeight() - margin * 2 - legendSpace;
        
        int maxTime = history.get(history.size() - 1).time;
        int maxQuantity = history.stream()
            .flatMap(s -> s.stocks.values().stream())
            .max(Integer::compareTo)
            .orElse(100);
        
        maxQuantity = Math.max(10, maxQuantity);
        
        double timeScale = (double) graphWidth / Math.max(1, maxTime);
        double quantityScale = (double) graphHeight / maxQuantity;
        
        // Draw title
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.setColor(Color.BLACK);
        g2.drawString("Resource Evolution Over Time", margin, 30);
        
        // Draw axes
        int originX = margin;
        int originY = margin + 50 + graphHeight;
        
        g2.setColor(Color.BLACK);
        g2.drawLine(originX, originY, originX + graphWidth, originY); // X axis
        g2.drawLine(originX, originY, originX, originY - graphHeight); // Y axis
        
        // Draw X axis labels (time)
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        for (int t = 0; t <= maxTime; t += Math.max(1, maxTime / 10)) {
            int x = originX + (int) (t * timeScale);
            g2.drawLine(x, originY, x, originY + 5);
            g2.drawString(String.valueOf(t), x - 10, originY + 20);
        }
        g2.drawString("Time (cycles)", originX + graphWidth / 2 - 40, originY + 40);
        
        // Draw Y axis labels (quantity)
        for (int q = 0; q <= maxQuantity; q += Math.max(1, maxQuantity / 10)) {
            int y = originY - (int) (q * quantityScale);
            g2.drawLine(originX - 5, y, originX, y);
            g2.drawString(String.valueOf(q), originX - 35, y + 5);
        }
        g2.drawString("Quantity", originX - 50, originY - graphHeight / 2);
        
        // Draw resource lines
        for (String resource : resourceColors.keySet()) {
            Color color = resourceColors.get(resource);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2));
            
            Integer prevX = null;
            Integer prevY = null;
            
            for (ResourceSnapshot snapshot : history) {
                int quantity = snapshot.stocks.getOrDefault(resource, 0);
                int x = originX + (int) (snapshot.time * timeScale);
                int y = originY - (int) (quantity * quantityScale);
                
                if (prevX != null) {
                    g2.drawLine(prevX, prevY, x, y);
                }
                
                // Draw point
                g2.fillOval(x - 3, y - 3, 6, 6);
                
                prevX = x;
                prevY = y;
            }
        }
        
        // Draw legend
        int legendX = originX;
        int legendY = originY + 60;
        int col = 0;
        
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        for (var entry : resourceColors.entrySet()) {
            int x = legendX + (col % 4) * 150;
            int y = legendY + (col / 4) * 25;
            
            g2.setColor(entry.getValue());
            g2.fillRect(x, y - 10, 15, 15);
            g2.setColor(Color.BLACK);
            g2.drawRect(x, y - 10, 15, 15);
            g2.drawString(entry.getKey(), x + 20, y + 2);
            
            col++;
        }
    }
    
    private List<ResourceSnapshot> simulateResourceHistory() {
        List<ResourceSnapshot> history = new ArrayList<>();
        Map<String, Integer> stocks = new LinkedHashMap<>(config.initialStocks());
        PriorityQueue<Event> active = new PriorityQueue<>();
        Map<String, Process> processMap = new HashMap<>();
        
        for (var p : config.processes()) {
            processMap.put(p.name(), p);
        }
        
        // Add initial snapshot
        history.add(new ResourceSnapshot(0, stocks));
        
        int currentTime = 0;
        
        for (String traceLine : result.trace()) {
            String[] parts = traceLine.split(":");
            int startTime = Integer.parseInt(parts[0]);
            String processName = parts[1];
            
            // Apply completions up to start time
            while (!active.isEmpty() && active.peek().time() <= startTime) {
                Event e = active.poll();
                currentTime = e.time();
                Process p = processMap.get(e.processName());
                if (p != null) {
                    p.results().forEach((k, v) -> stocks.merge(k, v, Integer::sum));
                    history.add(new ResourceSnapshot(currentTime, stocks));
                }
            }
            
            // Start new process
            currentTime = startTime;
            Process p = processMap.get(processName);
            if (p != null) {
                p.needs().forEach((k, v) -> stocks.merge(k, -v, Integer::sum));
                history.add(new ResourceSnapshot(currentTime, stocks));
                active.add(new Event(startTime + p.delay(), processName));
            }
        }
        
        // Complete remaining
        while (!active.isEmpty()) {
            Event e = active.poll();
            currentTime = e.time();
            Process p = processMap.get(e.processName());
            if (p != null) {
                p.results().forEach((k, v) -> stocks.merge(k, v, Integer::sum));
                history.add(new ResourceSnapshot(currentTime, stocks));
            }
        }
        
        return history;
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(1200, 700);
    }
}
