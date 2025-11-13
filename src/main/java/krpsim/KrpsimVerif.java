package krpsim;

import krpsim.model.Process;
import krpsim.utils.Parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class KrpsimVerif {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: krpsim_verif <config> <trace>");
            return;
        }

        String configFile = args[0];
        String traceFile = args[1];

        var config = Parser.parse(configFile);
        List<String> trace = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(traceFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) trace.add(line.trim());
            }
        }

        verify(config, trace);
    }

    private static void verify(Parser.Config config, List<String> trace) {
        Map<String, Integer> stocks = new HashMap<>(config.initialStocks());
        Map<String, Process> processMap = new HashMap<>();
        config.processes().forEach(p -> processMap.put(p.name(), p));

        Map<String, Integer> pending = new HashMap<>(); // process -> end time
        int currentTime = 0;

        for (String line : trace) {
            String[] parts = line.split(":", 2);
            int time = Integer.parseInt(parts[0]);
            String procName = parts[1];

            // Прыгаем по времени
            while (currentTime < time) {
                applyCompleted(stocks, pending, currentTime, processMap);
                currentTime++;
            }

            Process p = processMap.get(procName);
            if (p == null) {
                System.out.println("ERROR: Unknown process " + procName + " at cycle " + time);
                printFinal(stocks, time);
                return;
            }

            // Проверка: хватает ли ресурсов?
            for (var need : p.needs().entrySet()) {
                int available = stocks.getOrDefault(need.getKey(), 0);
                if (available < need.getValue()) {
                    System.out.println("ERROR: Not enough " + need.getKey() +
                        " at cycle " + time + " for " + procName);
                    printFinal(stocks, time);
                    return;
                }
            }

            // Запуск
            consume(stocks, p);
            pending.put(procName + "@" + time, time + p.delay());
        }

        // Завершить всё
        while (!pending.isEmpty()) {
            applyCompleted(stocks, pending, currentTime, processMap);
            currentTime++;
        }

        System.out.println("Trace is correct!");
        printFinal(stocks, currentTime - 1);
    }

    private static void applyCompleted(Map<String, Integer> stocks, Map<String, Integer> pending,
                                       int time, Map<String, Process> processMap) {
        pending.entrySet().removeIf(e -> {
            if (e.getValue() <= time) {
                String procName = e.getKey().split("@")[0];
                Process p = processMap.get(procName);
                if (p != null) applyResults(stocks, p);
                return true;
            }
            return false;
        });
    }

    private static void consume(Map<String, Integer> stocks, Process p) {
        p.needs().forEach((k, v) -> stocks.merge(k, -v, Integer::sum));
    }

    private static void applyResults(Map<String, Integer> stocks, Process p) {
        p.results().forEach((k, v) -> stocks.merge(k, v, Integer::sum));
    }

    private static void printFinal(Map<String, Integer> stocks, int time) {
        System.out.println("Final stocks at cycle " + time + ":");
        stocks.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> System.out.println(e.getKey() + " => " + e.getValue()));
    }
}