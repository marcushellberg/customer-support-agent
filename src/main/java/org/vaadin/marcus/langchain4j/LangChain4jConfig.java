package org.vaadin.marcus.langchain4j;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;

/**
 * @TODO: This entire class will go away once https://github.com/quarkiverse/quarkus-langchain4j/pull/1027 is merged and released
 */
public class LangChain4jConfig {

    @Produces
    @ApplicationScoped
    public RetrievalAugmentor retrievalAugmentor(EmbeddingStore embeddingStore, EmbeddingModel embeddingModel) {
        var contentRetriever = EmbeddingStoreContentRetriever.builder()
          .embeddingStore(embeddingStore)
          .embeddingModel(embeddingModel)
          .maxResults(2)
          .minScore(0.6)
          .build();

        return DefaultRetrievalAugmentor.builder()
          .contentRetriever(contentRetriever)
          .build();
    }
}
