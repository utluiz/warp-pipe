package org.luizricardo.warppipe.matcher;


/**
 * Base class for HTML matching.
 * It does not process contents, but finishes when finds a '{@code >}' character.
 */
public abstract class AbstractHtmlTagStreamMatcher implements StreamMatcher {

    private final String tagName;
    private final String initialText;
    private final int initialTextLength;
    private final TextStreamMatcher initialMatcher;

    public AbstractHtmlTagStreamMatcher(final String tagName, final String initialText) {
        this.tagName = tagName;
        this.initialText = initialText;
        this.initialTextLength = initialText.length();
        this.initialMatcher = new TextStreamMatcher(initialText, false);
    }


    @Override
    public MatchingStatus matches(StringBuilder stringBuilder) {
        if (stringBuilder.charAt(0) == '<') {
            final int length = stringBuilder.length();
            if (length == 1) {
                return MatchingStatus.FIRST;
            } else if (length <= initialTextLength) {
                return initialMatcher.matches(stringBuilder) == MatchingStatus.NONE ? MatchingStatus.NONE : MatchingStatus.PARTIALLY;
            } else if (stringBuilder.charAt(length - 1) == '>') {
                return matchesTag(tagName, stringBuilder);
            } else if (stringBuilder.charAt(length - 1) == '<') {
                return MatchingStatus.FIRST;
            } else {
                return MatchingStatus.PARTIALLY;
            }
        }
        return MatchingStatus.NONE;
    }

    protected abstract MatchingStatus matchesTag(String tagName, StringBuilder stringBuilder);

}
