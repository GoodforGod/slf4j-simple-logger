package io.goodforgod.slf4j.simplelogger;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.Util;

/**
 * Optimized MessageFormatter analog to {@link org.slf4j.helpers.MessageFormatter}
 *
 * @author Anton Kurako (GoodforGod)
 * @since 20.02.2022
 */
final class MessageFormatter {

    private static class FormatBuilder {

        private final StringBuilder builder;
        private int i = 0;
        private int j = 0;
        private int a = 0;

        private FormatBuilder(int size) {
            this.builder = new StringBuilder(size);
        }
    }

    private static final String DELIM_STR = "{}";
    private static final char ESCAPE_CHAR = '\\';

    private MessageFormatter() {}

    static FormattingTuple format(String messagePattern, Object arg) {
        if (messagePattern == null) {
            return new FormattingTuple(null, null, null);
        }

        final int j = messagePattern.lastIndexOf(DELIM_STR);
        if (j == -1) {
            return new FormattingTuple(messagePattern, null, null);
        }

        final int argumentSize = predictArgumentSize(arg);
        final FormatBuilder fb = new FormatBuilder(messagePattern.length() + argumentSize);

        fb.j = j;
        appendArgument(messagePattern, fb, arg);

        fb.builder.append(messagePattern, fb.i, messagePattern.length());
        return new FormattingTuple(fb.builder.toString(), null, null);
    }

    static FormattingTuple format(String messagePattern, Object arg1, Object arg2) {
        if (messagePattern == null) {
            return new FormattingTuple(null, null, null);
        }

        final int j2 = messagePattern.lastIndexOf(DELIM_STR);
        if (j2 == -1) {
            return new FormattingTuple(messagePattern, null, null);
        }

        final int j1 = messagePattern.lastIndexOf(DELIM_STR, j2 - 1);

        final int argumentsSize = predictArgumentSize(arg1) + predictArgumentSize(arg2);
        final FormatBuilder fb = new FormatBuilder(messagePattern.length() + argumentsSize);

        fb.j = (j1 < 0)
                ? j2
                : j1;

        appendArgument(messagePattern, fb, arg1);
        if (j1 >= 0) {
            fb.j = j2;
            if (fb.a == -1) {
                appendArgument(messagePattern, fb, arg1);
            } else {
                appendArgument(messagePattern, fb, arg2);
            }
        }

        fb.builder.append(messagePattern, fb.i, messagePattern.length());
        return new FormattingTuple(fb.builder.toString(), null, null);
    }

    static FormattingTuple formatArray(String messagePattern, Object[] argArray) {
        Throwable throwableCandidate = org.slf4j.helpers.MessageFormatter.getThrowableCandidate(argArray);
        Object[] args = argArray;
        if (throwableCandidate != null) {
            args = org.slf4j.helpers.MessageFormatter.trimmedCopy(argArray);
        }

        return formatArray(messagePattern, args, throwableCandidate);
    }

    static FormattingTuple formatArray(String messagePattern, Object[] argArray, Throwable throwable) {
        if (messagePattern == null) {
            return new FormattingTuple(null, argArray, throwable);
        } else if (argArray == null) {
            return new FormattingTuple(messagePattern);
        } else {
            int firstArg = messagePattern.indexOf(DELIM_STR);
            if (firstArg == -1) {
                return new FormattingTuple(messagePattern, argArray, throwable);
            }

            final int limit = argArray.length;
            final FormatBuilder fb = new FormatBuilder(messagePattern.length() + 50);
            fb.j = firstArg;
            while (fb.a < limit) {
                final Object arg = argArray[fb.a];
                appendArgument(messagePattern, fb, arg);
                fb.a++;

                fb.j = messagePattern.indexOf(DELIM_STR, fb.i);
                if (fb.j == -1) {
                    break;
                }
            }

            fb.builder.append(messagePattern, fb.i, messagePattern.length());
            return new FormattingTuple(fb.builder.toString(), argArray, throwable);
        }
    }

    private static void appendArgument(String messagePattern, FormatBuilder fb, Object arg) {
        if (isEscapedDelimeter(messagePattern, fb.j)) {
            if (!isDoubleEscaped(messagePattern, fb.j)) {
                --fb.a;
                fb.builder.append(messagePattern, fb.i, fb.j - 1);
                fb.builder.append('{');
                fb.i = fb.j + 1;
            } else {
                fb.builder.append(messagePattern, fb.i, fb.j - 1);
                deeplyAppendParameter(fb.builder, arg, null);
                fb.i = fb.j + 2;
            }
        } else {
            fb.builder.append(messagePattern, fb.i, fb.j);
            deeplyAppendParameter(fb.builder, arg, null);
            fb.i = fb.j + 2;
        }
    }

    private static int predictArgumentSize(Object argument) {
        if (argument == null) {
            return 5;
        } else if (argument instanceof String) {
            return ((String) argument).length() + 1;
        } else {
            return 50;
        }
    }

