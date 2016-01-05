package org.luizricardo.warppipe.pipeline;


import org.luizricardo.warppipe.api.StepData;

public class PipelineException extends Exception {

    private StepData itemFailed;

    public PipelineException(String message, Throwable cause, StepData itemFailed) {
        super(message, cause);
        this.itemFailed = itemFailed;
    }

    public PipelineException(String message, StepData itemFailed) {
        super(message);
        this.itemFailed = itemFailed;
    }

    public StepData itemFailed() {
        return itemFailed;
    }

}
