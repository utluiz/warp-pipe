package org.luizricardo.warppipe;

import org.junit.Test;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class WarpFilterTest {

    WarpFilter filter = new WarpFilter();
    FakeHttpServletRequest request = new FakeHttpServletRequest();
    FakeHttpServletResponse response = new FakeHttpServletResponse();

    @Test
    public void plainHtmlNoFilter() throws Exception {
        doFilterWithOutputStream("Olá, mundo!!!");
        assertEquals("Olá, mundo!!!", new String(response.getOutput().toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    public void simpleHtmlFilter() throws Exception {
        filter.init(new FakeFilterConfig().param("matcher", "org.luizricardo.warppipe.HiBodyMatcher"));
        doFilterWithOutputStream("<body>bla</body>");
        assertEquals("<body>bla<hi/></body>", new String(response.getOutput().toByteArray(), StandardCharsets.UTF_8));
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
