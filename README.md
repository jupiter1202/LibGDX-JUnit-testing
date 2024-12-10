
# LibGDX-JUnit-Testing

This repository contains a template to test LibGDX projects with JUnit 5.11.3 and Mockito 5.5.0




## Installation

It is assumed that a LibGDX project has been set up

* Copy the test folder of the repo into your LibGDX root directory


* Add the following line to the settings.gradle
```gradle
  include 'test'
```

* Refresh the gradle dependencies


## Writing Tests

To write JUnit tests that use the LibGDX headless backend, you must annotate the test class with:

```java
  @ExtendWith(GdxExtension.class)
  public class TestClass{
    @Test
    public void test(){

    }
  }
```


## Running Tests

To run tests, run the following command

```bash
  ./gradlew test
```


## Inspired By

[https://github.com/TomGrill/gdx-testing](https://github.com/TomGrill/gdx-testing)

