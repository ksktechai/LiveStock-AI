package io.livestock.ai;

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

  @Bean
  @Primary
  ChatModel primaryChatModel(
      @org.springframework.beans.factory.annotation.Value("${livestock.ai.mode:ollama}") String mode,
      @Qualifier("ollamaChatModel") ObjectProvider<ChatModel> ollama,
      @Qualifier("openAiChatModel") ObjectProvider<ChatModel> openai) {
    if ("mock".equalsIgnoreCase(mode)) {
      return new NoOpChatModel();
    }

    if ("openai".equalsIgnoreCase(mode)) {
      ChatModel m = openai.getIfAvailable();
      if (m != null)
        return m;
      throw new IllegalStateException("livestock.ai.mode=openai but no openAiChatModel is configured");
    }

    if ("ollama".equalsIgnoreCase(mode)) {
      ChatModel m = ollama.getIfAvailable();
      if (m != null)
        return m;
      throw new IllegalStateException("livestock.ai.mode=ollama but no ollamaChatModel is configured");
    }

    throw new IllegalStateException("Unsupported livestock.ai.mode=" + mode + " (expected: ollama, openai, mock)");
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
