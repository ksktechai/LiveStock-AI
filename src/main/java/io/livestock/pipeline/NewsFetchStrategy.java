package io.livestock.pipeline;

import io.livestock.domain.NewsModels.Article;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Strategy interface for fetching news articles.
 */
public interface NewsFetchStrategy {
    /**
     * Fetch a list of articles.
     * 
     * @return Flux of article lists.
     */
    Flux<List<Article>> fetchArticles();
}
