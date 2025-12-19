package io.livestock.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Web filter that logs incoming and outgoing requests.
 */
@Component
public class LoggingWebFilter implements WebFilter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingWebFilter.class);

    /**
     * Logs incoming and outgoing requests.
     *
     * @param exchange the current server exchange
     * @param chain    provides a way to delegate to the next filter
     * @return Mono<Void> indicating completion of the filter operation.
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long startTime = System.currentTimeMillis();
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        logger.info("Incoming Request: {} {}", method, path);

        return chain.filter(exchange)
                .doOnSuccess(v -> {
                    long duration = System.currentTimeMillis() - startTime;
                    int statusCode = exchange.getResponse().getStatusCode() != null
                            ? exchange.getResponse().getStatusCode().value()
                            : 0;
                    logger.info("Outgoing Response: {} {} Status: {} ({} ms)", method, path, statusCode, duration);
                })
                .doOnError(e -> {
                    long duration = System.currentTimeMillis() - startTime;
                    logger.error("Request Failed: {} {} Error: {} ({} ms)", method, path, e.getMessage(), duration);
                });
    }
}
