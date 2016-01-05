package org.luizricardo.warppipe.pipeline.step;


import org.luizricardo.warppipe.api.Step;
import org.luizricardo.warppipe.api.StepContext;
import org.luizricardo.warppipe.api.StepData;
import org.luizricardo.warppipe.api.StepManager;
import org.luizricardo.warppipe.pipeline.PipelineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default Step implementation which should be feed manually with instances of {@link Step} and also
 * will try to instantiate and cache steps based on the attribute "step-class", which should contain the full class name of a Step.
 * <p>
 * If you have a Servlet based applications, {@link org.luizricardo.warppipe.DefaultWarpFilter} will try to populate
 * Steps using the filter configuration, but you can also inject your own populated instance of this class in the filter.
 * </p>
 */
public class DefaultStepManager implements StepManager {

    final static Logger logger = LoggerFactory.getLogger(DefaultStepManager.class);

    protected final Map<String, Step> stepMap;
    protected final Map<String, Step> instanceCache;

    public DefaultStepManager(final Map<String, Step> stepMap) {
        this.stepMap = Collections.unmodifiableMap(new HashMap<>(stepMap));
        this.instanceCache = new ConcurrentHashMap<>();
    }

    @Override
    public void execute(final StepData stepData, final StepContext context) throws PipelineException {
        executeStep(resolveStep(stepData), stepData, context);
    }

    @Override
    public Optional<Integer> defaultPriority(final StepData stepData, final StepContext stepContext) throws PipelineException {
        try {
            return resolveStep(stepData).defaultPriority(stepData, stepContext);
        } catch (Throwable e) {
            logger.warn("Error obtaining step priority.", e);
            return Optional.empty();
        }
    }

    /**
     * Looks for a specified step. If not found, look for class name attribute and try to instantiate it.
     * @param stepData Data being processed
     * @return Step instance.
     * @throws PipelineException
     */
    protected Step resolveStep(final StepData stepData) throws PipelineException {
        if (stepMap.containsKey(stepData.id())) {
            //execute a pre-configured step
            return stepMap.get(stepData.id());
        } else {
            //execute an inline-configured step
            final String stepClassAttr = stepData.attributes().get("step-class");
            if (stepClassAttr != null) {
                return createInstance(stepClassAttr, stepData);
            }
        }
        throw new PipelineException("No step found to execute this data!", stepData);
    }

    /**
     * Tries to instantiate a {@link Step} based on its class name, which is provided by an attribute of {@link StepData}.
     * It'll cache instances based on the class name.
     * @param stepClassName Full class name. Should have default constructor.
     * @param stepData Data being processed.
     * @return New instance or cached instance.
     * @throws PipelineException
     */
    protected Step createInstance(final String stepClassName, final StepData stepData) throws PipelineException {
        final Step cached = instanceCache.get(stepClassName);
        if (cached != null) {
            return cached;
        } else {
            final Class<?> loadingClass;
            try {
                loadingClass = Class.forName(stepClassName);
            } catch (ClassNotFoundException e) {
                logger.error("Error loading step class.", e);
                throw new PipelineException(String.format("Cannot find class '%s'", stepClassName), e, stepData);
            }
            if (Step.class.isAssignableFrom(loadingClass)) {
                @SuppressWarnings("unchecked")
                final Class<Step> stepClass = (Class<Step>) loadingClass;
                try {
                    final Step newInstance = stepClass.newInstance();
                    instanceCache.putIfAbsent(stepClassName, newInstance);
                    return newInstance;
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("Failed to create new step instance.", e);
                    throw new PipelineException(String.format("Cannot instantiate class '%s'", stepClassName), e, stepData);
                }
            } else {
                throw new PipelineException(String.format("Class '%s' is not an instance of %s", stepClassName, Step.class.getName()), stepData);
            }
        }
    }

    /**
     * Execute Step with Stepdata in StepContext, log and throw proper exceptions
     */
    protected void executeStep(final Step step, final StepData stepData, final StepContext context) throws PipelineException {
        try {
            step.execute(stepData, context);
        } catch (Throwable e) {
            logger.error("Error executing step.", e);
            throw new PipelineException(String.format("Exception when executing step '%s': %s", step.getClass().getName(), e.getLocalizedMessage()), e, stepData);
        }
    }

}
