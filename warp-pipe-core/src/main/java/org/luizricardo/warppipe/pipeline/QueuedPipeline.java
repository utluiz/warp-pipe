package org.luizricardo.warppipe.pipeline;

import org.luizricardo.warppipe.api.Pipeline;
import org.luizricardo.warppipe.api.PipelineResult;
import org.luizricardo.warppipe.api.StepContext;
import org.luizricardo.warppipe.api.StepData;
import org.luizricardo.warppipe.api.StepManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Queues {@link org.luizricardo.warppipe.api.Step}s according to their priorities.
 */
public class QueuedPipeline implements Pipeline {

    final static Logger logger = LoggerFactory.getLogger(QueuedPipeline.class);

    /**
     * Default comparator sorts higher values (priorities) first.
     */
    public static Comparator<DataPriorityTuple> DEFAULT_COMPARATOR = (d1, d2) ->  d2.getResolvedPriority().compareTo(d1.getResolvedPriority());

    public class DataPriorityTuple {
        private StepData stepData;
        private Integer resolvedPriority;

        protected DataPriorityTuple(StepData stepData, StepContext stepContext) {
            this.stepData = stepData;
            this.resolvedPriority = Priority.resolve(stepData, stepContext, stepManager);
        }

        public Integer getResolvedPriority() {
            return resolvedPriority;
        }

        public StepData getStepData() {
            return stepData;
        }
    }

    private final List<StepData> data;
    private final StepManager stepManager;
    private final Comparator<DataPriorityTuple> prioritizer;

    public QueuedPipeline(final StepManager stepManager, final List<StepData> data) {
        this(stepManager, data, DEFAULT_COMPARATOR);
    }

    public QueuedPipeline(final StepManager stepManager, final List<StepData> data, final Comparator<DataPriorityTuple> prioritizer) {
        this.stepManager = stepManager;
        this.data = data;
        this.prioritizer = prioritizer;
    }

    public static Builder<QueuedPipeline> create(final StepManager stepManager) {
        return new Builder<QueuedPipeline>() {
            private final List<StepData> data = new ArrayList<>();
            @Override
            public Builder<QueuedPipeline> include(final StepData stepData) {
                data.add(stepData);
                return this;
            }

            @Override
            public QueuedPipeline build() {
                return new QueuedPipeline(stepManager, data);
            }
        };
    }

    @Override
    public PipelineResult execute(final StepContext stepContext) {
        final ArrayList<DataPriorityTuple> sorted = new ArrayList<>();
        sorted.addAll(data.stream().map(stepData -> new DataPriorityTuple(stepData, stepContext)).collect(Collectors.toList()));
        Collections.sort(sorted, prioritizer);
        final PipelineResult.Builder result = PipelineResult.builder();
        sorted.forEach(tuple -> {
            try {
                stepManager.execute(tuple.stepData, stepContext);
                result.success(tuple.stepData);
            } catch (PipelineException e) {
                logger.error("Failed to execute step.", e);
                result.error(tuple.stepData, e);
            }
        });
        return result.build();
    }

}
