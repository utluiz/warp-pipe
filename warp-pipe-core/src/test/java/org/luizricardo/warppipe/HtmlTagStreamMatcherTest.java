package org.luizricardo.warppipe;


import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.luizricardo.warppipe.MatchingStatus.*;


public class HtmlTagStreamMatcherTest {

    HtmlTagStreamMatcher matcher = new HtmlTagStreamMatcher("body", StandardCharsets.UTF_8, (s) -> "head");

    @Test
    public void matchSimple() {
        performAndAssertMatching("<body>", matcher, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, FULLY);
    }

    @Test
    public void matchWithSpace() {
        performAndAssertMatching("<body >", matcher, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, FULLY);
    }

    @Test
    public void failFromBeginning() {
        performAndAssertMatching("body", matcher, NONE, NONE, NONE, NONE);
    }

    @Test
    public void failInTheMiddle() {
        performAndAssertMatching("<bady>", matcher, PARTIALLY, PARTIALLY, NONE, NONE, NONE, NONE);
    }

    @Test
    public void failInTheEnd() {
        performAndAssertMatching("<body2", matcher, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, NONE);
    }

    @Test
    public void matchInTheMiddle() {
        performAndAssertMatching("<head><body><html>", matcher, PARTIALLY, NONE, NONE, NONE, NONE, NONE,
                PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, FULLY, PARTIALLY, NONE, NONE, NONE, NONE, NONE);
    }

    @Test
    public void matchInTheMiddleAndPartiallyInTheEnd() {
        performAndAssertMatching("<head><body><html><bod>", matcher, PARTIALLY, NONE, NONE, NONE, NONE, NONE,
                PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, FULLY, PARTIALLY, NONE, NONE, NONE, NONE, NONE,
                PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, NONE);
    }

    @Test
    public void matchInSequence() {
        performAndAssertMatching("<body><body>", matcher, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, FULLY,
                PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, PARTIALLY, FULLY);
    }

    @Test
    public void matchMultiByteCharacters() {
        performAndAssertMatching("é<é><>é", new HtmlTagStreamMatcher("é", StandardCharsets.UTF_8, (s) -> "la"),
                NONE, PARTIALLY, PARTIALLY, FULLY, PARTIALLY, NONE, NONE);
    }
//
//    @Test
//    public void matchMoreMultiByteCharacters() {
//        performAndAssertMatching("bláéblá", new TextStreamMatcher("blá", StandardCharsets.UTF_8, (s) -> "la"),
//                PARTIALLY, PARTIALLY, FULLY, NONE, PARTIALLY, PARTIALLY, FULLY);
//    }

//    @Test
//    public void simpleTransformation() {
//        assertEquals("la", matcher.transform("text"));
//    }

    private void performAndAssertMatching(String textToMatch, StreamMatcher matcher, MatchingStatus... matchingStatuses) {
        for (int i = 0, l = textToMatch.length(); i < l; i++) {
            byte[] bytes = textToMatch.substring(i, i+1).getBytes(StandardCharsets.UTF_8);
            for (int j = 0; j < bytes.length; j++) {
                MatchingStatus expected = matchingStatuses[i];
                if (expected == FULLY & j < bytes.length - 1) {
                    expected = PARTIALLY;
                }
                assertEquals(
                        String.format("Failed to match '%s' at char '%c' pos %d", textToMatch, textToMatch.charAt(i), i + 1),
                        expected, matcher.matchNext(bytes[j]).matching());
            }
        }
    }

}
