package org.luizricardo.warppipe.decoder;


import org.luizricardo.warppipe.listener.StreamListener;
import org.luizricardo.warppipe.matcher.StreamMatcher;

import java.io.IOException;
import java.io.Writer;

/**
 * A decorated {@link Writer} for matching characters written to it, making it possible to match text content in the
 * stream and intercept it or transform it as desired,
 * while the result will be written to the delegated {@link Writer}.
 *
 * <p>
 *     Each character will be stored in a buffer and a checked against a {@link StreamMatcher}. If the buffer doesn't
 *     match, it'll be written to the delegated output. When a buffer completely matches a given matcher, the associated
 *     {@link StreamListener} will be executed, where the buffer can be transformed and any operation can be executed.
 * </p>
 */
public class StreamDecoderWriter extends Writer {

    /**
     * Delegated OutputStream, which will get the transformed output.
     */
    private final Writer writer;

    /**
     * Generic Decoder to delegate actual decoding.
     */
    private final StreamDecoder decoder;

    protected StreamDecoderWriter(
            final Writer writer,
            final StreamMatcher[] matchers,
            final StreamListener[] listeners,
            final int bufferLimit) {
        this.writer = writer;
        this.decoder = new StreamDecoder(matchers, listeners, bufferLimit) {
            @Override
            protected void write(String buffer) throws IOException {
                writer.write(buffer);
            }

            @Override
            protected void flush() throws IOException {
                writer.flush();
            }
        };
    }

    @Override
    public void write(final int c) throws IOException {
        decoder.write((char) c);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            write(cbuf[off + i]);
        }
    }

    /**
     * Writes all buffered content and closes delegated writer.
     */
    @Override
    public void close() throws IOException {
        decoder.writeAllBuffer();
        writer.close();
    }

    /**
     * Flushes delegated Writer, but does not write matched buffer.
     */
    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    /**
     * Builder class for {@link StreamDecoderWriter}.
     */
    public static class Builder extends StreamDecoder.Builder<StreamDecoderWriter> {
        private Writer writer;

        protected Builder(Writer writer) {
            this.writer = writer;
        }

        public StreamDecoderWriter build() {
            return new StreamDecoderWriter(writer,
                    matchers.toArray(new StreamMatcher[matchers.size()]),
                    listeners.toArray(new StreamListener[listeners.size()]),
                    bufferLimit);
        }
    }
}
