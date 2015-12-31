package org.luizricardo.warppipe.decoder;

import org.luizricardo.warppipe.listener.MatchingContext;
import org.luizricardo.warppipe.listener.MatchingWriter;
import org.luizricardo.warppipe.listener.StreamListener;
import org.luizricardo.warppipe.matcher.MatchingStatus;
import org.luizricardo.warppipe.matcher.StreamMatcher;

import java.io.IOException;

/**
 * Keeps individual buffer for a given pair of matcher and listener.
 * Characters will be buffered while it keeps matching new input.
 */
public class MatchingBuffer {

    private final StreamMatcher streamMatcher;
    private final StreamListener streamListener;
    private final StringBuilder buffer;
    private final MatchingWriter matchingWriter;

    private MatchingBuffer(final StreamMatcher streamMatcher, final StreamListener streamListener, MatchingWriter matchingWriter) {
        this.streamMatcher = streamMatcher;
        this.streamListener = streamListener;
        this.matchingWriter = matchingWriter;
        this.buffer = new StringBuilder();
    }

    public static MatchingBuffer create(final StreamMatcher streamMatcher, final StreamListener streamListener, MatchingWriter matchingWriter) {
        return new MatchingBuffer(streamMatcher, streamListener, matchingWriter);
    }

    /**
     * Execute the matcher with the current buffer content.
     */
    public MatchingStatus matches() {
        return streamMatcher.matches(buffer);
    }

    /**
     * Get current string value of this buffer.
     */
    public String content() {
        return buffer.toString();
    }

    /**
     * Append a char to the buffer.
     */
    public void append(final char c) {
        buffer.append(c);
    }

    /**
     * Process listener using matched buffer.
     */
    public void process() throws IOException {
        streamListener.process(MatchingContext.create(buffer, matchingWriter));
    }

    /**
     * Size of this individual buffer.
     */
    public int size() {
        return buffer.length();
    }

    /**
     * Reset this individual buffer.
     */
    public void reset() {
        buffer.setLength(0);
    }

}
