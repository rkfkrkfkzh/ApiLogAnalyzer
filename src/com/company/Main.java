package com.company;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        String filePath = "/Users/imhyojin/Downloads/ApiLogAnalyzer/src/com/company/input.log";
        analyzeLog(filePath);
    }

    private static void analyzeLog(String filePath) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            Map<String, Integer> apiKeyCounts = new HashMap<>();
            Map<String, Integer> serviceIdCounts = new HashMap<>();
            Map<String, Integer> browserCounts = new HashMap<>();
            Set<String> browsers = new HashSet<>(Arrays.asList("IE", "Firefox", "Opera", "Chrome", "Safari"));

            lines.stream()
                    .filter(line -> line.startsWith("[200]"))
                    .forEach(line -> {
                        String[] parts = line.split("\\[|\\]|\\?");
                        String url = parts[4];
                        String search = parts[3];
                        String browser = parts[6];

                        // API Key 추출 및 카운트. 값이 없는 경우 예외를 피하기 위해 orElse를 사용
                        String apiKey = Arrays.stream(url.split("&"))
                                .filter(param -> param.startsWith("apikey="))
                                .findFirst().orElse("apikey=unknown").substring(7);
                        apiKeyCounts.put(apiKey, apiKeyCounts.getOrDefault(apiKey, 0) + 1);

                        // Service ID 추출 및 카운트
                        String serviceId = search.substring(search.indexOf("/search/") + 8);
                        serviceIdCounts.put(serviceId, serviceIdCounts.getOrDefault(serviceId, 0) + 1);

                        // 브라우저 카운트
                        if (browsers.contains(browser)) {
                            browserCounts.put(browser, browserCounts.getOrDefault(browser, 0) + 1);
                        }
                    });

            // 최다 호출 APIKEY 출력
            String mostCalledApiKey = Collections.max(apiKeyCounts.entrySet(), Map.Entry.comparingByValue()).getKey();
            System.out.println("최다호출 API KEY");
            System.out.println(mostCalledApiKey);
            System.out.println();
            System.out.println("상위 3개의 API Service ID와 각각의 요청 수");
            // 상위 3개의 API Service ID와 각각의 요청수 출력
            serviceIdCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(3)
                    .forEach(e -> System.out.println(e.getKey() + " : " + e.getValue()));
            System.out.println();
            System.out.println("웹브라우저별 사용 비율");
            // 웹브라우저 사용 비율 출력
            int totalBrowserUsage = browserCounts.values().stream().mapToInt(Integer::intValue).sum();
            browsers.forEach(browser -> {
                double percentage = 100.0 * browserCounts.getOrDefault(browser, 0) / totalBrowserUsage;
                System.out.printf("%s : %.0f%%\n", browser, percentage);
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
