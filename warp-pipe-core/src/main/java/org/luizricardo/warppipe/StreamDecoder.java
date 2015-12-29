package org.luizricardo.warppipe;


import org.luizricardo.warppipe.listener.StreamListener;
import org.luizricardo.warppipe.matcher.MatchingStatus;
import org.luizricardo.warppipe.matcher.StreamMatcher;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * A decorated OutputStream capable of decoding characters from the stream of bytes written to it,
 * making it possible to match text content in the stream and intercept it or transform it as desired,
 * while the result will be written to the delegated {@link OutputStream}.
 *
 * <p>
 *     This class makes use of {@link CharsetDecoder} to decode characters from each byte that's written to it.
 *     Each character will be stores in a buffer and a checked against a {@link StreamMatcher}. If the buffer doesn't
 *     match, it'll be written to the delegated output. When a buffer completely matches a given matcher, the associated
 *     {@link StreamListener} will be executed, where the buffer can be transformed and any operation can be executed.
 * </p>
 */
public class StreamDecoder {

    /**
     * Buffer which store information about each matcher for the current stream.
     */
    private final MatchingBuffer[] buffers;

    /**
     * Size of buffers array.
     */
    private final int buffersLength;

    /**
     * Max size of each individual buffer. If a buffer matches partially and reach this limit, it'll be discarded.
     */
    private final int bufferLimit;

    /**
     * Store all buffered characters which will be written to the delegated OutputStream in the following conditions:
     * - None matchers are matching;
     * - When one matcher fully matches the buffer;
     * - When one or more matchers are matching partially but the individual buffers and smaller than the global.
     */
    private final StringBuilder globalStringBuilder;

    private StreamDecoder(
            final StreamMatcher[] matchers,
            final StreamListener[] listeners,
            final int bufferLimit) {
        this.bufferLimit = bufferLimit;
        this.globalStringBuilder = new StringBuilder();
        //create buffers for each matcher
        this.buffers = new MatchingBuffer[matchers.length];
        this.buffersLength = buffers.length;
        for (int i = 0; i < buffersLength; i++) {
            buffers[i] = new MatchingBuffer(matchers[i], listeners[i]);
        }
    }

    public void write(final int c) throws IOException {
        //append char to buffers
        globalStringBuilder.append(c);
        //and too al matcher's buffers
        for (int i = 0; i < buffersLength; i++) {
            buffers[i].getMatchingString().append(c);
        }
        //store if there's at least one buffer partially matching
        boolean partiallyMatching = false;
        //store the first buffer fully matching
        MatchingBuffer fullyMatching = null;
        //store the larger buffer currently matching; if the largest buffer is less than the global buffer,
        // it means some characters can be written
        int maxBufferSize = 0;
        //test each buffer for matching
        for (int i = 0; i < buffersLength; i++) {
            final MatchingBuffer buffer = buffers[i];
            //execute matcher
            final MatchingStatus status = buffer.matches();
            if (status == MatchingStatus.FULLY) {
                //if fully matching, saves it and quit for
                fullyMatching = buffer;
                break;
            } else if (status == MatchingStatus.NONE || buffer.size() >= bufferLimit) {
                //if not matching or too much chars, reset buffer
                buffer.reset();
            } else {
                //if partially matching, saves it and store the largest buffer
                partiallyMatching = true;
                if (buffer.size() > maxBufferSize) {
                    maxBufferSize = buffer.size();
                }
            }
        }
        //test results
        if (fullyMatching != null) {
            //if one buffer matches, check if there are previous characters to be written in the global buffer,
            //which may be buffered due to another matcher
            if (fullyMatching.size() != globalStringBuilder.length()) {
                //write those previous characters
                writeBufferPartially(globalStringBuilder.length() - fullyMatching.size(), false);
            }
            //process using listener which could transform the content
            fullyMatching.process();
            //write transformed content
            writeCustomBuffer(fullyMatching);
            //reset all buffers
            resetBuffers();
        } else if (!partiallyMatching) {
            //if none match at all, write current buffer
            writeAllBuffer();
            //and reset everything
            resetBuffers();
        } else if (maxBufferSize < globalStringBuilder.length()) {
            //if the biggest buffer is smaller than the global buffer, then we can write the part of the global buffer
            //that has no use for any buffer
            writeBufferPartially(globalStringBuilder.length() - maxBufferSize, true);
        }
    }

