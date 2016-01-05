package org.luizricardo.warppipe.fakes;

import org.luizricardo.warppipe.api.StepContext;
import org.luizricardo.warppipe.api.StepData;
import org.luizricardo.warppipe.api.StepManager;
import org.luizricardo.warppipe.pipeline.PipelineException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FakeStepManager implements StepManager {

    private Optional<Integer> priority = Optional.empty();
    private List<StepData> executions = new ArrayList<>();
    private StepData stepData;
    private StepContext stepContext;

    @Override
    public void execute(StepData stepData, StepContext stepContext) throws PipelineException {
        executions.add(stepData);
        this.stepData = stepData;
        this.stepContext = stepContext;
        if (stepData.id().equals("throw")) {
            throw new PipelineException(stepData.id(), stepData);
        }
    }

    @Override
    public Optional<Integer> defaultPriority(final StepData stepData, StepContext stepContext) throws PipelineException {
        executions.add(stepData);
        this.stepData = stepData;
        this.stepContext = stepContext;
        return priority;
    }


    public FakeStepManager setPriority(Optional<Integer> priority) {
        this.priority = priority;
        return this;
    }

    public StepContext getStepContext() {
        return stepContext;
    }

    public StepData getStepData() {
        return stepData;
    }

    public List<StepData> getExecutions() {
        return executions;
    }

}
