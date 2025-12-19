package io.livestock.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggingWebFilterTest {

    private LoggingWebFilter filter;

    @Mock
    private ServerWebExchange exchange;
    @Mock
    private WebFilterChain chain;
    @Mock
    private ServerHttpRequest request;
    @Mock
    private ServerHttpResponse response;

    @BeforeEach
    void setUp() {
        filter = new LoggingWebFilter();
        when(exchange.getRequest()).thenReturn(request);
        when(request.getURI()).thenReturn(URI.create("/test/path"));
        when(request.getMethod()).thenReturn(HttpMethod.GET);
    }

    @Test
    void filterShouldLogAndProceedOnSuccess() {
        when(chain.filter(exchange)).thenReturn(Mono.empty());
        when(exchange.getResponse()).thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain).filter(exchange);
        verify(response, atLeastOnce()).getStatusCode(); // Confirms doOnSuccess accessed status
    }

    @Test
    void filterShouldLogAndProceedOnError() {
        RuntimeException ex = new RuntimeException("Test Error");
        when(chain.filter(exchange)).thenReturn(Mono.error(ex));

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyErrorMatches(e -> e.getMessage().equals("Test Error"));

        verify(chain).filter(exchange);
        // Ensure response status is NOT accessed on error (logic is in doOnSuccess)
        verify(exchange, never()).getResponse();
    }

    @Test
    void filterShouldHandleNullStatusCode() {
        when(chain.filter(exchange)).thenReturn(Mono.empty());
        when(exchange.getResponse()).thenReturn(response);
        when(response.getStatusCode()).thenReturn(null); // Simulate null status

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(response).getStatusCode();
    }
}
