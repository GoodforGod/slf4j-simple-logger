package io.goodforgod.slf4j.simplelogger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ClassNameAbbreviatorTests extends Assertions {

    @Test
    void testShortName() {
        String name = "hello";
        assertEquals(name, ClassNameAbbreviator.abbreviate(name, 100));

        name = "hello.world";
        assertEquals(name, ClassNameAbbreviator.abbreviate(name, 100));
    }

    @Test
    void testNoDot() {
        String name = "hello";
        assertEquals(name, ClassNameAbbreviator.abbreviate(name, 1));
    }

    @Test
    void testOneDot() {
        String name = "hello.world";
        assertEquals("h.world", ClassNameAbbreviator.abbreviate(name, 1));

        name = "h.world";
        assertEquals("h.world", ClassNameAbbreviator.abbreviate(name, 1));

        name = ".world";
        assertEquals(".world", ClassNameAbbreviator.abbreviate(name, 1));
    }

    @Test
    void testTwoDot() {
        String name = "com.logback.Foobar";
        assertEquals("c.l.Foobar", ClassNameAbbreviator.abbreviate(name, 1));

        name = "c.logback.Foobar";
        assertEquals("c.l.Foobar", ClassNameAbbreviator.abbreviate(name, 1));

        name = "c..Foobar";
        assertEquals("c..Foobar", ClassNameAbbreviator.abbreviate(name, 1));

        name = "..Foobar";
        assertEquals("..Foobar", ClassNameAbbreviator.abbreviate(name, 1));
    }

    @Test
    void test3Dot() {
        String name = "com.logback.xyz.Foobar";
        assertEquals("c.l.x.Foobar", ClassNameAbbreviator.abbreviate(name, 1));

        name = "com.logback.xyz.Foobar";
        assertEquals("c.l.x.Foobar", ClassNameAbbreviator.abbreviate(name, 13));

        name = "com.logback.xyz.Foobar";
        assertEquals("c.l.xyz.Foobar", ClassNameAbbreviator.abbreviate(name, 14));

        name = "com.logback.alligator.Foobar";
        assertEquals("c.l.a.Foobar", ClassNameAbbreviator.abbreviate(name, 15));
    }

    @Test
    void testXDot() {
        String name = "com.logback.wombat.alligator.Foobar";
        assertEquals("c.l.w.a.Foobar", ClassNameAbbreviator.abbreviate(name, 21));

        name = "com.logback.wombat.alligator.Foobar";
        assertEquals("c.l.w.alligator.Foobar", ClassNameAbbreviator.abbreviate(name, 22));

        name = "com.logback.wombat.alligator.tomato.Foobar";
        assertEquals("c.l.w.a.t.Foobar", ClassNameAbbreviator.abbreviate(name, 1));

        name = "com.logback.wombat.alligator.tomato.Foobar";
        assertEquals("c.l.w.a.tomato.Foobar", ClassNameAbbreviator.abbreviate(name, 21));

        name = "com.logback.wombat.alligator.tomato.Foobar";
        assertEquals("c.l.w.alligator.tomato.Foobar", ClassNameAbbreviator.abbreviate(name, 29));
    }
}
