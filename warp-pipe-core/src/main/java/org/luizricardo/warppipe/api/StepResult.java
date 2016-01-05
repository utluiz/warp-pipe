package org.luizricardo.warppipe.api;

import java.util.Optional;

/**
 * Encapsulates the result of a {@link Step} execution for a given {@link StepData}.
 */
public interface StepResult {

    /**
     * @return Whether the execution was successfully or not.
     */
    boolean success();

    /**
     * @return Data being processed.
     */
    StepData data();

    /**
     * @return Optional exception in case of error.
     */
    Optional<Throwable> exception();

    /**
     * Default implementation for successful processing.
     * @param data Data being processed.
     */
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

    /**
     * Default implementation for unsuccessful processing.
     * @param data Data being processed.
     * @param exception Cause of the error.
     */
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
