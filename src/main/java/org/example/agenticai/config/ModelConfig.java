package org.example.agenticai.config;


import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ModelConfig {

    @Value("${openai.api.key}")
    private String apiKey;
    @Value("${openai.model}")
    private String modelName;

    @Value("${ollama.base-url}")
    private String ollamaUrl;
    @Value("${ollama.model-name}")
    private String ollamaModel;

//    @Bean
//    public ChatModel chatModel() {
//        return OpenAiChatModel.builder()
//                .apiKey(apiKey)
//                .modelName(modelName)
//                .build();
//    }

    @Bean
    public ChatModel chatModel() {
        return OllamaChatModel.builder()
                .baseUrl(ollamaUrl)
                .modelName(ollamaModel)
                .timeout(Duration.ofMinutes(5))
                .build();
    }

    @Bean
    public StreamingChatModel streamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .build();
    }
}
