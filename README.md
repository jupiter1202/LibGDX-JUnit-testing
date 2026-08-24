# libGDX JUnit Testing

This repository is a small reference project for testing a multi-module
[libGDX](https://libgdx.com/) application with JUnit and Mockito. It demonstrates how to:

- write fast, platform-independent unit tests in `core`;
- mock platform boundaries with Mockito;
- test code that uses the global `Gdx.*` services with libGDX's headless backend; and
- test launcher-specific behavior in the platform module that owns it.

The project contains deliberately small examples rather than a complete game. `PlayerTest` covers
ordinary domain logic, `MockitoTest` demonstrates Mockito's JUnit integration, `HeadlessGdxTest`
uses the custom `@GdxTest` environment, and `Lwjgl3LauncherTest` verifies desktop configuration
without opening a window.

## Versions

| Component | Version |
| --- | --- |
| libGDX | 1.14.2 |
| Java | 25 |
| JUnit | 6.1.1 |
| Mockito | 5.23.0 |
| Gradle wrapper | 9.6.1 |

The Gradle build compiles both production and test sources for Java 25, so use JDK 25 to build and
run this project.

## Setup

The complete guide for adding this testing setup to a multi-module Gradle/libGDX project is in
[Setup.MD](Setup.MD). It explains the shared JUnit and Mockito dependencies, the Mockito Java
agent, module-specific test dependencies, test source layout, and headless libGDX configuration.

## Writing tests

Place a test in the module whose production code it verifies, using Gradle's standard layout:

```text
<module>/src/test/java/       Java test classes
<module>/src/test/resources/  Test-only files
```

Use the following guidelines:

- Put game rules, domain logic, screens, and other platform-independent tests in `core`.
- Keep tests that do not access `Gdx.*` as plain JUnit tests. These are the smallest and fastest
  tests and do not need `@GdxTest`.
- Use Mockito for collaborators and platform boundaries. Prefer mocking a narrow interface rather
  than static libGDX state.
- Use `@GdxTest` only when the code under test needs services such as `Gdx.app`, `Gdx.files`,
  `Gdx.graphics`, or `Gdx.gl20`.
- Put launcher, lifecycle, filesystem-path, and other platform-specific tests in `lwjgl3`,
  `headless`, or the relevant platform module. Do not duplicate the `core` test suite there.

A plain JUnit test needs only `@Test` and assertions:

```java
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ScoreTest {
    @Test
    void addsEarnedPoints() {
        Score score = new Score();

        score.add(10);

        assertEquals(10, score.getPoints());
    }
}
```

For Mockito's annotation support, register `MockitoExtension` on the test class:

```java
@ExtendWith(MockitoExtension.class)
class AudioServiceTest {
    @Mock
    private Sound sound;

    @Test
    void playsTheSound() {
        sound.play();

        verify(sound).play();
    }
}
```

See the repository's executable examples in
[`core/src/test/java`](core/src/test/java/de/jupiter1202/test) and
[`lwjgl3/src/test/java`](lwjgl3/src/test/java/de/jupiter1202/test/lwjgl3).

## The `@GdxTest` annotation

`@GdxTest` is a class-level annotation for tests that require libGDX's global environment. It is
defined in
[`core/src/test/java/de/jupiter1202/test/support/GdxTest.java`](core/src/test/java/de/jupiter1202/test/support/GdxTest.java)
and combines two JUnit features:

- `@ExtendWith(HeadlessGdxExtension.class)` starts and stops the test environment.
- `@ResourceLock` prevents headless test classes from concurrently modifying the static `Gdx.*`
  fields if JUnit parallel execution is enabled.

Before each annotated test class, the extension starts a fresh `HeadlessApplication`, waits for it
to initialize, and assigns a Mockito mock to `Gdx.gl` and `Gdx.gl20`. After all tests in the class,
it stops the application and clears the global libGDX state so it cannot leak into another test.

Use it like this:

```java
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Gdx;
import de.jupiter1202.test.support.GdxTest;
import org.junit.jupiter.api.Test;

@GdxTest
class AssetLoadingTest {
    @Test
    void loadsAClasspathFixture() {
        assertTrue(Gdx.files.classpath("fixtures/map.tmx").exists());
    }
}
```

Put classpath fixtures in `core/src/test/resources`; the path passed to
`Gdx.files.classpath(...)` is relative to that directory.

The headless backend does not create a real window, audio device, input device, or OpenGL context.
The installed `GL20` object is only a mock. Tests that depend on actual rendering or window
behavior belong in a focused platform integration test.

The annotation and extension currently live in `core`'s test sources and are therefore available
only to `core` tests. If several modules need them, move the two support classes into a dedicated
test-support module and add that module as a `testImplementation` dependency.

## Running the tests

Run every module's tests from the repository root:

```shell
# Windows (PowerShell or Command Prompt)
gradlew.bat test

# macOS or Linux
./gradlew test
```

Run a single module or test class with its Gradle test task:

```shell
# Replace ./gradlew with gradlew.bat on Windows
./gradlew :core:test
./gradlew :lwjgl3:test
./gradlew :core:test --tests "de.jupiter1202.test.HeadlessGdxTest"
```

Gradle writes the HTML reports to `<module>/build/reports/tests/test/index.html`. The tests can
also be run directly from an IDE with JUnit Platform support.

## Documentation

- [JUnit 6.1.1 User Guide](https://docs.junit.org/6.1.1/user-guide/)
- [Mockito 5.23.0 documentation](https://javadoc.io/doc/org.mockito/mockito-core/5.23.0/org.mockito/org/mockito/Mockito.html)

## Inspired by

The headless libGDX testing approach was inspired by
[TomGrill/gdx-testing](https://github.com/TomGrill/gdx-testing).
