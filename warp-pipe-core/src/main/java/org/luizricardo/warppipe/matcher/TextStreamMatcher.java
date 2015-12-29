package org.luizricardo.warppipe.matcher;


import static org.luizricardo.warppipe.matcher.MatchingStatus.FIRST;
import static org.luizricardo.warppipe.matcher.MatchingStatus.FULLY;
import static org.luizricardo.warppipe.matcher.MatchingStatus.NONE;
import static org.luizricardo.warppipe.matcher.MatchingStatus.PARTIALLY;

/**
 * Matches a static text.
 */
public class TextStreamMatcher implements StreamMatcher {

    private final String text;
    private final boolean caseSensitive;
    private final int textLength;

    public TextStreamMatcher(final String text, boolean caseSensitive) {
        this.text = text;
        this.caseSensitive = caseSensitive;
        this.textLength = text.length();
    }

    @Override
    public MatchingStatus matches(final StringBuilder stringBuilder) {
        final int sbLength = stringBuilder.length();
        final String str = stringBuilder.toString();
        if (sbLength == textLength && compare(str, text)) {
            return FULLY;
        } else if (sbLength < textLength && compare(str, text.substring(0, sbLength))) {
            return sbLength == 1 ? FIRST : PARTIALLY;
        } else {
            return NONE;
        }
    }

    private boolean compare(String s1, String s2) {
        return caseSensitive ? s1.equals(s2) : s1.equalsIgnoreCase(s2);
    }
}
