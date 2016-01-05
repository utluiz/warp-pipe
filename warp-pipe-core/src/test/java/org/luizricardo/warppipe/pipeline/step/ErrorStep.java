package org.luizricardo.warppipe.pipeline.step;


import org.luizricardo.warppipe.api.Step;
import org.luizricardo.warppipe.api.StepContext;
import org.luizricardo.warppipe.api.StepData;
import org.luizricardo.warppipe.pipeline.Priority;

import java.io.IOException;
import java.util.Optional;

public class ErrorStep implements Step {

    private final String text;

    public ErrorStep() {
        text = "NONE";
    }

    public ErrorStep(final String text) {
        this.text = text;
    }

    @Override
    public void execute(final StepData data, final StepContext context) throws IOException {
        throw new NullPointerException("Nll");
    }

    @Override
    public Optional<Integer> defaultPriority(final StepData data, final StepContext context) {
        throw new NullPointerException("Nll");
    }
}
