package org.luizricardo.warppipe;


import org.luizricardo.warppipe.pipeline.Context;
import org.luizricardo.warppipe.pipeline.step.Step;
import org.luizricardo.warppipe.pipeline.step.StepData;

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
    public void execute(StepData data, Context context) throws IOException {
        context.writer().write(text);
    }

    @Override
    public Optional<Integer> priority() {
        return Optional.empty();
    }
}
