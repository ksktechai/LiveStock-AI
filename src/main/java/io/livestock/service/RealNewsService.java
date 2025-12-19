package io.livestock.service;

import io.livestock.domain.NewsItem;
import io.livestock.domain.NewsModels.Article;
import io.livestock.pipeline.NewsFetchStrategy;
import io.livestock.pipeline.NewsPipeline;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service for fetching and processing real-time (or mock) news.
 */
@Service
public class RealNewsService {

    @Value("${livestock.real-feed.enabled:false}")
    private boolean enabled;

    private final NewsPipeline pipeline;
    private final NewsFetchStrategy fetchStrategy;

    private final AtomicReference<Disposable> running = new AtomicReference<>();

    // Cache to prevent duplicate processing
    private final List<String> processedHeadlines = Collections.synchronizedList(new ArrayList<>());

    // Configurable delay for testing
    private final Duration streamingDelay = Duration.ofSeconds(5);

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RealNewsService.class);

    public RealNewsService(NewsPipeline pipeline, NewsFetchStrategy fetchStrategy) {
        this.pipeline = pipeline;
        this.fetchStrategy = fetchStrategy;
    }

    /**
     * Initialize the service and start the news pipeline if enabled.
     */
    @jakarta.annotation.PostConstruct
    public void init() {
        if (enabled) {
            start();
        }
    }

    /**
     * Start the news pipeline if it is enabled and not already running.
     * This method is synchronized to ensure thread safety.
     */
    public synchronized void start() {
        if (!enabled)
            return;
        if (running.get() != null && !running.get().isDisposed())
            return;

        Disposable disposable = Flux.interval(Duration.ZERO, Duration.ofMinutes(2))
                .flatMap(tick -> fetchStrategy.fetchArticles())
                .map(list -> {
                    // Sort Oldest -> Newest
                    List<Article> sorted = new ArrayList<>(list);
                    sorted.sort(java.util.Comparator.comparing(Article::publishedAt));
                    return sorted;
                })
                .flatMapIterable(list -> list) // Flatten List<Article> to Article
                .filter(article -> !processedHeadlines.contains(article.title())) // Deduplicate
                .delayElements(this.streamingDelay) // The "Streaming" Effect
                .map(this::convertToNewsItem)
                .doOnNext(news -> processedHeadlines.add(news.headline()))
                .subscribe(pipeline::ingest);

        running.set(disposable);
        logger.info("RealNewsService started.");
    }

    public synchronized void stop() {
        Disposable disposable = running.getAndSet(null);
        if (disposable != null) {
            disposable.dispose();
            logger.info("RealNewsService stopped.");
        }
    }

    /**
     * Convert a NewsAPI Article to a NewsItem for processing.
     *
     * @param article The NewsAPI Article to convert.
     * @return A NewsItem representing the article.
     */
    private NewsItem convertToNewsItem(Article article) {
        String source = article.source() != null ? article.source().name() : "Unknown";
        String content = article.description() != null ? article.description()
                : (article.content() != null ? article.content() : article.title());

        return new NewsItem(
                source,
                article.title(),
                article.url(),
                content);
    }
}