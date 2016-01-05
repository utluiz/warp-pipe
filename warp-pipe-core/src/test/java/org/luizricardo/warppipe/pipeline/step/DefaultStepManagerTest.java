package org.luizricardo.warppipe.pipeline.step;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.luizricardo.warppipe.api.Step;
import org.luizricardo.warppipe.api.StepContext;
import org.luizricardo.warppipe.api.StepData;
import org.luizricardo.warppipe.fakes.FakeHttpServletRequest;
import org.luizricardo.warppipe.fakes.FakeHttpServletResponse;
import org.luizricardo.warppipe.pipeline.PipelineException;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

public class DefaultStepManagerTest {

    StepContext stepContext;
    TextStep textStep = new TextStep("Hello");
    ErrorStep errorStep = new ErrorStep();
    FakeHttpServletResponse response = new FakeHttpServletResponse();

    public DefaultStepManager create() throws Exception {
        stepContext = StepContext.create(response.getWriter(), new FakeHttpServletRequest());
        Map<String, Step> stepMap = new HashMap<>();
        stepMap.put("text", textStep);
        stepMap.put("error", errorStep);
        return new DefaultStepManager(stepMap);
    }

    @Test
    public void resolveStepById() throws Exception {
        StepData stepData = StepData.create("text", Optional.empty(), new HashMap<>());
        assertThat(create().resolveStep(stepData), is(textStep));
    }

    @Test
    public void resolveStepByAttribute() throws Exception {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("step-class", "org.luizricardo.warppipe.pipeline.step.TextStep");
        StepData anotherData = StepData.create("bla", Optional.ofNullable(1), attributes);
        Step result = create().resolveStep(anotherData);
        assertThat(result, notNullValue());
        assertThat(result, not(sameInstance(textStep)));
    }

    @Test
    public void cacheResolvedSteps() throws Exception {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("step-class", "org.luizricardo.warppipe.pipeline.step.TextStep");
        StepData data = StepData.create("bla", Optional.ofNullable(1), attributes);
        StepData anotherData = StepData.create("bla", Optional.ofNullable(1), attributes);
        attributes.put("step-class", "org.luizricardo.warppipe.pipeline.step.ErrorStep");
        StepData errorData = StepData.create("blaerror", Optional.empty(), attributes);
        DefaultStepManager manager = create();
        assertThat(manager.instanceCache.size(), is(0));
        manager.resolveStep(data);
        assertThat(manager.instanceCache.size(), is(1));
        assertThat(manager.instanceCache.keySet(), hasItem("org.luizricardo.warppipe.pipeline.step.TextStep"));
        manager.resolveStep(data);
        manager.resolveStep(anotherData);
        assertThat(manager.instanceCache.size(), is(1));
        manager.resolveStep(errorData);
        assertThat(manager.instanceCache.size(), is(2));
        assertThat(manager.instanceCache.keySet(), hasItems("org.luizricardo.warppipe.pipeline.step.TextStep", "org.luizricardo.warppipe.pipeline.step.ErrorStep"));
    }

    @Test(expected = PipelineException.class)
    public void resolveStepFailure() throws Exception {
        StepData anotherData = StepData.create("bla", Optional.ofNullable(1), new HashMap<>());
        create().resolveStep(anotherData);
    }

    @Test
    public void executeSimpleStep() throws Exception {
        StepData stepData = StepData.create("text", Optional.empty(), new HashMap<>());
        create().execute(stepData, stepContext);
        assertThat(new String(response.getOutput().toByteArray(), StandardCharsets.UTF_8), is("Hello"));
    }

    @Test
    public void getPriority() throws Exception {
        StepData stepData = StepData.create("text", Optional.empty(), new HashMap<>());
        assertThat(create().defaultPriority(stepData, stepContext).get(), is(textStep.defaultPriority(null, null).get()));
    }

    @Test(expected = PipelineException.class)
    public void executionError() throws Exception {
        StepData stepData = StepData.create("error", Optional.empty(), new HashMap<>());
        create().execute(stepData, stepContext);
    }

    @Test()
    public void getPriorityErrorThenEmpty() throws Exception {
        StepData stepData = StepData.create("error", Optional.empty(), new HashMap<>());
        assertThat(create().defaultPriority(stepData, stepContext), is(Optional.empty()));
    }

    @Test()
    public void getPriorityNotResolvingThenEmpty() throws Exception {
        StepData stepData = StepData.create("bla", Optional.empty(), new HashMap<>());
        assertThat(create().defaultPriority(stepData, stepContext), is(Optional.empty()));
    }

}
