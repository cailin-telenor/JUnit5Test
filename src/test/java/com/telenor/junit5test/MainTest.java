/**
 * The code is the presentation.
 *
 * For more information about JUnit 5, see here: http://junit.org/junit5/
 *
 * - JUnit requires Java 8
 * - JUnit 4 and 5 tests can run together (but not within the same test). How to do this
 *   is outside the scope of this presentation.
 * - Neat new features, some features slightly changed.
 *
 * To follow along on your laptop, check out the source code from
 * https://github.com/cailin-telenor/JUnit5Test
 *
 * Need some things in the POM also
 *
 * Shameless theft of slides from here: https://codefx-org.github.io/talk-junit-5
 */
package com.telenor.junit5test;

// Main import, imports most of the interfaces used, such as @Test etc
import org.junit.jupiter.api.*;
// Used for paramter injection
import org.junit.jupiter.api.extension.ParameterResolver;

// Generic imports
import java.util.Random;
import java.util.stream.Stream;

// Static import of things like assertEquals, etc
import static org.junit.jupiter.api.Assertions.*;

// We also want to use Matchers from Hamcrest
import static org.hamcrest.Matchers.*;

// Extension related imports
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 *
 * @author cailin
 */
// This annotation is explained below
@ExtendWith(MainTest.IntegerParameterResolver.class)
@ExtendWith(MainTest.ExtensionContextParameterResolver.class)
//@RunWith(JUnitPlatform.class)
public class MainTest {

    // New names for lifecycle annotations
    @BeforeAll
    static // Note that just like junit 4, this must be static
    // public // Note that public visibility is no longer needed! Package visibility suffices.
    void beforeAll() {
        System.err.println("I am run once before the class is run");
    }

    @BeforeEach // Before this was @Before
    void beforeEach() {
        System.out.println("I am run before each test");
    }

    @AfterEach // Before this was @After
    void afterEach() {
        System.out.println("I am run after each test");
    }

    @AfterAll
    static // Also must be static
    void afterAll() {
        System.out.println("I am run once after all class tests are completed");
    }

    @Test
    @DisplayName("Temporarily disabled")
    @Disabled("because") // Not @Ignored
    void test0() {

    }

    @Test
    @DisplayName("THIS IS TEST 1")
    void test1() {
        // Message comes last now
        assertEquals(1, 1, "The message comes last now");
    }

    @Test
    @DisplayName("THIS IS TEST 2")
    void test2() {
        System.out.println("I am also a test");
    }

    @Test
    @DisplayName("Multi failure")
    void test3() {
        assertAll(() -> assertEquals("failure 1", "Correct"),
                () -> assertEquals("failure 2", "Correct"),
                () -> assertEquals("Correct", "Correct"));
    }

    @Nested
    @DisplayName("Nested also supports display name")
    class TestGrouping {

        @Test
        @DisplayName("Happy Test")
        void GroupTest() {
            // Happy test
        }

        @Nested
        @DisplayName("Nesting deeper also works")
        class TestInnerGrouping {
            @Test
            @DisplayName("MOAR NESTING")
            void test() {
                fail(() -> "Lazy strings");
                // Most (all?) things support a Supplier<String>
            }
        }
    }

    // JUnit 4 way: @Test(expected = NumberFormatException.class)
    // The disadvantage here is that it can only handle one exception type
    @Test
    @DisplayName("Expects NFE")
    void test4() {
        assertThrows(NumberFormatException.class, () -> Integer.parseInt("Not a number"));
        assertThrows(NumberFormatException.class, () -> Integer.parseInt("42"));
    }

    @Test
    @DisplayName("Lazy messages")
    void test5() {
        // Lazy messages, in case generating the message takes a while or something
        assertEquals(1, 0, () -> "Lazily evaluated failure message: " + new Random().nextInt());
    }

    @Test
    @DisplayName("Check exception details")
    void test6() {
        NumberFormatException e = expectThrows(NumberFormatException.class, () -> Integer.parseInt("Still not a number"));
        assertEquals("For input string: \"Still not a number\"", e.getMessage());
    }

    // @TestFactory replaces (and expands) the @Theory/@DataPoint mechanism
    @TestFactory
    @DisplayName("Dynamic Tests")
    Stream<DynamicTest> dynamicTests() {
        return Stream.of(1, 2, 3, 4, 5)
                .map(i -> DynamicTest.dynamicTest("Testing int " + i, () -> integerTest(i)));
    }

    void integerTest(int i) {
        // Note that we aren't using assertThat from JUnit. Dependency on hamcrest was removed from junit itself,
        // to prevent the dependency hell situation that sometimes arose if you wanted to include another version
        // of hamcrest. We can still use hamcrest ourself, however, by explicitely importing it from the pom.
        // Actually removed in JUnit 4.12
        org.hamcrest.MatcherAssert.assertThat(i, is(lessThan(5)));
    }

    @Test
    @DisplayName("Injected parameter")
    // In order to fully support this, we are using the IntegerParameterResolver, and the @ExtendWith annotation on this class
    void testWithParameter(int i) {
        assertEquals(1, i);
    }

    static class IntegerParameterResolver implements ParameterResolver {

        @Override
        public boolean supports(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            return parameterContext.getParameter().getType() == int.class;
        }

        @Override
        public Object resolve(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            return 1;
        }

    }

    @Test
    @DisplayName("TestInfo parameter")
    void testInfo(ExtensionContext ec) {
        assertEquals("testInfo", ec.getTestMethod().get().getName());
        assertEquals("TestInfo parameter", ec.getDisplayName());
        // Lots of interesting information here
        System.out.println(ec);
    }

    static class ExtensionContextParameterResolver implements ParameterResolver {
        @Override
        public boolean supports(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            return parameterContext.getParameter().getType() == ExtensionContext.class;
        }

        @Override
        public Object resolve(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            return extensionContext;
        }
    }

    // Note that the way that we are doing IntegerParamaterResolver works with lots of other things too, there are several
    // "Extension Points" in junit 5 that allow you to do all sorts of other neat things, including at minimum:
    // Test Instance Post Processor
    // BeforeAll Callback
    // Test and Container Execution Condition
    // BeforeEach Callback
    // Parameter Resolution
    // Before Test Execution
    // After Test Execution
    // Exception Handling
    // AfterEach Callback
    // AfterAll Callback

    // Could use TestInfo in one of these extension points to do neat global test things

    // Questions?



}
