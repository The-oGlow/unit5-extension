package org.junit.function;

/**
 * This interface is need to evaluate the exception in an {@code ThrowingRunnable}
 *
 * @param <E> the type of the exception
 */
@FunctionalInterface
public interface IThrowingRunnable<E extends Throwable> {

    void run() throws E;
}
