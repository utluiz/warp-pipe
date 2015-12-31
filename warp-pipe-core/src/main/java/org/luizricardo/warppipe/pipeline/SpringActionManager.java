package org.luizricardo.warppipe.pipeline;

/**
 * Find action beans in Spring context.
 */
public class SpringActionManager implements ActionManager {

    public SpringActionManager() {
    }

    @Override
    public void execute(final PipelineItem pipelineItem) throws PipelineException {
        throw new UnsupportedOperationException("Not implemented");
    }
}
