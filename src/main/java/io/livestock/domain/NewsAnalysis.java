package io.livestock.domain;

import java.time.Instant;

/**
 * News Analysis Object.
 *
 * @param id
 * @param timestamp
 * @param source
 * @param headline
 * @param url
 * @param sentiment
 * @param riskScore
 * @param summary
 */
public record NewsAnalysis(
        String id,
        Instant timestamp,
        String source,
        String headline,
        String url,
        Sentiment sentiment,
        int riskScore,
        String summary
) {
}
