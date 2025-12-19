package io.livestock.pipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.livestock.domain.NewsModels.NewsApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileNewsFetchStrategyTest {

    private FileNewsFetchStrategy strategy;

    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        strategy = new FileNewsFetchStrategy(objectMapper);
    }

    @Test
    void fetchArticlesShouldReturnArticlesFromFile() throws IOException {
        NewsApiResponse apiResponse = new NewsApiResponse("ok", 0, Collections.emptyList());

        // We can't easily mock the InputStream from ClassPathResource without more
        // complex setup
        // But we can verify that objectMapper is called with *an* InputStream
        when(objectMapper.readValue(any(InputStream.class), eq(NewsApiResponse.class))).thenReturn(apiResponse);

        StepVerifier.create(strategy.fetchArticles())
                .expectNextMatches(List::isEmpty)
                .verifyComplete();
    }

    @Test
    void fetchArticlesShouldErrorOnReadFailure() throws IOException {
        when(objectMapper.readValue(any(InputStream.class), eq(NewsApiResponse.class)))
                .thenThrow(new IOException("File read error"));

        StepVerifier.create(strategy.fetchArticles())
                .verifyErrorMatches(e -> e.getMessage().contains("File read error"));
    }
}
