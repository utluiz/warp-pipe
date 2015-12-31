package org.luizricardo.warppipe.pipeline;

import java.io.IOException;

/**
 * Describes an action that will be executed to handle a specific {@link PipelineItem}.
 */
public interface PipelineAction {

    /**
     * Handles item in the pipeline.
     */
    void execute(PipelineItem item) throws IOException;

}
