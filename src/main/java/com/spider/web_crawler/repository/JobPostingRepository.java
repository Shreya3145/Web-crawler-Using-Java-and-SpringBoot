package com.spider.web_crawler.repository;

import com.spider.web_crawler.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    boolean existsByUrl(String url);

    List<JobPosting> findByJobTitleContainingOrJobDescriptionContaining(String keyword1, String keyword2);

    Optional<JobPosting> findByUrl(String url);

    JobPosting findByJobHash(String jobHash);
    }


