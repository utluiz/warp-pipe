package org.luizricardo.warppipe.matcher;


import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;

import java.util.List;

/**
 * Matches a opening HTML tag like {@code <a>} or an enclosing tag like {@code <br/>}.
 * It does not process inner tags, but finishes when finds a '{@code >}' character.
 */
public class HtmlTagStreamMatcher extends AbstractHtmlTagStreamMatcher {

    public HtmlTagStreamMatcher(final String tagName) {
        super(tagName, "<" + tagName);
    }

    @Override
    protected MatchingStatus matchesTag(final String tagName, final StringBuilder stringBuilder){
        final Source source = new Source(stringBuilder);
        final List<StartTag> all = source.getAllStartTags();
        if (!all.isEmpty() && all.get(0).getName().equals(tagName)) {
            return MatchingStatus.FULLY;
        } else {
            return MatchingStatus.NONE;
        }
    }

}
