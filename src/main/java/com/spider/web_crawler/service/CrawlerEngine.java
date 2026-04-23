package com.spider.web_crawler.service;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CrawlerEngine {
    private Queue<String> queue = new LinkedList<>();
    private Set<String> visited = new HashSet<>();
    private final PageFetcher pageFetcher;
    private final PageExtractor pageExtractor;

    public CrawlerEngine(PageFetcher pageFetcher, PageExtractor pageExtractor) {
        this.pageFetcher = pageFetcher;
        this.pageExtractor = pageExtractor;
    }

    public void beginCrawl(String url, int maxPages) {
        queue.add(url);
        visited.add(url);

        while (!queue.isEmpty() && visited.size() < maxPages) {
            String currentUrl = queue.poll();

            Document doc = pageFetcher.fetchPage(currentUrl);
            if (doc == null) continue;

            pageExtractor.extractAndSave(doc, currentUrl);

            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String absUrl = link.attr("abs:href");
                if (absUrl.isEmpty() || visited.contains(absUrl) || !absUrl.startsWith("http")) {
                    continue;
                }
                queue.add(absUrl);
                visited.add(absUrl);
            }
        }
    }
}