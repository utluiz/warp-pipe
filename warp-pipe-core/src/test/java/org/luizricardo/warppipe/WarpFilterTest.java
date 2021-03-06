package org.luizricardo.warppipe;

import org.junit.Before;
import org.junit.Test;
import org.luizricardo.warppipe.api.Step;
import org.luizricardo.warppipe.fakes.FakeFilterChain;
import org.luizricardo.warppipe.fakes.FakeHttpServletRequest;
import org.luizricardo.warppipe.fakes.FakeHttpServletResponse;
import org.luizricardo.warppipe.pipeline.step.DefaultStepManager;
import org.luizricardo.warppipe.pipeline.step.TextStep;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class WarpFilterTest {

    WarpFilter filter;
    FakeHttpServletRequest request = new FakeHttpServletRequest();
    FakeHttpServletResponse response = new FakeHttpServletResponse();

    @Before
    public void setup() {
        Map<String, Step> stepMap = new HashMap<>();
        stepMap.put("text", new TextStep());
        filter = new WarpFilter(
                new WarpFilterConfiguration(StandardCharsets.UTF_8, true, true, true),
                new DefaultStepManager(stepMap));
    }

    @Test
    public void plainHtmlNoFilter() throws Exception {
        doFilterWithOutputStream("Olá, mundo!!!");
        assertEquals("Olá, mundo!!!", new String(response.getOutput().toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    public void simpleHtmlFilter() throws Exception {
        doFilterWithOutputStream("<body>bla <placeholder id=\"text\"/></body>");
        assertEquals("<body>bla <placeholder id=\"text\"/>NONE</body>", new String(response.getOutput().toByteArray(), StandardCharsets.UTF_8));
    }

    void doFilterWithOutputStream(final String content) throws Exception {
        filter.filter(request, response, FakeFilterChain.of((req, res) -> {
            try {
                byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
                final ServletOutputStream out = res.getOutputStream();
                for (byte b : bytes) {
                    out.write(b);
                }
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
    }

}
