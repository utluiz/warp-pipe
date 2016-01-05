package org.luizricardo.warppipe;

import org.luizricardo.warppipe.api.Step;
import org.luizricardo.warppipe.api.StepManager;
import org.luizricardo.warppipe.pipeline.step.DefaultStepManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class DefaultWarpFilter implements Filter {

    private static Logger logger = LoggerFactory.getLogger(DefaultWarpFilter.class);

    private static WarpFilterConfiguration config;
    private static volatile WarpFilter warpFilter;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        config = new WarpFilterConfiguration(
                charset(filterConfig.getInitParameter("encoding")),
                booleanParam(filterConfig.getInitParameter("flush-after-head"), true),
                booleanParam(filterConfig.getInitParameter("auto-execute-before-closing-body"), true),
                booleanParam(filterConfig.getInitParameter("auto-detect-placeholders"), true));
        warpFilter = new WarpFilter(config, new DefaultStepManager(resolveSteps(filterConfig.getInitParameter("steps"))));
    }

    public static void setStepManager(StepManager stepManager) {
        warpFilter = new WarpFilter(config, stepManager);
    }

    private Map<String, Step> resolveSteps(String stepConfiguration) {
        final Map<String, Step> result = new HashMap<>();
        if (stepConfiguration != null && !stepConfiguration.isEmpty()) {
            logger.info("Loading steps from filter configuration: ", stepConfiguration);
            String[] steps = stepConfiguration.split("[,;]");
            for (String stepConfig : steps) {
                String[] tuple = stepConfig.split("=");
                if (tuple.length == 2) {
                    String id = tuple[0];
                    String className = tuple[1];
                    try {
                        result.put(id, (Step) Class.forName(className).newInstance());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return result;
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

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response,
                         final FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            warpFilter.filter((HttpServletRequest) request, response, chain);
        }
    }

    @Override
    public void destroy() {
        config = null;
        logger = null;
        warpFilter = null;
    }

}
