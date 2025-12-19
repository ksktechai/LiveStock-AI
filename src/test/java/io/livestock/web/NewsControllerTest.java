package io.livestock.web;

import io.livestock.domain.NewsAnalysis;
import io.livestock.domain.NewsItem;
import io.livestock.domain.Sentiment;
import io.livestock.pipeline.NewsPipeline;
import io.livestock.pipeline.RealNewsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsControllerTest {

    @Mock
    private NewsPipeline pipeline;
    @Mock
    private RealNewsService realNewsService;

    private WebTestClient webClient;

    @BeforeEach
    void setUp() {
        NewsController controller = new NewsController(pipeline, realNewsService);
        webClient = WebTestClient.bindToController(controller).build();
    }

    @Test
    void testStreamEndpoint() {
        NewsAnalysis analysis = new NewsAnalysis(
                UUID.randomUUID().toString(),
                Instant.now(), "Source", "Headline", "URL", Sentiment.NEUTRAL, 5, "Summary");

        when(pipeline.stream()).thenReturn(Flux.just(analysis));

        webClient.get().uri("/api/stream")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(NewsAnalysis.class)
                .hasSize(1);
    }

    @Test
    void testIngestEndpoint() {
        NewsItem item = new NewsItem("Source", "Headline", "URL", "Content");

        webClient.post().uri("/api/news")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(item)
                .exchange()
                .expectStatus().isOk();

        verify(pipeline).ingest(any(NewsItem.class));
    }

    @Test
    void testStartFeed() {
        webClient.post().uri("/api/feed/start")
                .exchange()
                .expectStatus().isOk();

        verify(realNewsService).start();
    }

    @Test
    void testStopFeed() {
        webClient.post().uri("/api/feed/stop")
                .exchange()
                .expectStatus().isOk();

        verify(realNewsService).stop();
    }
}
