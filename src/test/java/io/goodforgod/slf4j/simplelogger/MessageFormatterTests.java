package io.goodforgod.slf4j.simplelogger;

import java.util.concurrent.Callable;
import java.util.function.Supplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.helpers.FormattingTuple;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 20.02.2022
 */
class MessageFormatterTests extends Assertions {

    private static final Integer i1 = 1;
    private static final Integer i2 = 2;
    private static final Integer i3 = 3;
    private static final Integer[] a0 = new Integer[] { i1, i2, i3 };
    private static final Integer[] a1 = new Integer[] { 10, 20, 30 };

    @Test
    void nullMessageResultNull() {
        String result = MessageFormatter.format(null, i1).getMessage();
        assertNull(result);

        result = MessageFormatter.format(null, i1, i2).getMessage();
        assertNull(result);

        result = MessageFormatter.formatArray(null, a0).getMessage();
        assertNull(result);
    }

    @Test
    void messageEqualFormatterCauseNoPlaceholders() {
        final String msg = "no placeholder";
        String result = MessageFormatter.format(msg, i1).getMessage();
        assertEquals(msg, result);

        result = MessageFormatter.format(msg, i1, i2).getMessage();
        assertEquals(msg, result);

        result = MessageFormatter.formatArray(msg, a0).getMessage();
        assertEquals(msg, result);
    }

    @Test
    void messageSupplierAppended() {
        Supplier<String> supplier = () -> "argument";
        String result = MessageFormatter.format("Value supplier is {}.", supplier).getMessage();
        assertEquals("Value supplier is argument.", result);
    }

    @Test
    void messageCallableAppended() {
        Callable<String> supplier = () -> "argument";
        String result = MessageFormatter.format("Value supplier is {}.", supplier).getMessage();
        assertEquals("Value supplier is argument.", result);
    }

    @Test
    void parameterContainingAnAnchor() {
        String result = MessageFormatter.format("Value is {}.", "[{}]").getMessage();
        assertEquals("Value is [{}].", result);

        result = MessageFormatter.format("Values are {} and {}.", i1, "[{}]").getMessage();
        assertEquals("Values are 1 and [{}].", result);
    }

    @Test
    void nullParametersShouldBeHandledWithoutBarfing() {
        String result = MessageFormatter.format("Value is {}.", null).getMessage();
        assertEquals("Value is null.", result);

        result = MessageFormatter.format("Val1 is {}, val2 is {}.", null, null).getMessage();
        assertEquals("Val1 is null, val2 is null.", result);

        result = MessageFormatter.format("Val1 is {}, val2 is {}.", i1, null).getMessage();
        assertEquals("Val1 is 1, val2 is null.", result);

        result = MessageFormatter.format("Val1 is {}, val2 is {}.", null, i2).getMessage();
        assertEquals("Val1 is null, val2 is 2.", result);

        result = MessageFormatter.formatArray("Val1 is {}, val2 is {}, val3 is {}", new Integer[] { null, null, null })
                .getMessage();
        assertEquals("Val1 is null, val2 is null, val3 is null", result);

        result = MessageFormatter.formatArray("Val1 is {}, val2 is {}, val3 is {}", new Integer[] { null, i2, i3 }).getMessage();
        assertEquals("Val1 is null, val2 is 2, val3 is 3", result);

        result = MessageFormatter.formatArray("Val1 is {}, val2 is {}, val3 is {}", new Integer[] { null, null, i3 })
                .getMessage();
        assertEquals("Val1 is null, val2 is null, val3 is 3", result);
    }

    @Test
    void verifyOneParameterIsHandledCorrectly() {
        String result = MessageFormatter.format("Value is {}.", i3).getMessage();
        assertEquals("Value is 3.", result);

        result = MessageFormatter.format("Value is {", i3).getMessage();
        assertEquals("Value is {", result);

        result = MessageFormatter.format("{} is larger than 2.", i3).getMessage();
        assertEquals("3 is larger than 2.", result);

        result = MessageFormatter.format("No subst", i3).getMessage();
        assertEquals("No subst", result);

        result = MessageFormatter.format("Incorrect {subst", i3).getMessage();
        assertEquals("Incorrect {subst", result);

        result = MessageFormatter.format("Value is {bla} {}", i3).getMessage();
        assertEquals("Value is {bla} 3", result);

        result = MessageFormatter.format("Escaped \\{} subst", i3).getMessage();
        assertEquals("Escaped {} subst", result);

        result = MessageFormatter.format("{Escaped", i3).getMessage();
        assertEquals("{Escaped", result);

        result = MessageFormatter.format("\\{}Escaped", i3).getMessage();
        assertEquals("{}Escaped", result);

        result = MessageFormatter.format("File name is {{}}.", "App folder.zip").getMessage();
        assertEquals("File name is {App folder.zip}.", result);

        // escaping the escape character
        result = MessageFormatter.format("File name is C:\\\\{}.", "App folder.zip").getMessage();
        assertEquals("File name is C:\\App folder.zip.", result);
    }

