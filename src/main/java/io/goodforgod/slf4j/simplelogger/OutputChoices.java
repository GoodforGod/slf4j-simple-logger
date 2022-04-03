package io.goodforgod.slf4j.simplelogger;

import java.io.PrintStream;

/**
 * This class encapsulates the user's choice of output target.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author Anton Kurako (GoodforGod)
 * @since 09.10.2021
 */
final class OutputChoices {

    private OutputChoices() {}

    static class FileOutputChoice implements OutputChoice {

        private final PrintStream printStream;

        FileOutputChoice(PrintStream printStream) {
            this.printStream = printStream;
        }

        @Override
        public PrintStream getStream() {
            return printStream;
        }
    }

    static class SystemOutOutputChoice implements OutputChoice {

        @Override
        public PrintStream getStream() {
            return System.out;
        }
    }

    static class CachedSystemOutOutputChoice implements OutputChoice {

        private final PrintStream printStream = System.out;

        @Override
        public PrintStream getStream() {
            return printStream;
        }
    }

    static class SystemErrOutputChoice implements OutputChoice {

        @Override
        public PrintStream getStream() {
            return System.err;
        }
    }

    static class CachedSystemErrOutputChoice implements OutputChoice {

        private final PrintStream printStream = System.err;

        @Override
        public PrintStream getStream() {
            return printStream;
        }
    }
}
