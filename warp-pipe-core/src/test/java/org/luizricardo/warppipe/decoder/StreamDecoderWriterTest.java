package org.luizricardo.warppipe.decoder;

import org.junit.Assert;
import org.junit.Before;

import java.io.StringWriter;

import static org.hamcrest.CoreMatchers.is;

public class StreamDecoderWriterTest extends BaseStreamDecoderTests {

    StringWriter writer = new StringWriter();

    @Before
    public void setup() {
        builder = StreamDecoder.Builder.forWriter(writer);
    }

    void writeBytesAndCheckResult(StreamDecoder.Builder builder, String text, String expected) throws Exception {
        StreamDecoderWriter target = (StreamDecoderWriter) builder.build();
        for (char c : text.toCharArray()) {
            target.write(c);
        }
        target.close();
        String result = this.writer.toString();
        Assert.assertThat(result, is(expected));
    }

}
