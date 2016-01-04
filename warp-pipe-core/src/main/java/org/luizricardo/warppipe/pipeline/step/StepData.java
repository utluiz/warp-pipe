package org.luizricardo.warppipe.pipeline.step;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;


public class StepData {

    private final String id;
    private final Optional<Integer> priority;
    private final Map<String, String> attributes;

    public StepData(final String id, final Optional<Integer> priority, final Map<String, String> attributes) {
        this.id = id;
        this.priority = priority;
        this.attributes = Collections.unmodifiableMap(attributes);
    }

    public String id() {
        return id;
    }

    public Optional<Integer> priority() {
        return priority;
    }

    public Map<String, String> attributes() {
        return attributes;
    }

}
