package org.luizricardo.warppipe.pipeline.step;

import org.luizricardo.warppipe.pipeline.Context;
import org.luizricardo.warppipe.pipeline.PipelineException;

import java.util.Optional;

/**
 * Find step beans in Spring context.
 */
public class SpringStepManager implements StepManager {

    public SpringStepManager() {
    }

    @Override
    public void execute(final StepData stepData, final Context context) throws PipelineException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Optional<Integer> getPriority(StepData stepData, Context context) throws PipelineException {
        throw new UnsupportedOperationException("Not implemented");
    }
}
