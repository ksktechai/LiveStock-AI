package io.livestock.pipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.livestock.domain.NewsModels.Article;
import io.livestock.domain.NewsModels.NewsApiResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
@Profile("mock")
public class FileNewsFetchStrategy implements NewsFetchStrategy {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(FileNewsFetchStrategy.class);
    private final ObjectMapper objectMapper;

    public FileNewsFetchStrategy(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Flux<List<Article>> fetchArticles() {
        return Flux.create(sink -> {
            try {
                logger.info("Reading news from mock file: news-mock-data.json");
                ClassPathResource resource = new ClassPathResource("news-mock-data.json");
                NewsApiResponse apiResponse = objectMapper.readValue(resource.getInputStream(), NewsApiResponse.class);
                sink.next(apiResponse.articles());
            } catch (Exception e) {
                logger.error("Failed to read mock news data", e);
                // In mock mode, if file fails, we might just emit empty list or error
                sink.error(e);
            }
        });
    }
}
