package org.luizricardo.warppipe.pipeline;

import org.junit.Before;
import org.junit.Test;
import org.luizricardo.warppipe.api.Pipeline;
import org.luizricardo.warppipe.api.PipelineResult;
import org.luizricardo.warppipe.api.StepContext;
import org.luizricardo.warppipe.api.StepData;
import org.luizricardo.warppipe.fakes.FakeHttpServletRequest;
import org.luizricardo.warppipe.fakes.FakeHttpServletResponse;
import org.luizricardo.warppipe.fakes.FakeStepManager;

import java.util.HashMap;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class QueuedPipelineTest {

    FakeStepManager stepManager = new FakeStepManager();
    StepContext stepContext;
    StepData data1 = StepData.create("id1", Optional.of(1), new HashMap<>());
    StepData data2 = StepData.create("id2", Optional.of(3), new HashMap<>());
    QueuedPipeline pipeline;
    PipelineResult result;

    public QueuedPipeline create(StepData... dataParam) throws Exception {
        stepContext = StepContext.create(new FakeHttpServletResponse().getWriter(), new FakeHttpServletRequest());

        Pipeline.Builder<QueuedPipeline> builder = QueuedPipeline.create(stepManager);
        for (StepData stepData : dataParam) {
            builder.include(stepData);
        }
        return builder.build();
    }

    @Before
    public void setup() throws Exception {
        pipeline = create(data1, data2);
        result = pipeline.execute(stepContext);
    }

    @Test
    public void allStepsExecuted() throws Exception {
        assertThat(stepManager.getExecutions(), hasItems(data1, data2));
        assertThat(result.results().size(), is(2));
        assertThat(result.success(), is(true));
    }

    @Test
    public void resultsFollowsStepsInOrder() throws Exception {
        assertThat(result.results().size(), is(2));
        assertThat(result.results().get(0).data(), is(data2));
        assertThat(result.results().get(0).success(), is(true));
        assertThat(result.results().get(0).exception(), is(Optional.empty()));
        assertThat(result.results().get(1).data(), is(data1));
        assertThat(result.results().get(1).success(), is(true));
        assertThat(result.results().get(1).exception(), is(Optional.empty()));
    }

    @Test
    public void checkError() throws Exception {
        StepData dataError = StepData.create("throw", Optional.of(2), new HashMap<>());
        QueuedPipeline pipelineError = create(data1, data2, dataError);
        PipelineResult result = pipelineError.execute(stepContext);
        assertThat(result.success(), is(false));
        assertThat(stepManager.getExecutions(), hasItems(data1, data2, dataError));
        assertThat(result.results().get(0).data(), is(data2));
        assertThat(result.results().get(0).success(), is(true));
        assertThat(result.results().get(0).exception(), is(Optional.empty()));
        assertThat(result.results().get(1).data(), is(dataError));
        assertThat(result.results().get(1).success(), is(false));
        assertThat(result.results().get(1).exception().get().getMessage(), is("throw"));
        assertThat(result.results().get(2).data(), is(data1));
        assertThat(result.results().get(2).success(), is(true));
        assertThat(result.results().get(2).exception(), is(Optional.empty()));
    }

}
