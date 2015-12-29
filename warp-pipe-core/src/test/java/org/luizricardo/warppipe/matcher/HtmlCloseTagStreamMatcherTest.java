package org.luizricardo.warppipe.matcher;


import org.junit.Test;

import static org.luizricardo.warppipe.matcher.MatchingStatus.FIRST;
import static org.luizricardo.warppipe.matcher.MatchingStatus.FULLY;
import static org.luizricardo.warppipe.matcher.MatchingStatus.PARTIALLY;
import static org.luizricardo.warppipe.matcher.TextStreamMatcherTest.performAndAssertMatching;


public class HtmlCloseTagStreamMatcherTest {

    HtmlCloseTagStreamMatcher matcher = new HtmlCloseTagStreamMatcher("table");

    @Test
    public void matchSimple() {
        performAndAssertMatching("</table>", matcher, FIRST, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, FULLY);
    }

    @Test
    public void matchWithSpace() {
        performAndAssertMatching("</table >", matcher, FIRST, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, FULLY);
    }

}
