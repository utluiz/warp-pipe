package org.luizricardo.warppipe.fakes;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class FakeFilterConfig implements FilterConfig {

    private final Map<String,String> parameters = new HashMap<>();

    @Override
    public String getFilterName() {
        return "Dude";
    }

    @Override
    public ServletContext getServletContext() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getInitParameter(String name) {
        return parameters.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(parameters.keySet());
    }

    public FakeFilterConfig param(String name, String value) {
        parameters.put(name, value);
        return this;
    }

}
