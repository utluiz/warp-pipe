package org.luizricardo.warppipe.matcher;


import net.htmlparser.jericho.EndTagType;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.Tag;

import java.util.List;

/**
 * Matches closing tags like {@code </body>}.
 */
public class HtmlCloseTagStreamMatcher extends BaseHtmlTagStreamMatcher {

    private HtmlCloseTagStreamMatcher(final String tagName) {
        super(tagName, "</" + tagName);
    }

    /**
     * Builds a {@link HtmlCloseTagStreamMatcher}.
     */
    public static HtmlCloseTagStreamMatcher forTag(final String tagName) {
        return new HtmlCloseTagStreamMatcher(tagName);
    }

    @Override
    public MatchingStatus matchesTag(final StringBuilder stringBuilder) {
        final Source source = new Source(stringBuilder);
        final List<Tag> all = source.getAllTags();
        if (!all.isEmpty() && all.get(0).getName().equals(tagName()) && all.get(0).getTagType() instanceof EndTagType) {
            return MatchingStatus.FULLY;
        } else {
            return MatchingStatus.NONE;
        }
    }

}
