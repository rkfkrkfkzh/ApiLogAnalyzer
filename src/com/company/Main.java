package com.company;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        String filePath = "./src/com/company/input.log";
        analyzeLog(filePath);
    }

    private static void analyzeLog(String filePath) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            PrintStream fileOut = new PrintStream("./src/com/company/output.txt");
            System.setOut(fileOut);

            Map<String, Integer> apiKeyCounts = new HashMap<>();
            Map<String, Integer> serviceIdCounts = new HashMap<>();
            Map<String, Integer> browserCounts = new HashMap<>();
            Set<String> browsers = new HashSet<>(Arrays.asList("IE", "Firefox", "Opera", "Chrome", "Safari"));

            for (String line : lines) {
                if (!line.startsWith("[200]")) continue;
                String[] parts = line.split("\\[|\\]|\\?");
                String url = parts[4];
                String urlSearch = parts[3];
                String browser = parts[6];

                // API Key 추출 및 카운트
                String apiKey = "unknown";
                for (String param : url.split("&")) {
                    if (param.startsWith("apikey=")) {
                        apiKey = param.substring(7);
                        break;
                    }
                }
                apiKeyCounts.merge(apiKey, 1, Integer::sum);

                // Service ID 추출 및 카운트
                String serviceId = urlSearch.substring(urlSearch.indexOf("/search/") + 8);
                serviceIdCounts.merge(serviceId, 1, Integer::sum);

                // 브라우저 카운트
                if (browsers.contains(browser)) {
                    browserCounts.merge(browser, 1, Integer::sum);
                }
            }

            // 최다 호출 APIKEY 출력
            String mostCalledApiKey = Collections.max(apiKeyCounts.entrySet(), Map.Entry.comparingByValue()).getKey();
            System.out.println("최다호출 API KEY\n" + mostCalledApiKey);
            System.out.println("\n상위 3개의 API Service ID와 각각의 요청 수");
            // 상위 3개의 API Service ID와 각각의 요청수 출력
            serviceIdCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(3)
                    .forEach(e -> System.out.println(e.getKey() + " : " + e.getValue()));
            System.out.println("\n웹브라우저별 사용 비율");
            // 웹브라우저 사용 비율 출력
            int totalBrowserUsage = browserCounts.values().stream().mapToInt(Integer::intValue).sum();

            browserCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()) // 내림차순 정렬
                    .forEach(entry -> {
                        String browser = entry.getKey();
                        int count = entry.getValue();
                        double percentage = 100.0 * count / totalBrowserUsage;
                        System.out.printf("%s : %.0f%%\n", browser, percentage);
                    });
            fileOut.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
