package krpsim;

import krpsim.model.Event;
import krpsim.model.Process;
import krpsim.utils.Parser;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.PriorityQueue;
import java.util.Comparator;

public class Krpsim {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: krpsim <file> <delay>");
            return;
        }

        String file = args[0];
        int maxDelay = Integer.parseInt(args[1]);

        var config = Parser.parse(file);
        var result = simulate(config, maxDelay);

        // Вывод
        System.out.println("Nice file! " + config.processes().size() + " processes, " +
            config.initialStocks().size() + " stocks, " + config.optimizeTargets().size() + " to optimize");

        System.out.println("Evaluating .................. done.");
        System.out.println("Main walk");
        for (String line : result.trace) {
            System.out.println(line);
        }

        if (result.finished) {
            System.out.println("no more process doable at time " + result.finalTime);
        } else {
            System.out.println("reached delay " + maxDelay);
        }

        System.out.println("Stock :");
        config.initialStocks().forEach((k, v) -> {
            int current = result.finalStocks.getOrDefault(k, 0);
            System.out.println(k + " => " + current);
        });
    }

    public record SimResult(
        List<String> trace,
        Map<String, Integer> finalStocks,
        int finalTime,
        boolean finished
    ) {}

    public static SimResult simulate(Parser.Config config, int maxDelay) {
        Map<String, Integer> stocks = new HashMap<>(config.initialStocks());
        List<Process> processes = config.processes();
        Set<String> targets = config.optimizeTargets();

        PriorityQueue<Event> active = new PriorityQueue<>();
        List<String> trace = new ArrayList<>();
        int currentTime = 0;

        while (currentTime <= maxDelay) {
            // Завершаем готовые процессы
            while (!active.isEmpty() && active.peek().time() <= currentTime) {
                Event e = active.poll();
                Process p = findProcess(processes, e.processName());
                applyResults(stocks, p);
                trace.add(currentTime + ":" + e.processName());
            }

            // Запускаем новые
            List<Process> candidates = getRunnable(stocks, processes);
            if (candidates.isEmpty()) {
                if (active.isEmpty()) break;
                currentTime = active.peek().time();
                continue;
            }

            Process best = chooseBest(candidates, stocks, targets, maxDelay - currentTime);
            if (best == null) {
                if (active.isEmpty()) break;
                currentTime = active.peek().time();
                continue;
            }

            consumeResources(stocks, best);
            active.add(new Event(currentTime + best.delay(), best.name()));
            currentTime = active.isEmpty() ? currentTime + 1 : Math.min(currentTime + 1, active.peek().time());
        }

        // Завершить оставшиеся
        while (!active.isEmpty()) {
            Event e = active.poll();
            Process p = findProcess(processes, e.processName());
            applyResults(stocks, p);
            trace.add(e.time() + ":" + e.processName());
        }

        int finalTime = trace.isEmpty() ? 0 : Integer.parseInt(trace.get(trace.size() - 1).split(":")[0]);
        boolean finished = active.isEmpty() && getRunnable(stocks, processes).isEmpty();

        return new SimResult(trace, new HashMap<>(stocks), finalTime, finished);
    }

    private static Process findProcess(List<Process> processes, String name) {
        return processes.stream().filter(p -> p.name().equals(name)).findFirst().orElse(null);
    }

    private static List<Process> getRunnable(Map<String, Integer> stocks, List<Process> processes) {
        return processes.stream()
            .filter(p -> p.needs().entrySet().stream()
                .allMatch(e -> stocks.getOrDefault(e.getKey(), 0) >= e.getValue()))
            .toList();
    }

    private static void consumeResources(Map<String, Integer> stocks, Process p) {
        p.needs().forEach((k, v) -> stocks.merge(k, -v, Integer::sum));
    }

    private static void applyResults(Map<String, Integer> stocks, Process p) {
        p.results().forEach((k, v) -> stocks.merge(k, v, Integer::sum));
    }

    // Простая жадная стратегия: максимизировать целевые ресурсы
    private static Process chooseBest(List<Process> candidates, Map<String, Integer> stocks,
                                      Set<String> targets, int remaining) {
        return candidates.stream()
            .max(Comparator.comparingInt(p -> {
                int score = 0;
                for (var e : p.results().entrySet()) {
                    if (targets.contains(e.getKey())) {
                        score += e.getValue() * 1000; // приоритет цели
                    } else {
                        score += e.getValue();
                    }
                }
                score = score * 1000 / Math.max(1, p.delay());
                return score;
            }))
            .orElse(null);
    }
}