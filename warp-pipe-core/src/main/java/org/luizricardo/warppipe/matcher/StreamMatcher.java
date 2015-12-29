package org.luizricardo.warppipe.matcher;

import org.luizricardo.warppipe.OutputStreamDecoder;

/**
 * A StreamMatcher is an interface designed to identify text from a stream os characters, to be used by
 * {@link OutputStreamDecoder}.
 * It'll receive a partial increading buffer of characters and should return a status of how it matches.
 * <p>
 *     Firstly, it'll get a buffer with one single character. If this matcher recognizes that character but it's not yet
 *     the complete text it's aiming, it should return {@link MatchingStatus#FIRST}.
 * </p>
 * <p>
 *     After that, each subsequent call will add characters to the buffer. It the buffer still matches but not yet
 *     it reaches the whole text, it should return {@link MatchingStatus#PARTIALLY}.
 * </p>
 * <p>
 *     When at least the buffer corresponds completely to the expected content, it should return {@link MatchingStatus#FULLY}.
 * </p>
 * <p>
 *     At any point, if the buffer does not correspond to that this matcher is aiming, it should return {@link MatchingStatus#NONE}.
 *     After that, the buffer will reset and start again.
 * </p>
 */
public interface StreamMatcher {

    MatchingStatus matches(StringBuilder stringBuilder);

}
