package com.spider.web_crawler.service;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

    private final Map<String, Long> domainLastAccess = new ConcurrentHashMap<>();
    private static final long DELAY_MS = 500;

    private static final Logger log = LoggerFactory.getLogger(CrawlerEngine.class);

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
        log.info("Starting crawl with seed URL: {}, maxPages: {}", url, maxPages);
        log.info("Crawled: {}", crawledCount.get());
    }
private void crawlTask(int maxPages,  String seedDomain){
    while (true) {

        if (queue.size() > 10000) {
            log.warn("Queue size exceeded limit, stopping thread");
            return;
        }

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

        applyRateLimit(currentUrl);
        Document doc = pageFetcher.fetchPage(currentUrl);
        if (doc == null){
            log.warn("Failed to fetch or parse URL: {}", currentUrl);
            continue;
        }

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
                log.warn("Skipping invalid URL: {}", absUrl, e);
            }
        }
    }
}
    public void shutdown() {
        log.info("Shutting down crawler...");

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
    private void applyRateLimit(String url) {
        try {
            String domain = new java.net.URL(url).getHost();
            long now = System.currentTimeMillis();

            domainLastAccess.putIfAbsent(domain, 0L);
            long lastAccess = domainLastAccess.get(domain);

            if (now - lastAccess < DELAY_MS) {
                Thread.sleep(DELAY_MS - (now - lastAccess));
            }

            domainLastAccess.put(domain, System.currentTimeMillis());
        } catch (Exception e) {
            log.warn("Rate limiting failed for URL: {}", url, e);
        }
    }

}

