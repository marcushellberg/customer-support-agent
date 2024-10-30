package org.vaadin.marcus.langchain4j;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.arc.profile.UnlessBuildProfile;

import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.openai.OpenAiTokenizer;

@Dependent
public class LangChain4jTokenizerConfig {
	@Produces
	@ApplicationScoped
	@UnlessBuildProfile("ollama")
	public Tokenizer openAITokenizer(@ConfigProperty(name = "quarkus.langchain4j.openai.chat-model.model-name") String modelName) {
		return new OpenAiTokenizer(modelName);
	}
}
