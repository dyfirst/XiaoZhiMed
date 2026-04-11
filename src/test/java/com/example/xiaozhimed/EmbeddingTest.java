package com.example.xiaozhimed;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.Arrays;
import java.util.List;

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

    @Test
    public void testUploadKnowledegeLibrary(){

        //使用默认的文档解析器对文档进行解析
        Document document1 = FileSystemDocumentLoader.loadDocument("E:/JavaProjects/XiaoZhiMed/src/main/resources/knowledge/医院信息.md");
        Document document2 = FileSystemDocumentLoader.loadDocument("E:/JavaProjects/XiaoZhiMed/src/main/resources/knowledge/科室信息.md");
        Document document3 = FileSystemDocumentLoader.loadDocument("E:/JavaProjects/XiaoZhiMed/src/main/resources/knowledge/神经内科.md");
        List<Document> documents = Arrays.asList(document1, document2, document3);

        //文本向量化并存入向量数据库，将每个片段进行向量化，得到一个嵌入向量
        EmbeddingStoreIngestor.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .documentSplitter(
                        DocumentSplitters.recursive(300, 50) // 每300字符一段，重叠50
                )
                .build()
                .ingest(documents);
    }

}
