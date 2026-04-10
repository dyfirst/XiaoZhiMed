package com.example.xiaozhimed;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmbeddingTest {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Test
    public void testEmbeddingModel(){
        Response<Embedding> embed = embeddingModel.embed("你好");
        System.out.println("向量维度，" + embed.content().dimension());
        System.out.println("向量输出，" + embed.toString());
    }

    @Autowired
    private EmbeddingStore embeddingStore;

    @Test
    public void testEmbeddingStore(){
        //将文本转换成向量
        TextSegment segment1 = TextSegment.from("I like playing football");
        Embedding embedding1 = embeddingModel.embed(segment1).content();

        //存入向量数据库
        embeddingStore.add(embedding1, segment1);

        TextSegment segment2 = TextSegment.from("I like playing basketball");
        Embedding embedding2 = embeddingModel.embed(segment2).content();
        embeddingStore.add(embedding2, segment2);
    }

    @Test
    public void testEmbeddingQuery(){
        Embedding queryEmbedding = embeddingModel.embed("最近天气怎么样？").content();

        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(3)
//                .minScore(0.8)
                .build();

        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);

        EmbeddingMatch<TextSegment> embeddingMatch1 = searchResult.matches().get(0);
        EmbeddingMatch<TextSegment> embeddingMatch2 = searchResult.matches().get(1);
        System.out.println(embeddingMatch1.score() + " " + embeddingMatch1.embedded().text());
        System.out.println();
        System.out.println(embeddingMatch2.score() + " " + embeddingMatch2.embedded().text());
    }

}
