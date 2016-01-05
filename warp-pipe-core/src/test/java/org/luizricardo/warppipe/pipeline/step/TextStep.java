package org.luizricardo.warppipe.pipeline.step;


import org.luizricardo.warppipe.api.Step;
import org.luizricardo.warppipe.api.StepContext;
import org.luizricardo.warppipe.api.StepData;
import org.luizricardo.warppipe.pipeline.Priority;

import java.io.IOException;
import java.util.Optional;

public class TextStep implements Step {

    private final String text;

    public TextStep() {
        text = "NONE";
    }

    public TextStep(final String text) {
        this.text = text;
    }

    @Override
    public void execute(final StepData data, final StepContext context) throws IOException {
        context.writer().write(text);
        context.writer().flush();
    }

    @Override
    public Optional<Integer> defaultPriority(final StepData data, final StepContext context) {
        return Optional.of(Priority.HIGHER_PRIORITY);
    }
}
