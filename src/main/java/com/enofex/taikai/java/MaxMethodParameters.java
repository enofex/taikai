package com.enofex.taikai.java;

import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

final class MaxMethodParameters {

    private MaxMethodParameters() {
    }

    static ArchCondition<JavaMethod> notExceedMaxParameters(int maxMethodParameters) {
        return new ArchCondition<>("not have more than %d parameters".formatted(maxMethodParameters)) {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                int parameterCount = method.getRawParameterTypes().size();

                if (parameterCount > maxMethodParameters) {
                    String message = "Method %s has %d parameters, max allowed: %d".formatted(
                            method.getFullName(), parameterCount, maxMethodParameters);
                    events.add(SimpleConditionEvent.violated(method, message));
                }
            }
        };
    }
}
