package de.jupiter1202.tests;


import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Gdx;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GdxExtension.class)
public class ExampleTest {

  @Test
  public void test(){
    assertTrue(Gdx.files.internal("testasset.txt").exists());
  }

}
