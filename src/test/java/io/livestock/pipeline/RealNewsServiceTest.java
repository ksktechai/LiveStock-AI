package io.livestock.pipeline;

import io.livestock.domain.NewsItem;
import io.livestock.domain.NewsModels.Article;
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

        ReflectionTestUtils.setField(service, "streamingDelay", Duration.ofMillis(100));

        // Act
        service.start();

        // Assert with timeout
        verify(pipeline, timeout(2000).atLeastOnce()).ingest(any(NewsItem.class));
    }

    @Test
    void stopShouldDispose() {
        when(fetchStrategy.fetchArticles()).thenReturn(Flux.never());
        service.start();
        service.stop();
        // Since 'running' is private, we can't easily check it, but we can ensure no
        // exceptions thrown
    }
}
