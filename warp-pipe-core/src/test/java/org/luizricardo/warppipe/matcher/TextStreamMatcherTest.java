package org.luizricardo.warppipe.matcher;


import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.luizricardo.warppipe.matcher.MatchingStatus.FIRST;
import static org.luizricardo.warppipe.matcher.MatchingStatus.FULLY;
import static org.luizricardo.warppipe.matcher.MatchingStatus.NONE;
import static org.luizricardo.warppipe.matcher.MatchingStatus.PARTIALLY;


public class TextStreamMatcherTest {

    TextStreamMatcher matcher = new TextStreamMatcher("bla", true);

    @Test
    public void matchSimple() {
        performAndAssertMatching("bla", matcher, FIRST, PARTIALLY, FULLY);
    }

    @Test
    public void failFromBeginning() {
        performAndAssertMatching("cla", matcher, NONE, NONE, NONE);
    }

    @Test
    public void failInTheMiddle() {
        performAndAssertMatching("bra", matcher, FIRST, NONE, NONE);
    }

    @Test
    public void failInTheEnd() {
        performAndAssertMatching("blu", matcher, FIRST, PARTIALLY, NONE);
    }

    @Test
    public void matchInTheMiddle() {
        performAndAssertMatching("lablala", matcher, NONE, NONE, FIRST, PARTIALLY, FULLY, NONE, NONE);
    }

    @Test
    public void matchInTheMiddleAndPartiallyInTheEnd() {
        performAndAssertMatching("lablalabl", matcher, NONE, NONE, FIRST, PARTIALLY, FULLY, NONE, NONE, FIRST, PARTIALLY);
    }

    @Test
    public void matchInSequence() {
        performAndAssertMatching("blabla", matcher, FIRST, PARTIALLY, FULLY, FIRST, PARTIALLY, FULLY);
    }

    @Test
    public void matchMultiByteCharacters() {
        performAndAssertMatching("blábla", new TextStreamMatcher("blá", true),
                FIRST, PARTIALLY, FULLY, FIRST, PARTIALLY, NONE);
    }

    @Test
    public void matchMoreMultiByteCharacters() {
        performAndAssertMatching("bláéblá", new TextStreamMatcher("blá", true),
                FIRST, PARTIALLY, FULLY, NONE, FIRST, PARTIALLY, FULLY);
    }

    @Test
    public void matchCaseInsensitive() {
        performAndAssertMatching("bláéblá", new TextStreamMatcher("BlÁ", false),
                FIRST, PARTIALLY, FULLY, NONE, FIRST, PARTIALLY, FULLY);
    }

    @Test
    public void matchSingleCharacter() {
        performAndAssertMatching("uuxuxX", new TextStreamMatcher("X", false),
                NONE, NONE, FULLY, NONE, FULLY, FULLY);
    }

    static void performAndAssertMatching(String textToMatch, StreamMatcher matcher, MatchingStatus... matchingStatuses) {
        int lastMatching = 0;
        for (int i = 0, l = textToMatch.length(); i < l; i++) {
            String currentText = textToMatch.substring(lastMatching, i + 1);
            MatchingStatus result = matcher.matches(new StringBuilder(currentText));
            assertEquals(
                    String.format("Failed to match '%s' at char %c pos %d for text '%s'",
                            textToMatch, textToMatch.charAt(i), i + 1, currentText),
                    matchingStatuses[i], result);
            if (result == NONE || result == FULLY) {
                lastMatching = i + 1;
            } else if (result == FIRST) {
                lastMatching = i;
            }
        }
    }

}
