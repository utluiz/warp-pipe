package org.luizricardo.warppipe.pipeline;

import org.luizricardo.warppipe.pipeline.step.StepData;
import org.luizricardo.warppipe.pipeline.step.StepManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ConcurrentPipeline implements Pipeline {

    private final List<StepData> data;
    private final StepManager stepManager;
    private final ExecutorService executorService;

    public ConcurrentPipeline(final StepManager stepManager, final int threadPoolSize, final List<StepData> data) {
        this.stepManager = stepManager;
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
        this.data = data;
    }

    public static Builder<ConcurrentPipeline> create(final StepManager stepManager, final int threadPoolSize) {
        return new Builder<ConcurrentPipeline>() {
            private final List<StepData> data = new ArrayList<>();
            @Override
            public Builder<ConcurrentPipeline> include(StepData stepData) {
                data.add(stepData);
                return this;
            }

            @Override
            public ConcurrentPipeline build() {
                return new ConcurrentPipeline(stepManager, threadPoolSize, data);
            }
        };
    }

    @Override
    public PipelineResult execute(final Context context) {
        try {
            final PipelineResult.Builder result = PipelineResult.builder();
            executorService.invokeAll(data.stream().map(pipelineData -> (Callable<Void>) () -> {
                try {
                    stepManager.execute(pipelineData, context);
                    synchronized (result) {
                        result.success(pipelineData);
                    }
                } catch (PipelineException e) {
                    synchronized (result) {
                        result.error(pipelineData, e);
                    }
                }
                return null;
            }).collect(Collectors.toList()));
            return result.build();
        } catch (InterruptedException e) {
            //log
            e.printStackTrace();
            //TODO handle timeouts in a loop
            throw new RuntimeException("InterruptedException", e);
        }
    }

}
