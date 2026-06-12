package com.enofex.taikai.test;

import com.enofex.taikai.Taikai;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JUnitConfigurerTest {

    @Test
    void shouldApplyMethodNameConvention() {
        Taikai taikai = Taikai.builder()
                .classes(ValidTestMethodName.class)
                .test(test -> test.junit(
                        junit -> junit.methodsShouldMatch("should[A-Z].*")
                ))
                .build();

        assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowOnInvalidMethodNameConvention() {
        Taikai taikai = Taikai.builder()
                .classes(InvalidTestMethodName.class)
                .test(test -> test.junit(
                        junit -> junit.methodsShouldMatch("should[A-Z].*")
                ))
                .build();

        assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldApplyNoDeclaredExceptionsRule() {
        Taikai taikai = Taikai.builder()
                .classes(ValidNoExceptionTest.class)
                .test(test -> test.junit(
                        JUnitConfigurer::methodsShouldNotDeclareExceptions
                ))
                .build();

        assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodDeclaresException() {
        Taikai taikai = Taikai.builder()
                .classes(InvalidExceptionTest.class)
                .test(test -> test.junit(
                        JUnitConfigurer::methodsShouldNotDeclareExceptions
                ))
                .build();

        assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldApplyDisplayNameAnnotationRule() {
        Taikai taikai = Taikai.builder()
                .classes(ValidDisplayNameTest.class)
                .test(test -> test.junit(
                        JUnitConfigurer::methodsShouldBeAnnotatedWithDisplayName
                ))
                .build();

        assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMissingDisplayNameAnnotation() {
        Taikai taikai = Taikai.builder()
                .classes(InvalidDisplayNameTest.class)
                .test(test -> test.junit(
                        JUnitConfigurer::methodsShouldBeAnnotatedWithDisplayName
                ))
                .build();

        assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldApplyPackagePrivateMethodRule() {
        Taikai taikai = Taikai.builder()
                .classes(ValidPackagePrivateMethodTest.class)
                .test(test -> test.junit(
                        JUnitConfigurer::methodsShouldBePackagePrivate
                ))
                .build();

        assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodIsNotPackagePrivate() {
        Taikai taikai = Taikai.builder()
                .classes(InvalidPackagePrivateMethodTest.class)
                .test(test -> test.junit(
                        JUnitConfigurer::methodsShouldBePackagePrivate
                ))
                .build();

        assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldApplyNoDisabledMethodRule() {
        Taikai taikai = Taikai.builder()
                .classes(ValidNoDisabledTest.class)
                .test(test -> test.junit(
                        JUnitConfigurer::methodsShouldNotBeAnnotatedWithDisabled
                ))
                .build();

        assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodIsDisabled() {
        Taikai taikai = Taikai.builder()
                .classes(InvalidDisabledTest.class)
                .test(test -> test.junit(
                        JUnitConfigurer::methodsShouldNotBeAnnotatedWithDisabled
                ))
                .build();

        assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldApplyNoDisabledClassRule() {
        Taikai taikai = Taikai.builder()
                .classes(ValidNoDisabledTest.class)
                .test(test -> test.junit(
                        JUnitConfigurer::classesShouldNotBeAnnotatedWithDisabled
                ))
                .build();

        assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenClassIsDisabled() {
        Taikai taikai = Taikai.builder()
                .classes(InvalidDisabledClass.class)
                .test(test -> test.junit(
                        JUnitConfigurer::classesShouldNotBeAnnotatedWithDisabled
                ))
                .build();

        assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldApplyPackagePrivateClassRule() {
        Taikai taikai = Taikai.builder()
                .classes(PackagePrivateTestClass.class)
                .test(test -> test.junit(
                        junit -> junit.classesShouldBePackagePrivate(".*PackagePrivateTestClass")
                ))
                .build();

        assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenClassIsNotPackagePrivate() {
        Taikai taikai = Taikai.builder()
                .classes(PublicTestClass.class)
                .test(test -> test.junit(
                        junit -> junit.classesShouldBePackagePrivate(".*PublicTestClass")
                ))
                .build();

        assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldApplyMethodsContainAssertionsOrVerificationsRule() {
        Taikai taikai = Taikai.builder()
                .classes(ValidAssertionTest.class)
                .test(test -> test.junit(
                        JUnitConfigurer::methodsShouldContainAssertionsOrVerifications
                ))
                .build();

        assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodDoesNotContainAssertionsOrVerifications() {
        Taikai taikai = Taikai.builder()
                .classes(InvalidAssertionTest.class)
                .test(test -> test.junit(
                        JUnitConfigurer::methodsShouldContainAssertionsOrVerifications
                ))
                .build();

        assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldDisableJUnitConfigurer() {
        Taikai taikai = Taikai.builder()
                .classes(InvalidDisplayNameTest.class)
                .test(test -> test.junit(junit -> {
                    junit.methodsShouldBeAnnotatedWithDisplayName();
                    junit.disable();
                }))
                .build();

        assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldApplyMethodsContainAssertionsOrVerificationsWithMockitoInOrder() {
        Taikai taikai = Taikai.builder()
                .classes(MockitoInOrderTest.class)
                .test(test -> test.junit(
                        JUnitConfigurer::methodsShouldContainAssertionsOrVerifications
                ))
                .build();

        assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldApplyMethodsContainAssertionsOrVerificationsWithArchRuleCheck() {
        Taikai taikai = Taikai.builder()
                .classes(ArchRuleCheckTest.class)
                .test(test -> test.junit(
                        JUnitConfigurer::methodsShouldContainAssertionsOrVerifications
                ))
                .build();

        assertDoesNotThrow(taikai::check);
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

    @Test
    void shouldApplyTestClassNameConvention() {
        Taikai taikai = Taikai.builder()
                .classes(ValidNoExceptionTest.class)
                .test(test -> test.junit(
                        junit -> junit.classesShouldMatch(".*Test")
                ))
                .build();

        assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowOnInvalidTestClassNameConvention() {
        Taikai taikai = Taikai.builder()
                .classes(ValidNoExceptionTest.class)
                .test(test -> test.junit(
                        junit -> junit.classesShouldMatch(".*IT")
                ))
                .build();

        assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldApplyClassesShouldEndWithTest() {
        Taikai taikai = Taikai.builder()
                .classes(ValidNoExceptionTest.class)
                .test(test -> test.junit(
                        JUnitConfigurer::classesShouldEndWithTest
                ))
                .build();

        assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotApplyTestClassNameConventionToClassesWithoutTests() {
        Taikai taikai = Taikai.builder()
                .classes(HelperWithoutTests.class)
                .test(test -> test.junit(
                        junit -> junit.classesShouldMatch(".*IT")
                ))
                .build();

        assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationForClassesShouldEndWithTest() {
        Taikai taikai = Taikai.builder()
                .classes(ValidNoExceptionTest.class)
                .test(test -> test.junit(
                        junit -> junit.classesShouldEndWithTest(
                                com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())
                ))
                .build();

        assertDoesNotThrow(taikai::check);
    }

    static class ValidTestMethodName {
        @Test
        void shouldDoSomething() {}
    }

    static class HelperWithoutTests {
        void doSomething() {}
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

    static class MockitoInOrderTest {
        Object mock = Mockito.mock(Object.class);

        @Test
        void shouldVerifyWithInOrder() {
            Mockito.inOrder(mock);
        }
    }

    static class ArchRuleCheckTest {
        ArchRule rule = ArchRuleDefinition.classes().should().bePublic();

        @Test
        void shouldVerifyArchRule() {
            rule.check(new ClassFileImporter().importClasses(Object.class));
        }
    }
}
