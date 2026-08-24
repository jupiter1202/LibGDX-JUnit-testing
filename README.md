# test

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

This project was generated with a template including simple application launchers and a main class extending `Game` that sets the first screen.

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.
- `headless`: Desktop platform without a graphical interface.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `headless:run`: starts the headless application. Note: if headless sources were not modified - and the application still creates `ApplicationListener` from `core` project - this task might fail due to no graphics support.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.

## Tests

The shared Gradle build configures JUnit 6 and Mockito for every JVM module. Run the complete test
suite with:

```shell
./gradlew test
```

On Windows, use `gradlew.bat test`. Tests belong to the module whose code they verify:

- Put platform-independent game and domain tests in `core/src/test/java`.
- Keep tests that do not touch `Gdx.*` as plain JUnit tests. Mock platform boundaries with narrow
  interfaces implemented by `lwjgl3`, `android`, or another launcher module.
- Annotate a test class with `@GdxTest` when it needs `Gdx.files`, `Gdx.graphics`, or other global
  libGDX services. The annotation starts the headless backend, supplies a mocked `GL20`, prevents
  concurrent access to the static `Gdx.*` state, and cleans that state after the class.
- Put only launcher, lifecycle, permissions, filesystem-path, and other platform integration tests
  in a platform module. Do not repeat the `core` suite there.
- Put test-only resources in the module's `src/test/resources` directory. Headless tests can load
  them with `Gdx.files.classpath(...)`.

Example:

```java
@GdxTest
class AssetLoadingTest {
    @Test
    void loadsFixture() {
        assertTrue(Gdx.files.classpath("fixtures/map.tmx").exists());
    }
}
```

`GdxTest` and `HeadlessGdxExtension` currently live in `core/src/test/java/.../support`, because
`core` is their only consumer. If a second module needs the same helpers, move them unchanged into
a small `test-support` Java-library module and add `testImplementation project(':test-support')`
to each consumer. Production platform services should stay behind interfaces declared in `core`;
inject a fake or Mockito mock in core tests and bind the real implementation only in the
corresponding launcher module. Headless tests do not provide a real OpenGL context; rendering and
window behavior require a focused platform integration test instead.
