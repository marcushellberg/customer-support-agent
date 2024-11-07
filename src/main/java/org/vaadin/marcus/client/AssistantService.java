package org.vaadin.marcus.client;

import org.vaadin.marcus.langchain4j.LangChain4jAssistant;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;

@BrowserCallable
@AnonymousAllowed
public class AssistantService {

    private final LangChain4jAssistant langChain4JAssistant;

    public AssistantService(LangChain4jAssistant langChain4JAssistant) {
        this.langChain4JAssistant = langChain4JAssistant;
    }

    public String chat(String chatId, String userMessage) {
        return langChain4JAssistant.chat(chatId, userMessage);
    }
}
