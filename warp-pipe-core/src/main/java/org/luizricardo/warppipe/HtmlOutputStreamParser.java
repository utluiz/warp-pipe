package org.luizricardo.warppipe;


import org.luizricardo.warppipe.listener.StreamListener;
import org.luizricardo.warppipe.matcher.MatchingStatus;
import org.luizricardo.warppipe.matcher.StreamMatcher;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.util.ArrayList;
import java.util.Arrays;
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
public class HtmlOutputStreamParser extends OutputStream {

    /**
     * Max character size supported, in bytes.
     */
    private static final int MAX_CHAR_SIZE = 4;

    /**
     * Delegated OutputStream, which will get the transformed output.
     */
    private final OutputStream outputStream;

    /**
     * Charset to decode characters from the byte stream.
     */
    private final Charset charset;

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

    /**
     * Character decoder, which can identify characters from one or more bytes.
     */
    private final CharsetDecoder charsetDecoder;

    /**
     * Store current bytes to be decoded. If a byte is successfully decoded into a character, it'll be stored in the charArray.
     * When current bytes are partially decoded, they'll remain in the array awaiting for the next byte.
     */
    private final byte[] byteArray;

    /**
     * Current position of byteArray. It'll be incremented when current byteArray cannot be decoded because contains
     * an incomplete character.
     */
    private int byteArrayPosition;

    /**
     * Stores decoded chars, when they're successfully decoded by the charsetDecoder from the byteArray.
     */
    private final char[] charArray;

    /**
     * Wraps the byte array for use with the charsetDecoder. It'll be resetted on each attempt to decode the byteArray.
     */
    private final ByteBuffer byteBuffer;

    /**
     * Wraps the charArray for use with the charsetDecoder. The charsetDecoder will set the position greater than zero
     * in this object after decoding one or more characters.
     */
    private final CharBuffer charBuffer;

    private HtmlOutputStreamParser(
            final OutputStream outputStream,
            final Charset charset,
            final StreamMatcher[] matchers,
            final StreamListener[] listeners,
            final int bufferLimit) {
        this.outputStream = outputStream;
        this.charset = charset;
        this.bufferLimit = bufferLimit;
        this.charsetDecoder = charset.newDecoder();
        //buffers to hold decoded chars
        this.globalStringBuilder = new StringBuilder();
        this.byteArray = new byte[MAX_CHAR_SIZE]; //supports max 4 bytes characters
        this.byteBuffer = ByteBuffer.wrap(this.byteArray);
        this.charArray = new char[2];
        this.charBuffer = CharBuffer.wrap(this.charArray);
        //create buffers for each matcher
        this.buffers = new MatchingBuffer[matchers.length];
        this.buffersLength = buffers.length;
        for (int i = 0; i < buffersLength; i++) {
            buffers[i] = new MatchingBuffer(matchers[i], listeners[i]);
        }
    }

    @Override
    public void write(final int b) throws IOException {
        //try to decode character; if is not a complete char, return
        if (!decode(b)) {
            return;
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

    /**
     * Writes all buffered content and closes delegated buffer.
     */
    @Override
    public void close() throws IOException {
        writeAllBuffer();
        outputStream.close();
    }

    /**
     * Flushes delegated OutputStream, but does not write buffer.
     */
    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }

    /**
     * Get the next byte and try to decode the next character.
     * If it's not a complete character, then it'll be stored for the next call
     * @param b Byte to decode
     * @return Returns true when at least one char is decoded and false when none.
     */
    private boolean decode(final int b) throws IOException {
        //add new byte to buffer
        byteArray[byteArrayPosition++] = (byte) b;
        //reset buffers positions and limits
        byteBuffer.position(0);
        byteBuffer.limit(byteArrayPosition);
        charBuffer.clear();
        //try to decode the character
        final CoderResult coderResult = charsetDecoder.decode(byteBuffer, charBuffer, false);
        //Underflow means it was able to decode OR is partially decoded
        if (coderResult.isUnderflow()) {
            //if char buffer is not empty, it means at least one char was decoded, so try matching it
            if (charBuffer.position() > 0) {
                //adds current chars into the buffer
                globalStringBuilder.append(charArray, 0, charBuffer.position());
                //and too al matcher's buffers
                for (int i = 0; i < buffersLength; i++) {
                    buffers[i].getMatchingString().append(charArray, 0, charBuffer.position());
                }
                byteArrayPosition = 0;
                //should apply matcher
                return true;
            } else {
                //it no characters were decoded, increment position and wait for another byte
                //unless there the byteArray is full and not more bytes can be stored, then fail miserably
                if (byteArrayPosition >= MAX_CHAR_SIZE) throw new IOException("This error should never occur.");
            }
            return false;
        } else {
            //this exception can be thrown in some specific situations when an invalid result is returned from CharsetDecoder.
            //for instance when a character code is not recognized or when the resulting chars cannot be stored
            //the charArray because they exceed its size.
            //for normal encodings like UTF-8 and ISO-8859-1 it should never happen.
            throw new IOException(String.format("Error while decoding bytes %s encoded in %s. Current buffer is '%s', and CoderResult is %s.",
                    Arrays.toString(Arrays.copyOf(byteArray, byteArrayPosition)), charset, globalStringBuilder, coderResult));
        }
    }

    /**
     * Writes all the global buffer to the output and cleans it.
     */
    private void writeAllBuffer() throws IOException {
        outputStream.write(globalStringBuilder.toString().getBytes(charset));
        globalStringBuilder.setLength(0);
    }

    /**
     * Writes part from the beginning of the global buffer to the output, optionally deleting this part from the global buffer.
     * It'll be called when the beginning of the buffer is not relevant for any individual buffer anymore.
     */
    private void writeBufferPartially(final int length, boolean delete) throws IOException {
        outputStream.write(globalStringBuilder.subSequence(0, length).toString().getBytes(charset));
        if (delete) globalStringBuilder.delete(0, length);
    }

    /**
     * Writes an individual buffer to the output.
     */
    private void writeCustomBuffer(final MatchingBuffer matchingBuffer) throws IOException {
        outputStream.write(matchingBuffer.getMatchingString().toString().getBytes(charset));
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
     * Builder class for {@link HtmlOutputStreamParser}.
     */
    public static class Builder {
        private OutputStream outputStream;
        private Charset charset;
        private int bufferLimit;
        private List<StreamMatcher> matchers = new ArrayList<>();
        private List<StreamListener> listeners = new ArrayList<>();

        /**
         * Start building an instance with required parameters.
         * @param outputStream Delegated output where bytes will be eventually written.
         * @param charset Encoding to decode characters. DOES NOT support BOM (Byte Order Mark).
         */
        public static Builder create(OutputStream outputStream, Charset charset) {
            return new Builder(outputStream, charset);
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
        public HtmlOutputStreamParser build() {
            return new HtmlOutputStreamParser(outputStream, charset,
                    matchers.toArray(new StreamMatcher[matchers.size()]),
                    listeners.toArray(new StreamListener[listeners.size()]),
                    bufferLimit);
        }
    }

}
