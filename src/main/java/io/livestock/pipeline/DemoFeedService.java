package io.livestock.pipeline;

import io.livestock.domain.NewsItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Service
@ConditionalOnProperty(prefix = "livestock.demo", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DemoFeedService {

  private final NewsPipeline pipeline;
  private final boolean enabledByDefault;
  private final Duration interval;

  private final AtomicReference<Disposable> running = new AtomicReference<>();

  private final List<NewsItem> samples = List.of(
      new NewsItem("Reuters", "Tech giant beats earnings expectations", "https://example.com/a",
          "The company reported quarterly earnings above analyst estimates and raised guidance."),
      new NewsItem("Bloomberg", "Bank faces lawsuit over disclosures", "https://example.com/b",
          "A new lawsuit alleges the bank misled investors. Shares fell in early trading."),
      new NewsItem("WSJ", "Oil prices surge amid supply concerns", "https://example.com/c",
          "Crude oil rose sharply as traders reacted to possible supply disruptions."),
      new NewsItem("CNBC", "Regulator signals possible rate cuts", "https://example.com/d",
          "Comments hinted at easing policy, lifting equity futures."));

  public DemoFeedService(
      NewsPipeline pipeline,
      @Value("${livestock.demo.enabled:true}") boolean enabledByDefault,
      @Value("${livestock.demo.interval:3s}") Duration interval) {
    this.pipeline = pipeline;
    this.enabledByDefault = enabledByDefault;
    this.interval = interval;

    if (this.enabledByDefault) {
      start();
    }
  }

  public synchronized void start() {
    if (running.get() != null && !running.get().isDisposed())
      return;

    Disposable d = Flux.interval(interval)
        .map(i -> samples.get((int) (i % samples.size())))
        .subscribe(pipeline::ingest);

    running.set(d);
  }

  public synchronized void stop() {
    Disposable d = running.getAndSet(null);
    if (d != null)
      d.dispose();
  }

  public boolean isRunning() {
    Disposable d = running.get();
    return d != null && !d.isDisposed();
  }
}
