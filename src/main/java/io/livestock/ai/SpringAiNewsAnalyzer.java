package io.livestock.ai;

import io.livestock.domain.NewsAnalysis;
import io.livestock.domain.NewsItem;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.UUID;

/**
 * Spring AI News Analyzer.
 */
public class SpringAiNewsAnalyzer implements NewsAiAnalyzer {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SpringAiNewsAnalyzer.class);

    private final ChatClient chatClient;

    public SpringAiNewsAnalyzer(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Analyzes news item; returns analysis with AI insights
     *
     * @param item The news item to analyze.
     * @return A NewsAnalysis object.
     */
    @Override
    public Mono<NewsAnalysis> analyze(NewsItem item) {
        return Mono.fromCallable(() -> {
            String systemPrompt = AiPrompt.SYSTEM;
            String userPrompt = AiPrompt.user(item.source(), item.headline(), item.content());

            logger.info("Sending Prompt to AI. Source: {}, Headline: {}", item.source(), item.headline());
            logger.info("Full AI Prompt: System=[{}], User=[{}]", systemPrompt, userPrompt);

            String raw = chatClient.prompt()
                    .messages(
                            new SystemMessage(systemPrompt),
                            new UserMessage(userPrompt))
                    .call()
                    .content();

            logger.info("Received AI Response for '{}': {}", item.headline(), raw);

            var parsed = AiJsonParser.parse(raw);

            // Creates news analysis from parsed AI response
            return new NewsAnalysis(
                    UUID.randomUUID().toString(),
                    Instant.now(),
                    item.source(),
                    item.headline(),
                    item.url(),
                    parsed.sentiment(),
                    parsed.riskScore(),
                    parsed.summary());
        })
                .subscribeOn(Schedulers.boundedElastic());
    }
}
