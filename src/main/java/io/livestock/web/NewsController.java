package io.livestock.web;

import io.livestock.domain.FeedStatus;
import io.livestock.domain.NewsItem;
import io.livestock.pipeline.NewsPipeline;
import io.livestock.pipeline.RealNewsService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * Controller for managing news-related operations.
 */
@RestController
@RequestMapping("/api")
public class NewsController {

    private final NewsPipeline pipeline;
    private final RealNewsService realNewsService; // Add this

    public NewsController(NewsPipeline pipeline, RealNewsService realNewsService) {
        this.pipeline = pipeline;
        this.realNewsService = realNewsService;
    }

    /**
     * Ingest a news item into the pipeline.
     *
     * @param item The news item to ingest.
     */
    @PostMapping("/news")
    public void ingest(@Valid @RequestBody NewsItem item) {
        pipeline.ingest(item);
    }

    /**
     * Stream the latest news analyses.
     *
     * @return Flux of NewsAnalysis objects.
     */
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<?> stream() {
        // returning NewsAnalysis records directly works; browser EventSource receives
        // JSON per event
        return pipeline.stream();
    }

    /**
     * Start the news feed.
     */
    @PostMapping("/feed/start")
    public void startFeed() {
        if (realNewsService != null)
            realNewsService.start();
    }

    /**
     * Stop the news feed.
     */
    @PostMapping("/feed/stop")
    public void stopFeed() {
        if (realNewsService != null)
            realNewsService.stop();
    }

    /**
     * Check the status of the news feed.
     *
     * @return FeedStatus object indicating the running status.
     */
    @GetMapping("/feed/status")
    public FeedStatus status() {
        return new FeedStatus(true); // Always return true for now, or update logic
    }

}
