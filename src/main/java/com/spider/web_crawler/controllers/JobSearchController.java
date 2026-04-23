package com.spider.web_crawler.controllers;


import com.spider.web_crawler.entity.JobPosting;
import com.spider.web_crawler.repository.JobPostingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController

@RequestMapping("/jobs")
public class JobSearchController {
    private final JobPostingRepository jobPostingRepository;

    public JobSearchController(JobPostingRepository jobPostingRepository){
        this.jobPostingRepository = jobPostingRepository;
    }

    @GetMapping("/search")
    public ResponseEntity<List<JobPosting>> searchJobs(@RequestParam String keyword){
        return ResponseEntity.ok(jobPostingRepository.findByJobTitleContainingOrJobDescriptionContaining(keyword, keyword));
    }
}
