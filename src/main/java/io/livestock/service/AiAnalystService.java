package io.livestock.service;

import io.livestock.domain.NewsModels.MarketNews;
import io.livestock.domain.NewsModels.SentimentAnalysis;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AiAnalystService {

    private final ChatClient chatClient;

    public AiAnalystService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public SentimentAnalysis analyze(MarketNews news) {
        // Prompt Engineering inside the code
        String prompt = """
                Analyze the following financial news headline for stock: %s
                Headline: "%s"
                
                Respond ONLY with the JSON structure matching the SentimentAnalysis record.
                """
                .formatted(news.symbol(), news.headline());

        // Call AI and map directly to Java Record (Structured Output)
        return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(SentimentAnalysis.class);
    }

    // Java 25 Style Switch Expression (demonstration)
    public String getActionRecommendation(SentimentAnalysis analysis) {
        return switch (analysis.sentiment().toUpperCase(Locale.ROOT)) {
            case "BULLISH" -> {
                if (analysis.confidenceScore() > 80) yield "STRONG BUY";
                yield "BUY";
            }
            case "BEARISH" -> {
                if (analysis.confidenceScore() > 80) yield "STRONG SELL";
                yield "SELL";
            }
            case "NEUTRAL" -> "HOLD";
            default -> "REVIEW MANUALLY";
        };
    }
}
