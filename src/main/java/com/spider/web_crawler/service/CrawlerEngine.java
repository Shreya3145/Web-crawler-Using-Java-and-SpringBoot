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

        String seedDomain;
        try {
            seedDomain = new java.net.URL(url).getHost();
        } catch (Exception e) {
            return;
        }

        for (int i = 0; i < THREADS; i++) {
            executor.submit(() -> crawlTask(maxPages, seedDomain));
        }
        System.out.println("Starting crawl with seed URL: " + url + ", maxPages: " + maxPages);
        System.out.println("Crawled: " + crawledCount.get());
    }
private void crawlTask(int maxPages,  String seedDomain){
    while (true) {

        if (queue.size() > 10000) return;

        if (crawledCount.get() >= maxPages) {
            return;
        }

        String currentUrl = queue.poll();

        if (currentUrl == null) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException  e) {
                Thread.currentThread().interrupt();
                return;
            }
            continue;
        }

        if (crawledCount.incrementAndGet() > maxPages) {
            return;
        }

        Document doc = pageFetcher.fetchPage(currentUrl);
        if (doc == null) continue;

        System.out.println("Crawled: " + crawledCount.get() + " | URL: " + currentUrl);

        pageExtractor.extractAndSave(doc, currentUrl);

        Elements links = doc.select("a[href]");
        for (Element link : links) {
            String absUrl = link.attr("abs:href");
            try {
                String linkHost = new java.net.URL(absUrl).getHost();

                if (absUrl.isEmpty()
                        || redisService.isVisited(absUrl)
                        || !absUrl.startsWith("http")
                        || !linkHost.endsWith(seedDomain)) {
                    continue;
                }

                queue.add(absUrl);
                redisService.markVisited(absUrl);
            }
            catch (Exception e) {
               continue;
            }
        }
    }
}
    public void shutdown() {
        System.out.println("Shutting down crawler...");

        executor.shutdown();

        try {
            if (!executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)) {
                System.out.println("Forcing shutdown...");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("Crawler stopped.");
    }

    @jakarta.annotation.PreDestroy
    public void onShutdown() {
        shutdown();
    }
}

