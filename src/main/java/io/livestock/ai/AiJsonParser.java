package io.livestock.ai;

import io.livestock.domain.Sentiment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses AI JSON response into NewsAnalysis object.
 */
public final class AiJsonParser {
    private AiJsonParser() {
    }

    private static final Pattern SENTIMENT = Pattern.compile("\"sentiment\"\\s*:\\s*\"(BULLISH|BEARISH|NEUTRAL)\"",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern RISK = Pattern.compile("\"riskScore\"\\s*:\\s*(\\d{1,2})");
    private static final Pattern SUMMARY = Pattern.compile("\"summary\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL);

    /**
     * Parses AI JSON response into NewsAnalysis object.
     * Parses raw input; returns sentiment, risk, summary
     *
     * @param raw The raw JSON response from AI.
     * @return A Parsed object containing sentiment, risk score, and summary.
     */
    public static Parsed parse(String raw) {
        String response = raw == null ? "" : raw.trim();
        Sentiment sentiment = matchSentiment(response);
        int risk = matchInt(response, RISK, 5);
        String summary = matchString(response, SUMMARY, "Summary unavailable.");
        if (risk < 1)
            risk = 1;
        if (risk > 10)
            risk = 10;
        return new Parsed(sentiment, risk, summary);
    }

    /**
     * Matches sentiment from JSON response.
     *
     * @param sentiment The JSON response string.
     * @return The matched sentiment or NEUTRAL if not found.
     */
    private static Sentiment matchSentiment(String sentiment) {
        Matcher m = SENTIMENT.matcher(sentiment);
        if (m.find()) {
            return Sentiment.valueOf(m.group(1).toUpperCase());
        }
        return Sentiment.NEUTRAL;
    }

    /**
     * Matches integer from JSON response.
     *
     * @param sentiment The JSON response string.
     * @param pattern   The regex pattern to match.
     * @param fallback  The fallback value if no match is found.
     * @return The matched integer or fallback if not found.
     */
    private static int matchInt(String sentiment, Pattern pattern, int fallback) {
        Matcher m = pattern.matcher(sentiment);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (Exception ignored) {
            }
        }
        return fallback;
    }

    /**
     * Matches string from JSON response.
     *
     * @param response The JSON response string.
     * @param pattern  The regex pattern to match.
     * @param fallback The fallback value if no match is found.
     * @return The matched string or fallback if not found.
     */
    private static String matchString(String response, Pattern pattern, String fallback) {
        Matcher m = pattern.matcher(response);
        if (m.find()) {
            return unescape(m.group(1)).trim();
        }
        return fallback;
    }

    /**
     * Unescape JSON string.
     *
     * @param in The input string to unescape.
     * @return The unescaped string.
     */
    private static String unescape(String in) {
        return in.replace("\\\\n", "\n")
                .replace("\\\\\"", "\"")
                .replace("\\\\t", "\t")
                .replace("\\\\r", "\r");
    }

    /**
     * Parsed AI response into NewsAnalysis object.
     *
     * @param sentiment The sentiment from AI response.
     * @param riskScore The risk score from AI response.
     * @param summary   The summary from AI response.
     */
    public record Parsed(Sentiment sentiment, int riskScore, String summary) {
    }
}
