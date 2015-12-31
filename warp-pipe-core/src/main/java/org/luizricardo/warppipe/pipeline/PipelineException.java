package org.luizricardo.warppipe.pipeline;


public class PipelineException extends Exception {

    private PipelineItem itemFailed;

    public PipelineException(String message, Throwable cause, PipelineItem itemFailed) {
        super(message, cause);
        this.itemFailed = itemFailed;
    }

    public PipelineException(String message, PipelineItem itemFailed) {
        super(message);
        this.itemFailed = itemFailed;
    }

    public PipelineItem itemFailed() {
        return itemFailed;
    }
}
