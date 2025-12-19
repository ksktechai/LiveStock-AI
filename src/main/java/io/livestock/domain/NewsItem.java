package io.livestock.domain;

import jakarta.validation.constraints.NotBlank;

/**
 * News Item Object.
 * @param source
 * @param headline
 * @param url
 * @param content
 */
public record NewsItem(
    @NotBlank String source,
    @NotBlank String headline,
    String url,
    @NotBlank String content
) {}
