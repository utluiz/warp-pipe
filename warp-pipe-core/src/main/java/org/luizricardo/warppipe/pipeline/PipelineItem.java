package org.luizricardo.warppipe.pipeline;

import javax.servlet.http.HttpServletRequest;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;


public class PipelineItem {

    private final String id;
    private final Integer priority;
    private final Map<String, String> attributes;
    private final Writer writer;
    private final HttpServletRequest request;

    public PipelineItem(final String id, final Integer priority, final Map<String, String> attributes, final Writer writer,
                        final HttpServletRequest request) {
        this.id = id;
        this.priority = priority;
        this.writer = writer;
        this.request = request;
        this.attributes = Collections.unmodifiableMap(attributes);
    }

    public String id() {
        return id;
    }

    public Integer priority() {
        return priority;
    }

    public Map<String, String> attributes() {
        return attributes;
    }

    public Writer writer() {
        return writer;
    }

    public HttpServletRequest request() {
        return request;
    }

}
