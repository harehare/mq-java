package com.github.harehare.mq;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;

import java.util.Arrays;
import java.util.List;

/**
 * JNA interface to the mq-ffi native library.
 */
interface MqLibrary extends Library {

    MqLibrary INSTANCE = Native.load("mq_ffi", MqLibrary.class);

    /**
     * C-compatible result structure.
     */
    class MqResultStruct extends Structure {
        public Pointer values;
        public long valuesLen;
        public Pointer errorMsg;

        public MqResultStruct() {
            super();
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("values", "valuesLen", "errorMsg");
        }

        public static class ByValue extends MqResultStruct implements Structure.ByValue {
        }
    }

    /**
     * C-compatible conversion options, passed by value.
     * C {@code bool} is one byte, so flags are mapped to {@code byte}.
     */
    class MqConversionOptionsStruct extends Structure implements Structure.ByValue {
        public byte extractScriptsAsCodeBlocks;
        public byte generateFrontMatter;
        public byte useTitleAsH1;
        public Pointer baseUrl;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("extractScriptsAsCodeBlocks", "generateFrontMatter", "useTitleAsH1", "baseUrl");
        }
    }

    Pointer mq_create();

    void mq_destroy(Pointer enginePtr);

    MqResultStruct.ByValue mq_eval(Pointer enginePtr, String code, String input, String inputFormat);

    void mq_free_result(MqResultStruct.ByValue result);

    Pointer mq_html_to_markdown(String htmlInput, MqConversionOptionsStruct options, PointerByReference errorMsg);

    void mq_free_string(Pointer s);

    String mq_version();

    void mq_set_max_call_stack_depth(Pointer enginePtr, int maxCallStackDepth);

    void mq_set_search_paths(Pointer enginePtr, String[] paths, long pathsLen);

    void mq_define_string_value(Pointer enginePtr, String name, String value);

    Pointer mq_import_module(Pointer enginePtr, String moduleName);

    Pointer mq_load_module(Pointer enginePtr, String moduleName);
}
