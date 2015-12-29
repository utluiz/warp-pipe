package org.luizricardo.warppipe.listener;

import org.luizricardo.warppipe.decoder.StreamDecoderOutputStream;
import org.luizricardo.warppipe.matcher.StreamMatcher;

/**
 * Functional interface whose {@link #process(StringBuilder)} method will be executed when a {@link org.luizricardo.warppipe.matcher.StreamMatcher}
 * reaches the status of {@link org.luizricardo.warppipe.matcher.MatchingStatus#FULLY}.
 * Listeners are associated with matchers when building a {@link StreamDecoderOutputStream} through
 * method {@link StreamDecoderOutputStream.Builder#bind(StreamMatcher, StreamListener)}.
 */
@FunctionalInterface
public interface StreamListener {

    /**
     * Allows the client to intercept and possibly to transform the matched buffer, changing the content of the {@link StringBuilder}.
     * @param stringBuilder Contains the text matched. Changes to this buffer will reflect in the output, replacing the original content.
     * @return Returns a boolean value indicating if the result should be flushed after written to the output.
     */
    Boolean process(StringBuilder stringBuilder);

    static StreamListener flush() {
        return sb -> Boolean.TRUE;
    }

}
