package org.luizricardo.warppipe.matcher;

/**
 * Status of a current stream matcher.
 */
public enum MatchingStatus {
    /**
     * Does not match current buffer.
     */
    NONE,
    /**
     * Begin of matching, i.e., matched the first character.
     */
    FIRST,
    /**
     * Matched more than one character, but not yet fully matching.
     */
    PARTIALLY,
    /**
     * Matcher fully matching the current buffer..
     */
    FULLY
}