package org.luizricardo.warppipe.listener;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * Implementations will delegate actions to some underlying object.
 */
public interface MatchingWriter {

    /**
     * Subclasses should use this method to effectively write to the delegated object.
     * @param buffer Content to be written.
     */
    MatchingWriter write(String buffer) throws IOException;

    /**
     * Force the flushing of buffered content in the underlying object.
     */
    MatchingWriter flush() throws IOException;

    /**
     * Builds an instance which delegated to an {@link OutputStream}
     */
    static MatchingWriter forOutputStream(final OutputStream outputStream, final Charset charset) {
        return new MatchingWriter() {
            @Override
            public MatchingWriter write(final String buffer) throws IOException {
                outputStream.write(buffer.getBytes(charset));
                return this;
            }

            @Override
            public MatchingWriter flush() throws IOException {
                outputStream.flush();
                return this;
            }
        };
    }

    /**
     * Builds an instance which delegated to an {@link Writer}
     */
    static MatchingWriter forWriter(final Writer writer) {
        return new MatchingWriter() {
            @Override
            public MatchingWriter write(final String buffer) throws IOException {
                writer.write(buffer);
                return this;
            }

            @Override
            public MatchingWriter flush() throws IOException {
                writer.flush();
                return this;
            }
        };
    }

}
