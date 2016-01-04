package org.luizricardo.warppipe;

import org.junit.Before;
import org.junit.Test;
import org.luizricardo.warppipe.fakes.FakeFilterChain;
import org.luizricardo.warppipe.fakes.FakeFilterConfig;
import org.luizricardo.warppipe.fakes.FakeHttpServletRequest;
import org.luizricardo.warppipe.fakes.FakeHttpServletResponse;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class WarpFilterTest {

    DefaultWarpFilter filter = new DefaultWarpFilter();
    FakeHttpServletRequest request = new FakeHttpServletRequest();
    FakeHttpServletResponse response = new FakeHttpServletResponse();

    @Before
    public void setup() {
    }

    @Test
    public void plainHtmlNoFilter() throws Exception {
        filter.init(new FakeFilterConfig());
        doFilterWithOutputStream("Olá, mundo!!!");
        assertEquals("Olá, mundo!!!", new String(response.getOutput().toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    public void simpleHtmlFilter() throws Exception {
        filter.init(new FakeFilterConfig().param("steps", "text=org.luizricardo.warppipe.TextStep"));
        doFilterWithOutputStream("<body>bla<placeholder id=\"text\"/></body>");
        assertEquals("<body>bla <placeholder id=\"text\"/>NONE</body>", new String(response.getOutput().toByteArray(), StandardCharsets.UTF_8));
    }

    void doFilterWithOutputStream(final String content) throws Exception {
        filter.doFilter(request, response, FakeFilterChain.of((req, res) -> {
            try {
                byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
                final ServletOutputStream out = response.getOutputStream();
                for (byte b : bytes) {
                    out.write(b);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
    }

}
