package de.jupiter1202.tests;

import static org.mockito.Mockito.mock;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import org.junit.jupiter.api.extension.Extension;

/**
 * This class is a JUnit extension to start a headless application so that unit tests can access
 * LibGdx functions. To do this, the annotation @ExtendWith(GdxExtension.class) must be placed
 * before the class declaration in each test class.
 *
 * @author Florian Möschler
 */
public class GdxExtension extends ApplicationAdapter implements Extension {

  /**
   * Creates a new Headless application.
   */
  public GdxExtension() {
    HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
    new HeadlessApplication(this, config);
    Gdx.gl = mock(GL20.class);
  }
}
