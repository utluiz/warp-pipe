package org.luizricardo.warppipe;

import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.Source;
import org.luizricardo.warppipe.decoder.StreamDecoder;
import org.luizricardo.warppipe.decoder.StreamDecoderOutputStream;
import org.luizricardo.warppipe.listener.StreamListener;
import org.luizricardo.warppipe.matcher.HtmlCloseTagStreamMatcher;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class WarpFilter implements Filter {

    private Charset charset = StandardCharsets.UTF_8;
    private HtmlCloseTagStreamMatcher closeHeadFlush;
    private HtmlCloseTagStreamMatcher placeholderMapping;
    private HtmlCloseTagStreamMatcher closeBodyProcess;


    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        final String encodingParam = filterConfig.getInitParameter("encoding");
        if (encodingParam != null && !encodingParam.isEmpty()) {
            this.charset = Charset.forName(encodingParam);
        }
        closeHeadFlush = new HtmlCloseTagStreamMatcher("head");
        placeholderMapping = new HtmlCloseTagStreamMatcher("placeholder");
        closeBodyProcess = new HtmlCloseTagStreamMatcher("body");
    }

    private <T extends StreamDecoder.Builder> T addFilters(T builder) {
        builder.bind(closeHeadFlush, StreamListener.flush());
        builder.bind(closeBodyProcess, StreamListener.flush());
        return builder;
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response,
                         final FilterChain chain) throws IOException, ServletException {

        final Map<String, Attributes> placeholders = new HashMap<>();
        final StreamListener storePipeListener = sb -> {
            Source source = new Source(sb);
            String id = source.getFirstElement().getAttributeValue("id");
            placeholders.put(id, source.getFirstElement().getAttributes());
            return false;
        };

        final StreamListener processPipeListener = sb -> {
            placeholders.entrySet().forEach(entry -> {
                //each id is associated with a process
            });
            return false;
        };

        final HttpServletResponse responseWrapper = new HttpServletResponseWrapper((HttpServletResponse) response) {
            @Override
            public PrintWriter getWriter() throws IOException {
                return new PrintWriter(addFilters(StreamDecoder.Builder.forWriter(super.getWriter()))
                        .bind(placeholderMapping, storePipeListener)
                        .bind(closeBodyProcess, processPipeListener)
                        .build());
            }

            @Override
            public ServletOutputStream getOutputStream() throws IOException {
                final ServletOutputStream servletOutputStream = super.getOutputStream();
                final StreamDecoderOutputStream decoder = addFilters(StreamDecoder.Builder.forOutputStream(servletOutputStream, charset))
                        .bind(placeholderMapping, storePipeListener)
                        .bind(closeBodyProcess, processPipeListener)
                        .build();
                return new ServletOutputStream() {
                    @Override
                    public boolean isReady() {
                        return servletOutputStream.isReady();
                    }

                    @Override
                    public void setWriteListener(WriteListener writeListener) {
                        servletOutputStream.setWriteListener(writeListener);
                    }

                    @Override
                    public void write(int b) throws IOException {
                        decoder.write(b);
                    }
                };

            }
        };
        chain.doFilter(request, responseWrapper);
    }

    @Override
    public void destroy() {
    }
}
