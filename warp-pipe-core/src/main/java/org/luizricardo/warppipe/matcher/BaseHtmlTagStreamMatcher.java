package org.luizricardo.warppipe.matcher;


/**
 * Base class for HTML matching.
 * It does not process contents, but finishes when finds a '{@code >}' character.
 */
public abstract class BaseHtmlTagStreamMatcher implements StreamMatcher {

    private final String tagName;
    private final int initialTextLength;
    private final TextStreamMatcher initialMatcher;

    public BaseHtmlTagStreamMatcher(final String tagName, final String initialText) {
        this.tagName = tagName;
        this.initialTextLength = initialText.length();
        this.initialMatcher = TextStreamMatcher.forText(initialText, false);
    }

    @Override
    public MatchingStatus matches(final StringBuilder stringBuilder) {
        if (stringBuilder.charAt(0) == '<') {
            final int length = stringBuilder.length();
            if (length == 1) {
                return MatchingStatus.FIRST;
            } else if (length <= initialTextLength) {
                return initialMatcher.matches(stringBuilder) == MatchingStatus.NONE ? MatchingStatus.NONE : MatchingStatus.PARTIALLY;
            } else if (stringBuilder.charAt(length - 1) == '>') {
                return matchesTag(stringBuilder);
            } else if (stringBuilder.charAt(length - 1) == '<') {
                return MatchingStatus.FIRST;
            } else {
                return MatchingStatus.PARTIALLY;
            }
        }
        return MatchingStatus.NONE;
    }

    public String tagName() {
        return tagName;
    }

    /**
     * Implements matching for this tag when it should have a complete tag like {@code <...>}.
     * @param stringBuilder tagContent
     */
    public abstract MatchingStatus matchesTag(final StringBuilder stringBuilder);

}
