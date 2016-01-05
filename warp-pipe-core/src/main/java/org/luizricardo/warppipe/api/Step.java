package org.luizricardo.warppipe.api;

import java.io.IOException;
import java.util.Optional;

/**
 * Describes an step that will be executed to handle a specific {@link StepData}.
 */
public interface Step {

    /**
     * Handles item in the pipeline.
     */
    void execute(StepData data, StepContext context) throws IOException;

    /**
     * Defines default priority for this process. It can be overridden specifying arbitrary.
     * Implementations may return {@link Optional#empty()} to abstain to interfere in prioritization.
     */
    Optional<Integer> defaultPriority(StepData data, StepContext context);

}
