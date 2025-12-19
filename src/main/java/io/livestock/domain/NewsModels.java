package io.livestock.domain;

import java.util.List;

/**
 * News Models.
 */
public class NewsModels {

    // 1. Structure for the External API Response (NewsAPI.org)
    public record NewsApiResponse(
            String status,
            int totalResults,
            List<Article> articles
    ) {
    }

    public record Article(
            Source source,
            String author,
            String title,
            String description,
            String url,
            String publishedAt,
            String content
    ) {
    }

    public record Source(String id, String name) {
    }

    // 2. Internal Domain Model (Cleaned up for our App)
    public record MarketNews(String symbol, String headline, String url, String timestamp) {
    }

    // 3. Output Analysis (Same as before)
    public record SentimentAnalysis(
            String symbol,
            String sentiment,
            int confidenceScore,
            String reasoning
    ) {
    }
}