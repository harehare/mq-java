package com.github.harehare.mq;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MqTest {

    private Mq mq;

    @BeforeEach
    void setUp() {
        mq = new Mq();
    }

    @AfterEach
    void tearDown() {
        mq.close();
    }

    @Nested
    class Run {

        @Test
        void extractsH1Headings() {
            String content = "# Hello World\n\n## Heading2\n\nText";
            MqResult result = mq.run(".h1", content);
            assertEquals(List.of("# Hello World"), result.values());
        }

        @Test
        void extractsH2Headings() {
            String content = "# Hello World\n\n## Heading2\n\nText";
            MqResult result = mq.run(".h2", content);
            assertEquals(List.of("## Heading2"), result.values());
        }

        @Test
        void extractsMultipleH2Headings() {
            String content = "# Main Title\n\n## Heading2A\n\nText\n\n## Heading2B\n\nMore text";
            MqResult result = mq.run(".h2", content);
            assertEquals(List.of("## Heading2A", "## Heading2B"), result.values());
        }

        @Test
        void filtersHeadingsWithSelect() {
            String content = "# Product\n\n## Features\n\nText\n\n## Installation\n\nMore text";
            MqResult result = mq.run(".h2 | select(contains(\"Feature\"))", content);
            assertEquals(List.of("## Features"), result.values());
        }

        @Test
        void extractsListItems() {
            String content = "# List\n\n- Item 1\n- Item 2\n- Item 3";
            MqResult result = mq.run(".[]", content);
            assertEquals(List.of("- Item 1", "- Item 2", "- Item 3"), result.values());
        }

        @Test
        void extractsCodeBlocks() {
            String content = "# Code\n\n```python\nprint('Hello')\n```";
            MqResult result = mq.run(".code", content);
            assertEquals(List.of("```python\nprint('Hello')\n```"), result.values());
        }
    }

    @Nested
    class InputFormats {

        @Test
        void processesTextFormat() {
            String content = "Line 1\nLine 2\nLine 3";
            MqResult result = mq.run("select(contains(\"2\"))", content, InputFormat.TEXT);
            assertEquals(List.of("Line 2"), result.values());
        }

        @Test
        void processesMdxFormat() {
            String content = "# MDX Content\n\n<Component />";
            MqResult result = mq.run("select(is_mdx())", content, InputFormat.MDX);
            assertEquals(List.of("<Component />"), result.values());
        }

        @Test
        void processesHtmlFormat() {
            String content = "<h1>Hello</h1><p>World</p>";
            MqResult result = mq.run("select(contains(\"Hello\"))", content, InputFormat.HTML);
            assertEquals(List.of("# Hello"), result.values());
        }
    }

    @Nested
    class ErrorHandling {

        @Test
        void throwsExceptionForInvalidSyntax() {
            assertThrows(MqException.class, () -> {
                mq.run(".invalid_selector!!!", "# Heading");
            });
        }

        @Test
        void throwsExceptionWhenClosed() {
            mq.close();
            assertThrows(MqException.class, () -> {
                mq.run(".h1", "# Heading");
            });
        }
    }

    @Nested
    class HtmlToMarkdown {

        @Test
        void convertsHtmlToMarkdown() {
            String html = "<h1>Hello World</h1><p>This is a <strong>test</strong>.</p>";
            String markdown = Mq.htmlToMarkdown(html);
            assertTrue(markdown.contains("# Hello World"));
            assertTrue(markdown.contains("**test**"));
        }

        @Test
        void convertsHtmlWithOptions() {
            String html = "<html><head><title>Page Title</title></head><body><h1>Content</h1></body></html>";
            ConversionOptions options = new ConversionOptions();
            options.useTitleAsH1 = true;
            String markdown = Mq.htmlToMarkdown(html, options);
            assertTrue(markdown.contains("# Page Title"));
        }
    }

    @Nested
    class MqResultTest {

        @Test
        void textJoinsValues() {
            String content = "# Title\n\n## Section 1\n\n## Section 2";
            MqResult result = mq.run(".h2", content);
            assertEquals("## Section 1\n## Section 2", result.text());
        }

        @Test
        void valuesReturnsNonEmptyValues() {
            String content = "# Title\n\n## Section 1\n\n## Section 2";
            MqResult result = mq.run(".h2", content);
            assertEquals(List.of("## Section 1", "## Section 2"), result.values());
        }

        @Test
        void lengthReturnsCount() {
            String content = "# Title\n\n## Section 1\n\n## Section 2";
            MqResult result = mq.run(".h2", content);
            assertTrue(result.length() > 0);
        }

        @Test
        void getAccessesByIndex() {
            String content = "# Title\n\n## Section 1\n\n## Section 2";
            MqResult result = mq.run(".h2", content);
            List<String> values = result.values();
            assertEquals("## Section 1", values.get(0));
            assertEquals("## Section 2", values.get(1));
        }

        @Test
        void iteratorWorks() {
            String content = "# Title\n\n## Section 1\n\n## Section 2";
            MqResult result = mq.run(".h2", content);
            List<String> collected = new ArrayList<>();
            for (String value : result) {
                collected.add(value);
            }
            assertTrue(collected.size() > 0);
        }
    }
}
