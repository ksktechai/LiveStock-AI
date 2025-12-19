package io.livestock.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.prompt.Prompt;

import static org.junit.jupiter.api.Assertions.*;

class NoOpChatModelTest {

    private NoOpChatModel chatModel;

    @BeforeEach
    void setUp() {
        chatModel = new NoOpChatModel();
    }

    @Test
    void callShouldThrowUnsupportedOperationException() {
        Prompt prompt = new Prompt("test");
        assertThrows(UnsupportedOperationException.class, () -> chatModel.call(prompt));
    }

    @Test
    void getDefaultOptionsShouldReturnNull() {
        assertNull(chatModel.getDefaultOptions());
    }
}
