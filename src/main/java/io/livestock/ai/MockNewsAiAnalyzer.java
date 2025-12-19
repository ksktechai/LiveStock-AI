package io.livestock.ai;

import io.livestock.domain.NewsAnalysis;
import io.livestock.domain.NewsItem;
import io.livestock.domain.Sentiment;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Mock News AI Analyzer.
 */
@Component
@ConditionalOnProperty(prefix = "livestock.ai", name = "mode", havingValue = "mock", matchIfMissing = true)
public class MockNewsAiAnalyzer implements NewsAiAnalyzer {

    /**
     * Analyze a news item with mock data.
     *
     * @param item The news item to analyze.
     * @return A Mono containing the analyzed news item.
     */
    @Override
    public Mono<NewsAnalysis> analyze(NewsItem item) {
        String text = (item.headline() + " " + item.content()).toLowerCase();

        Sentiment sentiment = (text.contains("beats") || text.contains("surge") || text.contains("record")
                || text.contains("upgrade")) ? Sentiment.BULLISH
                : (text.contains("miss") || text.contains("plunge") || text.contains("downgrade")
                || text.contains("lawsuit")) ? Sentiment.BEARISH : Sentiment.NEUTRAL;

        int risk = sentiment == Sentiment.BEARISH ? 8 : (sentiment == Sentiment.BULLISH ? 3 : 5);
        String summary = "Mock summary: " + item.headline();

        // Creates immutable news analysis result
        return Mono.just(new NewsAnalysis(
                UUID.randomUUID().toString(),
                Instant.now(),
                item.source(),
                item.headline(),
                item.url(),
                sentiment,
                risk,
                summary));
    }
}
