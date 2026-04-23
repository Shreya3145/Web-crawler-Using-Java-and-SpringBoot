package com.spider.web_crawler.service;

import com.spider.web_crawler.entity.JobPosting;
import com.spider.web_crawler.repository.JobPostingRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.LocalDateTime;

@Component
public class PageExtractor {
    private final JobPostingRepository jobPostingRepository;

    public PageExtractor(JobPostingRepository jobPostingRepository) {
        this.jobPostingRepository = jobPostingRepository;
    }

    public void extractAndSave(Document doc, String url) {
        try {
            if (jobPostingRepository.findByUrl(url).isPresent()) {
                return;
            }
            URI uri = new URI(url);
            String host = uri.getHost();

            JobPosting jobPosting = new JobPosting();
            jobPosting.setSource(host);
            jobPosting.setUrl(url);
            jobPosting.setJobTitle(doc.title());
            jobPosting.setJobDescription(doc.body().text());
            jobPosting.setCompany("Unknown");
            jobPosting.setLocation("Unknown");

            saveOrUpdateJob(jobPosting);
        } catch (Exception e) {
            System.err.println("Failed to extract: " + url + " - " + e.getMessage());
        }
    }

    private String generateJobHash(String company, String title, String location) {
        String normalized = (company + "|" + title + "|" + location)
                .toLowerCase()
                .replaceAll("\\s+", "")
                .trim();
        return DigestUtils.sha256Hex(normalized);
    }

    private void saveOrUpdateJob(JobPosting jobPosting) {
        String hash = generateJobHash(
                jobPosting.getCompany(),
                jobPosting.getJobTitle(),
                jobPosting.getLocation()
        );

        JobPosting existing = jobPostingRepository.findByJobHash(hash);

        if (existing != null && existing.isActive()) {
            existing.setLastSeenAt(LocalDateTime.now());
            jobPostingRepository.save(existing);
        } else {
            jobPosting.setJobHash(hash);
            jobPosting.setLastSeenAt(LocalDateTime.now());
            jobPostingRepository.save(jobPosting);
        }
    }
}