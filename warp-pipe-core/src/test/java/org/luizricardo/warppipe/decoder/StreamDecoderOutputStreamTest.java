package org.luizricardo.warppipe.decoder;

import org.junit.Assert;
import org.junit.Before;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;

public class StreamDecoderOutputStreamTest extends BaseStreamDecoderTests {

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    @Before
    public void setup() {
        builder = StreamDecoder.forOutputStream(baos, StandardCharsets.UTF_8);
    }


    void writeBytesAndCheckResult(StreamDecoderBuilder builder, String text, String expected) throws Exception {
        StreamDecoderOutputStream os = (StreamDecoderOutputStream) builder.build();
        final byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        for (byte aByte : bytes) {
            os.write(aByte);
        }
        os.close();
        String result = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        Assert.assertThat(result, is(expected));
    }

}
