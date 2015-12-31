package org.luizricardo.warppipe.matcher;


import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;

import java.util.List;

/**
 * Matches a opening HTML tag like {@code <a>} or an enclosing tag like {@code <br/>}.
 * It does not process inner tags, but finishes when finds a '{@code >}' character.
 */
public class HtmlTagStreamMatcher extends BaseHtmlTagStreamMatcher {

    private HtmlTagStreamMatcher(final String tagName) {
        super(tagName, "<" + tagName);
    }

    /**
     * Builds a {@link HtmlTagStreamMatcher}.
     */
    public static HtmlTagStreamMatcher forTag(final String tagName) {
        return new HtmlTagStreamMatcher(tagName);
    }

    @Override
    public MatchingStatus matchesTag(final StringBuilder stringBuilder){
        return findStartTag(stringBuilder) != null ? MatchingStatus.FULLY : MatchingStatus.NONE;
    }

    public StartTag findStartTag(final StringBuilder stringBuilder) {
        final Source source = new Source(stringBuilder);
        final List<StartTag> all = source.getAllStartTags();
        if (!all.isEmpty() && all.get(0).getName().equals(tagName())) {
            return all.get(0);
        }
        return null;
    }

}
