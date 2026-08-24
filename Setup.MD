# Add JUnit 6 and Mockito to a multi-module Gradle project

This guide adds JUnit Jupiter from JUnit 6 and Mockito to an existing multi-module Java project
using the Gradle Groovy DSL. It assumes a libGDX-style layout with modules such as `core`,
`lwjgl3`, and `headless`, but the same setup works for additional JVM modules.

Testing is configured once in the root build. Every current and future JVM subproject then receives
the same JUnit platform, Mockito dependencies, Java agent, and Gradle test settings. Each module
still owns its test code and platform-specific test dependencies.

## Resulting layout

```text
project-root/
  build.gradle
  gradle.properties
  settings.gradle
  core/
    build.gradle
    src/main/java/
    src/test/java/
    src/test/resources/
  lwjgl3/
    build.gradle
    src/main/java/
    src/test/java/
    src/test/resources/
  headless/
    build.gradle
    src/main/java/
    src/test/java/
  another-module/
    build.gradle
    src/main/java/
    src/test/java/
```

JUnit 6 requires Java 17 or newer. This repository uses JDK 25, JUnit 6.1.1, and Mockito 5.23.0.

## Step 1: Register the modules

If you created your project with `gdx-liftoff` this step is probably already completed.
Every module must be included in the root `settings.gradle`:

```groovy
include 'core', 'lwjgl3', 'headless'
```

Add future modules to the same file:

```groovy
include 'core', 'lwjgl3', 'headless', 'tools'
```

Each JVM module must apply `java`, `java-library`, or a plugin such as `application` that applies
the Java plugin. This project applies `java-library` to every subproject from the root build.

## Step 2: Declare shared versions

Add the versions once in the root `gradle.properties`:

```properties
junitVersion=6.1.1
mockitoVersion=5.23.0
```

These properties are available to every subproject. Keep all JUnit artifacts aligned through the
JUnit BOM instead of assigning versions to individual Platform and Jupiter dependencies.

## Step 3: Configure every JVM module from the root

Open the root `build.gradle`. If every subproject is a JVM module, use
`configure(subprojects)`.
Add this next to the rest of your dependencies and configurations:

```groovy
configure(subprojects) {
  apply plugin: 'java-library'

  repositories {
    mavenCentral()
  }

  configurations {
    mockitoAgent {
      canBeConsumed = false
      canBeResolved = true
    }
  }

  dependencies {
    testImplementation platform("org.junit:junit-bom:$junitVersion")
    testImplementation 'org.junit.jupiter:junit-jupiter'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'

    testImplementation "org.mockito:mockito-core:$mockitoVersion"
    testImplementation "org.mockito:mockito-junit-jupiter:$mockitoVersion"
    mockitoAgent("org.mockito:mockito-core:$mockitoVersion") {
      transitive = false
    }
  }

  def mockitoAgentClasspath = configurations.mockitoAgent

  tasks.withType(Test).configureEach {
    useJUnitPlatform()
    jvmArgumentProviders.add({
      ["-javaagent:${mockitoAgentClasspath.singleFile.absolutePath}"]
    } as org.gradle.process.CommandLineArgumentProvider)

    testLogging {
      events 'passed', 'skipped', 'failed'
      exceptionFormat = 'full'
    }
  }
}
```

This single block configures `core:test`, `lwjgl3:test`, `headless:test`, and the test task of
every other subproject.

- `junit-bom` keeps the JUnit Platform and Jupiter artifacts compatible.
- `junit-jupiter` supplies the Jupiter test API and engine.
- `junit-platform-launcher` aligns Gradle and IDE test discovery with the JUnit version.
- `mockito-core` supplies mocking, stubbing, and verification.
- `mockito-junit-jupiter` supplies `MockitoExtension` and support for `@Mock`.
- `mockitoAgent` resolves the Mockito JAR for explicit attachment to each test JVM.

The Java-agent setup prevents Mockito from relying on dynamic self-attachment on modern JDKs.


## Step 4: Keep module dependencies in their owning modules

The root block contains the shared test-framework dependencies. Production dependencies remain in
each module's own `build.gradle`.

For example, `lwjgl3/build.gradle` depends on `core`:

```groovy
dependencies {
  implementation project(':core')
  implementation "com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion"
  implementation "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop"
}
```

Tests in `lwjgl3` can access the module's production classes and public classes from `core`.
Do not repeat JUnit and Mockito in `lwjgl3/build.gradle`.

