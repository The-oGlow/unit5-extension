package com.glowanet.util.junit.jupiter.api.extension;

import com.glowanet.util.reflect.ReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.function.Executable;
import org.opentest4j.MultipleFailuresError;
import org.opentest4j.TestAbortedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The ErrorCollector rule allows execution of a test to continue after the
 * first problem is found (for example, to collect _all_ the incorrect rows in a
 * table, and report them all at once):
 *
 * <pre>
 * public static clazz UsesErrorCollectorTwice {
 *     &#064;Rule
 *     public ErrorCollector collector= new ErrorCollector();
 *
 *     &#064;Test
 *     public void example() {
 *         collector.addError(new Throwable(&quot;first thing went wrong&quot;));
 *         collector.addError(new Throwable(&quot;second thing went wrong&quot;));
 *         collector.checkThat(getResult(), not(containsString(&quot;ERROR!&quot;)));
 *         // all lines will run, and then a combined failure logged at the end.
 *     }
 * }
 * </pre>
 *
 * @since 4.7
 */
public class ErrorCollector implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback, AfterTestExecutionCallback {

    private static final Logger LOGGER = LogManager.getLogger();

    private final List<Throwable> errors = new ArrayList<>();

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        LOGGER.trace("beforeAll");
        reset();
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        LOGGER.trace("afterAll");
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        LOGGER.trace("beforeEach");
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        LOGGER.trace("afterEach");
        assertEmpty(errors);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        LOGGER.trace("afterTestExecution");
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
     * } catch (Throwable throwableException} {
     *   throw rethrowAsException(throwableException);
     * }
     * doSomethingLater();
     * </pre>
     *
     * @param throwableException exception to rethrow
     *
     * @return does not return anything
     *
     * @throws Exception throws the {@code throwableException}
     * @since 4.12
     */
    public static Exception rethrowAsException(Throwable throwableException) throws Exception {
        ErrorCollector.<Exception>rethrow(throwableException);
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
    @SuppressWarnings("java:S1162")
    protected void assertEmpty(List<Throwable> errors) throws Exception {
        if (errors.isEmpty()) {
            return;
        }
        if (errors.size() == 1) {
            if (errors.get(0) != null) {
                throw rethrowAsException(errors.get(0));
            } else {
                throw rethrowAsException(new NullPointerException());
            }
        }

        /*
         * Many places in the code are documented to throw
         * org.junit.internal.runners.model.MultipleFailuresError.
         * That clazz now extends this one, so we throw the internal
         * exception in case developers have tests that catch
         * MultipleFailuresError.
         */
        String heading = "";
        throw new MultipleFailuresError(heading, errors);
    }

    /**
     * Adds a Throwable to the table.  Execution continues, but the test will fail at the end.
     *
     * @param throwableException raised exeception
     */
    public void addError(Throwable throwableException) {
        if (throwableException == null) {
            throw new NullPointerException("Error cannot be null"); //NOSONAR java:S1695
        }
        if (throwableException instanceof TestAbortedException) {
            AssertionError e = new AssertionError(throwableException.getMessage());
            e.initCause(throwableException);
            errors.add(e);
        } else {
            errors.add(throwableException);
        }
    }

    /**
     * Adds a failure to the table if {@code matcher} does not match {@code value}.
     * Execution continues, but the test will fail at the end if the match fails.
     *
     * @param value   a value which will be checked
     * @param matcher the matcher which checks the {@code value}
     * @param <T>     the type of {@code value}
     */
    public <T> void checkThat(final T value, final Matcher<T> matcher) {
        checkThat("", value, matcher);
    }

    /**
     * Adds a failure with the given {@code reason}
     * to the table if {@code matcher} does not match {@code value}.
     * Execution continues, but the test will fail at the end if the match fails.
     *
     * @param reason  a message which will be displayed, if not matching
     * @param value   a value which will be checked
     * @param matcher the matcher which checks the {@code value}
     * @param <T>     the type of {@code value}
     */
    public <T> void checkThat(final String reason, final T value, final Matcher<T> matcher) {
        checkSucceeds(new Callable<>() {
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
     *
     * @param callable amessage
     * @param <T>      the type which is used in {@code callable}
     *
     * @return the result of the {@code callable}
     */
    public <T> T checkSucceeds(Callable<T> callable) {
        try {
            return callable.call();
        } catch (TestAbortedException e) {
            AssertionError error = new AssertionError("Callable threw TestAbortedException");
            error.initCause(e);
            addError(error);
            return null;
        } catch (Exception e) {
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

    /**
     * Write a list of throwables into the collector.
     *
     * @param errors List of throwables
     */
    protected void writeErrors(List<Throwable> errors) {
        ReflectionHelper.writeField("errors", this, errors);
    }

    /**
     * @return How many errors are collected.
     */
    public int getErrorSize() {
        int size = 0;
        List<Throwable> errorsRead = readErrors();
        if (errorsRead != null) {
            size = readErrors().size();
        }
        return size;
    }

    /**
     * @return List of collected error messages
     *
     * @see #getErrorTextsToString()
     */
    public List<String> getErrorTexts() {
        List<Throwable> errorsRead = readErrors();
        List<String> errorTexts = new ArrayList<>();
        if (errorsRead != null) {
            errorTexts = errorsRead.stream()
                    .map(m -> Optional.ofNullable(m.getMessage()).orElse(m.getClass().getName()))
                    .collect(Collectors.toList());
        }
        return errorTexts;
    }

    /**
     * @return All collected error messages, delimited with '\n'
     *
     * @see #getErrorTexts()
     */
    public String getErrorTextsToString() {
        return getErrorTexts().stream()
                .map(String::toString)
                .collect(Collectors.joining("\n"));
    }

    /**
     * Clear the collector.
     */
    public void reset() {
        writeErrors(new ArrayList<>());
    }

    /**
     * @return List or collected throwables.
     */
    protected List<Throwable> readErrors() {
        return ReflectionHelper.readField("errors", this);
    }

}
