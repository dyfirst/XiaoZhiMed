package com.example.xiaozhimed;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class EmbeddingTest {

    private static final String KNOWLEDGE_BASE_DIR = "D:/Java_projects/xiaozhiMed/src/main/resources/knowledge_base";

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Test
    public void testEmbeddingModel() {
        Response<Embedding> embed = embeddingModel.embed("你好");
        System.out.println("向量维度: " + embed.content().dimension());
    }

    @Test
    public void testEmbeddingQuery() {
        Embedding queryEmbedding = embeddingModel.embed("头疼挂什么科").content();

        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(5)
                .minScore(0.5)
                .build();

        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);

        System.out.println("查询: 头疼挂什么科");
        System.out.println("结果数: " + searchResult.matches().size());
        for (EmbeddingMatch<TextSegment> match : searchResult.matches()) {
            System.out.println("分数: " + match.score());
            System.out.println("内容: " + match.embedded().text().substring(0, Math.min(100, match.embedded().text().length())));
            System.out.println("---");
        }
    }

    /**
     * 上传知识库到向量数据库
     *
     * 逻辑说明：
     * 1. 扫描 knowledge_base/ 目录下所有 .md 文件
     * 2. 每个文件 = 1个Document = 1个TextSegment = 1个向量
     * 3. 不做切分，因为每个文件已经是原子语义单元：
     *    - hospital.md     ~500字，完整医院信息
     *    - departments/*.md ~300字，完整科室简介
     *    - doctors/*.md     ~200字，完整医生信息
     * 4. 逐个 embed + add 存入 Pinecone
     */
    @Test
    public void testUploadKnowledgeBase() {
        List<File> mdFiles = new ArrayList<>();
        collectMdFiles(new File(KNOWLEDGE_BASE_DIR), mdFiles);

        System.out.println("扫描到 " + mdFiles.size() + " 个知识文档");

        int success = 0;
        for (File file : mdFiles) {
            try {
                // 加载文档
                Document document = FileSystemDocumentLoader.loadDocument(file.getAbsolutePath());

                // 跳过空文档
                if (document.text().trim().isEmpty()) {
                    System.out.println("跳过空文件: " + file.getName());
                    continue;
                }

                // 向量化
                TextSegment segment = TextSegment.from(document.text());
                Embedding embedding = embeddingModel.embed(segment).content();

                // 存入向量库
                embeddingStore.add(embedding, segment);

                success++;
                String relativePath = file.getAbsolutePath().replace(KNOWLEDGE_BASE_DIR + File.separator, "");
                System.out.println("上传成功 [" + success + "]: " + relativePath
                        + " (" + document.text().length() + "字)");

            } catch (Exception e) {
                System.out.println("上传失败: " + file.getName() + " - " + e.getMessage());
            }
        }

        System.out.println("\n上传完成: 成功 " + success + "/" + mdFiles.size());
    }

    /**
     * 批量测试 RAG 召回效果
     * 模拟 5 个典型用户问题，验证向量检索是否能召回正确内容
     */
    @Test
    public void testBatchQuery() {
        String[] queries = {
                "华西医院地址在哪",
                "头疼挂什么科",
                "陈永平医生擅长什么",
                "骨科哪个医生好",
                "心内科能看什么病"
        };

        System.out.println("=" .repeat(60));
        System.out.println("RAG 召回批量测试");
        System.out.println("=".repeat(60));

        for (int i = 0; i < queries.length; i++) {
            System.out.println("\n【测试 " + (i + 1) + "】查询: " + queries[i]);
            System.out.println("-".repeat(40));

            Embedding queryEmbedding = embeddingModel.embed(queries[i]).content();

            EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(3)
                    .minScore(0.4)
                    .build();

            EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);

            if (result.matches().isEmpty()) {
                System.out.println("  ❌ 未召回任何结果");
            } else {
                for (int j = 0; j < result.matches().size(); j++) {
                    EmbeddingMatch<TextSegment> match = result.matches().get(j);
                    String content = match.embedded().text();
                    // 截取前80个字符作为预览
                    String preview = content.length() > 80
                            ? content.substring(0, 80).replace("\n", " ") + "..."
                            : content.replace("\n", " ");

                    String rank = j == 0 ? "✅ Top1" : "   Top" + (j + 1);
                    System.out.println("  " + rank + " [分数: " + String.format("%.4f", match.score()) + "] " + preview);
                }
            }
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("测试完成");
    }

    /**
     * 测试不同 minScore 的召回质量
     */
    @Test
    public void testMinScore() {
        String[] queries = {
                "华西医院地址在哪",
                "头疼挂什么科",
                "陈永平医生擅长什么",
                "骨科哪个医生好",
                "心内科能看什么病"
        };

        double[] minScoreOptions = {0.5, 0.6, 0.65, 0.7};

        System.out.println("=".repeat(70));
        System.out.println("minScore 召回质量测试 (maxResults=3)");
        System.out.println("=".repeat(70));

        for (double minScore : minScoreOptions) {
            int totalHits = 0;
            int emptyQueries = 0;

            System.out.println("\n【minScore=" + minScore + "】");
            System.out.println("-".repeat(50));

            for (String query : queries) {
                Embedding queryEmbedding = embeddingModel.embed(query).content();
                EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .maxResults(3)
                        .minScore(minScore)
                        .build();
                EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);

                int hits = result.matches().size();
                totalHits += hits;

                if (hits == 0) {
                    emptyQueries++;
                    System.out.println("  ❌ \"" + query + "\" → 无结果");
                } else {
                    String top1Preview = result.matches().get(0).embedded().text().split("\n")[0];
                    if (top1Preview.length() > 25) top1Preview = top1Preview.substring(0, 25) + "...";

                    System.out.println("  ✅ \"" + query + "\" → " + hits + "条, Top1["
                            + String.format("%.3f", result.matches().get(0).score()) + "] " + top1Preview);
                }
            }

            double avgHits = (double) totalHits / queries.length;
            System.out.println("  汇总: 平均召回=" + String.format("%.1f", avgHits) + ", 空查询=" + emptyQueries);
        }

        System.out.println("\n" + "=".repeat(70));
    }

    /**
     * 测试不同 maxResults 的响应时间
     */
    @Test
    public void testPerformance() {
        String[] queries = {
                "华西医院地址在哪",
                "头疼挂什么科",
                "陈永平医生擅长什么",
                "骨科哪个医生好",
                "心内科能看什么病"
        };

        int[] maxResultsOptions = {3, 5, 7};

        System.out.println("=".repeat(60));
        System.out.println("RAG 响应时间测试");
        System.out.println("=".repeat(60));

        for (int maxResults : maxResultsOptions) {
            long totalTime = 0;

            for (String query : queries) {
                long start = System.currentTimeMillis();

                Embedding queryEmbedding = embeddingModel.embed(query).content();
                EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .maxResults(maxResults)
                        .minScore(0.5)
                        .build();
                embeddingStore.search(request);

                long cost = System.currentTimeMillis() - start;
                totalTime += cost;
            }

            double avgTime = (double) totalTime / queries.length;
            System.out.println("maxResults=" + maxResults
                    + " | 总耗时=" + totalTime + "ms"
                    + " | 平均=" + String.format("%.1f", avgTime) + "ms/次");
        }

        System.out.println("=".repeat(60));
    }

    /**
     * 递归收集目录下所有 .md 文件
     */
    private void collectMdFiles(File dir, List<File> result) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                collectMdFiles(file, result);
            } else if (file.getName().endsWith(".md")) {
                result.add(file);
            }
        }
    }
}
