package org.luizricardo.warppipe.pipeline.step;

import org.luizricardo.warppipe.api.StepContext;
import org.luizricardo.warppipe.api.StepData;
import org.luizricardo.warppipe.api.StepManager;
import org.luizricardo.warppipe.pipeline.PipelineException;

import java.util.Optional;

/**
 * Find step beans in Spring context.
 */
public class SpringStepManager implements StepManager {

    public SpringStepManager() {
    }

    @Override
    public void execute(final StepData stepData, final StepContext context) throws PipelineException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Optional<Integer> defaultPriority(final StepData stepData, final StepContext context) throws PipelineException {
        throw new UnsupportedOperationException("Not implemented");
    }
}
