package org.luizricardo.warppipe.pipeline;

import java.util.PriorityQueue;

public class QueuedPipeline implements Pipeline {

    private final PriorityQueue<PipelineItem> queue;
    private final ActionManager actionManager;

    public QueuedPipeline(final ActionManager actionManager) {
        this.actionManager = actionManager;
        this.queue = new PriorityQueue<>((item1, item2) -> item1.priority().compareTo(item2.priority()));
    }

    public void add(final PipelineItem item) {
        queue.add(item);
    }

    public void execute() {
        queue.forEach(item -> {
            try {
                actionManager.execute(item);
            } catch (PipelineException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
