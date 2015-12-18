package org.luizricardo.warppipe;


import java.nio.charset.Charset;
import java.util.function.Function;

import static org.luizricardo.warppipe.MatchingStatus.*;

public class TextStreamMatcher implements StreamMatcher {

    private final byte[] bytes;
    private final Function<String, String> textTransformer;
    private int position;
    private MatchingStatus status;

    public TextStreamMatcher(final String text, final Charset charset, final Function<String, String> textTransformer) {
        this.bytes = text.getBytes(charset);
        this.textTransformer = textTransformer;
        this.position = 0;
        this.status = NONE;
    }

    public StreamMatcher matchNext(final byte b) {
        if (position < bytes.length && bytes[position] == b) {
            position++;
            if (position == bytes.length) {
                position = 0;
                status = FULLY;
            } else {
                status = PARTIALLY;
            }
        } else {
            if (bytes[0] == b) {
                position = 1;
                status = PARTIALLY;
            } else {
                position = 0;
                status = NONE;
            }
        }
        return this;
    }

    @Override
    public StreamMatcher reset() {
        position = 0;
        status = NONE;
        return this;
    }

    @Override
    public MatchingStatus matching() {
        return status;
    }

    @Override
    public String transform(final String buffer) {
        return textTransformer.apply(buffer);
    }

}
