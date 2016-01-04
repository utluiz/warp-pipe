package org.luizricardo.warppipe.pipeline.priority;

import org.luizricardo.warppipe.pipeline.Context;
import org.luizricardo.warppipe.pipeline.step.StepData;
import org.luizricardo.warppipe.pipeline.PipelineException;
import org.luizricardo.warppipe.pipeline.step.StepManager;

import java.util.Optional;

public class Priority implements Comparable<Priority> {

    public static final Integer DEFAULT_PRIORITY = 0; //nem fede, nem cheira
    private final Integer priority;

    protected Priority(final Integer priority) {
        this.priority = priority;
    }

    public static Priority resolve(final StepData stepData, final Context context, final StepManager stepManager) {
        Optional<Integer> priority = stepData.priority();
        if (!priority.isPresent()) {
            try {
                priority = stepManager.getPriority(stepData, context);
            } catch (PipelineException e) {
                //log
            }
        }
        return new Priority(priority.orElse(DEFAULT_PRIORITY));
    }

    public Integer priority() {
        return priority;
    }

    @Override
    public int compareTo(final Priority other) {
        return this.priority.compareTo(other.priority);
    }
}
