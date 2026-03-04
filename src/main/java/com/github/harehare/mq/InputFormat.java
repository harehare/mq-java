package com.github.harehare.mq;

/**
 * Input format for mq query processing.
 */
public enum InputFormat {
    /** Standard Markdown (CommonMark/GFM) */
    MARKDOWN("markdown"),
    /** Markdown with JSX support */
    MDX("mdx"),
    /** Plain text (split by lines) */
    TEXT("text"),
    /** HTML content (auto-converted to Markdown) */
    HTML("html"),
    /** Raw input format (no parsing, returns original content) */
    RAW("RAW");

    private final String value;

    InputFormat(String value) {
        this.value = value;
    }

    /**
     * Returns the string value used by the FFI layer.
     *
     * @return the format string
     */
    public String getValue() {
        return value;
    }
}
