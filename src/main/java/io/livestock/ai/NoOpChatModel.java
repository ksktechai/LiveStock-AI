package io.livestock.ai;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.ChatOptions;

public class NoOpChatModel implements ChatModel {

    @Override
    public ChatResponse call(Prompt prompt) {
        throw new UnsupportedOperationException("NoOpChatModel should not be called in mock mode");
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return null;
    }

    // Some versions of Spring AI might have other methods
    // implementing only main ones for now, hoping default methods cover others
}
