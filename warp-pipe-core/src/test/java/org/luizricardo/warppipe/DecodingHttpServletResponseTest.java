package org.luizricardo.warppipe;


import org.junit.Assert;
import org.junit.Test;
import org.luizricardo.warppipe.decoder.StreamDecoderBuilder;
import org.luizricardo.warppipe.fakes.FakeHttpServletResponse;
import org.luizricardo.warppipe.matcher.TextStreamMatcher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DecodingHttpServletResponseTest {

    FakeHttpServletResponse response = new FakeHttpServletResponse();
    ByteArrayOutputStream output = response.getOutput();

    @Test
    public void stream() throws IOException {
        DecodingHttpServletResponse decodingResponse = new DecodingHttpServletResponse(response, StandardCharsets.UTF_8,
                StreamDecoderBuilder::build);
        decodingResponse.getOutputStream().write("Hello".getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals("Hello", new String(output.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    public void streamWithMatching() throws IOException {
        DecodingHttpServletResponse decodingResponse = new DecodingHttpServletResponse(response, StandardCharsets.UTF_8,
                builder -> builder.bind(TextStreamMatcher.forText("world", false), c -> c.clear().output().write("universe")).build());
        decodingResponse.getOutputStream().write("Hello world!".getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals("Hello universe!", new String(output.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    public void writer() throws IOException {
        DecodingHttpServletResponse decodingResponse = new DecodingHttpServletResponse(response, StandardCharsets.UTF_8,
                StreamDecoderBuilder::build);
        decodingResponse.getWriter().write("Hello");
        decodingResponse.getWriter().flush();
        Assert.assertEquals("Hello", new String(output.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    public void writerWithMatching() throws IOException {
        DecodingHttpServletResponse decodingResponse = new DecodingHttpServletResponse(response, StandardCharsets.UTF_8,
                builder -> builder.bind(TextStreamMatcher.forText("world", false), c -> c.clear().output().write("universe")).build());
        decodingResponse.getWriter().write("Hello world!");
        decodingResponse.getWriter().flush();
        Assert.assertEquals("Hello universe!", new String(output.toByteArray(), StandardCharsets.UTF_8));
    }

}
