package org.luizricardo.warppipe.pipeline.step;


import org.luizricardo.warppipe.pipeline.Context;
import org.luizricardo.warppipe.pipeline.PipelineException;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class DefaultStepManager implements StepManager {

    private final Map<String, Step> actions;

    public DefaultStepManager(final Map<String, Step> actions) {
        this.actions = Collections.unmodifiableMap(actions);
    }

    @Override
    public void execute(final StepData stepData, final Context context) throws PipelineException {
        executeAction(resolveAction(stepData), stepData, context);
    }

    @Override
    public Optional<Integer> getPriority(final StepData stepData, final Context context) throws PipelineException {
        return resolveAction(stepData).priority();
    }

    public Step resolveAction(final StepData stepData) throws PipelineException {
        if (actions.containsKey(stepData.id())) {
            //execute a pre-configured step
            return actions.get(stepData.id());
        } else {
            //execute an inline-configured step
            final String actionClassAttr = stepData.attributes().get("step-class");
            if (actionClassAttr != null) {
                return createInstance(actionClassAttr, stepData);
            }
        }
        throw new PipelineException("No step found to execute this step!", stepData);
    }

    private Step createInstance(final String actionClassName, final StepData stepData) throws PipelineException {
        final Class<?> loadingClass;
        try {
            loadingClass = Class.forName(actionClassName);
        } catch (ClassNotFoundException e) {
            throw new PipelineException(String.format("Cannot find class '%s'", actionClassName), e, stepData);
        }
        if (Step.class.isAssignableFrom(loadingClass)) {
            @SuppressWarnings("unchecked")
            final Class<Step> actionClass = (Class<Step>) loadingClass;
            try {
                return actionClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new PipelineException(String.format("Cannot instantiate class '%s'", actionClassName), e, stepData);
            }
        } else {
            throw new PipelineException(String.format("Class '%s' is not an instance of %s", actionClassName, Step.class.getName()), stepData);
        }
    }

    private void executeAction(final Step step, final StepData stepData, final Context context) throws PipelineException {
        try {
            step.execute(stepData, context);
        } catch (Throwable e) {
            throw new PipelineException(String.format("Exception when executing step '%s': %s", step.getClass().getName(), e.getLocalizedMessage()), e, stepData);
        }
    }

}
