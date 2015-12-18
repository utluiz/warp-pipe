package org.luizricardo.warppipe;

public interface StreamMatcher {

    StreamMatcher matchNext(byte b);

    StreamMatcher reset();

    MatchingStatus matching();

    String transform(String buffer);

}
