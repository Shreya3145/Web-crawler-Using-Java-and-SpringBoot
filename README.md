# Web Crawler

A web crawler built with Spring Boot, Redis, and MySQL.

## Tech Stack
- Spring Boot 3.5
- Spring Data JPA
- Redis 
- MySQL (persistence)
- Jsoup (HTML parsing)

## Features
- BFS-based web crawling
- Deduplication using content hashing
- Redis caching for scalability
- RESTful API for searching crawled pages

## API
- `GET /crawl/start?url=<url>&limit=<pages>` - Start crawl
- `GET /jobs/search?keyword=<keyword>` - Search crawled pages