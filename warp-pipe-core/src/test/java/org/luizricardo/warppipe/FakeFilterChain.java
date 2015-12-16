package org.luizricardo.warppipe;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.function.BiConsumer;


public class FakeFilterChain implements FilterChain {

    private BiConsumer<HttpServletRequest, HttpServletResponse> filterMethod;

    public static FilterChain of(BiConsumer<HttpServletRequest, HttpServletResponse> filterMethod) {
        return new FakeFilterChain(filterMethod);
    }

    private FakeFilterChain(BiConsumer<HttpServletRequest, HttpServletResponse> filterMethod) {
        this.filterMethod = filterMethod;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        filterMethod.accept((HttpServletRequest) request, (HttpServletResponse) response);
    }
}
