package org.luizricardo.warppipe;

import org.luizricardo.warppipe.decoder.StreamDecoder;
import org.luizricardo.warppipe.decoder.StreamDecoderBuilder;
import org.luizricardo.warppipe.decoder.StreamDecoderOutputStream;
import org.luizricardo.warppipe.decoder.StreamDecoderWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.function.Consumer;

/**
 * Decorates a {@link HttpServletResponse} adding decoding capabilities to either {@link ServletOutputStream}
 * or {@link PrintWriter} which are also decorated and decoded and are checked against
 * {@link org.luizricardo.warppipe.matcher.StreamMatcher}s.
 */
public class DecodingHttpServletResponse extends HttpServletResponseWrapper {

    private final Charset charset;
    private final Consumer<StreamDecoderBuilder> binderFunction;

    /**
     * @param response Original servlet response
     * @param charset Charset to decode characters when using {@link ServletOutputStream}
     * @param binderFunction Consumer function which allows client bind matchers and listeners to decode the stream.
     */
    public DecodingHttpServletResponse(final ServletResponse response, final Charset charset, final Consumer<StreamDecoderBuilder> binderFunction) {
        super((HttpServletResponse) response);
        this.charset = charset;
        this.binderFunction = binderFunction;
    }

    private Writer decorate(final Writer delegate) {
        final StreamDecoderBuilder<StreamDecoderWriter> builder = StreamDecoder.forWriter(delegate);
        binderFunction.accept(builder);
        return builder.build();
    }

    private OutputStream decorate(final OutputStream delegate) {
        final StreamDecoderBuilder<StreamDecoderOutputStream> builder = StreamDecoder.forOutputStream(delegate, charset);
        binderFunction.accept(builder);
        return builder.build();
    }


    @Override
    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(decorate(super.getWriter()));
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        final ServletOutputStream servletOutputStream = super.getOutputStream();
        final OutputStream decoder = decorate(servletOutputStream);
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

}
