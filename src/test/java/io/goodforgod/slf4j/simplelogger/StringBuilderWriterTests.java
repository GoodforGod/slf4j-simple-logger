package io.goodforgod.slf4j.simplelogger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 11.03.2022
 */
class StringBuilderWriterTests extends Assertions {

    @Test
    void appendSuccess() {
        final StringBuilderWriter writer = new StringBuilderWriter(new StringBuilder());
        writer.write('c');
        writer.write(new char[] { 'h', 'a' }, 0, 2);
        writer.append("rac");
        writer.append("ter", 0, 3);
        writer.append('!');

        writer.flush();
        writer.close();

        assertEquals("character!", writer.toString());
    }
}
