package io.livestock.pipeline;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.livestock.domain.NewsModels.Article;
import io.livestock.domain.NewsModels.NewsApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;

import org.mockito.ArgumentMatchers;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiNewsFetchStrategyTest {

    private ApiNewsFetchStrategy strategy;

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private HttpClient httpClient;
    @Mock
    private HttpResponse<String> httpResponse;

    @BeforeEach
    void setUp() {
        strategy = new ApiNewsFetchStrategy("test-key", objectMapper, httpClient);
    }

    @Test
    void fetchArticlesShouldReturnArticlesOnSuccess() throws IOException, InterruptedException {
        String json = "{\"status\":\"ok\",\"articles\":[]}";
        NewsApiResponse apiResponse = new NewsApiResponse("ok", 0, Collections.emptyList());

        when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(json);
        when(objectMapper.readValue(json, NewsApiResponse.class)).thenReturn(apiResponse);

        StepVerifier.create(strategy.fetchArticles())
                .expectNextMatches(List::isEmpty)
                .verifyComplete();
    }

    @Test
    void fetchArticlesShouldErrorOnNon200Status() throws IOException, InterruptedException {
        when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(500);

        StepVerifier.create(strategy.fetchArticles())
                .verifyErrorMatches(e -> e.getMessage().contains("API Error: 500"));
    }

    @Test
    void fetchArticlesShouldErrorOnNetworkException() throws IOException, InterruptedException {
        when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenThrow(new IOException("Network error"));

        StepVerifier.create(strategy.fetchArticles())
                .verifyErrorMatches(e -> e.getMessage().contains("Network error"));
    }

    @Test
    void fetchArticlesShouldErrorOnJsonParseException() throws IOException, InterruptedException {
        String json = "invalid-json";

        when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(json);
        when(objectMapper.readValue(json, NewsApiResponse.class))
                .thenThrow(new com.fasterxml.jackson.core.JsonParseException(null, "Parse error"));

        StepVerifier.create(strategy.fetchArticles())
                .verifyError(com.fasterxml.jackson.core.JsonParseException.class);
    }
}
