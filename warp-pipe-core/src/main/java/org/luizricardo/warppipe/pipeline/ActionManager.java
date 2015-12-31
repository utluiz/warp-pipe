package org.luizricardo.warppipe.pipeline;


/**
 * Handles which action should be executed
 */
public interface ActionManager {

    /**
     * Execute one action.
     * @throws PipelineException Item to be executed.
     */
    void execute(PipelineItem pipelineItem) throws PipelineException;

}
