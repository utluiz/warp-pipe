package org.luizricardo.warppipe.pipeline;

/**
 * Find action beans in CDI context
 */
public class CdiActionManager implements ActionManager {

    public CdiActionManager() {
    }

    @Override
    public void execute(PipelineItem pipelineItem) throws PipelineException {
        throw new UnsupportedOperationException("Not implemented");
    }
}
