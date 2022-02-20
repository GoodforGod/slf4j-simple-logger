package io.goodforgod.slf4j.simplelogger.multiThreadedExecution;

import java.io.PrintStream;
import java.util.regex.Pattern;

/**
 * This PrintStream checks that output lines are in an expected order.
 * 
 * @author ceki
 */
public class StateCheckingPrintStream extends PrintStream {

    enum State {
        INITIAL,
        UNKNOWN,
        HELLO,
        THROWABLE,
        AT1,
        AT2,
        OTHER;
    }

    volatile State currentState = State.INITIAL;

    public StateCheckingPrintStream(PrintStream ps) {
        super(ps);
    }

    public void print(String s) {}

    public void println(String s) {
        final State next = computeState(s);
        switch (currentState) {
            case INITIAL:
                currentState = next;
                break;

            case UNKNOWN:
                // ignore garbage
                currentState = next;
                break;

            case OTHER:
                if (next == State.UNKNOWN) {
                    currentState = State.UNKNOWN;
                    return;
                }

                if (next != State.OTHER && next != State.HELLO) {
                    throw badState(s, currentState, next);
                }
                currentState = next;
                break;

            case HELLO:
                if (next != State.THROWABLE) {
                    throw badState(s, currentState, next);
                }
                currentState = next;
                break;
            case THROWABLE:
                if (next != State.AT1) {
                    throw badState(s, currentState, next);
                }
                currentState = next;
                break;

            case AT1:
                if (next != State.AT2) {
                    throw badState(s, currentState, next);
                }
                currentState = next;
                break;

            case AT2:
                currentState = next;
                break;
            default:
                throw new IllegalStateException("Unreachable code");
        }
    }

    private IllegalStateException badState(String s, State currentState2, State next) {
        return new IllegalStateException("Unexpected state " + next + " for current state " + currentState2 + " for " + s);
    }

    String OTHER_PATTERN_STR = "Other \\d{1,5}$";
    String HELLO_PATTERN_STR = "Hello \\d{1,5}$";
    String THROWABLE_PATTERN_STR = "java.lang.Throwable: i=\\d{1,5}";
    String AT1_PATTERN_STR = "\\s*at " + getClass().getPackage().getName() + ".*";
    String AT2_PATTERN_STR = "\\s*at " + ".*Thread.java.*";

    Pattern PATTERN_OTHER = Pattern.compile(OTHER_PATTERN_STR);
    Pattern PATTERN_HELLO = Pattern.compile(HELLO_PATTERN_STR);
    Pattern PATTERN_THROWABLE = Pattern.compile(THROWABLE_PATTERN_STR);
    Pattern PATTERN_AT1 = Pattern.compile(AT1_PATTERN_STR);
    Pattern PATTERN_AT2 = Pattern.compile(AT2_PATTERN_STR);

    private State computeState(String s) {
        if (PATTERN_OTHER.matcher(s).matches()) {
            return State.OTHER;
        } else if (PATTERN_HELLO.matcher(s).matches()) {
            return State.HELLO;
        } else if (PATTERN_THROWABLE.matcher(s).matches()) {
            return State.THROWABLE;
        } else if (PATTERN_AT1.matcher(s).matches()) {
            return State.AT1;
        } else if (PATTERN_AT2.matcher(s).matches()) {
            return State.AT2;
        } else {
            return State.UNKNOWN;
        }
    }

    public void println(Object o) {
        println(o.toString());
    }
}
