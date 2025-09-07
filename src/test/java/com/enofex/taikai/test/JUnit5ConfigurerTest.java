package com.enofex.taikai.test;

import com.enofex.taikai.Taikai;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JUnit5ConfigurerTest {

    @Test
    void shouldApplyMethodNameConvention() {
        Taikai taikai = Taikai.builder()
                .classes(new ClassFileImporter().importClasses(ValidTestMethodName.class))
                .test(test -> test.junit5(
                        junit5 -> junit5.methodsShouldMatch("should[A-Z].*")
                ))
                .build();

        assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowOnInvalidMethodNameConvention() {
        Taikai taikai = Taikai.builder()
                .classes(new ClassFileImporter().importClasses(InvalidTestMethodName.class))
                .test(test -> test.junit5(
                        junit5 -> junit5.methodsShouldMatch("should[A-Z].*")
                ))
                .build();

        assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldApplyNoDeclaredExceptionsRule() {
        Taikai taikai = Taikai.builder()
                .classes(new ClassFileImporter().importClasses(ValidNoExceptionTest.class))
                .test(test -> test.junit5(
                        JUnit5Configurer::methodsShouldNotDeclareExceptions
                ))
                .build();

        assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodDeclaresException() {
        Taikai taikai = Taikai.builder()
                .classes(new ClassFileImporter().importClasses(InvalidExceptionTest.class))
                .test(test -> test.junit5(
                        JUnit5Configurer::methodsShouldNotDeclareExceptions
                ))
                .build();

        assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldApplyDisplayNameAnnotationRule() {
        Taikai taikai = Taikai.builder()
                .classes(new ClassFileImporter().importClasses(ValidDisplayNameTest.class))
                .test(test -> test.junit5(
                        JUnit5Configurer::methodsShouldBeAnnotatedWithDisplayName
                ))
                .build();

        assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMissingDisplayNameAnnotation() {
        Taikai taikai = Taikai.builder()
                .classes(new ClassFileImporter().importClasses(InvalidDisplayNameTest.class))
                .test(test -> test.junit5(
                        JUnit5Configurer::methodsShouldBeAnnotatedWithDisplayName
                ))
                .build();

        assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldApplyPackagePrivateMethodRule() {
        Taikai taikai = Taikai.builder()
                .classes(new ClassFileImporter().importClasses(ValidPackagePrivateMethodTest.class))
                .test(test -> test.junit5(
                        JUnit5Configurer::methodsShouldBePackagePrivate
                ))
                .build();

        assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodIsNotPackagePrivate() {
        Taikai taikai = Taikai.builder()
                .classes(new ClassFileImporter().importClasses(InvalidPackagePrivateMethodTest.class))
                .test(test -> test.junit5(
                        JUnit5Configurer::methodsShouldBePackagePrivate
                ))
                .build();

        assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldApplyNoDisabledMethodRule() {
        Taikai taikai = Taikai.builder()
                .classes(new ClassFileImporter().importClasses(ValidNoDisabledTest.class))
                .test(test -> test.junit5(
                        JUnit5Configurer::methodsShouldNotBeAnnotatedWithDisabled
                ))
                .build();

        assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodIsDisabled() {
        Taikai taikai = Taikai.builder()
                .classes(new ClassFileImporter().importClasses(InvalidDisabledTest.class))
                .test(test -> test.junit5(
                        JUnit5Configurer::methodsShouldNotBeAnnotatedWithDisabled
                ))
                .build();

        assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldApplyNoDisabledClassRule() {
        Taikai taikai = Taikai.builder()
                .classes(new ClassFileImporter().importClasses(ValidNoDisabledTest.class))
                .test(test -> test.junit5(
                        JUnit5Configurer::classesShouldNotBeAnnotatedWithDisabled
                ))
                .build();

        assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenClassIsDisabled() {
        Taikai taikai = Taikai.builder()
                .classes(new ClassFileImporter().importClasses(InvalidDisabledClass.class))
                .test(test -> test.junit5(
                        JUnit5Configurer::classesShouldNotBeAnnotatedWithDisabled
                ))
                .build();

        assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldApplyPackagePrivateClassRule() {
        Taikai taikai = Taikai.builder()
                .classes(new ClassFileImporter().importClasses(PackagePrivateTestClass.class))
                .test(test -> test.junit5(
                        junit5 -> junit5.classesShouldBePackagePrivate(".*PackagePrivateTestClass")
                ))
                .build();

        assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenClassIsNotPackagePrivate() {
        Taikai taikai = Taikai.builder()
                .classes(new ClassFileImporter().importClasses(PublicTestClass.class))
                .test(test -> test.junit5(
                        junit5 -> junit5.classesShouldBePackagePrivate(".*PublicTestClass")
                ))
                .build();

        assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldApplyMethodsContainAssertionsOrVerificationsRule() {
        Taikai taikai = Taikai.builder()
                .classes(new ClassFileImporter().importClasses(ValidAssertionTest.class))
                .test(test -> test.junit5(
                        JUnit5Configurer::methodsShouldContainAssertionsOrVerifications
                ))
                .build();

        assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodDoesNotContainAssertionsOrVerifications() {
        Taikai taikai = Taikai.builder()
                .classes(new ClassFileImporter().importClasses(InvalidAssertionTest.class))
                .test(test -> test.junit5(
                        JUnit5Configurer::methodsShouldContainAssertionsOrVerifications
                ))
                .build();

        assertThrows(AssertionError.class, taikai::check);
    }

   static class ValidAssertionTest {
        @Test
        void shouldContainAssertion() {
            assertTrue(true);
        }
    }

    static class InvalidAssertionTest {
        @Test
        void shouldNotContainAssertion() {
            int a = 1 + 1; // no assertion here
        }
    }

    static class ValidTestMethodName {
        @Test
        void shouldDoSomething() {}
    }

    static class InvalidTestMethodName {
        @Test
        void testSomething() {}
    }

    static class ValidNoExceptionTest {
        @Test
        void shouldWork() {}
    }

    static class InvalidExceptionTest {
        @Test
        void shouldFail() throws Exception {}
    }

    static class ValidDisplayNameTest {
        @Test
        @DisplayName("Descriptive name")
        void shouldWork() {}
    }

    static class InvalidDisplayNameTest {
        @Test
        void shouldFail() {}
    }

    static class ValidPackagePrivateMethodTest {
        @Test
        void shouldBePackagePrivate() {}
    }

    static class InvalidPackagePrivateMethodTest {
        @Test
        public void shouldNotBePublic() {}
    }

    static class ValidNoDisabledTest {
        @Test
        void shouldBeEnabled() {}
    }

    static class InvalidDisabledTest {
        @Test
        @Disabled
        void shouldNotBeDisabled() {}
    }

    static class PackagePrivateTestClass {
        @Test
        void shouldWork() {}
    }

    public static class PublicTestClass {
        @Test
        void shouldFail() {}
    }

    @Disabled
    public static class InvalidDisabledClass {
        @Test
        void shouldNotRun() {}
    }
}
