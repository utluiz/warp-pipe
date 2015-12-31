package org.luizricardo.warppipe.listener;

/**
 * Interface to {@link StreamListener}s interact with the matched content.
 * Implementations should use method chaining pattern.
 */
public interface MatchingContext {

    /**
     * Access the underlying content. Changes in this object will affect the content written to the output.
     */
    StringBuilder content();

    /**
     * Clear current content;
     */
    MatchingContext clear();

    /**
     * Access to the underlying output.
     */
    MatchingWriter output();

    static MatchingContext create(final StringBuilder content, final MatchingWriter matchingWriter) {
        return new MatchingContext() {
            @Override
            public StringBuilder content() {
                return content;
            }

            @Override
            public MatchingContext clear() {
                content.setLength(0);
                return this;
            }

            @Override
            public MatchingWriter output() {
                return matchingWriter;
            }
        };
    }

}
