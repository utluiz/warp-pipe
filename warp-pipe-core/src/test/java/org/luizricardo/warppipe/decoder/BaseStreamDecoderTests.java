package org.luizricardo.warppipe.decoder;

import org.junit.Test;
import org.luizricardo.warppipe.listener.StreamListener;
import org.luizricardo.warppipe.matcher.HtmlCloseTagStreamMatcher;
import org.luizricardo.warppipe.matcher.HtmlTagStreamMatcher;
import org.luizricardo.warppipe.matcher.TextStreamMatcher;

public abstract class BaseStreamDecoderTests {

    TextStreamMatcher matcher = TextStreamMatcher.forText("bla", true);
    StreamDecoderBuilder builder;

    //replaces buffer
    StreamListener addPar = context -> context.content().insert(0, "(").append(")");

    //clear buffer and writes directly to the output
    StreamListener addBraq = context -> {
        context.output().write("[" + context.content() + "]");
        context.clear();
    };

    @Test
    public void simplestSanityCheck() throws Exception {
        writeBytesAndCheckResult(builder, "123", "123");
    }

    @Test
    public void decodeSpecialChars() throws Exception {
        writeBytesAndCheckResult(builder, "éÁñ", "éÁñ");
    }

    @Test
    public void simpleTextReplacementUsingBuffer() throws Exception {
        TextStreamMatcher matcher = TextStreamMatcher.forText("bla", true);
        writeBytesAndCheckResult(builder.bind(matcher, addPar), "lablala", "la(bla)la");
    }

    @Test
    public void simpleTextReplacementUsingOUtput() throws Exception {
        TextStreamMatcher matcher = TextStreamMatcher.forText("bla", true);
        writeBytesAndCheckResult(builder.bind(matcher, addBraq), "lablala", "la[bla]la");
    }

    @Test
    public void multipleMatchersFirstMatching() throws Exception {
        TextStreamMatcher matcher2 = TextStreamMatcher.forText("blu", true);
        writeBytesAndCheckResult(builder.bind(matcher, addPar).bind(matcher2, addBraq),
                "lablala", "la(bla)la");
    }

    @Test
    public void multipleMatchersSecondMatching() throws Exception {
        TextStreamMatcher matcher2 = TextStreamMatcher.forText("blu", true);
        writeBytesAndCheckResult(builder.bind(matcher, addPar).bind(matcher2, addBraq),
                "lablula", "la[blu]la");
    }

    @Test
    public void multipleMatchersFirstMatchingDifferentOrder() throws Exception {
        TextStreamMatcher matcher2 = TextStreamMatcher.forText("bla", true);
        writeBytesAndCheckResult(builder.bind(matcher2, addBraq).bind(matcher, addPar),
                "lablala", "la[bla]la");
    }

    @Test
    public void multipleSimultaneousMatchingSmaller() throws Exception {
        TextStreamMatcher matcher2 = TextStreamMatcher.forText("bla2", true);
        writeBytesAndCheckResult(builder.bind(matcher, addPar).bind(matcher2, addBraq),
                "labla2la", "la(bla)2la");
    }

    @Test
    public void multipleSimultaneousMatchingOnePartiallyThenOtherFully() throws Exception {
        TextStreamMatcher matcher2 = TextStreamMatcher.forText("lol", true);
        writeBytesAndCheckResult(builder.bind(matcher, addPar).bind(matcher2, addBraq),
                "lablola", "lab[lol]a");
    }

    @Test
    public void multipleSequence() throws Exception {
        TextStreamMatcher matcher2 = TextStreamMatcher.forText("lol", true);
        writeBytesAndCheckResult(builder.bind(matcher, addPar).bind(matcher2, addBraq),
                "lablalolbla", "la(bla)[lol](bla)");
    }

    @Test
    public void multipleOneNotEnoughBuffer() throws Exception {
        TextStreamMatcher matcher2 = TextStreamMatcher.forText("lol2", true);
        writeBytesAndCheckResult(builder.bufferLimit(3).bind(matcher, addPar).bind(matcher2, addBraq),
                "lablalol2bla", "la(bla)lol2(bla)");
    }

    @Test
    public void multipleSimultaneousOneNotEnoughBuffer() throws Exception {
        TextStreamMatcher matcher2 = TextStreamMatcher.forText("labla", true);
        writeBytesAndCheckResult(builder.bufferLimit(3).bind(matcher, addPar).bind(matcher2, addBraq),
                "lablalol2bla", "la(bla)lol2(bla)");
    }

    @Test
    public void notEnoughBuffer() throws Exception {
        writeBytesAndCheckResult(builder.bufferLimit(2).bind(matcher, addPar),"blblala", "blblala");
    }

    @Test
    public void generalTestWithHtml() throws Exception {
        String htmlStart = "<!DOCTYPE html>\n<html>\n\t<head>\n\t\t<script/>\n\n\n\t</HeAd>\n<body>\n" +
                "\n<h1>My First Heading</h1>\n\n\"";
        String myScript = "<script src=\"myscrip\"/>";
        String realContent = "<p>Real content here.</p>";
        String originalHtml = htmlStart + "<placeholder id=\"slow-area\"/>\n\n</body>\n</html>\n";
        String expectedHtml = htmlStart + realContent + "\n\n" + myScript + "</body>\n</html>\n";
        HtmlCloseTagStreamMatcher closingHeadMatcher = HtmlCloseTagStreamMatcher.forTag("head");
        HtmlCloseTagStreamMatcher closingBodyMatcher = HtmlCloseTagStreamMatcher.forTag("body");
        HtmlTagStreamMatcher bigPipePlaceholderMatcher = HtmlTagStreamMatcher.forTag("placeholder");
        writeBytesAndCheckResult(builder
                        .bind(bigPipePlaceholderMatcher, content -> content.content().replace(0, content.content().length(), realContent) )
                        .bind(closingBodyMatcher, context -> context.content().insert(0, myScript) )
                        .bind(closingHeadMatcher, context -> {}),
                originalHtml, expectedHtml);
    }

    abstract void writeBytesAndCheckResult(StreamDecoderBuilder builder, String text, String expected) throws Exception;
    
}
