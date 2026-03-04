package com.github.harehare.mq;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Result of an mq query evaluation.
 *
 * <p>Contains the list of string values produced by the query.</p>
 */
public class MqResult implements Iterable<String> {

    private final List<String> values;

    MqResult(String[] values) {
        this.values = values != null
                ? Collections.unmodifiableList(Arrays.asList(values))
                : Collections.emptyList();
    }

    /**
     * Returns all result values joined by newlines.
     *
     * @return the text representation
     */
    public String text() {
        return values.stream()
                .filter(v -> v != null && !v.isEmpty())
                .collect(Collectors.joining("\n"));
    }

    /**
     * Returns all non-empty result values as a list.
     *
     * @return the list of values
     */
    public List<String> values() {
        return values.stream()
                .filter(v -> v != null && !v.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Returns the total number of result values.
     *
     * @return the count
     */
    public int length() {
        return values.size();
    }

    /**
     * Returns the value at the specified index.
     *
     * @param index the zero-based index
     * @return the value at the index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public String get(int index) {
        return values.get(index);
    }

    @Override
    public Iterator<String> iterator() {
        return values.iterator();
    }
}
