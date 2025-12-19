package io.livestock.pipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.livestock.domain.NewsModels.Article;
import io.livestock.domain.NewsModels.NewsApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Component
@Profile("!mock")
public class ApiNewsFetchStrategy implements NewsFetchStrategy {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ApiNewsFetchStrategy.class);
    private static final String[] CATEGORIES = { "business", "technology", "science", "general" };

    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private int currentCategoryIndex = 0;

    @org.springframework.beans.factory.annotation.Autowired
    public ApiNewsFetchStrategy(
            @Value("${news.api.key}") String apiKey,
            ObjectMapper objectMapper) {
        this(apiKey, objectMapper, HttpClient.newHttpClient());
    }

    public ApiNewsFetchStrategy(String apiKey, ObjectMapper objectMapper, HttpClient httpClient) {
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
    }

    @Override
    public Flux<List<Article>> fetchArticles() {
        return Flux.create(sink -> {
            try {
                // Rotate category
                String category = CATEGORIES[currentCategoryIndex];
                currentCategoryIndex = (currentCategoryIndex + 1) % CATEGORIES.length;

                String url = "https://newsapi.org/v2/top-headlines?country=us&category=" + category + "&apiKey="
                        + apiKey;

                logger.info("Fetching news from NewsAPI: {}",
                        "https://newsapi.org/v2/top-headlines?country=us&category=" + category + "&apiKey=[HIDDEN]");

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    processResponse(response.body(), sink);
                } else {
                    logger.error("News API Error: {} Status {}", response, response.statusCode());
                    sink.error(new RuntimeException("API Error: " + response.statusCode()));
                }
            } catch (Exception e) {
                logger.error("News API Exception", e);
                sink.error(e); // Propagate error but don't stop the flux forever (retry handled upstream if
                // any)
            }
        });
    }

    /**
     * Process the response from the News API and emit articles to the sink.
     *
     * @param body The response body as a string.
     * @param sink The sink to emit articles to.
     */
    private void processResponse(String body, reactor.core.publisher.FluxSink<List<Article>> sink) {
        try {
            logger.info("Received News API Response: {} Content-Length: {}", body, body.length());
            NewsApiResponse apiResponse = objectMapper.readValue(body, NewsApiResponse.class);
            sink.next(apiResponse.articles());
            sink.complete();
        } catch (Exception e) {
            sink.error(e);
        }
    }
}
