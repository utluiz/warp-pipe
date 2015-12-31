package org.luizricardo.warppipe;

import net.htmlparser.jericho.StartTag;
import org.luizricardo.warppipe.listener.MatchingContext;
import org.luizricardo.warppipe.listener.StreamListener;
import org.luizricardo.warppipe.matcher.HtmlCloseTagStreamMatcher;
import org.luizricardo.warppipe.matcher.HtmlTagStreamMatcher;
import org.luizricardo.warppipe.pipeline.ActionManager;
import org.luizricardo.warppipe.pipeline.DefaultActionManager;
import org.luizricardo.warppipe.pipeline.Pipeline;
import org.luizricardo.warppipe.pipeline.PipelineItem;
import org.luizricardo.warppipe.pipeline.QueuedPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class WarpFilter implements Filter {

    final static Logger logger = LoggerFactory.getLogger(WarpFilter.class);

    private WarpFilterConfiguration config;
    private HtmlCloseTagStreamMatcher closeHeadFlush;
    private HtmlTagStreamMatcher placeholderMapping;
    private HtmlCloseTagStreamMatcher closeBodyProcess;
    private ActionManager actionManager;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        this.config = new WarpFilterConfiguration(
                charset(filterConfig.getInitParameter("encoding")),
                booleanParam("flush-after-head", true),
                booleanParam("auto-execute-before-closing-body", true),
                booleanParam("auto-detect-placeholders", true));

        closeHeadFlush = HtmlCloseTagStreamMatcher.forTag("head");
        placeholderMapping = HtmlTagStreamMatcher.forTag("placeholder");
        closeBodyProcess = HtmlCloseTagStreamMatcher.forTag("body");
        actionManager = new DefaultActionManager();
    }

    private Charset charset(final String param) {
        if (param != null && !param.isEmpty()) {
            return Charset.forName(param);
        }
        return StandardCharsets.UTF_8;
    }

    private boolean booleanParam(final String param, final boolean defaultValue) {
        return param != null && !param.isEmpty() ? Boolean.valueOf(param) : defaultValue;
    }

    private PipelineItem buildPipelineItem(final MatchingContext content, final HttpServletRequest request) {
        final StartTag tag = placeholderMapping.findStartTag(content.content());
        if (tag != null) {
            return new PipelineItem(
                    tag.getAttributeValue("id"),
                    priority(tag.getAttributeValue("pipeline-priority")),
                    tag.getAttributes().populateMap(new HashMap<>(), true),
                    new BufferedWriter(
                        new Writer() {
                            @Override
                            public void write(char[] cbuf, int off, int len) throws IOException {
                                content.output().write(new String(cbuf, off, len));
                            }

                            @Override
                            public void flush() throws IOException {
                                content.output().flush();
                            }

                            @Override
                            public void close() throws IOException {
                                //nothing
                            }
                        }
                    ),
                    request);
        } else {
            throw new RuntimeException("Tag matched but not found later!");
        }
    }

    private Integer priority(String attr) {
        if (attr != null && !attr.isEmpty()) {
            try {
                return Integer.valueOf(attr);
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer in attribute pipeline-priority: {}", attr);
            }
        }
        return 1;
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response,
                         final FilterChain chain) throws IOException, ServletException {
        final Pipeline pipelineExecutor = new QueuedPipeline(actionManager);
        final HttpServletResponse responseWrapper = new DecodingHttpServletResponse(response, config.charset(),
                builder -> {
                    if (config.flushAfterHead()) {
                        builder.bind(closeHeadFlush, StreamListener.flushListener());
                    }
                    if (config.autoExecuteBeforeClosingBody()) {
                        builder.bind(closeBodyProcess, content -> pipelineExecutor.execute());
                    }
                    if (config.autoDetectPlaceholders()) {
                        builder.bind(placeholderMapping, content ->
                                pipelineExecutor.add(buildPipelineItem(content, (HttpServletRequest) request)));
                    }
                });
        chain.doFilter(request, responseWrapper);
    }

    @Override
    public void destroy() {
    }

}