    @Test
    void testTwoParameters() {
        String result = MessageFormatter.format("Value {} is smaller than {}.", i1, i2).getMessage();
        assertEquals("Value 1 is smaller than 2.", result);

        result = MessageFormatter.format("Value {} is smaller than {}", i1, i2).getMessage();
        assertEquals("Value 1 is smaller than 2", result);

        result = MessageFormatter.format("{}{}", i1, i2).getMessage();
        assertEquals("12", result);

        result = MessageFormatter.format("Val1={}, Val2={", i1, i2).getMessage();
        assertEquals("Val1=1, Val2={", result);

        result = MessageFormatter.format("Value {} is smaller than \\{}", i1, i2).getMessage();
        assertEquals("Value 1 is smaller than {}", result);

        result = MessageFormatter.format("Value {} is smaller than \\{} tail", i1, i2).getMessage();
        assertEquals("Value 1 is smaller than {} tail", result);

        result = MessageFormatter.format("Value {} is smaller than \\{", i1, i2).getMessage();
        assertEquals("Value 1 is smaller than \\{", result);

        result = MessageFormatter.format("Value {} is smaller than {tail", i1, i2).getMessage();
        assertEquals("Value 1 is smaller than {tail", result);

        result = MessageFormatter.format("Value \\{} is smaller than {}", i1, i2).getMessage();
        assertEquals("Value {} is smaller than 1", result);
    }

    @Test
    void testExceptionIn_toString() {
        Object o = new Object() {

            @Override
            public String toString() {
                throw new IllegalStateException("a");
            }
        };

        String result = MessageFormatter.format("Troublesome object {}", o).getMessage();
        assertEquals("Troublesome object [FAILED toString()]", result);
    }

    @Test
    void testNullArray() {
        String msg0 = "msg0";
        String msg1 = "msg1 {}";
        String msg2 = "msg2 {} {}";
        String msg3 = "msg3 {} {} {}";

        Object[] args = null;

        String result = MessageFormatter.formatArray(msg0, args).getMessage();
        assertEquals(msg0, result);

        result = MessageFormatter.formatArray(msg1, args).getMessage();
        assertEquals(msg1, result);

        result = MessageFormatter.formatArray(msg2, args).getMessage();
        assertEquals(msg2, result);

        result = MessageFormatter.formatArray(msg3, args).getMessage();
        assertEquals(msg3, result);
    }

    // tests the case when the parameters are supplied in a single array
    @Test
    void testArrayFormat() {
        String result = MessageFormatter.formatArray("Value {} is smaller than {} and {}.", a0).getMessage();
        assertEquals("Value 1 is smaller than 2 and 3.", result);

        result = MessageFormatter.formatArray("{}{}{}", a0).getMessage();
        assertEquals("123", result);

        result = MessageFormatter.formatArray("Value {} is smaller than {}.", a0).getMessage();
        assertEquals("Value 1 is smaller than 2.", result);

        result = MessageFormatter.formatArray("Value {} is smaller than {}", a0).getMessage();
        assertEquals("Value 1 is smaller than 2", result);

        result = MessageFormatter.formatArray("Val={}, {, Val={}", a0).getMessage();
        assertEquals("Val=1, {, Val=2", result);

        result = MessageFormatter.formatArray("Val={}, {, Val={}", a0).getMessage();
        assertEquals("Val=1, {, Val=2", result);

        result = MessageFormatter.formatArray("Val1={}, Val2={", a0).getMessage();
        assertEquals("Val1=1, Val2={", result);
    }

    @Test
    void testArrayValues() {
        Integer p0 = i1;
        Integer[] p1 = new Integer[] { i2, i3 };

        String result = MessageFormatter.format("{}{}", p0, p1).getMessage();
        assertEquals("1[2, 3]", result);

        // Integer[]
        result = MessageFormatter.formatArray("{}{}", new Object[] { "a", p1 }).getMessage();
        assertEquals("a[2, 3]", result);

        // byte[]
        result = MessageFormatter.formatArray("{}{}", new Object[] { "a", new byte[] { 1, 2 } }).getMessage();
        assertEquals("a[1, 2]", result);

        // int[]
        result = MessageFormatter.formatArray("{}{}", new Object[] { "a", new int[] { 1, 2 } }).getMessage();
        assertEquals("a[1, 2]", result);

        // float[]
        result = MessageFormatter.formatArray("{}{}", new Object[] { "a", new float[] { 1, 2 } }).getMessage();
        assertEquals("a[1.0, 2.0]", result);

        // double[]
        result = MessageFormatter.formatArray("{}{}", new Object[] { "a", new double[] { 1, 2 } }).getMessage();
        assertEquals("a[1.0, 2.0]", result);

        // long[]
        result = MessageFormatter.formatArray("{}{}", new Object[] { "a", new long[] { 1L, 2L } }).getMessage();
        assertEquals("a[1, 2]", result);

        // char[]
        result = MessageFormatter.formatArray("{}{}", new Object[] { "a", new char[] { 'a', 'b' } }).getMessage();
        assertEquals("a[a, b]", result);

        // short[]
        result = MessageFormatter.formatArray("{}{}", new Object[] { "a", new short[] { 1, 2 } }).getMessage();
        assertEquals("a[1, 2]", result);

        // boolean[]
        result = MessageFormatter.formatArray("{}{}", new Object[] { "a", new boolean[] { true, false } }).getMessage();
        assertEquals("a[true, false]", result);
    }

