package io.goodforgod.slf4j.simplelogger;

import java.io.*;
import java.lang.reflect.Field;

/**
 * Wraps passed output stream to get access for its internals
 *
 * @author Anton Kurako (GoodforGod)
 * @since 02.04.2022
 */
final class SimplePrintStream extends PrintStream {

    SimplePrintStream(PrintStream out) {
        super(getParentStream(out));
    }

    @Override
    public void write(byte[] buf) throws IOException {
        try {
            out.write(buf);
        } catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
    }

    private static OutputStream getParentStream(PrintStream printStream) {
        try {
            final Field field = printStream.getClass().getSuperclass().getDeclaredField("out");
            field.setAccessible(true);
            return (OutputStream) field.get(printStream);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }
}
