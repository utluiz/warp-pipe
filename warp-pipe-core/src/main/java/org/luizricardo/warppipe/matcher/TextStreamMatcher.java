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

    private TextStreamMatcher(final String text, boolean caseSensitive) {
        this.text = text;
        this.caseSensitive = caseSensitive;
        this.textLength = text.length();
    }

    /**
     * Builds a {@link TextStreamMatcher}.
     * @param text Text to match.
     * @param caseSensitive Will ignore case when false.
     */
    public static TextStreamMatcher forText(final String text, boolean caseSensitive) {
        return new TextStreamMatcher(text, caseSensitive);
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

    /**
     * Compare considering case-sensitivity options
     */
    private boolean compare(final String s1, final String s2) {
        return caseSensitive ? s1.equals(s2) : s1.equalsIgnoreCase(s2);
    }

}
