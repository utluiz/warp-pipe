package org.luizricardo.warppipe.api;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the result of processing all {@link Step}s of a pipeline.
 * May be used for logging, diagnostics and error handling.
 */
public interface PipelineResult {

    /**
     * @return Whether pipeline completed successfully or not.
     */
    boolean success();

    /**
     * Individual results for the processing of each {@link StepData}.
     */
    List<StepResult> results();

    /**
     * Creates a Builder for the default implementation.
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * Default builder implementation.
     */
    class Builder {
        final List<StepResult> results = new ArrayList<>();
        boolean success = true;

        protected Builder add(final StepResult result) {
            results.add(result);
            if (!result.success()) success = false;
            return this;
        }

        /**
         * Adds an error result.
         * @param data Data being processed.
         * @param exception Cause of the error.
         */
        public Builder error(final StepData data, Throwable exception) {
            return this.add(StepResult.error(data, exception));
        }

        /**
         * Signals a successful execution.
         * @param data Data being processed
         */
        public Builder success(final StepData data) {
            return this.add(StepResult.success(data));
        }

        /**
         * Builds the result.
         */
        public PipelineResult build() {
            final List<StepResult> finalResults = Collections.unmodifiableList(new ArrayList<>(results));
            final boolean finalSuccess = success;
            return new PipelineResult() {
                @Override
                public boolean success() {
                    return finalSuccess;
                }

                @Override
                public List<StepResult> results() {
                    return finalResults;
                }
            };
        }
    }

}
