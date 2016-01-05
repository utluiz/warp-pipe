package org.luizricardo.warppipe.api;


import org.luizricardo.warppipe.pipeline.PipelineException;

import java.util.Optional;

/**
 * Handles which step should be executed.
 */
public interface StepManager {

    /**
     * Execute a step with to the current {@link StepData} and {@link StepContext}.
     */
    void execute(StepData stepData, StepContext stepContext) throws PipelineException;

    /**
     * Return an optional default priority for one step, according to the current context, so it can be properly prioritized.
     */
    Optional<Integer> defaultPriority(StepData stepData, StepContext stepContext) throws PipelineException;

}
