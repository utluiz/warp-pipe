package org.luizricardo.warppipe.pipeline.step;


import org.luizricardo.warppipe.pipeline.Context;
import org.luizricardo.warppipe.pipeline.PipelineException;

import java.util.Optional;

/**
 * Handles which step should be executed
 */
public interface StepManager {

    /**
     * Execute one step.
     * @throws PipelineException Item to be executed.
     */
    void execute(StepData stepData, Context context) throws PipelineException;

    Optional<Integer> getPriority(StepData stepData, Context context) throws PipelineException;

}
