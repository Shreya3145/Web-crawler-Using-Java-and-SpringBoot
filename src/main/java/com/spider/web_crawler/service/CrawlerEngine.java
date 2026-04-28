package com.spider.web_crawler.service;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CrawlerEngine {
    // private final Queue<String> queue = new LinkedList<>();
    // private final Set<String> visited = new HashSet<>();
    private final Queue<String> queue = new ConcurrentLinkedQueue<>();
    private final PageFetcher pageFetcher;
    private final PageExtractor pageExtractor;
    private final RedisService redisService;
    private final AtomicInteger crawledCount = new AtomicInteger(0); //crawl counter

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public CrawlerEngine(PageFetcher pageFetcher, PageExtractor pageExtractor, RedisService redisService) {
        this.pageFetcher = pageFetcher;
        this.pageExtractor = pageExtractor;
        this.redisService = redisService;
    }

    public void beginCrawl(String url, int maxPages) {
        queue.add(url);
        redisService.markVisited(url);
        // visited.add(url);
        int THREADS = 10;

        for (int i = 0; i < THREADS; i++) {
            executor.submit(() -> crawlTask(maxPages));
        }
        System.out.println("Starting crawl with seed URL: " + url + ", maxPages: " + maxPages);
        System.out.println("Crawled: " + crawledCount.get());
    }
//        while (!queue.isEmpty() && crawledCount < maxPages) { //visited.size() < maxPages
//            String currentUrl = queue.poll();
//            crawledCount++;
//
//            Document doc = pageFetcher.fetchPage(currentUrl);
//            if (doc == null) continue;
//
//            pageExtractor.extractAndSave(doc, currentUrl);
//
//            Elements links = doc.select("a[href]");
//            for (Element link : links) {
//                String absUrl = link.attr("abs:href");
//                if (absUrl.isEmpty() ||  redisService.isVisited(absUrl) || !absUrl.startsWith("http")) {
//                    continue;
//                }
//                queue.add(absUrl);
//                redisService.markVisited(absUrl);
//                // visited.add(absUrl);
//            }
private void crawlTask(int maxPages) {
    while (true) {

        if (crawledCount.get() >= maxPages) {
            return;
        }

        String currentUrl = queue.poll();

        if (currentUrl == null) {
            try { Thread.sleep(100); } catch (Exception e) {}
            continue;
        }

        if (crawledCount.incrementAndGet() > maxPages) {
            return;
        }

        Document doc = pageFetcher.fetchPage(currentUrl);
        if (doc == null) continue;

        pageExtractor.extractAndSave(doc, currentUrl);

        Elements links = doc.select("a[href]");
        for (Element link : links) {
            String absUrl = link.attr("abs:href");

            if (absUrl.isEmpty() || redisService.isVisited(absUrl) || !absUrl.startsWith("http")) {
                continue;
            }

            queue.add(absUrl);
            redisService.markVisited(absUrl);
        }
    }
}
}

