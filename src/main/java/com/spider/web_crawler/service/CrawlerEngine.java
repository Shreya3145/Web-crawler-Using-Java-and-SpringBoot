package com.spider.web_crawler.service;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CrawlerEngine {
    private final Queue<String> queue = new LinkedList<>();
    // private final Set<String> visited = new HashSet<>();
    private final PageFetcher pageFetcher;
    private final PageExtractor pageExtractor;
    private final RedisService redisService;

    public CrawlerEngine(PageFetcher pageFetcher, PageExtractor pageExtractor, RedisService redisService) {
        this.pageFetcher = pageFetcher;
        this.pageExtractor = pageExtractor;
        this.redisService = redisService;
    }

    public void beginCrawl(String url, int maxPages) {
        queue.add(url);
        redisService.markVisited(url);
        // visited.add(url);

        int crawledCount = 0;

        while (!queue.isEmpty() && crawledCount < maxPages) { //visited.size() < maxPages
            String currentUrl = queue.poll();
            crawledCount++;

            Document doc = pageFetcher.fetchPage(currentUrl);
            if (doc == null) continue;

            pageExtractor.extractAndSave(doc, currentUrl);

            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String absUrl = link.attr("abs:href");
                if (absUrl.isEmpty() ||  redisService.isVisited(absUrl) || !absUrl.startsWith("http")) {
                    continue;
                }
                queue.add(absUrl);
                redisService.markVisited(absUrl);
                // visited.add(absUrl);
            }
        }
    }
}