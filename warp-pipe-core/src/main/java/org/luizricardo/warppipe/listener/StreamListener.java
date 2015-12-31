package org.luizricardo.warppipe.listener;

import org.luizricardo.warppipe.decoder.StreamDecoderBuilder;
import org.luizricardo.warppipe.decoder.StreamDecoderOutputStream;
import org.luizricardo.warppipe.matcher.StreamMatcher;

import java.io.IOException;

/**
 * Functional interface whose {@link #process(MatchingContext)} method will be executed when a {@link org.luizricardo.warppipe.matcher.StreamMatcher}
 * reaches the status of {@link org.luizricardo.warppipe.matcher.MatchingStatus#FULLY}.
 * Listeners are associated with matchers when building a {@link StreamDecoderOutputStream} through
 * method {@link StreamDecoderBuilder.WriterBuilder#bind(StreamMatcher, StreamListener)}.
 */
@FunctionalInterface
public interface StreamListener {

    /**
     * Allows the client to intercept, transform, and flush the matched buffer.
     * @param matchingContext Contains the content matched. Changes to this object will reflect in the underlying output.
     */
    void process(final MatchingContext matchingContext) throws IOException;

    /**
     * Listener which just quickly flushes buffered content.
     */
    static StreamListener flushListener() {
        return context -> context.output().flush();
    }

}
