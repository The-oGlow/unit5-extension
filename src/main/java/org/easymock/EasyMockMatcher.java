package org.easymock;

import org.easymock.internal.matchers.PrimaryId;

/**
 * Additional EasyMockMatcher class. Contains a list of additional matchers.
 *
 * @author Oliver Glowa
 * @see EasyMock
 * @since 0.10.000
 */
public class EasyMockMatcher {

    private EasyMockMatcher() {
        // hide public constructor
    }

    /**
     * @param expectedInstance an instance of the expected type
     * @param primIdFieldName  the field name which is used as primary-id.
     * @param <T>              type of the method argument to match
     *
     * @return {@code null}.
     */
    public static <T> T eqPrimaryId(final T expectedInstance, final String primIdFieldName) {
        EasyMock.reportMatcher(new PrimaryId<>(expectedInstance, primIdFieldName));
        return null;
    }
}
