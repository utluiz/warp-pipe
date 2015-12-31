package org.luizricardo.warppipe.decoder;


import org.luizricardo.warppipe.listener.MatchingWriter;
import org.luizricardo.warppipe.listener.StreamListener;
import org.luizricardo.warppipe.matcher.MatchingStatus;
import org.luizricardo.warppipe.matcher.StreamMatcher;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

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
     * Output which will delegate data to the actual output.
     */
    private MatchingWriter matchingWriter;

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
     * Start building an instance of {@link StreamDecoderOutputStream} with required parameters.
     * @param outputStream Delegated output where bytes will be eventually written.
     * @param charset Encoding to decode characters. DOES NOT support BOM (Byte Order Mark).
     */
    public static StreamDecoderBuilder<StreamDecoderOutputStream> forOutputStream(OutputStream outputStream, Charset charset) {
        return new StreamDecoderBuilder.OutputStreamBuilder(outputStream, charset);
    }

    /**
     * Start building an instance of {@link StreamDecoderWriter} with required parameters.
     * @param writer Delegated output where bytes will be eventually written.
     */
    public static StreamDecoderBuilder<StreamDecoderWriter> forWriter(Writer writer) {
        return new StreamDecoderBuilder.WriterBuilder(writer);
    }

    protected StreamDecoder(
            final MatchingWriter matchingWriter,
            final StreamMatcher[] matchers,
            final StreamListener[] listeners,
            final int bufferLimit) {
        this.matchingWriter = matchingWriter;
        this.bufferLimit = bufferLimit;
        this.globalStringBuilder = new StringBuilder();
        //create buffers for each matcher
        this.buffers = new MatchingBuffer[matchers.length];
        this.buffersLength = buffers.length;
        for (int i = 0; i < buffersLength; i++) {
            buffers[i] = MatchingBuffer.create(matchers[i], listeners[i], matchingWriter);
        }
    }

    /**
     * Handled a new char, adding it to the buffers and checking all matchers.
     */
    public void write(final char c) throws IOException {
        //append char to buffers
        globalStringBuilder.append(c);
        //and too al matcher's buffers
        for (int i = 0; i < buffersLength; i++) {
            buffers[i].append(c);
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
            //process using listener which could transform and flush new content
            fullyMatching.process();
            //write remaining content if any
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
     * Writes all the global buffer to the output and cleans it.
     */
    public void writeAllBuffer() throws IOException {
        matchingWriter.write(globalStringBuilder.toString());
        globalStringBuilder.setLength(0);
    }

    /**
     * Retrieves current buffer, for inspection
     */
    public StringBuilder currentBuffer() {
        return globalStringBuilder;
    }

    /**
     * Writes part from the beginning of the global buffer to the output, optionally deleting this part from the global buffer.
     * It'll be called when the beginning of the buffer is not relevant for any individual buffer anymore.
     */
    protected void writeBufferPartially(final int length, boolean delete) throws IOException {
        matchingWriter.write(globalStringBuilder.subSequence(0, length).toString());
        if (delete) globalStringBuilder.delete(0, length);
    }

    /**
     * Writes an individual buffer to the output.
     */
    protected void writeCustomBuffer(final MatchingBuffer matchingBuffer) throws IOException {
        if (matchingBuffer.size() > 0) {
            matchingWriter.write(matchingBuffer.content());
        }
    }

    /**
     * Reset all buffers, inclusing the global buffer.
     */
    protected void resetBuffers() {
        for (int i = 0; i < buffersLength; i++) {
            buffers[i].reset();
        }
        globalStringBuilder.setLength(0);
    }

}
