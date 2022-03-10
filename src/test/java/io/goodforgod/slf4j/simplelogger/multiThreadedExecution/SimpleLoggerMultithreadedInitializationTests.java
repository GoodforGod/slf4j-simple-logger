package io.goodforgod.slf4j.simplelogger.multiThreadedExecution;

import io.goodforgod.slf4j.simplelogger.LoggerFactoryFriend;
import io.goodforgod.slf4j.simplelogger.SimpleLoggerProperties;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class SimpleLoggerMultithreadedInitializationTests extends MultithreadedInitializationTests {

    static int NUM_LINES_IN_SLF4J_REPLAY_WARNING = 3;
    private final PrintStream oldOut = System.out;
    StringPrintStream sps = new StringPrintStream(oldOut, true);

    @BeforeEach
    public void setup() {
        System.out.println("THREAD_COUNT=" + THREAD_COUNT);
        System.setOut(sps);
        System.setProperty(SimpleLoggerProperties.LOG_FILE, "System.out");
        LoggerFactoryFriend.reset();
    }

    @AfterEach
    public void tearDown() {
        LoggerFactoryFriend.reset();
        System.clearProperty(SimpleLoggerProperties.LOG_FILE);
        System.setOut(oldOut);
    }

    @Override
    protected long getRecordedEventCount() {
        return sps.stringList.size();
    };

    @Override
    protected int extraLogEvents() {
        return NUM_LINES_IN_SLF4J_REPLAY_WARNING;
    }

    static class StringPrintStream extends PrintStream {

        public static final String LINE_SEP = System.lineSeparator();
        PrintStream other;
        boolean duplicate;

        List<String> stringList = Collections.synchronizedList(new ArrayList<>());

        public StringPrintStream(PrintStream ps, boolean duplicate) {
            super(ps);
            other = ps;
            this.duplicate = duplicate;
        }

        @Override
        public void print(Object s) {
            if (duplicate)
                other.print(s);
            stringList.add(s.toString());
        }

        @Override
        public void print(String s) {
            if (duplicate)
                other.print(s);
            stringList.add(s);
        }

        @Override
        public void println(String s) {
            if (duplicate)
                other.println(s);
            stringList.add(s);
        }

        @Override
        public void println(Object o) {
            if (duplicate)
                other.println(o);
            stringList.add(o.toString());
        }
    }
}
