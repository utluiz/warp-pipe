package org.luizricardo.warppipe.matcher;


import org.junit.Test;

import static org.luizricardo.warppipe.matcher.MatchingStatus.FIRST;
import static org.luizricardo.warppipe.matcher.MatchingStatus.FULLY;
import static org.luizricardo.warppipe.matcher.MatchingStatus.NONE;
import static org.luizricardo.warppipe.matcher.MatchingStatus.PARTIALLY;


public class HtmlTagStreamMatcherTest {

    HtmlTagStreamMatcher matcher = new HtmlTagStreamMatcher("table");

    @Test
    public void matchSimple() {
        TextStreamMatcherTest.performAndAssertMatching("<table>", matcher, FIRST, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, FULLY);
    }

    @Test
    public void matchWithSpace() {
        TextStreamMatcherTest.performAndAssertMatching("<table >", matcher, FIRST, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, FULLY);
    }

    @Test
    public void matchWithSpaceAndAutoClose() {
        TextStreamMatcherTest.performAndAssertMatching("<table />", matcher, FIRST, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, FULLY);
    }

    @Test
    public void matchWithAttribute() {
        TextStreamMatcherTest.performAndAssertMatching("<table id=\"10\">", matcher,
                FIRST, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY,
                PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, FULLY);
    }

    @Test
    public void failFromBeginning() {
        TextStreamMatcherTest.performAndAssertMatching("table", matcher, NONE, NONE, NONE, NONE, NONE);
    }

    @Test
    public void failInTheMiddle() {
        TextStreamMatcherTest.performAndAssertMatching("<tuble>", matcher, FIRST, PARTIALLY, NONE, NONE, NONE, NONE, NONE);
    }

    @Test
    public void failInTheEnd() {
        TextStreamMatcherTest.performAndAssertMatching("<table2>", matcher, FIRST, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, NONE);
    }

    @Test
    public void matchInTheMiddle() {
        TextStreamMatcherTest.performAndAssertMatching("<head><table><html>", matcher, FIRST, NONE, NONE, NONE, NONE, NONE,
                FIRST, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, FULLY,
                FIRST, NONE, NONE, NONE, NONE, NONE);
    }

    @Test
    public void matchInTheMiddleAndPartiallyInTheEnd() {
        TextStreamMatcherTest.performAndAssertMatching("<head><table><html><tab>", matcher, FIRST, NONE, NONE, NONE, NONE, NONE,
                FIRST, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, FULLY,
                FIRST, NONE, NONE, NONE, NONE, NONE,
                FIRST, PARTIALLY, PARTIALLY, PARTIALLY, NONE);
    }

    @Test
    public void matchInSequence() {
        TextStreamMatcherTest.performAndAssertMatching("<table><table>", matcher, FIRST, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, FULLY,
                FIRST, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, FULLY);
    }

    @Test
    public void matchMultiByteCharacters() {
        TextStreamMatcherTest.performAndAssertMatching("<bigpipe é=\"é\"/>", new HtmlTagStreamMatcher("bigpipe"),
                FIRST, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY,
                PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, FULLY);
    }

    @Test
    public void matchInvalidHtml() {
        TextStreamMatcherTest.performAndAssertMatching("<table <a>", matcher, FIRST, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY,
                PARTIALLY, FIRST, NONE, NONE);
    }

}
