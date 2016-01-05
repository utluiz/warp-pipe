package org.luizricardo.warppipe.api;

import javax.servlet.http.HttpServletRequest;
import java.io.Writer;

/**
 * Context objects which can be useful when executing a step of the pipeline.
 */
public interface StepContext {

    /**
     * Access to write to the output.
     */
    Writer writer();

    /**
     * Access to the current request object.
     */
    HttpServletRequest request();

    /**
     * Provides a default implementation.
     */
    static StepContext create(final Writer writer, final HttpServletRequest request) {
        return new StepContext() {
            @Override
            public Writer writer() {
                return writer;
            }

            @Override
            public HttpServletRequest request() {
                return request;
            }
        };
    }

}
