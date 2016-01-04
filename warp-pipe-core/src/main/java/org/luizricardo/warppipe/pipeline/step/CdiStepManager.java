package org.luizricardo.warppipe.pipeline.step;

import org.luizricardo.warppipe.pipeline.Context;
import org.luizricardo.warppipe.pipeline.PipelineException;

import java.util.Optional;

/**
 * Find step beans in CDI context
 */
public class CdiStepManager implements StepManager {

    public CdiStepManager() {
    }

    @Override
    public void execute(StepData stepData, Context context) throws PipelineException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Optional<Integer> getPriority(StepData stepData, Context context) throws PipelineException {
        throw new UnsupportedOperationException("Not implemented");
    }
}
