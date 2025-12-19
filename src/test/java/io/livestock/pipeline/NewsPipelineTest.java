package io.livestock.pipeline;

import io.livestock.ai.NewsAiAnalyzer;
import io.livestock.domain.NewsAnalysis;
import io.livestock.domain.NewsItem;
import io.livestock.domain.Sentiment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsPipelineTest {

    @Mock
    private NewsAiAnalyzer analyzer;

    @Test
    void shouldIngestAnalyzeAndEmit() {
        // Arrange
        NewsItem item = new NewsItem("Source", "Headline", "URL", "Content");
        NewsAnalysis analysis = new NewsAnalysis(
                UUID.randomUUID().toString(),
                Instant.now(),
                "Source",
                "Headline",
                "URL",
                Sentiment.BULLISH,
                5,
                "Summary");

        when(analyzer.analyze(any(NewsItem.class))).thenReturn(Mono.just(analysis));

        NewsPipeline pipeline = new NewsPipeline(analyzer);

        // Act & Assert
        StepVerifier.create(pipeline.stream())
                .then(() -> pipeline.ingest(item)) // Trigger ingest
                .expectNext(analysis) // Expect the analyzed item
                .thenCancel() // Stop listening
                .verify();
    }
}
