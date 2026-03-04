package com.github.harehare.mq;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Java bindings for mq - a jq-like tool for Markdown processing.
 *
 * <p>This class wraps the mq-ffi native library, providing a safe, idiomatic Java API
 * for querying and transforming Markdown, MDX, HTML, and text content.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * try (Mq mq = new Mq()) {
 *     MqResult result = mq.run(".h1", "# Hello World\n\nText");
 *     System.out.println(result.text()); // "# Hello World"
 * }
 * }</pre>
 */
public class Mq implements AutoCloseable {

    private Pointer engine;

    /**
     * Creates a new mq engine instance.
     *
     * @throws MqException if the engine could not be created
     */
    public Mq() {
        this.engine = MqLibrary.INSTANCE.mq_create();
        if (this.engine == null) {
            throw new MqException("Failed to create mq engine");
        }
    }

    /**
     * Executes an mq query on the provided content using Markdown format.
     *
     * @param code    the mq query string
     * @param content the content to process
     * @return the query result
     * @throws MqException if the query fails
     */
    public MqResult run(String code, String content) {
        return run(code, content, InputFormat.MARKDOWN);
    }

    /**
     * Executes an mq query on the provided content.
     *
     * @param code        the mq query string
     * @param content     the content to process
     * @param inputFormat the input format
     * @return the query result
     * @throws MqException if the query fails
     */
    public MqResult run(String code, String content, InputFormat inputFormat) {
        ensureOpen();
        MqLibrary.MqResultStruct.ByValue result = MqLibrary.INSTANCE.mq_eval(
                engine, code, content, inputFormat.getValue()
        );
        try {
            if (result.errorMsg != null && result.errorMsg != Pointer.NULL) {
                String error = result.errorMsg.getString(0);
                throw new MqException(error);
            }
            String[] values = new String[(int) result.valuesLen];
            if (result.values != null && result.valuesLen > 0) {
                Pointer[] pointers = result.values.getPointerArray(0, (int) result.valuesLen);
                for (int i = 0; i < pointers.length; i++) {
                    values[i] = pointers[i] != null ? pointers[i].getString(0) : "";
                }
            }
            return new MqResult(values);
        } finally {
            MqLibrary.INSTANCE.mq_free_result(result);
        }
    }

    /**
     * Converts HTML content to Markdown.
     *
     * @param htmlContent the HTML content to convert
     * @return the converted Markdown string
     * @throws MqException if the conversion fails
     */
    public static String htmlToMarkdown(String htmlContent) {
        return htmlToMarkdown(htmlContent, new ConversionOptions());
    }

    /**
     * Converts HTML content to Markdown with the specified options.
     *
     * @param htmlContent the HTML content to convert
     * @param options     the conversion options
     * @return the converted Markdown string
     * @throws MqException if the conversion fails
     */
    public static String htmlToMarkdown(String htmlContent, ConversionOptions options) {
        PointerByReference errorMsgRef = new PointerByReference();
        Pointer resultPtr = MqLibrary.INSTANCE.mq_html_to_markdown(
                htmlContent, options, errorMsgRef
        );
        if (resultPtr == null || resultPtr == Pointer.NULL) {
            Pointer errorPtr = errorMsgRef.getValue();
            String error = errorPtr != null ? errorPtr.getString(0) : "Unknown error";
            if (errorPtr != null) {
                MqLibrary.INSTANCE.mq_free_string(errorPtr);
            }
            throw new MqException(error);
        }
        try {
            return resultPtr.getString(0);
        } finally {
            MqLibrary.INSTANCE.mq_free_string(resultPtr);
        }
    }

    /**
     * Closes the mq engine and releases native resources.
     */
    @Override
    public void close() {
        if (engine != null) {
            MqLibrary.INSTANCE.mq_destroy(engine);
            engine = null;
        }
    }

    private void ensureOpen() {
        if (engine == null) {
            throw new MqException("Mq engine has been closed");
        }
    }
}
