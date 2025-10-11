package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class MaxMethodParametersTest {

    @Test
    void shouldNotThrowWhenMethodsDoNotExceedMaxParameters() {
        Taikai taikai = Taikai.builder()
                .classes(new ClassFileImporter().importClasses(ValidMethods.class))
                .java(java -> java.methodsShouldNotExceedMaxParameters(3))
                .build();

        assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodExceedsMaxParameters() {
        Taikai taikai = Taikai.builder()
                .classes(new ClassFileImporter().importClasses(InvalidMethods.class))
                .java(java -> java.methodsShouldNotExceedMaxParameters(2))
                .build();

        assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldAllowExactlyMaxParameters() {
        Taikai taikai = Taikai.builder()
                .classes(new ClassFileImporter().importClasses(ExactlyMaxMethods.class))
                .java(java -> java.methodsShouldNotExceedMaxParameters(3))
                .build();

        assertDoesNotThrow(taikai::check);
    }

    static class ValidMethods {

        void noParams() {
        }

        void oneParam(String name) {
        }

        void twoParams(String name, int age) {
        }
    }

    static class InvalidMethods {

        void tooManyParams(String a, String b, String c) {
        }
    }

    static class ExactlyMaxMethods {

        void exactlyThree(String a, String b, String c) {
        }
    }
}
