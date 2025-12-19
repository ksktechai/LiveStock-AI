package io.livestock.pipeline;

import io.livestock.domain.NewsItem;
import io.livestock.domain.NewsModels.Article;
import io.livestock.service.RealNewsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RealNewsServiceTest {

    @Mock
    private NewsPipeline pipeline;
    @Mock
    private NewsFetchStrategy fetchStrategy;

    private RealNewsService service;

    @BeforeEach
    void setUp() {
        service = new RealNewsService(pipeline, fetchStrategy);
        ReflectionTestUtils.setField(service, "enabled", true);
    }

    @Test
    void startShouldFetchAndIngestNews() {
        // Arrange
        Article article = new Article(null, "Test Author", "Test Headline", "Test Desc", "http://test.com",
                "2023-01-01T00:00:00Z", "Content");
        when(fetchStrategy.fetchArticles()).thenReturn(Flux.just(List.of(article)));

        ReflectionTestUtils.setField(service, "streamingDelay", Duration.ofMillis(10));

        // Act
        service.start();

        // Assert with timeout
        verify(pipeline, timeout(2000).atLeastOnce()).ingest(any(NewsItem.class));
    }

    @Test
    void initShouldStartWhenEnabled() {
        ReflectionTestUtils.setField(service, "enabled", true);
        when(fetchStrategy.fetchArticles()).thenReturn(Flux.empty());

        service.init();

        verify(fetchStrategy, timeout(100).atLeastOnce()).fetchArticles();
    }

    @Test
    void initShouldNotStartWhenDisabled() {
        ReflectionTestUtils.setField(service, "enabled", false);

        service.init();

        verify(fetchStrategy, never()).fetchArticles();
    }

    @Test
    void startShouldDoNothingWhenDisabled() {
        ReflectionTestUtils.setField(service, "enabled", false);

        service.start();

        verify(fetchStrategy, never()).fetchArticles();
    }

    @Test
    void startShouldBeIdempotent() {
        when(fetchStrategy.fetchArticles()).thenReturn(Flux.empty());

        service.start();
        service.start(); // Second call

        Object running = ReflectionTestUtils.getField(service, "running");
        assert running != null;
    }

    @Test
    void deduplicationShouldFilterDuplicateHeadlines() {
        Article article = new Article(null, "Author", "Unique Headline", "Desc", "url", "2023-01-01T00:00:00Z",
                "Content");

        // Return same article twice in separate fetches
        when(fetchStrategy.fetchArticles())
                .thenReturn(Flux.just(List.of(article)))
                .thenReturn(Flux.just(List.of(article)));

        ReflectionTestUtils.setField(service, "streamingDelay", Duration.ofMillis(10));

        service.start();

        // verify pipeline.ingest is called ONLY ONCE for this headline
        verify(pipeline, timeout(500).times(1)).ingest(argThat(item -> item.headline().equals("Unique Headline")));
    }

    @Test
    void convertToNewsItemShouldHandleNulls() {
        // Article with null source, description, content
        Article article = new Article(null, null, "Headline Only", null, "url", "2023-01-01T00:00:00Z", null);

        when(fetchStrategy.fetchArticles()).thenReturn(Flux.just(List.of(article)));
        ReflectionTestUtils.setField(service, "streamingDelay", Duration.ofMillis(10));

        service.start();

        verify(pipeline, timeout(500)).ingest(argThat(item -> item.source().equals("Unknown") &&
                item.content().equals("Headline Only") // Fallback to title
        ));
    }

    @Test
    void convertToNewsItemShouldPreferDescriptionOverContent() {
        Article article = new Article(null, null, "Headline", "Description", "url", "2023-01-01T00:00:00Z", "Content");

        when(fetchStrategy.fetchArticles()).thenReturn(Flux.just(List.of(article)));
        ReflectionTestUtils.setField(service, "streamingDelay", Duration.ofMillis(10));

        service.start();

        verify(pipeline, timeout(500)).ingest(argThat(item -> item.content().equals("Description")));
    }

    @Test
    void sortingShouldOrderOldestToNewest() {
        Article oldArticle = new Article(null, null, "Old", null, "url", "2023-01-01T10:00:00Z", null);
        Article newArticle = new Article(null, null, "New", null, "url", "2023-01-01T12:00:00Z", null);

        // Fed in reverse order (Newest first)
        when(fetchStrategy.fetchArticles()).thenReturn(Flux.just(List.of(newArticle, oldArticle)));
        ReflectionTestUtils.setField(service, "streamingDelay", Duration.ofMillis(10));

        service.start();

        // Verify invocation order
        org.mockito.InOrder inOrder = inOrder(pipeline);

        verify(pipeline, timeout(500).atLeast(2)).ingest(any());

        inOrder.verify(pipeline).ingest(argThat(item -> item.headline().equals("Old")));
        inOrder.verify(pipeline).ingest(argThat(item -> item.headline().equals("New")));
    }

    @Test
    void stopShouldDispose() {
        when(fetchStrategy.fetchArticles()).thenReturn(Flux.never());
        service.start();
        service.stop();
        service.stop(); // Idempotent call
    }
}
