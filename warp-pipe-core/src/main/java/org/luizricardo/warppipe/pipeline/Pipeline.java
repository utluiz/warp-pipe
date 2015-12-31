package org.luizricardo.warppipe.pipeline;

/**
 * Defines a pipeline of items to be processed in order fo fulfill the request.
 */
public interface Pipeline {

    /**
     * Includes an item to be processed.
     */
    void add(PipelineItem item);

    /**
     * Handles the processing of all items and returns when all are finished.
     * It does not mean the items cannot be already processed, but it's the last call.
     */
    void execute();

}
