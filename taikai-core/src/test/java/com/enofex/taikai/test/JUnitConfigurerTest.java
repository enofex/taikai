package com.enofex.taikai.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.Taikai;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

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
            junit -> junit.classesShouldBePackagePrivate(ClassCriteria.nameMatching(".*PackagePrivateTestClass"))
        ))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldThrowWhenClassIsNotPackagePrivate() {
    Taikai taikai = Taikai.builder()
        .classes(PublicTestClass.class)
        .test(test -> test.junit(
            junit -> junit.classesShouldBePackagePrivate(ClassCriteria.nameMatching(".*PublicTestClass"))
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

  @Test
  void shouldApplyMethodsContainAssertionsOrVerificationsWithHelperMethod() {
    Taikai taikai = Taikai.builder()
        .classes(HelperMethodAssertionTest.class)
        .test(test -> test.junit(
            JUnitConfigurer::methodsShouldContainAssertionsOrVerifications
        ))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldApplyMethodsContainAssertionsOrVerificationsWithInheritedHelperMethod() {
    Taikai taikai = Taikai.builder()
        .classes(InheritedHelperMethodAssertionTest.class, BaseAssertionTest.class)
        .test(test -> test.junit(
            JUnitConfigurer::methodsShouldContainAssertionsOrVerifications
        ))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldThrowWhenRecursiveHelperMethodsDoNotContainAssertionsOrVerifications() {
    Taikai taikai = Taikai.builder()
        .classes(RecursiveHelperMethodWithoutAssertionTest.class)
        .test(test -> test.junit(
            JUnitConfigurer::methodsShouldContainAssertionsOrVerifications
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

  static class HelperMethodAssertionTest {
    @Test
    void shouldContainAssertionInHelperMethod() {
      assertHelper();
    }

    @Test
    void shouldContainAssertionInNestedHelperMethod() {
      nestedHelper();
    }

    private static void nestedHelper() {
      assertHelper();
    }

    private static void assertHelper() {
      assertTrue(true);
    }
  }

  abstract static class BaseAssertionTest {
    protected void assertHelper() {
      assertTrue(true);
    }
  }

  static class InheritedHelperMethodAssertionTest extends BaseAssertionTest {
    @Test
    void shouldContainAssertionInInheritedHelperMethod() {
      assertHelper();
    }
  }

  static class RecursiveHelperMethodWithoutAssertionTest {
    @Test
    void shouldNotContainAssertion() {
      ping(1);
    }

    private static void ping(int n) {
      if (n > 0) {
        pong(n - 1);
      }
    }

    private static void pong(int n) {
      if (n > 0) {
        ping(n - 1);
      }
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

  @Test
  void shouldApplyNestedClassesShouldBeAnnotatedWithNested() {
    Taikai taikai = Taikai.builder()
        .classes(ValidNestedTest.Inner.class)
        .failOnEmpty(true)
        .test(test -> test.junit(
            JUnitConfigurer::nestedClassesShouldBeAnnotatedWithNested
        ))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldApplyNestedClassesShouldBeAnnotatedWithNestedWhenMetaAnnotated() {
    Taikai taikai = Taikai.builder()
        .classes(ValidMetaNestedTest.Inner.class)
        .failOnEmpty(true)
        .test(test -> test.junit(
            JUnitConfigurer::nestedClassesShouldBeAnnotatedWithNested
        ))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldThrowWhenInnerClassWithTestMethodsIsNotAnnotatedWithNested() {
    Taikai taikai = Taikai.builder()
        .classes(MissingNestedTest.Inner.class)
        .test(test -> test.junit(
            JUnitConfigurer::nestedClassesShouldBeAnnotatedWithNested
        ))
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldThrowWhenInnerClassWithParameterizedTestMethodsIsNotAnnotatedWithNested() {
    Taikai taikai = Taikai.builder()
        .classes(MissingNestedParameterizedTest.Inner.class)
        .test(test -> test.junit(
            JUnitConfigurer::nestedClassesShouldBeAnnotatedWithNested
        ))
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldNotApplyNestedClassesShouldBeAnnotatedWithNestedToStaticNestedClasses() {
    Taikai taikai = Taikai.builder()
        .classes(StaticNestedTest.Inner.class)
        .test(test -> test.junit(
            JUnitConfigurer::nestedClassesShouldBeAnnotatedWithNested
        ))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldNotApplyNestedClassesShouldBeAnnotatedWithNestedToAbstractInnerClasses() {
    Taikai taikai = Taikai.builder()
        .classes(AbstractInnerTest.Inner.class)
        .test(test -> test.junit(
            JUnitConfigurer::nestedClassesShouldBeAnnotatedWithNested
        ))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldNotApplyNestedClassesShouldBeAnnotatedWithNestedToInnerClassesWithoutTests() {
    Taikai taikai = Taikai.builder()
        .classes(InnerHelperTest.Helper.class)
        .test(test -> test.junit(
            JUnitConfigurer::nestedClassesShouldBeAnnotatedWithNested
        ))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldSupportConfigurationForNestedClassesShouldBeAnnotatedWithNested() {
    Taikai taikai = Taikai.builder()
        .classes(MissingNestedTest.Inner.class)
        .test(test -> test.junit(
            junit -> junit.nestedClassesShouldBeAnnotatedWithNested(
                com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())
        ))
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldApplyTestsShouldBeExecutable() {
    Taikai taikai = Taikai.builder()
        .classes(ValidTestDeclarationsTest.class, ValidTestDeclarationsTest.Inner.class,
            PrivateAbstractTest.class, NestedAnnotation.class, ValidMetaNestedTest.Inner.class)
        .test(test -> test.junit(
            JUnitConfigurer::testsShouldBeExecutable
        ))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @ParameterizedTest
  @MethodSource("nonExecutableTests")
  void shouldThrowWhenTestsAreNotExecutable(Class<?> fixture, String expectedViolation) {
    Taikai taikai = Taikai.builder()
        .classes(fixture)
        .test(test -> test.junit(
            JUnitConfigurer::testsShouldBeExecutable
        ))
        .build();

    AssertionError error = assertThrows(AssertionError.class, taikai::check);
    assertTrue(error.getMessage().contains(fixture.getName()), error.getMessage());
    assertTrue(error.getMessage().contains(expectedViolation), error.getMessage());
  }

  static Stream<Arguments> nonExecutableTests() {
    return Stream.of(
        Arguments.of(PrivateTestMethodTest.class, "annotated with @Test is private"),
        Arguments.of(StaticTestMethodTest.class, "annotated with @Test is static"),
        Arguments.of(TestMethodReturningValueTest.class, "annotated with @Test returns a value"),
        Arguments.of(MetaAnnotatedPrivateTestMethodTest.class, "annotated with @UnitTest is private"),
        Arguments.of(ParameterizedTestReturningValueTest.class, "annotated with @ParameterizedTest returns a value"),
        Arguments.of(StaticRepeatedTestTest.class, "annotated with @RepeatedTest is static"),
        Arguments.of(PrivateTestFactoryTest.class, "annotated with @TestFactory is private"),
        Arguments.of(TestFactoryReturningVoidTest.class, "annotated with @TestFactory does not return a DynamicNode"),
        Arguments.of(TestFactoryReturningStringsTest.class, "annotated with @TestFactory does not return a DynamicNode"),
        Arguments.of(TestFactoryReturningStringArrayTest.class, "annotated with @TestFactory does not return a DynamicNode"),
        Arguments.of(TestFactoryReturningLowerBoundTest.class, "annotated with @TestFactory does not return a DynamicNode"),
        Arguments.of(StaticNestedClassTest.Inner.class, "annotated with @Nested is static"),
        Arguments.of(PrivateNestedClassTest.Inner.class, "annotated with @Nested is private"),
        Arguments.of(PrivateTestClass.class, "containing test methods is private"),
        Arguments.of(localTestClass(), "containing test methods is a local class"),
        Arguments.of(anonymousTestClass(), "containing test methods is an anonymous class"));
  }

  @Test
  void shouldSupportConfigurationForTestsShouldBeExecutable() {
    Taikai taikai = Taikai.builder()
        .classes(PrivateTestMethodTest.class)
        .test(test -> test.junit(
            junit -> junit.testsShouldBeExecutable(
                com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())
        ))
        .build();

    assertThrows(AssertionError.class, taikai::check);
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
      Mockito.inOrder(this.mock);
    }
  }

  static class ArchRuleCheckTest {
    ArchRule rule = ArchRuleDefinition.classes().should().bePublic();

    @Test
    void shouldVerifyArchRule() {
      this.rule.check(new ClassFileImporter().importClasses(Object.class));
    }
  }

  static class ValidNestedTest {
    @Nested
    class Inner {
      @Test
      void shouldWork() {}
    }
  }

  static class ValidMetaNestedTest {
    @NestedAnnotation
    class Inner {
      @Test
      void shouldWork() {}
    }
  }

  static class MissingNestedTest {
    class Inner {
      @Test
      void shouldNotRun() {}
    }
  }

  static class MissingNestedParameterizedTest {
    class Inner {
      @ParameterizedTest
      @EmptySource
      void shouldNotRun(String empty) {}
    }
  }

  static class StaticNestedTest {
    static class Inner {
      @Test
      void shouldWork() {}
    }
  }

  static class AbstractInnerTest {
    abstract class Inner {
      @Test
      void shouldWork() {}
    }
  }

  static class InnerHelperTest {
    class Helper {
      void doSomething() {}
    }
  }

  static class ValidTestDeclarationsTest {
    @BeforeAll
    static void setUp() {}

    @Test
    void shouldRun() {}

    @UnitTest
    void shouldRunMetaAnnotated() {}

    @ParameterizedTest
    @EmptySource
    void shouldRunParameterized(String empty) {}

    @RepeatedTest(2)
    void shouldRunRepeated() {}

    @TestFactory
    DynamicTest shouldCreateDynamicTest() {
      return DynamicTest.dynamicTest("test", () -> {});
    }

    @TestFactory
    DynamicNode[] shouldCreateArray() {
      return new DynamicNode[0];
    }

    @TestFactory
    Stream<DynamicTest> shouldCreateStream() {
      return Stream.empty();
    }

    @TestFactory
    Collection<DynamicNode> shouldCreateCollection() {
      return List.of();
    }

    @TestFactory
    Iterator<? extends DynamicNode> shouldCreateIterator() {
      return Collections.emptyIterator();
    }

    @TestFactory
    Stream<?> shouldCreateUnboundedStream() {
      return Stream.empty();
    }

    @TestFactory
    DynamicTestProvider shouldCreateIteratorProvider() {
      return new DynamicTestProvider();
    }

    @TestFactory
    Object shouldCreateObject() {
      return Stream.empty();
    }

    private void helper() {}

    static String staticHelper() {
      return "";
    }

    @Nested
    class Inner {
      @Test
      void shouldRunNested() {}
    }
  }

  static class DynamicTestProvider {
    Iterator<DynamicTest> iterator() {
      return Collections.emptyIterator();
    }
  }

  private abstract static class PrivateAbstractTest {
    @Test
    void shouldRunInSubclass() {}
  }

  static class PrivateTestMethodTest {
    @Test
    private void shouldNotRun() {}
  }

  static class StaticTestMethodTest {
    @Test
    static void shouldNotRun() {}
  }

  static class TestMethodReturningValueTest {
    @Test
    String shouldNotRun() {
      return "";
    }
  }

  static class MetaAnnotatedPrivateTestMethodTest {
    @UnitTest
    private void shouldNotRun() {}
  }

  static class ParameterizedTestReturningValueTest {
    @ParameterizedTest
    @EmptySource
    int shouldNotRun(String empty) {
      return 0;
    }
  }

  static class StaticRepeatedTestTest {
    @RepeatedTest(2)
    static void shouldNotRun() {}
  }

  static class PrivateTestFactoryTest {
    @TestFactory
    private Stream<DynamicTest> shouldNotRun() {
      return Stream.empty();
    }
  }

  static class TestFactoryReturningVoidTest {
    @TestFactory
    void shouldNotRun() {}
  }

  static class TestFactoryReturningStringsTest {
    @TestFactory
    List<String> shouldNotRun() {
      return List.of();
    }
  }

  static class TestFactoryReturningStringArrayTest {
    @TestFactory
    String[] shouldNotRun() {
      return new String[0];
    }
  }

  static class TestFactoryReturningLowerBoundTest {
    @TestFactory
    Stream<? super DynamicTest> shouldNotRun() {
      return Stream.empty();
    }
  }

  static class StaticNestedClassTest {
    @Nested
    static class Inner {
      @Test
      void shouldNotRun() {}
    }
  }

  static class PrivateNestedClassTest {
    @Nested
    private class Inner {
      @Test
      void shouldNotRun() {}
    }
  }

  private static class PrivateTestClass {
    @Test
    void shouldNotRun() {}
  }

  static Class<?> localTestClass() {
    class LocalTest {
      @Test
      void shouldNotRun() {}
    }

    return LocalTest.class;
  }

  static Class<?> anonymousTestClass() {
    return new Object() {
      @Test
      void shouldNotRun() {}
    }.getClass();
  }

  @Nested
  @interface NestedAnnotation {
  }

  @Test
  @interface UnitTest {
  }
}