    @Test
    void testMultiDimensionalArrayValues() {
        Integer[][] multiIntegerA = new Integer[][] { a0, a1 };
        String result = MessageFormatter.formatArray("{}{}", new Object[] { "a", multiIntegerA }).getMessage();
        assertEquals("a[[1, 2, 3], [10, 20, 30]]", result);

        int[][] multiIntA = new int[][] { { 1, 2 }, { 10, 20 } };
        result = MessageFormatter.formatArray("{}{}", new Object[] { "a", multiIntA }).getMessage();
        assertEquals("a[[1, 2], [10, 20]]", result);

        float[][] multiFloatA = new float[][] { { 1, 2 }, { 10, 20 } };
        result = MessageFormatter.formatArray("{}{}", new Object[] { "a", multiFloatA }).getMessage();
        assertEquals("a[[1.0, 2.0], [10.0, 20.0]]", result);

        Object[][] multiOA = new Object[][] { a0, a1 };
        result = MessageFormatter.formatArray("{}{}", new Object[] { "a", multiOA }).getMessage();
        assertEquals("a[[1, 2, 3], [10, 20, 30]]", result);

        Object[][][] _3DOA = new Object[][][] { multiOA, multiOA };
        result = MessageFormatter.formatArray("{}{}", new Object[] { "a", _3DOA }).getMessage();
        assertEquals("a[[[1, 2, 3], [10, 20, 30]], [[1, 2, 3], [10, 20, 30]]]", result);
    }

    @Test
    void testCyclicArrays() {
        Object[] cyclicA = new Object[1];
        cyclicA[0] = cyclicA;
        assertEquals("[[...]]", MessageFormatter.formatArray("{}", cyclicA).getMessage());

        Object[] a = new Object[2];
        a[0] = i1;
        Object[] c = new Object[] { i3, a };
        Object[] b = new Object[] { i2, c };
        a[1] = b;
        assertEquals("1[2, [3, [1, [...]]]]", MessageFormatter.formatArray("{}{}", a).getMessage());
    }

    @Test
    void testArrayThrowable() {
        FormattingTuple ft;
        Throwable t = new Throwable();
        Object[] ia = new Object[] { i1, i2, i3, t };
        Object[] iaWitness = new Object[] { i1, i2, i3 };

        ft = MessageFormatter.formatArray("Value {} is smaller than {} and {}.", ia);
        assertEquals("Value 1 is smaller than 2 and 3.", ft.getMessage());
        assertArrayEquals(iaWitness, ft.getArgArray());
        assertEquals(t, ft.getThrowable());

        ft = MessageFormatter.formatArray("{}{}{}", ia);
        assertEquals("123", ft.getMessage());
        assertArrayEquals(iaWitness, ft.getArgArray());
        assertEquals(t, ft.getThrowable());

        ft = MessageFormatter.formatArray("Value {} is smaller than {}.", ia);
        assertEquals("Value 1 is smaller than 2.", ft.getMessage());
        assertArrayEquals(iaWitness, ft.getArgArray());
        assertEquals(t, ft.getThrowable());

        ft = MessageFormatter.formatArray("Value {} is smaller than {}", ia);
        assertEquals("Value 1 is smaller than 2", ft.getMessage());
        assertArrayEquals(iaWitness, ft.getArgArray());
        assertEquals(t, ft.getThrowable());

        ft = MessageFormatter.formatArray("Val={}, {, Val={}", ia);
        assertEquals("Val=1, {, Val=2", ft.getMessage());
        assertArrayEquals(iaWitness, ft.getArgArray());
        assertEquals(t, ft.getThrowable());

        ft = MessageFormatter.formatArray("Val={}, \\{, Val={}", ia);
        assertEquals("Val=1, \\{, Val=2", ft.getMessage());
        assertArrayEquals(iaWitness, ft.getArgArray());
        assertEquals(t, ft.getThrowable());

        ft = MessageFormatter.formatArray("Val1={}, Val2={", ia);
        assertEquals("Val1=1, Val2={", ft.getMessage());
        assertArrayEquals(iaWitness, ft.getArgArray());
        assertEquals(t, ft.getThrowable());

        ft = MessageFormatter.formatArray("Value {} is smaller than {} and {}.", ia);
        assertEquals("Value 1 is smaller than 2 and 3.", ft.getMessage());
        assertArrayEquals(iaWitness, ft.getArgArray());
        assertEquals(t, ft.getThrowable());

        ft = MessageFormatter.formatArray("{}{}{}{}", ia);
        assertEquals("123{}", ft.getMessage());
        assertArrayEquals(iaWitness, ft.getArgArray());
        assertEquals(t, ft.getThrowable());

        ft = MessageFormatter.formatArray("1={}", new Object[] { i1 }, t);
        assertEquals("1=1", ft.getMessage());
        assertArrayEquals(new Object[] { i1 }, ft.getArgArray());
        assertEquals(t, ft.getThrowable());
    }
}
