package org.luizricardo.warppipe;


import static org.junit.Assert.*;
import static org.luizricardo.warppipe.MatchingStatus.*;

import org.junit.Test;

import java.nio.charset.StandardCharsets;


public class TextStreamMatcherTest {

    TextStreamMatcher matcher = new TextStreamMatcher("bla", StandardCharsets.UTF_8, (s) -> "la");

    @Test
    public void matchSimple() {
        performAndAssertMatching("bla", matcher, PARTIALLY, PARTIALLY, FULLY);
    }

    @Test
    public void failFromBeginning() {
        performAndAssertMatching("cla", matcher, NONE, NONE, NONE);
    }

    @Test
    public void failInTheMiddle() {
        performAndAssertMatching("bra", matcher, PARTIALLY, NONE, NONE);
    }

    @Test
    public void failInTheEnd() {
        performAndAssertMatching("blu", matcher, PARTIALLY, PARTIALLY, NONE);
    }

    @Test
    public void matchInTheMiddle() {
        performAndAssertMatching("lablala", matcher, NONE, NONE, PARTIALLY, PARTIALLY, FULLY, NONE, NONE);
    }

    @Test
    public void matchInTheMiddleAndPartiallyInTheEnd() {
        performAndAssertMatching("lablalabl", matcher, NONE, NONE, PARTIALLY, PARTIALLY, FULLY, NONE, NONE, PARTIALLY, PARTIALLY);
    }

    @Test
    public void matchInSequence() {
        performAndAssertMatching("blabla", matcher, PARTIALLY, PARTIALLY, FULLY, PARTIALLY, PARTIALLY, FULLY);
    }

    @Test
    public void matchMultiByteCharacters() {
        performAndAssertMatching("blábla", new TextStreamMatcher("blá", StandardCharsets.UTF_8, (s) -> "la"),
                PARTIALLY, PARTIALLY, FULLY, PARTIALLY, PARTIALLY, NONE);
    }

    @Test
    public void matchMoreMultiByteCharacters() {
        performAndAssertMatching("bláéblá", new TextStreamMatcher("blá", StandardCharsets.UTF_8, (s) -> "la"),
                PARTIALLY, PARTIALLY, FULLY, NONE, PARTIALLY, PARTIALLY, FULLY);
    }

    @Test
    public void simpleTransformation() {
        assertEquals("la", matcher.transform("text"));
    }

    @Test
    public void resetting() {
        byte[] bytes = "bbla".getBytes(StandardCharsets.UTF_8);
        assertEquals(PARTIALLY, matcher.matchNext(bytes[0]).matching());
        matcher.reset();
        assertEquals(NONE, matcher.matching());
        performAndAssertMatching("bla", matcher, PARTIALLY, PARTIALLY, FULLY);
    }

    private void performAndAssertMatching(String textToMatch, StreamMatcher matcher, MatchingStatus... matchingStatuses) {
        for (int i = 0, l = textToMatch.length(); i < l; i++) {
            byte[] bytes = textToMatch.substring(i, i+1).getBytes(StandardCharsets.UTF_8);
            for (int j = 0; j < bytes.length; j++) {
                MatchingStatus expected = matchingStatuses[i];
                if (expected == FULLY & j < bytes.length - 1) {
                    expected = PARTIALLY;
                }
                assertEquals(
                        String.format("Failed to match '%s' at char %c pos %d", textToMatch, textToMatch.charAt(i), i + 1),
                        expected, matcher.matchNext(bytes[j]).matching());
            }
        }
    }

}
