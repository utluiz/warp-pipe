package org.luizricardo.warppipe.api;

/**
 * Defines a chain of items to be processed in order fo fulfill the request.
 */
public interface Pipeline {

    /**
     * Handles the processing of all items and returns when all are finished.
     * It does not mean the items cannot be already processed, but it's the last call.
     */
    PipelineResult execute(StepContext context);

    /**
     * Builder template for implementations.
     */
    interface Builder<T extends Pipeline> {

        /**
         * Includes an item to be processed.
         */
        Builder<T> include(StepData stepData);

        /**
         * Builds the current pipeline.
         */
        T build();

    }

}
