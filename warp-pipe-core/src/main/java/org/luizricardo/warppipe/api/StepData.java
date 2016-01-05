package org.luizricardo.warppipe.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * Encapsulates data to be processed by a {@link Step} in the request pipeline.
 * Each StepData instance refers to a region of the page that should be lazily rendered.
 * SteData instances are collected from specific tags in the output using a filter like {@link org.luizricardo.warppipe.WarpFilter}
 * or queued manually during the request, to be consumed after the main page processing is done.
 */
public interface StepData {

    /**
     * Identifies the kind of data. By default it'll be used by the {@link StepManager} to determine which {@link Step}
     * will process this data.
     */
    String id();

    /**
     * Priority which overrides the default one from {@link Step#defaultPriority(StepData, StepContext)}.
     */
    Optional<Integer> priority();

    /**
     * Additional attributes which can influence the {@link Step} processing, enabling reusing Steps in different contexts.
     */
    Map<String, String> attributes();

    /**
     * Provides default implementation.
     */
    static StepData create(final String id, final Optional<Integer> priority, final Map<String, String> attributes) {
        final Map<String, String> immutableAttributes = Collections.unmodifiableMap(new HashMap<>(attributes));
        return new StepData() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public Optional<Integer> priority() {
                return priority;
            }

            @Override
            public Map<String, String> attributes() {
                return immutableAttributes;
            }
        };
    }

}
