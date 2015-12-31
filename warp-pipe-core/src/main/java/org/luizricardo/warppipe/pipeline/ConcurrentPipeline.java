package org.luizricardo.warppipe.pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ConcurrentPipeline implements Pipeline {

    private final List<Future<Optional<PipelineException>>> results;
    private final ActionManager actionManager;
    private final ExecutorService executorService;

    public ConcurrentPipeline(final ActionManager actionManager, final int threadPoolSize) {
        this.actionManager = actionManager;
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
        this.results = new ArrayList<>();
    }

    public void add(PipelineItem item) {
        results.add(executorService.submit(() -> {
            try {
                actionManager.execute(item);
                return Optional.empty();
            } catch (PipelineException e) {
                //log
                return Optional.of(e);
            }
        }));
    }

    public void execute() {
        for (Future<Optional<PipelineException>> future : results) {
            try {
                Optional<PipelineException> e = future.get();
                if (e.isPresent()) {
                    throw e.get();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (PipelineException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
