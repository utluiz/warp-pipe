package org.luizricardo.warppipe.pipeline;


import org.luizricardo.warppipe.pipeline.step.StepData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface PipelineResult {

    boolean success();

    List<StepResult> results();

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        final List<StepResult> results = new ArrayList<>();
        boolean success = true;

        public Builder add(final StepResult result) {
            results.add(result);
            if (!result.success()) success = false;
            return this;
        }

        public Builder error(final StepData data, Throwable exception) {
            return this.add(StepResult.error(data, exception));
        }

        public Builder success(final StepData data) {
            return this.add(StepResult.success(data));
        }

        public PipelineResult build() {
            final List<StepResult> finalResults = Collections.unmodifiableList(results);
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

    interface StepResult {

        boolean success();

        StepData data();

        Optional<Throwable> exception();

        static StepResult success(final StepData data) {
            return new StepResult() {
                @Override
                public boolean success() {
                    return true;
                }

                @Override
                public StepData data() {
                    return data;
                }

                @Override
                public Optional<Throwable> exception() {
                    return Optional.empty();
                }
            };
        }

        static StepResult error(final StepData data, final Throwable exception) {
            return new StepResult() {
                @Override
                public boolean success() {
                    return false;
                }

                @Override
                public StepData data() {
                    return data;
                }

                @Override
                public Optional<Throwable> exception() {
                    return Optional.of(exception);
                }
            };
        }
    }

}
