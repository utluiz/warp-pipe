package org.luizricardo.warppipe;


import org.luizricardo.warppipe.pipeline.PipelineAction;
import org.luizricardo.warppipe.pipeline.PipelineItem;

import java.io.IOException;

public class TextPipelineAction implements PipelineAction {

    private final String text;

    public TextPipelineAction(final String text) {
        this.text = text;
    }

    @Override
    public void execute(final PipelineItem item) throws IOException {
        item.writer().write(text);
    }

}
