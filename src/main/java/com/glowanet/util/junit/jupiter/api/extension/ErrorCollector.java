package com.glowanet.util.junit.jupiter.api.extension;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.rules.Verifier;
import org.opentest4j.MultipleFailuresError;
import org.opentest4j.TestAbortedException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The ErrorCollector rule allows execution of a test to continue after the
 * first problem is found (for example, to collect _all_ the incorrect rows in a
 * table, and report them all at once):
 *
 * <pre>
 * public static class UsesErrorCollectorTwice {
 * 	&#064;Rule
 * 	public ErrorCollector collector= new ErrorCollector();
 *
 * &#064;Test
 * public void example() {
 *      collector.addError(new Throwable(&quot;first thing went wrong&quot;));
 *      collector.addError(new Throwable(&quot;second thing went wrong&quot;));
 *      collector.checkThat(getResult(), not(containsString(&quot;ERROR!&quot;)));
 *      // all lines will run, and then a combined failure logged at the end.
 *     }
 * }
 * </pre>
 *
 * @since 4.7
 */
@ExtendWith(SoftAssertionsExtension.class)
public class ErrorCollector extends Verifier {
    private List<Throwable> errors = new ArrayList<Throwable>();

    @Override
    protected void verify() throws Throwable {
        assertEmpty(errors);
    }

    /**
     * Rethrows the given {@code Throwable}, allowing the caller to
     * declare that it throws {@code Exception}. This is useful when
     * your callers have nothing reasonable they can do when a
     * {@code Throwable} is thrown. This is declared to return {@code Exception}
     * so it can be used in a {@code throw} clause:
     * <pre>
     * try {
     *   doSomething();
     * } catch (Throwable e} {
     *   throw Throwables.rethrowAsException(e);
     * }
     * doSomethingLater();
     * </pre>
     *
     * @param e exception to rethrow
     *
     * @return does not return anything
     *
     * @since 4.12
     */
    public static Exception rethrowAsException(Throwable e) throws Exception {
        ErrorCollector.<Exception>rethrow(e);
        return null; // we never get here
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void rethrow(Throwable e) throws T {
        throw (T) e;
    }

    /**
     * Asserts that a list of throwables is empty. If it isn't empty,
     * will throw {@link MultipleFailuresError} (if there are
     * multiple throwables in the list) or the first element in the list
     * (if there is only one element).
     *
     * @param errors list to check
     *
     * @throws Exception or Error if the list is not empty
     */
    private void assertEmpty(List<Throwable> errors) throws Exception {
        if (errors.isEmpty()) {
            return;
        }
        if (errors.size() == 1) {
            throw rethrowAsException(errors.get(0));
        }

        /*
         * Many places in the code are documented to throw
         * org.junit.internal.runners.model.MultipleFailuresError.
         * That class now extends this one, so we throw the internal
         * exception in case developers have tests that catch
         * MultipleFailuresError.
         */
        String heading = "";
        throw new MultipleFailuresError(heading, errors);
    }

    /**
     * Adds a Throwable to the table.  Execution continues, but the test will fail at the end.
     */
    public void addError(Throwable error) {
        if (error == null) {
            throw new NullPointerException("Error cannot be null");
        }
        if (error instanceof TestAbortedException) {
            AssertionError e = new AssertionError(error.getMessage());
            e.initCause(error);
            errors.add(e);
        } else {
            errors.add(error);
        }
    }

    /**
     * Adds a failure to the table if {@code matcher} does not match {@code value}.
     * Execution continues, but the test will fail at the end if the match fails.
     */
    public <T> void checkThat(final T value, final Matcher<T> matcher) {
        checkThat("", value, matcher);
    }

    /**
     * Adds a failure with the given {@code reason}
     * to the table if {@code matcher} does not match {@code value}.
     * Execution continues, but the test will fail at the end if the match fails.
     */
    public <T> void checkThat(final String reason, final T value, final Matcher<T> matcher) {
        checkSucceeds(new Callable<Object>() {
            public Object call() throws Exception {
                assertThat(reason, value, matcher);
                return value;
            }
        });
    }

    /**
     * Adds to the table the exception, if any, thrown from {@code callable}.
     * Execution continues, but the test will fail at the end if
     * {@code callable} threw an exception.
     */
    public <T> T checkSucceeds(Callable<T> callable) {
        try {
            return callable.call();
        } catch (TestAbortedException e) {
            AssertionError error = new AssertionError("Callable threw TestAbortedException");
            error.initCause(e);
            addError(error);
            return null;
        } catch (Throwable e) {
            addError(e);
            return null;
        }
    }

    /**
     * Adds a failure to the table if {@code runnable} does not throw an
     * exception of type {@code expectedThrowable} when executed.
     * Execution continues, but the test will fail at the end if the runnable
     * does not throw an exception, or if it throws a different exception.
     *
     * @param expectedThrowable the expected type of the exception
     * @param runnable          a function that is expected to throw an exception when executed
     *
     * @since 4.13
     */
    public void checkThrows(Class<? extends Throwable> expectedThrowable, Executable runnable) {
        try {
            assertThrows(expectedThrowable, runnable);
        } catch (AssertionError e) {
            addError(e);
        }
    }

}
