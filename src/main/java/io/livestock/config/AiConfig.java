package io.livestock.config;

import io.livestock.ai.NewsAiAnalyzer;
import io.livestock.ai.NoOpChatModel;
import io.livestock.ai.SpringAiNewsAnalyzer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AiConfig {
    private static final String AI_MODE_PROPERTY = "livestock.ai.mode";

    @Bean
    @Primary
    ChatModel primaryChatModel(
            @org.springframework.beans.factory.annotation.Value("${livestock.ai.mode:ollama}") String mode,
            @Qualifier("ollamaChatModel") ObjectProvider<ChatModel> ollama,
            @Qualifier("openAiChatModel") ObjectProvider<ChatModel> openai) {
        return switch (mode.toLowerCase()) {
            case "mock" -> new NoOpChatModel();
            case "openai" -> resolveChatModel(openai, "openai", "openAiChatModel");
            case "ollama" -> resolveChatModel(ollama, "ollama", "ollamaChatModel");
            default -> throw new IllegalStateException(
                    "Unsupported %s=%s (expected: ollama, openai, mock)".formatted(AI_MODE_PROPERTY, mode));
        };
    }

    /**
     * Resolve the chat model from the provided ObjectProvider.
     *
     * @param provider The ObjectProvider containing the chat model.
     * @param modeName The name of the mode being used.
     * @param beanName The name of the bean in the ObjectProvider.
     * @return ChatModel
     */
    private ChatModel resolveChatModel(ObjectProvider<ChatModel> provider, String modeName, String beanName) {
        ChatModel model = provider.getIfAvailable();
        if (model == null) {
            throw new IllegalStateException("%s=%s but no %s is configured".formatted(AI_MODE_PROPERTY, modeName, beanName));
        }
        return model;
    }

    @Bean
    @ConditionalOnProperty(prefix = "livestock.ai", name = "mode", havingValue = "ollama")
    public NewsAiAnalyzer ollamaAnalyzer(ChatClient.Builder builder) {
        return new SpringAiNewsAnalyzer(builder.build());
    }

    @Bean
    @ConditionalOnProperty(prefix = "livestock.ai", name = "mode", havingValue = "openai")
    public NewsAiAnalyzer openAiAnalyzer(ChatClient.Builder builder) {
        return new SpringAiNewsAnalyzer(builder.build());
    }
}
