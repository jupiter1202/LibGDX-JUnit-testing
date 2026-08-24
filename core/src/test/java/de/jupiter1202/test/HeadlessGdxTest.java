package de.jupiter1202.test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import de.jupiter1202.test.support.GdxTest;
import org.junit.jupiter.api.Test;

@GdxTest
class HeadlessGdxTest {
    @Test
    void suppliesLibGdxServicesAndTestResources() {
        assertAll(
            () -> assertEquals(ApplicationType.HeadlessDesktop, Gdx.app.getType()),
            () -> assertNotNull(Gdx.files),
            () -> assertNotNull(Gdx.graphics),
            () -> assertNotNull(Gdx.gl20),
            () -> assertTrue(Gdx.files.classpath("fixtures/headless.txt").exists())
        );
    }
}
