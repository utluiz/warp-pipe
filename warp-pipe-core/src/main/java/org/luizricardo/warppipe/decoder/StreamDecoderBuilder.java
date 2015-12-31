package org.luizricardo.warppipe.decoder;

import org.luizricardo.warppipe.listener.StreamListener;
import org.luizricardo.warppipe.matcher.StreamMatcher;

import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Base builder class for decoders.
 */
public abstract class StreamDecoderBuilder<T> {

    protected final List<StreamMatcher> matchers = new ArrayList<>();
    protected final List<StreamListener> listeners = new ArrayList<>();
    protected int bufferLimit = 64;

    protected StreamDecoderBuilder() { }

    /**
     * Binds a matcher and a listener, i.e., when some content matches the corresponding listener will be executed.
     */
    public StreamDecoderBuilder<T> bind(final StreamMatcher matcher, final StreamListener listener) {
        this.matchers.add(matcher);
        this.listeners.add(listener);
        return this;
    }

    /**
     * Sets the maximum number of characters that will be stored in the buffers. Default is 64.
     */
    public StreamDecoderBuilder<T> bufferLimit(final int bufferLimit) {
        this.bufferLimit = bufferLimit;
        return this;
    }

    /**
     * Build an instance with the current settings.
     */
    public abstract T build();

    /**
     * Builder class for {@link StreamDecoderOutputStream}.
     */
    protected static class OutputStreamBuilder extends StreamDecoderBuilder<StreamDecoderOutputStream> {
        private final OutputStream outputStream;
        private final Charset charset;

        protected OutputStreamBuilder(final OutputStream outputStream, final Charset charset) {
            this.outputStream = outputStream;
            this.charset = charset;
        }

        @Override
        public StreamDecoderOutputStream build() {
            return new StreamDecoderOutputStream(outputStream, charset,
                    matchers.toArray(new StreamMatcher[matchers.size()]),
                    listeners.toArray(new StreamListener[listeners.size()]),
                    bufferLimit);
        }
    }

    /**
     * Builder class for {@link StreamDecoderWriter}.
     */
    public static class WriterBuilder extends StreamDecoderBuilder<StreamDecoderWriter> {
        private final Writer writer;

        protected WriterBuilder(final Writer writer) {
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
