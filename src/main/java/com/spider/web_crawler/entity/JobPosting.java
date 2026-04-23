package com.spider.web_crawler.entity;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name="job_posting", indexes = {
        @Index(name = "idx_url", columnList = "url"),
        @Index(name = "idx_company_title_location", columnList = "company,jobTitle,location"),
        @Index(name = "idx_source", columnList = "source")
})

public class JobPosting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String url;

    @Column(length = 300, nullable = false)
    private String jobTitle;

    @Column(length = 300, nullable = false)
    private String company;

    @Column(length = 500, nullable = false)
    private String location;

    @Lob
    @Column(name = "job_description", columnDefinition = "LONGTEXT")
    private String jobDescription;

    private String source;

    @CreationTimestamp
    private LocalDateTime crawledAt;

    @Column(name = "last_seen")
    private LocalDateTime lastSeenAt;

    @Column(name = "job_hash", unique = true, nullable = false)
    private String jobHash;

    //Salary
    private Long minSalary;
    private Long maxSalary;
    private String salaryCurrency;
    private String salaryFrequency;

    //Job Metadata
    private String experienceLevel;
    private String jobType;

    private boolean isActive = true;
}
