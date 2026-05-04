package com.spider.web_crawler.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
public class PageFetcher {
    private static final int MAX_RETRIES = 3;

    public Document fetchPage(String url) {
        int attempts = 0;

        while (attempts < MAX_RETRIES) {
            try {
                return Jsoup.connect(url)
                        .userAgent("Mozilla/5.0")
                        .timeout(5000)
                        .get();
            } catch (Exception e) {
                attempts++;
                System.err.println("Failed to fetch: " + url + " - " + e.getMessage());
                try {
                    long delay = (long) Math.pow(2, attempts) * 500;
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        return null;
    }
}
