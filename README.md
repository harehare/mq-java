<h1 align="center">mq-java</h1>

Java bindings for [mq](https://github.com/harehare/mq) — a jq-like command-line tool for Markdown processing.

## Requirements

- Java 11+
- Maven
- Rust toolchain (for building the native library)

## Setup

Clone and build the native library, then compile the Java bindings:

```bash
make build
```

Or step by step:

```bash
make setup       # Clone the mq repository
make build-rust  # Build the mq-ffi native library
mvn compile      # Compile the Java sources
```

## Running Tests

```bash
make test
```

## Usage

### Basic Query

```java
try (Mq mq = new Mq()) {
    MqResult result = mq.run(".h1", "# Hello World\n\nSome text");
    System.out.println(result.text()); // "# Hello World"
}
```

### Input Formats

```java
try (Mq mq = new Mq()) {
    // Markdown (default)
    MqResult md = mq.run(".h2", content, InputFormat.MARKDOWN);

    // MDX
    MqResult mdx = mq.run("select(is_mdx())", content, InputFormat.MDX);

    // HTML (auto-converted to Markdown)
    MqResult html = mq.run(".h1", "<h1>Title</h1>", InputFormat.HTML);

    // Plain text (split by lines)
    MqResult text = mq.run("select(contains(\"foo\"))", content, InputFormat.TEXT);
}
```

### HTML to Markdown Conversion

```java
String markdown = Mq.htmlToMarkdown("<h1>Hello</h1><p>World</p>");
```

With options:

```java
ConversionOptions options = new ConversionOptions();
options.useTitleAsH1 = true;
options.generateFrontMatter = true;
options.extractScriptsAsCodeBlocks = true;

String markdown = Mq.htmlToMarkdown(html, options);
```

### Working with Results

```java
MqResult result = mq.run(".h2", content);

// Get all values joined by newline
String text = result.text();

// Get as a list
List<String> values = result.values();

// Access by index
String first = result.get(0);

// Iterate
for (String value : result) {
    System.out.println(value);
}
```

## API

### `Mq`

| Method | Description |
|---|---|
| `run(String code, String content)` | Run a query on Markdown content |
| `run(String code, String content, InputFormat format)` | Run a query with a specified input format |
| `static htmlToMarkdown(String html)` | Convert HTML to Markdown |
| `static htmlToMarkdown(String html, ConversionOptions options)` | Convert HTML to Markdown with options |
| `static version()` | Return the mq-ffi library version |
| `setMaxCallStackDepth(int depth)` | Limit call stack depth |
| `setSearchPaths(List<String> paths)` | Set module search paths |
| `defineStringValue(String name, String value)` | Define a string variable for later queries |
| `importModule(String name)` / `loadModule(String name)` | Import / load a module |
|| `close()` | Release native resources (`AutoCloseable`) |

### `InputFormat`

| Value | Description |
|---|---|
| `MARKDOWN` | CommonMark / GFM (default) |
| `MDX` | Markdown with JSX |
| `HTML` | HTML (auto-converted to Markdown) |
| `TEXT` | Plain text, split by lines |
| `RAW` | Raw input, no parsing |

### `ConversionOptions`

| Field | Type | Description |
|---|---|---|
| `extractScriptsAsCodeBlocks` | `boolean` | Extract `<script>` tags as code blocks |
| `generateFrontMatter` | `boolean` | Generate front matter from HTML `<head>` metadata |
| `useTitleAsH1` | `boolean` | Use `<title>` as an H1 heading |
| `baseUrl` | `String` | Base URL for resolving relative `href`/`src` (falls back to `<base href>`) |

## License

[MIT](LICENSE)
