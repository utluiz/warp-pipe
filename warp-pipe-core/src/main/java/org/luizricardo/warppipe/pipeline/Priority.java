package org.luizricardo.warppipe.pipeline;

import org.luizricardo.warppipe.api.Step;
import org.luizricardo.warppipe.api.StepContext;
import org.luizricardo.warppipe.api.StepData;
import org.luizricardo.warppipe.api.StepManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Encapsulates basic priority logic of a {@link StepData} to be processed.
 * It can be influenced by multiple factors which by default are {@link Step#defaultPriority(StepData, StepContext)} and
 * {@link StepData#priority()}, where the latter overrides the former and {@link #DEFAULT_PRIORITY} is applied when
 * both are absent.
 */
public interface Priority {

    Logger logger = LoggerFactory.getLogger(Priority.class);

    /**
     * Default priority to processing.
     */
    Integer DEFAULT_PRIORITY = 0; // "nem fede, nem cheira"

    /**
     * Default lower priority.
     */
    Integer LOWER_PRIORITY = -1;

    /**
     * Default higher priority.
     */
    Integer HIGHER_PRIORITY = 1;

    /**
     * Resolves actual priority for the current pipeline data.
     * @param stepData Data being processed.
     * @param context Context for the current pipeline.
     * @param stepManager Uses to retrieve the default priority for the {@link Step} that process the data.
     * @return Immutable instance.
     */
    static Integer resolve(final StepData stepData, final StepContext context, final StepManager stepManager) {
        Optional<Integer> priority = stepData.priority();
        if (!priority.isPresent()) {
            try {
                priority = stepManager.defaultPriority(stepData, context);
            } catch (PipelineException e) {
                logger.warn("Failed to obtain priority", e);
            }
        }
        return priority.orElse(DEFAULT_PRIORITY);
    }

}
