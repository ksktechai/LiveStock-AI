package io.livestock.pipeline;

import io.livestock.ai.NewsAiAnalyzer;
import io.livestock.domain.NewsAnalysis;
import io.livestock.domain.NewsItem;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * Service for processing news items and generating analyses.
 */
@Service
public class NewsPipeline {

    private final NewsAiAnalyzer newsAiAnalyzer;

    // incoming items
    private final Sinks.Many<NewsItem> ingestSink = Sinks.many().multicast().onBackpressureBuffer();

    // outgoing analyses for SSE
    private final Sinks.Many<NewsAnalysis> analysisSink = Sinks.many().replay().limit(50);

    /**
     * Constructor.
     *
     * @param newsAiAnalyzer The NewsAiAnalyzer instance to use for analysis.
     */
    public NewsPipeline(NewsAiAnalyzer newsAiAnalyzer) {
        this.newsAiAnalyzer = newsAiAnalyzer;

        // Build the pipeline once:
        ingestSink.asFlux()
                .flatMap(newsAiAnalyzer::analyze, 8) // parallelism
                .doOnNext(analysisSink::tryEmitNext)
                .subscribe();
    }

    /**
     * Ingest a new item into the pipeline.
     *
     * @param item The NewsItem to ingest.
     */
    public void ingest(NewsItem item) {
        ingestSink.tryEmitNext(item);
    }

    /**
     * Stream the latest news analyses.
     *
     * @return Flux of NewsAnalysis objects.
     */
    public Flux<NewsAnalysis> stream() {
        return analysisSink.asFlux();
    }
}
