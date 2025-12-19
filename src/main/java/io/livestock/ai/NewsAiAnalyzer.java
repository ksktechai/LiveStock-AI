package io.livestock.ai;

import io.livestock.domain.NewsItem;
import io.livestock.domain.NewsAnalysis;
import reactor.core.publisher.Mono;

/**
 * News AI Analyzer.
 */
public interface NewsAiAnalyzer {
  Mono<NewsAnalysis> analyze(NewsItem item);
}
