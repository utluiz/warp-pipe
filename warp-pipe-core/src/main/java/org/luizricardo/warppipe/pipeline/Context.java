package org.luizricardo.warppipe.pipeline;

import javax.servlet.http.HttpServletRequest;
import java.io.Writer;


public class Context {

    private final Writer writer;
    private final HttpServletRequest request;

    public Context(final Writer writer, final HttpServletRequest request) {
        this.writer = writer;
        this.request = request;
    }

    public Writer writer() {
        return writer;
    }

    public HttpServletRequest request() {
        return request;
    }

}
