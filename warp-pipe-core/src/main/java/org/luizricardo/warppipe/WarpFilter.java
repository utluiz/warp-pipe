package org.luizricardo.warppipe;

import net.htmlparser.jericho.StartTag;
import org.luizricardo.warppipe.api.StepContext;
import org.luizricardo.warppipe.api.StepManager;
import org.luizricardo.warppipe.listener.MatchingContext;
import org.luizricardo.warppipe.listener.StreamListener;
import org.luizricardo.warppipe.matcher.HtmlCloseTagStreamMatcher;
import org.luizricardo.warppipe.matcher.HtmlTagStreamMatcher;
import org.luizricardo.warppipe.pipeline.QueuedPipeline;
import org.luizricardo.warppipe.api.StepData;
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

    private StepData buildStepData(final MatchingContext matchingContext) {
        final StartTag tag = placeholderMapping.findStartTag(matchingContext.content());
        if (tag != null) {
            return StepData.create(
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

    private StepContext buildStepContext(final HttpServletRequest request, final MatchingContext context) {
        return StepContext.create(
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
                        builder.bind(closeBodyProcess, matchingContext ->
                                pipelineBuilder.build().execute(buildStepContext(request, matchingContext)));
                    }
                    if (config.autoDetectPlaceholders()) {
                        builder.bind(placeholderMapping, matchingContext ->
                                pipelineBuilder.include(buildStepData(matchingContext)));
                    }
                });
        chain.doFilter(request, responseWrapper);
    }

}
