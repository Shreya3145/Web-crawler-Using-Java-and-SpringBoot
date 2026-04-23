package com.spider.web_crawler.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
public class PageFetcher {

    public Document fetchPage(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(5000)
                    .get();
        } catch (Exception e) {
            System.err.println("Failed to fetch: " + url + " - " + e.getMessage());
            return null;
        }
    }
}
