package io.goodforgod.slf4j.simplelogger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.slf4j.LoggerFactory;

public class LoggerFactoryFriend {

    static public void reset() {
        final Method method = Arrays.stream(LoggerFactory.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("reset"))
                .findFirst().orElseThrow();

        try {
            method.setAccessible(true);
            method.invoke(LoggerFactory.class);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }
}