    private void writeAllBuffer() {
        throw new UnsupportedOperationException("Not implemented");
    }

    private void writeCustomBuffer(MatchingBuffer fullyMatching) {
        throw new UnsupportedOperationException("Not implemented");
    }

    private void writeBufferPartially(int i, boolean b) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Reset all buffers, inclusing the global buffer.
     */
    private void resetBuffers() {
        for (int i = 0; i < buffersLength; i++) {
            buffers[i].reset();
        }
        globalStringBuilder.setLength(0);
    }

    /**
     * Buffer for a given pair of matcher and listener.
     * Characters will be buffered while it matches the global buffer.
     */
    private static class MatchingBuffer {
        private final StreamMatcher streamMatcher;
        private final StreamListener streamListener;
        private final StringBuilder matchingString;

        MatchingBuffer(StreamMatcher streamMatcher, StreamListener streamListener) {
            this.streamMatcher = streamMatcher;
            this.streamListener = streamListener;
            this.matchingString = new StringBuilder();
        }

        /**
         * Execute the matcher for the current buffer.
         */
        MatchingStatus matches() {
            return streamMatcher.matches(matchingString);
        }

        /**
         * Get current individual buffer.
         */
        StringBuilder getMatchingString() {
            return matchingString;
        }

        /**
         * Process listener using matched buffer.
         */
        void process() {
            streamListener.process(matchingString);
        }

        /**
         * Size of this individual buffer.
         */
        int size() {
            return matchingString.length();
        }

        /**
         * Reset this individual buffer.
         */
        void reset() {
            matchingString.setLength(0);
        }
    }

    /**
     * Builder class for {@link StreamDecoder}.
     */
    public static class Builder {
        private OutputStream outputStream;
        private Charset charset;
        private Writer writer;
        private int bufferLimit;
        private List<StreamMatcher> matchers = new ArrayList<>();
        private List<StreamListener> listeners = new ArrayList<>();

        /**
         * Start building an instance with required parameters.
         * @param writer Delegated output where bytes will be eventually written.
         */
        public static Builder create(Writer writer) {
            return new Builder(writer);
        }

        /**
         * Start building an instance with required parameters.
         * @param outputStream Delegated output where bytes will be eventually written.
         * @param charset Encoding to decode characters. DOES NOT support BOM (Byte Order Mark).
         */
        public static Builder create(OutputStream outputStream, Charset charset) {
            return new Builder(outputStream, charset);
        }

        private Builder(Writer writer) {
            this.writer = writer;
            this.bufferLimit = 64;
        }

        private Builder(OutputStream outputStream, Charset charset) {
            this.outputStream = outputStream;
            this.charset = charset;
            this.bufferLimit = 64;
        }

        /**
         * Binds a matcher and a listener, i.e., when some content matches the corresponding listener will be executed.
         */
        public Builder bind(StreamMatcher matcher, StreamListener listener) {
            this.matchers.add(matcher);
            this.listeners.add(listener);
            return this;
        }

        /**
         * Sets the maximum number of characters that will be stored in the buffers. Default is 64.
         */
        public Builder bufferLimit(int bufferLimit) {
            this.bufferLimit = bufferLimit;
            return this;
        }

        /**
         * Build an instance with the current settings.
         */
        public StreamDecoder build() {
            if (outputStream != null) {
                return new OutputStreamDecoder(outputStream, charset,
                        matchers.toArray(new StreamMatcher[matchers.size()]),
                        listeners.toArray(new StreamListener[listeners.size()]),
                        bufferLimit);
            } else {
                return new WriterDecoder(writer,
                        matchers.toArray(new StreamMatcher[matchers.size()]),
                        listeners.toArray(new StreamListener[listeners.size()]),
                        bufferLimit);
            }
        }
    }

}
