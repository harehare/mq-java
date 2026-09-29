package com.github.harehare.mq;

/**
 * Options for HTML to Markdown conversion.
 *
 * <p>These map to the {@code MqConversionOptions} C struct used by the mq-ffi library.</p>
 */
public class ConversionOptions {

    /** Extract script tags as code blocks. */
    public boolean extractScriptsAsCodeBlocks;

    /** Generate front matter from HTML head metadata. */
    public boolean generateFrontMatter;

    /** Use HTML title tag as H1 heading. */
    public boolean useTitleAsH1;

    /** Base URL for resolving relative {@code href}/{@code src} values; {@code null} falls back to {@code <base href>}. */
    public String baseUrl;

    /**
     * Creates a new ConversionOptions with all options disabled.
     */
    public ConversionOptions() {
    }
}
