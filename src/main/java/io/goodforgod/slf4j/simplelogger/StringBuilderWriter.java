package io.goodforgod.slf4j.simplelogger;

import java.io.Writer;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 19.02.2022
 */
final class StringBuilderWriter extends Writer {

    private final StringBuilder builder;

    StringBuilderWriter(StringBuilder builder) {
        this.builder = builder;
    }

    /**
     * Write a single character.
     */
    @Override
    public void write(int c) {
        builder.append((char) c);
    }

    /**
     * Write a portion of an array of characters.
     *
     * @param cbuf Array of characters
     * @param off  Offset from which to start writing characters
     * @param len  Number of characters to write
     */
    @Override
    public void write(char cbuf[], int off, int len) {
        if ((off < 0) || (off > cbuf.length)
                || (len < 0)
                || ((off + len) > cbuf.length)
                || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }

        builder.append(cbuf, off, len);
    }

    /**
     * Write a string.
     */
    @Override
    public void write(String str) {
        builder.append(str);
    }

    /**
     * Write a portion of a string.
     *
     * @param str String to be written
     * @param off Offset from which to start writing characters
     * @param len Number of characters to write
     */
    @Override
    public void write(String str, int off, int len) {
        builder.append(str, off, off + len);
    }

    /**
     * Appends the specified character sequence to this writer.
     *
     * @param csq The character sequence to append. If {@code csq} is {@code null}, then the four
     *            characters {@code "null"} are appended to this writer.
     * @return self
     */
    @Override
    public StringBuilderWriter append(CharSequence csq) {
        builder.append(csq);
        return this;
    }

    /**
     * Appends a subsequence of the specified character sequence to this writer.
     *
     * @param csq   The character sequence from which a subsequence will be appended. If {@code csq} is
     *              {@code null}, then characters
     *              will be appended as if {@code csq} contained the four characters {@code "null"}.
     * @param start The index of the first character in the subsequence
     * @param end   The index of the character following the last character in the subsequence
     * @return self
     */
    @Override
    public StringBuilderWriter append(CharSequence csq, int start, int end) {
        if (csq == null)
            csq = "null";

        return append(csq.subSequence(start, end));
    }

    @Override
    public StringBuilderWriter append(char c) {
        builder.append(c);
        return this;
    }

    /**
     * Return the buffer's current value as a string.
     */
    public String toString() {
        return builder.toString();
    }

    public void flush() {
        // do nothing
    }

    public void close() {
        // do nothing
    }
}
