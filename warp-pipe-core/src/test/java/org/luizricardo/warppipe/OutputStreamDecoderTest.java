package org.luizricardo.warppipe;

import org.junit.Assert;
import org.junit.Test;
import org.luizricardo.warppipe.matcher.HtmlCloseTagStreamMatcher;
import org.luizricardo.warppipe.matcher.HtmlTagStreamMatcher;
import org.luizricardo.warppipe.matcher.TextStreamMatcher;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;

public class OutputStreamDecoderTest {

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    TextStreamMatcher matcher = new TextStreamMatcher("bla", true);
    OutputStreamDecoder.Builder builder = OutputStreamDecoder.Builder.create(baos, StandardCharsets.UTF_8);

    @Test
    public void simplestSanityCheck() throws Exception {
        writeBytesAndCheckResult(builder.build(), "123", "123");
    }

    @Test
    public void decodeSpecialChars() throws Exception {
        writeBytesAndCheckResult(builder.build(), "éÁñ", "éÁñ");
    }

    @Test
    public void simpleTextReplacement() throws Exception {
        TextStreamMatcher matcher = new TextStreamMatcher("bla", true);
        writeBytesAndCheckResult(builder.bind(matcher, sb -> sb.insert(0, '(').append(')')).build(), "lablala", "la(bla)la");
    }

    @Test
    public void multipleMatchersFirstMatching() throws Exception {
        TextStreamMatcher matcher2 = new TextStreamMatcher("blu", true);
        writeBytesAndCheckResult(builder
                        .bind(matcher, sb -> sb.insert(0, '(').append(')'))
                        .bind(matcher2, sb -> sb.insert(0, '[').append(']')).build(),
                "lablala", "la(bla)la");
    }

    @Test
    public void multipleMatchersSecondMatching() throws Exception {
        TextStreamMatcher matcher2 = new TextStreamMatcher("blu", true);
        writeBytesAndCheckResult(builder
                        .bind(matcher, sb -> sb.insert(0, '(').append(')'))
                        .bind(matcher2, sb -> sb.insert(0, '[').append(']')).build(),
                "lablula", "la[blu]la");
    }

    @Test
    public void multipleMatchersFirstMatchingDifferentOrder() throws Exception {
        TextStreamMatcher matcher2 = new TextStreamMatcher("bla", true);
        writeBytesAndCheckResult(builder
                        .bind(matcher2, sb -> sb.insert(0, '[').append(']'))
                        .bind(matcher, sb -> sb.insert(0, '(').append(')')).build(),
                "lablala", "la[bla]la");
    }

    @Test
    public void multipleSimultaneousMatchingSmaller() throws Exception {
        TextStreamMatcher matcher2 = new TextStreamMatcher("bla2", true);
        writeBytesAndCheckResult(builder
                        .bind(matcher, sb -> sb.insert(0, '(').append(')'))
                        .bind(matcher2, sb -> sb.insert(0, '[').append(']')).build(),
                "labla2la", "la(bla)2la");
    }

    @Test
    public void multipleSimultaneousMatchingOnePartiallyThenOtherFully() throws Exception {
        TextStreamMatcher matcher2 = new TextStreamMatcher("lol", true);
        writeBytesAndCheckResult(builder
                        .bind(matcher, sb -> sb.insert(0, '(').append(')'))
                        .bind(matcher2, sb -> sb.insert(0, '[').append(']')).build(),
                "lablola", "lab[lol]a");
    }

    @Test
    public void multipleSequence() throws Exception {
        TextStreamMatcher matcher2 = new TextStreamMatcher("lol", true);
        writeBytesAndCheckResult(builder
                        .bind(matcher, sb -> sb.insert(0, '(').append(')'))
                        .bind(matcher2, sb -> sb.insert(0, '[').append(']')).build(),
                "lablalolbla", "la(bla)[lol](bla)");
    }

    @Test
    public void multipleOneNotEnoughBuffer() throws Exception {
        TextStreamMatcher matcher2 = new TextStreamMatcher("lol2", true);
        writeBytesAndCheckResult(builder.bufferLimit(3)
                        .bind(matcher, sb -> sb.insert(0, '(').append(')'))
                        .bind(matcher2, sb -> sb.insert(0, '[').append(']')).build(),
                "lablalol2bla", "la(bla)lol2(bla)");
    }

    @Test
    public void multipleSimultaneousOneNotEnoughBuffer() throws Exception {
        TextStreamMatcher matcher2 = new TextStreamMatcher("labla", true);
        writeBytesAndCheckResult(builder.bufferLimit(3)
                        .bind(matcher, sb -> sb.insert(0, '(').append(')'))
                        .bind(matcher2, sb -> sb.insert(0, '[').append(']')).build(),
                "lablalol2bla", "la(bla)lol2(bla)");
    }

    @Test
    public void notEnoughBuffer() throws Exception {
        writeBytesAndCheckResult(builder.bufferLimit(2)
                        .bind(matcher, sb -> sb.insert(0, '(').append(')')).build(),
                "blblala", "blblala");
    }

    @Test
    public void generalTestWithHtml() throws Exception {
        String htmlStart = "<!DOCTYPE html>\n<html>\n\t<head>\n\t\t<script/>\n\n\n\t</HeAd>\n<body>\n" +
                "\n<h1>My First Heading</h1>\n\n\"";
        String myScript = "<script src=\"myscrip\"/>";
        String realContent = "<p>Real content here.</p>";
        String originalHtml = htmlStart + "<placeholder id=\"slow-area\"/>\n\n</body>\n</html>\n";
        String expectedHtml = htmlStart + realContent + "\n\n" + myScript + "</body>\n</html>\n";
        HtmlCloseTagStreamMatcher closingHeadMatcher = new HtmlCloseTagStreamMatcher("head");
        HtmlCloseTagStreamMatcher closingBodyMatcher = new HtmlCloseTagStreamMatcher("body");
        HtmlTagStreamMatcher bigPipePlaceholderMatcher = new HtmlTagStreamMatcher("placeholder");
        writeBytesAndCheckResult(builder
                        .bind(bigPipePlaceholderMatcher, sb -> sb.replace(0, sb.length(), realContent) )
                        .bind(closingBodyMatcher, sb -> sb.insert(0, myScript) )
                        .bind(closingHeadMatcher, sb -> { } ).build(),
                originalHtml, expectedHtml);
    }

    void writeBytesAndCheckResult(OutputStream os, String text, String expected) throws Exception {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < bytes.length; i++) {
            os.write(bytes[i]);
        }
        os.close();
        String result = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        Assert.assertThat(result, is(expected));
    }

}
