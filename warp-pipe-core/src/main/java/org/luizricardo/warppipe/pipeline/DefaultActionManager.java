package org.luizricardo.warppipe.pipeline;


import java.util.HashMap;
import java.util.Map;

public class DefaultActionManager implements ActionManager {

    private final Map<String, PipelineAction> actions;

    public DefaultActionManager() {
        actions = new HashMap<>();
    }

    @Override
    public void execute(final PipelineItem pipelineItem) throws PipelineException {
        if (actions.containsKey(pipelineItem.id())) {
            //execute a pre-configured action
            executeAction(actions.get(pipelineItem.id()), pipelineItem);
        } else {
            //execute an inline-configured action
            final String actionClassAttr = pipelineItem.attributes().get("action-class");
            if (actionClassAttr != null) {
                executeAction(createInstance(actionClassAttr, pipelineItem), pipelineItem);
            }
        }
    }

    private PipelineAction createInstance(final String actionClassName, final PipelineItem pipelineItem) throws PipelineException {
        final Class<?> loadingClass;
        try {
            loadingClass = Class.forName(actionClassName);
        } catch (ClassNotFoundException e) {
            throw new PipelineException(String.format("Cannot find class '%s'", actionClassName), e, pipelineItem);
        }
        if (PipelineAction.class.isAssignableFrom(loadingClass)) {
            @SuppressWarnings("unchecked")
            final Class<PipelineAction> actionClass = (Class<PipelineAction>) loadingClass;
            try {
                return actionClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new PipelineException(String.format("Cannot instantiate class '%s'", actionClassName), e, pipelineItem);
            }
        } else {
            throw new PipelineException(String.format("Class '%s' is not an instance of %s", actionClassName, PipelineAction.class.getName()), pipelineItem);
        }
    }

    private void executeAction(final PipelineAction pipelineAction, final PipelineItem pipelineItem) throws PipelineException {
        try {
            pipelineAction.execute(pipelineItem);
        } catch (Throwable e) {
            throw new PipelineException(String.format("Exception when executing action '%s': %s", pipelineAction.getClass().getName(), e.getLocalizedMessage()), e, pipelineItem);
        }
    }

}
