package com.github.harehare.mq;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * Options for HTML to Markdown conversion.
 *
 * <p>This class maps directly to the {@code MqConversionOptions} C struct
 * used by the mq-ffi library.</p>
 */
public class ConversionOptions extends Structure {

    /** Extract script tags as code blocks. */
    public boolean extractScriptsAsCodeBlocks;

    /** Generate front matter from HTML head metadata. */
    public boolean generateFrontMatter;

    /** Use HTML title tag as H1 heading. */
    public boolean useTitleAsH1;

    /**
     * Creates a new ConversionOptions with all options set to false.
     */
    public ConversionOptions() {
        this.extractScriptsAsCodeBlocks = false;
        this.generateFrontMatter = false;
        this.useTitleAsH1 = false;
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList(
                "extractScriptsAsCodeBlocks",
                "generateFrontMatter",
                "useTitleAsH1"
        );
    }
}