    private static boolean isEscapedDelimeter(String messagePattern, int delimeterStartIndex) {
        return delimeterStartIndex != 0
                && messagePattern.charAt(delimeterStartIndex - 1) == '\\';
    }

    private static boolean isDoubleEscaped(String messagePattern, int delimeterStartIndex) {
        return delimeterStartIndex >= 2
                && messagePattern.charAt(delimeterStartIndex - 2) == ESCAPE_CHAR;
    }

    private static void deeplyAppendParameter(StringBuilder builder, Object o, Map<Object[], Object> seenMap) {
        if (o == null) {
            builder.append("null");
        } else {
            if (!o.getClass().isArray()) {
                objectAppendSafe(builder, o);
                return;
            }

            builder.append('[');
            if (o instanceof boolean[]) {
                booleanArrayAppend(builder, (boolean[]) o);
            } else if (o instanceof byte[]) {
                byteArrayAppend(builder, (byte[]) o);
            } else if (o instanceof char[]) {
                charArrayAppend(builder, (char[]) o);
            } else if (o instanceof short[]) {
                shortArrayAppend(builder, (short[]) o);
            } else if (o instanceof int[]) {
                intArrayAppend(builder, (int[]) o);
            } else if (o instanceof long[]) {
                longArrayAppend(builder, (long[]) o);
            } else if (o instanceof float[]) {
                floatArrayAppend(builder, (float[]) o);
            } else if (o instanceof double[]) {
                doubleArrayAppend(builder, (double[]) o);
            } else {
                objectArrayAppend(builder, (Object[]) o, seenMap);
            }
            builder.append(']');
        }
    }

    private static void objectAppendSafe(StringBuilder builder, Object o) {
        try {
            builder.append(o.toString());
        } catch (Throwable var3) {
            Util.report("SLF4J: Failed toString() invocation on an object of type [" + o.getClass().getName() + "]", var3);
            builder.append("[FAILED toString()]");
        }
    }

    private static void objectArrayAppend(StringBuilder sbuf, Object[] a, Map<Object[], Object> seenMapPrev) {
        Map<Object[], Object> seenMap = (seenMapPrev == null)
                ? new HashMap<>(a.length + 3)
                : seenMapPrev;

        if (!seenMap.containsKey(a)) {
            seenMap.put(a, null);

            final int len = a.length;
            for (int i = 0; i < len; ++i) {
                deeplyAppendParameter(sbuf, a[i], seenMap);
                if (i != len - 1) {
                    sbuf.append(", ");
                }
            }

            seenMap.remove(a);
        } else {
            sbuf.append("...");
        }
    }

    private static void booleanArrayAppend(StringBuilder sbuf, boolean[] a) {
        int len = a.length;
        for (int i = 0; i < a.length; ++i) {
            sbuf.append(a[i]);
            if (i != len - 1) {
                sbuf.append(", ");
            }
        }
    }

    private static void byteArrayAppend(StringBuilder sbuf, byte[] a) {
        int len = a.length;
        for (int i = 0; i < len; ++i) {
            sbuf.append(a[i]);
            if (i != len - 1) {
                sbuf.append(", ");
            }
        }
    }

    private static void charArrayAppend(StringBuilder sbuf, char[] a) {
        int len = a.length;
        for (int i = 0; i < len; ++i) {
            sbuf.append(a[i]);
            if (i != len - 1) {
                sbuf.append(", ");
            }
        }
    }

    private static void shortArrayAppend(StringBuilder sbuf, short[] a) {
        int len = a.length;
        for (int i = 0; i < len; ++i) {
            sbuf.append(a[i]);
            if (i != len - 1) {
                sbuf.append(", ");
            }
        }
    }

    private static void intArrayAppend(StringBuilder sbuf, int[] a) {
        int len = a.length;
        for (int i = 0; i < len; ++i) {
            sbuf.append(a[i]);
            if (i != len - 1) {
                sbuf.append(", ");
            }
        }
    }

    private static void longArrayAppend(StringBuilder sbuf, long[] a) {
        int len = a.length;
        for (int i = 0; i < len; ++i) {
            sbuf.append(a[i]);
            if (i != len - 1) {
                sbuf.append(", ");
            }
        }
    }

    private static void floatArrayAppend(StringBuilder sbuf, float[] a) {
        int len = a.length;
        for (int i = 0; i < len; ++i) {
            sbuf.append(a[i]);
            if (i != len - 1) {
                sbuf.append(", ");
            }
        }
    }

    private static void doubleArrayAppend(StringBuilder sbuf, double[] a) {
        int len = a.length;
        for (int i = 0; i < len; ++i) {
            sbuf.append(a[i]);
            if (i != len - 1) {
                sbuf.append(", ");
            }
        }
    }
}
