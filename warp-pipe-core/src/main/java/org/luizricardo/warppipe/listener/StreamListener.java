package org.luizricardo.warppipe.listener;

import org.luizricardo.warppipe.OutputStreamDecoder;
import org.luizricardo.warppipe.matcher.StreamMatcher;

/**
 * Functional interface whose {@link #process(StringBuilder)} method will be executed when a {@link org.luizricardo.warppipe.matcher.StreamMatcher}
 * reaches the status of {@link org.luizricardo.warppipe.matcher.MatchingStatus#FULLY}.
 * Listeners are associated with matchers when building a {@link OutputStreamDecoder} through
 * method {@link OutputStreamDecoder.Builder#bind(StreamMatcher, StreamListener)}.
 */
@FunctionalInterface
public interface StreamListener {

    /**
     * Allows the client to intercept and possibly to transform the matched buffer, changing the content of the {@link StringBuilder}.
     * @param stringBuilder Contains the text matched. Changes to this buffer will reflect in the output, replacing the original content.
     */
    void process(StringBuilder stringBuilder);

}
