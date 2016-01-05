package org.luizricardo.warppipe.pipeline;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.luizricardo.warppipe.api.StepContext;
import org.luizricardo.warppipe.api.StepData;
import org.luizricardo.warppipe.fakes.FakeHttpServletRequest;
import org.luizricardo.warppipe.fakes.FakeHttpServletResponse;
import org.luizricardo.warppipe.fakes.FakeStepManager;

import java.util.HashMap;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PriorityTest {

    FakeStepManager stepManager;
    StepData stepData;
    StepContext stepContext;

    public Integer resolve(Integer dataPriority, Integer stepPriority) throws Exception {
        stepData = StepData.create("id", Optional.ofNullable(dataPriority), new HashMap<>());
        stepContext = StepContext.create(new FakeHttpServletResponse().getWriter(), new FakeHttpServletRequest());
        stepManager = new FakeStepManager().setPriority(Optional.ofNullable(stepPriority));
        return Priority.resolve(stepData, stepContext, stepManager);
    }

    @Test
    public void dataPriority() throws Exception {
        assertThat(resolve(1, null), is(1));
        assertThat(stepManager.getStepContext(), CoreMatchers.nullValue());
        assertThat(stepManager.getStepData(), CoreMatchers.nullValue());
    }

    @Test
    public void defaultPriority() throws Exception {
        assertThat(resolve(null, null), is(Priority.DEFAULT_PRIORITY));
        assertThat(stepManager.getStepContext(), is(stepContext));
        assertThat(stepManager.getStepData(), is(stepData));
    }

    @Test
    public void stepPriority() throws Exception {
        assertThat(resolve(null, 2), is(2));
    }

    @Test
    public void overriddenPriority() throws Exception {
        assertThat(resolve(3, 2), is(3));
    }

    @Test
    public void comparisonPriority() throws Exception {
        assertThat(resolve(2, null).compareTo(resolve(1,null)), is(1));
        assertThat(resolve(1, null).compareTo(resolve(2,null)), is(-1));
        assertThat(resolve(2, null).compareTo(resolve(2,null)), is(0));
    }

    @Test
    public void comparisonDefaultPriorities() throws Exception {
        assertThat(resolve(Priority.HIGHER_PRIORITY, null).compareTo(resolve(Priority.DEFAULT_PRIORITY,null)), is(1));
        assertThat(resolve(Priority.DEFAULT_PRIORITY, null).compareTo(resolve(Priority.LOWER_PRIORITY,null)), is(1));
    }

}