Add a dependency directly to a module only when its tests uniquely need it:

```groovy
dependencies {
  testImplementation 'com.example:desktop-test-helper:1.0.0'
  testRuntimeOnly 'com.example:desktop-test-runtime:1.0.0'
}
```

These could be dependencies for `core` if you need to access `Gdx.files` or `Gdx.graphics` or in general `Gdx.*`.
For more information look at [step 10](#step-10-configure-libgdx-headless-tests-in-core).
```groovy
dependencies {
    // Core tests that touch Gdx.* run against the display-free backend.
    testImplementation "com.badlogicgames.gdx:gdx-backend-headless:$gdxVersion"
    testRuntimeOnly "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop"
    testRuntimeOnly "com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-desktop"
    testRuntimeOnly "com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-desktop"
}
```


## Step 5: Put each test in the module it verifies

Every module uses the standard Gradle test layout:

```text
<module>/src/test/java/
<module>/src/test/resources/
```

Use this division of responsibility:

| Module | Test responsibility |
| --- | --- |
| `core` | Game rules, domain logic, screens, services, and platform-independent behavior |
| `lwjgl3` | Desktop launcher logic, LWJGL3 configuration, desktop paths, and adapters |
| `headless` | Headless launcher wiring and behavior owned by that module |
| Other JVM modules | Production code and integration boundaries owned by that module |

Do not copy core tests into platform modules. Test shared behavior once in `core), then add focused
tests for behavior that differs by platform.

## Step 6: Add a JUnit and Mockito test to core
The Following tests show just the basics of JUnit and Mockito and are not meant to be usefull.
This also counts for the other example tests in the following steps.


Create `core/src/test/java/de/jupiter1202/test/MockitoTest.java`:

```java
package de.jupiter1202.test;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MockitoTest {
    @Mock
    private Runnable platformPort;

    @Test
    void delegatesToThePlatformPort() {
        platformPort.run();

        verify(platformPort).run();
    }
}
```

Only classes that use Mockito annotations need `@ExtendWith(MockitoExtension.class)`. Plain JUnit
tests can use `@Test` without the extension.

## Step 7: Add a test to lwjgl3

Create:

```text
lwjgl3/src/test/java/de/jupiter1202/test/lwjgl3/Lwjgl3LauncherTest.java
```

```java
package de.jupiter1202.test.lwjgl3;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class Lwjgl3LauncherTest {
    @Test
    void launcherClassIsAvailable() {
        assertNotNull(Lwjgl3Launcher.class);
    }
}
```

Keep ordinary unit tests display-free. Code that opens a GLFW window or creates a real OpenGL
context is a platform integration test and may require a display, OS-specific natives, and
dedicated CI configuration.

## Step 8: Add tests to headless or another module

For `headless`, create test classes below:

```text
headless/src/test/java/de/jupiter1202/test/headless/
```

No JUnit or Mockito dependency changes are required because the root configuration already supplies
them.

For any other existing JVM module:

1. Confirm that it is included in `settings.gradle`.
2. Confirm that a Java-capable plugin is applied.
3. Confirm that the root test configuration includes the module.
4. Create `<module>/src/test/java`.
5. Add only module-specific test dependencies to `<module>/build.gradle`.
6. Run `.\gradlew.bat :<module>:test`.

## Step 9: Add a new JVM module

For a new module named `tools`:

1. Add `include 'tools'` to `settings.gradle`.
2. Create `tools/build.gradle`:

   ```groovy
   plugins {
     id 'java-library'
   }

   dependencies {
     implementation project(':core')
   }
   ```

3. Create `tools/src/main/java` and `tools/src/test/java`.
4. If the root uses `configure(subprojects)`, no testing configuration is needed in `tools`.
5. If the root uses an allowlist, add `tools` to it.
6. Run `.\gradlew.bat :tools:test`.

## Step 10: Configure libGDX headless tests in core

Plain domain tests should not start a libGDX application. When a `core` test needs `Gdx.files`,
`Gdx.graphics`, Box2D, or FreeType, add these module-specific dependencies to
`core/build.gradle`:

```groovy
dependencies {
  testImplementation "com.badlogicgames.gdx:gdx-backend-headless:$gdxVersion"
  testRuntimeOnly "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop"
  testRuntimeOnly "com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-desktop"
  testRuntimeOnly "com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-desktop"
}
```

This repository provides `@GdxTest` and `HeadlessGdxExtension` in the `core` test sources:

```java
package de.jupiter1202.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Gdx;
import de.jupiter1202.test.support.GdxTest;
import org.junit.jupiter.api.Test;

@GdxTest
class AssetLoadingTest {
    @Test
    void loadsAClasspathFixture() {
        assertTrue(Gdx.files.classpath("fixtures/headless.txt").exists());
    }
}
```

The extension starts the headless backend, supplies a mocked `GL20`, serializes access to static
`Gdx.*` state, and cleans that state after the class. It does not provide a real OpenGL context.

If multiple modules need the same test helpers, move them into a `test-support` Java-library module
and add it to `settings.gradle`. Because reusable helpers live in that module's main source set,
declare the libraries used by those helpers as regular `implementation` or `api` dependencies in
`test-support/build.gradle`:

```groovy
plugins {
  id 'java-library'
}

dependencies {
  implementation platform("org.junit:junit-bom:$junitVersion")
  implementation 'org.junit.jupiter:junit-jupiter-api'
  implementation "org.mockito:mockito-core:$mockitoVersion"
  implementation "com.badlogicgames.gdx:gdx-backend-headless:$gdxVersion"
}
```

Then add the helper module to each consuming module's test classpath:

```groovy
dependencies {
  testImplementation project(':test-support')
}
```

Keep reusable helpers in `test-support/src/main/java` so they are visible to consuming test source
sets.

## Step 11: Run tests across the project

Run every module's tests from the repository root.

On Windows:

```powershell
.\gradlew.bat test
```

On Linux or macOS:

```shell
./gradlew test
```

Run individual modules:

```powershell
.\gradlew.bat :core:test
.\gradlew.bat :lwjgl3:test
.\gradlew.bat :headless:test
```

Run one test class:

```powershell
.\gradlew.bat :core:test --tests "de.jupiter1202.test.MockitoTest"
.\gradlew.bat :lwjgl3:test --tests "de.jupiter1202.test.lwjgl3.Lwjgl3LauncherTest"
```

A successful setup ends with `BUILD SUCCESSFUL`. Each module writes its HTML report to:

```text
<module>/build/reports/tests/test/index.html
```

## Step 12: Configure IntelliJ IDEA

Delegate test execution to Gradle so every module uses the shared Mockito agent:

1. Open **Settings > Build, Execution, Deployment > Build Tools > Gradle**.
2. Set **Run tests using** to **Gradle**.
3. Reload the Gradle project.
4. Run tests from `core` and `lwjgl3` using their gutter icons.

## Android modules

Android local JVM tests live under `src/test/java` and follow the Android Gradle plugin's test
configuration. Instrumented device tests live under `src/androidTest/java` and require an Android
test runner. They commonly use `mockito-android`, not the desktop Mockito Java agent.

Keep Android modules out of a root block that unconditionally applies `java-library` or attaches
the desktop Mockito agent.

## Troubleshooting

- **One module finds no tests:** check `<module>/src/test/java`, `settings.gradle`, and whether its
  `Test` task receives `useJUnitPlatform()`.
- **JUnit works in core but not lwjgl3:** confirm that the root block targets both modules and that
  `lwjgl3` applies a Java-capable plugin.
- **Mockito annotations are null:** add `@ExtendWith(MockitoExtension.class)`.
- **Mockito reports dynamic agent loading:** create `mockitoAgent` and the JVM argument inside each
  configured subproject.
- **A test cannot see another module:** add the correct project dependency, such as
  `implementation project(':core')` or `testImplementation project(':test-support')`.
- **`Gdx.*` is null:** use the headless extension or inject and mock the platform dependency.
- **A native cannot be loaded:** add the matching native artifact to the owning module with
  `testRuntimeOnly` and keep it aligned with `gdxVersion`.
- **Dependencies do not refresh:** run
  `.\gradlew.bat test --refresh-dependencies` and reload the Gradle project.

## References

- [JUnit 6 User Guide](https://docs.junit.org/current/user-guide/)
- [JUnit Gradle build support](https://docs.junit.org/current/user-guide/running-tests/build-support.html)
- [Mockito documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html)
- [Mockito JUnit Jupiter API](https://javadoc.io/doc/org.mockito/mockito-junit-jupiter/latest/)
