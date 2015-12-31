package org.luizricardo.warppipe.decoder;


import org.luizricardo.warppipe.listener.MatchingWriter;
import org.luizricardo.warppipe.listener.StreamListener;
import org.luizricardo.warppipe.matcher.StreamMatcher;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.util.Arrays;

/**
 * A decorated {@link OutputStream} capable of decoding characters from the stream of bytes written to it,
 * making it possible to match text content in the stream and intercept it or transform it as desired,
 * while the result will be written to the delegated {@link OutputStream}.
 *
 * <p>
 *     This class makes use of {@link CharsetDecoder} to decode characters from each byte that's written to it.
 *     Each character will be stored in a buffer and a checked against a {@link StreamMatcher}. If the buffer doesn't
 *     match, it'll be written to the delegated output. When a buffer completely matches a given matcher, the associated
 *     {@link StreamListener} will be executed, where the buffer can be transformed and any operation can be executed.
 * </p>
 */
public class StreamDecoderOutputStream extends OutputStream {

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
     * Generic Decoder to delegate actual decoding.
     */
    private final StreamDecoder decoder;

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

    protected StreamDecoderOutputStream(
            final OutputStream outputStream,
            final Charset charset,
            final StreamMatcher[] matchers,
            final StreamListener[] listeners,
            final int bufferLimit) {
        this.outputStream = outputStream;
        this.charset = charset;
        //decoder implementation that delegates to the output stream
        this.decoder = new StreamDecoder(MatchingWriter.forOutputStream(outputStream, charset), matchers, listeners, bufferLimit);
        //byte to character decoder
        this.charsetDecoder = charset.newDecoder();
        //buffers to hold decoded chars
        this.byteArray = new byte[MAX_CHAR_SIZE]; //supports max 4 bytes characters
        this.byteBuffer = ByteBuffer.wrap(this.byteArray);
        this.charArray = new char[2];
        this.charBuffer = CharBuffer.wrap(this.charArray);
    }

    /**
     * Get the next byte and try to decode the next character.
     * If it's not a complete character, then it'll be stored for the next call
     * @param b Byte to decode
     */
    @Override
    public void write(final int b) throws IOException {
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
                //writes decoded characters to the stream decoder
                for (int i = 0; i < charBuffer.position(); i++) {
                    decoder.write(charArray[i]);
                }
                byteArrayPosition = 0;
            } else {
                //it no characters were decoded, increment position and wait for another byte
                //unless there the byteArray is full and not more bytes can be stored, then fail miserably
                if (byteArrayPosition >= MAX_CHAR_SIZE) throw new IOException("This error should never occur.");
            }
        } else {
            //this exception can be thrown in some specific situations when an invalid result is returned from CharsetDecoder.
            //for instance when a character code is not recognized or when the resulting chars cannot be stored
            //the charArray because they exceed its size.
            //for normal encodings like UTF-8 and ISO-8859-1 it should never happen.
            throw new IOException(String.format("Error while decoding bytes %s encoded in %s. Current buffer is '%s', and CoderResult is %s.",
                    Arrays.toString(Arrays.copyOf(byteArray, byteArrayPosition)), charset, decoder.currentBuffer(), coderResult));
        }
    }

    /**
     * Writes all buffered content and closes delegated stream.
     */
    @Override
    public void close() throws IOException {
        decoder.writeAllBuffer();
        outputStream.close();
    }

    /**
     * Flushes delegated OutputStream, but does not write matched buffer.
     */
    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }

}
