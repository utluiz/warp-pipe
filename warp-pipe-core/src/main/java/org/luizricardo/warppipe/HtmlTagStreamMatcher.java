package org.luizricardo.warppipe;


import java.nio.charset.Charset;
import java.util.function.Function;

import static org.luizricardo.warppipe.MatchingStatus.FULLY;
import static org.luizricardo.warppipe.MatchingStatus.NONE;
import static org.luizricardo.warppipe.MatchingStatus.PARTIALLY;

public class HtmlTagStreamMatcher implements StreamMatcher {

    private final TextStreamMatcher startingMatcher;
    private final TextStreamMatcher endindMatcher;
    private final Function<String, String> textTransformer;
    private MatchingStatus status;
    private boolean matchedSpace;

    public HtmlTagStreamMatcher(final String tagName, final Charset charset, final Function<String, String> textTransformer) {
        this.textTransformer = textTransformer;
        this.startingMatcher = new TextStreamMatcher("<" + tagName, charset, textTransformer);
        this.endindMatcher = new TextStreamMatcher(">", charset, textTransformer);
        this.status = NONE;
        this.matchedSpace = false;
    }

    public StreamMatcher matchNext(final byte b) {
        if (startingMatcher.matching() != FULLY) {
            //if starting matcher is not fully matching, keep trying
            switch (startingMatcher.matchNext(b).matching()) {
                case FULLY:
                case PARTIALLY:
                    status = PARTIALLY;
                    break;
                case NONE:
                    reset();
            }
        } else if (!matchedSpace) {

        } else {
            //when fully matched starting matcher, try match the ending matcher
            switch (endindMatcher.matchNext(b).matching()) {
                case FULLY:
                    reset();
                    status = FULLY;
                    break;
                case PARTIALLY:
                    status = PARTIALLY;
                    break;
            }
        }
        return this;
    }

    @Override
    public StreamMatcher reset() {
        startingMatcher.reset();
        endindMatcher.reset();
        matchedSpace = false;
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
