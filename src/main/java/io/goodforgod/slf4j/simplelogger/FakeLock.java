package io.goodforgod.slf4j.simplelogger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 02.04.2022
 */
final class FakeLock implements Lock {

    @Override
    public void lock() {
        // do nothing
    }

    @Override
    public void lockInterruptibly() {
        lock();
    }

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) {
        return false;
    }

    @Override
    public void unlock() {
        // do nothing
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }
}
