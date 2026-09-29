package com.github.harehare.mq;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import java.nio.charset.StandardCharsets;
import java.util.List;

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
        MqLibrary.MqConversionOptionsStruct opts = new MqLibrary.MqConversionOptionsStruct();
        opts.extractScriptsAsCodeBlocks = (byte) (options.extractScriptsAsCodeBlocks ? 1 : 0);
        opts.generateFrontMatter = (byte) (options.generateFrontMatter ? 1 : 0);
        opts.useTitleAsH1 = (byte) (options.useTitleAsH1 ? 1 : 0);
        Memory baseUrlMem = null;
        if (options.baseUrl != null) {
            byte[] bytes = options.baseUrl.getBytes(StandardCharsets.UTF_8);
            baseUrlMem = new Memory(bytes.length + 1L);
            baseUrlMem.write(0, bytes, 0, bytes.length);
            baseUrlMem.setByte(bytes.length, (byte) 0);
            opts.baseUrl = baseUrlMem;
        }
        Pointer resultPtr = MqLibrary.INSTANCE.mq_html_to_markdown(
                htmlContent, opts, errorMsgRef
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
     * Returns the mq-ffi library version.
     *
     * @return the version string
     */
    public static String version() {
        return MqLibrary.INSTANCE.mq_version();
    }

    /**
     * Sets the maximum call stack depth, guarding against runaway recursion.
     *
     * @param depth the maximum call stack depth
     */
    public void setMaxCallStackDepth(int depth) {
        ensureOpen();
        MqLibrary.INSTANCE.mq_set_max_call_stack_depth(engine, depth);
    }

    /**
     * Sets the search paths used to resolve modules.
     *
     * @param paths the module search paths
     */
    public void setSearchPaths(List<String> paths) {
        ensureOpen();
        MqLibrary.INSTANCE.mq_set_search_paths(engine, paths.toArray(new String[0]), paths.size());
    }

    /**
     * Defines a string variable that can be referenced from subsequently evaluated mq code.
     *
     * @param name  the variable name
     * @param value the variable value
     */
    public void defineStringValue(String name, String value) {
        ensureOpen();
        MqLibrary.INSTANCE.mq_define_string_value(engine, name, value);
    }

    /**
     * Imports a module by name, searched for in the configured search paths.
     *
     * @param moduleName the module name
     * @throws MqException if the import fails
     */
    public void importModule(String moduleName) {
        ensureOpen();
        throwIfError(MqLibrary.INSTANCE.mq_import_module(engine, moduleName));
    }

    /**
     * Loads a module by name, searched for in the configured search paths.
     *
     * @param moduleName the module name
     * @throws MqException if the load fails
     */
    public void loadModule(String moduleName) {
        ensureOpen();
        throwIfError(MqLibrary.INSTANCE.mq_load_module(engine, moduleName));
    }

    private static void throwIfError(Pointer errorPtr) {
        if (errorPtr == null || errorPtr == Pointer.NULL) {
            return;
        }
        try {
            throw new MqException(errorPtr.getString(0));
        } finally {
            MqLibrary.INSTANCE.mq_free_string(errorPtr);
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
