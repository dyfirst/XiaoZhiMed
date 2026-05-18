package com.example.xiaozhimed;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class RAGTest {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    /**
     * 测试不同 maxResults + minScore 组合的召回效果
     * 找到最稳定的参数
     */
    @Test
    public void testParameterTuning() {
        String[] queries = {
                "华西医院地址在哪",
                "头疼挂什么科",
                "陈永平医生擅长什么",
                "骨科哪个医生好",
                "心内科能看什么病",
                "神经外科有哪些医生",
                "急诊科怎么走",
                "消化内科厉害吗"
        };

        int[] maxResultsOptions = {3, 5, 7};
        double[] minScoreOptions = {0.3, 0.4, 0.5, 0.6, 0.7};

        System.out.println("=".repeat(70));
        System.out.println("RAG 参数调优测试");
        System.out.println("测试查询数: " + queries.length);
        System.out.println("=".repeat(70));

        // 记录最佳组合
        int bestMaxResults = 5;
        double bestMinScore = 0.5;
        int bestTotalHits = 0;
        int bestTop1Hits = 0;

        for (int maxResults : maxResultsOptions) {
            for (double minScore : minScoreOptions) {
                int totalHits = 0;      // 总命中数
                int top1Relevant = 0;   // Top1 是否相关
                int emptyResults = 0;   // 空结果数

                System.out.println("\n【参数组合】maxResults=" + maxResults + ", minScore=" + minScore);
                System.out.println("-".repeat(70));

                for (String query : queries) {
                    Embedding queryEmbedding = embeddingModel.embed(query).content();

                    EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                            .queryEmbedding(queryEmbedding)
                            .maxResults(maxResults)
                            .minScore(minScore)
                            .build();

                    EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);
                    int hitCount = result.matches().size();

                    if (hitCount == 0) {
                        emptyResults++;
                        System.out.println("  ❌ \"" + query + "\" → 无结果");
                    } else {
                        totalHits += hitCount;

                        // 判断 Top1 是否相关（分数 > 0.6 视为相关）
                        if (result.matches().get(0).score() >= 0.6) {
                            top1Relevant++;
                        }

                        // 简化输出：只显示 Top1 的分数和内容摘要
                        String top1Preview = result.matches().get(0).embedded().text();
                        top1Preview = top1Preview.split("\n")[0]; // 取第一行
                        if (top1Preview.length() > 30) {
                            top1Preview = top1Preview.substring(0, 30) + "...";
                        }

                        System.out.println("  " + (result.matches().get(0).score() >= 0.6 ? "✅" : "⚠️")
                                + " \"" + query + "\" → Top1["
                                + String.format("%.3f", result.matches().get(0).score()) + "] "
                                + top1Preview);
                    }
                }

                // 评估
                double avgHits = (double) totalHits / queries.length;
                boolean isStable = emptyResults == 0 && top1Relevant >= queries.length - 1;

                System.out.println("\n  汇总: 平均命中=" + String.format("%.1f", avgHits)
                        + ", Top1相关=" + top1Relevant + "/" + queries.length
                        + ", 空结果=" + emptyResults
                        + (isStable ? " ✅ 稳定" : ""));

                // 更新最佳组合（优先：无空结果 + Top1相关最多 + 平均命中适中）
                if (emptyResults == 0 && top1Relevant > bestTop1Hits) {
                    bestTop1Hits = top1Relevant;
                    bestMaxResults = maxResults;
                    bestMinScore = minScore;
                    bestTotalHits = totalHits;
                }
            }
        }

        System.out.println("\n" + "=".repeat(70));
        System.out.println("🏆 最佳参数组合: maxResults=" + bestMaxResults + ", minScore=" + bestMinScore);
        System.out.println("   Top1相关率: " + bestTop1Hits + "/" + queries.length);
        System.out.println("=".repeat(70));
    }

    @Test
    public void testNeurosurgeonRecommendation() {
        String query = "神经外科有哪些医生推荐的？";

        System.out.println("=".repeat(60));
        System.out.println("用户提问: " + query);
        System.out.println("=".repeat(60));

        Embedding queryEmbedding = embeddingModel.embed(query).content();

        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(5)
                .minScore(0.4)
                .build();

        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);

        System.out.println("\n召回结果数: " + result.matches().size());
        System.out.println("-".repeat(60));

        for (int i = 0; i < result.matches().size(); i++) {
            EmbeddingMatch<TextSegment> match = result.matches().get(i);
            String content = match.embedded().text();

            System.out.println("\n【Top" + (i + 1) + "】分数: " + String.format("%.4f", match.score()));
            System.out.println("内容:");
            System.out.println(content);
            System.out.println("-".repeat(60));
        }

        System.out.println("\n测试完成");
    }

    /**
     * 用带标签样本评估 minScore：
     * 1. 正例：应该召回到目标知识
     * 2. 反例：不应该召回静态知识
     *
     * 这样可以更直观看到 minScore 过低时的误召回，以及过高时的漏召回。
     */
    @Test
    public void testMinScoreWithLabeledCases() {
        List<RagEvalCase> cases = List.of(
                new RagEvalCase("华西医院地址在哪", true, List.of("国学巷37号", "华西坝院区")),
                new RagEvalCase("头疼挂什么科", true, List.of("神经内科")),
                new RagEvalCase("陈永平医生擅长什么", true, List.of("帕金森病", "运动神经元病")),
                new RagEvalCase("神经外科有哪些医生", true, List.of("神经外科", "马潞", "蔡博文")),
                new RagEvalCase("急诊科怎么走", true, List.of("急诊", "24小时")),
                new RagEvalCase("帮我查询我目前有哪些预约", false, List.of()),
                new RagEvalCase("你好", false, List.of()),
                new RagEvalCase("谢谢", false, List.of())
        );

        double[] minScoreOptions = {0.55, 0.60, 0.65, 0.70, 0.75, 0.80};
        int maxResults = 3;

        System.out.println("=".repeat(80));
        System.out.println("minScore 带标签评估 (maxResults=" + maxResults + ")");
        System.out.println("正例目标：命中目标知识；反例目标：不召回静态知识");
        System.out.println("=".repeat(80));

        double bestScore = -1;
        double bestMinScore = 0.65;

        for (double minScore : minScoreOptions) {
            int positiveCount = 0;
            int positiveHit = 0;
            int top1Hit = 0;
            int negativeCount = 0;
            int negativeClean = 0;

            System.out.println("\n【minScore=" + minScore + "】");
            System.out.println("-".repeat(80));

            for (RagEvalCase evalCase : cases) {
                Embedding queryEmbedding = embeddingModel.embed(evalCase.query()).content();
                EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .maxResults(maxResults)
                        .minScore(minScore)
                        .build();

                EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);
                List<EmbeddingMatch<TextSegment>> matches = result.matches();

                if (evalCase.shouldRetrieve()) {
                    positiveCount++;
                    boolean anyHit = matches.stream()
                            .map(match -> match.embedded().text())
                            .anyMatch(text -> containsAny(text, evalCase.expectedKeywords()));
                    boolean top1Matched = !matches.isEmpty()
                            && containsAny(matches.get(0).embedded().text(), evalCase.expectedKeywords());

                    if (anyHit) {
                        positiveHit++;
                    }
                    if (top1Matched) {
                        top1Hit++;
                    }

                    String status = top1Matched ? "✅" : anyHit ? "⚠️" : "❌";
                    String scoreText = matches.isEmpty() ? "无结果"
                            : String.format("Top1=%.3f", matches.get(0).score());
                    System.out.println("  " + status + " 正例 \"" + evalCase.query() + "\" -> " + scoreText);
                } else {
                    negativeCount++;
                    boolean clean = matches.isEmpty();
                    if (clean) {
                        negativeClean++;
                    }

                    String status = clean ? "✅" : "❌";
                    String scoreText = matches.isEmpty() ? "无结果"
                            : String.format("误召回Top1=%.3f", matches.get(0).score());
                    System.out.println("  " + status + " 反例 \"" + evalCase.query() + "\" -> " + scoreText);
                }
            }

            double positiveRecall = positiveCount == 0 ? 0 : (double) positiveHit / positiveCount;
            double top1Accuracy = positiveCount == 0 ? 0 : (double) top1Hit / positiveCount;
            double negativePrecision = negativeCount == 0 ? 0 : (double) negativeClean / negativeCount;
            double weightedScore = top1Accuracy * 0.5 + positiveRecall * 0.3 + negativePrecision * 0.2;

            System.out.println("  正例召回率: " + positiveHit + "/" + positiveCount
                    + " = " + String.format("%.2f", positiveRecall));
            System.out.println("  Top1命中率: " + top1Hit + "/" + positiveCount
                    + " = " + String.format("%.2f", top1Accuracy));
            System.out.println("  反例干净率: " + negativeClean + "/" + negativeCount
                    + " = " + String.format("%.2f", negativePrecision));
            System.out.println("  综合得分: " + String.format("%.3f", weightedScore));

            if (weightedScore > bestScore) {
                bestScore = weightedScore;
                bestMinScore = minScore;
            }
        }

        System.out.println("\n" + "=".repeat(80));
        System.out.println("推荐 minScore: " + bestMinScore + "（基于当前标注样本）");
        System.out.println("说明：这个值只对当前知识库和查询分布有效，后续应随着样本集持续更新。");
        System.out.println("=".repeat(80));
    }

    private boolean containsAny(String text, List<String> keywords) {
        return keywords.stream().anyMatch(text::contains);
    }

    private record RagEvalCase(String query, boolean shouldRetrieve, List<String> expectedKeywords) {
    }
}
