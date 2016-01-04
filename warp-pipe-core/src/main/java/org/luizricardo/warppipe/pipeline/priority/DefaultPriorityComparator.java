package org.luizricardo.warppipe.pipeline.priority;


import org.luizricardo.warppipe.pipeline.Context;
import org.luizricardo.warppipe.pipeline.step.StepData;
import org.luizricardo.warppipe.pipeline.step.StepManager;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class DefaultPriorityComparator implements Comparator<StepData> {

    private final Context context;
    private final StepManager stepManager;
    private final Map<String,Priority> priorityMap = new HashMap<>();

    public DefaultPriorityComparator(final Context context, final StepManager stepManager) {
        this.context = context;
        this.stepManager = stepManager;
    }

    @Override
    public int compare(StepData d1, StepData d2) {
        return priority(d1, context, stepManager).compareTo(priority(d2, context, stepManager));
    }

    private Priority priority(final StepData stepData, final Context context, final StepManager stepManager) {
        return priorityMap.computeIfAbsent(stepData.id(),
                k -> Priority.resolve(stepData, context, stepManager));
    }

}
