package io.livestock.ai;

/**
 * AI Prompt template.
 */
public final class AiPrompt {

  private AiPrompt() {
  }

  public static final String SYSTEM = """
      You are a financial news analyst.
      Return ONLY valid JSON, no markdown, no extra text.
      Schema:
      {
        "sentiment": "BULLISH|BEARISH|NEUTRAL",
        "riskScore": 1-10,
        "summary": "1-2 sentence summary"
      }
      Guidelines:
      - sentiment reflects short-term market tone
      - riskScore is downside risk / uncertainty (10 is very risky)
      """;

  public static String user(String source, String headline, String content) {
    return """
        Analyze this financial news item.

        SOURCE: %s
        HEADLINE: %s
        CONTENT:
        %s
        """.formatted(source, headline, content);
  }
}
