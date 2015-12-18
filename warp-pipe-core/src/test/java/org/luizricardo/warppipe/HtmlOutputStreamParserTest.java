package org.luizricardo.warppipe;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class HtmlOutputStreamParserTest {

    @Test
    public void test() throws Exception {
        TextStreamMatcher matcher = new TextStreamMatcher("bla", StandardCharsets.UTF_8, p -> "("+p+")");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HtmlOutputStreamParser p = new HtmlOutputStreamParser(baos, Collections.singletonList(matcher), StandardCharsets.UTF_8);
        String text = "lablala";
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < bytes.length; i++) {
            p.write(bytes[i]);
        }
        p.close();
        Assert.assertThat(baos.toString(StandardCharsets.UTF_8.name()), CoreMatchers.is("la(bla)la"));
    }
}
