package org.luizricardo.warppipe;


import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;

public class HtmlOutputStreamParser extends OutputStream {

    private final OutputStream outputStream;
    private final List<StreamMatcher> matchers;
    private final Charset charset;
    private final byte[] byteBuffer;
    private int position;

    public HtmlOutputStreamParser(final OutputStream outputStream, final List<StreamMatcher> matchers, final Charset charset) {
        this(outputStream, matchers, charset, 64);
    }

    public HtmlOutputStreamParser(final OutputStream outputStream, final List<StreamMatcher> matchers, final Charset charset, final int bufferSize) {
        this.outputStream = outputStream;
        this.matchers = matchers;
        this.charset = charset;
        this.byteBuffer = new byte[bufferSize];
        this.position = 0;
    }

    /**
     * Check multi-byte char? https://en.wikipedia.org/wiki/UTF-8#Description
     * 1s and 0s say how many bytes a char got
     * if now UTF-8?
     */
    @Override
    public void write(final int b) throws IOException {
        //adds to buffer
        byteBuffer[position++] = (byte) b;
        //store information
        StreamMatcher fully = null;
        int count = 0;
        //test all matchers
        ext: for (StreamMatcher matcher : matchers) {
            switch (matcher.matchNext((byte) b).matching()) {
                case FULLY:
                    //matches fully? write that one
                    fully = matcher;
                    break ext;
                case PARTIALLY:
                    //matches partially? count
                    count++;
            }
        }
        //if matches, transform the content, writes it, and reset other matchers
        if (fully != null) {
            //obtain buffered string
            final String buffer = new String(byteBuffer, 0, position, charset);
            //process using matcher fully matched and write result
            final String result = fully.transform(buffer);
            //write result
            outputStream.write(result.getBytes(charset));
            position = 0;
            //reset matchers
            matchers.stream().forEach(StreamMatcher::reset);
        } else if (count == 0) {
            //none match
            writeBuffer();
        }
    }

    @Override
    public void close() throws IOException {
        writeBuffer();
        outputStream.close();
    }

    @Override
    public void flush() throws IOException {
        writeBuffer();
        outputStream.flush();
    }

    private void writeBuffer() throws IOException {
        outputStream.write(byteBuffer, 0, position);
        position = 0;
    }

}
