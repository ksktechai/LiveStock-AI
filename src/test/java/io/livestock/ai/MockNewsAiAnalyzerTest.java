package io.livestock.ai;

import io.livestock.domain.NewsItem;
import io.livestock.domain.Sentiment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class MockNewsAiAnalyzerTest {

    private MockNewsAiAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new MockNewsAiAnalyzer();
    }

    @Test
    void analyzeShouldReturnBullishSentimentForPositiveKeywords() {
        NewsItem item = new NewsItem("Source", "Stock surges to record highs", "http://test.com", "Beats expectations");

        StepVerifier.create(analyzer.analyze(item))
                .expectNextMatches(analysis -> analysis.sentiment() == Sentiment.BULLISH && analysis.riskScore() == 3)
                .verifyComplete();
    }

    @Test
    void analyzeShouldReturnBearishSentimentForNegativeKeywords() {
        NewsItem item = new NewsItem("Source", "Stock plunges after earnings miss", "http://test.com", "Lawsuit filed");

        StepVerifier.create(analyzer.analyze(item))
                .expectNextMatches(analysis -> analysis.sentiment() == Sentiment.BEARISH && analysis.riskScore() == 8)
                .verifyComplete();
    }

    @Test
    void analyzeShouldReturnNeutralSentimentForPlainText() {
        NewsItem item = new NewsItem("Source", "Market is open today", "http://test.com", "Nothing special happening");

        StepVerifier.create(analyzer.analyze(item))
                .expectNextMatches(analysis -> analysis.sentiment() == Sentiment.NEUTRAL && analysis.riskScore() == 5)
                .verifyComplete();
    }
}
