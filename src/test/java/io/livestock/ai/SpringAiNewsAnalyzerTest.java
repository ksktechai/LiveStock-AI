package io.livestock.ai;

import io.livestock.domain.NewsAnalysis;
import io.livestock.domain.NewsItem;
import io.livestock.domain.Sentiment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpringAiNewsAnalyzerTest {

    @Mock
    private ChatClient chatClient;
    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;
    @Mock
    private ChatClient.CallResponseSpec responseSpec;

    @Test
    void analyzeShouldReturnAnalysis() {
        // Arrange
        String aiJson = """
                {
                    "sentiment": "BULLISH",
                    "riskScore": 2,
                    "summary": "AI Summary"
                }
                """;

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.messages(any(Message.class), any(Message.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(aiJson);

        SpringAiNewsAnalyzer analyzer = new SpringAiNewsAnalyzer(chatClient);
        NewsItem item = new NewsItem("Source", "Headline", "URL", "Content");

        // Act & Assert
        StepVerifier.create(analyzer.analyze(item))
                .assertNext(analysis -> {
                    assertThat(analysis.sentiment()).isEqualTo(Sentiment.BULLISH);
                    assertThat(analysis.riskScore()).isEqualTo(2);
                    assertThat(analysis.summary()).isEqualTo("AI Summary");
                })
                .verifyComplete();
    }
}
