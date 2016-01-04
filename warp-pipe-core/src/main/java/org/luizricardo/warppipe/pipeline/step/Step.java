package org.luizricardo.warppipe.pipeline.step;

import org.luizricardo.warppipe.pipeline.Context;

import java.io.IOException;
import java.util.Optional;

/**
 * Describes an step that will be executed to handle a specific {@link StepData}.
 */
public interface Step {

    /**
     * Handles item in the pipeline.
     */
    void execute(StepData data, Context context) throws IOException;

    /**
     * Defines default priority for this process. It can be overriden specifying arbitrary.
     */
    Optional<Integer> priority();

}
