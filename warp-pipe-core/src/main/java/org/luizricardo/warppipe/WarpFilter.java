package org.luizricardo.warppipe;

import net.htmlparser.jericho.StartTag;
import org.luizricardo.warppipe.listener.MatchingContext;
import org.luizricardo.warppipe.listener.StreamListener;
import org.luizricardo.warppipe.matcher.HtmlCloseTagStreamMatcher;
import org.luizricardo.warppipe.matcher.HtmlTagStreamMatcher;
import org.luizricardo.warppipe.pipeline.Context;
import org.luizricardo.warppipe.pipeline.step.StepData;
import org.luizricardo.warppipe.pipeline.step.StepManager;
import org.luizricardo.warppipe.pipeline.QueuedPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Optional;

public class WarpFilter {

    final static Logger logger = LoggerFactory.getLogger(WarpFilter.class);

    private final WarpFilterConfiguration config;
    private final HtmlCloseTagStreamMatcher closeHeadFlush;
    private final HtmlTagStreamMatcher placeholderMapping;
    private final HtmlCloseTagStreamMatcher closeBodyProcess;
    private final StepManager stepManager;

    public WarpFilter(final WarpFilterConfiguration config, final StepManager stepManager) {
        this.config = config;
        this.stepManager = stepManager;
        this.closeHeadFlush = HtmlCloseTagStreamMatcher.forTag("head");
        this.placeholderMapping = HtmlTagStreamMatcher.forTag("placeholder");
        this.closeBodyProcess = HtmlCloseTagStreamMatcher.forTag("body");
    }

    private StepData buildPipelineItem(final MatchingContext content) {
        final StartTag tag = placeholderMapping.findStartTag(content.content());
        if (tag != null) {
            return new StepData(
                    tag.getAttributeValue("id"),
                    priority(tag.getAttributeValue("pipeline-priority")),
                    tag.getAttributes().populateMap(new HashMap<>(), true));
        } else {
            throw new RuntimeException("Tag matched but not found later!");
        }
    }

    private Optional<Integer> priority(String attr) {
        if (attr != null && !attr.isEmpty()) {
            try {
                return Optional.of(Integer.valueOf(attr));
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer in attribute pipeline-priority: {}", attr);
            }
        }
        return Optional.empty();
    }

    private Context buildPipelineContext(final HttpServletRequest request, final MatchingContext context) {
        return new Context(
                new BufferedWriter(
                        new Writer() {
                            @Override
                            public void write(char[] cbuf, int off, int len) throws IOException {
                                context.output().write(new String(cbuf, off, len));
                            }

                            @Override
                            public void flush() throws IOException {
                                context.output().flush();
                            }

                            @Override
                            public void close() throws IOException {
                                //nothing
                            }
                        }
                ),
                request
        );
    }

    public void filter(final HttpServletRequest request, final ServletResponse response,
                         final FilterChain chain) throws IOException, ServletException {
        final QueuedPipeline.Builder pipelineBuilder = QueuedPipeline.create(stepManager);
        final HttpServletResponse responseWrapper = new DecodingHttpServletResponse(response, config.charset(),
                builder -> {
                    if (config.flushAfterHead()) {
                        builder.bind(closeHeadFlush, StreamListener.flushListener());
                    }
                    if (config.autoExecuteBeforeClosingBody()) {
                        builder.bind(closeBodyProcess, context -> pipelineBuilder.build().execute(
                                buildPipelineContext(request, context)));
                    }
                    if (config.autoDetectPlaceholders()) {
                        builder.bind(placeholderMapping, context -> pipelineBuilder.include(buildPipelineItem(context)));
                    }
                });
        chain.doFilter(request, responseWrapper);
    }

}
