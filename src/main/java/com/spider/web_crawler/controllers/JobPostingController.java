package com.spider.web_crawler.controllers;

import com.spider.web_crawler.service.CrawlerEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/crawl")
public class JobPostingController {
    private final CrawlerEngine crawlerEngine;

    JobPostingController( CrawlerEngine crawlerEngine){
        this.crawlerEngine = crawlerEngine;
    }

    @GetMapping("/start")
    public ResponseEntity<String> startCrawl(
            @RequestParam String url,
            @RequestParam int limit){
        crawlerEngine.beginCrawl(url, limit);
        return ResponseEntity.ok("Crawling started successfully");
    }
}
