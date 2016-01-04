package org.luizricardo.warppipe.pipeline;

import org.luizricardo.warppipe.pipeline.step.StepData;
import org.luizricardo.warppipe.pipeline.step.StepManager;
import org.luizricardo.warppipe.pipeline.priority.DefaultPriorityComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class QueuedPipeline implements Pipeline {

    private final List<StepData> data;
    private final StepManager stepManager;

    public QueuedPipeline(final StepManager stepManager, final List<StepData> data) {
        this.stepManager = stepManager;
        this.data = new ArrayList<>(data);
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

    public PipelineResult execute(Context context) {
        final PriorityQueue<StepData> queue = new PriorityQueue<>(
                new DefaultPriorityComparator(context, stepManager));
        queue.addAll(data);
        final PipelineResult.Builder result = PipelineResult.builder();
        queue.forEach(pipelineData -> {
            try {
                stepManager.execute(pipelineData, context);
                result.success(pipelineData);
            } catch (PipelineException e) {
                result.error(pipelineData, e);
            }
        });
        return result.build();
    }

}
